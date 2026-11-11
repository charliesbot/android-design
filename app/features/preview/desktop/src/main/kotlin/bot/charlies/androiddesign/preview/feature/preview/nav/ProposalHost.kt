package bot.charlies.androiddesign.preview.feature.preview.nav

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.Posture
import androidx.compose.material3.adaptive.WindowAdaptiveInfo
import androidx.compose.material3.adaptive.layout.PaneScaffoldDirective
import androidx.compose.material3.adaptive.layout.ThreePaneScaffoldScope
import androidx.compose.material3.adaptive.layout.calculatePaneScaffoldDirective
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.material3.adaptive.navigation3.SupportingPaneSceneStrategy
import androidx.compose.material3.adaptive.navigation3.rememberListDetailSceneStrategy
import androidx.compose.material3.adaptive.navigation3.rememberSupportingPaneSceneStrategy
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.NavDisplay
import androidx.window.core.layout.WindowSizeClass
import androidx.window.core.layout.computeWindowSizeClass
import bot.charlies.androiddesign.preview.domain.design.DesignSystemResolver
import bot.charlies.androiddesign.preview.domain.navigation.AppNavigation
import bot.charlies.androiddesign.preview.domain.navigation.DetailPlaceholders
import bot.charlies.androiddesign.preview.feature.preview.record.PreviewFrame
import bot.charlies.androiddesign.preview.feature.preview.record.TreeRecorder
import bot.charlies.androiddesign.preview.feature.preview.render.AppNavigationFrame
import bot.charlies.androiddesign.preview.feature.preview.render.ProposalHostState
import bot.charlies.androiddesign.preview.feature.preview.render.ProposalRenderer
import bot.charlies.androiddesign.preview.feature.preview.theme.ProposalTheme
import bot.charlies.androiddesign.preview.model.Catalogs
import bot.charlies.androiddesign.preview.model.DesignSystem
import bot.charlies.androiddesign.preview.model.PaneRole
import bot.charlies.androiddesign.preview.model.SizeClass
import bot.charlies.androiddesign.preview.model.ViewSpec
import bot.charlies.androiddesign.preview.model.Views
import bot.charlies.androiddesign.preview.model.WearCatalog

/**
 * Runs a proposal as a Navigation 3 back stack of screen ids. Screens with a pane role in
 * `navigation.panes` sit beside each other in Material 3's list-detail or supporting-pane scene
 * when the preview frame is wide enough (expanded width), and replace each other otherwise.
 *
 * @param frameSize The size of the preview frame. Pane counts and `showIn` follow it, not the size
 *   of the window the frame is in.
 * @param primaryScreen The screen the recorder reports with plain ids; the other panes get a
 *   `screen/` prefix so equal ids on two screens stay apart.
 */
@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun ProposalHost(
  host: ProposalHostState,
  frameSize: DpSize,
  modifier: Modifier = Modifier,
  recorder: TreeRecorder? = null,
  primaryScreen: String = host.current,
) {
  val sizeClass = SizeClass.forWidth(frameSize.width.value.toInt())
  // NavDisplay keeps each screen's entry content across recompositions, so the entries read these
  // through state: a captured value would stay at what the screen was first drawn with.
  val currentRecorder by rememberUpdatedState(recorder)
  val currentPrimary by rememberUpdatedState(primaryScreen)
  val directive = rememberDirective(frameSize)
  val panes = host.proposal.navigation.panes
  val supportingLayout = PaneRole.Supporting in panes.values
  val listDetail = rememberListDetailSceneStrategy<String>(directive = directive)
  val supporting = rememberSupportingPaneSceneStrategy<String>(directive = directive)
  val twoPanes = directive.maxHorizontalPartitions >= 2
  val emptyDetail = emptyDetailList(host, directive, supportingLayout)
  DisposableEffect(recorder, emptyDetail) {
    emptyDetail?.let { recorder?.reportEmptyDetail(it, empty = true) }
    onDispose { emptyDetail?.let { recorder?.reportEmptyDetail(it, empty = false) } }
  }
  // A new size class starts a new NavDisplay: the nav is rebuilt only when the pane count can
  // change. Keeping one would move a screen's content from the pane scaffold, which measures in a
  // lookahead pass, into a one-pane scene, and the moved content keeps a stale lookahead state:
  // Material 3's clickable ListItem then throws a NullPointerException, and an outer
  // LookaheadScope instead leaves the list blank. Within one class the pane scaffold follows the
  // size itself. The back stack lives in the host, so the window stays on its screen.
  key(sizeClass) {
    // The navigation is drawn around the pane scaffold, outside every pane, and the scaffold is
    // measured in the width that is left: the directive above follows the whole frame, as the
    // adaptive library's own default does (it reads the window, not the room a suite scaffold
    // leaves), so a rail narrows the panes without changing how many there are.
    val destinations = AppNavigation.destinations(host.proposal)
    val stack = host.backStack.toList()
    AppNavigationFrame(
      shown = AppNavigation.shown(host.proposal, sizeClass, stack, twoPanes),
      destinations = destinations,
      selected = AppNavigation.selectedIndex(host.proposal, stack),
      onSelect = host::openDestination,
      recorder = recorder,
      modifier = modifier,
    ) {
      NavDisplay(
        backStack = host.backStack,
        modifier = Modifier.fillMaxSize(),
        sceneStrategies = listOf(if (supportingLayout) supporting else listDetail),
        onBack = { host.back() },
        entryProvider = { screenId ->
          val placeholder: (@Composable ThreePaneScaffoldScope.() -> Unit)? =
            if (DetailPlaceholders.of(host.proposal, screenId) == null) null
            else {
              { DetailPlaceholder(host, screenId, sizeClass, currentRecorder) }
            }
          NavEntry(
            screenId,
            metadata = paneMetadata(panes[screenId], supportingLayout, placeholder),
          ) {
            ProposalRenderer(
              host = host,
              screenId = screenId,
              sizeClass = sizeClass,
              recorder = currentRecorder,
              idPrefix = if (screenId == currentPrimary) "" else "$screenId/",
            )
          }
        },
      )
    }
  }
}

