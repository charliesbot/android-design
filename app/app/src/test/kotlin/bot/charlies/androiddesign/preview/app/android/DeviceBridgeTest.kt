package bot.charlies.androiddesign.preview.app.android

import bot.charlies.androiddesign.preview.model.DeviceAckResponse
import bot.charlies.androiddesign.preview.model.DeviceHasAssetsResponse
import bot.charlies.androiddesign.preview.model.DeviceLiveStateRequest
import bot.charlies.androiddesign.preview.model.DevicePingResponse
import bot.charlies.androiddesign.preview.model.LiveDeviceCodec
import bot.charlies.androiddesign.preview.model.LiveDeviceOp
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.net.InetAddress
import java.net.ServerSocket
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class DeviceBridgeTest {

  @Test
  fun `syncAssets memoizes hashes per sandbox session and re-syncs after reconnect and PID change`() {
    val blobA = byteArrayOf(1, 2, 3)
    val blobB = byteArrayOf(4, 5, 6)
    val hasAssetsQueries = mutableListOf<List<String>>()
    val putAssetBatches = mutableListOf<List<String>>()
    val sandboxPid = AtomicInteger(1001)
    val dropNextHasAssets = AtomicInteger(0)
    val restarts = AtomicInteger(0)

    ServerSocket(0, 4, InetAddress.getByName("127.0.0.1")).use { server ->
      server.soTimeout = 5_000
      val serverPool = Executors.newSingleThreadExecutor()
      val stopServer = AtomicInteger(0)
      val serverTask = serverPool.submit {
        while (stopServer.get() == 0) {
          val sock =
            try {
              server.accept()
            } catch (_: Exception) {
              break
            }
          sock.use { s ->
            val inp = DataInputStream(BufferedInputStream(s.getInputStream()))
            val out = DataOutputStream(BufferedOutputStream(s.getOutputStream()))
            while (true) {
              val op =
                try {
                  LiveDeviceCodec.readOpOrNull(inp)
                } catch (_: Exception) {
                  null
                } ?: break
              when (op) {
                LiveDeviceOp.PING -> {
                  LiveDeviceCodec.writePingResponse(
                    out,
                    DevicePingResponse(ok = true, pid = sandboxPid.get()),
                  )
                }
                LiveDeviceOp.HAS_ASSETS -> {
                  val req = LiveDeviceCodec.readHasAssetsRequest(inp)
                  if (dropNextHasAssets.compareAndSet(1, 0)) {
                    break
                  }
                  hasAssetsQueries += req.hashes
                  LiveDeviceCodec.writeHasAssetsResponse(
                    out,
                    DeviceHasAssetsResponse(missing = req.hashes),
                  )
                }
                LiveDeviceOp.PUT_ASSETS -> {
                  val items = LiveDeviceCodec.readPutAssetsRequest(inp)
                  putAssetBatches += items.map { it.first }
                  LiveDeviceCodec.writeAck(out, DeviceAckResponse(ok = true, stored = items.size))
                }
                else -> break
              }
            }
          }
        }
      }

      try {
        AdbTcpDeviceTransport(
            connectAndEnsurePort = { server.localPort },
            restartSandboxActivity = { restarts.incrementAndGet() },
          )
          .use { transport ->
            assertEquals(1001, transport.ping().pid)

            // 1. First sync uploads hash-a; repeat sync in same session skips wire I/O.
            assertEquals(1, transport.syncAssets(mapOf("hash-a" to blobA)))
            assertEquals(0, transport.syncAssets(mapOf("hash-a" to blobA)))
            assertEquals(listOf(listOf("hash-a")), hasAssetsQueries)
            assertEquals(listOf(listOf("hash-a")), putAssetBatches)

            // 2. Adding hash-b while hash-a is already synced only queries hash-b.
            assertEquals(1, transport.syncAssets(mapOf("hash-a" to blobA, "hash-b" to blobB)))
            assertEquals(listOf(listOf("hash-a"), listOf("hash-b")), hasAssetsQueries)
            assertEquals(listOf(listOf("hash-a"), listOf("hash-b")), putAssetBatches)

            // 3. Reconnect during syncAssets clears syncedAssetHashes so all hashes re-sync.
            dropNextHasAssets.set(1)
            val blobC = byteArrayOf(7, 8, 9)
            assertEquals(
              3,
              transport.syncAssets(
                linkedMapOf("hash-a" to blobA, "hash-b" to blobB, "hash-c" to blobC),
              ),
            )
            assertEquals(1, restarts.get())
            assertEquals(listOf("hash-a", "hash-b", "hash-c"), hasAssetsQueries.last())
            assertEquals(listOf("hash-a", "hash-b", "hash-c"), putAssetBatches.last())

            // 4. Sandbox PID change detected via ping() invalidates syncedAssetHashes.
            sandboxPid.set(2002)
            assertEquals(2002, transport.ping().pid)
            assertEquals(1, transport.syncAssets(mapOf("hash-a" to blobA)))
            assertEquals(listOf("hash-a"), hasAssetsQueries.last())
            assertEquals(listOf("hash-a"), putAssetBatches.last())
          }
      } finally {
        stopServer.set(1)
        server.close()
        serverTask.get(5, TimeUnit.SECONDS)
        serverPool.shutdownNow()
      }
    }
  }

  @Test
  fun `setLive re-syncs assets on dead-socket reconnect and not-preloaded recovery`() {
    val assets = mapOf("hash-a" to byteArrayOf(1, 2, 3))
    val dexBytes = byteArrayOf(9, 8, 7, 6)
    val eventLog = mutableListOf<String>()
    val dropNextSetLive = AtomicInteger(0)
    val replyNotPreloadedNextLive = AtomicInteger(0)
    val restarts = AtomicInteger(0)

    ServerSocket(0, 4, InetAddress.getByName("127.0.0.1")).use { server ->
      server.soTimeout = 5_000
      val serverPool = Executors.newSingleThreadExecutor()
      val stopServer = AtomicInteger(0)
      val serverTask = serverPool.submit {
        while (stopServer.get() == 0) {
          val sock =
            try {
              server.accept()
            } catch (_: Exception) {
              break
            }
          sock.use { s ->
            val inp = DataInputStream(BufferedInputStream(s.getInputStream()))
            val out = DataOutputStream(BufferedOutputStream(s.getOutputStream()))
            while (true) {
              val op =
                try {
                  LiveDeviceCodec.readOpOrNull(inp)
                } catch (_: Exception) {
                  null
                } ?: break
              when (op) {
                LiveDeviceOp.HAS_ASSETS -> {
                  val req = LiveDeviceCodec.readHasAssetsRequest(inp)
                  eventLog += "HAS_ASSETS:${req.hashes.joinToString(",")}"
                  LiveDeviceCodec.writeHasAssetsResponse(
                    out,
                    DeviceHasAssetsResponse(missing = req.hashes),
                  )
                }
                LiveDeviceOp.PUT_ASSETS -> {
                  val items = LiveDeviceCodec.readPutAssetsRequest(inp)
                  eventLog += "PUT_ASSETS:${items.joinToString(",") { it.first }}"
                  LiveDeviceCodec.writeAck(out, DeviceAckResponse(ok = true, stored = items.size))
                }
                LiveDeviceOp.SET_LIVE -> {
                  val payload = LiveDeviceCodec.readSetLiveRequest(inp)
                  if (dropNextSetLive.compareAndSet(1, 0)) {
                    eventLog += "SET_LIVE_DROP:dex=${payload.dexBytes != null}"
                    break
                  }
                  if (replyNotPreloadedNextLive.compareAndSet(1, 0)) {
                    eventLog += "SET_LIVE_NOT_PRELOADED:dex=${payload.dexBytes != null}"
                    LiveDeviceCodec.writeAck(
                      out,
                      DeviceAckResponse(ok = false, error = "dex not preloaded"),
                    )
                  } else {
                    eventLog += "SET_LIVE_OK:dex=${payload.dexBytes != null}"
                    LiveDeviceCodec.writeAck(out, DeviceAckResponse(ok = true))
                  }
                }
                else -> break
              }
            }
          }
        }
      }

      try {
        AdbTcpDeviceTransport(
            connectAndEnsurePort = { server.localPort },
            restartSandboxActivity = { restarts.incrementAndGet() },
          )
          .use { transport ->
            val liveReq =
              DeviceLiveStateRequest(
                dexHash = "dex-1",
                entryClass = "TestEntry",
                direction = "d1",
                screen = "s1",
              )

            // 1. First setLive syncs assets and sends DEX; second skips assets and DEX.
            assertTrue(transport.setLive(dexBytes, liveReq, assets))
            assertTrue(transport.setLive(dexBytes, liveReq, assets))
            assertEquals(
              listOf(
                "HAS_ASSETS:hash-a",
                "PUT_ASSETS:hash-a",
                "SET_LIVE_OK:dex=true",
                "SET_LIVE_OK:dex=false",
              ),
              eventLog,
            )
            eventLog.clear()

            // 2. Dead-socket reconnect clears caches, re-syncs assets, and resends DEX.
            dropNextSetLive.set(1)
            assertTrue(transport.setLive(dexBytes, liveReq, assets))
            assertEquals(1, restarts.get())
            assertEquals(
              listOf(
                "SET_LIVE_DROP:dex=false",
                "HAS_ASSETS:hash-a",
                "PUT_ASSETS:hash-a",
                "SET_LIVE_OK:dex=true",
              ),
              eventLog,
            )
            eventLog.clear()

            // 3. "dex not preloaded" recovery clears caches and retries SET_LIVE with DEX.
            replyNotPreloadedNextLive.set(1)
            assertTrue(transport.setLive(dexBytes, liveReq, assets))
            assertEquals(
              listOf(
                "SET_LIVE_NOT_PRELOADED:dex=false",
                "HAS_ASSETS:hash-a",
                "PUT_ASSETS:hash-a",
                "SET_LIVE_OK:dex=true",
              ),
              eventLog,
            )
          }
      } finally {
        stopServer.set(1)
        server.close()
        serverTask.get(5, TimeUnit.SECONDS)
        serverPool.shutdownNow()
      }
    }
  }
}
