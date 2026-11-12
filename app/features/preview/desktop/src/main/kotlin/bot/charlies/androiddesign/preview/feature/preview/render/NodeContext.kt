package bot.charlies.androiddesign.preview.feature.preview.render

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import bot.charlies.androiddesign.preview.domain.CarouselItemGate
import bot.charlies.androiddesign.preview.domain.layout.growsSemanticsBounds
import bot.charlies.androiddesign.preview.domain.node.PropReader
import bot.charlies.androiddesign.preview.feature.preview.record.ArrangementNeed
import bot.charlies.androiddesign.preview.feature.preview.record.NodeMeta
import bot.charlies.androiddesign.preview.feature.preview.record.TextReport
import bot.charlies.androiddesign.preview.feature.preview.render.wear.LocalWearAmbient
import bot.charlies.androiddesign.preview.feature.preview.render.wear.WearStyledAmbient
import bot.charlies.androiddesign.preview.feature.preview.render.wear.describeWear
import bot.charlies.androiddesign.preview.feature.preview.render.wear.wearStyleBackground
import bot.charlies.androiddesign.preview.model.Axis
import bot.charlies.androiddesign.preview.model.Catalog
import bot.charlies.androiddesign.preview.model.ColorRole
import bot.charlies.androiddesign.preview.model.ComponentDef
import bot.charlies.androiddesign.preview.model.ComponentNode
import bot.charlies.androiddesign.preview.model.CornerClip
import bot.charlies.androiddesign.preview.model.LayoutScope
import bot.charlies.androiddesign.preview.model.M3Component
import bot.charlies.androiddesign.preview.model.M3ExpressiveCatalog
import bot.charlies.androiddesign.preview.model.MaterialShape
import bot.charlies.androiddesign.preview.model.NodeWeight
import bot.charlies.androiddesign.preview.model.ShapeToken
import bot.charlies.androiddesign.preview.model.StyleDef
import bot.charlies.androiddesign.preview.model.TypeRole
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

/** A node's named style, read from `material.styles`. Explicit props win over it. */
internal class ResolvedStyle(
  val type: TypeRole?,
  val color: ColorRole?,
  val container: ColorRole?,
  val shape: String?,
  val padding: String?,
  /** The style as declared, whose role names the Wear renderers read as they are. */
  val def: StyleDef? = null,
) {
  companion object {
    val None = ResolvedStyle(null, null, null, null, null)
  }
}

