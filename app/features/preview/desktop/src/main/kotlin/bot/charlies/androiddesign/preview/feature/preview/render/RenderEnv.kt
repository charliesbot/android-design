package bot.charlies.androiddesign.preview.feature.preview.render

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.FloatingActionButtonMenuScope
import androidx.compose.runtime.Stable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import bot.charlies.androiddesign.preview.domain.OverlayPlacement
import bot.charlies.androiddesign.preview.domain.navigation.AppNavigation
import bot.charlies.androiddesign.preview.domain.navigation.NavigationGraph
import bot.charlies.androiddesign.preview.feature.preview.record.TreeRecorder
import bot.charlies.androiddesign.preview.feature.preview.theme.PreviewTheme
import bot.charlies.androiddesign.preview.feature.preview.theme.ResolvedTheme
import bot.charlies.androiddesign.preview.feature.preview.theme.wear.WearTheme
import bot.charlies.androiddesign.preview.model.Catalog
import bot.charlies.androiddesign.preview.model.Catalogs
import bot.charlies.androiddesign.preview.model.ColorRole
import bot.charlies.androiddesign.preview.model.ComponentNode
import bot.charlies.androiddesign.preview.model.PaneRole
import bot.charlies.androiddesign.preview.model.Proposal
import bot.charlies.androiddesign.preview.model.Screen
import bot.charlies.androiddesign.preview.model.SizeClass
import bot.charlies.androiddesign.preview.model.StyleDef
import bot.charlies.androiddesign.preview.model.TypeRole
import bot.charlies.androiddesign.preview.model.WearCatalog
import java.nio.file.Path

/**
 * Everything a proposal changes locally: toggles, selections, typed text, slider positions, and
 * which overlay is open on which screen. Keys are node instance ids, qualified by the screen.
 */
@Stable
class InteractionState {
  private val values = mutableStateMapOf<String, Any>()
  private val overlays = mutableStateMapOf<String, String>()

  /** The value kept under [key] when it is a [T]; a value of another type reads as absent. */
  inline operator fun <reified T : Any> get(key: String): T? = raw(key) as? T

  @PublishedApi internal fun raw(key: String): Any? = values[key]

  operator fun set(key: String, value: Any) {
    values[key] = value
  }

  /** Whether a toggle, selection, slider or text field holds a value of its own. */
  fun hasValues(): Boolean = values.isNotEmpty()

  /** The overlay open on [screen], or null. */
  fun openOverlay(screen: String): String? = overlays[screen]

  fun open(screen: String, overlay: String) {
    overlays[screen] = overlay
  }

  fun dismiss(screen: String) {
    overlays.remove(screen)
  }

  /** Forgets all local state, e.g. after the proposal changed. */
  fun reset() {
    values.clear()
    overlays.clear()
  }

  /** Makes this state equal to [other], so a headless render can show what a window shows. */
  fun replaceWith(other: InteractionState) {
    val otherValues = other.values.toMap()
    val otherOverlays = other.overlays.toMap()
    values.clear()
    values.putAll(otherValues)
    overlays.clear()
    overlays.putAll(otherOverlays)
  }
}

/**
 * The state of one running proposal: the back stack of screen ids, the local [interaction] state,
 * and where relative images load from. Actions call into it.
 */
@Stable
class ProposalHostState(proposal: Proposal, proposalDir: Path? = null, screen: String? = null) {
  var proposal: Proposal by mutableStateOf(proposal)
    private set

  var proposalDir: Path? by mutableStateOf(proposalDir)
    private set

  val interaction = InteractionState()

  val backStack: SnapshotStateList<String> =
    mutableStateListOf<String>().apply {
      addAll(NavigationGraph(proposal).backStack(screen ?: proposal.navigation.start))
    }

  val current: String
    get() = backStack.last()

  /** Replaces the proposal after a file change and keeps the screens that still exist. */
  fun update(proposal: Proposal, proposalDir: Path?) {
    this.proposal = proposal
    this.proposalDir = proposalDir
    val kept = backStack.filter { it in proposal.screens }
    backStack.clear()
    backStack.addAll(kept.ifEmpty { listOf(proposal.navigation.start) })
  }

  /**
   * Takes over the back stack and the local state of [other], which runs the same proposal. A
   * headless render uses it to show exactly what a live window shows, open overlays included.
   */
  fun adoptStateOf(other: ProposalHostState) {
    val stack = other.backStack.toList().filter { it in proposal.screens }
    if (stack.isNotEmpty()) {
      backStack.clear()
      backStack.addAll(stack)
    }
    interaction.replaceWith(other.interaction)
  }

  /** Shows [screen] the way the shortest path from the start would reach it. */
  fun showScreen(screen: String) {
    if (screen !in proposal.screens) return
    backStack.clear()
    backStack.addAll(NavigationGraph(proposal).backStack(screen))
    interaction.reset()
  }

  fun navigate(screen: String) {
    if (screen !in proposal.screens) return
    val existing = backStack.indexOf(screen)
    if (existing >= 0) {
      while (backStack.size > existing + 1) backStack.removeAt(backStack.lastIndex)
      return
    }
    val panes = proposal.navigation.panes
    // A detail replaces the detail beside the list instead of stacking on it.
    if (panes[screen] == PaneRole.Detail && panes[backStack.last()] == PaneRole.Detail) {
      backStack.removeAt(backStack.lastIndex)
    }
    backStack.add(screen)
  }

