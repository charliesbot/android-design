package bot.charlies.androiddesign.preview.app.server

import bot.charlies.androiddesign.preview.app.android.AndroidExplorationCompiler
import bot.charlies.androiddesign.preview.domain.bootstrap.BootstrapMeta
import bot.charlies.androiddesign.preview.domain.image.FastPngEncoder
import bot.charlies.androiddesign.preview.model.ClassBundleCodec
import bot.charlies.androiddesign.preview.model.ValidationError
import bot.charlies.androiddesign.preview.model.WorkerBatchRequest
import bot.charlies.androiddesign.preview.model.WorkerBatchWireResult
import bot.charlies.androiddesign.preview.model.WorkerPipeCodec
import bot.charlies.androiddesign.preview.model.WorkerPreloadResponse
import bot.charlies.androiddesign.preview.model.WorkerRenderTarget
import bot.charlies.androiddesign.preview.model.WorkerWireFrame
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.nio.file.Files
import java.nio.file.Path
import java.util.ArrayDeque
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import java.util.concurrent.atomic.AtomicInteger
import kotlin.io.path.exists

/**
 * [RenderWorker] that runs a child JVM hosting layoutlib (`LayoutlibWorkerMainKt`) over stdin and
 * stdout using [WorkerPipeCodec] frames, with no Android device or D8 step.
 */