/** One component being drawn: its definition, effective props and place in the tree. */
internal class NodeContext(
  val env: RenderEnv,
  val node: ComponentNode,
  val def: ComponentDef,
  /** The tree id: the node id plus template indices, plus the pane prefix. */
  val id: String,
  val parentId: String?,
  val order: List<Int>,
  val props: Props,
  val scope: DataScope,
  val style: ResolvedStyle,
  val host: ChildHost,
) {
  val component: String
    get() = node.component

  /** This component drawn in [other], for a second copy that must not record itself. */
  fun withEnv(other: RenderEnv) =
    NodeContext(other, node, def, id, parentId, order, props, scope, style, host)

  fun string(key: String) = props.string(key)

  /** This node's props read with their catalog defaults and bounds. */
  val reader: PropReader = PropReader(def, props::value)

  /** A boolean prop: its value, else the catalog default, else false. */
  fun bool(key: String): Boolean = reader.bool(key)

  /** An enum prop: its value, else the catalog default, which every enum read here has. */
  fun enum(key: String): String = reader.enum(key)

  val primary: Boolean
    get() = bool("primary")

  val enabled: Boolean
    get() = bool("enabled")

  /** The padding around the component: the `padding` prop, else its style. */
  val padding: Dp?
    get() = env.spacing(props.string("padding") ?: style.padding)

  fun spacing(key: String): Dp? = env.spacing(props.string(key))

  /** A number prop, moved into the bounds the catalog gives it (see [PropReader.number]). */
  fun number(key: String): Float? = reader.number(key)?.toFloat()

  /**
   * Whether this component stretches its children to its width. Not when it wraps (a Column in a
   * list item's trailing slot, or an unweighted one in a Row): a stretched child would take all the
   * width the slot offers. The exporter asks the catalog the same way.
   */
  val stretchesChildren: Boolean
    get() =
      def.stretchesChildrenWhen(
        def.wrapsAt(host.fillWidth, host.row != null && number("weight") != null, props::value),
      )

  /** A whole-number prop, moved into its bounds like [number]. */
  fun whole(key: String): Int? = number(key)?.toInt()

  /** A number prop with a catalog default: [number], else the default. */
  fun numberOrDefault(key: String): Float = reader.numberOrDefault(key).toFloat()

  /** A whole-number prop with a catalog default: [whole], else the default. */
  fun wholeOrDefault(key: String): Int = numberOrDefault(key).toInt()

  /** The text style of the `type` prop, else the style's, else [default]. */
  fun textStyle(default: TypeRole): TextStyle = env.typeStyle(typeRole(default))

  fun typeRole(default: TypeRole): TypeRole =
    props.string("type")?.let(TypeRole::fromWire) ?: style.type ?: default

  fun colorRole(key: String = "color"): ColorRole? =
    props.string(key)?.let(ColorRole::fromWire) ?: if (key == "color") style.color else null

  /** The token or `MaterialShapes` polygon named by the `shape` prop (or the style). */
  fun shapeName(key: String = "shape"): String? = props.string(key) ?: style.shape

  /** Draws a child at [slot] of this node. */
  @Composable
  fun ChildNode(
    id: String?,
    slot: Int,
    host: ChildHost = ChildHost.Wrap,
    scope: DataScope = this.scope,
    index: Int = 0,
  ) {
    if (id == null) return
    RenderNode(env, ChildRef(id, scope), this.id, order + listOf(slot, index), host)
  }

  /**
   * [host] as the catalog scopes slot [key] ([ComponentDef.childLayout]): the `RowScope` or
   * `ColumnScope` the composable hosts is kept for `weight` only where the catalog says the slot
   * has it, so the exporter, which reads the same declaration, writes `weight` in the same slots.
   */
  private fun scoped(key: String, host: ChildHost): ChildHost {
    val layout = def.childLayout(key, props::value)
    return host.copy(
      row = host.row.takeIf { layout == LayoutScope.Row },
      column = host.column.takeIf { layout == LayoutScope.Column },
    )
  }

  /** Draws the child a `Child` prop points at. */
  @Composable
  fun Child(key: String, slot: Int, host: ChildHost = ChildHost.Wrap) {
    ChildNode(props.childId(key), slot, scoped(key, host))
  }

  /** Draws the children of a `ChildList` prop; each is told its place among its siblings. */
  @Composable
  fun Children(key: String, slot: Int, host: ChildHost = ChildHost.Flow) {
    val refs = props.children(key)
    val scoped = scoped(key, host)
    refs.forEachIndexed { index, ref ->
      RenderNode(env, ref, id, order + listOf(slot, index), scoped.at(index, refs.size))
    }
  }

  /**
   * Like [Children], but each child is told its place among the siblings that show at this size
   * class, for a parent whose children are shaped by their neighbors (a menu's groups and items),
   * where a hidden sibling is not a neighbor.
   */
  @Composable
  fun ShownChildren(key: String, slot: Int, host: ChildHost = ChildHost.Flow) {
    val shown =
      props.children(key).withIndex().filter { (_, ref) ->
        env.nodes[ref.id]?.isShownAt(env.sizeClass) == true
      }
    val scoped = scoped(key, host)
    shown.forEachIndexed { position, (index, ref) ->
      RenderNode(env, ref, id, order + listOf(slot, index), scoped.at(position, shown.size))
    }
  }
}

