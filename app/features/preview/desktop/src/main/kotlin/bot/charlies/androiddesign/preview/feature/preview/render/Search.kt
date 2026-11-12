package bot.charlies.androiddesign.preview.feature.preview.render

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.AppBarWithSearch
import androidx.compose.material3.ExpandedDockedSearchBarWithGap
import androidx.compose.material3.ExpandedFullScreenContainedSearchBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarColors
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.SearchBarState
import androidx.compose.material3.SearchBarValue
import androidx.compose.material3.rememberContainedSearchBarState
import androidx.compose.material3.rememberSearchBarWithGapState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import bot.charlies.androiddesign.preview.domain.SearchBars
import bot.charlies.androiddesign.preview.domain.SearchSpec
import bot.charlies.androiddesign.preview.model.ColorRole
import bot.charlies.androiddesign.preview.model.M3ExpressiveCatalog
import bot.charlies.androiddesign.preview.model.TypeRole

/**
 * What a search view needs from the bar that opens it: the one [SearchBarState] they share, the
 * colors, and the input field. The bar and its view each draw the input field, as Compose's own
 * samples do, so the field in the view is [viewInputField], a copy that does not record itself.
 */
internal class SearchHost(
  val state: SearchBarState,
  val colors: SearchBarColors,
  val docked: Boolean,
  val inputField: @Composable () -> Unit,
  val viewInputField: @Composable () -> Unit,
)

internal val LocalSearchHost = compositionLocalOf<SearchHost?> { null }

/** Whether this node draws as a search app bar at the current width, by the shared predicate. */
internal fun NodeContext.isSearchAppBar(): Boolean =
  SearchBars.isSearchAppBar(component, effectiveProps(node, env))

/** The look of the bar: a filled, fully rounded container with the hint in on surface variant. */
private val SearchBarAmbient = Ambient(ColorRole.SurfaceContainerHigh, ColorRole.OnSurfaceVariant)

internal fun NodeContext.searchBarLook(hint: String? = string("placeholder")) =
  Look(SearchBarAmbient, TypeRole.BodyLarge, "full", hint, "Text field")

/** The search view's container: full-screen sits on surface container low, docked on high. */
internal fun NodeContext.searchViewLook(): Look =
  Look(
    if (enum("layout") == "docked") ColorRole.SurfaceContainerHigh
    else ColorRole.SurfaceContainerLow,
    ColorRole.OnSurface,
  )

/** The `layout` of search view [viewId] at this size: its own, else the catalog's default. */
private fun NodeContext.searchViewLayout(viewId: String): String? {
  val view = env.nodes[viewId] ?: return null
  val def = env.catalog.find(view.component) ?: return null
  return Props(effectiveProps(view, env), scope).string("layout")
    ?: def.defaultOf("layout")?.content
}

/** The icon a search bar starts with when its proposal names none. */
private val DefaultLeadingIcon: String
  get() =
    checkNotNull(M3ExpressiveCatalog.find("SearchBar")?.defaultOf("leadingIcon")?.content) {
      "SearchBar.leadingIcon has no catalog default"
    }

/**
 * Draws the pieces a search bar and a search app bar share, then hands the bar's [SearchHost] to
 * [content] to draw the bar itself. The search view it names draws beside it, in the same `Box`,
 * while it is the open overlay.
 *
 * The [SearchBarState] follows the overlay: it starts expanded when the view is open, and it is
 * made again when the view opens or closes, which snaps it. A focus on the field opens the view, as
 * tapping a bar does on Android, and a state that collapses on its own (the dialog's own dismiss)
 * closes it.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun NodeContext.WithSearch(
  placeholder: String,
  collapsedLeading: @Composable (NodeContext) -> Unit,
  trailing: (@Composable (NodeContext) -> Unit)?,
  content: @Composable (SearchHost) -> Unit,
) {
  val ctx = this
  val viewId = props.childId("expanded")?.takeIf { env.placement.expandedViewOf(node.id) == it }
  val docked = viewId?.let(::searchViewLayout) == "docked"
  val open = viewId != null && env.openOverlay == viewId
  val initial = if (open) SearchBarValue.Expanded else SearchBarValue.Collapsed
  val state =
    if (docked) rememberSearchBarWithGapState(initial) else rememberContainedSearchBarState(initial)
  val colors = if (docked) SearchBarDefaults.colors() else SearchBarDefaults.containedColors(state)
  val query = rememberTextFieldState()
  LaunchedEffect(state.targetValue) {
    if (
      state.targetValue == SearchBarValue.Collapsed && viewId != null && env.openOverlay == viewId
    ) {
      env.dismiss()
    }
  }
  fun field(quiet: Boolean): @Composable () -> Unit = {
    val c = if (quiet) ctx.withEnv(env.unrecorded()) else ctx
    SearchBarDefaults.InputField(
      textFieldState = query,
      searchBarState = state,
      onSearch = { env.dismiss() },
      modifier =
        if (quiet) Modifier
        else
          Modifier.onFocusChanged {
            if (it.isFocused && viewId != null && env.openOverlay != viewId) env.open(viewId)
          },
      placeholder = { c.PartText(placeholder, "placeholder", maxLines = 1) },
      leadingIcon = {
        if (state.currentValue == SearchBarValue.Expanded) {
          IconButton(onClick = { env.dismiss() }) {
            SymbolIcon("arrow_back", contentDescription = "Back")
          }
        } else {
          collapsedLeading(c)
        }
      },
      trailingIcon = trailing?.let { slot -> { slot(c) } },
      colors = colors.inputFieldColors,
    )
  }
  val host = SearchHost(state, colors, docked, field(quiet = false), field(quiet = true))
  CompositionLocalProvider(LocalSearchHost provides host) {
    Box {
      content(host)
      if (open && viewId != null) {
        RenderNode(env, ChildRef(viewId, scope), id, order + listOf(99, 0), ChildHost.Wrap)
      }
    }
  }
}

/** The 30dp circle a bar ends with, from the `avatar` picture; a neutral block if it is missing. */
@Composable
internal fun NodeContext.Avatar() {
  val src = string("avatar") ?: return
  val bitmap = env.assets.image(src)
  Box(Modifier.size(SearchSpec.AVATAR_BOX_DP.dp), contentAlignment = Alignment.Center) {
    val circle = Modifier.size(SearchSpec.AVATAR_DP.dp).clip(CircleShape)
    if (bitmap == null) {
      Box(circle.background(env.neutralFill()))
    } else {
      Image(bitmap, contentDescription = null, modifier = circle, contentScale = ContentScale.Crop)
    }
  }
}

