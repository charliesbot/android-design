package bot.charlies.androiddesign.preview.model

import kotlinx.serialization.Serializable

/**
 * Opcodes for the live-device ADB forward TCP transport (`AdbTcpDeviceTransport` to Android
 * `:sandbox` `RenderForegroundService`).
 */
object LiveDeviceOp {
  const val PING = 1
  const val HAS_ASSETS = 2
  const val PUT_ASSETS = 3
  const val SET_LIVE = 4
}

/** Memory stats reported by `:sandbox` [RenderForegroundService] during PING. */
@Serializable
data class DeviceMemoryStats(
  val javaHeapUsedBytes: Long = 0L,
  val javaHeapMaxBytes: Long = 0L,
  val nativeHeapAllocatedBytes: Long = 0L,
  val nativeHeapSizeBytes: Long = 0L,
  val totalPssKb: Int = 0,
  val nativePssKb: Int = 0,
  val dalvikPssKb: Int = 0,
)

@Serializable
data class DevicePingResponse(
  val ok: Boolean = true,
  val status: String = "pong",
  val pid: Int = 0,
  val reloadCount: Int = 0,
  val loadedDexHashes: List<String> = emptyList(),
  val memory: DeviceMemoryStats? = null,
  val inFlightOp: String? = null,
  val inFlightAgeMs: Long = 0L,
  val inFlightBudgetMs: Long = 0L,
)

/**
 * True when an op is actively running on `:sandbox` and has already outlived its own watchdog
 * budget (indicating the worker is stuck inside user Compose code and requires force-stop).
 */
fun DevicePingResponse.isWorkerStuck(): Boolean =
  inFlightOp != null && inFlightBudgetMs > 0L && inFlightAgeMs > inFlightBudgetMs

/** Abstract Unix socket name in `:sandbox` that `adb forward` targets (`localabstract:<name>`). */
object DeviceTransportSpec {
  const val SOCKET_NAME = "adp-render-sandbox"
  const val ADB_REMOTE = "localabstract:$SOCKET_NAME"
}

@Serializable data class DeviceHasAssetsRequest(val hashes: List<String>)

@Serializable data class DeviceHasAssetsResponse(val missing: List<String>)

@Serializable
data class DeviceAckResponse(
  val ok: Boolean,
  val stored: Int = 0,
  val error: String? = null,
)

@Serializable
data class DeviceLiveStateRequest(
  val dexHash: String,
  val entryClass: String,
  val direction: String? = null,
  val screen: String? = null,
  val overlayId: String? = null,
  val darkOverride: Boolean? = null,
  val seedOverride: String? = null,
  val variantOverride: String? = null,
  val fontScale: Float = 1f,
  val assetMap: Map<String, String> = emptyMap(),
  val designTheme: ThemeOverride? = null,
)

/** Decoded `SET_LIVE` request with optional inline DEX bytes. */
data class DeviceSetLiveWireRequest(
  val dexBytes: ByteArray?,
  val request: DeviceLiveStateRequest,
) {
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is DeviceSetLiveWireRequest) return false
    val bytesEqual =
      (dexBytes == null && other.dexBytes == null) ||
        (dexBytes != null && other.dexBytes != null && dexBytes.contentEquals(other.dexBytes))
    return bytesEqual && request == other.request
  }

  override fun hashCode(): Int {
    var result = dexBytes?.contentHashCode() ?: 0
    result = 31 * result + request.hashCode()
    return result
  }
}