/**
 * Whether [node]'s `itemSize` lets it show on the carousel item it is in, at the item's size right
 * now. The size changes every scroll frame, so the answer is derived state: the node recomposes
 * only when it flips, as in the exported code.
 */
@Composable
private fun shownOnCarouselItem(node: ComponentNode): Boolean {
  val minimum = node.carouselItemSize
  val info = LocalCarouselItemDrawInfo.current
  val density = LocalDensity.current.density
  if (minimum == null || info == null) return true
  val shown by
    remember(minimum, info, density) {
      derivedStateOf { CarouselItemGate.isShown(minimum, info.size, info.maxSize, density) }
    }
  return shown
}

/** The props of [node] in the frame of [env], with its size class overrides applied. */
internal fun effectiveProps(node: ComponentNode, env: RenderEnv): JsonObject {
  val overrides = node.sizeOverridesAt(env.catalog.sizeBreakpoints, env.widthDp)
  if (overrides.isEmpty()) return node.props
  return JsonObject(LinkedHashMap(node.props).apply { putAll(overrides) })
}

private fun resolveStyle(env: RenderEnv, node: ComponentNode): ResolvedStyle {
  val name = (node.props[M3ExpressiveCatalog.STYLE] as? JsonPrimitive)?.content
  val def = env.style(name) ?: return ResolvedStyle.None
  // A Wear style names Wear roles, which the phone's role enums do not know: it is read from def.
  if (env.isWear) return ResolvedStyle(null, null, null, def.shape, def.padding, def)
  return ResolvedStyle(
    type = def.type?.let(TypeRole::fromWire),
    color = def.color?.let(ColorRole::fromWire),
    container = def.container?.let(ColorRole::fromWire),
    shape = def.shape,
    padding = def.padding,
    def = def,
  )
}

/**
 * Draws one component of the screen, if it shows at the current size class. Records it, then
 * dispatches to the composable of its catalog type.
 */
@Composable
internal fun RenderNode(
  env: RenderEnv,
  ref: ChildRef,
  parentId: String?,
  order: List<Int>,
  host: ChildHost,
) {
  val node = env.nodes[ref.id] ?: return
  val def = env.catalog.find(node.component) ?: return
  if (!node.isShownAt(env.sizeClass)) return
  if (!shownOnCarouselItem(node)) return
  val ctx =
    NodeContext(
      env = env,
      node = node,
      def = def,
      id = env.idPrefix + node.id + ref.scope.instanceSuffix,
      parentId = parentId,
      order = order,
      props = Props(effectiveProps(node, env), ref.scope),
      scope = ref.scope,
      style = resolveStyle(env, node),
      host = host,
    )
  val recorder = env.recorder
  if (recorder != null) {
    // The ambient is read here, while composing: registering runs later, as a side effect.
    val meta: () -> NodeMeta =
      if (env.isWear) {
        val ambient = LocalWearAmbient.current
        { describeWear(ctx, ambient) }
      } else {
        val ambient = LocalAmbient.current
        { describe(ctx, ambient) }
      }
    SideEffect { recorder.register(meta()) }
    DisposableEffect(ctx.id) { onDispose { recorder.unregister(ctx.id) } }
  }
  val menuId = env.placement.menuAt(node.id)
  val tooltipId = env.placement.tooltipAt(node.id)
  val anchored: @Composable () -> Unit =
    if (tooltipId != null && !env.isOverlay(node)) {
      { TooltipAnchor(env, tooltipId, ref.scope, ctx.id, order) { RenderComponent(ctx) } }
    } else {
      { RenderComponent(ctx) }
    }
  val pickerId = env.placement.datePickerAt(node.id)
  if ((menuId != null || pickerId != null) && !env.isOverlay(node)) {
    androidx.compose.foundation.layout.Box {
      anchored()
      if (menuId != null) MenuOverlay(env, menuId, ref.scope, ctx.id, order)
      if (pickerId != null) PickerOverlay(env, pickerId, ref.scope, ctx.id, order)
    }
  } else {
    anchored()
  }
}

