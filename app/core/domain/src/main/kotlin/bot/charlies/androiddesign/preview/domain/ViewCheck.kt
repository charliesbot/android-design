package bot.charlies.androiddesign.preview.domain

import bot.charlies.androiddesign.preview.domain.layout.LayoutSummary
import bot.charlies.androiddesign.preview.domain.layout.ViewLayout
import bot.charlies.androiddesign.preview.model.LayoutTree
import bot.charlies.androiddesign.preview.model.Proposal
import bot.charlies.androiddesign.preview.model.Severity
import bot.charlies.androiddesign.preview.model.firstLine
import kotlinx.serialization.Serializable

/**
 * One screen at one view. Info findings are only counted; [errors] fail the view. [layout] says
 * what the view shows, so adaptation can be compared across views without reading a tree; a render
 * that threw has none.
 *
 * This is the one verdict on a view: `check_proposal` returns it and the `render` command's report
 * is flattened from it, so both fail the same views for the same reasons.
 */
@Serializable
data class ViewCheck(
  val pass: Boolean,
  val info: Int = 0,
  val errors: List<CheckError> = emptyList(),
  val layout: ViewLayout? = null,
) {
  /**
   * The verdict on a render of a screen with an overlay open, given [base], the verdict on the same
   * screen at the same view without it: only the errors [base] does not already report, matched by
   * rule and component id (a whole-view error by rule), so a repeat is dropped and what the overlay
   * causes stays. It has no [layout], which is the screen's, and counts only the info findings
   * beyond the base's.
   */
  fun overlaying(base: ViewCheck): ViewCheck {
    val known =
      base.errors.flatMap { e -> e.ids.ifEmpty { listOf(null) }.map { e.rule to it } }.toSet()
    val errors = errors.mapNotNull { e ->
      val ids = e.ids.filter { (e.rule to it) !in known }
      val wholeView = e.ids.isEmpty() && (e.rule to null) !in known
      if (ids.isEmpty() && !wholeView) null else e.copy(ids = ids)
    }
    return ViewCheck(
      pass = errors.isEmpty(),
      info = (info - base.info).coerceAtLeast(0),
      errors = errors,
    )
  }

  companion object {
    /** The rule of a view whose render threw. */
    const val RENDER_ERROR = "render-error"

    /** The verdict on [tree], the render of a screen of [proposal]. */
    fun of(proposal: Proposal, tree: LayoutTree): ViewCheck {
      val errors =
        tree.findings
          .filter { it.severity == Severity.Error }
          .groupBy({ it.rule.wire to it.detail }, { it.id })
          .map { (key, ids) -> CheckError(key.first, ids.filterNotNull(), key.second) }
      return ViewCheck(
        pass = errors.isEmpty(),
        info = tree.findings.count { it.severity == Severity.Info },
        errors = errors,
        layout = LayoutSummary.of(proposal, tree),
      )
    }

    /** The verdict on a view whose render threw [failure]: failed, with its first line as why. */
    fun renderError(failure: Throwable): ViewCheck =
      ViewCheck(
        pass = false,
        errors = listOf(CheckError(RENDER_ERROR, detail = failure.firstLine())),
      )
  }
}

/**
 * The error findings of one view that share a [rule] and [detail], with the components they are
 * about in [ids] (empty when the finding concerns the whole view). A rule such as `one-fab` words
 * its detail once for all offenders, so grouping keeps the result small without dropping a node. A
 * render that threw is the rule [ViewCheck.RENDER_ERROR].
 */
@Serializable
data class CheckError(
  val rule: String,
  val ids: List<String> = emptyList(),
  val detail: String? = null,
)
