package bot.charlies.androiddesign.preview.app.server

import bot.charlies.androiddesign.preview.app.cli.ReportTheme
import bot.charlies.androiddesign.preview.domain.ViewCheck
import bot.charlies.androiddesign.preview.domain.layout.ViewLayout
import bot.charlies.androiddesign.preview.model.Bounds
import bot.charlies.androiddesign.preview.model.LayoutTree
import bot.charlies.androiddesign.preview.model.ValidationError
import bot.charlies.androiddesign.preview.model.ViewSpec
import kotlinx.serialization.Serializable

@Serializable
data class StatusResponse(
  val ok: Boolean = true,
  val running: Boolean = true,
  val pid: Long,
  val port: Int,
  val jarVersion: String,
  val startedAt: Long,
  val compileCount: Int,
  val repoRoot: String,
  val boardUrl: String,
  val activeFile: String? = null,
)

@Serializable
data class CheckRequest(
  val path: String,
  val design: String? = null,
  val direction: String? = null,
  val screen: String? = null,
  val view: String? = null,
)

@Serializable
data class ImageOutput(
  val direction: String,
  val screen: String,
  val overlay: String? = null,
  val view: String,
  val path: String,
  val density: Int,
  val pass: Boolean,
  val endPath: String? = null,
)

@Serializable
data class CheckRenderSummary(
  val direction: String,
  val screen: String,
  val overlay: String? = null,
  val view: String,
  val pass: Boolean,
  val renderMs: Long,
  val check: ViewCheck,
  val failures: List<String> = emptyList(),
)

@Serializable
data class CheckResponse(
  val ok: Boolean,
  val valid: Boolean,
  val pass: Boolean,
  val scoped: Boolean,
  val contentHash: String? = null,
  val compileMs: Long = 0L,
  val renderMs: Long = 0L,
  val compileCount: Int = 0,
  val cached: Boolean = false,
  val surface: String = "phone",
  val boardName: String? = null,
  val sourcePath: String? = null,
  val designPath: String? = null,
  val boardUrl: String? = null,
  val errors: List<ValidationError> = emptyList(),
  val renders: List<CheckRenderSummary> = emptyList(),
  val images: List<ImageOutput> = emptyList(),
  val selection: NodeSelection? = null,
  val error: String? = null,
  val next: String? = null,
  val ask: String? = null,
  val tell: String? = null,
)

@Serializable
data class TreeRequest(
  val path: String,
  val direction: String? = null,
  val screen: String? = null,
  val overlay: String? = null,
  val view: String? = null,
  val maxDepth: Int? = null,
)

@Serializable
data class TreeResponse(
  val ok: Boolean,
  val direction: String? = null,
  val screen: String? = null,
  val overlay: String? = null,
  val view: String? = null,
  val tree: LayoutTree? = null,
  val error: String? = null,
  val next: String? = null,
  val tell: String? = null,
)

@Serializable
data class SelectionResponse(
  val ok: Boolean,
  val selection: NodeSelection? = null,
  val error: String? = null,
  val ask: String? = null,
  val next: String? = null,
  val tell: String? = null,
)

@Serializable
data class BoardFocusRequest(
  val path: String? = null,
  val direction: String,
  val screen: String? = null,
  val overlay: String? = null,
  val view: String,
)

@Serializable
data class BoardSelectionRequest(
  val path: String? = null,
  val file: String,
  val line: Int,
  val callName: String,
  val callChain: List<String> = emptyList(),
  val direction: String,
  val screen: String,
  val overlay: String? = null,
  val view: String,
  val bounds: Bounds,
  val contentHash: String,
)

@Serializable
data class BoardRenderRequest(
  val direction: String? = null,
  val screen: String? = null,
  val overlay: String? = null,
  val view: String? = null,
  val seed: String? = null,
  val variant: String? = null,
  val dark: Boolean? = null,
)

@Serializable
data class BoardRenderResponse(
  val direction: String,
  val screen: String,
  val overlay: String? = null,
  val view: String,
  val contentHash: String,
  val pass: Boolean,
  val renderMs: Long,
  val compileCount: Int,
  val check: ViewCheck,
  val layout: ViewLayout? = null,
  val tree: LayoutTree? = null,
  val pngBase64: String? = null,
  val endPngBase64: String? = null,
  val failures: List<String> = emptyList(),
)

@Serializable
data class BoardViewRef(
  val direction: String,
  val screen: String,
  val overlay: String? = null,
  val view: String,
)

@Serializable data class OverlayMeta(val id: String, val screen: String? = null)

@Serializable
data class DirectionMeta(
  val name: String,
  val surface: String,
  val startScreen: String,
  val screens: List<String>,
  val overlays: List<OverlayMeta>,
)

@Serializable
data class BoardMeta(
  val directions: List<DirectionMeta>,
  val views: List<ViewSpec>,
)

@Serializable
data class BoardPayload(
  val valid: Boolean,
  val pass: Boolean,
  val contentHash: String,
  val compileMs: Long,
  val renderMs: Long,
  val compileCount: Int,
  val surface: String,
  val boardName: String? = null,
  val sourcePath: String,
  val sourceFile: String,
  val designPath: String? = null,
  val theme: ReportTheme,
  val errors: List<ValidationError> = emptyList(),
  val boardMeta: BoardMeta? = null,
  val renders: List<BoardRenderResponse> = emptyList(),
  val staleViews: List<BoardViewRef> = emptyList(),
  val updatedAt: Long,
)

@Serializable
data class StaleEventPayload(
  val contentHash: String,
  val sourcePath: String,
  val views: List<BoardViewRef> = emptyList(),
)

@Serializable
data class HaltedEventPayload(
  val contentHash: String,
  val scope: String,
  val restartCommand: String,
)

@Serializable
data class ErrorEnvelope(
  val ok: Boolean = false,
  val error: String,
  val message: String,
  val next: String? = null,
  val ask: String? = null,
  val tell: String? = null,
)
