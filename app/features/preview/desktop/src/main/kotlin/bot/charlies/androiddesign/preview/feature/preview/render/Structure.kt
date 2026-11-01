package bot.charlies.androiddesign.preview.feature.preview.render

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumFlexibleTopAppBar
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ShortNavigationBar
import androidx.compose.material3.ShortNavigationBarArrangement
import androidx.compose.material3.ShortNavigationBarItem
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.VerticalFloatingToolbar
import androidx.compose.material3.WideNavigationRail
import androidx.compose.material3.WideNavigationRailItem
import androidx.compose.material3.WideNavigationRailValue
import androidx.compose.material3.rememberWideNavigationRailState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import bot.charlies.androiddesign.preview.model.ColorRole

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun RenderScaffold(ctx: NodeContext) {
  val env = ctx.env
  val rail = ctx.props.childId("navigationRail")
  val scaffold: @Composable (Modifier) -> Unit = { modifier ->
    Scaffold(
      modifier = modifier,
      topBar = { ctx.Child("topBar", 1, ChildHost.Wrap) },
      bottomBar = { ctx.Child("bottomBar", 3, ChildHost.Flow) },
      floatingActionButton = { ctx.Child("floatingActionButton", 4, ChildHost.Wrap) },
      containerColor = env.color(ColorRole.Background),
      contentWindowInsets = WindowInsets(0, 0, 0, 0),
    ) { padding ->
      Box(Modifier.padding(padding).fillMaxSize()) { ctx.Child("content", 2, ChildHost.Fill) }
    }
  }
  CompositionLocalProvider(
    LocalAmbient provides Ambient(ColorRole.Background, ColorRole.OnBackground),
  ) {
    val frame = ctx.modifier().fillMaxSize()
    if (rail == null) {
      scaffold(frame)
    } else {
      Row(frame) {
        ctx.Child("navigationRail", 0, ChildHost.Wrap)
        scaffold(Modifier.weight(1f).fillMaxHeight())
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun RenderTopAppBar(ctx: NodeContext) {
  val title = ctx.string("title").orEmpty()
  val subtitle = ctx.string("subtitle")
  val titleSlot: @Composable () -> Unit = {
    ctx.PartText(title, "title", maxLines = 1, overflow = TextOverflow.Ellipsis)
  }
  val subtitleSlot: (@Composable () -> Unit)? = subtitle?.let { text ->
    { ctx.PartText(text, "subtitle", maxLines = 1, overflow = TextOverflow.Ellipsis) }
  }
  val navigation: @Composable () -> Unit = { ctx.Child("navigationIcon", 0, ChildHost.Wrap) }
  val actions: @Composable androidx.compose.foundation.layout.RowScope.() -> Unit = {
    ctx.Children("actions", 1, ChildHost.Wrap)
  }
  val modifier = ctx.modifier(fillWidth = true)
  val insets = WindowInsets(0, 0, 0, 0)
  // Medium and large bars draw expanded; the behavior is only here so they are not collapsed.
  val scroll = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
  when (ctx.enum("size")) {
    "center" ->
      CenterAlignedTopAppBar(titleSlot, modifier, navigation, actions, windowInsets = insets)
    "medium" ->
      MediumFlexibleTopAppBar(
        title = titleSlot,
        modifier = modifier,
        subtitle = subtitleSlot,
        navigationIcon = navigation,
        actions = actions,
        windowInsets = insets,
        scrollBehavior = scroll,
      )
    "large" ->
      LargeFlexibleTopAppBar(
        title = titleSlot,
        modifier = modifier,
        subtitle = subtitleSlot,
        navigationIcon = navigation,
        actions = actions,
        windowInsets = insets,
        scrollBehavior = scroll,
      )
    else ->
      if (subtitleSlot != null) {
        TopAppBar(
          title = titleSlot,
          subtitle = subtitleSlot,
          modifier = modifier,
          navigationIcon = navigation,
          actions = actions,
          windowInsets = insets,
        )
      } else {
        TopAppBar(
          title = titleSlot,
          modifier = modifier,
          navigationIcon = navigation,
          actions = actions,
          windowInsets = insets,
        )
      }
  }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun RenderNavigationBar(ctx: NodeContext) {
  CompositionLocalProvider(
    LocalAmbient provides Ambient(ColorRole.SurfaceContainer, ColorRole.OnSurfaceVariant),
  ) {
    // The bar fills its parent by itself; a size modifier on it breaks EqualWeight.
    ShortNavigationBar(
      modifier = ctx.modifier(),
      arrangement = ShortNavigationBarArrangement.EqualWeight,
    ) {
      ctx.Children("children", 0, ChildHost(groupKey = ctx.id))
    }
  }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun RenderNavigationBarItem(ctx: NodeContext) {
  val selected = ctx.rememberSelection()
  ShortNavigationBarItem(
    selected = selected,
    onClick = { ctx.select() },
    icon = { IconSlot(selectedIcon(ctx, selected), filled = selected) },
    label = { ctx.PartText(ctx.string("label").orEmpty(), maxLines = 1) },
    modifier = ctx.modifier(),
  )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun RenderNavigationRail(ctx: NodeContext) {
  val state =
    rememberWideNavigationRailState(
      if (ctx.bool("expanded")) WideNavigationRailValue.Expanded
      else WideNavigationRailValue.Collapsed,
    )
  CompositionLocalProvider(
    LocalAmbient provides Ambient(ColorRole.Surface, ColorRole.OnSurfaceVariant),
  ) {
    WideNavigationRail(modifier = ctx.modifier(), state = state) {
      ctx.Children(
        "children",
        0,
        ChildHost(fillWidth = false, groupKey = ctx.id, railExpanded = ctx.bool("expanded")),
      )
    }
  }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun RenderNavigationRailItem(ctx: NodeContext) {
  val selected = ctx.rememberSelection()
  WideNavigationRailItem(
    selected = selected,
    onClick = { ctx.select() },
    icon = { IconSlot(selectedIcon(ctx, selected), filled = selected) },
    label = { ctx.PartText(ctx.string("label").orEmpty(), maxLines = 1) },
    railExpanded = ctx.host.railExpanded,
    modifier = ctx.modifier(),
  )
}

private fun selectedIcon(ctx: NodeContext, selected: Boolean): String? =
  if (selected) ctx.string("selectedIcon") ?: ctx.string("icon") else ctx.string("icon")

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalLayoutApi::class)
@Composable
internal fun RenderFloatingToolbar(ctx: NodeContext) {
  val vibrant = ctx.enum("colors") == "vibrant"
  val colors =
    if (vibrant) FloatingToolbarDefaults.vibrantFloatingToolbarColors()
    else FloatingToolbarDefaults.standardFloatingToolbarColors()
  val ambient =
    if (vibrant) Ambient(ColorRole.PrimaryContainer, ColorRole.OnPrimaryContainer)
    else Ambient(ColorRole.SurfaceContainer, ColorRole.OnSurface)
  CompositionLocalProvider(LocalAmbient provides ambient) {
    if (ctx.enum("orientation") == "vertical") {
      VerticalFloatingToolbar(
        expanded = true,
        modifier = ctx.modifier(),
        colors = colors,
        content = { ctx.Children("children", 0, ChildHost(fillWidth = false, column = this)) },
      )
    } else {
      Box(
        Modifier.fillMaxWidth().padding(bottom = FloatingToolbarDefaults.ScreenOffset),
        contentAlignment = Alignment.Center,
      ) {
        HorizontalFloatingToolbar(
          expanded = true,
          modifier = ctx.modifier(),
          colors = colors,
          content = { ctx.Children("children", 0, ChildHost(fillWidth = false, row = this)) },
        )
      }
    }
  }
}

@Composable
internal fun RenderCard(ctx: NodeContext) {
  val variant = ctx.enum("variant")
  val shape = ctx.shapeName()?.let { shapeFor(it) } ?: MaterialTheme.shapes.medium
  val containerRole =
    ctx.style.container
      ?: when (variant) {
        "elevated" -> ColorRole.SurfaceContainerLow
        "outlined" -> ColorRole.Surface
        else -> ColorRole.SurfaceContainerHighest
      }
  val container = ctx.env.color(containerRole)
  val contentRole = ctx.style.color ?: ColorRole.OnSurface
  val action = ctx.props.action("action")
  val modifier = ctx.modifier(fillWidth = true)
  val content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit = {
    CompositionLocalProvider(LocalAmbient provides Ambient(containerRole, contentRole)) {
      ctx.Children("content", 0, ChildHost(fillWidth = true, column = this))
    }
  }
  val colors =
    CardDefaults.cardColors(containerColor = container, contentColor = ctx.env.color(contentRole))
  when {
    variant == "elevated" && action != null ->
      ElevatedCard(
        onClick = { ctx.env.act(action) },
        modifier = modifier,
        shape = shape,
        colors =
          CardDefaults.elevatedCardColors(
            containerColor = container,
            contentColor = ctx.env.color(contentRole),
          ),
        content = content,
      )
    variant == "elevated" ->
      ElevatedCard(
        modifier = modifier,
        shape = shape,
        colors =
          CardDefaults.elevatedCardColors(
            containerColor = container,
            contentColor = ctx.env.color(contentRole),
          ),
        content = content,
      )
    variant == "outlined" && action != null ->
      OutlinedCard(
        onClick = { ctx.env.act(action) },
        modifier = modifier,
        shape = shape,
        colors =
          CardDefaults.outlinedCardColors(
            containerColor = container,
            contentColor = ctx.env.color(contentRole),
          ),
        content = content,
      )
    variant == "outlined" ->
      OutlinedCard(
        modifier = modifier,
        shape = shape,
        colors =
          CardDefaults.outlinedCardColors(
            containerColor = container,
            contentColor = ctx.env.color(contentRole),
          ),
        content = content,
      )
    action != null ->
      Card(
        onClick = { ctx.env.act(action) },
        modifier = modifier,
        shape = shape,
        colors = colors,
        content = content,
      )
    else -> Card(modifier = modifier, shape = shape, colors = colors, content = content)
  }
}

/** Whether this item is the selected one of its group; the first tap on a sibling moves it. */
@Composable
internal fun NodeContext.rememberSelection(): Boolean {
  val group = host.groupKey ?: return bool("selected")
  val key = env.stateKey("selection:$group")
  val selected = env.host.interaction.get<Int>(key)
  if (selected != null) return selected == host.groupIndex
  return bool("selected")
}

/** Makes this item the selected one of its group and runs its action. */
internal fun NodeContext.select() {
  host.groupKey?.let { env.host.interaction[env.stateKey("selection:$it")] = host.groupIndex }
  env.act(props.action("action"))
}
