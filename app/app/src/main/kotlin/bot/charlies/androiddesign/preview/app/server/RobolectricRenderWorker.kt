package bot.charlies.androiddesign.preview.app.server

import bot.charlies.androiddesign.preview.app.android.AndroidExplorationCompiler
import bot.charlies.androiddesign.preview.domain.bootstrap.BootstrapMeta
import bot.charlies.androiddesign.preview.model.DeviceRenderTarget
import bot.charlies.androiddesign.preview.model.FrameFormat
import bot.charlies.androiddesign.preview.model.PreviewJson
import bot.charlies.androiddesign.preview.model.RobolectricRenderRequest
import bot.charlies.androiddesign.preview.model.RobolectricRenderResponse
import bot.charlies.androiddesign.preview.model.ThemeOverride
import bot.charlies.androiddesign.preview.model.ValidationError
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong

/**
 * Headless AndroidX + Robolectric Native Graphics worker backed by one persistent child JVM
 * (`@robolectric.cp` -> `RobolectricRunnerMain`) communicating via single-line JSON over stdio.
 */
class RobolectricRenderWorker
private constructor(
  private val meta: BootstrapMeta?,
  private val robolectricCp: Path?,
  private val bootstrapDir: Path?,
  private val requestExecutor: ((RobolectricRenderRequest) -> RobolectricRenderResponse)?,
  commandOverride: List<String>?,
  private val classStager: (Path, Path?) -> AndroidExplorationCompiler.ClassStageOutcome,
  private val renderTimeoutMs: Long,
  private val controlTimeoutMs: Long,
) : RenderWorker {
  /**
   * [classStager] compiles an exploration to the class stage the runner loads; the default is the
   * device path's own compiler. [renderTimeoutMs] bounds a render request like the device batch
   * watchdog, and [controlTimeoutMs] bounds `ping` and `compile`, which include a cold start.
   */
  constructor(
    meta: BootstrapMeta? = null,
    robolectricCp: Path? = null,
    bootstrapDir: Path? = meta?.bootstrapDir,
    requestExecutor: ((RobolectricRenderRequest) -> RobolectricRenderResponse)? = null,
    classStager: ((Path, Path?) -> AndroidExplorationCompiler.ClassStageOutcome)? = null,
    renderTimeoutMs: Long = DEFAULT_RENDER_TIMEOUT_MS,
    controlTimeoutMs: Long = DEFAULT_CONTROL_TIMEOUT_MS,
  ) : this(
    meta,
    robolectricCp,
    bootstrapDir,
    requestExecutor,
    null,
    classStager
      ?: { source, design ->
        AndroidExplorationCompiler.compileClassStage(
          source,
          design,
          checkNotNull(meta) { "Robolectric class staging needs bootstrap metadata" },
        )
      },
    renderTimeoutMs,
    controlTimeoutMs,
  )

  internal constructor(
    workerCommand: List<String>,
    classStager: ((Path, Path?) -> AndroidExplorationCompiler.ClassStageOutcome)? = null,
    renderTimeoutMs: Long = DEFAULT_RENDER_TIMEOUT_MS,
    controlTimeoutMs: Long = DEFAULT_CONTROL_TIMEOUT_MS,
  ) : this(
    null,
    null,
    null,
    null,
    workerCommand,
    classStager ?: { _, _ -> error("Worker-command workers need an explicit class stager") },
    renderTimeoutMs,
    controlTimeoutMs,
  )

  override val name: String = RuntimeIdentity.BACKEND_ROBOLECTRIC
  override val supportsHeadlessSpeculativePrecompile: Boolean = false
  override val scopedCheckDensity: Int = 1

  private val requestIds = AtomicLong(0L)
  /** One stage per source file, like `RenderQueue`'s compile cache: a recompile replaces it. */
  private val stages = HashMap<Path, HashedStage>()

  private class HashedStage(val contentHash: String, val stage: RobolectricStage)

  private var stageRoot: Path? = null
  private var stageSequence = 0
  private val processClient =
    when {
      commandOverride != null -> RobolectricProcessClient(commandOverride = commandOverride)
      requestExecutor == null ->
        RobolectricProcessClient(
          robolectricCp =
            robolectricCp
              ?: meta?.bootstrapDir?.resolve("robolectric.cp")
              ?: error("Missing robolectric.cp path for RobolectricRenderWorker"),
          bootstrapDir =
            bootstrapDir
              ?: meta?.bootstrapDir
              ?: robolectricCp?.toAbsolutePath()?.normalize()?.parent
              ?: error("Missing bootstrap directory for RobolectricRenderWorker"),
        )
      else -> null
    }

  /** Eagerly starts the child JVM and initializes Robolectric SDK 35 before HTTP checks arrive. */
  fun warmUp() {
    val resp = execRequest(RobolectricRenderRequest(id = nextId(), mode = "ping"))
    check(resp.ok) { resp.error ?: "Robolectric warmup ping failed" }
  }

  /** Re-warms the worker after a crash or watchdog kill; a live worker returns immediately. */
  override fun prepare() {
    val client = processClient ?: return
    if (!client.isRunning()) warmUp()
  }

  override fun compile(snapshot: ContentHasher.Snapshot): WorkerCompileResult {
    val normSource = snapshot.sourcePath.toAbsolutePath().normalize()
    val stagedClasses =
      when (val outcome = classStager(normSource, snapshot.designPath)) {
        is AndroidExplorationCompiler.ClassStageOutcome.Failed ->
          return WorkerCompileResult.Failed(
            sourcePath = normSource,
            designPath = outcome.designPath,
            designTheme = outcome.designTheme,
            compileMs = outcome.compileMs,
            errors = outcome.errors,
          )
        is AndroidExplorationCompiler.ClassStageOutcome.Ready -> outcome.stage
      }
    val designRes = StagedDesign(stagedClasses.designPath, stagedClasses.designTheme)
    val staged = registerStage(normSource, snapshot.contentHash, stagedClasses)
    val resp =
      execRequest(
        staged.applyTo(
          RobolectricRenderRequest(
            id = nextId(),
            sourcePath = normSource.toString(),
            designPath = designRes.path?.toString(),
            mode = "compile",
          ),
        ),
      )
    val compileMs = stagedClasses.compileMs + resp.compileMs
    if (!resp.ok) {
      val msg = resp.error ?: "Robolectric compilation failed"
      return WorkerCompileResult.Failed(
        sourcePath = normSource,
        designPath = designRes.path,
        designTheme = designRes.theme,
        compileMs = compileMs,
        errors =
          resp.errors.ifEmpty { listOf(ValidationError(normSource.fileName.toString(), msg)) },
      )
    }
    val devMeta = resp.boardMeta
    if (!resp.valid || devMeta == null) {
      return WorkerCompileResult.Failed(
        sourcePath = normSource,
        designPath = designRes.path,
        designTheme = designRes.theme,
        compileMs = compileMs,
        errors = resp.errors,
        boardTheme = resp.boardTheme,
      )
    }
    return WorkerCompileResult.Ready(
      sourcePath = normSource,
      designPath = designRes.path,
      designTheme = designRes.theme,
      compileMs = compileMs,
      boardMeta = devMeta,
      boardTheme = resp.boardTheme,
    )
  }

  override fun renderTargets(
    entry: CompileEntry,
    targets: List<DeviceRenderTarget>,
    stageRoot: Path,
  ): WorkerBatchResult {
    val normalizedTargets = targets.map { t ->
      val wantFrame = t.frameFormat != FrameFormat.NONE
      t.copy(
        frameFormat = if (wantFrame) FrameFormat.LZ4_RGBA else FrameFormat.NONE,
        includeEndFrame = wantFrame && t.includeEndFrame,
      )
    }
    val wantAnyFrame = normalizedTargets.any { it.frameFormat != FrameFormat.NONE }
    val stagedOutputs: List<WorkerTargetOutput>
    val resp: RobolectricRenderResponse
    if (wantAnyFrame) {
      val (r, collected) =
        withTempStageDir(stageRoot) { stageDir ->
          val batchResp =
            renderRequest(
              entry,
              RobolectricRenderRequest(
                id = nextId(),
                sourcePath = entry.sourcePath.toString(),
                designPath = entry.loaded.designPath?.toString(),
                outDir = stageDir.toString(),
                targets = normalizedTargets,
              ),
            )
          val outputs =
            batchResp.results.map { res ->
              WorkerTargetOutput(
                direction = res.direction,
                screen = res.screen,
                viewName = res.viewName,
                overlayId = res.overlayId,
                renderMs = res.renderMs,
                tree = res.tree,
                endTree = res.endTree,
                frame = readStagedPngFrame(res.pngPath),
                endFrame = readStagedPngFrame(res.endPngPath),
                error = res.error,
              )
            }
          batchResp to outputs
        }
      resp = r
      stagedOutputs = collected
    } else {
      resp =
        renderRequest(
          entry,
          RobolectricRenderRequest(
            id = nextId(),
            sourcePath = entry.sourcePath.toString(),
            designPath = entry.loaded.designPath?.toString(),
            targets = normalizedTargets,
            encodePng = false,
          ),
        )
      stagedOutputs =
        resp.results.map { res ->
          WorkerTargetOutput(
            direction = res.direction,
            screen = res.screen,
            viewName = res.viewName,
            overlayId = res.overlayId,
            renderMs = res.renderMs,
            tree = res.tree,
            endTree = res.endTree,
            error = res.error,
          )
        }
    }
    return WorkerBatchResult(
      ok = resp.ok,
      renderTotalMs = resp.renderMs,
      error = resp.error,
      targets = stagedOutputs,
    )
  }

  override fun close() {
    processClient?.close()
    synchronized(stages) {
      stages.values.forEach { it.stage.delete() }
      stages.clear()
      stageRoot?.toFile()?.deleteRecursively()
      stageRoot = null
    }
  }

  private fun renderRequest(
    entry: CompileEntry,
    request: RobolectricRenderRequest,
  ): RobolectricRenderResponse {
    val staged =
      synchronized(stages) {
        stages[entry.sourcePath.toAbsolutePath().normalize()]?.takeIf {
          it.contentHash == entry.contentHash
        }
      } ?: error("No staged classes for ${entry.sourcePath}; compile it again")
    return execRequest(staged.stage.applyTo(request), renderTimeoutMs)
  }

  /** Runs [req] on the worker; a worker that outlives [timeoutMs] is killed and reported. */
  private fun execRequest(
    req: RobolectricRenderRequest,
    timeoutMs: Long = controlTimeoutMs,
  ): RobolectricRenderResponse {
    val resp =
      requestExecutor?.invoke(req) ?: processClient!!.request(req, timeoutMs, controlTimeoutMs)
    if (resp.recycleReason == WATCHDOG_TIMEOUT) {
      throw SandboxWatchdogException(resp.error ?: "Robolectric request timed out")
    }
    return resp
  }

  private fun registerStage(
    sourcePath: Path,
    contentHash: String,
    stage: AndroidExplorationCompiler.ClassStage,
  ): RobolectricStage {
    return synchronized(stages) {
      val root = stageRoot ?: Files.createTempDirectory("adp-robo-stage-").also { stageRoot = it }
      val dir = root.resolve("stage-${stageSequence++}")
      RobolectricStage.write(dir, stage).also { fresh ->
        stages.put(sourcePath, HashedStage(contentHash, fresh))?.stage?.delete()
      }
    }
  }

  private data class StagedDesign(
    val path: Path?,
    val theme: ThemeOverride?,
  )

  private fun nextId(): String = "rb-${requestIds.incrementAndGet()}"

  private inline fun <T> withTempStageDir(rendersRoot: Path, block: (Path) -> T): T {
    Files.createDirectories(rendersRoot)
    val dir = Files.createTempDirectory(rendersRoot, ".robo-stage-")
    return try {
      block(dir)
    } finally {
      dir.toFile().deleteRecursively()
    }
  }

  private fun readStagedPngFrame(path: String?): WorkerFrame? {
    if (path.isNullOrBlank()) return null
    val p = Path.of(path)
    if (!Files.isRegularFile(p)) return null
    val bytes = Files.readAllBytes(p).takeIf { it.isNotEmpty() } ?: return null
    return WorkerFrame(bytes = bytes, mime = "image/png", isPng = true)
  }

  companion object {
    const val DEFAULT_COLD_BUDGET_MS = 60_000L
    const val DEFAULT_WARM_BUDGET_MS = 30_000L

    /** Matches the device batch watchdog. */
    const val DEFAULT_RENDER_TIMEOUT_MS = 15_000L

    /** `ping` and `compile` may include a cold JVM and Robolectric SDK start. */
    const val DEFAULT_CONTROL_TIMEOUT_MS = DEFAULT_COLD_BUDGET_MS
    private const val WATCHDOG_TIMEOUT = "watchdog_timeout"
  }
}

