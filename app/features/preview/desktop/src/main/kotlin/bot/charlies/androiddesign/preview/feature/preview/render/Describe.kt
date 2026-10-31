package bot.charlies.androiddesign.preview.feature.preview.render

import bot.charlies.androiddesign.preview.feature.preview.record.NodeMeta
import bot.charlies.androiddesign.preview.feature.preview.theme.toHex
import bot.charlies.androiddesign.preview.model.ColorRole
import bot.charlies.androiddesign.preview.model.NodeColors
import bot.charlies.androiddesign.preview.model.NodeRoles
import bot.charlies.androiddesign.preview.model.TypeRole

/** The roles and the text a component draws with, before they become hex and font names. */
private class Look(
  val container: ColorRole,
  val content: ColorRole,
  val type: TypeRole? = null,
  val shape: String? = null,
  val text: String? = null,
  val a11yRole: String? = null,
)

/**
 * What the tree says about [ctx]: the Material roles its colors come from (the same table the
 * renderer passes to the components, from `compose-mapping.md`), its text, and how a screen reader
 * would announce it. The measured parts (bounds, text layout) are added by the recorder.
 */
internal fun describe(ctx: NodeContext, ambient: Ambient): NodeMeta {
  val env = ctx.env
  val look = look(ctx, ambient)
  val label = ctx.string("label") ?: ctx.string("contentDescription") ?: look.text
  val interactive =
    ctx.def.interactive || (ctx.component in ACTION_HOSTS && ctx.props.action("action") != null)
  return NodeMeta(
    id = ctx.id,
    type = ctx.component,
    parent = ctx.parentId,
    order = ctx.order,
    primary = ctx.primary,
    interactive = interactive,
    roles =
      NodeRoles(
        container = look.container.wire,
        content = look.content.wire,
        type = look.type?.wire,
        shape = look.shape,
      ),
    colors =
      NodeColors(
        container = env.color(look.container).toHex(),
        content = env.color(look.content).toHex(),
      ),
    text = look.text?.takeIf { it.isNotEmpty() },
    font = look.type?.takeIf { look.text != null }?.let { env.theme.fontFamilies[it] },
    a11yRole = look.a11yRole,
    a11yLabel = label,
    actions =
      buildList {
        if (interactive) add("click")
      },
  )
}

private val ACTION_HOSTS = setOf("Card", "ListItem")

private fun buttonType(size: String): TypeRole =
  when (size) {
    "medium" -> TypeRole.TitleMedium
    "large" -> TypeRole.HeadlineSmall
    "extra-large" -> TypeRole.HeadlineLarge
    else -> TypeRole.LabelLarge
  }

