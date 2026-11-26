package bot.charlies.androiddesign.preview.render

import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.PaparazziSdk
import bot.charlies.androiddesign.preview.domain.bootstrap.BootstrapMeta
import bot.charlies.androiddesign.preview.domain.sandbox.SandboxClassLoader
import bot.charlies.androiddesign.preview.feature.preview.sandbox.ComposeBoardRenderer
import bot.charlies.androiddesign.preview.feature.preview.sandbox.RenderTargetRequest
import bot.charlies.androiddesign.preview.kit.ExplorationBoard
import bot.charlies.androiddesign.preview.model.ClassBundleCodec
import bot.charlies.androiddesign.preview.model.Views
import bot.charlies.androiddesign.preview.model.WorkerAckResponse
import bot.charlies.androiddesign.preview.model.WorkerBatchRequest
import bot.charlies.androiddesign.preview.model.WorkerBatchResponse
import bot.charlies.androiddesign.preview.model.WorkerMemoryStats
import bot.charlies.androiddesign.preview.model.WorkerPingResponse
import bot.charlies.androiddesign.preview.model.WorkerPipeCodec
import bot.charlies.androiddesign.preview.model.WorkerPipeOp
import bot.charlies.androiddesign.preview.model.WorkerPreloadResponse
import bot.charlies.androiddesign.preview.model.WorkerRenderTarget
import bot.charlies.androiddesign.preview.model.WorkerTargetResult
import bot.charlies.androiddesign.preview.model.WorkerWireFrame
import com.android.resources.Density
import com.android.resources.ScreenOrientation
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.nio.file.Path
import kotlin.system.exitProcess
import kotlinx.coroutines.runBlocking

private const val MAX_CACHED_BOARDS = 4
private const val RECYCLE_RELOAD_LIMIT = 200
private const val RECYCLE_HEAP_BYTES = 768L * 1024L * 1024L
private const val NANOS_PER_MS = 1_000_000.0

fun main(args: Array<String>) {
  val protocolOut = System.out
  System.setOut(System.err)

  val bootstrapDirOverride = parseFlagValue(args, "--bootstrap-dir")
  val meta =
    if (bootstrapDirOverride != null) {
      BootstrapMeta.fromFile(Path.of(bootstrapDirOverride).resolve("bootstrap-meta.properties"))
    } else {
      BootstrapMeta.locate()
    }

  val sdk = LayoutlibEnvironment.open(meta)
  if ("--self-test" in args) {
    val exitCode = runSelfTest(sdk)
    exitProcess(exitCode)
  }

  try {
    val worker = LayoutlibWorkerSession(sdk)
    val inp = DataInputStream(BufferedInputStream(System.`in`, 65536))
    val out = DataOutputStream(BufferedOutputStream(protocolOut, 65536))
    worker.serve(inp, out)
  } finally {
    runCatching { sdk.teardown() }
  }
  exitProcess(0)
}

private fun parseFlagValue(args: Array<String>, flag: String): String? {
  val idx = args.indexOf(flag)
  return if (idx >= 0 && idx + 1 < args.size) args[idx + 1] else null
}

private fun runSelfTest(sdk: PaparazziSdk): Int {
  return try {
    sdk.resetSession(
      DeviceConfig(
        screenWidth = 400,
        screenHeight = 300,
        xdpi = 320,
        ydpi = 320,
        orientation = ScreenOrientation.LANDSCAPE,
        density = Density.XHIGH,
        softButtons = false,
      ),
    )
    // Use the prepared sdk directly via openSession + ComposeView so one Bridge handles it.
    val view =
      androidx.compose.ui.platform.ComposeView(sdk.context).apply {
        layoutParams =
          android.view.ViewGroup.LayoutParams(
            android.view.ViewGroup.LayoutParams.MATCH_PARENT,
            android.view.ViewGroup.LayoutParams.MATCH_PARENT,
          )
        setContent {
          MaterialTheme {
            Surface {
              Button(onClick = {}) { Text("Layoutlib Ready") }
            }
          }
        }
      }
    val img =
      sdk.openSession(view).use { session ->
        session.advanceFrame(0L)
        session.captureFrame(0L)
      }
    check(img.width == 400 && img.height == 300) {
      "Unexpected self-test image dimensions: ${img.width}x${img.height}"
    }
    val firstPixel = img.getRGB(0, 0)
    var hasDifferentPixel = false
    for (y in 0 until img.height step 4) {
      for (x in 0 until img.width step 4) {
        if (img.getRGB(x, y) != firstPixel) {
          hasDifferentPixel = true
          break
        }
      }
      if (hasDifferentPixel) break
    }
    check(hasDifferentPixel) { "Self-test image is completely uniform" }
    System.err.println("Layoutlib worker self-test passed (${img.width}x${img.height})")
    runCatching { sdk.teardown() }
    0
  } catch (t: Throwable) {
    t.printStackTrace(System.err)
    runCatching { sdk.teardown() }
    1
  }
}

