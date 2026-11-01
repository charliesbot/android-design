package bot.charlies.androiddesign.preview.feature.preview.render

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import bot.charlies.androiddesign.preview.feature.preview.record.TextReport
import bot.charlies.androiddesign.preview.model.ColorRole
import bot.charlies.androiddesign.preview.model.ComponentDef
import bot.charlies.androiddesign.preview.model.ComponentNode
import bot.charlies.androiddesign.preview.model.M3ExpressiveCatalog
import bot.charlies.androiddesign.preview.model.MaterialShape
import bot.charlies.androiddesign.preview.model.NumberBounds
import bot.charlies.androiddesign.preview.model.PropKind
import bot.charlies.androiddesign.preview.model.ShapeToken
import bot.charlies.androiddesign.preview.model.SizeClass
import bot.charlies.androiddesign.preview.model.TypeRole
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.float

/** A node's named style, read from `material.styles`. Explicit props win over it. */
internal class ResolvedStyle(
  val type: TypeRole?,
  val color: ColorRole?,
  val container: ColorRole?,
  val shape: String?,
  val padding: String?,
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

  fun string(key: String) = props.string(key)

  /** A boolean prop: its value, else the catalog default, else false. */
  fun bool(key: String): Boolean = props.bool(key) ?: def.defaultOf(key)?.booleanOrNull ?: false

  /** An enum prop: its value, else the catalog default, which every enum read here has. */
  fun enum(key: String): String = props.string(key) ?: requiredDefault(key).content

  val primary: Boolean
    get() = bool("primary")

  val enabled: Boolean
    get() = bool("enabled")

  private fun requiredDefault(key: String): JsonPrimitive =
    def.defaultOf(key) ?: error("${def.name}.$key has no catalog default")

  /** The padding around the component: the `padding` prop, else its style. */
  val padding: Dp?
    get() = env.spacing(props.string("padding") ?: style.padding)

  fun spacing(key: String): Dp? = env.spacing(props.string(key))

  /**
   * A number prop, moved into the bounds the catalog gives it. A bound value skips validation, so
   * this is what keeps one that is out of range from reaching a Compose API that throws on it.
   */
  fun number(key: String): Float? {
    val value = props.float(key) ?: return null
    return boundsOf(key)?.coerce(value.toDouble())?.toFloat() ?: value
  }

  /** A whole-number prop, moved into its bounds like [number]. */
  fun whole(key: String): Int? = number(key)?.toInt()

  /** A number prop with a catalog default: [number], else the default. */
  fun numberOrDefault(key: String): Float = number(key) ?: requiredDefault(key).float

  /** A whole-number prop with a catalog default: [whole], else the default. */
  fun wholeOrDefault(key: String): Int = numberOrDefault(key).toInt()

  private fun boundsOf(key: String): NumberBounds? =
    ((def.props + M3ExpressiveCatalog.commonProps).firstOrNull { it.name == key }?.kind
        as? PropKind.Number)
      ?.bounds

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

  /** Draws the child a `Child` prop points at. */
  @Composable
  fun Child(key: String, slot: Int, host: ChildHost = ChildHost.Wrap) {
    ChildNode(props.childId(key), slot, host)
  }

  /** Draws the children of a `ChildList` prop; each is told its place among its siblings. */
  @Composable
  fun Children(key: String, slot: Int, host: ChildHost = ChildHost.Flow) {
    val refs = props.children(key)
    refs.forEachIndexed { index, ref ->
      RenderNode(env, ref, id, order + listOf(slot, index), host.at(index, refs.size))
    }
  }
}

/** The props of [node] at [sizeClass], with its size class overrides applied. */
private fun effectiveProps(node: ComponentNode, sizeClass: SizeClass): JsonObject {
  val overrides = node.sizeOverridesAt(sizeClass)
  if (overrides.isEmpty()) return node.props
  return JsonObject(LinkedHashMap(node.props).apply { putAll(overrides) })
}

private fun visibleAt(node: ComponentNode, sizeClass: SizeClass): Boolean {
  val showIn = node.props[M3ExpressiveCatalog.SHOW_IN] as? JsonArray ?: return true
  return showIn.any { (it as? JsonPrimitive)?.content == sizeClass.wire }
}

private fun resolveStyle(env: RenderEnv, node: ComponentNode): ResolvedStyle {
  val name = (node.props[M3ExpressiveCatalog.STYLE] as? JsonPrimitive)?.content
  val def = env.style(name) ?: return ResolvedStyle.None
  return ResolvedStyle(
    type = def.type?.let(TypeRole::fromWire),
    color = def.color?.let(ColorRole::fromWire),
    container = def.container?.let(ColorRole::fromWire),
    shape = def.shape,
    padding = def.padding,
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
  val def = M3ExpressiveCatalog.find(node.component) ?: return
  if (!visibleAt(node, env.sizeClass)) return
  val ctx =
    NodeContext(
      env = env,
      node = node,
      def = def,
      id = env.idPrefix + node.id + ref.scope.instanceSuffix,
      parentId = parentId,
      order = order,
      props = Props(effectiveProps(node, env.sizeClass), ref.scope),
      scope = ref.scope,
      style = resolveStyle(env, node),
      host = host,
    )
  val ambient = LocalAmbient.current
  val recorder = env.recorder
  if (recorder != null) {
    SideEffect { recorder.register(describe(ctx, ambient)) }
    DisposableEffect(ctx.id) { onDispose { recorder.unregister(ctx.id) } }
  }
  val menuId = env.anchoredMenus[node.id]
  if (menuId != null && !env.isOverlay(node)) {
    androidx.compose.foundation.layout.Box {
      RenderComponent(ctx)
      MenuOverlay(env, menuId, ref.scope, ctx.id, order)
    }
  } else {
    RenderComponent(ctx)
  }
}

/** The modifier of a component: weight, stretch, padding, then its id for the tree. */
@Composable
internal fun NodeContext.modifier(
  fillWidth: Boolean = false,
  fillHeight: Boolean = false,
): Modifier {
  var modifier: Modifier = Modifier
  if (host.flexItem) modifier = modifier.restoreUnboundedHeight()
  val weight = number("weight")
  if (weight != null) {
    host.column?.let { modifier = modifier.then(with(it) { Modifier.weight(weight) }) }
    host.row?.let { modifier = modifier.then(with(it) { Modifier.weight(weight) }) }
  }
  if (fillWidth && host.fillWidth) modifier = modifier.fillMaxWidth()
  if (fillHeight && host.fillHeight) modifier = modifier.fillMaxHeight()
  padding?.let { modifier = modifier.padding(it) }
  return modifier.then(tagged())
}

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

/**
 * The background a style's `container` gives, for components that have no container of their own.
 */
@Composable
internal fun NodeContext.styleBackground(): Modifier {
  val container = style.container ?: return Modifier
  val shape = shapeName()?.let { shapeFor(it) } ?: RectangleShape
  return Modifier.clip(shape).background(env.color(container), shape)
}

/** Draws [content] with the style's container and content colors as the ambient ones. */
@Composable
internal fun NodeContext.StyledAmbient(content: @Composable () -> Unit) {
  val ambient = LocalAmbient.current
  val container = style.container ?: ambient.container
  val contentRole = style.color ?: ambient.content
  if (style.container == null && style.color == null) {
    content()
  } else {
    CompositionLocalProvider(
      LocalAmbient provides Ambient(container, contentRole),
      androidx.compose.material3.LocalContentColor provides env.color(contentRole),
    ) {
      content()
    }
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
