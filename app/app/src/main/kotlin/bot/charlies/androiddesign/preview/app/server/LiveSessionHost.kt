package bot.charlies.androiddesign.preview.app.server

import bot.charlies.androiddesign.preview.app.android.AdbTcpDeviceTransport
import bot.charlies.androiddesign.preview.app.android.AndroidExplorationCompiler
import bot.charlies.androiddesign.preview.app.android.DeviceManager
import bot.charlies.androiddesign.preview.app.android.DeviceSetupResult
import bot.charlies.androiddesign.preview.app.android.DeviceTransport
import bot.charlies.androiddesign.preview.app.client.ClientExitCode
import bot.charlies.androiddesign.preview.domain.bootstrap.BootstrapMeta
import bot.charlies.androiddesign.preview.model.DeviceLiveStateRequest
import bot.charlies.androiddesign.preview.model.DevicePingResponse
import java.nio.file.Path
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference

/** Result of starting or focusing an opt-in emulator/device live session. */
sealed interface LiveSessionStartResult {
  data class Ready(val serial: String, val forwardPort: Int) : LiveSessionStartResult

  data class Failed(val exitCode: Int, val error: String, val message: String) :
    LiveSessionStartResult
}

/**
 * Server-owned opt-in live device session seam. Inactive by default; when activated by `open
 * --live`, boots/connects a visible Android device and streams `SET_LIVE` updates on edits and
 * focus changes.
 */
interface LiveSessionHost : AutoCloseable {
  val isActive: Boolean
  val deviceSerial: String?
  val adbForwardPort: Int?

  fun startSession(
    serial: String?,
    avd: String?,
    entry: CompileEntry?,
    update: LivePreviewUpdate?,
  ): LiveSessionStartResult

  fun updateIfActive(entry: CompileEntry, update: LivePreviewUpdate)

  override fun close() {}
}

/** Inactive [LiveSessionHost] for headless server tests. */
object NoOpLiveSessionHost : LiveSessionHost {
  override val isActive: Boolean = false
  override val deviceSerial: String? = null
  override val adbForwardPort: Int? = null

  override fun startSession(
    serial: String?,
    avd: String?,
    entry: CompileEntry?,
    update: LivePreviewUpdate?,
  ): LiveSessionStartResult =
    LiveSessionStartResult.Failed(
      ClientExitCode.NOT_READY,
      "live_unsupported",
      "Live preview host is unavailable in this server configuration",
    )

  override fun updateIfActive(entry: CompileEntry, update: LivePreviewUpdate) {}
}

/**
 * Lazy opt-in live Android device session backed by [DeviceManager], on-demand D8 lowering via
 * [AndroidExplorationCompiler.lowerClassesToDex], and [AdbTcpDeviceTransport] (`SET_LIVE`).
 */
