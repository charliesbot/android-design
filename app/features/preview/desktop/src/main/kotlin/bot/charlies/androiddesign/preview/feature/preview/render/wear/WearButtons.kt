package bot.charlies.androiddesign.preview.feature.preview.render.wear

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.wear.compose.material3.ButtonColors
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.ButtonGroup
import androidx.wear.compose.material3.CheckboxButton
import androidx.wear.compose.material3.CompactButton
import androidx.wear.compose.material3.EdgeButton
import androidx.wear.compose.material3.EdgeButtonDefaults
import androidx.wear.compose.material3.EdgeButtonSize
import androidx.wear.compose.material3.FilledIconButton
import androidx.wear.compose.material3.FilledTonalIconButton
import androidx.wear.compose.material3.IconButton
import androidx.wear.compose.material3.IconButtonDefaults
import androidx.wear.compose.material3.IconToggleButton
import androidx.wear.compose.material3.IconToggleButtonDefaults
import androidx.wear.compose.material3.OutlinedIconButton
import androidx.wear.compose.material3.RadioButton
import androidx.wear.compose.material3.SwitchButton
import androidx.wear.compose.material3.TextButton
import androidx.wear.compose.material3.TextToggleButton
import androidx.wear.compose.material3.TextToggleButtonDefaults
import bot.charlies.androiddesign.preview.feature.preview.render.ChildHost
import bot.charlies.androiddesign.preview.feature.preview.render.NodeContext
import bot.charlies.androiddesign.preview.feature.preview.render.checkedState
import bot.charlies.androiddesign.preview.feature.preview.render.describedBy
import bot.charlies.androiddesign.preview.feature.preview.render.isPickedIn
import bot.charlies.androiddesign.preview.feature.preview.render.modifier
import bot.charlies.androiddesign.preview.feature.preview.render.selectInGroup

// region looks

/** The container and content roles of a button [variant], from the upstream token tables. */
private fun variantRoles(variant: String, ambient: WearAmbient): Pair<String, String> =
  when (variant) {
    "filled" -> "primary" to "onPrimary"
    "filledTonal" -> "surfaceContainer" to "onSurface"
    "filledVariant" -> "primaryContainer" to "onPrimaryContainer"
    else -> ambient.container to "onSurface" // outlined and child sit on what is behind them
  }

internal fun NodeContext.buttonLook(ambient: WearAmbient): WearLook {
  val (container, content) = variantRoles(enum("variant"), ambient)
  val type = if (component == "CompactButton") "labelSmall" else "labelMedium"
  return WearLook(container, content, type, "full", string("label"), "Button")
}

internal fun NodeContext.iconButtonLook(ambient: WearAmbient): WearLook {
  val (container, content) =
    when (enum("variant")) {
      "filled" -> "primary" to "onPrimary"
      "filledTonal" -> "surfaceContainer" to "primary"
      else -> ambient.container to "primary"
    }
  return WearLook(container, content, shape = "full", a11yRole = "Button")
}

internal fun NodeContext.textButtonLook(ambient: WearAmbient): WearLook {
  val (container, content) =
    when (enum("variant")) {
      "filled" -> "primary" to "onPrimary"
      "filledTonal" -> "surfaceContainer" to "onSurface"
      else -> ambient.container to "onSurface"
    }
  return WearLook(container, content, "labelMedium", "full", string("label"), "Button")
}

internal fun NodeContext.edgeButtonLook() =
  WearLook("primary", "onPrimary", "labelMedium", "edge", string("label"), "Button")

internal fun NodeContext.buttonGroupLook(ambient: WearAmbient) = WearLook(ambient)

internal fun NodeContext.toggleButtonLook(): WearLook {
  val checked = bool("checked")
  val container = if (checked) "primary" else "surfaceContainer"
  val content = if (checked) "onPrimary" else "onSurfaceVariant"
  val text = string("label")
  return WearLook(
    container,
    content,
    if (text != null) "labelMedium" else null,
    "full",
    text,
    "Checkbox",
  )
}

internal fun NodeContext.rowToggleLook(): WearLook {
  val on = if (component == "RadioButton") bool("selected") else bool("checked")
  val role =
    when (component) {
      "RadioButton" -> "RadioButton"
      "CheckboxButton" -> "Checkbox"
      else -> "Switch"
    }
  return WearLook(
    if (on) "primaryContainer" else "surfaceContainer",
    if (on) "onPrimaryContainer" else "onSurface",
    "labelMedium",
    "full",
    string("label"),
    role,
  )
}

// endregion

private fun NodeContext.onClick(): () -> Unit = { env.act(props.action("action")) }

private val NodeContext.labelSlot: @Composable RowScope.() -> Unit
  get() = { WearText(string("label").orEmpty()) }