/**
 * The modifier of a component, in the order the exporter writes it (`ScreenEmitter.modifier`):
 * weight, stretch, its style's container when [container] is set, a vertical scroll when [scroll]
 * is set, padding, then its id for the tree. The container comes before the padding so the padding
 * sits inside it, and the scroll comes before the padding so the padding scrolls with the content
 * rather than cropping it. It stretches across the width when the catalog says the component does
 * and the parent lets it. The tree reports the box inside the padding, which for a scrolling
 * component is its whole content, not the viewport. A component that pads its own content inside
 * its container (a Card) sets [padded] to false and applies [padding] there.
 */
@Composable
internal fun NodeContext.modifier(
  fillHeight: Boolean = false,
  container: Boolean = false,
  scroll: Boolean = false,
  padded: Boolean = true,
): Modifier {
  var modifier: Modifier = Modifier
  if (host.flexItem && growsSemanticsBounds(node, env.nodes, env.catalog, ::flexProps)) {
    modifier = modifier.restoreUnboundedHeight()
  }
  val weight = number("weight")
  if (weight != null) {
    host.column?.let { modifier = modifier.then(with(it) { Modifier.weight(weight) }) }
    host.row?.let { modifier = modifier.then(with(it) { Modifier.weight(weight) }) }
  }
  modifier = modifier.then(host.scopeModifier)
  number(Catalog.MAX_WIDTH)?.let { modifier = modifier.widthIn(max = it.dp) }
  if (host.fillWidth && def.fillsWidth(props::value)) modifier = modifier.fillMaxWidth()
  if (fillHeight && host.fillHeight) modifier = modifier.fillMaxHeight()
  // Before the container and the padding, so it sees the box the parent lays this node out in.
  env.recorder?.let { recorder ->
    modifier = modifier.onGloballyPositioned { recorder.reportOuterSize(id, it.size) }
  }
  if (container) {
    modifier = modifier.then(styleBackground())
    ReportClip(styleClip(scroll, padded))
  }
  if (scroll) modifier = modifier.verticalScroll(rememberScrollState())
  if (padded) padding?.let { modifier = modifier.padding(it) }
  return modifier.then(tagged())
}

/**
 * The weight [modifier] gives this node and the axis it shares, for the tree: a column's height or
 * a row's width. Null without a weight, or where the parent's slot has no `RowScope` or
 * `ColumnScope`.
 */
internal fun NodeContext.weightOnAxis(): NodeWeight? {
  val weight = number("weight") ?: return null
  return when {
    host.column != null -> NodeWeight(weight, Axis.Height)
    host.row != null -> NodeWeight(weight, Axis.Width)
    else -> null
  }
}

/**
 * The arrangement a Row or Column draws, when it needs free space on its main axis to show. It
 * wraps when this node is neither weighted in its parent's scope nor stretched along that axis: a
 * Row stretches across the width the way [modifier] does it, a Column down the height when its
 * parent offers it.
 */
internal fun NodeContext.arrangementNeed(): ArrangementNeed? {
  val name = string("arrangement") ?: return null
  if (name !in SPACE_NEEDING) return null
  val horizontal = component == M3Component.Row.wire
  if (!horizontal && component != M3Component.Column.wire) return null
  val weighted =
    number("weight") != null && (if (horizontal) host.row != null else host.column != null)
  val stretched =
    if (horizontal) host.fillWidth && def.fillsWidth(props::value) else host.fillHeight
  val gap = if (name in setOf("center", "end")) spacing("spacing")?.value ?: 0f else 0f
  return ArrangementNeed(
    name = name,
    axis = if (horizontal) Axis.Width else Axis.Height,
    gapDp = gap,
    wraps = !weighted && !stretched,
  )
}

private val SPACE_NEEDING = setOf("spaceBetween", "spaceAround", "spaceEvenly", "center", "end")

