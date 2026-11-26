package bot.charlies.androiddesign.preview.app.server

import bot.charlies.androiddesign.preview.model.DeviceBatchResponse
import bot.charlies.androiddesign.preview.model.DeviceBoardMeta
import bot.charlies.androiddesign.preview.model.DeviceDirectionMeta
import bot.charlies.androiddesign.preview.model.DeviceOp
import bot.charlies.androiddesign.preview.model.DevicePingResponse
import bot.charlies.androiddesign.preview.model.DevicePreloadResponse
import bot.charlies.androiddesign.preview.model.DeviceTargetResult
import bot.charlies.androiddesign.preview.model.DeviceWireCodec
import bot.charlies.androiddesign.preview.model.DeviceWireFrame
import bot.charlies.androiddesign.preview.model.FrameFormat
import bot.charlies.androiddesign.preview.model.LayoutTree
import bot.charlies.androiddesign.preview.model.Views
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.nio.file.Files
import java.nio.file.Path
import kotlin.system.exitProcess

/**
 * Deterministic child JVM fake speaking [DeviceWireCodec] over stdin/stdout to test timeout,
 * respawn, recycle, and clean stdin-EOF exit in [LayoutlibRenderWorkerTest].
 */
object FakeLayoutlibWorkerMain {
  @JvmStatic
  fun main(args: Array<String>) {
    val protocolOut = System.out
    System.setOut(System.err)

    val mode = args.getOrNull(0) ?: "normal"
    val markerDir = args.getOrNull(1)?.let(Path::of)
    val inp = DataInputStream(BufferedInputStream(System.`in`))
    val out = DataOutputStream(BufferedOutputStream(protocolOut))
    var batchCalls = 0
    var evictedOnce = false

    fun nextCounter(fileName: String): Int {
      val dir = markerDir ?: return 1
      val countFile = dir.resolve(fileName)
      val prior =
        if (Files.isRegularFile(countFile)) {
          Files.readString(countFile).trim().toIntOrNull() ?: 0
        } else {
          0
        }
      val next = prior + 1
      Files.writeString(countFile, next.toString())
      return next
    }

    while (true) {
      val op = DeviceWireCodec.readOpOrNull(inp) ?: break
      when (op) {
        DeviceOp.PING -> {
          val pingCall =
            if (mode == "hang-first-ping" || mode == "fail-first-ping") {
              nextCounter("ping-count.txt")
            } else {
              1
            }
          if (mode == "hang-first-ping" && pingCall == 1) {
            Thread.sleep(60_000L)
          }
          val pingOk = !(mode == "fail-first-ping" && pingCall == 1)
          DeviceWireCodec.writePingResponse(
            out,
            DevicePingResponse(ok = pingOk, pid = ProcessHandle.current().pid().toInt()),
          )
        }
        DeviceOp.PRELOAD_DEX -> {
          DeviceWireCodec.readPreloadDexRequest(inp)
          DeviceWireCodec.writePreloadDexResponse(
            out,
            DevicePreloadResponse(
              ok = true,
              boardMeta =
                DeviceBoardMeta(
                  surface = "phone",
                  boardName = "Fake",
                  directions =
                    listOf(
                      DeviceDirectionMeta(
                        name = "Main",
                        surface = "phone",
                        startScreen = "home",
                        screens = listOf("home"),
                      ),
                    ),
                  views = listOf(Views.Light),
                ),
            ),
          )
        }
        DeviceOp.RENDER_BATCH -> {
          val wire = DeviceWireCodec.readRenderBatchRequest(inp)
          batchCalls++
          if (markerDir != null) {
            val tag = if (wire.dexBytes == null) "null-bytes\n" else "with-bytes\n"
            val logFile = markerDir.resolve("wire-bytes-log.txt")
            val prev = if (Files.isRegularFile(logFile)) Files.readString(logFile) else ""
            Files.writeString(logFile, prev + tag)
          }
          if (mode == "hang-until-killed" && markerDir != null) {
            if (nextCounter("spawn-count.txt") == 1) {
              Thread.sleep(60_000L)
            }
          }
          if (mode == "evict-after-preload" && wire.dexBytes == null && !evictedOnce) {
            evictedOnce = true
            DeviceWireCodec.writeRenderBatchResponse(
              out,
              DeviceBatchResponse(
                ok = false,
                renderTotalMs = 0.0,
                reloadCount = batchCalls,
                error = "Classes '${wire.request.dexHash}' not preloaded and no bytes supplied",
                results = emptyList(),
              ),
              emptyList(),
            )
            continue
          }
          val results =
            wire.request.targets.map { t ->
              val view = Views.byName(t.viewName) ?: Views.Light
              DeviceTargetResult(
                direction = t.direction,
                screen = t.screen,
                viewName = t.viewName,
                overlayId = t.overlayId,
                renderMs = 1.0,
                pass = true,
                tree =
                  LayoutTree(
                    screen = t.screen,
                    view = view,
                    nodes = emptyList(),
                    findings = emptyList(),
                  ),
                hasFrame = t.frameFormat != FrameFormat.NONE,
                frameFormat = t.frameFormat,
              )
            }
          val frames =
            wire.request.targets.mapNotNull { t ->
              if (t.frameFormat == FrameFormat.NONE) {
                null
              } else {
                DeviceWireFrame(
                  direction = t.direction,
                  screen = t.screen,
                  viewName = t.viewName,
                  overlayId = t.overlayId,
                  isEndFrame = false,
                  format = FrameFormat.RAW_RGBA,
                  widthPx = 2,
                  heightPx = 2,
                  rawLength = 16,
                  payload = ByteArray(16) { 0x7F.toByte() },
                )
              }
            }
          val shouldRecycle = mode == "recycle-always" && batchCalls >= 1
          DeviceWireCodec.writeRenderBatchResponse(
            out,
            DeviceBatchResponse(
              ok = true,
              renderTotalMs = 1.0,
              reloadCount = batchCalls,
              recycleRecommended = shouldRecycle,
              recycleReason = if (shouldRecycle) "fake_recycle" else null,
              results = results,
            ),
            frames,
          )
        }
        else -> {
          exitProcess(2)
        }
      }
    }
    exitProcess(0)
  }
}
