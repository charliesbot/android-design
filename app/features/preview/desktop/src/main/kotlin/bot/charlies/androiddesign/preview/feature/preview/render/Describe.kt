package bot.charlies.androiddesign.preview.feature.preview.render

import bot.charlies.androiddesign.preview.feature.preview.record.NodeMeta
import bot.charlies.androiddesign.preview.feature.preview.theme.toHex
import bot.charlies.androiddesign.preview.model.ColorRole
import bot.charlies.androiddesign.preview.model.M3Component
import bot.charlies.androiddesign.preview.model.NodeColors
import bot.charlies.androiddesign.preview.model.NodeRoles
import bot.charlies.androiddesign.preview.model.TypeRole

/**
 * The roles and the text a component draws with, before they become hex and font names. Each
 * component family builds its own beside its renderer, from the same role decisions.
 */
internal class Look(
  val container: ColorRole,
  val content: ColorRole,
  val type: TypeRole? = null,
  val shape: String? = null,
  val text: String? = null,
  val a11yRole: String? = null,
) {
  /** A look that takes both roles from [on]. */
  constructor(
    on: Ambient,
    type: TypeRole? = null,
    shape: String? = null,
    text: String? = null,
    a11yRole: String? = null,
  ) : this(on.container, on.content, type, shape, text, a11yRole)
}

/**
 * What the tree says about [ctx]: the Material roles its colors come from (the family's `*Look`
 * beside its renderer), its text, and how a screen reader would announce it. The measured parts
 * (bounds, text layout) are added by the recorder.
 */
internal fun describe(ctx: NodeContext, ambient: Ambient): NodeMeta {
  val env = ctx.env
  val look = renderKitOf(ctx.component).look(ctx, ambient)
  val label = ctx.string("label") ?: ctx.string("contentDescription") ?: look.text
  val interactive =
    ctx.def.interactive || (ctx.component in ACTION_HOSTS && ctx.props.action("action") != null)
  return NodeMeta(
    id = ctx.id,
    type = ctx.component,
    parent = ctx.parentId,
    order = ctx.order,
    primary = ctx.primary,
    variant = if (ctx.component == M3Component.DatePicker.wire) ctx.enum("variant") else null,
    weight = ctx.weightOnAxis(),
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
    font = look.type?.takeIf { look.text != null }?.let { env.phone.fontFamilies[it] },
    a11yRole = look.a11yRole,
    a11yLabel = label,
    actions =
      buildList {
        if (interactive) add("click")
      },
  )
}

private val ACTION_HOSTS = setOf(M3Component.Card.wire, M3Component.ListItem.wire)
