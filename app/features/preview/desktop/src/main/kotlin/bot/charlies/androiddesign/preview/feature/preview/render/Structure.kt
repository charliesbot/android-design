package bot.charlies.androiddesign.preview.feature.preview.render

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.FlexibleBottomAppBar
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumFlexibleTopAppBar
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SecondaryScrollableTabRow
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.ShortNavigationBar
import androidx.compose.material3.ShortNavigationBarArrangement
import androidx.compose.material3.ShortNavigationBarItem
import androidx.compose.material3.Tab
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.VerticalFloatingToolbar
import androidx.compose.material3.WideNavigationRail
import androidx.compose.material3.WideNavigationRailItem
import androidx.compose.material3.WideNavigationRailValue
import androidx.compose.material3.rememberWideNavigationRailState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.style.TextOverflow
import bot.charlies.androiddesign.preview.domain.CollapsingAppBars
import bot.charlies.androiddesign.preview.domain.ColorStyle
import bot.charlies.androiddesign.preview.model.ColorRole
import bot.charlies.androiddesign.preview.model.CornerClip
import bot.charlies.androiddesign.preview.model.TypeRole

/** The scroll behavior of the Scaffold whose top bar this is, if its bar collapses. */
@OptIn(ExperimentalMaterial3Api::class)
private val LocalTopBarScroll = compositionLocalOf<TopAppBarScrollBehavior?> { null }

private val ScaffoldAmbient = Ambient(ColorRole.Background, ColorRole.OnBackground)

