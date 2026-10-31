package bot.charlies.androiddesign.preview.feature.preview.record

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsNode
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.text.TextLayoutResult
import bot.charlies.androiddesign.preview.domain.findings.FindingRules
import bot.charlies.androiddesign.preview.model.A11yInfo
import bot.charlies.androiddesign.preview.model.Bounds
import bot.charlies.androiddesign.preview.model.LayoutNode
import bot.charlies.androiddesign.preview.model.LayoutTree
import bot.charlies.androiddesign.preview.model.NodeColors
import bot.charlies.androiddesign.preview.model.NodeRoles
import bot.charlies.androiddesign.preview.model.TextInfo
import bot.charlies.androiddesign.preview.model.ViewSpec

/**
 * What the renderer knows about a component it drew, set when the component enters the composition.
 *
 * @property id The test tag and tree id. Components under a template carry the item indices
 *   (`plate_row[0]`), and components of a pane other than the one asked for carry `screen/`.
 * @property order The pre-order position: the tree lists nodes by this key.
 * @property text The text the node shows, when it shows any.
 * @property font The family name of its type role, when it shows text.
 */
class NodeMeta(
  val id: String,
  val type: String,
  val parent: String?,
  val order: List<Int>,
  val primary: Boolean,
  val interactive: Boolean,
  val roles: NodeRoles,
  val colors: NodeColors,
  val text: String?,
  val font: String?,
  val a11yRole: String?,
  val a11yLabel: String?,
  val actions: List<String>,
)

/** One `onTextLayout` report of a text the node shows. */
class TextReport(val lines: Int, val overflow: Boolean, val truncated: Boolean) {
  companion object {
    fun of(layout: TextLayoutResult) =
      TextReport(
        lines = layout.lineCount,
        overflow = layout.hasVisualOverflow,
        truncated = (0 until layout.lineCount).any { layout.isLineEllipsized(it) },
      )
  }
}

/**
 * The semantics nodes of a composition by test tag. A headless render has them (it owns the test
 * harness); a live window does not, and then the recorder falls back to its own measurements.
 */
class SemanticsIndex(
  private val merged: Map<String, SemanticsNode>,
  private val unmerged: Map<String, SemanticsNode>,
) {
  fun merged(tag: String): SemanticsNode? = merged[tag]

  fun unmerged(tag: String): SemanticsNode? = unmerged[tag]

  companion object {
    /** Indexes every root of the composition: dialogs and sheets are roots of their own. */
    fun of(mergedRoots: List<SemanticsNode>, unmergedRoots: List<SemanticsNode>) =
      SemanticsIndex(byTag(mergedRoots), byTag(unmergedRoots))

    private fun byTag(roots: List<SemanticsNode>): Map<String, SemanticsNode> {
      val found = linkedMapOf<String, SemanticsNode>()
      fun visit(node: SemanticsNode) {
        node.config.getOrNull(SemanticsProperties.TestTag)?.let { found.putIfAbsent(it, node) }
        node.children.forEach(::visit)
      }
      roots.forEach(::visit)
      return found
    }
  }
}

/**
 * Collects the layout tree while the renderer draws. Every proposal component registers itself
 * ([register]), reports where it landed ([reportPosition]) and, for text, how the text laid out
 * ([reportText]). [snapshot] turns that into a [LayoutTree] in dp, relative to the preview frame.
 */
class TreeRecorder {
  private val metas = HashMap<String, NodeMeta>()
  private val positions = HashMap<String, Rect>()
  private val texts = HashMap<String, MutableMap<String, TextReport>>()
  private var frame: LayoutCoordinates? = null

  /** Called by the preview frame: bounds are measured relative to this. */
  @Synchronized
  fun attachFrame(coordinates: LayoutCoordinates?) {
    frame = coordinates
  }

  @Synchronized
  fun register(meta: NodeMeta) {
    metas[meta.id] = meta
  }

  @Synchronized
  fun unregister(id: String) {
    metas.remove(id)
    positions.remove(id)
    texts.remove(id)
  }

  @Synchronized
  fun reportPosition(id: String, coordinates: LayoutCoordinates) {
    if (!coordinates.isAttached) return
    val frame = frame?.takeIf { it.isAttached }
    positions[id] =
      when {
        frame == null -> coordinates.boundsInRoot()
        else ->
          try {
            frame.localBoundingBoxOf(coordinates, clipBounds = false)
          } catch (_: IllegalArgumentException) {
            // Sheets, dialogs and menus live in a window layer of their own.
            coordinates.boundsInWindow().translate(-frame.positionInWindow())
          }
      }
  }