private const val PENDING = 0
private const val ANSWERED = 1
private const val TIMED_OUT = 2

private class RobolectricProcessClient(
  private val robolectricCp: Path? = null,
  private val bootstrapDir: Path? = null,
  private val commandOverride: List<String>? = null,
) : AutoCloseable {
  private val requestLock = Any()
  private val stateLock = Any()
  private var process: Process? = null
  private var writer: BufferedWriter? = null
  private var reader: BufferedReader? = null
  private var closed = false
  private val stderrTail = ArrayDeque<String>(64)
  private val stderrLock = Any()
  private val watchdog = Executors.newSingleThreadScheduledExecutor { r ->
    Thread(r, "robolectric-worker-watchdog").apply { isDaemon = true }
  }

  private data class ProcessHandles(
    val process: Process?,
    val writer: BufferedWriter?,
    val reader: BufferedReader?,
  )

  /**
   * Sends [req] and waits at most [timeoutMs] for the reply. A worker that outlives the budget is
   * killed and the reply reports `watchdog_timeout`, like the device `:sandbox`; the next request
   * starts a fresh worker.
   */
  fun request(
    req: RobolectricRenderRequest,
    timeoutMs: Long,
    coldTimeoutMs: Long = RobolectricRenderWorker.DEFAULT_COLD_BUDGET_MS,
  ): RobolectricRenderResponse {
    synchronized(requestLock) {
      val started = ensureStarted()
      if (started.fresh && req.mode != "ping") {
        // A fresh worker pays JVM and Robolectric SDK warmup; keep that out of the request budget.
        val ping =
          exchange(
            started,
            RobolectricRenderRequest(id = "${req.id}-warmup", mode = "ping"),
            coldTimeoutMs,
          )
        if (!ping.ok) return ping
      }
      return exchange(started, req, timeoutMs)
    }
  }

  private fun exchange(
    started: StartedWorker,
    req: RobolectricRenderRequest,
    timeoutMs: Long,
  ): RobolectricRenderResponse {
    val (proc, w, r) = started
    val payload = PreviewJson.compact.encodeToString(RobolectricRenderRequest.serializer(), req)
    // The reply and the timer race for this flag, so a worker that answered is never killed.
    val outcome = AtomicInteger(PENDING)
    val timedOut = AtomicBoolean(false)
    val timer =
      watchdog.schedule(
        {
          if (!outcome.compareAndSet(PENDING, TIMED_OUT)) return@schedule
          timedOut.set(true)
          // Killing the process ends the blocked readLine(); never close the reader from here.
          runCatching { proc.descendants().forEach { it.destroyForcibly() } }
          runCatching { proc.destroyForcibly() }
        },
        timeoutMs,
        TimeUnit.MILLISECONDS,
      )
    try {
      w.write(payload)
      w.newLine()
      w.flush()
      while (true) {
        val line = r.readLine()
        if (line == null) {
          val wasClosed = synchronized(stateLock) { closed }
          val detached = synchronized(stateLock) { detachIfCurrentLocked(proc) }
          terminateDetached(detached, forceFirst = wasClosed)
          runCatching { w.close() }
          runCatching { r.close() }
          if (wasClosed) {
            throw IllegalStateException("RobolectricProcessClient is closed")
          }
          val exit = runCatching { proc.exitValue() }.getOrNull()
          val tail = recentStderr()
          throw IllegalStateException(
            "Robolectric worker exited unexpectedly (code=${exit ?: "?"}): $tail",
          )
        }
        val trimmed = line.trim()
        if (trimmed.isEmpty()) continue
        val parsed =
          PreviewJson.compact.decodeFromString(RobolectricRenderResponse.serializer(), trimmed)
        if (!outcome.compareAndSet(PENDING, ANSWERED)) {
          // The timer won the flag and is killing this worker, so report the timeout.
          throw IllegalStateException("Robolectric worker killed at the request budget")
        }
        return parsed
      }
    } catch (e: Exception) {
      val wasClosed = synchronized(stateLock) { closed }
      val detached = synchronized(stateLock) { detachIfCurrentLocked(proc) }
      terminateDetached(detached, forceFirst = wasClosed || timedOut.get())
      runCatching { w.close() }
      runCatching { r.close() }
      if (wasClosed) {
        throw IllegalStateException("RobolectricProcessClient is closed", e)
      }
      if (timedOut.get()) {
        return RobolectricRenderResponse(
          id = req.id,
          ok = false,
          valid = false,
          pass = false,
          error = "Robolectric request timed out after $timeoutMs ms; worker restarted",
          recycleReason = "watchdog_timeout",
        )
      }
      if (e is IllegalStateException && detached.process == null) throw e
      val tail = recentStderr()
      throw IllegalStateException(
        "Robolectric worker request failed: ${e.message}${if (tail.isNotBlank()) "\nstderr:\n$tail" else ""}",
        e,
      )
    } finally {
      timer.cancel(false)
    }
  }

  override fun close() {
    val detached =
      synchronized(stateLock) {
        if (closed) return
        closed = true
        detachAllLocked()
      }
    terminateDetached(detached, forceFirst = true)
    watchdog.shutdownNow()
  }

  private data class StartedWorker(
    val process: Process,
    val writer: BufferedWriter,
    val reader: BufferedReader,
    val fresh: Boolean,
  )

  /**
   * True when a worker process is alive and attached; false before the first start or after a kill.
   */
  fun isRunning(): Boolean =
    synchronized(stateLock) {
      val p = process
      !closed && p != null && p.isAlive && writer != null && reader != null
    }

  private fun ensureStarted(): StartedWorker {
    synchronized(stateLock) {
      check(!closed) { "RobolectricProcessClient is closed" }
      val p = process
      val w = writer
      val r = reader
      if (p != null && p.isAlive && w != null && r != null) {
        return StartedWorker(p, w, r, fresh = false)
      }
    }
    val stale = synchronized(stateLock) { detachAllLocked() }
    terminateDetached(stale, forceFirst = false)

    val cmd =
      commandOverride
        ?: run {
          val rcp = checkNotNull(robolectricCp)
          val bdir = checkNotNull(bootstrapDir)
          require(Files.isRegularFile(rcp)) {
            "Missing robolectric.cp at $rcp; run `./gradlew :host:exportBootstrap` from app/"
          }
          listOf(
            resolveJavaBinary(),
            "-Xmx3072m",
            "-Djava.awt.headless=true",
            "-Dadp.bootstrap.dir=${bdir.toAbsolutePath().normalize()}",
            "@${rcp.toAbsolutePath().normalize()}",
            "bot.charlies.androiddesign.preview.host.robolectric.RobolectricRunnerMain",
          )
        }
    synchronized(stderrLock) { stderrTail.clear() }
    val proc = ProcessBuilder(cmd).start()
    val w = BufferedWriter(OutputStreamWriter(proc.outputStream, StandardCharsets.UTF_8))
    val r = BufferedReader(InputStreamReader(proc.inputStream, StandardCharsets.UTF_8))
    val errStream = proc.errorStream

    val installed =
      synchronized(stateLock) {
        if (closed) {
          false
        } else {
          process = proc
          writer = w
          reader = r
          true
        }
      }
    if (!installed) {
      terminateDetached(ProcessHandles(proc, w, r), forceFirst = true)
      runCatching { errStream.close() }
      throw IllegalStateException("RobolectricProcessClient is closed")
    }

    Thread(
        {
          try {
            BufferedReader(InputStreamReader(errStream, StandardCharsets.UTF_8)).use { errReader ->
              while (true) {
                val line = errReader.readLine() ?: break
                synchronized(stderrLock) {
                  if (stderrTail.size >= 64) stderrTail.removeFirst()
                  stderrTail.addLast(line)
                }
              }
            }
          } catch (_: Exception) {
            // Stream closes when worker exits.
          }
        },
        "robolectric-worker-stderr",
      )
      .apply {
        isDaemon = true
        start()
      }
    return StartedWorker(proc, w, r, fresh = true)
  }

  private fun recentStderr(): String = synchronized(stderrLock) { stderrTail.joinToString("\n") }

  private fun detachAllLocked(): ProcessHandles {
    val res = ProcessHandles(process, writer, reader)
    process = null
    writer = null
    reader = null
    return res
  }

  private fun detachIfCurrentLocked(expected: Process): ProcessHandles {
    if (process !== expected) return ProcessHandles(null, null, null)
    return detachAllLocked()
  }

  private fun terminateDetached(handles: ProcessHandles, forceFirst: Boolean) {
    val p = handles.process
    if (forceFirst) {
      // Never call BufferedReader.close()/BufferedWriter.close() from an external close() thread:
      // Java's BufferedReader synchronizes close() on the same internal lock held by readLine().
      if (p != null) {
        runCatching { p.descendants().forEach { it.destroyForcibly() } }
        runCatching { p.destroyForcibly() }
        runCatching { p.outputStream.close() }
        runCatching { p.inputStream.close() }
        runCatching { p.errorStream.close() }
        runCatching { p.waitFor(2, TimeUnit.SECONDS) }
      }
      return
    }
    if (p != null) {
      runCatching { p.outputStream.close() }
      runCatching {
        if (!p.waitFor(2, TimeUnit.SECONDS)) {
          p.descendants().forEach { it.destroyForcibly() }
          p.destroyForcibly()
          p.waitFor(2, TimeUnit.SECONDS)
        }
      }
      if (p.isAlive) {
        runCatching { p.descendants().forEach { it.destroyForcibly() } }
        runCatching { p.destroyForcibly() }
      }
      runCatching { p.inputStream.close() }
      runCatching { p.errorStream.close() }
    }
    runCatching { handles.writer?.close() }
    runCatching { handles.reader?.close() }
  }

  private fun resolveJavaBinary(): String {
    val override = System.getenv("ANDROID_DESIGN_PREVIEW_JAVA")?.takeIf { it.isNotBlank() }
    if (override != null && Files.isExecutable(Path.of(override))) return override
    val home = System.getProperty("java.home")
    if (!home.isNullOrBlank()) {
      val unix = Path.of(home, "bin", "java")
      if (Files.isExecutable(unix)) return unix.toString()
      val win = Path.of(home, "bin", "java.exe")
      if (Files.isRegularFile(win)) return win.toString()
    }
    return "java"
  }
}
