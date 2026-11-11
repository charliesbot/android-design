package bot.charlies.androiddesign.preview.domain

import bot.charlies.androiddesign.preview.domain.layout.LayoutSummary
import bot.charlies.androiddesign.preview.domain.layout.ViewLayout
import bot.charlies.androiddesign.preview.model.LayoutTree
import bot.charlies.androiddesign.preview.model.Proposal
import bot.charlies.androiddesign.preview.model.Severity
import bot.charlies.androiddesign.preview.model.firstLine
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * One screen at one view. [errors] fail the view; [notes] are the info findings, which do not. A
 * note is a finding to look at, not a failure, so a view with notes still passes. [layout] says
 * what the view shows, so adaptation can be compared across views without reading a tree; a render
 * that threw has none.
 *
 * This is the one verdict on a view: `check_proposal` returns it and the `render` command's report
 * is flattened from it, so both fail the same views for the same reasons.
 *
 * `check_proposal` writes a view whose verdict equals an earlier view's of the same screen as just
 * [same], the name of that view ([collapse]). Such an entry has no verdict of its own: [same] is
 * its only content, and [pass] says so rather than guess. [same] cannot be set by any other
 * constructor, so a value cannot claim to repeat a view and also carry a verdict.
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

  /** Whether the view passes; not defined for an entry that is [same] as another view's. */
  val pass: Boolean
    get() = checkNotNull(verdict) { "this entry repeats the view '$same'; read that one" }

  /**
   * The verdict on a render of a screen with an overlay open, given [base], the verdict on the same
   * screen at the same view without it: only the errors and notes [base] does not already report,
   * matched by rule and component id (a whole-view one by rule), so a repeat is dropped and what
   * the overlay causes stays. It has no [layout], which is the screen's.
   */
  fun overlaying(base: ViewCheck): ViewCheck {
    val errors = errors.beyond(base.errors, { it.rule }, { it.ids }) { e, ids -> e.copy(ids = ids) }
    val notes = notes.beyond(base.notes, { it.rule }, { it.ids }) { n, ids -> n.copy(ids = ids) }
    return ViewCheck(pass = errors.isEmpty(), notes = notes, errors = errors)
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
      val notes =
        tree.findings
          .filter { it.severity == Severity.Info }
          .groupBy({ it.rule.wire }, { it.id })
          .map { (rule, ids) -> CheckNote(rule, ids.filterNotNull()) }
      return ViewCheck(
        pass = errors.isEmpty(),
        notes = notes,
        errors = errors,
        layout = LayoutSummary.of(proposal, tree),
      )
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

/**
 * The info findings of one view that share a [rule], with the components they are about in [ids]
 * (empty when the finding concerns the whole view). A note has no detail: the rule names it, and
 * `get_tree` has the rest.
 */
@Serializable data class CheckNote(val rule: String, val ids: List<String> = emptyList())

/**
 * The entries of [this] that [known] does not already report, matched by rule and component id (an
 * entry with no ids by rule), each with only its new ids.
 */
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
