package bot.charlies.androiddesign.preview.feature.preview.render

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ButtonShapes
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ElevatedToggleButton
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledIconToggleButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilledTonalIconToggleButton
import androidx.compose.material3.FilledTonalToggleButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.FloatingActionButtonMenu
import androidx.compose.material3.FloatingActionButtonMenuItem
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.LargeExtendedFloatingActionButton
import androidx.compose.material3.LargeFloatingActionButton
import androidx.compose.material3.MediumExtendedFloatingActionButton
import androidx.compose.material3.MediumFloatingActionButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.OutlinedIconToggleButton
import androidx.compose.material3.OutlinedToggleButton
import androidx.compose.material3.SplitButtonDefaults
import androidx.compose.material3.SplitButtonLayout
import androidx.compose.material3.TextButton
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.material3.ToggleFloatingActionButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import bot.charlies.androiddesign.preview.model.ColorRole

internal fun buttonHeight(size: String): Dp =
  when (size) {
    "extra-small" -> ButtonDefaults.ExtraSmallContainerHeight
    "medium" -> ButtonDefaults.MediumContainerHeight
    "large" -> ButtonDefaults.LargeContainerHeight
    "extra-large" -> ButtonDefaults.ExtraLargeContainerHeight
    else -> ButtonDefaults.MinHeight
  }