class LayoutlibRenderWorker(
  private val meta: BootstrapMeta,
  private val env: Map<String, String> = System.getenv(),
  private val batchTimeoutMs: Long = DEFAULT_BATCH_TIMEOUT_MS,
  private val controlTimeoutMs: Long = DEFAULT_CONTROL_TIMEOUT_MS,
  private val workerCommandFactory: (BootstrapMeta) -> List<String> = { m ->
    defaultWorkerCommand(m, env)
  },
) : RenderWorker {
  override val name: String = RuntimeIdentity.BACKEND_LAYOUTLIB

  private class WorkerProcess(
    val process: Process,
    val out: DataOutputStream,
    val inp: DataInputStream,
    val stderrLines: ArrayDeque<String>,
    val preloadedHashes: MutableSet<String> = HashSet(),
  ) {
    fun pid(): Long = process.pid()

    fun isAlive(): Boolean = process.isAlive

    fun tailSummary(): String =
      synchronized(stderrLines) { stderrLines.joinToString("\n") }.takeLast(1200)
  }

  private val lock = Any()
  private val ioThreadCounter = AtomicInteger(0)
  private val ioExecutor = Executors.newCachedThreadPool { r ->
    Thread(r, "layoutlib-worker-io-${ioThreadCounter.incrementAndGet()}").apply {
      isDaemon = true
    }
  }

  @Volatile private var activeProcess: WorkerProcess? = null
  @Volatile private var closed = false

  override fun prepare() {
    synchronized(lock) {
      check(!closed) { "LayoutlibRenderWorker is closed" }
      val proc = ensureWorkerAliveLocked()
      try {
        val ping =
          runWithTimeout(proc, controlTimeoutMs, "PING") {
            WorkerPipeCodec.writePingRequest(proc.out)
            WorkerPipeCodec.readPingResponse(proc.inp)
          }
        check(ping.ok) { "Layoutlib worker PING returned ok=false" }
      } catch (t: Throwable) {
        killAndClearLocked(proc)
        throw t
      }
    }
  }

  override fun compile(snapshot: ContentHasher.Snapshot): WorkerCompileResult {
    return when (
      val stageOutcome =
        AndroidExplorationCompiler.compileClassStage(
          sourcePath = snapshot.sourcePath,
          explicitDesign = snapshot.designPath,
          meta = meta,
          snapshot = snapshot,
        )
    ) {
      is AndroidExplorationCompiler.ClassStageOutcome.Failed ->
        WorkerCompileResult.Failed(
          sourcePath = snapshot.sourcePath,
          designPath = stageOutcome.designPath,
          designTheme = stageOutcome.designTheme,
          compileMs = stageOutcome.compileMs,
          errors = stageOutcome.errors,
        )
      is AndroidExplorationCompiler.ClassStageOutcome.Ready -> {
        val stage = stageOutcome.stage
        val bundle = ClassBundleCodec.encode(stage.classBytes)
        val classHash = ContentHasher.sha256Hex(bundle)
        val preload =
          try {
            preloadWithRetry(classHash, stage.entryClass, bundle)
          } catch (t: Throwable) {
            return WorkerCompileResult.Failed(
              sourcePath = stage.sourcePath,
              designPath = stage.designPath,
              designTheme = stage.designTheme,
              compileMs = stage.compileMs,
              errors =
                listOf(
                  ValidationError(
                    stage.sourceFile,
                    "Layoutlib preload failed: ${t.message ?: t::class.simpleName}",
                  ),
                ),
            )
          }

        val boardMeta = preload.boardMeta
        if (!preload.ok || boardMeta == null) {
          val errMsg = preload.error ?: "Layoutlib worker PRELOAD returned no board metadata"
          return WorkerCompileResult.Failed(
            sourcePath = stage.sourcePath,
            designPath = stage.designPath,
            designTheme = stage.designTheme,
            compileMs = stage.compileMs,
            errors = listOf(ValidationError(stage.sourceFile, errMsg)),
            boardTheme = preload.boardTheme,
          )
        }

        val compiledData =
          CompiledBoardData(
            sourceFile = stage.sourceFile,
            sourceText = stage.sourceText,
            entryClass = stage.entryClass,
            classBytes = stage.classBytes,
            classBundleBytes = bundle,
            classHash = classHash,
            explorationDir = stage.sourcePath.parent,
            assetMap = stage.assetMap,
            assetBlobs = stage.assetBlobs,
            boardTheme = preload.boardTheme,
          )
        WorkerCompileResult.Ready(
          sourcePath = stage.sourcePath,
          designPath = stage.designPath,
          designTheme = stage.designTheme,
          compileMs = stage.compileMs,
          boardMeta = boardMeta,
          boardTheme = preload.boardTheme,
          compiledData = compiledData,
        )
      }
    }
  }

  override fun renderTargets(
    entry: CompileEntry,
    targets: List<WorkerRenderTarget>,
    stageRoot: Path,
  ): WorkerBatchResult {
    val data =
      checkNotNull(entry.compiledData) { "Missing CompiledBoardData for ${entry.sourcePath}" }
    val request =
      WorkerBatchRequest(
        classHash = data.classHash,
        entryClass = data.entryClass,
        sourceFile = data.sourceFile,
        sourceText = data.sourceText,
        designTheme = entry.loaded.designTheme,
        targets = targets,
        timeoutMs = batchTimeoutMs,
        explorationDir = data.explorationDir?.toString(),
      )

    val batchWire =
      synchronized(lock) {
        if (closed) {
          return WorkerBatchResult(ok = false, error = "LayoutlibRenderWorker is closed")
        }
        val proc =
          try {
            ensureWorkerAliveLocked()
          } catch (t: Throwable) {
            return WorkerBatchResult(
              ok = false,
              error = "Failed spawning layoutlib worker: ${t.message ?: t::class.simpleName}",
            )
          }

        val initialWireBytes =
          if (data.classHash in proc.preloadedHashes) null else data.classBundleBytes
        var attempt = sendBatchOnceLocked(proc, initialWireBytes, data.entryClass, request)
        var result =
          when (attempt) {
            is BatchWireAttempt.Failed ->
              return WorkerBatchResult(ok = false, error = attempt.error)
            is BatchWireAttempt.Completed -> attempt.wire
          }
        if (
          !result.response.ok &&
            initialWireBytes == null &&
            "not preloaded" in result.response.error.orEmpty()
        ) {
          proc.preloadedHashes.remove(data.classHash)
          attempt = sendBatchOnceLocked(proc, data.classBundleBytes, data.entryClass, request)
          result =
            when (attempt) {
              is BatchWireAttempt.Failed ->
                return WorkerBatchResult(ok = false, error = attempt.error)
              is BatchWireAttempt.Completed -> attempt.wire
            }
        }

        if (result.response.ok) {
          proc.preloadedHashes.add(data.classHash)
        }
        if (result.response.recycleRecommended) {
          closeProcessLocked(proc)
          if (activeProcess === proc) activeProcess = null
          runCatching { ensureWorkerAliveLocked() }
        }
        result
      }

    val resp = batchWire.response
    val framesByKey = HashMap<FrameKey, WorkerWireFrame>(batchWire.frames.size * 2)
    for (frame in batchWire.frames) {
      framesByKey[
        FrameKey(
          frame.direction,
          frame.screen,
          frame.overlayId,
          frame.viewName,
          frame.isEndFrame,
        )] = frame
    }

    val outputs =
      resp.results.map { res ->
        val mainFrame =
          framesByKey[FrameKey(res.direction, res.screen, res.overlayId, res.viewName, false)]
            ?.toPngWorkerFrame()
        val endFrame =
          framesByKey[FrameKey(res.direction, res.screen, res.overlayId, res.viewName, true)]
            ?.toPngWorkerFrame()
        WorkerTargetOutput(
          direction = res.direction,
          screen = res.screen,
          viewName = res.viewName,
          overlayId = res.overlayId,
          renderMs = res.renderMs.toLong(),
          tree = res.tree,
          endTree = res.endTree,
          frame = mainFrame,
          endFrame = endFrame,
          error = res.error,
        )
      }

    return WorkerBatchResult(
      ok = resp.ok,
      renderTotalMs = resp.renderTotalMs.toLong(),
      error = resp.error,
      targets = outputs,
    )
  }

  override fun close() {
    synchronized(lock) {
      if (closed) return
      closed = true
      activeProcess?.let { proc ->
        closeProcessLocked(proc)
        activeProcess = null
      }
    }
    ioExecutor.shutdownNow()
  }

  internal fun currentWorkerPid(): Long? = activeProcess?.takeIf { it.isAlive() }?.pid()

  private sealed interface BatchWireAttempt {
    data class Completed(val wire: WorkerBatchWireResult) : BatchWireAttempt

    data class Failed(val error: String) : BatchWireAttempt
  }

  private fun sendBatchOnceLocked(
    proc: WorkerProcess,
    wireBytes: ByteArray?,
    entryClass: String,
    request: WorkerBatchRequest,
  ): BatchWireAttempt {
    return try {
      val res =
        runWithTimeout(proc, batchTimeoutMs, "RENDER_BATCH") {
          WorkerPipeCodec.writeRenderBatchRequest(proc.out, wireBytes, entryClass, request)
          WorkerPipeCodec.readRenderBatchResponse(proc.inp)
        }
      BatchWireAttempt.Completed(res)
    } catch (timeout: TimeoutException) {
      killAndClearLocked(proc)
      runCatching { ensureWorkerAliveLocked() }
      BatchWireAttempt.Failed(
        timeout.message ?: "Layoutlib render batch timed out after ${batchTimeoutMs}ms",
      )
    } catch (t: Throwable) {
      val tail = proc.tailSummary()
      killAndClearLocked(proc)
      val detail = buildString {
        append(t.message ?: t::class.simpleName ?: "Worker communication failed")
        if (tail.isNotBlank()) {
          append("\nworker stderr:\n")
          append(tail)
        }
      }
      BatchWireAttempt.Failed(detail)
    }
  }

  private fun preloadWithRetry(
    classHash: String,
    entryClass: String,
    bundle: ByteArray,
  ): WorkerPreloadResponse {
    synchronized(lock) {
      check(!closed) { "LayoutlibRenderWorker is closed" }
      var lastError: Throwable? = null
      for (attempt in 0..1) {
        val proc = ensureWorkerAliveLocked()
        try {
          val resp =
            runWithTimeout(proc, controlTimeoutMs, "PRELOAD") {
              WorkerPipeCodec.writePreloadClassesRequest(proc.out, classHash, entryClass, bundle)
              WorkerPipeCodec.readPreloadClassesResponse(proc.inp)
            }
          if (resp.ok) {
            proc.preloadedHashes.add(classHash)
          }
          return resp
        } catch (t: Throwable) {
          lastError = t
          killAndClearLocked(proc)
          if (attempt == 1) throw t
        }
      }
      throw checkNotNull(lastError)
    }
  }

  private fun ensureWorkerAliveLocked(): WorkerProcess {
    val existing = activeProcess
    if (existing != null && existing.isAlive()) {
      return existing
    }
    if (existing != null) {
      killAndClearLocked(existing)
    }
    val cmd = workerCommandFactory(meta)
    val pb = ProcessBuilder(cmd)
    val proc = pb.start()
    val stderrLines = ArrayDeque<String>(STDERR_TAIL_LIMIT)
    val stderrThread =
      Thread(
        {
          runCatching {
            proc.errorStream.bufferedReader(Charsets.UTF_8).use { reader ->
              while (true) {
                val line = reader.readLine() ?: break
                synchronized(stderrLines) {
                  if (stderrLines.size >= STDERR_TAIL_LIMIT) {
                    stderrLines.removeFirst()
                  }
                  stderrLines.addLast(line)
                }
              }
            }
          }
        },
        "layoutlib-worker-stderr-${proc.pid()}",
      )
    stderrThread.isDaemon = true
    stderrThread.start()

    val wrapped =
      WorkerProcess(
        process = proc,
        out = DataOutputStream(BufferedOutputStream(proc.outputStream, IO_BUFFER_BYTES)),
        inp = DataInputStream(BufferedInputStream(proc.inputStream, IO_BUFFER_BYTES)),
        stderrLines = stderrLines,
      )
    activeProcess = wrapped
    return wrapped
  }

  private fun <T> runWithTimeout(
    proc: WorkerProcess,
    timeoutMs: Long,
    opName: String,
    block: () -> T,
  ): T {
    val future = ioExecutor.submit(Callable { block() })
    return try {
      future.get(timeoutMs, TimeUnit.MILLISECONDS)
    } catch (e: TimeoutException) {
      future.cancel(true)
      val tail = proc.tailSummary()
      val msg = buildString {
        append("Layoutlib worker $opName timed out after ${timeoutMs}ms")
        if (tail.isNotBlank()) {
          append("\nworker stderr:\n")
          append(tail)
        }
      }
      throw TimeoutException(msg)
    } catch (e: java.util.concurrent.ExecutionException) {
      throw e.cause ?: e
    }
  }

  private fun closeProcessLocked(proc: WorkerProcess) {
    runCatching { proc.out.close() }
    val exited =
      try {
        proc.process.waitFor(GRACEFUL_EXIT_WAIT_MS, TimeUnit.MILLISECONDS)
      } catch (_: InterruptedException) {
        Thread.currentThread().interrupt()
        false
      }
    if (!exited && proc.process.isAlive) {
      proc.process.destroy()
      val terminated =
        try {
          proc.process.waitFor(500, TimeUnit.MILLISECONDS)
        } catch (_: InterruptedException) {
          Thread.currentThread().interrupt()
          false
        }
      if (!terminated && proc.process.isAlive) {
        proc.process.destroyForcibly()
        runCatching { proc.process.waitFor(500, TimeUnit.MILLISECONDS) }
      }
    }
    runCatching { proc.inp.close() }
  }

  private fun killAndClearLocked(proc: WorkerProcess) {
    if (activeProcess === proc) activeProcess = null
    if (proc.process.isAlive) {
      proc.process.destroyForcibly()
    }
    runCatching { proc.out.close() }
    runCatching { proc.inp.close() }
    runCatching { proc.process.waitFor(1_000, TimeUnit.MILLISECONDS) }
  }

  private fun WorkerWireFrame.toPngWorkerFrame(): WorkerFrame? {
    if (payload.isEmpty() || widthPx <= 0 || heightPx <= 0) return null
    val png = FastPngEncoder.encodeFast(widthPx, heightPx, payload)
    return WorkerFrame(bytes = png)
  }

  private data class FrameKey(
    val direction: String,
    val screen: String,
    val overlayId: String?,
    val viewName: String,
    val isEndFrame: Boolean,
  )

  companion object {
    const val WORKER_MAIN_CLASS = "bot.charlies.androiddesign.preview.render.LayoutlibWorkerMainKt"
    const val JAVA_21_PROP = "adp.java21.bin"
    private const val DEFAULT_BATCH_TIMEOUT_MS = 20_000L
    private const val DEFAULT_CONTROL_TIMEOUT_MS = 15_000L
    private const val GRACEFUL_EXIT_WAIT_MS = 2_000L
    private const val STDERR_TAIL_LIMIT = 80
    private const val IO_BUFFER_BYTES = 65_536

    fun defaultWorkerCommand(
      meta: BootstrapMeta,
      env: Map<String, String> = System.getenv(),
    ): List<String> {
      val workerCp =
        env[RuntimeIdentity.WORKER_CP_ENV]
          ?.trim()
          ?.takeIf { it.isNotEmpty() }
          ?.let(Path::of)
          ?.takeIf { Files.isRegularFile(it) } ?: meta.workerCp
      return listOf(
        resolveJava21Binary(env),
        "-Xmx2g",
        "-Djava.awt.headless=true",
        "-Djdk.attach.allowAttachSelf=true",
        "-XX:+EnableDynamicAgentLoading",
        "@${workerCp.toAbsolutePath().normalize()}",
        WORKER_MAIN_CLASS,
        "--bootstrap-dir",
        meta.bootstrapDir.toAbsolutePath().normalize().toString(),
      )
    }

    internal fun resolveJava21Binary(env: Map<String, String> = System.getenv()): String {
      val envJava = env["ANDROID_DESIGN_PREVIEW_JAVA"]?.trim()?.takeIf { it.isNotEmpty() }
      if (envJava != null && Path.of(envJava).exists()) {
        return envJava
      }
      if (Runtime.version().feature() >= 21) {
        val home = System.getProperty("java.home")
        if (!home.isNullOrBlank()) {
          val unix = Path.of(home, "bin", "java")
          if (Files.isExecutable(unix)) return unix.toString()
          val win = Path.of(home, "bin", "java.exe")
          if (Files.isRegularFile(win)) return win.toString()
        }
      }
      val propJava21 = System.getProperty(JAVA_21_PROP)?.trim()?.takeIf { it.isNotEmpty() }
      if (propJava21 != null && Files.isExecutable(Path.of(propJava21))) {
        return propJava21
      }
      return System.getProperty("java.home")
        ?.takeIf { it.isNotBlank() }
        ?.let { Path.of(it, "bin", "java").toString() } ?: "java"
    }
  }
}
