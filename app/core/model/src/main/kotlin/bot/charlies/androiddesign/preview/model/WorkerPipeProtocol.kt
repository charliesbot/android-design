package bot.charlies.androiddesign.preview.model

import kotlinx.serialization.Serializable

/** Opcodes for the persistent stdio pipe between the host server and the layoutlib JVM worker. */
object WorkerPipeOp {
  const val PING = 1
  const val PRELOAD_CLASSES = 2
  const val RENDER_BATCH = 3
}

/** Java heap stats reported by the layoutlib child JVM worker. */
@Serializable
data class WorkerMemoryStats(
  val javaHeapUsedBytes: Long = 0L,
  val javaHeapMaxBytes: Long = 0L,
)

@Serializable
data class WorkerPingResponse(
  val ok: Boolean = true,
  val pid: Int = 0,
  val reloadCount: Int = 0,
  val loadedClassHashes: List<String> = emptyList(),
  val memory: WorkerMemoryStats? = null,
)

@Serializable
data class WorkerRenderTarget(
  val direction: String,
  val screen: String,
  val viewName: String,
  val overlayId: String? = null,
  val densityScale: Float = 2f,
  val darkOverride: Boolean? = null,
  val seedOverride: String? = null,
  val variantOverride: String? = null,
  val includeFrame: Boolean = true,
  val includeEndFrame: Boolean = false,
)

@Serializable
data class WorkerBatchRequest(
  val classHash: String,
  val entryClass: String,
  val sourceFile: String,
  val sourceText: String,
  val designTheme: ThemeOverride? = null,
  val targets: List<WorkerRenderTarget>,
  val timeoutMs: Long = 60_000L,
  val explorationDir: String? = null,
)

@Serializable
data class WorkerPreloadResponse(
  val ok: Boolean,
  val classHash: String = "",
  val error: String? = null,
  val boardMeta: BoardMeta? = null,
  val boardTheme: ThemeOverride? = null,
)

@Serializable
data class WorkerTargetResult(
  val direction: String,
  val screen: String,
  val viewName: String,
  val overlayId: String? = null,
  val renderMs: Double = 0.0,
  val tree: LayoutTree? = null,
  val endTree: LayoutTree? = null,
  val error: String? = null,
)

@Serializable
data class WorkerBatchResponse(
  val ok: Boolean,
  val renderTotalMs: Double = 0.0,
  val reloadCount: Int = 0,
  val recycleRecommended: Boolean = false,
  val error: String? = null,
  val results: List<WorkerTargetResult> = emptyList(),
)

@Serializable
data class WorkerAckResponse(
  val ok: Boolean,
  val error: String? = null,
)

/** Raw RGBA frame produced by the layoutlib worker for a rendered target. */
data class WorkerWireFrame(
  val direction: String,
  val screen: String,
  val viewName: String,
  val overlayId: String? = null,
  val isEndFrame: Boolean = false,
  val widthPx: Int = 0,
  val heightPx: Int = 0,
  val payload: ByteArray = ByteArray(0),
) {
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is WorkerWireFrame) return false
    return direction == other.direction &&
      screen == other.screen &&
      viewName == other.viewName &&
      overlayId == other.overlayId &&
      isEndFrame == other.isEndFrame &&
      widthPx == other.widthPx &&
      heightPx == other.heightPx &&
      payload.contentEquals(other.payload)
  }

  override fun hashCode(): Int {
    var result = direction.hashCode()
    result = 31 * result + screen.hashCode()
    result = 31 * result + viewName.hashCode()
    result = 31 * result + (overlayId?.hashCode() ?: 0)
    result = 31 * result + isEndFrame.hashCode()
    result = 31 * result + widthPx
    result = 31 * result + heightPx
    result = 31 * result + payload.contentHashCode()
    return result
  }
}

/** Decoded `PRELOAD_CLASSES` request received by the layoutlib worker. */
data class WorkerPreloadWireRequest(
  val classHash: String,
  val entryClass: String,
  val classBundle: ByteArray,
) {
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is WorkerPreloadWireRequest) return false
    return classHash == other.classHash &&
      entryClass == other.entryClass &&
      classBundle.contentEquals(other.classBundle)
  }

  override fun hashCode(): Int {
    var result = classHash.hashCode()
    result = 31 * result + entryClass.hashCode()
    result = 31 * result + classBundle.contentHashCode()
    return result
  }
}

/** Decoded `RENDER_BATCH` request with optional inline class bundle bytes. */
data class WorkerBatchWireRequest(
  val classBundle: ByteArray?,
  val entryClass: String,
  val request: WorkerBatchRequest,
) {
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is WorkerBatchWireRequest) return false
    val bytesEqual =
      (classBundle == null && other.classBundle == null) ||
        (classBundle != null &&
          other.classBundle != null &&
          classBundle.contentEquals(other.classBundle))
    return bytesEqual && entryClass == other.entryClass && request == other.request
  }

  override fun hashCode(): Int {
    var result = classBundle?.contentHashCode() ?: 0
    result = 31 * result + entryClass.hashCode()
    result = 31 * result + request.hashCode()
    return result
  }
}

/** Paired batch metadata response and raw RGBA frame payloads returned from `RENDER_BATCH`. */
data class WorkerBatchWireResult(
  val response: WorkerBatchResponse,
  val frames: List<WorkerWireFrame>,
)
