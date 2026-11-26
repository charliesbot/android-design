package bot.charlies.androiddesign.preview.model

import kotlinx.serialization.Serializable

object DeviceOp {
  const val PING: Int = 1
  const val PRELOAD_DEX: Int = 2
  const val PUT_ASSETS: Int = 3
  const val HAS_ASSETS: Int = 4
  const val RENDER_BATCH: Int = 5
  const val SET_LIVE: Int = 6
}

@Serializable
enum class FrameFormat {
  NONE,
  RAW_RGBA,
}

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
  /** Name of the op the single worker thread is running, or null when the worker is idle. */
  val inFlightOp: String? = null,
  val inFlightAgeMs: Long = 0L,
  /** Watchdog budget of [inFlightOp]; the service kills itself when the op outlives it. */
  val inFlightBudgetMs: Long = 0L,
)

/**
 * True when the worker has been running one op longer than its watchdog budget, which means the
 * in-process watchdog failed and the host must kill the process itself.
 */
fun DevicePingResponse.isWorkerStuck(): Boolean =
  inFlightOp != null && inFlightBudgetMs > 0L && inFlightAgeMs > inFlightBudgetMs

/**
 * The device service listens on an Android abstract-namespace socket, reached from the host with
 * `adb forward tcp:<port> localabstract:<name>`. Only the adb shell (uid 2000) or root may connect.
 */
object DeviceTransportSpec {
  const val SOCKET_NAME: String = "adp-render-sandbox"
  const val ADB_REMOTE: String = "localabstract:$SOCKET_NAME"
}

@Serializable
data class DeviceHasAssetsRequest(
  val hashes: List<String>,
)

@Serializable
data class DeviceHasAssetsResponse(
  val missing: List<String>,
)

@Serializable
data class DeviceRenderTarget(
  val direction: String,
  val screen: String,
  val viewName: String,
  val overlayId: String? = null,
  val densityScale: Float = 2f,
  val fontScale: Float = 1f,
  val darkOverride: Boolean? = null,
  val seedOverride: String? = null,
  val variantOverride: String? = null,
  val frameFormat: FrameFormat = FrameFormat.NONE,
  val includeEndFrame: Boolean = true,
)

@Serializable
data class DeviceBatchRequest(
  val dexHash: String,
  val entryClass: String,
  val sourceFile: String,
  val sourceText: String,
  val designTheme: ThemeOverride? = null,
  val assetMap: Map<String, String> = emptyMap(),
  val targets: List<DeviceRenderTarget>,
  val timeoutMs: Long = 15_000L,
  val forceNewClassLoader: Boolean = false,
  val explorationDir: String? = null,
)

@Serializable
data class DeviceOverlayMeta(
  val id: String,
  val screen: String? = null,
)

@Serializable
data class DeviceDirectionMeta(
  val name: String,
  val surface: String,
  val startScreen: String,
  val screens: List<String>,
  val overlays: List<DeviceOverlayMeta> = emptyList(),
)

@Serializable
data class DeviceBoardMeta(
  val surface: String,
  val boardName: String? = null,
  val directions: List<DeviceDirectionMeta>,
  val views: List<ViewSpec>,
)

@Serializable
data class DevicePreloadResponse(
  val ok: Boolean,
  val dexHash: String? = null,
  val error: String? = null,
  val boardMeta: DeviceBoardMeta? = null,
  val boardTheme: ThemeOverride? = null,
  val defaultSeed: String? = null,
  val defaultVariant: String? = null,
  val defaultContrast: Double? = null,
)

@Serializable
data class DeviceTargetResult(
  val direction: String,
  val screen: String,
  val viewName: String,
  val overlayId: String? = null,
  val densityScale: Float = 2f,
  val widthPx: Int = 0,
  val heightPx: Int = 0,
  val renderMs: Double = 0.0,
  val pass: Boolean = true,
  val tree: LayoutTree,
  val endTree: LayoutTree? = null,
  val hasFrame: Boolean = false,
  val hasEndFrame: Boolean = false,
  val frameFormat: FrameFormat = FrameFormat.NONE,
  val error: String? = null,
)

