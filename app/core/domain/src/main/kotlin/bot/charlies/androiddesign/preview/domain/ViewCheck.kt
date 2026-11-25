package bot.charlies.androiddesign.preview.domain

import bot.charlies.androiddesign.preview.domain.layout.LayoutSummary
import bot.charlies.androiddesign.preview.domain.layout.ViewLayout
import bot.charlies.androiddesign.preview.model.Bounds
import bot.charlies.androiddesign.preview.model.FindingRule
import bot.charlies.androiddesign.preview.model.LayoutTree
import bot.charlies.androiddesign.preview.model.Severity
import bot.charlies.androiddesign.preview.model.SurfaceSpec
import bot.charlies.androiddesign.preview.model.SurfaceSpecs
import bot.charlies.androiddesign.preview.model.firstLine
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

/**
 * One screen at one view. [errors] fail the view; [notes] are info findings, which do not. [layout]
 * says what the view shows, so adaptation can be compared across views without reading a tree.
 *
 * [collapse] writes a view whose verdict equals an earlier view's of the same screen as [same], the
 * name of that view.
 */
@Serializable
@ConsistentCopyVisibility
data class ViewCheck
private constructor(
  @SerialName("pass") private val verdict: Boolean? = null,
  val notes: List<CheckNote> = emptyList(),
  val errors: List<CheckError> = emptyList(),
  val layout: ViewLayout? = null,
  val same: String? = null,
) {
  constructor(
    pass: Boolean,
    notes: List<CheckNote> = emptyList(),
    errors: List<CheckError> = emptyList(),
    layout: ViewLayout? = null,
  ) : this(verdict = pass, notes = notes, errors = errors, layout = layout)

  /**
   * The text this view draws, by component id. [overlaying] compares it across views. It is not
   * serialized and not part of equality, so [collapse] compares verdicts only.
   */
  @Transient private var drawnText: Map<String, DrawnText> = emptyMap()

  /**
   * A text as drawn: the same in two renders of a screen when it is the same text in the same
   * place, whatever id each render gave it.
   */
  private data class DrawnText(val value: String, val bounds: Bounds, val sourceLine: Int?)

  /** Whether the view passes; not defined for an entry that is [same] as another view's. */
  val pass: Boolean
    get() = checkNotNull(verdict) { "this entry repeats the view '$same'; read that one" }

  /** Whether the render threw ([RENDER_ERROR]) instead of producing a layout to check. */
  val isRenderError: Boolean
    get() = errors.any { it.rule == RENDER_ERROR }

  /**
   * The verdict on a render of a screen with an overlay open, given [base], the verdict on the same
   * screen at the same view without it: only the errors and notes [base] does not already report,
   * matched by rule and component id (a whole-view one by rule), so a repeat is dropped and what
   * the overlay causes stays. It has no [layout], which is the screen's.
   *
   * Contrast on text [base] draws in the same place is dropped too: the overlay may cover that
   * text, and the sampled backdrop is then the overlay's own pixels, not the text's. Where the
   * overlay does not cover it, the pixels match [base] and so does the verdict. Text is matched by
   * what and where it draws, not id, because an overlay that draws the screen again gives its copy
   * new ids.
   */
  fun overlaying(base: ViewCheck): ViewCheck {
    val baseText = base.drawnText.values.toSet()
    val ownErrors = errors.mapNotNull { e ->
      if (e.rule != FindingRule.Contrast.wire) return@mapNotNull e
      val ids = e.ids.filter { drawnText[it] !in baseText }
      if (ids.isEmpty() && e.ids.isNotEmpty()) null else e.copy(ids = ids)
    }
    val errors =
      ownErrors.beyond(base.errors, { it.rule }, { it.ids }) { e, ids -> e.copy(ids = ids) }
    val notes = notes.beyond(base.notes, { it.rule }, { it.ids }) { n, ids -> n.copy(ids = ids) }
    return ViewCheck(pass = errors.isEmpty(), notes = notes, errors = errors)
  }

  companion object {
    /** The rule of a view whose render threw. */
    const val RENDER_ERROR = "render-error"

    /** Names a render of [screen] with [overlay] open (`screen+overlay`). */
    fun renderOverlayName(screen: String, overlay: String): String = "$screen+$overlay"

    /** The verdict on an `.explore.kt` [tree] rendered under [surface]. */
    fun of(tree: LayoutTree, surface: SurfaceSpec = SurfaceSpecs.forView(tree.view)): ViewCheck {
      val errors =
        tree.findings
          .filter { it.severity == Severity.Error }
          .groupBy { Triple(it.rule.wire, it.detail, it.reference to it.hint) }
          .map { (key, group) ->
            CheckError(
              rule = key.first,
              ids = group.mapNotNull { it.id },
              detail = key.second,
              reference = key.third.first,
              hint = key.third.second,
            )
          }
      val notes =
        tree.findings
          .filter { it.severity == Severity.Info }
          .groupBy { it.rule.wire }
          .map { (rule, group) ->
            val first = group.firstOrNull()
            CheckNote(
              rule = rule,
              ids = group.mapNotNull { it.id },
              reference = first?.reference,
              hint = first?.hint,
            )
          }
      return ViewCheck(
          pass = errors.isEmpty(),
          notes = notes,
          errors = errors,
          layout = LayoutSummary.of(tree, surface),
        )
        .apply {
          drawnText =
            tree.nodes
              .mapNotNull { n ->
                n.text?.let { n.id to DrawnText(it.value, n.bounds, n.sourceLine) }
              }
              .toMap()
        }
    }

    /** The entry for a view whose verdict equals the one of the view named [view]. */
    fun same(view: String): ViewCheck = ViewCheck(same = view)

    /**
     * [views] (by view name, for one screen) with every view whose verdict equals an earlier view's
     * replaced by [same] that view: always the first view with that verdict, never another [same]
     * entry. Views that differ in anything, `pass`, errors, notes or layout, stay whole.
     */
    fun collapse(views: Map<String, ViewCheck>): Map<String, ViewCheck> {
      val firsts = mutableListOf<Pair<String, ViewCheck>>()
      return views.mapValues { (name, check) ->
        val first = firsts.firstOrNull { it.second == check }
        if (first == null) {
          firsts += name to check
          check
        } else {
          same(first.first)
        }
      }
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
 * about in [ids] (empty when the finding concerns the whole view). A render that threw is the rule
 * [ViewCheck.RENDER_ERROR].
 */
@Serializable
data class CheckError(
  val rule: String,
  val ids: List<String> = emptyList(),
  val detail: String? = null,
  val reference: String? = null,
  val hint: String? = null,
)

/**
 * The info findings of one view that share a [rule], with the components they are about in [ids]
 * (empty when the finding concerns the whole view).
 */
@Serializable
data class CheckNote(
  val rule: String,
  val ids: List<String> = emptyList(),
  val reference: String? = null,
  val hint: String? = null,
)

private fun <T> List<T>.beyond(
  known: List<T>,
  rule: (T) -> String,
  ids: (T) -> List<String>,
  withIds: (T, List<String>) -> T,
): List<T> {
  val seen = known.flatMap { e -> ids(e).ifEmpty { listOf(null) }.map { rule(e) to it } }.toSet()
  return mapNotNull { e ->
    val fresh = ids(e).filter { (rule(e) to it) !in seen }
    val wholeView = ids(e).isEmpty() && (rule(e) to null) !in seen
    if (fresh.isEmpty() && !wholeView) null else withIds(e, fresh)
  }
}
