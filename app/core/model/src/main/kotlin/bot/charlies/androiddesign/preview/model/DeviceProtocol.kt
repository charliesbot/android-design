package bot.charlies.androiddesign.preview.model

import kotlinx.serialization.Serializable

object DeviceOp {
  const val PING: Int = 1
  const val PRELOAD_DEX: Int = 2
  const val PUT_ASSETS: Int = 3
  const val HAS_ASSETS: Int = 4
  const val RENDER_BATCH: Int = 5
  const val SET_LIVE: Int = 6
  const val RECYCLE: Int = 7
}

@Serializable
enum class FrameFormat {
  NONE,
  LZ4_RGBA,
  WEBP_LOSSLESS,
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
)

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

@Serializable
data class RobolectricRenderRequest(
  val id: String = "1",
  val sourcePath: String,
  val designPath: String? = null,
  val views: List<String>? = null,
  val screens: List<String>? = null,
  val direction: String? = null,
  val overlay: String? = null,
  val outDir: String? = null,
  val density: Float = 2f,
  val encodePng: Boolean = true,
)

@Serializable
data class RobolectricRenderResponse(
  val id: String = "1",
  val ok: Boolean,
  val valid: Boolean,
  val pass: Boolean,
  val compileMs: Long = 0L,
  val renderMs: Long = 0L,
  val surface: String = "phone",
  val boardName: String? = null,
  val errors: List<ValidationError> = emptyList(),
  val trees: List<LayoutTree> = emptyList(),
  val endTrees: List<LayoutTree> = emptyList(),
  val writtenFiles: List<String> = emptyList(),
  val error: String? = null,
)
