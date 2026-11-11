package bot.charlies.androiddesign.preview.feature.preview.render

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ShortNavigationBar
import androidx.compose.material3.ShortNavigationBarArrangement
import androidx.compose.material3.ShortNavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.material3.WideNavigationRail
import androidx.compose.material3.WideNavigationRailItem
import androidx.compose.material3.WideNavigationRailValue
import androidx.compose.material3.rememberWideNavigationRailState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import bot.charlies.androiddesign.preview.domain.BadgeContent
import bot.charlies.androiddesign.preview.domain.navigation.AppNavigation
import bot.charlies.androiddesign.preview.feature.preview.record.NodeMeta
import bot.charlies.androiddesign.preview.feature.preview.record.TextReport
import bot.charlies.androiddesign.preview.feature.preview.record.TreeRecorder
import bot.charlies.androiddesign.preview.feature.preview.theme.LocalResolvedTheme
import bot.charlies.androiddesign.preview.feature.preview.theme.toHex
import bot.charlies.androiddesign.preview.model.ColorRole
import bot.charlies.androiddesign.preview.model.Destination
import bot.charlies.androiddesign.preview.model.NavigationNodes
import bot.charlies.androiddesign.preview.model.NodeColors
import bot.charlies.androiddesign.preview.model.NodeRoles
import bot.charlies.androiddesign.preview.model.TypeRole
import kotlinx.serialization.json.JsonPrimitive

/**
 * Draws the app-level navigation of `navigation.destinations` once, around [content] and outside
 * every pane: a `ShortNavigationBar` below the content at compact widths and a `WideNavigationRail`
 * on the leading edge, full height, from medium up. What shows, and which destination is selected,
 * is decided by [AppNavigation], which the exporter's code follows too.
 *
 * The bar or rail is not a component of any screen, yet it records itself in the layout tree as
 * `@navigation` with one `@navigation[index]` item per destination ([NavigationNodes]), so the
 * touch target, contrast, font and label rules cover it.
 *
 * [content] has one call site in one layout, whatever shows, so a screen does not rebuild and keeps
 * what it remembers when the navigation appears, goes, or changes between a bar and a rail.
 */
@Composable
internal fun AppNavigationFrame(
  shown: AppNavigation.Shown,
  destinations: List<Destination>,
  selected: Int?,
  onSelect: (String) -> Unit,
  recorder: TreeRecorder?,
  modifier: Modifier = Modifier,
  content: @Composable () -> Unit,
) {
  val rail = shown as? AppNavigation.Shown.Rail
  Row(modifier.fillMaxSize()) {
    if (rail != null) {
      key(rail.expanded) {
        NavigationRail(rail.expanded, destinations, selected, onSelect, recorder)
      }
    }
    Column(Modifier.weight(1f).fillMaxHeight()) {
      Box(Modifier.weight(1f).fillMaxWidth()) { content() }
      if (shown == AppNavigation.Shown.Bar) {
        NavigationBar(destinations, selected, onSelect, recorder)
      }
    }
  }
}