private fun look(ctx: NodeContext, ambient: Ambient): Look {
  val on = ambient.container
  return when (ctx.component) {
    "Text" ->
      Look(
        ambient.container,
        ctx.colorRole() ?: ambient.content,
        ctx.typeRole(TypeRole.BodyLarge),
        text = ctx.string("text"),
      )
    "Icon" -> Look(on, ctx.colorRole() ?: ambient.content, a11yRole = "Image")
    "Image" -> Look(on, ambient.content, a11yRole = "Image")
    "Scaffold" -> Look(ColorRole.Background, ColorRole.OnBackground)
    "TopAppBar" ->
      Look(ColorRole.Surface, ColorRole.OnSurface, TypeRole.TitleLarge, text = ctx.string("title"))
    "NavigationBar" -> Look(ColorRole.SurfaceContainer, ColorRole.OnSurfaceVariant)
    "NavigationBarItem" ->
      Look(
        ColorRole.SurfaceContainer,
        ColorRole.OnSurfaceVariant,
        TypeRole.LabelMedium,
        text = ctx.string("label"),
        a11yRole = "Tab",
      )
    "NavigationRail" -> Look(ColorRole.Surface, ColorRole.OnSurfaceVariant)
    "NavigationRailItem" ->
      Look(
        ColorRole.Surface,
        ColorRole.OnSurfaceVariant,
        TypeRole.LabelMedium,
        text = ctx.string("label"),
        a11yRole = "Tab",
      )
    "FloatingToolbar" ->
      if (ctx.enum("colors", "standard") == "vibrant") {
        Look(ColorRole.PrimaryContainer, ColorRole.OnPrimaryContainer, shape = "full")
      } else {
        Look(ColorRole.SurfaceContainer, ColorRole.OnSurface, shape = "full")
      }
    "Card" -> {
      val container =
        ctx.style.container
          ?: when (ctx.enum("variant", "filled")) {
            "elevated" -> ColorRole.SurfaceContainerLow
            "outlined" -> ColorRole.Surface
            else -> ColorRole.SurfaceContainerHighest
          }
      Look(container, ctx.style.color ?: ColorRole.OnSurface, shape = ctx.shapeName() ?: "medium")
    }
    "ListItem" ->
      Look(
        if (ctx.host.segmented) ColorRole.SurfaceContainer else ColorRole.Surface,
        ColorRole.OnSurface,
        TypeRole.BodyLarge,
        text = ctx.string("headline"),
        a11yRole = if (ctx.props.action("action") != null) "Button" else null,
      )
    "Button" -> {
      val (container, content) =
        when (ctx.enum("variant", "filled")) {
          "tonal" -> ColorRole.SecondaryContainer to ColorRole.OnSecondaryContainer
          "elevated" -> ColorRole.SurfaceContainerLow to ColorRole.Primary
          "outlined",
          "text" -> on to ColorRole.Primary
          else -> ColorRole.Primary to ColorRole.OnPrimary
        }
      Look(
        container,
        content,
        buttonType(ctx.enum("size", "small")),
        if (ctx.enum("shape", "round") == "square") "large" else "full",
        ctx.string("label"),
        "Button",
      )
    }
    "ToggleButton" -> {
      val checked = ctx.bool("checked")
      val (container, content) =
        when (ctx.enum("variant", "filled")) {
          "tonal" ->
            if (checked) ColorRole.Secondary to ColorRole.OnSecondary
            else ColorRole.SecondaryContainer to ColorRole.OnSecondaryContainer
          "elevated" ->
            if (checked) ColorRole.Primary to ColorRole.OnPrimary
            else ColorRole.SurfaceContainerLow to ColorRole.Primary
          "outlined" ->
            if (checked) ColorRole.InverseSurface to ColorRole.InverseOnSurface
            else on to ColorRole.OnSurfaceVariant
          else ->
            if (checked) ColorRole.Primary to ColorRole.OnPrimary
            else ColorRole.SurfaceContainer to ColorRole.OnSurface
        }
      Look(
        container,
        content,
        buttonType(ctx.enum("size", "small")),
        "full",
        ctx.string("label"),
        "Checkbox",
      )
    }
    "ButtonGroup" -> Look(on, ambient.content)
    "IconButton" -> {
      val checked = ctx.bool("toggle") && ctx.bool("checked")
      val toggle = ctx.bool("toggle")
      val (container, content) =
        when (ctx.enum("variant", "standard")) {
          "filled" ->
            if (toggle && !checked) ColorRole.SurfaceContainerHighest to ColorRole.Primary
            else ColorRole.Primary to ColorRole.OnPrimary
          "tonal" ->
            if (toggle && !checked) ColorRole.SurfaceContainerHighest to ColorRole.OnSurfaceVariant
            else ColorRole.SecondaryContainer to ColorRole.OnSecondaryContainer
          "outlined" ->
            if (checked) ColorRole.InverseSurface to ColorRole.InverseOnSurface
            else on to ColorRole.OnSurfaceVariant
          else -> on to (if (checked) ColorRole.Primary else ColorRole.OnSurfaceVariant)
        }
      Look(container, content, shape = "full", a11yRole = if (toggle) "Checkbox" else "Button")
    }
    "SplitButton" -> {
      val (container, content) =
        when (ctx.enum("variant", "filled")) {
          "tonal" -> ColorRole.SecondaryContainer to ColorRole.OnSecondaryContainer
          "elevated" -> ColorRole.SurfaceContainerLow to ColorRole.Primary
          "outlined" -> on to ColorRole.Primary
          else -> ColorRole.Primary to ColorRole.OnPrimary
        }
      Look(container, content, TypeRole.LabelLarge, "full", ctx.string("label"), "Button")
    }
    "FloatingActionButton" -> {
      val (container, content) = fabColors(ctx.enum("color", "primary-container"))
      val shape =
        when (ctx.enum("size", "small")) {
          "medium" -> "largeIncreased"
          "large" -> "extraLarge"
          else -> "large"
        }
      Look(
        container,
        content,
        if (ctx.bool("extended")) TypeRole.LabelLarge else null,
        shape,
        ctx.string("label")?.takeIf { ctx.bool("extended") },
        "Button",
      )
    }
    "FabMenu" ->
      Look(
        ColorRole.PrimaryContainer,
        ColorRole.OnPrimaryContainer,
        shape = "large",
        a11yRole = "Button",
      )
    "FabMenuItem" ->
      Look(
        ColorRole.PrimaryContainer,
        ColorRole.OnPrimaryContainer,
        TypeRole.TitleMedium,
        "full",
        ctx.string("label"),
        "Button",
      )
    "AssistChip",
    "SuggestionChip" ->
      Look(
        if (ctx.enum("variant", "outlined") == "elevated") ColorRole.SurfaceContainerLow else on,
        ColorRole.OnSurface,
        TypeRole.LabelLarge,
        "small",
        ctx.string("label"),
        "Button",
      )
    "FilterChip",
    "InputChip" -> {
      val selected = ctx.bool("selected")
      val container =
        when {
          selected -> ColorRole.SecondaryContainer
          ctx.enum("variant", "outlined") == "elevated" -> ColorRole.SurfaceContainerLow
          else -> on
        }
      Look(
        container,
        if (selected) ColorRole.OnSecondaryContainer else ColorRole.OnSurfaceVariant,
        TypeRole.LabelLarge,
        "small",
        ctx.string("label"),
        "Checkbox",
      )
    }
    "ChipGroup" -> Look(on, ambient.content)
    "Switch" ->
      if (ctx.bool("checked"))
        Look(ColorRole.Primary, ColorRole.OnPrimary, shape = "full", a11yRole = "Switch")
      else
        Look(
          ColorRole.SurfaceContainerHighest,
          ColorRole.Outline,
          shape = "full",
          a11yRole = "Switch",
        )
    "Checkbox" ->
      Look(
        on,
        ctx.colorRole() ?: ColorRole.OnSurface,
        TypeRole.BodyLarge,
        text = ctx.string("label"),
        a11yRole = "Checkbox",
      )
    "Slider",
    "RangeSlider" -> Look(on, ColorRole.Primary)
    "TextField" -> {
      val filled = ctx.enum("variant", "filled") == "filled"
      Look(
        if (filled) ColorRole.SurfaceContainerHighest else on,
        ColorRole.OnSurface,
        TypeRole.BodyLarge,
        "extraSmall",
        ctx.string("value")?.takeIf { it.isNotEmpty() } ?: ctx.string("label"),
      )
    }
    "LoadingIndicator" ->
      if (ctx.bool("contained"))
        Look(ColorRole.SecondaryContainer, ColorRole.Primary, shape = "full")
      else Look(on, ColorRole.Primary)
    "ProgressIndicator" -> Look(on, ColorRole.Primary)
    "BottomSheet" -> Look(ColorRole.SurfaceContainerLow, ColorRole.OnSurface, shape = "extraLarge")
    "Dialog" ->
      Look(
        ColorRole.SurfaceContainerHigh,
        ColorRole.OnSurface,
        TypeRole.HeadlineSmall,
        "extraLarge",
        ctx.string("title"),
      )
    "Menu" -> Look(ColorRole.SurfaceContainer, ColorRole.OnSurface, shape = "extraLarge")
    "MenuItem" ->
      Look(
        ColorRole.SurfaceContainer,
        ColorRole.OnSurface,
        TypeRole.LabelLarge,
        text = ctx.string("label"),
        a11yRole = "Button",
      )
    else -> Look(ctx.style.container ?: on, ctx.style.color ?: ambient.content)
  }
}