@Composable
internal fun RenderSearchBar(ctx: NodeContext) {
  val hasTrailing = ctx.props.children("trailing").isNotEmpty() || ctx.string("avatar") != null
  CompositionLocalProvider(LocalAmbient provides SearchBarAmbient) {
    ctx.WithSearch(
      placeholder = ctx.string("placeholder").orEmpty(),
      collapsedLeading = { c ->
        if (c.props.childId("navigationIcon") != null) {
          c.Child("navigationIcon", 0, ChildHost.Wrap)
        } else {
          SymbolIcon(c.string("leadingIcon") ?: DefaultLeadingIcon)
        }
      },
      trailing =
        if (hasTrailing) {
          { c ->
            Row(verticalAlignment = Alignment.CenterVertically) {
              c.Children("trailing", 1, ChildHost(fillWidth = false, row = this))
              c.Avatar()
            }
          }
        } else {
          null
        },
    ) { host ->
      SearchBar(
        state = host.state,
        inputField = host.inputField,
        modifier = ctx.modifier(),
        colors = host.colors,
      )
    }
  }
}

/** The search size of the top app bar: the real `AppBarWithSearch`, the title as its hint. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun RenderSearchAppBar(ctx: NodeContext) {
  val hasActions = ctx.props.children("actions").isNotEmpty() || ctx.string("avatar") != null
  CompositionLocalProvider(LocalAmbient provides SearchBarAmbient) {
    ctx.WithSearch(
      placeholder = ctx.string("title").orEmpty(),
      collapsedLeading = { SymbolIcon(DefaultLeadingIcon) },
      trailing = null,
    ) { host ->
      AppBarWithSearch(
        state = host.state,
        inputField = host.inputField,
        modifier = ctx.modifier(),
        navigationIcon =
          ctx.props.childId("navigationIcon")?.let {
            { ctx.Child("navigationIcon", 0, ChildHost.Wrap) }
          },
        actions =
          if (hasActions) {
            {
              ctx.Children("actions", 1, ChildHost(fillWidth = false, row = this))
              ctx.Avatar()
            }
          } else {
            null
          },
        colors = SearchBarDefaults.appBarWithSearchColors(searchBarColors = host.colors),
        windowInsets = WindowInsets(0, 0, 0, 0),
      )
    }
  }
}

/**
 * The search view: the bar's own `SearchBarState` expanded, with the results under the input field.
 * Full-screen is a window of its own that fills the screen; docked is a dropdown below the bar.
 * Both are Compose's contained style, with no divider.
 */
@Composable
internal fun RenderSearchView(ctx: NodeContext) {
  val host = LocalSearchHost.current ?: return
  val look = ctx.searchViewLook()
  val results: @Composable ColumnScope.() -> Unit = {
    CompositionLocalProvider(LocalAmbient provides Ambient(look.container, look.content)) {
      ctx.Children("content", 0, ChildHost(fillWidth = ctx.stretchesChildren, column = this))
    }
  }
  if (host.docked) {
    ExpandedDockedSearchBarWithGap(
      state = host.state,
      inputField = host.viewInputField,
      modifier = ctx.modifier(),
      colors = host.colors,
      content = results,
    )
  } else {
    ExpandedFullScreenContainedSearchBar(
      state = host.state,
      inputField = host.viewInputField,
      modifier = ctx.modifier(),
      colors = host.colors,
      content = results,
    )
  }
}