/** The ambient colors of the bar and of the rail, as Material 3 gives them. */
private val BarContainer = ColorRole.SurfaceContainer
private val RailContainer = ColorRole.Surface
private val NavContent = ColorRole.OnSurfaceVariant

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun NavigationBar(
  destinations: List<Destination>,
  selected: Int?,
  onSelect: (String) -> Unit,
  recorder: TreeRecorder?,
) {
  // The bar sits below the screens, so it comes after them in document order.
  val order = listOf(1)
  RecordNavigation(recorder, NavigationNodes.BAR, order, BarContainer, null)
  // The bar fills its parent by itself; a size modifier on it breaks EqualWeight.
  ShortNavigationBar(
    modifier = Modifier.tagged(NavigationNodes.CONTAINER_ID, recorder),
    arrangement = ShortNavigationBarArrangement.EqualWeight,
  ) {
    destinations.forEachIndexed { index, destination ->
      val isSelected = index == selected
      val id = NavigationNodes.itemId(index)
      RecordNavigationItem(
        recorder,
        NavigationNodes.BAR_ITEM,
        index,
        order,
        destination,
        BarContainer,
      )
      ShortNavigationBarItem(
        selected = isSelected,
        onClick = { onSelect(destination.screen) },
        icon = { DestinationIcon(destination, isSelected) },
        label = { DestinationLabel(destination, id, recorder) },
        modifier = Modifier.tagged(id, recorder),
      )
    }
  }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun NavigationRail(
  expanded: Boolean,
  destinations: List<Destination>,
  selected: Int?,
  onSelect: (String) -> Unit,
  recorder: TreeRecorder?,
) {
  // The rail is on the leading edge, so it comes before the screens in document order.
  val order = listOf(-1)
  val state =
    rememberWideNavigationRailState(
      if (expanded) WideNavigationRailValue.Expanded else WideNavigationRailValue.Collapsed,
    )
  RecordNavigation(recorder, NavigationNodes.RAIL, order, RailContainer, null)
  WideNavigationRail(
    modifier = Modifier.tagged(NavigationNodes.CONTAINER_ID, recorder),
    state = state,
  ) {
    destinations.forEachIndexed { index, destination ->
      val isSelected = index == selected
      val id = NavigationNodes.itemId(index)
      RecordNavigationItem(
        recorder,
        NavigationNodes.RAIL_ITEM,
        index,
        order,
        destination,
        RailContainer,
      )
      WideNavigationRailItem(
        selected = isSelected,
        onClick = { onSelect(destination.screen) },
        icon = { DestinationIcon(destination, isSelected) },
        label = { DestinationLabel(destination, id, recorder) },
        railExpanded = expanded,
        modifier = Modifier.tagged(id, recorder),
      )
    }
  }
}

/** The icon, in its selected form while the destination is selected, with its badge. */
@Composable
private fun DestinationIcon(destination: Destination, selected: Boolean) {
  WithBadge(BadgeContent.of(destination.badge as? JsonPrimitive)) {
    IconSlot(
      if (selected) destination.selectedIcon ?: destination.icon else destination.icon,
      filled = selected,
    )
  }
}

@Composable
private fun DestinationLabel(destination: Destination, id: String, recorder: TreeRecorder?) {
  Text(
    text = destination.label,
    maxLines = 1,
    overflow = TextOverflow.Ellipsis,
    onTextLayout = { recorder?.reportText(id, "label", TextReport.of(it)) },
  )
}

/** Makes a node findable by id and reports where it landed. */
private fun Modifier.tagged(id: String, recorder: TreeRecorder?): Modifier =
  testTag(id).let { base ->
    if (recorder == null) base else base.onGloballyPositioned { recorder.reportPosition(id, it) }
  }

/** Records the bar or the rail itself while it is on screen. */
@Composable
private fun RecordNavigation(
  recorder: TreeRecorder?,
  type: String,
  order: List<Int>,
  container: ColorRole,
  text: String?,
) {
  record(
    recorder,
    NodeMeta(
      id = NavigationNodes.CONTAINER_ID,
      type = type,
      parent = null,
      order = order,
      primary = false,
      interactive = false,
      roles = roles(container, null),
      colors = colors(container),
      text = text,
      font = null,
      a11yRole = null,
      a11yLabel = null,
      actions = emptyList(),
    ),
  )
}

/** Records one destination: interactive, a tab, with its label as the text it shows. */
@Composable
private fun RecordNavigationItem(
  recorder: TreeRecorder?,
  type: String,
  index: Int,
  containerOrder: List<Int>,
  destination: Destination,
  container: ColorRole,
) {
  val id = NavigationNodes.itemId(index)
  val theme = LocalResolvedTheme.current
  record(
    recorder,
    NodeMeta(
      id = id,
      type = type,
      parent = NavigationNodes.CONTAINER_ID,
      order = containerOrder + listOf(0, index),
      primary = false,
      interactive = true,
      roles = roles(container, TypeRole.LabelMedium),
      colors = colors(container),
      text = destination.label.takeIf { it.isNotEmpty() },
      font = theme.fontFamilies[TypeRole.LabelMedium],
      a11yRole = "Tab",
      a11yLabel = destination.label,
      actions = listOf("click"),
    ),
  )
}

@Composable
private fun record(recorder: TreeRecorder?, meta: NodeMeta) {
  if (recorder == null) return
  SideEffect { recorder.register(meta) }
  DisposableEffect(meta.id) { onDispose { recorder.unregister(meta.id) } }
}

private fun roles(container: ColorRole, type: TypeRole?) =
  NodeRoles(container = container.wire, content = NavContent.wire, type = type?.wire)

@Composable
private fun colors(container: ColorRole): NodeColors {
  val theme = LocalResolvedTheme.current
  return NodeColors(
    container = theme.color(container).toHex(),
    content = theme.color(NavContent).toHex(),
  )
}
