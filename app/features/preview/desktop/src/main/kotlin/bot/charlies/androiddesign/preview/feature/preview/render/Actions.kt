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
import bot.charlies.androiddesign.preview.model.TypeRole

internal fun buttonHeight(size: String): Dp =
  when (size) {
    "extra-small" -> ButtonDefaults.ExtraSmallContainerHeight
    "medium" -> ButtonDefaults.MediumContainerHeight
    "large" -> ButtonDefaults.LargeContainerHeight
    "extra-large" -> ButtonDefaults.ExtraLargeContainerHeight
    else -> ButtonDefaults.MinHeight
  }

private fun buttonType(size: String): TypeRole =
  when (size) {
    "medium" -> TypeRole.TitleMedium
    "large" -> TypeRole.HeadlineSmall
    "extra-large" -> TypeRole.HeadlineLarge
    else -> TypeRole.LabelLarge
  }

/** Adds the node's `contentDescription` as accessibility text. */
internal fun NodeContext.describedBy(): Modifier =
  string("contentDescription")?.let { text -> Modifier.semantics { contentDescription = text } }
    ?: Modifier

/** The icon, spacer and label inside a button of [height]; the icon is [filled] when selected. */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ButtonContent(
  ctx: NodeContext,
  icon: String?,
  label: String,
  height: Dp,
  filled: Boolean = false,
) {
  if (icon != null) {
    SymbolIcon(icon, size = ButtonDefaults.iconSizeFor(height), filled = filled)
    Spacer(Modifier.width(ButtonDefaults.iconSpacingFor(height)))
  }
  ctx.PartText(label, style = ButtonDefaults.textStyleFor(height))
}

