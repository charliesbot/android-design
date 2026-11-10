package bot.charlies.androiddesign.preview.feature.preview.render.wear

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.Dp
import androidx.wear.compose.foundation.lazy.TransformingLazyColumnItemScope
import androidx.wear.compose.material3.LocalContentColor
import androidx.wear.compose.material3.SurfaceTransformation
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.lazy.TransformationSpec
import bot.charlies.androiddesign.preview.feature.preview.record.NodeMeta
import bot.charlies.androiddesign.preview.feature.preview.render.NodeContext
import bot.charlies.androiddesign.preview.feature.preview.render.RenderBox
import bot.charlies.androiddesign.preview.feature.preview.render.RenderColumn
import bot.charlies.androiddesign.preview.feature.preview.render.RenderImage
import bot.charlies.androiddesign.preview.feature.preview.render.RenderRow
import bot.charlies.androiddesign.preview.feature.preview.render.RenderSpacer
import bot.charlies.androiddesign.preview.feature.preview.render.SymbolIcon
import bot.charlies.androiddesign.preview.feature.preview.render.shapeFor
import bot.charlies.androiddesign.preview.feature.preview.render.textReporter
import bot.charlies.androiddesign.preview.feature.preview.theme.toHex
import bot.charlies.androiddesign.preview.model.NodeColors
import bot.charlies.androiddesign.preview.model.NodeRoles
import bot.charlies.androiddesign.preview.model.WearComponent
import java.util.EnumMap

/**
 * How one Wear component draws, and the look the layout tree records for it, like the phone's
 * `RenderKit`.
 */
internal class WearRenderKit(
  val render: @Composable (NodeContext) -> Unit,
  val look: NodeContext.(WearAmbient) -> WearLook,
)

/**
 * Draws one Wear component with its upstream Wear Compose Material 3 composable. [RenderNode] calls
 * this for a screen of the Wear catalog, in place of the phone's `RenderComponent`.
 */
@Composable
internal fun RenderWearComponent(ctx: NodeContext) {
  wearKitOf(ctx.component).render(ctx)
}

private fun wearKitOf(component: String): WearRenderKit =
  wearKits.getValue(WearComponent.of(component))

private val wearKits: Map<WearComponent, WearRenderKit> =
  WearComponent.entries.associateWithTo(EnumMap(WearComponent::class.java), ::wearKit)