private val NodeContext.secondarySlot: (@Composable RowScope.() -> Unit)?
  get() = string("secondaryLabel")?.let { text -> { WearText(text, "secondaryLabel") } }

private val NodeContext.iconSlot:
  (@Composable
  androidx.compose.foundation.layout.BoxScope.() -> Unit)?
  get() = string("icon")?.let { name -> { WearIcon(name, ButtonDefaults.IconSize) } }

@Composable
private fun NodeContext.colorsOf(variant: String): ButtonColors =
  when (variant) {
    "filledTonal" -> ButtonDefaults.filledTonalButtonColors()
    "filledVariant" -> ButtonDefaults.filledVariantButtonColors()
    "outlined" -> ButtonDefaults.outlinedButtonColors()
    "child" -> ButtonDefaults.childButtonColors()
    else -> ButtonDefaults.buttonColors()
  }

@Composable
internal fun RenderWearButton(ctx: NodeContext) {
  val variant = ctx.enum("variant")
  androidx.wear.compose.material3.Button(
    onClick = ctx.onClick(),
    modifier = ctx.modifier().then(ctx.describedBy()).then(ctx.itemModifier()),
    secondaryLabel = ctx.secondarySlot,
    icon = ctx.iconSlot,
    enabled = ctx.enabled,
    colors = ctx.colorsOf(variant),
    border = if (variant == "outlined") ButtonDefaults.outlinedButtonBorder(ctx.enabled) else null,
    transformation = ctx.transformation(),
    label = ctx.labelSlot,
  )
}

@Composable
internal fun RenderWearCompactButton(ctx: NodeContext) {
  val variant = ctx.enum("variant")
  val label = ctx.string("label")
  CompactButton(
    onClick = ctx.onClick(),
    modifier = ctx.modifier().then(ctx.describedBy()).then(ctx.itemModifier()),
    icon = ctx.iconSlot,
    enabled = ctx.enabled,
    colors = ctx.colorsOf(variant),
    border = if (variant == "outlined") ButtonDefaults.outlinedButtonBorder(ctx.enabled) else null,
    label = label?.let { text -> { ctx.WearText(text) } },
  )
}

private fun iconButtonSize(size: String): Dp =
  when (size) {
    "extra-small" -> IconButtonDefaults.ExtraSmallButtonSize
    "small" -> IconButtonDefaults.SmallButtonSize
    "large" -> IconButtonDefaults.LargeButtonSize
    else -> IconButtonDefaults.DefaultButtonSize
  }

@Composable
internal fun RenderWearIconButton(ctx: NodeContext) {
  val size = iconButtonSize(ctx.enum("size"))
  val modifier = ctx.modifier().then(ctx.describedBy()).then(ctx.itemModifier()).size(size)
  val name = ctx.string("icon")
  val content: @Composable androidx.compose.foundation.layout.BoxScope.() -> Unit = {
    WearIcon(name, IconButtonDefaults.iconSizeFor(size))
  }
  val onClick = ctx.onClick()
  when (ctx.enum("variant")) {
    "filled" -> FilledIconButton(onClick, modifier, enabled = ctx.enabled, content = content)
    "filledTonal" ->
      FilledTonalIconButton(onClick, modifier, enabled = ctx.enabled, content = content)
    "outlined" -> OutlinedIconButton(onClick, modifier, enabled = ctx.enabled, content = content)
    else -> IconButton(onClick, modifier, enabled = ctx.enabled, content = content)
  }
}

private fun textButtonSize(size: String): Dp =
  when (size) {
    "small" -> androidx.wear.compose.material3.TextButtonDefaults.SmallButtonSize
    "large" -> androidx.wear.compose.material3.TextButtonDefaults.LargeButtonSize
    else -> androidx.wear.compose.material3.TextButtonDefaults.DefaultButtonSize
  }

@Composable
internal fun RenderWearTextButton(ctx: NodeContext) {
  val modifier =
    ctx
      .modifier()
      .then(ctx.describedBy())
      .then(ctx.itemModifier())
      .size(textButtonSize(ctx.enum("size")))
  val onClick = ctx.onClick()
  val content: @Composable androidx.compose.foundation.layout.BoxScope.() -> Unit = {
    ctx.WearText(ctx.string("label").orEmpty())
  }
  val defaults = androidx.wear.compose.material3.TextButtonDefaults
  val colors =
    when (ctx.enum("variant")) {
      "filled" -> defaults.filledTextButtonColors()
      "filledTonal" -> defaults.filledTonalTextButtonColors()
      "outlined" -> defaults.outlinedTextButtonColors()
      else -> defaults.textButtonColors()
    }
  val border =
    if (ctx.enum("variant") == "outlined") ButtonDefaults.outlinedButtonBorder(ctx.enabled)
    else null
  TextButton(
    onClick,
    modifier,
    enabled = ctx.enabled,
    colors = colors,
    border = border,
    content = content,
  )
}