/** The props [node] is drawn with in this context's frame, as [growsSemanticsBounds] asks. */
private fun NodeContext.flexProps(other: ComponentNode): List<JsonObject> =
  listOf(effectiveProps(other, env))

/**
 * FlexBox measures a wrapped item with the largest height Constraints can hold instead of an
 * unbounded one. Material components that grow their semantics bounds then add to that maximum and
 * overflow Constraints. This hands such an item the unbounded height it was meant to get.
 */
private fun Modifier.restoreUnboundedHeight(): Modifier = layout { measurable, constraints ->
  // Fitting a too-large height clamps it to the largest one this width can hold.
  val largest =
    Constraints.fitPrioritizingWidth(
        constraints.minWidth,
        constraints.maxWidth,
        0,
        Constraints.Infinity - 1,
      )
      .maxHeight
  val bounded =
    if (constraints.hasBoundedHeight && constraints.maxHeight >= largest) {
      constraints.copy(maxHeight = Constraints.Infinity)
    } else {
      constraints
    }
  val placeable = measurable.measure(bounded)
  layout(placeable.width, placeable.height) { placeable.place(0, 0) }
}

/** Makes the component findable by id and reports where it landed. */
@Composable
internal fun NodeContext.tagged(): Modifier {
  val recorder = env.recorder
  val tagId = id
  return Modifier.testTag(tagId).let { base ->
    if (recorder == null) base else base.onGloballyPositioned { recorder.reportPosition(tagId, it) }
  }
}

/** The container role behind this node: its style's `container`, else the [ambient] one. */
internal fun NodeContext.backdropRole(ambient: Ambient): ColorRole =
  style.container ?: ambient.container

/** The ambient this node's style makes for its content: its container and color over [ambient]. */
internal fun NodeContext.styledAmbient(ambient: Ambient): Ambient =
  Ambient(backdropRole(ambient), style.color ?: ambient.content)

/**
 * The background a style's `container` gives, for components that have no container of their own.
 */
@Composable
internal fun NodeContext.styleBackground(): Modifier {
  if (env.isWear) return wearStyleBackground()
  val container = style.container ?: return Modifier
  val shape = shapeName()?.let { shapeFor(it) } ?: RectangleShape
  return Modifier.clip(shape).background(env.color(container), shape)
}

/**
 * The rounded shape a style's `container` clips this node to, for the tree. The shape is drawn
 * around the padding, so the padding is its outset. Null on Wear, for a scrolling container (the
 * tree reports its whole content, not the shape's box), and for a shape with no single corner
 * radius.
 */
private fun NodeContext.styleClip(scroll: Boolean, padded: Boolean): CornerClip? {
  if (env.isWear || scroll || style.container == null) return null
  val radius = shapeName()?.let(env::cornerRadius) ?: return null
  return CornerClip(radius, outset = if (padded) padding?.value ?: 0f else 0f)
}

/** Reports to the recorder the rounded shape this node clips its content to, or none. */
@Composable
internal fun NodeContext.ReportClip(clip: CornerClip?) {
  val recorder = env.recorder ?: return
  val tagId = id
  SideEffect { recorder.reportClip(tagId, clip) }
}

/** Draws [content] with the style's container and content colors as the ambient ones. */
@Composable
internal fun NodeContext.StyledAmbient(content: @Composable () -> Unit) {
  if (env.isWear) {
    WearStyledAmbient(content)
  } else if (style.container == null && style.color == null) {
    content()
  } else {
    val styled = styledAmbient(LocalAmbient.current)
    CompositionLocalProvider(
      LocalAmbient provides styled,
      androidx.compose.material3.LocalContentColor provides env.color(styled.content),
    ) {
      content()
    }
  }
}

/**
 * The corner radius in dp of the shape [name], as [shapeFor] draws it: a token's, infinite for a
 * full shape (the tree caps it at half the shorter side), and null for a square or a
 * `MaterialShapes` polygon, which has no single corner radius.
 */
