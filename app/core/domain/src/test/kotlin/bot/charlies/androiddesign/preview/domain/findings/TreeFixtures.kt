package bot.charlies.androiddesign.preview.domain.findings

import bot.charlies.androiddesign.preview.model.Bounds
import bot.charlies.androiddesign.preview.model.CornerClip
import bot.charlies.androiddesign.preview.model.Finding
import bot.charlies.androiddesign.preview.model.FindingRule
import bot.charlies.androiddesign.preview.model.LayoutNode
import bot.charlies.androiddesign.preview.model.LayoutTree
import bot.charlies.androiddesign.preview.model.NodeColors
import bot.charlies.androiddesign.preview.model.NodeRoles
import bot.charlies.androiddesign.preview.model.RingInset
import bot.charlies.androiddesign.preview.model.Severity
import bot.charlies.androiddesign.preview.model.TextInfo
import bot.charlies.androiddesign.preview.model.ViewSpec
import bot.charlies.androiddesign.preview.model.Views

internal const val FLEX = "Google Sans Flex"

internal fun tree(view: ViewSpec = Views.Light, vararg nodes: LayoutNode) =
  LayoutTree(screen = "home", view = view, nodes = nodes.toList(), findings = emptyList())

internal fun node(
  id: String,
  type: String,
  x: Float = 0f,
  y: Float = 0f,
  w: Float = 100f,
  h: Float = 40f,
  parent: String? = null,
  primary: Boolean = false,
  scrolls: Boolean = false,
  layout: Bounds? = null,
  touch: Bounds? = null,
  text: TextInfo? = null,
  typeRole: String? = null,
  content: String? = null,
  container: String? = null,
  ring: RingInset? = null,
  clip: CornerClip? = null,
  variant: String? = null,
) =
  LayoutNode(
    id = id,
    type = type,
    parent = parent,
    primary = primary,
    scrolls = scrolls,
    bounds = Bounds(x, y, w, h),
    layout = layout,
    touch = touch,
    text = text,
    ring = ring,
    clip = clip,
    variant = variant,
    roles = typeRole?.let { NodeRoles(type = it) },
    colors = if (content != null || container != null) NodeColors(container, content) else null,
  )

internal fun text(
  value: String = "Label",
  lines: Int = 1,
  overflow: Boolean = false,
  truncated: Boolean = false,
  font: String? = FLEX,
  ink: Bounds? = null,
  undrawn: Boolean = false,
) = TextInfo(value, lines, overflow, truncated, font, ink, undrawn)

/** A touch area centered on the bounds and grown to at least 48 x 48 dp. */
internal fun touchOf(x: Float, y: Float, w: Float, h: Float): Bounds {
  val tw = maxOf(w, 48f)
  val th = maxOf(h, 48f)
  return Bounds(x - (tw - w) / 2, y - (th - h) / 2, tw, th)
}

/** A button that meets every rule: 40 dp visual, 48 dp touch. */
internal fun button(
  id: String,
  x: Float = 16f,
  y: Float = 100f,
  w: Float = 100f,
  h: Float = 40f,
  primary: Boolean = false,
  parent: String? = null,
  label: String = "Label",
) =
  node(
    id,
    "Button",
    x,
    y,
    w,
    h,
    parent = parent,
    primary = primary,
    touch = touchOf(x, y, w, h),
    text = text(label),
    typeRole = "labelLarge",
    content = "#FFFFFF",
    container = "#6750A4",
  )

internal fun evaluate(tree: LayoutTree): List<Finding> = FindingRules.evaluate(tree)

internal fun List<Finding>.errors(): List<Finding> = filter { it.severity == Severity.Error }

internal fun List<Finding>.of(rule: FindingRule): List<Finding> = filter { it.rule == rule }

internal fun List<Finding>.rules(): List<FindingRule> = map { it.rule }.distinct()