@Serializable
data class DeviceBatchResponse(
  val ok: Boolean,
  val error: String? = null,
  val dexLoadMs: Double = 0.0,
  val renderTotalMs: Double = 0.0,
  val reloadCount: Int = 0,
  val recycleRecommended: Boolean = false,
  val recycleReason: String? = null,
  val boardMeta: DeviceBoardMeta? = null,
  val results: List<DeviceTargetResult> = emptyList(),
  val memoryAfter: DeviceMemoryStats? = null,
)

/** Binary acknowledgment returned by `:sandbox` ops without a specialized payload. */
@Serializable
data class DeviceAckResponse(
  val ok: Boolean,
  val error: String? = null,
  val stored: Int? = null,
)

/** Rendered frame metadata and compressed/encoded bytes streamed in a `RENDER_BATCH` response. */
data class DeviceWireFrame(
  val direction: String,
  val screen: String,
  val viewName: String,
  val overlayId: String?,
  val isEndFrame: Boolean,
  val format: FrameFormat,
  val widthPx: Int,
  val heightPx: Int,
  val rawLength: Int,
  val payload: ByteArray,
) {
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is DeviceWireFrame) return false
    return direction == other.direction &&
      screen == other.screen &&
      viewName == other.viewName &&
      overlayId == other.overlayId &&
      isEndFrame == other.isEndFrame &&
      format == other.format &&
      widthPx == other.widthPx &&
      heightPx == other.heightPx &&
      rawLength == other.rawLength &&
      payload.contentEquals(other.payload)
  }

  override fun hashCode(): Int {
    var result = direction.hashCode()
    result = 31 * result + screen.hashCode()
    result = 31 * result + viewName.hashCode()
    result = 31 * result + (overlayId?.hashCode() ?: 0)
    result = 31 * result + isEndFrame.hashCode()
    result = 31 * result + format.hashCode()
    result = 31 * result + widthPx
    result = 31 * result + heightPx
    result = 31 * result + rawLength
    result = 31 * result + payload.contentHashCode()
    return result
  }
}

/** Decoded `PRELOAD_DEX` wire request carrying identity metadata and compiled DEX bytes. */
data class PreloadDexWireRequest(
  val dexHash: String,
  val entryClass: String,
  val dexBytes: ByteArray,
) {
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is PreloadDexWireRequest) return false
    return dexHash == other.dexHash &&
      entryClass == other.entryClass &&
      dexBytes.contentEquals(other.dexBytes)
  }

  override fun hashCode(): Int {
    var result = dexHash.hashCode()
    result = 31 * result + entryClass.hashCode()
    result = 31 * result + dexBytes.contentHashCode()
    return result
  }
}

/** Decoded `RENDER_BATCH` wire request pairing optional inline DEX bytes with batch parameters. */
data class RenderBatchWireRequest(
  val dexBytes: ByteArray?,
  val entryClass: String,
  val request: DeviceBatchRequest,
) {
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is RenderBatchWireRequest) return false
    return dexBytes.contentEquals(other.dexBytes) &&
      entryClass == other.entryClass &&
      request == other.request
  }

  override fun hashCode(): Int {
    var result = dexBytes?.contentHashCode() ?: 0
    result = 31 * result + entryClass.hashCode()
    result = 31 * result + request.hashCode()
    return result
  }
}

/**
 * Decoded `SET_LIVE` wire request pairing optional inline DEX bytes with live-viewer focus state.
 */
data class SetLiveWireRequest(
  val dexBytes: ByteArray?,
  val request: DeviceLiveStateRequest,
) {
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is SetLiveWireRequest) return false
    return dexBytes.contentEquals(other.dexBytes) && request == other.request
  }

  override fun hashCode(): Int {
    var result = dexBytes?.contentHashCode() ?: 0
    result = 31 * result + request.hashCode()
    return result
  }
}

/** Decoded `RENDER_BATCH` response paired with its streamed [DeviceWireFrame] payloads. */
data class DeviceBatchWireResult(
  val response: DeviceBatchResponse,
  val frames: List<DeviceWireFrame>,
)

@Serializable
data class DeviceLiveStateRequest(
  val dexHash: String,
  val entryClass: String,
  val sourceFile: String = "",
  val designTheme: ThemeOverride? = null,
  val assetMap: Map<String, String> = emptyMap(),
  val direction: String? = null,
  val screen: String? = null,
  val overlayId: String? = null,
  val darkOverride: Boolean? = null,
  val seedOverride: String? = null,
  val variantOverride: String? = null,
  val fontScale: Float = 1f,
)