private fun wearKit(component: WearComponent): WearRenderKit =
  when (component) {
    WearComponent.Column -> WearRenderKit({ RenderColumn(it) }) { layoutLook(it) }
    WearComponent.Row -> WearRenderKit({ RenderRow(it) }) { layoutLook(it) }
    WearComponent.Box -> WearRenderKit({ RenderBox(it) }) { layoutLook(it) }
    WearComponent.Spacer -> WearRenderKit({ RenderSpacer(it) }) { layoutLook(it) }
    WearComponent.Screen -> WearRenderKit({ RenderWearScreen(it) }) { _ -> screenLook() }
    WearComponent.ListHeader -> WearRenderKit({ RenderWearListHeader(it) }) { listHeaderLook(it) }
    WearComponent.ListSubHeader ->
      WearRenderKit({ RenderWearListSubHeader(it) }) { listSubHeaderLook(it) }
    WearComponent.Button -> WearRenderKit({ RenderWearButton(it) }) { buttonLook(it) }
    WearComponent.CompactButton -> WearRenderKit({ RenderWearCompactButton(it) }) { buttonLook(it) }
    WearComponent.IconButton -> WearRenderKit({ RenderWearIconButton(it) }) { iconButtonLook(it) }
    WearComponent.TextButton -> WearRenderKit({ RenderWearTextButton(it) }) { textButtonLook(it) }
    WearComponent.EdgeButton ->
      WearRenderKit({ RenderWearEdgeButton(it) }) { _ -> edgeButtonLook() }
    WearComponent.ButtonGroup ->
      WearRenderKit({ RenderWearButtonGroup(it) }) { buttonGroupLook(it) }
    WearComponent.IconToggleButton ->
      WearRenderKit({ RenderWearIconToggleButton(it) }) { _ -> toggleButtonLook() }
    WearComponent.TextToggleButton ->
      WearRenderKit({ RenderWearTextToggleButton(it) }) { _ -> toggleButtonLook() }
    WearComponent.SwitchButton ->
      WearRenderKit({ RenderWearSwitchButton(it) }) { _ -> rowToggleLook() }
    WearComponent.CheckboxButton ->
      WearRenderKit({ RenderWearCheckboxButton(it) }) { _ -> rowToggleLook() }
    WearComponent.RadioButton ->
      WearRenderKit({ RenderWearRadioButton(it) }) { _ -> rowToggleLook() }
    WearComponent.Text -> WearRenderKit({ RenderWearText(it) }) { textLook(it) }
    WearComponent.Icon -> WearRenderKit({ RenderWearIcon(it) }) { iconLook(it) }
    WearComponent.Image -> WearRenderKit({ RenderImage(it) }) { imageLook(it) }
    WearComponent.Card -> WearRenderKit({ RenderWearCard(it) }) { _ -> cardLook() }
    WearComponent.TitleCard -> WearRenderKit({ RenderWearTitleCard(it) }) { _ -> cardLook() }
    WearComponent.AppCard -> WearRenderKit({ RenderWearAppCard(it) }) { _ -> cardLook() }
    WearComponent.Slider -> WearRenderKit({ RenderWearSlider(it) }) { _ -> sliderLook() }
    WearComponent.Stepper -> WearRenderKit({ RenderWearStepper(it) }) { _ -> sliderLook() }
    WearComponent.CircularProgressIndicator ->
      WearRenderKit({ RenderWearCircularProgress(it) }) { progressLook(it) }
    WearComponent.LinearProgressIndicator ->
      WearRenderKit({ RenderWearLinearProgress(it) }) { progressLook(it) }
    WearComponent.SegmentedCircularProgressIndicator ->
      WearRenderKit({ RenderWearSegmentedProgress(it) }) { progressLook(it) }
    WearComponent.AlertDialog ->
      WearRenderKit({ RenderWearAlertDialog(it) }) { _ -> alertDialogLook() }
    WearComponent.ConfirmationDialog ->
      WearRenderKit({ RenderWearConfirmationDialog(it) }) { _ -> confirmationDialogLook() }
  }

// region ambient and looks

/** The container and content color roles (Wear wire names) behind and inside a component. */
internal class WearAmbient(val container: String, val content: String)

internal val LocalWearAmbient = compositionLocalOf { WearAmbient("background", "onBackground") }

/**
 * The roles and text a Wear component draws with, as
 * [bot.charlies.androiddesign.preview.feature.preview.render.Look] does for the phone.
 */
internal class WearLook(
  val container: String,
  val content: String,
  val type: String? = null,
  val shape: String? = null,
  val text: String? = null,
  val a11yRole: String? = null,
) {
  constructor(
    on: WearAmbient,
    content: String = on.content,
    type: String? = null,
    shape: String? = null,
    text: String? = null,
    a11yRole: String? = null,
  ) : this(on.container, content, type, shape, text, a11yRole)
}

/** What the tree says about [ctx], the Wear counterpart of the phone's `describe`. */
internal fun describeWear(ctx: NodeContext, ambient: WearAmbient): NodeMeta {
  val wear = ctx.env.wear
  val look = wearKitOf(ctx.component).look(ctx, ambient)
  val label = ctx.string("label") ?: ctx.string("contentDescription") ?: look.text
  val interactive = ctx.def.interactive
  return NodeMeta(
    id = ctx.id,
    type = ctx.component,
    parent = ctx.parentId,
    order = ctx.order,
    primary = ctx.primary,
    // An AlertDialog scrolls only when its content does not fit, which the recorder learns from
    // the layout (TreeRecorder.snapshot).
    scrolls = ctx.component == WearComponent.Screen.wire && ctx.bool("scroll"),
    interactive = interactive,
    roles =
      NodeRoles(
        container = look.container,
        content = look.content,
        type = look.type,
        shape = look.shape,
      ),
    colors =
      NodeColors(
        container = wear.color(look.container).toHex(),
        content = wear.color(look.content).toHex(),
      ),
    text = look.text?.takeIf { it.isNotEmpty() },
    font = look.type?.takeIf { look.text != null }?.let { wear.fontFamilies[it] },
    a11yRole = look.a11yRole,
    a11yLabel = label,
    actions = if (interactive) listOf("click") else emptyList(),
  )
}

