package bot.charlies.androiddesign.preview.app.cli

import bot.charlies.androiddesign.preview.domain.ViewCheck
import bot.charlies.androiddesign.preview.model.ValidationError
import kotlinx.serialization.Serializable

/** The merged DESIGN.md and proposal theme as the render used it. */
@Serializable data class ReportTheme(val seed: String, val variant: String, val contrast: Double)

/**
 * One screen at one view. [screen] is `home+sheet` for the render with overlay `sheet` open. [png]
 * and [tree] are file names relative to the output folder, or null when the render threw (its
 * [failures] then start with `render-error`). [tree] covers a scrolling Wear screen's whole list
 * while [png] shows its top; [endPng] and [endTree] are the list scrolled to its end, and null for
 * any other screen.
 */
@Serializable
data class RenderEntry(
  val screen: String,
  val view: String,
  val png: String?,
  val tree: String?,
  val pass: Boolean,
  val failures: List<String>,
  val endPng: String? = null,
  val endTree: String? = null,
) {
  companion object {
    /**
     * The entry for [check]; [files] is the base name of the files the render wrote, null when it
     * threw, and [endFiles] the base name of the end-of-list files, when it wrote any.
     */
    fun of(
      screen: String,
      view: String,
      files: String?,
      check: ViewCheck,
      endFiles: String? = null,
    ) =
      RenderEntry(
        screen = screen,
        view = view,
        png = files?.let { "$it.png" },
        tree = files?.let { "$it.tree.json" },
        pass = check.pass,
        failures = check.failures(),
        endPng = endFiles?.let { "$it.png" },
        endTree = endFiles?.let { "$it.tree.json" },
      )
  }
}

/**
 * One `rule: id detail` line per component in each error, or `rule: detail` for one about the whole
 * view, which is what `evals/checks.py` reads the rule from.
 */
private fun ViewCheck.failures(): List<String> = errors.flatMap { error ->
  error.ids
    .ifEmpty { listOf(null) }
    .map { id ->
      "${error.rule}: ${listOfNotNull(id, error.detail).joinToString(" ")}".trimEnd(' ', ':')
    }
}

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