/** The button's variant colors with its named style over them, as `RenderButton` paints them. */
internal fun NodeContext.buttonLook(ambient: Ambient): Look {
  val (container, content) =
    when (enum("variant")) {
      "tonal" -> ColorRole.SecondaryContainer to ColorRole.OnSecondaryContainer
      "elevated" -> ColorRole.SurfaceContainerLow to ColorRole.Primary
      "outlined",
      "text" -> ambient.container to ColorRole.Primary
      else -> ColorRole.Primary to ColorRole.OnPrimary
    }
  return Look(
    style.container ?: container,
    style.color ?: content,
    buttonType(enum("size")),
    if (enum("shape") == "square") "large" else "full",
    string("label"),
    "Button",
  )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun RenderButton(ctx: NodeContext) {
  val env = ctx.env
  val height = buttonHeight(ctx.enum("size"))
  val icon = ctx.string("icon")
  val label = ctx.string("label").orEmpty()
  val action = ctx.props.action("action")
  val shapes: ButtonShapes =
    if (ctx.enum("shape") == "square") {
      ButtonDefaults.shapes(ButtonDefaults.squareShape, ButtonDefaults.pressedShape)
    } else {
      ButtonDefaults.shapesFor(height)
    }
  val contentPadding = ButtonDefaults.contentPaddingFor(height, icon != null, false)
  val modifier = ctx.modifier().heightIn(min = height).then(ctx.describedBy())
  val content: @Composable RowScope.() -> Unit = { ButtonContent(ctx, icon, label, height) }
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
  when (ctx.enum("variant")) {
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

internal fun NodeContext.toggleButtonLook(ambient: Ambient): Look {
  val checked = bool("checked")
  val (container, content) =
    when (enum("variant")) {
      "tonal" ->
        if (checked) ColorRole.Secondary to ColorRole.OnSecondary
        else ColorRole.SecondaryContainer to ColorRole.OnSecondaryContainer
      "elevated" ->
        if (checked) ColorRole.Primary to ColorRole.OnPrimary
        else ColorRole.SurfaceContainerLow to ColorRole.Primary
      "outlined" ->
        if (checked) ColorRole.InverseSurface to ColorRole.InverseOnSurface
        else ambient.container to ColorRole.OnSurfaceVariant
      else ->
        if (checked) ColorRole.Primary to ColorRole.OnPrimary
        else ColorRole.SurfaceContainer to ColorRole.OnSurface
    }
  return Look(container, content, buttonType(enum("size")), "full", string("label"), "Checkbox")
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun RenderToggleButton(ctx: NodeContext) {
  val env = ctx.env
  val height = buttonHeight(ctx.enum("size"))
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
    ButtonContent(ctx, icon, label, height, filled = checked.value)
  }
  val enabled = ctx.enabled
  when (ctx.enum("variant")) {
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

internal fun NodeContext.buttonGroupLook(ambient: Ambient) =
  Look(ambient.container, ambient.content)

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalLayoutApi::class)
@Composable
internal fun RenderButtonGroup(ctx: NodeContext) {
  if (ctx.enum("variant") == "connected") {
    Row(
      modifier = ctx.modifier(),
      horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
    ) {
      ctx.Children(
        "children",
        0,
        ChildHost(
          fillWidth = ctx.def.stretchesChildren,
          row = this,
          connected = true,
          groupKey = ctx.id,
        ),
      )
    }
  } else {
    Row(
      modifier = ctx.modifier(),
      horizontalArrangement = ButtonGroupDefaults.HorizontalArrangement,
    ) {
      ctx.Children(
        "children",
        0,
        ChildHost(fillWidth = ctx.def.stretchesChildren, row = this, groupKey = ctx.id),
      )
    }
  }
}

internal fun NodeContext.iconButtonLook(ambient: Ambient): Look {
  val toggle = bool("toggle")
  val checked = toggle && bool("checked")
  val (container, content) =
    when (enum("variant")) {
      "filled" ->
        if (toggle && !checked) ColorRole.SurfaceContainerHighest to ColorRole.Primary
        else ColorRole.Primary to ColorRole.OnPrimary
      "tonal" ->
        if (toggle && !checked) ColorRole.SurfaceContainerHighest to ColorRole.OnSurfaceVariant
        else ColorRole.SecondaryContainer to ColorRole.OnSecondaryContainer
      "outlined" ->
        if (checked) ColorRole.InverseSurface to ColorRole.InverseOnSurface
        else ambient.container to ColorRole.OnSurfaceVariant
      else -> ambient.container to (if (checked) ColorRole.Primary else ColorRole.OnSurfaceVariant)
    }
  return Look(container, content, shape = "full", a11yRole = if (toggle) "Checkbox" else "Button")
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
    when (ctx.enum("width")) {
      "narrow" -> IconButtonDefaults.IconButtonWidthOption.Narrow
      "wide" -> IconButtonDefaults.IconButtonWidthOption.Wide
      else -> IconButtonDefaults.IconButtonWidthOption.Uniform
    }
  val size = ctx.enum("size")
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
  val variant = ctx.enum("variant")
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

internal fun NodeContext.splitButtonLook(ambient: Ambient): Look {
  val (container, content) =
    when (enum("variant")) {
      "tonal" -> ColorRole.SecondaryContainer to ColorRole.OnSecondaryContainer
      "elevated" -> ColorRole.SurfaceContainerLow to ColorRole.Primary
      "outlined" -> ambient.container to ColorRole.Primary
      else -> ColorRole.Primary to ColorRole.OnPrimary
    }
  return Look(container, content, TypeRole.LabelLarge, "full", string("label"), "Button")
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun RenderSplitButton(ctx: NodeContext) {
  val env = ctx.env
  val label = ctx.string("label").orEmpty()
  val icon = ctx.string("icon")
  val action = ctx.props.action("action")
  val menuId = ctx.props.childId("menu")
  val menuOpen = menuId != null && env.openOverlay == menuId
  val variant = ctx.enum("variant")
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
      if (open) env.open(menuId) else env.dismiss()
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

internal fun fabColors(name: String): Pair<ColorRole, ColorRole> =
  when (name) {
    "primary" -> ColorRole.Primary to ColorRole.OnPrimary
    "secondary-container" -> ColorRole.SecondaryContainer to ColorRole.OnSecondaryContainer
    "secondary" -> ColorRole.Secondary to ColorRole.OnSecondary
    "tertiary-container" -> ColorRole.TertiaryContainer to ColorRole.OnTertiaryContainer
    "tertiary" -> ColorRole.Tertiary to ColorRole.OnTertiary
    "surface" -> ColorRole.SurfaceContainerHigh to ColorRole.Primary
    else -> ColorRole.PrimaryContainer to ColorRole.OnPrimaryContainer
  }

internal fun NodeContext.fabLook(): Look {
  val (container, content) = fabColors(enum("color"))
  val shape =
    when (enum("size")) {
      "medium" -> "largeIncreased"
      "large" -> "extraLarge"
      else -> "large"
    }
  val extended = bool("extended")
  return Look(
    container,
    content,
    if (extended) TypeRole.LabelLarge else null,
    shape,
    string("label")?.takeIf { extended },
    "Button",
  )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun RenderFab(ctx: NodeContext) {
  val env = ctx.env
  val (containerRole, contentRole) = fabColors(ctx.enum("color"))
  val container = env.color(containerRole)
  val content = env.color(contentRole)
  val action = ctx.props.action("action")
  val onClick = { env.act(action) }
  val icon = ctx.string("icon").orEmpty()
  val label = ctx.string("label")
  val size = ctx.enum("size")
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

internal fun NodeContext.fabMenuLook() =
  Look(
    ColorRole.PrimaryContainer,
    ColorRole.OnPrimaryContainer,
    shape = "large",
    a11yRole = "Button",
  )

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
    ctx.Children("children", 0, ChildHost(fillWidth = ctx.def.stretchesChildren, fabMenu = this))
  }
}

internal fun NodeContext.fabMenuItemLook() =
  Look(
    ColorRole.PrimaryContainer,
    ColorRole.OnPrimaryContainer,
    TypeRole.TitleMedium,
    "full",
    string("label"),
    "Button",
  )

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