/**
 * The list screen the scene shows beside an empty detail pane, if any: the screen on top is a list
 * pane with no placeholder, and the directive has room for two panes.
 */
@OptIn(ExperimentalMaterial3AdaptiveApi::class)
private fun emptyDetailList(
  host: ProposalHostState,
  directive: PaneScaffoldDirective,
  supportingLayout: Boolean,
): String? {
  val top = host.current
  val isList = host.proposal.navigation.panes[top] == PaneRole.List
  val twoPanes = directive.maxHorizontalPartitions >= 2
  return top.takeIf {
    !supportingLayout && isList && twoPanes && DetailPlaceholders.of(host.proposal, it) == null
  }
}

/** The placeholder screen of the list [listScreen], drawn in the detail pane beside it. */
@Composable
private fun DetailPlaceholder(
  host: ProposalHostState,
  listScreen: String,
  sizeClass: SizeClass,
  recorder: TreeRecorder?,
) {
  val screenId = DetailPlaceholders.of(host.proposal, listScreen) ?: return
  ProposalRenderer(
    host = host,
    screenId = screenId,
    sizeClass = sizeClass,
    recorder = recorder,
    idPrefix = "$screenId/",
  )
}

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
private fun rememberDirective(frameSize: DpSize): PaneScaffoldDirective =
  remember(frameSize) {
    calculatePaneScaffoldDirective(
      WindowAdaptiveInfo(
        WindowSizeClass.BREAKPOINTS_V1.computeWindowSizeClass(
          frameSize.width.value,
          frameSize.height.value,
        ),
        Posture(),
      ),
    )
  }

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
private fun paneMetadata(
  role: PaneRole?,
  supportingLayout: Boolean,
  detailPlaceholder: (@Composable ThreePaneScaffoldScope.() -> Unit)?,
): Map<String, Any> =
  when {
    role == null -> emptyMap()
    supportingLayout ->
      when (role) {
        PaneRole.List,
        PaneRole.Detail -> SupportingPaneSceneStrategy.mainPane()
        PaneRole.Supporting -> SupportingPaneSceneStrategy.supportingPane()
        PaneRole.Extra -> SupportingPaneSceneStrategy.extraPane()
      }
    else ->
      when (role) {
        PaneRole.List ->
          if (detailPlaceholder == null) ListDetailSceneStrategy.listPane()
          else ListDetailSceneStrategy.listPane(detailPlaceholder = detailPlaceholder)
        PaneRole.Detail,
        PaneRole.Supporting -> ListDetailSceneStrategy.detailPane()
        PaneRole.Extra -> ListDetailSceneStrategy.extraPane()
      }
  }

/**
 * The whole preview: theme, frame, and host, drawn at [frame], which defaults to the size of
 * [view]; the window passes the moving size while a device switch animates. The window and the
 * headless renderer both draw this.
 */
@Composable
fun ProposalPreview(
  host: ProposalHostState,
  design: DesignSystem,
  view: ViewSpec,
  frame: DpSize = DpSize(view.width.dp, view.height.dp),
  modifier: Modifier = Modifier,
  recorder: TreeRecorder? = null,
) {
  // The one place the catalog splits the preview: a watch view is drawn by the Wear host, in the
  // Wear theme and inside a device, and everything else by the phone's.
  if (Catalogs.forView(view) === WearCatalog) {
    WearProposalPreview(host, design, view, frame, modifier, recorder)
    return
  }
  ProposalTheme(design, view.dark) {
    PreviewFrame(recorder, modifier.size(frame.width, frame.height)) {
      Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        ProposalHost(
          host = host,
          frameSize = frame,
          recorder = recorder,
        )
      }
    }
  }
}

private val PreviewProposal by lazy {
  kotlinx.serialization.json.Json.decodeFromString(
    bot.charlies.androiddesign.preview.model.Proposal.serializer(),
    """{"format":1,"navigation":{"start":"home"},"screens":{"home":{
      "catalogId":"https://github.com/charliesbot/android-design/catalogs/m3-expressive/v1",
      "components":[
        {"id":"root","component":"Scaffold","content":"hello"},
        {"id":"hello","component":"Text","text":"Hello","type":"headlineLarge","padding":"lg"}
      ]}}}""",
  )
}

@Preview
@Composable
private fun ProposalPreviewPreview() {
  val host = remember { ProposalHostState(PreviewProposal) }
  ProposalPreview(host, DesignSystemResolver.resolve(null, null), Views.Light)
}

@Preview
@Composable
private fun ProposalHostPreview() {
  val host = remember { ProposalHostState(PreviewProposal) }
  ProposalTheme(DesignSystemResolver.resolve(null, null), dark = true) {
    ProposalHost(host, DpSize(411.dp, 600.dp))
  }
}