internal class LayoutlibWorkerSession(private val sdk: PaparazziSdk) {
  private val boardRenderer = ComposeBoardRenderer(LayoutlibBoardRenderHost(sdk))

  private data class LoadedBoard(val loader: SandboxClassLoader, val board: ExplorationBoard)

  private val boardCache =
    object : LinkedHashMap<String, LoadedBoard>(MAX_CACHED_BOARDS + 1, 0.75f, true) {
      override fun removeEldestEntry(
        eldest: MutableMap.MutableEntry<String, LoadedBoard>?,
      ): Boolean = size > MAX_CACHED_BOARDS
    }

  private var reloadCount: Int = 0

  fun serve(inp: DataInputStream, out: DataOutputStream) {
    while (true) {
      val op = WorkerPipeCodec.readOpOrNull(inp) ?: break
      when (op) {
        WorkerPipeOp.PING -> {
          val resp =
            WorkerPingResponse(
              ok = true,
              pid = ProcessHandle.current().pid().toInt(),
              reloadCount = reloadCount,
              loadedClassHashes = boardCache.keys.toList(),
              memory = collectMemoryStats(),
            )
          WorkerPipeCodec.writePingResponse(out, resp)
        }
        WorkerPipeOp.PRELOAD_CLASSES -> {
          val req = WorkerPipeCodec.readPreloadClassesRequest(inp)
          val resp = executePreload(req.classHash, req.entryClass, req.classBundle)
          WorkerPipeCodec.writePreloadClassesResponse(out, resp)
        }
        WorkerPipeOp.RENDER_BATCH -> {
          val wireReq = WorkerPipeCodec.readRenderBatchRequest(inp)
          val (resp, frames) =
            executeBatch(wireReq.classBundle, wireReq.entryClass, wireReq.request)
          WorkerPipeCodec.writeRenderBatchResponse(out, resp, frames)
        }
        else -> {
          WorkerPipeCodec.writeAck(
            out,
            WorkerAckResponse(ok = false, error = "unknown op $op"),
          )
        }
      }
    }
  }

  private fun executePreload(
    classHash: String,
    entryClass: String,
    bundleBytes: ByteArray,
  ): WorkerPreloadResponse {
    return try {
      val board =
        synchronized(boardCache) {
          boardCache[classHash]?.board ?: loadBoard(classHash, entryClass, bundleBytes).board
        }
      WorkerPreloadResponse(
        ok = true,
        classHash = classHash,
        boardMeta = ComposeBoardRenderer.buildBoardMeta(board),
        boardTheme = ComposeBoardRenderer.boardThemeOverride(board),
      )
    } catch (t: Throwable) {
      t.printStackTrace(System.err)
      WorkerPreloadResponse(ok = false, error = "${t::class.java.name}: ${t.message}")
    }
  }

  private fun executeBatch(
    bundleBytes: ByteArray?,
    entryClass: String,
    request: WorkerBatchRequest,
  ): Pair<WorkerBatchResponse, List<WorkerWireFrame>> {
    val totalStartNs = System.nanoTime()
    return try {
      val loaded = resolveBoard(request.classHash, entryClass, bundleBytes)
      val board = loaded.board
      val explorationDir = request.explorationDir?.takeIf { it.isNotBlank() }?.let(Path::of)

      val results = mutableListOf<WorkerTargetResult>()
      val frames = mutableListOf<WorkerWireFrame>()
      runBlocking {
        for (target in request.targets) {
          val (res, wireFrames) = renderSingleTarget(board, request, target, explorationDir)
          results += res
          frames += wireFrames
        }
      }

      val memStats = collectMemoryStats()
      val recycleRecommended = shouldRecycle(reloadCount, memStats)
      val totalMs = (System.nanoTime() - totalStartNs) / NANOS_PER_MS
      WorkerBatchResponse(
        ok = true,
        renderTotalMs = totalMs,
        reloadCount = reloadCount,
        recycleRecommended = recycleRecommended,
        results = results,
      ) to frames
    } catch (t: Throwable) {
      t.printStackTrace(System.err)
      WorkerBatchResponse(
        ok = false,
        error = "${t::class.java.name}: ${t.message}\n${t.stackTraceToString()}",
        reloadCount = reloadCount,
      ) to emptyList()
    }
  }

