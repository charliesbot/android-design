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
import bot.charlies.androiddesign.preview.model.DeviceAckResponse
import bot.charlies.androiddesign.preview.model.DeviceBatchRequest
import bot.charlies.androiddesign.preview.model.DeviceBatchResponse
import bot.charlies.androiddesign.preview.model.DeviceMemoryStats
import bot.charlies.androiddesign.preview.model.DeviceOp
import bot.charlies.androiddesign.preview.model.DevicePingResponse
import bot.charlies.androiddesign.preview.model.DevicePreloadResponse
import bot.charlies.androiddesign.preview.model.DeviceRenderTarget
import bot.charlies.androiddesign.preview.model.DeviceTargetResult
import bot.charlies.androiddesign.preview.model.DeviceWireCodec
import bot.charlies.androiddesign.preview.model.DeviceWireFrame
import bot.charlies.androiddesign.preview.model.FrameFormat
import bot.charlies.androiddesign.preview.model.Views
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
      val op = DeviceWireCodec.readOpOrNull(inp) ?: break
      when (op) {
        DeviceOp.PING -> {
          val resp =
            DevicePingResponse(
              ok = true,
              status = "pong",
              pid = ProcessHandle.current().pid().toInt(),
              reloadCount = reloadCount,
              loadedDexHashes = boardCache.keys.toList(),
              memory = collectMemoryStats(),
            )
          DeviceWireCodec.writePingResponse(out, resp)
        }
        DeviceOp.PRELOAD_DEX -> {
          val req = DeviceWireCodec.readPreloadDexRequest(inp)
          val resp = executePreload(req.dexHash, req.entryClass, req.dexBytes)
          DeviceWireCodec.writePreloadDexResponse(out, resp)
        }
        DeviceOp.RENDER_BATCH -> {
          val wireReq = DeviceWireCodec.readRenderBatchRequest(inp)
          val (resp, frames) = executeBatch(wireReq.dexBytes, wireReq.entryClass, wireReq.request)
          DeviceWireCodec.writeRenderBatchResponse(out, resp, frames)
        }
        else -> {
          DeviceWireCodec.writeAck(
            out,
            DeviceAckResponse(ok = false, error = "unknown op $op"),
          )
        }
      }
    }
  }

  private fun executePreload(
    classHash: String,
    entryClass: String,
    bundleBytes: ByteArray,
  ): DevicePreloadResponse {
    return try {
      val board =
        synchronized(boardCache) {
          boardCache[classHash]?.board ?: loadBoard(classHash, entryClass, bundleBytes).board
        }
      val defaultSystem = board.resolveDesignSystem(null)
      DevicePreloadResponse(
        ok = true,
        dexHash = classHash,
        boardMeta = ComposeBoardRenderer.buildBoardMeta(board),
        boardTheme = ComposeBoardRenderer.boardThemeOverride(board),
        defaultSeed = defaultSystem.seed,
        defaultVariant = defaultSystem.variant.wire,
        defaultContrast = defaultSystem.contrast,
      )
    } catch (t: Throwable) {
      t.printStackTrace(System.err)
      DevicePreloadResponse(ok = false, error = "${t::class.java.name}: ${t.message}")
    }
  }

  private fun executeBatch(
    bundleBytes: ByteArray?,
    entryClass: String,
    request: DeviceBatchRequest,
  ): Pair<DeviceBatchResponse, List<DeviceWireFrame>> {
    val totalStartNs = System.nanoTime()
    return try {
      val loadStartNs = System.nanoTime()
      val loaded =
        resolveBoard(request.dexHash, entryClass, bundleBytes, request.forceNewClassLoader)
      val loadMs = (System.nanoTime() - loadStartNs) / NANOS_PER_MS
      val board = loaded.board
      val explorationDir = request.explorationDir?.takeIf { it.isNotBlank() }?.let(Path::of)

      val results = mutableListOf<DeviceTargetResult>()
      val frames = mutableListOf<DeviceWireFrame>()
      runBlocking {
        for (target in request.targets) {
          val (res, wireFrames) = renderSingleTarget(board, request, target, explorationDir)
          results += res
          frames += wireFrames
        }
      }

      val memStats = collectMemoryStats()
      val recycleReason = evaluateRecycleReason(reloadCount, memStats)
      val totalMs = (System.nanoTime() - totalStartNs) / NANOS_PER_MS
      DeviceBatchResponse(
        ok = true,
        dexLoadMs = loadMs,
        renderTotalMs = totalMs,
        reloadCount = reloadCount,
        recycleRecommended = recycleReason != null,
        recycleReason = recycleReason,
        boardMeta = ComposeBoardRenderer.buildBoardMeta(board),
        results = results,
        memoryAfter = memStats,
      ) to frames
    } catch (t: Throwable) {
      t.printStackTrace(System.err)
      DeviceBatchResponse(
        ok = false,
        error = "${t::class.java.name}: ${t.message}\n${t.stackTraceToString()}",
        reloadCount = reloadCount,
        memoryAfter = collectMemoryStats(),
      ) to emptyList()
    }
  }

  private suspend fun renderSingleTarget(
    board: ExplorationBoard,
    request: DeviceBatchRequest,
    target: DeviceRenderTarget,
    explorationDir: Path?,
  ): Pair<DeviceTargetResult, List<DeviceWireFrame>> {
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
          fontScale = target.fontScale,
          darkOverride = target.darkOverride,
          seedOverride = target.seedOverride,
          variantOverride = target.variantOverride,
          explorationDir = explorationDir,
        ),
      )

    val viewName = rendered.target.effectiveView.name
    val payloads = mutableListOf<DeviceWireFrame>()
    val wantFrame = target.frameFormat != FrameFormat.NONE
    val endFrame = rendered.endFrame
    if (wantFrame) {
      payloads +=
        DeviceWireFrame(
          direction = target.direction,
          screen = target.screen,
          viewName = viewName,
          overlayId = target.overlayId,
          isEndFrame = false,
          format = FrameFormat.RAW_RGBA,
          widthPx = rendered.frame.width,
          heightPx = rendered.frame.height,
          rawLength = rendered.frame.bytes.size,
          payload = rendered.frame.bytes,
        )
      if (target.includeEndFrame && endFrame != null) {
        payloads +=
          DeviceWireFrame(
            direction = target.direction,
            screen = target.screen,
            viewName = viewName,
            overlayId = target.overlayId,
            isEndFrame = true,
            format = FrameFormat.RAW_RGBA,
            widthPx = endFrame.width,
            heightPx = endFrame.height,
            rawLength = endFrame.bytes.size,
            payload = endFrame.bytes,
          )
      }
    }

    val resultFormat = if (wantFrame) FrameFormat.RAW_RGBA else FrameFormat.NONE
    return DeviceTargetResult(
      direction = target.direction,
      screen = target.screen,
      viewName = target.viewName,
      overlayId = target.overlayId,
      densityScale = target.densityScale,
      widthPx = rendered.target.widthPx,
      heightPx = rendered.target.heightPx,
      renderMs = rendered.renderMs,
      pass = rendered.tree.pass,
      tree = rendered.tree,
      endTree = rendered.endTree,
      hasFrame = wantFrame,
      hasEndFrame = wantFrame && target.includeEndFrame && endFrame != null,
      frameFormat = resultFormat,
    ) to payloads
  }

  private fun resolveBoard(
    classHash: String,
    entryClass: String,
    bundleBytes: ByteArray?,
    forceNew: Boolean,
  ): LoadedBoard {
    synchronized(boardCache) {
      if (!forceNew) {
        boardCache[classHash]?.let {
          return it
        }
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

  private fun collectMemoryStats(): DeviceMemoryStats {
    val rt = Runtime.getRuntime()
    val usedJava = rt.totalMemory() - rt.freeMemory()
    return DeviceMemoryStats(
      javaHeapUsedBytes = usedJava,
      javaHeapMaxBytes = rt.maxMemory(),
    )
  }

  private fun evaluateRecycleReason(reloads: Int, mem: DeviceMemoryStats): String? =
    when {
      reloads >= RECYCLE_RELOAD_LIMIT -> "reload_limit:$reloads"
      mem.javaHeapUsedBytes > RECYCLE_HEAP_BYTES ->
        "java_heap_mb:${mem.javaHeapUsedBytes / (1024 * 1024)}"
      else -> null
    }
}