  /** A tap on a destination of the app-level navigation: that screen is the whole back stack. */
  fun openDestination(screen: String) {
    if (screen !in proposal.screens) return
    val stack = AppNavigation.stackAfterTap(screen)
    backStack.clear()
    backStack.addAll(stack)
  }

  fun back() {
    if (backStack.size > 1) backStack.removeAt(backStack.lastIndex)
  }
}

/** Colors behind and inside the component being drawn, for contrast and for child defaults. */
internal class Ambient(val container: ColorRole, val content: ColorRole)

internal val LocalAmbient = compositionLocalOf { Ambient(ColorRole.Surface, ColorRole.OnSurface) }

/**
 * What a screen needs to draw: its components by id, the theme and view, the shared state, and the
 * recorder. One per screen being drawn.
 */
internal class RenderEnv(
  val screenId: String,
  val screen: Screen,
  val host: ProposalHostState,
  val theme: PreviewTheme,
  val sizeClass: SizeClass,
  val recorder: TreeRecorder?,
  /** Prepended to every node id: empty for the screen the render asks about. */
  val idPrefix: String,
  val assets: AssetLoader,
  /** The width the `sizeClass` overrides are read at: the frame's, in dp. */
  val widthDp: Int = sizeClass.minWidthDp,
) {
  /** The catalog the screen draws from, which decides how every component is drawn. */
  val catalog: Catalog = Catalogs.of(screen)

  val isWear: Boolean
    get() = catalog === WearCatalog

  /** The phone theme; the phone's renderers are the only ones that ask for it. */
  val phone: ResolvedTheme
    get() = theme as ResolvedTheme

  /** The Wear theme; the Wear renderers are the only ones that ask for it. */
  val wear: WearTheme
    get() = theme as WearTheme

  val nodes: Map<String, ComponentNode> = screen.components.associateBy { it.id }

  val design = theme.design

  /** Which overlays draw beside a component and which at screen level. */
  val placement = OverlayPlacement(screen)

  /** The overlay open on this screen, or null. */
  val openOverlay: String?
    get() = host.interaction.openOverlay(screenId)

  fun open(overlay: String) = host.interaction.open(screenId, overlay)

  fun dismiss() = host.interaction.dismiss(screenId)

  fun stateKey(instanceId: String) = "$screenId::$instanceId"

  fun color(role: ColorRole) = phone.color(role)

  /** The neutral block that stands in for a picture that cannot be read. */
  fun neutralFill(): Color =
    if (isWear) wear.color("surfaceContainer") else color(ColorRole.SurfaceVariant)

  fun spacing(name: String?): Dp? = name?.let { design.spacing[it]?.dp }

  fun typeStyle(role: TypeRole) = phone.style(role)

  fun style(name: String?): StyleDef? = name?.let { design.styles[it] }

  fun act(action: ActionCall?) {
    when (action) {
      null -> Unit
      is ActionCall.Navigate -> host.navigate(action.screen)
      ActionCall.Back -> host.back()
      is ActionCall.Open -> open(action.overlay)
      ActionCall.Dismiss -> dismiss()
    }
  }

  fun isOverlay(node: ComponentNode) = node.component in catalog.overlayNames

  /**
   * This screen drawn without recording. A second copy of a component (a search field in its bar
   * and again in its view) draws and acts the same but must not register its nodes twice.
   */
  fun unrecorded() =
    RenderEnv(screenId, screen, host, theme, sizeClass, null, idPrefix, assets, widthDp)
}

internal val LocalRenderEnv =
  compositionLocalOf<RenderEnv> { error("no proposal screen is being drawn") }

/**
 * What the parent offers its children. Compose scopes give `weight`, and the parent decides whether
 * a child stretches across its width: the catalog says which parents do
 * ([bot.charlies.androiddesign.preview.model.ComponentDef.stretchesChildren]). A Row never does,
 * because that would push its siblings out (use `weight` there).
 *
 * @property flexItem The parent is a FlexBox, which measures its items with a finite stand-in for
 *   an unbounded height; an item that grows its semantics bounds gets it back (see
 *   [NodeContext.modifier]).
 * @property groupIndex The child's position among its siblings, when the parent is a group.
 * @property groupCount How many siblings there are.
 */
internal data class ChildHost(
  val fillWidth: Boolean = true,
  val fillHeight: Boolean = false,
  val column: ColumnScope? = null,
  val row: RowScope? = null,
  val fabMenu: FloatingActionButtonMenuScope? = null,
  val flexItem: Boolean = false,
  val groupKey: String? = null,
  val groupIndex: Int = 0,
  val groupCount: Int = 1,
  val connected: Boolean = false,
  val segmented: Boolean = false,
  /** The parent is a toolbar, whose content color its icon buttons take. */
  val toolbar: Boolean = false,
  /** The parent is, or sits in, a vibrant `Menu`. */
  val vibrantMenu: Boolean = false,
  /** The parent is a secondary `Tabs` row, whose selected tab is on surface, not primary. */
  val secondaryTabs: Boolean = false,
  /**
   * What the parent's own scope adds to a child's modifier, such as a Wear `ButtonGroup` weight.
   */
  val scopeModifier: Modifier = Modifier,
) {
  fun at(index: Int, count: Int) = copy(groupIndex = index, groupCount = count)

  companion object {
    /** A child with no special parent: stretches across the width. */
    val Flow = ChildHost()

    /** A child that fills its parent, such as a Scaffold's content. */
    val Fill = ChildHost(fillWidth = true, fillHeight = true)

    /** A child of a bar or other wrap-content parent. */
    val Wrap = ChildHost(fillWidth = false)
  }
}