internal fun RenderEnv.cornerRadius(name: String): Float? {
  val token = ShapeToken.fromWire(name) ?: return null
  val dp = design.rounded[token] ?: 0f
  return when {
    token == ShapeToken.Full || dp >= 1000f -> Float.POSITIVE_INFINITY
    token == ShapeToken.None || dp <= 0f -> null
    else -> dp
  }
}

/** A token (`large`) or `MaterialShapes` name (`sunny`) as a Compose shape. */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun shapeFor(name: String): Shape? {
  val env = LocalRenderEnv.current
  ShapeToken.fromWire(name)?.let { token ->
    val dp = env.design.rounded[token] ?: 0f
    return when {
      token == ShapeToken.None -> RectangleShape
      token == ShapeToken.Full || dp >= 1000f -> CircleShape
      else -> RoundedCornerShape(dp.dp)
    }
  }
  val polygon = MaterialShape.fromWire(name) ?: return null
  return polygon.toPolygon().toShape()
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
private fun MaterialShape.toPolygon() =
  when (this) {
    MaterialShape.Circle -> MaterialShapes.Circle
    MaterialShape.Square -> MaterialShapes.Square
    MaterialShape.Slanted -> MaterialShapes.Slanted
    MaterialShape.Arch -> MaterialShapes.Arch
    MaterialShape.Fan -> MaterialShapes.Fan
    MaterialShape.Arrow -> MaterialShapes.Arrow
    MaterialShape.SemiCircle -> MaterialShapes.SemiCircle
    MaterialShape.Oval -> MaterialShapes.Oval
    MaterialShape.Pill -> MaterialShapes.Pill
    MaterialShape.Triangle -> MaterialShapes.Triangle
    MaterialShape.Diamond -> MaterialShapes.Diamond
    MaterialShape.ClamShell -> MaterialShapes.ClamShell
    MaterialShape.Pentagon -> MaterialShapes.Pentagon
    MaterialShape.Gem -> MaterialShapes.Gem
    MaterialShape.Sunny -> MaterialShapes.Sunny
    MaterialShape.VerySunny -> MaterialShapes.VerySunny
    MaterialShape.Cookie4Sided -> MaterialShapes.Cookie4Sided
    MaterialShape.Cookie6Sided -> MaterialShapes.Cookie6Sided
    MaterialShape.Cookie7Sided -> MaterialShapes.Cookie7Sided
    MaterialShape.Cookie9Sided -> MaterialShapes.Cookie9Sided
    MaterialShape.Cookie12Sided -> MaterialShapes.Cookie12Sided
    MaterialShape.Ghostish -> MaterialShapes.Ghostish
    MaterialShape.Clover4Leaf -> MaterialShapes.Clover4Leaf
    MaterialShape.Clover8Leaf -> MaterialShapes.Clover8Leaf
    MaterialShape.Burst -> MaterialShapes.Burst
    MaterialShape.SoftBurst -> MaterialShapes.SoftBurst
    MaterialShape.Boom -> MaterialShapes.Boom
    MaterialShape.SoftBoom -> MaterialShapes.SoftBoom
    MaterialShape.Flower -> MaterialShapes.Flower
    MaterialShape.Puffy -> MaterialShapes.Puffy
    MaterialShape.PuffyDiamond -> MaterialShapes.PuffyDiamond
    MaterialShape.PixelCircle -> MaterialShapes.PixelCircle
    MaterialShape.PixelTriangle -> MaterialShapes.PixelTriangle
    MaterialShape.Bun -> MaterialShapes.Bun
    MaterialShape.Heart -> MaterialShapes.Heart
  }

/** Reports a text's layout to the recorder under [part]. */
internal fun NodeContext.textReporter(
  part: String = "text",
): (androidx.compose.ui.text.TextLayoutResult) -> Unit {
  val recorder = env.recorder
  val nodeId = id
  return { layout -> recorder?.reportText(nodeId, part, TextReport.of(layout)) }
}
