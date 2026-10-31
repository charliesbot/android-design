package bot.charlies.androiddesign.preview.app.cli

import bot.charlies.androiddesign.preview.model.ValidationError
import kotlinx.serialization.Serializable

/** The merged DESIGN.md and proposal theme as the render used it. */
@Serializable data class ReportTheme(val seed: String, val variant: String, val contrast: Double)

/**
 * One screen at one view. [png] and [tree] are file names relative to the output folder, or null
 * when the render threw (its [failures] then start with `render-error`).
 */
@Serializable
data class RenderEntry(
  val screen: String,
  val view: String,
  val png: String?,
  val tree: String?,
  val pass: Boolean,
  val failures: List<String>,
)

/** `report.json`, which the `render` command writes next to the images. */
@Serializable
data class RenderReport(
  val proposal: String,
  val design: String?,
  val theme: ReportTheme,
  val valid: Boolean,
  val errors: List<ValidationError>,
  val renders: List<RenderEntry>,
  val pass: Boolean,
)