internal fun NodeContext.scaffoldLook() = Look(ScaffoldAmbient)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun RenderScaffold(ctx: NodeContext) {
  val env = ctx.env
  val rail = ctx.props.childId("navigationRail")
  val topBar = ctx.props.childId("topBar")?.let(env.nodes::get)
  // Like the export, a medium or large bar collapses as the content scrolls. At rest it is
  // expanded.
  val collapsing =
    topBar != null &&
      CollapsingAppBars.collapsesAt(topBar, env.sizeClass, effectiveProps(topBar, env))
  val scrollBehavior =
    if (collapsing) TopAppBarDefaults.exitUntilCollapsedScrollBehavior() else null
  val scaffold: @Composable (Modifier) -> Unit = { modifier ->
    Scaffold(
      modifier =
        if (scrollBehavior != null) modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
        else modifier,
      topBar = {
        CompositionLocalProvider(LocalTopBarScroll provides scrollBehavior) {
          ctx.Child("topBar", 1, ChildHost.Wrap)
        }
      },
      bottomBar = { ctx.Child("bottomBar", 3, ChildHost.Flow) },
      floatingActionButton = { ctx.Child("floatingActionButton", 4, ChildHost.Wrap) },
      snackbarHost = {
        if (ctx.node.id == env.placement.snackbarScaffold) SnackbarSlot(env)
      },
      containerColor = env.color(ColorRole.Background),
      contentWindowInsets = WindowInsets(0, 0, 0, 0),
    ) { padding ->
      Box(Modifier.padding(padding).fillMaxSize()) { ctx.Child("content", 2, ChildHost.Fill) }
    }
  }
  CompositionLocalProvider(LocalAmbient provides ScaffoldAmbient) {
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

/** A search app bar's text is its field's hint, in body large on the field's container. */
internal fun NodeContext.topAppBarLook() =
  if (isSearchAppBar()) {
    searchBarLook(string("title"))
  } else {
    Look(
      ColorRole.Surface,
      ColorRole.OnSurface,
      TypeRole.TitleLarge,
      text = string("title"),
    )
  }

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun RenderTopAppBar(ctx: NodeContext) {
  if (ctx.isSearchAppBar()) {
    RenderSearchAppBar(ctx)
    return
  }
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
  val modifier = ctx.modifier()
  val insets = WindowInsets(0, 0, 0, 0)
  val scroll = LocalTopBarScroll.current
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

private val NavigationBarAmbient = Ambient(ColorRole.SurfaceContainer, ColorRole.OnSurfaceVariant)

internal fun NodeContext.navigationBarLook() = Look(NavigationBarAmbient)

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun RenderNavigationBar(ctx: NodeContext) {
  CompositionLocalProvider(LocalAmbient provides NavigationBarAmbient) {
    // The bar fills its parent by itself; a size modifier on it breaks EqualWeight.
    ShortNavigationBar(
      modifier = ctx.modifier(),
      arrangement = ShortNavigationBarArrangement.EqualWeight,
    ) {
      ctx.Children(
        "children",
        0,
        ChildHost(fillWidth = ctx.def.stretchesChildren, groupKey = ctx.id),
      )
    }
  }
}

internal fun NodeContext.navigationBarItemLook() =
  Look(
    NavigationBarAmbient,
    TypeRole.LabelMedium,
    text = string("label"),
    a11yRole = "Tab",
  )

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun RenderNavigationBarItem(ctx: NodeContext) {
  val selected = ctx.rememberSelection()
  ShortNavigationBarItem(
    selected = selected,
    onClick = { ctx.select() },
    icon = { ctx.WithBadge { IconSlot(selectedIcon(ctx, selected), filled = selected) } },
    label = { ctx.PartText(ctx.string("label").orEmpty(), maxLines = 1) },
    modifier = ctx.modifier(),
  )
}

private val TabsAmbient = Ambient(ColorRole.Surface, ColorRole.OnSurface)

internal fun NodeContext.tabsLook() = Look(TabsAmbient)

/** The index of the selected tab: the one tapped last, else the first whose `selected` is set. */
private fun NodeContext.selectedTab(): Int {
  val refs = props.children("children")
  val last = (refs.size - 1).coerceAtLeast(0)
  groupSelected(id)?.let {
    return it.coerceIn(0, last)
  }
  return refs
    .indexOfFirst { ref ->
      val node = env.nodes[ref.id] ?: return@indexOfFirst false
      Props(effectiveProps(node, env), ref.scope).bool("selected") == true
    }
    .coerceAtLeast(0)
}

@Composable
internal fun RenderTabs(ctx: NodeContext) {
  val selected = ctx.selectedTab()
  val tabs: @Composable () -> Unit = {
    ctx.Children(
      "children",
      0,
      ChildHost(
        fillWidth = ctx.def.stretchesChildren,
        groupKey = ctx.id,
        secondaryTabs = ctx.enum("variant") == "secondary",
      ),
    )
  }
  val secondary = ctx.enum("variant") == "secondary"
  val scrollable = ctx.bool("scrollable")
  CompositionLocalProvider(LocalAmbient provides TabsAmbient) {
    val modifier = ctx.modifier()
    when {
      secondary && scrollable -> SecondaryScrollableTabRow(selected, modifier, tabs = tabs)
      secondary -> SecondaryTabRow(selected, modifier, tabs = tabs)
      scrollable -> PrimaryScrollableTabRow(selected, modifier, tabs = tabs)
      else -> PrimaryTabRow(selected, modifier, tabs = tabs)
    }
  }
}

/** The content color of a selected tab: its row's (primary, or on surface in a secondary row). */
private fun NodeContext.selectedTabColor() =
  if (host.secondaryTabs) ColorRole.OnSurface else ColorRole.Primary

/** Whether this tab is the selected one of its row, as the viewer last left it. */
private fun NodeContext.tabSelected(): Boolean =
  host.groupKey?.let(::isPickedIn) ?: bool("selected")

/**
 * A selected tab sets its label in its row's content color, the others in on surface variant,
 * Material's inactive tab token.
 */
internal fun NodeContext.tabLook() =
  Look(
    ColorRole.Surface,
    if (tabSelected()) selectedTabColor() else ColorRole.OnSurfaceVariant,
    TypeRole.TitleSmall,
    text = string("label"),
    a11yRole = "Tab",
  )

@Composable
internal fun RenderTab(ctx: NodeContext) {
  val selected = ctx.rememberSelection()
  val icon = ctx.string("icon")
  Tab(
    selected = selected,
    onClick = { ctx.select() },
    modifier = ctx.modifier(),
    // The badge sits on the icon, else on the label.
    text = {
      val label: @Composable () -> Unit = {
        ctx.PartText(ctx.string("label").orEmpty(), maxLines = 1)
      }
      if (icon == null) ctx.WithBadge(label) else label()
    },
    icon = icon?.let { name -> { ctx.WithBadge { IconSlot(name) } } },
    unselectedContentColor = ctx.env.color(ColorRole.OnSurfaceVariant),
  )
}

private val NavigationRailAmbient = Ambient(ColorRole.Surface, ColorRole.OnSurfaceVariant)

internal fun NodeContext.navigationRailLook() = Look(NavigationRailAmbient)

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun RenderNavigationRail(ctx: NodeContext) {
  val state =
    rememberWideNavigationRailState(
      if (ctx.bool("expanded")) WideNavigationRailValue.Expanded
      else WideNavigationRailValue.Collapsed,
    )
  CompositionLocalProvider(LocalAmbient provides NavigationRailAmbient) {
    WideNavigationRail(modifier = ctx.modifier(), state = state) {
      ctx.Children(
        "children",
        0,
        ChildHost(
          fillWidth = ctx.def.stretchesChildren,
          groupKey = ctx.id,
          railExpanded = ctx.bool("expanded"),
        ),
      )
    }
  }
}

internal fun NodeContext.navigationRailItemLook() =
  Look(
    NavigationRailAmbient,
    TypeRole.LabelMedium,
    text = string("label"),
    a11yRole = "Tab",
  )

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun RenderNavigationRailItem(ctx: NodeContext) {
  val selected = ctx.rememberSelection()
  WideNavigationRailItem(
    selected = selected,
    onClick = { ctx.select() },
    icon = { ctx.WithBadge { IconSlot(selectedIcon(ctx, selected), filled = selected) } },
    label = { ctx.PartText(ctx.string("label").orEmpty(), maxLines = 1) },
    railExpanded = ctx.host.railExpanded,
    modifier = ctx.modifier(),
  )
}

private fun selectedIcon(ctx: NodeContext, selected: Boolean): String? =
  if (selected) ctx.string("selectedIcon") ?: ctx.string("icon") else ctx.string("icon")

private val NodeContext.vibrant: Boolean
  get() = enum("colors") == ColorStyle.VIBRANT

private fun NodeContext.floatingToolbarAmbient() =
  if (vibrant) Ambient(ColorRole.PrimaryContainer, ColorRole.OnPrimaryContainer)
  else Ambient(ColorRole.SurfaceContainer, ColorRole.OnSurface)

internal fun NodeContext.floatingToolbarLook() = Look(floatingToolbarAmbient(), shape = "full")

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalLayoutApi::class)
@Composable
internal fun RenderFloatingToolbar(ctx: NodeContext) {
  val colors =
    if (ctx.vibrant) FloatingToolbarDefaults.vibrantFloatingToolbarColors()
    else FloatingToolbarDefaults.standardFloatingToolbarColors()
  CompositionLocalProvider(LocalAmbient provides ctx.floatingToolbarAmbient()) {
    if (ctx.enum("orientation") == "vertical") {
      VerticalFloatingToolbar(
        expanded = true,
        modifier = ctx.modifier(),
        colors = colors,
        content = {
          ctx.Children(
            "children",
            0,
            ChildHost(fillWidth = ctx.def.stretchesChildren, column = this, toolbar = true),
          )
        },
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
          content = {
            ctx.Children(
              "children",
              0,
              ChildHost(fillWidth = ctx.def.stretchesChildren, row = this, toolbar = true),
            )
          },
        )
      }
    }
  }
}

private fun NodeContext.dockedToolbarAmbient() = floatingToolbarAmbient()

internal fun NodeContext.dockedToolbarLook() = Look(dockedToolbarAmbient(), shape = "none")

/**
 * The docked toolbar at the bottom of the window: `FlexibleBottomAppBar`, standard in surface
 * container and vibrant in primary container, as the floating toolbar is.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun RenderDockedToolbar(ctx: NodeContext) {
  val ambient = ctx.dockedToolbarAmbient()
  CompositionLocalProvider(LocalAmbient provides ambient) {
    FlexibleBottomAppBar(
      modifier = ctx.modifier(),
      containerColor = ctx.env.color(ambient.container),
      contentColor = ctx.env.color(ambient.content),
    ) {
      ctx.Children(
        "children",
        0,
        ChildHost(fillWidth = ctx.def.stretchesChildren, row = this, toolbar = true),
      )
    }
  }
}

/** The card's container and content roles: its style, else its variant's. */
private fun NodeContext.cardAmbient() =
  Ambient(
    style.container
      ?: when (enum("variant")) {
        "elevated" -> ColorRole.SurfaceContainerLow
        "outlined" -> ColorRole.Surface
        else -> ColorRole.SurfaceContainerHighest
      },
    style.color ?: ColorRole.OnSurface,
  )

internal fun NodeContext.cardLook() = Look(cardAmbient(), shape = shapeName() ?: "medium")

@Composable
internal fun RenderCard(ctx: NodeContext) {
  val variant = ctx.enum("variant")
  val shape = ctx.shapeName()?.let { shapeFor(it) } ?: MaterialTheme.shapes.medium
  val ambient = ctx.cardAmbient()
  val container = ctx.env.color(ambient.container)
  val action = ctx.props.action("action")
  // The padding sits inside the card, around its content, as the exporter writes it, so the
  // card's corners never cut the content.
  val modifier = ctx.modifier(padded = false)
  ctx.ReportClip(ctx.env.cornerRadius(ctx.shapeName() ?: "medium")?.let { CornerClip(it) })
  val padding = ctx.padding
  val children: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit = {
    ctx.Children("content", 0, ChildHost(fillWidth = ctx.def.stretchesChildren, column = this))
  }
  val content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit = {
    CompositionLocalProvider(LocalAmbient provides ambient) {
      if (padding == null) children() else Column(Modifier.padding(padding), content = children)
    }
  }
  val colors =
    CardDefaults.cardColors(
      containerColor = container,
      contentColor = ctx.env.color(ambient.content),
    )
  when {
    variant == "elevated" && action != null ->
      ElevatedCard(
        onClick = { ctx.env.act(action) },
        modifier = modifier,
        shape = shape,
        colors =
          CardDefaults.elevatedCardColors(
            containerColor = container,
            contentColor = ctx.env.color(ambient.content),
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
            contentColor = ctx.env.color(ambient.content),
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
            contentColor = ctx.env.color(ambient.content),
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
            contentColor = ctx.env.color(ambient.content),
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
  return isPickedIn(group)
}

/** Makes this item the selected one of its group and runs its action. */
internal fun NodeContext.select() {
  host.groupKey?.let(::selectInGroup)
  env.act(props.action("action"))
}