  /** [part] names which text of the node this is, such as `label` or `supporting`. */
  @Synchronized
  fun reportText(id: String, part: String, report: TextReport) {
    texts.getOrPut(id) { linkedMapOf() }[part] = report
  }

  /** Drops everything, e.g. when another screen replaces the one being drawn. */
  @Synchronized
  fun clear() {
    metas.clear()
    positions.clear()
    texts.clear()
  }

  /**
   * The tree as drawn now. [density] converts pixels to dp; [semantics] supplies visual bounds,
   * touch bounds and accessibility facts when the harness has them.
   */
  @Synchronized
  fun snapshot(
    screen: String,
    view: ViewSpec,
    density: Float,
    semantics: SemanticsIndex? = null,
  ): LayoutTree {
    val nodes =
      metas.values
        .sortedWith { a, b -> compareOrder(a.order, b.order) }
        .mapNotNull { meta -> node(meta, density, semantics) }
    val withoutFindings = LayoutTree(screen, view, nodes, emptyList())
    return withoutFindings.copy(findings = FindingRules.evaluate(withoutFindings))
  }

  private fun node(meta: NodeMeta, density: Float, semantics: SemanticsIndex?): LayoutNode? {
    val unmerged = semantics?.unmerged(meta.id)
    val merged = semantics?.merged(meta.id) ?: unmerged
    val layout = positions[meta.id]
    val bounds = unmerged?.boundsInRoot ?: layout ?: return null
    val touchPx =
      when {
        !meta.interactive -> null
        unmerged != null -> unmerged.touchBoundsInRoot
        else -> expandToMinimumTouch(bounds, density)
      }
    return LayoutNode(
      id = meta.id,
      type = meta.type,
      parent = meta.parent,
      primary = meta.primary,
      bounds = bounds.toDp(density),
      touch = touchPx?.toDp(density),
      text = textInfo(meta),
      roles = meta.roles,
      colors = meta.colors,
      a11y = a11y(meta, merged),
    )
  }

  private fun textInfo(meta: NodeMeta): TextInfo? {
    val value = meta.text ?: return null
    val reports = texts[meta.id]?.values.orEmpty()
    val primary = reports.firstOrNull()
    return TextInfo(
      value = value,
      lines = primary?.lines ?: 1,
      overflow = reports.any { it.overflow },
      truncated = reports.any { it.truncated },
      font = meta.font,
    )
  }

  private fun a11y(meta: NodeMeta, node: SemanticsNode?): A11yInfo? {
    if (node == null) {
      if (!meta.interactive && meta.a11yRole == null) return null
      return A11yInfo(meta.a11yRole, meta.a11yLabel, meta.actions)
    }
    val config = node.config
    val role = config.getOrNull(SemanticsProperties.Role)?.toString()
    val label =
      config.getOrNull(SemanticsProperties.ContentDescription)?.joinToString(" ")?.ifBlank { null }
        ?: config
          .getOrNull(SemanticsProperties.Text)
          ?.joinToString(" ") { it.text }
          ?.ifBlank { null }
    val actions = buildList {
      if (config.contains(SemanticsActions.OnClick)) add("click")
      if (config.contains(SemanticsActions.OnLongClick)) add("longClick")
      if (config.contains(SemanticsProperties.ToggleableState) && !contains("click")) add("toggle")
      if (config.contains(SemanticsActions.SetProgress)) add("setProgress")
      if (config.contains(SemanticsActions.SetText)) add("setText")
    }
    if (role == null && label == null && actions.isEmpty()) return null
    return A11yInfo(role ?: meta.a11yRole, label ?: meta.a11yLabel, actions)
  }

  private fun expandToMinimumTouch(bounds: Rect, density: Float): Rect {
    val min = MIN_TOUCH_DP * density
    val dx = ((min - bounds.width) / 2f).coerceAtLeast(0f)
    val dy = ((min - bounds.height) / 2f).coerceAtLeast(0f)
    return Rect(bounds.left - dx, bounds.top - dy, bounds.right + dx, bounds.bottom + dy)
  }

  private fun Rect.toDp(density: Float) =
    Bounds(left / density, top / density, width / density, height / density)

  private fun compareOrder(a: List<Int>, b: List<Int>): Int {
    for (i in 0 until minOf(a.size, b.size)) {
      if (a[i] != b[i]) return a[i].compareTo(b[i])
    }
    return a.size.compareTo(b.size)
  }

  companion object {
    const val MIN_TOUCH_DP = 48f
  }
}