// endregion

// region shared pieces

/** A text inside a component's slot; it reports its layout under [part], like the phone's. */
@Composable
internal fun NodeContext.WearText(
  text: String,
  part: String = "label",
  modifier: Modifier = Modifier,
) {
  Text(text = text, modifier = modifier, onTextLayout = textReporter(part))
}

/** A Material Symbols icon in the current Wear content color. */
@Composable
internal fun WearIcon(name: String?, size: Dp, filled: Boolean = false) {
  if (name.isNullOrEmpty()) return
  SymbolIcon(
    name = name,
    size = size,
    filled = filled,
    tint = LocalContentColor.current,
  )
}

/** An icon slot for a button: [WearIcon] at the size buttons give theirs. */
@Composable internal fun BoxScope.ButtonIcon(name: String?, size: Dp) = WearIcon(name, size)

/**
 * The scroll item a child of a scrolling Screen is. Its direct child applies the item's height
 * transformation and its surface transformation where the upstream component takes them, as the
 * official samples do; deeper components do not.
 */
internal class WearItem(
  val owner: String,
  val scope: TransformingLazyColumnItemScope,
  val spec: TransformationSpec,
)

internal val LocalWearItem = compositionLocalOf<WearItem?> { null }

private fun NodeContext.item(item: WearItem?): WearItem? = item?.takeIf { it.owner == parentId }

/**
 * The height transformation of the scroll item this component is, or none. It reports the height
 * the item measured before the display's edge scaled it down, which is the size the component was
 * designed at, so an item peeking in at the edge does not read as a too small touch target.
 */
@Composable
internal fun NodeContext.itemModifier(): Modifier {
  val item = item(LocalWearItem.current) ?: return Modifier
  val recorder = env.recorder
  val tagId = id
  return with(item.scope) {
    Modifier.transformedHeight { measuredHeight, progress ->
      recorder?.reportDesignHeight(tagId, measuredHeight.toFloat())
      item.spec.getTransformedHeight(measuredHeight, progress)
    }
  }
}

/** The surface transformation of the scroll item this component is, or none. */
@Composable
internal fun NodeContext.transformation(): SurfaceTransformation? =
  item(LocalWearItem.current)?.let { with(it.scope) { SurfaceTransformation(it.spec) } }

// endregion

// region style on layouts

/** A layout's look: its style's container and color over the ambient ones. */
internal fun NodeContext.layoutLook(ambient: WearAmbient): WearLook =
  WearLook(style.def?.container ?: ambient.container, style.def?.color ?: ambient.content)

/** The background a style's `container` gives, in the shape of the node's `shape` or style. */
@Composable
internal fun NodeContext.wearStyleBackground(): Modifier {
  val container = style.def?.container ?: return Modifier
  val shape = shapeName()?.let { shapeFor(it) } ?: RectangleShape
  return Modifier.clip(shape).background(env.wear.color(container), shape)
}

/** Draws [content] with the style's container and color roles as the ambient ones. */
@Composable
internal fun NodeContext.WearStyledAmbient(content: @Composable () -> Unit) {
  val def = style.def
  if (def?.container == null && def?.color == null) {
    content()
    return
  }
  val current = LocalWearAmbient.current
  val styled = WearAmbient(def.container ?: current.container, def.color ?: current.content)
  CompositionLocalProvider(
    LocalWearAmbient provides styled,
    LocalContentColor provides env.wear.color(styled.content),
  ) {
    content()
  }
}

// endregion
