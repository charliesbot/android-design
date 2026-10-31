package bot.charlies.androiddesign.preview.domain

import bot.charlies.androiddesign.preview.model.Device
import bot.charlies.androiddesign.preview.model.LayoutTree
import bot.charlies.androiddesign.preview.model.PaletteVariant
import bot.charlies.androiddesign.preview.model.ValidationError
import kotlinx.serialization.Serializable

/**
 * Thrown for problems the agent can fix, such as an unknown screen. The MCP layer reports it as a
 * tool error.
 */
class PreviewException(message: String) : Exception(message)

/**
 * What the MCP server calls; one method per tool. The app implements it by gluing the proposal
 * repository to the window and the headless renderer.
 */
interface PreviewController {
  /** `open_proposal`: loads and watches [path], shows the window. */
  suspend fun openProposal(path: String): OpenedProposal

  /**
   * `get_tree`: with no arguments, the window's current screen and view; otherwise a headless
   * render of [screen] at the view named [view] (light, dark, foldable, tablet, desktop). While the
   * window is hidden it is always headless.
   */
  suspend fun getTree(screen: String? = null, view: String? = null): LayoutTree

  /** `set_view`: changes the window's device, brightness, seed or variant; null keeps the value. */
  suspend fun setView(change: ViewChange): ViewState

  /** `show_preview`: shows the window if it is hidden. */
  suspend fun showPreview()

  /**
   * `check_proposal`: renders each of [screens] (every screen when null or empty) at each of
   * [views] (every named view when null or empty) headlessly, tree only, and reports which pass.
   */
  suspend fun checkProposal(
    screens: List<String>? = null,
    views: List<String>? = null,
  ): ProposalCheck

  /**
   * `get_catalog`: with no [components], a compact summary of the catalog (every component, its
   * props, the token vocabularies and the action functions); otherwise the full JSON Schema entries
   * of the named components.
   */
  fun getCatalog(components: List<String>? = null): String

  /** `export_kotlin`: Kotlin source for [screen], or for every screen when null. */
  suspend fun exportKotlin(screen: String? = null): String

  /** `status`. */
  suspend fun status(): PreviewStatus
}

/** A change request for `set_view`; viewer choices are never written to the proposal. */
data class ViewChange(
  val device: Device? = null,
  val dark: Boolean? = null,
  /** `#RRGGBB`. */
  val seed: String? = null,
  val variant: PaletteVariant? = null,
)

@Serializable
data class ViewState(
  val device: Device,
  val dark: Boolean,
  /** The viewer's seed override, or null when the proposal's own seed is in use. */
  val seed: String? = null,
  /** The viewer's variant override as a wire name, or null for the proposal's. */
  val variant: String? = null,
)

/**
 * What `check_proposal` returns: small enough to read after every edit. [screens] maps a screen id
 * to its views by name; [ms] is the time the renders took.
 */
@Serializable
data class ProposalCheck(
  val pass: Boolean,
  val ms: Long,
  val screens: Map<String, Map<String, ViewCheck>>,
)

/** One screen at one view. Info findings are only counted; [errors] fail the view. */
@Serializable
data class ViewCheck(
  val pass: Boolean,
  val info: Int = 0,
  val errors: List<CheckError> = emptyList(),
)

/**
 * The error findings of one view that share a [rule] and [detail], with the components they are
 * about in [ids] (empty when the finding concerns the whole view). A rule such as `one-fab` words
 * its detail once for all offenders, so grouping keeps the result small without dropping a node. A
 * render that threw is the rule `render-error`.
 */
@Serializable
data class CheckError(
  val rule: String,
  val ids: List<String> = emptyList(),
  val detail: String? = null,
)

@Serializable
data class OpenedProposal(
  val path: String,
  val name: String? = null,
  val start: String? = null,
  val screens: List<String>,
  val designMd: String? = null,
  val valid: Boolean,
  val errors: List<ValidationError>,
)

@Serializable
data class PreviewStatus(
  val version: String,
  val catalogId: String,
  val proposal: String? = null,
  val windowVisible: Boolean,
  val screen: String? = null,
  val view: ViewState? = null,
  val errors: List<ValidationError> = emptyList(),
)