private fun edgeButtonSize(size: String): EdgeButtonSize =
  when (size) {
    "extra-small" -> EdgeButtonSize.ExtraSmall
    "medium" -> EdgeButtonSize.Medium
    "large" -> EdgeButtonSize.Large
    else -> EdgeButtonSize.Small
  }

@Composable
internal fun RenderWearEdgeButton(ctx: NodeContext) {
  val size = edgeButtonSize(ctx.enum("size"))
  val icon = ctx.string("icon")
  val label = ctx.string("label")
  EdgeButton(
    onClick = ctx.onClick(),
    modifier = ctx.modifier().then(ctx.describedBy()),
    buttonSize = size,
    enabled = ctx.enabled,
  ) {
    if (icon != null) WearIcon(icon, EdgeButtonDefaults.iconSizeFor(size))
    if (label != null) ctx.WearText(label)
  }
}

@Composable
internal fun RenderWearButtonGroup(ctx: NodeContext) {
  ButtonGroup(modifier = ctx.modifier().then(ctx.itemModifier())) {
    // The group's scope gives each child its weight, so every member shares the width.
    ctx.Children(
      "children",
      0,
      ChildHost(fillWidth = false, groupKey = ctx.id, scopeModifier = Modifier.weight(1f)),
    )
  }
}

// region toggles

@Composable
internal fun RenderWearIconToggleButton(ctx: NodeContext) {
  val checked = ctx.checkedState()
  val size = iconButtonSize(ctx.enum("size"))
  val name = ctx.string("icon")
  IconToggleButton(
    checked = checked.value,
    onCheckedChange = {
      checked.set(it)
      ctx.env.act(ctx.props.action("action"))
    },
    modifier = ctx.modifier().then(ctx.describedBy()).then(ctx.itemModifier()).size(size),
    enabled = ctx.enabled,
  ) {
    WearIcon(name, IconToggleButtonDefaults.iconSizeFor(size), filled = checked.value)
  }
}

private fun toggleSize(size: String): Dp =
  when (size) {
    "small" -> IconToggleButtonDefaults.SmallSize
    "large" -> TextToggleButtonDefaults.LargeSize
    else -> TextToggleButtonDefaults.Size
  }

@Composable
internal fun RenderWearTextToggleButton(ctx: NodeContext) {
  val checked = ctx.checkedState()
  TextToggleButton(
    checked = checked.value,
    onCheckedChange = {
      checked.set(it)
      ctx.env.act(ctx.props.action("action"))
    },
    modifier =
      ctx
        .modifier()
        .then(ctx.describedBy())
        .then(ctx.itemModifier())
        .size(toggleSize(ctx.enum("size"))),
    enabled = ctx.enabled,
  ) {
    ctx.WearText(ctx.string("label").orEmpty())
  }
}

@Composable
internal fun RenderWearSwitchButton(ctx: NodeContext) {
  val checked = ctx.checkedState()
  SwitchButton(
    checked = checked.value,
    onCheckedChange = {
      checked.set(it)
      ctx.env.act(ctx.props.action("action"))
    },
    modifier = ctx.modifier().then(ctx.describedBy()).then(ctx.itemModifier()),
    enabled = ctx.enabled,
    transformation = ctx.transformation(),
    icon = ctx.iconSlot,
    secondaryLabel = ctx.secondarySlot,
    label = ctx.labelSlot,
  )
}

@Composable
internal fun RenderWearCheckboxButton(ctx: NodeContext) {
  val checked = ctx.checkedState()
  CheckboxButton(
    checked = checked.value,
    onCheckedChange = {
      checked.set(it)
      ctx.env.act(ctx.props.action("action"))
    },
    modifier = ctx.modifier().then(ctx.describedBy()).then(ctx.itemModifier()),
    enabled = ctx.enabled,
    transformation = ctx.transformation(),
    icon = ctx.iconSlot,
    secondaryLabel = ctx.secondarySlot,
    label = ctx.labelSlot,
  )
}

/** A radio button is one of its parent's group: tapping it picks it and unpicks the others. */
@Composable
internal fun RenderWearRadioButton(ctx: NodeContext) {
  val group = ctx.host.groupKey ?: ctx.parentId.orEmpty()
  RadioButton(
    selected = ctx.isPickedIn(group),
    onSelect = {
      ctx.selectInGroup(group)
      ctx.env.act(ctx.props.action("action"))
    },
    modifier = ctx.modifier().then(ctx.describedBy()).then(ctx.itemModifier()),
    enabled = ctx.enabled,
    transformation = ctx.transformation(),
    icon = ctx.iconSlot,
    secondaryLabel = ctx.secondarySlot,
    label = ctx.labelSlot,
  )
}

// endregion