  private suspend fun renderSingleTarget(
    board: ExplorationBoard,
    request: WorkerBatchRequest,
    target: WorkerRenderTarget,
    explorationDir: Path?,
  ): Pair<WorkerTargetResult, List<WorkerWireFrame>> {
    val direction =
      board.directions.firstOrNull { it.name == target.direction }
        ?: error("No direction '${target.direction}'")
    val baseView = Views.byName(target.viewName) ?: error("Unknown view '${target.viewName}'")

    val rendered =
      boardRenderer.render(
        RenderTargetRequest(
          board = board,
          direction = direction,
          screenId = target.screen,
          view = baseView,
          sourceFile = request.sourceFile,
          sourceText = request.sourceText,
          designMd = request.designTheme,
          overlayId = target.overlayId,
          density = target.densityScale,
          darkOverride = target.darkOverride,
          seedOverride = target.seedOverride,
          variantOverride = target.variantOverride,
          explorationDir = explorationDir,
        ),
      )

    val viewName = rendered.target.effectiveView.name
    val payloads = mutableListOf<WorkerWireFrame>()
    val endFrame = rendered.endFrame
    if (target.includeFrame) {
      payloads +=
        WorkerWireFrame(
          direction = target.direction,
          screen = target.screen,
          viewName = viewName,
          overlayId = target.overlayId,
          isEndFrame = false,
          widthPx = rendered.frame.width,
          heightPx = rendered.frame.height,
          payload = rendered.frame.bytes,
        )
      if (target.includeEndFrame && endFrame != null) {
        payloads +=
          WorkerWireFrame(
            direction = target.direction,
            screen = target.screen,
            viewName = viewName,
            overlayId = target.overlayId,
            isEndFrame = true,
            widthPx = endFrame.width,
            heightPx = endFrame.height,
            payload = endFrame.bytes,
          )
      }
    }

    return WorkerTargetResult(
      direction = target.direction,
      screen = target.screen,
      viewName = target.viewName,
      overlayId = target.overlayId,
      renderMs = rendered.renderMs,
      tree = rendered.tree,
      endTree = rendered.endTree,
    ) to payloads
  }

  private fun resolveBoard(
    classHash: String,
    entryClass: String,
    bundleBytes: ByteArray?,
  ): LoadedBoard {
    synchronized(boardCache) {
      boardCache[classHash]?.let {
        return it
      }
      val bytes =
        bundleBytes?.takeIf { it.isNotEmpty() }
          ?: throw IllegalArgumentException(
            "Classes '$classHash' not preloaded and no bytes supplied",
          )
      return loadBoard(classHash, entryClass, bytes)
    }
  }

  private fun loadBoard(
    classHash: String,
    entryClass: String,
    bundleBytes: ByteArray,
  ): LoadedBoard {
    val classes = ClassBundleCodec.decode(bundleBytes)
    val loader = SandboxClassLoader(classes, LayoutlibWorkerSession::class.java.classLoader!!)
    val clazz = loader.loadClass(entryClass)
    val getter =
      clazz.declaredMethods.firstOrNull {
        it.name == "getPreview" &&
          java.lang.reflect.Modifier.isStatic(it.modifiers) &&
          it.parameterCount == 0
      }
        ?: clazz.declaredMethods.firstOrNull {
          it.name == "preview" &&
            java.lang.reflect.Modifier.isStatic(it.modifiers) &&
            it.parameterCount == 0
        }
        ?: clazz.declaredMethods.firstOrNull {
          java.lang.reflect.Modifier.isStatic(it.modifiers) &&
            it.parameterCount == 0 &&
            ExplorationBoard::class.java.isAssignableFrom(it.returnType)
        }
        ?: error("No getPreview() method in $entryClass")
    val board = getter.invoke(null) as ExplorationBoard
    reloadCount++
    val loaded = LoadedBoard(loader, board)
    boardCache[classHash] = loaded
    return loaded
  }

  private fun collectMemoryStats(): WorkerMemoryStats {
    val rt = Runtime.getRuntime()
    val usedJava = rt.totalMemory() - rt.freeMemory()
    return WorkerMemoryStats(
      javaHeapUsedBytes = usedJava,
      javaHeapMaxBytes = rt.maxMemory(),
    )
  }

  private fun shouldRecycle(reloads: Int, mem: WorkerMemoryStats): Boolean =
    reloads >= RECYCLE_RELOAD_LIMIT || mem.javaHeapUsedBytes > RECYCLE_HEAP_BYTES
}