/** Adds the node's `contentDescription` as accessibility text. */
internal fun NodeContext.describedBy(): Modifier =
  string("contentDescription")?.let { text -> Modifier.semantics { contentDescription = text } }
    ?: Modifier

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun RenderButton(ctx: NodeContext) {
  val env = ctx.env
  val height = buttonHeight(ctx.enum("size", "small"))
  val icon = ctx.string("icon")
  val label = ctx.string("label").orEmpty()
  val action = ctx.props.action("action")
  val shapes: ButtonShapes =
    if (ctx.enum("shape", "round") == "square") {
      ButtonDefaults.shapes(ButtonDefaults.squareShape, ButtonDefaults.pressedShape)
    } else {
      ButtonDefaults.shapesFor(height)
    }
  val contentPadding = ButtonDefaults.contentPaddingFor(height, icon != null, false)
  val modifier = ctx.modifier().heightIn(min = height).then(ctx.describedBy())
  val content: @Composable RowScope.() -> Unit = {
    if (icon != null) {
      SymbolIcon(icon, size = ButtonDefaults.iconSizeFor(height))
      Spacer(Modifier.width(ButtonDefaults.iconSpacingFor(height)))
    }
    ctx.PartText(label, style = ButtonDefaults.textStyleFor(height))
  }
  val onClick = { env.act(action) }
  val enabled = ctx.enabled
  fun colors(base: ButtonColors): ButtonColors =
    if (ctx.style.container != null || ctx.style.color != null) {
      base.copy(
        containerColor = ctx.style.container?.let(env::color) ?: base.containerColor,
        contentColor = ctx.style.color?.let(env::color) ?: base.contentColor,
      )
    } else {
      base
    }
  when (ctx.enum("variant", "filled")) {
    "tonal" ->
      FilledTonalButton(
        onClick,
        shapes,
        modifier,
        enabled,
        colors(ButtonDefaults.filledTonalButtonColors()),
        contentPadding = contentPadding,
        content = content,
      )
    "elevated" ->
      ElevatedButton(
        onClick,
        shapes,
        modifier,
        enabled,
        colors(ButtonDefaults.elevatedButtonColors()),
        contentPadding = contentPadding,
        content = content,
      )
    "outlined" ->
      OutlinedButton(
        onClick,
        shapes,
        modifier,
        enabled,
        colors(ButtonDefaults.outlinedButtonColors()),
        contentPadding = contentPadding,
        content = content,
      )
    "text" ->
      TextButton(
        onClick,
        shapes,
        modifier,
        enabled,
        colors(ButtonDefaults.textButtonColors()),
        contentPadding = contentPadding,
        content = content,
      )
    else ->
      Button(
        onClick,
        shapes,
        modifier,
        enabled,
        colors(ButtonDefaults.buttonColors()),
        contentPadding = contentPadding,
        content = content,
      )
  }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun RenderToggleButton(ctx: NodeContext) {
  val env = ctx.env
  val height = buttonHeight(ctx.enum("size", "small"))
  val icon = ctx.string("icon")
  val label = ctx.string("label").orEmpty()
  val action = ctx.props.action("action")
  val checked = ctx.checkedState()
  val shapes =
    when {
      ctx.host.connected && ctx.host.groupCount > 1 && ctx.host.groupIndex == 0 ->
        ButtonGroupDefaults.connectedLeadingButtonShapes()
      ctx.host.connected &&
        ctx.host.groupCount > 1 &&
        ctx.host.groupIndex == ctx.host.groupCount - 1 ->
        ButtonGroupDefaults.connectedTrailingButtonShapes()
      ctx.host.connected -> ButtonGroupDefaults.connectedMiddleButtonShapes()
      else -> ToggleButtonDefaults.shapesFor(height)
    }
  val modifier = ctx.modifier().heightIn(min = height).then(ctx.describedBy())
  val padding = ToggleButtonDefaults.contentPaddingFor(height, icon != null, false)
  val onChange: (Boolean) -> Unit = {
    checked.set(it)
    env.act(action)
  }
  val content: @Composable RowScope.() -> Unit = {
    if (icon != null) {
      SymbolIcon(icon, size = ButtonDefaults.iconSizeFor(height), filled = checked.value)
      Spacer(Modifier.width(ButtonDefaults.iconSpacingFor(height)))
    }
    ctx.PartText(label, style = ButtonDefaults.textStyleFor(height))
  }
  val enabled = ctx.enabled
  when (ctx.enum("variant", "filled")) {
    "tonal" ->
      FilledTonalToggleButton(
        checked.value,
        onChange,
        modifier,
        enabled = enabled,
        shapes = shapes,
        contentPadding = padding,
        content = content,
      )
    "elevated" ->
      ElevatedToggleButton(
        checked.value,
        onChange,
        modifier,
        enabled = enabled,
        shapes = shapes,
        contentPadding = padding,
        content = content,
      )
    "outlined" ->
      OutlinedToggleButton(
        checked.value,
        onChange,
        modifier,
        enabled = enabled,
        shapes = shapes,
        contentPadding = padding,
        content = content,
      )
    else ->
      ToggleButton(
        checked.value,
        onChange,
        modifier,
        enabled = enabled,
        shapes = shapes,
        contentPadding = padding,
        content = content,
      )
  }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalLayoutApi::class)
@Composable
internal fun RenderButtonGroup(ctx: NodeContext) {
  if (ctx.enum("variant", "standard") == "connected") {
    Row(
      modifier = ctx.modifier(),
      horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
    ) {
      ctx.Children(
        "children",
        0,
        ChildHost(fillWidth = false, row = this, connected = true, groupKey = ctx.id),
      )
    }
  } else {
    Row(
      modifier = ctx.modifier(),
      horizontalArrangement = ButtonGroupDefaults.HorizontalArrangement,
    ) {
      ctx.Children("children", 0, ChildHost(fillWidth = false, row = this, groupKey = ctx.id))
    }
  }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun RenderIconButton(ctx: NodeContext) {
  val env = ctx.env
  val icon = ctx.string("icon").orEmpty()
  val action = ctx.props.action("action")
  val toggle = ctx.bool("toggle")
  val checked = ctx.checkedState()
  val width =
    when (ctx.enum("width", "uniform")) {
      "narrow" -> IconButtonDefaults.IconButtonWidthOption.Narrow
      "wide" -> IconButtonDefaults.IconButtonWidthOption.Wide
      else -> IconButtonDefaults.IconButtonWidthOption.Uniform
    }
  val size = ctx.enum("size", "small")
  val container =
    when (size) {
      "extra-small" -> IconButtonDefaults.extraSmallContainerSize(width)
      "medium" -> IconButtonDefaults.mediumContainerSize(width)
      "large" -> IconButtonDefaults.largeContainerSize(width)
      "extra-large" -> IconButtonDefaults.extraLargeContainerSize(width)
      else -> IconButtonDefaults.smallContainerSize(width)
    }
  val iconSize =
    when (size) {
      "extra-small" -> IconButtonDefaults.extraSmallIconSize
      "medium" -> IconButtonDefaults.mediumIconSize
      "large" -> IconButtonDefaults.largeIconSize
      "extra-large" -> IconButtonDefaults.extraLargeIconSize
      else -> IconButtonDefaults.smallIconSize
    }
  val modifier = ctx.modifier().size(container.width, container.height).then(ctx.describedBy())
  val enabled = ctx.enabled
  val variant = ctx.enum("variant", "standard")
  val onSurfaceVariant = env.color(ColorRole.OnSurfaceVariant)
  val outline = BorderStroke(1.dp, env.color(ColorRole.OutlineVariant))
  val content: @Composable () -> Unit = {
    SymbolIcon(icon, size = iconSize, filled = toggle && checked.value)
  }
  if (toggle) {
    val onChange: (Boolean) -> Unit = {
      checked.set(it)
      env.act(action)
    }
    val shapes = IconButtonDefaults.toggleableShapes()
    when (variant) {
      "filled" ->
        FilledIconToggleButton(
          checked.value,
          onChange,
          shapes,
          modifier,
          enabled,
          content = content,
        )
      "tonal" ->
        FilledTonalIconToggleButton(
          checked.value,
          onChange,
          shapes,
          modifier,
          enabled,
          content = content,
        )
      "outlined" ->
        OutlinedIconToggleButton(
          checked.value,
          onChange,
          shapes,
          modifier,
          enabled,
          colors =
            IconButtonDefaults.outlinedIconToggleButtonColors(contentColor = onSurfaceVariant),
          content = content,
        )
      else ->
        IconToggleButton(
          checked.value,
          onChange,
          shapes,
          modifier,
          enabled,
          colors = IconButtonDefaults.iconToggleButtonColors(contentColor = onSurfaceVariant),
          content = content,
        )
    }
  } else {
    val onClick = { env.act(action) }
    val shapes = IconButtonDefaults.shapes()
    when (variant) {
      "filled" -> FilledIconButton(onClick, shapes, modifier, enabled, content = content)
      "tonal" -> FilledTonalIconButton(onClick, shapes, modifier, enabled, content = content)
      "outlined" ->
        OutlinedIconButton(
          onClick,
          shapes,
          modifier,
          enabled,
          colors = IconButtonDefaults.outlinedIconButtonColors(contentColor = onSurfaceVariant),
          border = outline,
          content = content,
        )
      else ->
        IconButton(
          onClick,
          shapes,
          modifier,
          enabled,
          colors = IconButtonDefaults.iconButtonColors(contentColor = onSurfaceVariant),
          content = content,
        )
    }
  }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun RenderSplitButton(ctx: NodeContext) {
  val env = ctx.env
  val label = ctx.string("label").orEmpty()
  val icon = ctx.string("icon")
  val action = ctx.props.action("action")
  val menuId = ctx.props.childId("menu")
  val menuOpen = menuId != null && env.host.interaction.openOverlay(env.screenId) == menuId
  val variant = ctx.enum("variant", "filled")
  val height = SplitButtonDefaults.SmallContainerHeight
  val leadingContent: @Composable RowScope.() -> Unit = {
    if (icon != null) {
      SymbolIcon(icon, size = SplitButtonDefaults.LeadingIconSize)
      Spacer(Modifier.width(ButtonDefaults.IconSpacing))
    }
    ctx.PartText(label)
  }
  val toggleMenu: (Boolean) -> Unit = { open ->
    if (menuId != null) {
      if (open) env.host.interaction.open(env.screenId, menuId)
      else env.host.interaction.dismiss(env.screenId)
    }
  }
  val trailingContent: @Composable RowScope.() -> Unit = {
    SymbolIcon(
      "keyboard_arrow_down",
      modifier = Modifier.rotate(if (menuOpen) 180f else 0f),
      size = SplitButtonDefaults.TrailingIconSize,
    )
  }
  val onLead = { env.act(action) }
  val leadingShapes = SplitButtonDefaults.leadingButtonShapesFor(height)
  val trailingShapes = SplitButtonDefaults.trailingButtonShapesFor(height)
  Box(ctx.modifier().then(ctx.describedBy())) {
    SplitButtonLayout(
      leadingButton = {
        when (variant) {
          "tonal" ->
            SplitButtonDefaults.TonalLeadingButton(
              onLead,
              shapes = leadingShapes,
              content = leadingContent,
            )
          "elevated" ->
            SplitButtonDefaults.ElevatedLeadingButton(
              onLead,
              shapes = leadingShapes,
              content = leadingContent,
            )
          "outlined" ->
            SplitButtonDefaults.OutlinedLeadingButton(
              onLead,
              shapes = leadingShapes,
              content = leadingContent,
            )
          else ->
            SplitButtonDefaults.LeadingButton(
              onLead,
              shapes = leadingShapes,
              content = leadingContent,
            )
        }
      },
      trailingButton = {
        when (variant) {
          "tonal" ->
            SplitButtonDefaults.TonalTrailingButton(
              menuOpen,
              toggleMenu,
              shapes = trailingShapes,
              content = trailingContent,
            )
          "elevated" ->
            SplitButtonDefaults.ElevatedTrailingButton(
              menuOpen,
              toggleMenu,
              shapes = trailingShapes,
              content = trailingContent,
            )
          "outlined" ->
            SplitButtonDefaults.OutlinedTrailingButton(
              menuOpen,
              toggleMenu,
              shapes = trailingShapes,
              content = trailingContent,
            )
          else ->
            SplitButtonDefaults.TrailingButton(
              menuOpen,
              toggleMenu,
              shapes = trailingShapes,
              content = trailingContent,
            )
        }
      },
    )
    if (menuId != null && menuOpen) MenuOverlay(env, menuId, ctx.scope, ctx.id, ctx.order)
  }
}

private fun fabColors(name: String): Pair<ColorRole, ColorRole> =
  when (name) {
    "primary" -> ColorRole.Primary to ColorRole.OnPrimary
    "secondary-container" -> ColorRole.SecondaryContainer to ColorRole.OnSecondaryContainer
    "secondary" -> ColorRole.Secondary to ColorRole.OnSecondary
    "tertiary-container" -> ColorRole.TertiaryContainer to ColorRole.OnTertiaryContainer
    "tertiary" -> ColorRole.Tertiary to ColorRole.OnTertiary
    "surface" -> ColorRole.SurfaceContainerHigh to ColorRole.Primary
    else -> ColorRole.PrimaryContainer to ColorRole.OnPrimaryContainer
  }

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun RenderFab(ctx: NodeContext) {
  val env = ctx.env
  val (containerRole, contentRole) = fabColors(ctx.enum("color", "primary-container"))
  val container = env.color(containerRole)
  val content = env.color(contentRole)
  val action = ctx.props.action("action")
  val onClick = { env.act(action) }
  val icon = ctx.string("icon").orEmpty()
  val label = ctx.string("label")
  val size = ctx.enum("size", "small")
  val noElevation = FloatingActionButtonDefaults.elevation(0.dp, 0.dp, 0.dp, 0.dp)
  val extended = ctx.bool("extended") && label != null
  val shape =
    when (size) {
      "medium" ->
        if (extended) FloatingActionButtonDefaults.mediumExtendedFabShape
        else FloatingActionButtonDefaults.mediumShape
      "large" ->
        if (extended) FloatingActionButtonDefaults.largeExtendedFabShape
        else FloatingActionButtonDefaults.largeShape
      else ->
        if (extended) FloatingActionButtonDefaults.extendedFabShape
        else FloatingActionButtonDefaults.shape
    }
  // The elevation shadow is drawn by level3Shadow (see there), so the component itself has none.
  val modifier = ctx.modifier().then(ctx.describedBy()).level3Shadow(shape)
  val iconSize =
    when (size) {
      "medium" -> FloatingActionButtonDefaults.MediumIconSize
      "large" -> FloatingActionButtonDefaults.LargeIconSize
      else -> 24.dp
    }
  val iconSlot: @Composable () -> Unit = { SymbolIcon(icon, size = iconSize) }
  if (extended && label != null) {
    val text: @Composable () -> Unit = { ctx.PartText(label) }
    when (size) {
      "medium" ->
        MediumExtendedFloatingActionButton(
          text,
          iconSlot,
          onClick,
          modifier,
          containerColor = container,
          contentColor = content,
          elevation = noElevation,
        )
      "large" ->
        LargeExtendedFloatingActionButton(
          text,
          iconSlot,
          onClick,
          modifier,
          containerColor = container,
          contentColor = content,
          elevation = noElevation,
        )
      else ->
        ExtendedFloatingActionButton(
          text,
          iconSlot,
          onClick,
          modifier,
          containerColor = container,
          contentColor = content,
          elevation = noElevation,
        )
    }
  } else {
    when (size) {
      "medium" ->
        MediumFloatingActionButton(
          onClick,
          modifier,
          containerColor = container,
          contentColor = content,
          elevation = noElevation,
          content = iconSlot,
        )
      "large" ->
        LargeFloatingActionButton(
          onClick,
          modifier,
          containerColor = container,
          contentColor = content,
          elevation = noElevation,
          content = iconSlot,
        )
      else ->
        FloatingActionButton(
          onClick,
          modifier,
          containerColor = container,
          contentColor = content,
          elevation = noElevation,
          content = iconSlot,
        )
    }
  }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun RenderFabMenu(ctx: NodeContext) {
  val expanded = ctx.checkedState("expanded")
  FloatingActionButtonMenu(
    expanded = expanded.value,
    button = {
      ToggleFloatingActionButton(
        checked = expanded.value,
        onCheckedChange = { expanded.set(it) },
        modifier = ctx.tagged(),
      ) {
        SymbolIcon(ctx.string("icon").orEmpty(), size = 24.dp)
      }
    },
  ) {
    ctx.Children("children", 0, ChildHost(fillWidth = false, fabMenu = this))
  }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun RenderFabMenuItem(ctx: NodeContext) {
  val scope = ctx.host.fabMenu ?: return
  val action = ctx.props.action("action")
  with(scope) {
    FloatingActionButtonMenuItem(
      onClick = { ctx.env.act(action) },
      text = { ctx.PartText(ctx.string("label").orEmpty()) },
      icon = { SymbolIcon(ctx.string("icon").orEmpty(), size = 24.dp) },
      modifier = ctx.modifier(),
    )
  }
}