class AndroidLiveSessionHost(
  private val meta: BootstrapMeta,
  private val cacheRoot: Path = ServerPaths.cacheRoot(),
  private val env: Map<String, String> = System.getenv(),
  private val deviceManagerFactory: () -> DeviceManager = {
    DeviceManager(cacheRoot = cacheRoot, env = env)
  },
  private val pingForwardFn: (Int) -> DevicePingResponse? = { DeviceManager.pingLocalForward(it) },
  private val dexLowerer:
    (String, Map<String, ByteArray>, BootstrapMeta) -> AndroidExplorationCompiler.DexLowering =
    { fileName, classBytes, bMeta ->
      AndroidExplorationCompiler.lowerClassesToDex(fileName, classBytes, bMeta)
    },
  private val transportFactory: (DeviceManager, String, AtomicInteger) -> DeviceTransport =
    { devMgr, serial, portRef ->
      AdbTcpDeviceTransport(
        connectAndEnsurePort = {
          val ping = pingForwardFn(portRef.get())
          if (ping != null && ping.ok) {
            portRef.get()
          } else {
            val refreshed = devMgr.ensureDeviceReady(meta = meta, requestedSerial = serial)
            if (refreshed is DeviceSetupResult.Ready) {
              portRef.set(refreshed.forwardPort)
              refreshed.forwardPort
            } else {
              portRef.get()
            }
          }
        },
        restartSandboxActivity = { devMgr.restartSandboxOnSerial(serial) },
      )
    },
) : LiveSessionHost {
  private data class CachedDex(val dexHash: String, val dexBytes: ByteArray)

  private class ActiveSession(
    val serial: String,
    val avd: String?,
    val devMgr: DeviceManager,
    val forwardPort: AtomicInteger,
    val transport: DeviceTransport,
  ) {
    val dexCache =
      object : LinkedHashMap<String, CachedDex>(8, 0.75f, true) {
        override fun removeEldestEntry(
          eldest: MutableMap.MutableEntry<String, CachedDex>,
        ): Boolean = size > MAX_CACHED_DEX
      }
  }

  private val lock = Any()
  private val inFlightBootMgr = AtomicReference<DeviceManager?>(null)

  @Volatile private var activeSession: ActiveSession? = null
  @Volatile private var closed: Boolean = false

  override val isActive: Boolean
    get() = activeSession != null

  override val deviceSerial: String?
    get() = activeSession?.serial

  override val adbForwardPort: Int?
    get() = activeSession?.forwardPort?.get()

  override fun startSession(
    serial: String?,
    avd: String?,
    entry: CompileEntry?,
    update: LivePreviewUpdate?,
  ): LiveSessionStartResult =
    synchronized(lock) {
      if (closed) {
        return@synchronized LiveSessionStartResult.Failed(
          ClientExitCode.FAILED,
          "server_stopped",
          "Preview server is shutting down",
        )
      }

      val existing = activeSession
      if (existing != null && matchesRequestedTarget(existing, serial, avd)) {
        val ping = pingForwardFn(existing.forwardPort.get())
        if (ping != null && ping.ok) {
          if (entry != null) {
            runCatching { pushLiveLocked(existing, entry, update ?: LivePreviewUpdate()) }
          }
          existing.devMgr.launchLiveActivity(existing.serial)
          return@synchronized LiveSessionStartResult.Ready(
            existing.serial,
            existing.forwardPort.get(),
          )
        }
        teardownSessionLocked(existing)
      } else if (existing != null) {
        teardownSessionLocked(existing)
      }

      val devMgr = deviceManagerFactory()
      inFlightBootMgr.set(devMgr)
      val setup =
        try {
          devMgr.ensureDeviceReady(
            meta = meta,
            requestedSerial = serial,
            requestedAvd = avd,
            forceReinstall = false,
          )
        } finally {
          inFlightBootMgr.compareAndSet(devMgr, null)
        }

      when (setup) {
        is DeviceSetupResult.Failed -> {
          runCatching { devMgr.releaseLaunchedEmulator() }
          LiveSessionStartResult.Failed(setup.exitCode, setup.error, setup.message)
        }
        is DeviceSetupResult.Ready -> {
          if (closed) {
            runCatching { devMgr.releaseLaunchedEmulator() }
            return@synchronized LiveSessionStartResult.Failed(
              ClientExitCode.FAILED,
              "server_stopped",
              "Preview server stopped while starting live device",
            )
          }
          val portRef = AtomicInteger(setup.forwardPort)
          val transport = transportFactory(devMgr, setup.serial, portRef)
          val session =
            ActiveSession(
              serial = setup.serial,
              avd = setup.avdName,
              devMgr = devMgr,
              forwardPort = portRef,
              transport = transport,
            )
          activeSession = session
          if (entry != null) {
            runCatching { pushLiveLocked(session, entry, update ?: LivePreviewUpdate()) }
          }
          devMgr.launchLiveActivity(setup.serial)
          LiveSessionStartResult.Ready(setup.serial, portRef.get())
        }
      }
    }

  override fun updateIfActive(entry: CompileEntry, update: LivePreviewUpdate) {
    synchronized(lock) {
      val session = activeSession ?: return
      if (closed) return
      try {
        pushLiveLocked(session, entry, update)
      } catch (_: Exception) {
        // Emulator closed or device disconnected; tear down live session so board/server stay up.
        teardownSessionLocked(session)
      }
    }
  }

  override fun close() {
    closed = true
    inFlightBootMgr.getAndSet(null)?.let { mgr -> runCatching { mgr.releaseLaunchedEmulator() } }
    synchronized(lock) {
      activeSession?.let { teardownSessionLocked(it) }
    }
  }

  private fun matchesRequestedTarget(
    session: ActiveSession,
    requestedSerial: String?,
    requestedAvd: String?,
  ): Boolean {
    if (!requestedSerial.isNullOrBlank() && requestedSerial != session.serial) return false
    if (!requestedAvd.isNullOrBlank() && requestedAvd != session.avd) return false
    return true
  }

  private fun teardownSessionLocked(session: ActiveSession) {
    if (activeSession === session) {
      activeSession = null
    }
    runCatching { session.transport.close() }
    runCatching { session.devMgr.releaseLaunchedEmulator() }
  }

  private fun pushLiveLocked(
    session: ActiveSession,
    entry: CompileEntry,
    update: LivePreviewUpdate,
  ) {
    val data = entry.compiledData ?: return
    val dex =
      session.dexCache[data.classHash]
        ?: run {
          val lowered = dexLowerer(data.sourceFile, data.classBytes, meta)
          when (lowered) {
            is AndroidExplorationCompiler.DexLowering.Ready -> {
              val entryDex =
                CachedDex(
                  dexHash = ContentHasher.sha256Hex(lowered.dexBytes),
                  dexBytes = lowered.dexBytes,
                )
              session.dexCache[data.classHash] = entryDex
              entryDex
            }
            is AndroidExplorationCompiler.DexLowering.Failed -> return
          }
        }
    if (data.assetBlobs.isNotEmpty()) {
      session.transport.syncAssets(data.assetBlobs)
    }
    session.transport.setLive(
      dexBytes = dex.dexBytes,
      request =
        DeviceLiveStateRequest(
          dexHash = dex.dexHash,
          entryClass = data.entryClass,
          designTheme = entry.loaded.designTheme,
          assetMap = data.assetMap,
          direction = update.direction,
          screen = update.screenId,
          overlayId = update.overlayId,
          darkOverride = update.darkOverride,
          seedOverride = update.seedOverride,
          variantOverride = update.variantOverride,
        ),
      assetBlobs = data.assetBlobs,
    )
  }

  companion object {
    private const val MAX_CACHED_DEX = 4
  }
}
