package bot.charlies.androiddesign.preview.domain.findings

import bot.charlies.androiddesign.preview.domain.design.TypeScale
import bot.charlies.androiddesign.preview.model.BundledFont
import bot.charlies.androiddesign.preview.model.Catalog
import bot.charlies.androiddesign.preview.model.Catalogs
import bot.charlies.androiddesign.preview.model.ComponentGroup
import bot.charlies.androiddesign.preview.model.Finding
import bot.charlies.androiddesign.preview.model.FindingRule
import bot.charlies.androiddesign.preview.model.LayoutNode
import bot.charlies.androiddesign.preview.model.LayoutTree
import bot.charlies.androiddesign.preview.model.M3ExpressiveCatalog
import bot.charlies.androiddesign.preview.model.Severity
import bot.charlies.androiddesign.preview.model.SizeClass
import bot.charlies.androiddesign.preview.model.TypeGroup
import kotlin.math.hypot
import kotlin.math.round

/**
 * The checks a rendered view must pass, run over its layout tree: touch targets, fonts, clipped or
 * truncated text, contrast, a primary action at least as tall as its rivals, one FAB per pane,
 * content past the view's edges, a compact carousel with too many text items, and a navigation bar
 * with fewer than three destinations. [tree] arrives with its nodes and no findings yet. Findings
 * come out in rule order, then in document order.
 *
 * A node with no area is not laid out (a closed overlay is never composed), so the rules that
 * measure geometry skip it. While a modal sheet or dialog is open, everything outside it is inert
 * behind the scrim: it is exempt from the touch target and contrast rules, and the primary action
 * check is skipped.
 */
object FindingRules {
  /** Interactive controls are measured against this touch area, unless the catalog sets another. */
  const val MIN_TARGET_DP = 48f

  /** Rounding slack for the touch target. */
  private const val TARGET_SLACK_DP = 0.05f

  /** The primary may be this much shorter than the tallest action. */
  private const val PRIMARY_SLACK_DP = 1f

  /** Slack on the view edges. */
  private const val EDGE_SLACK_DP = 1f

  private const val MAX_OFFSCREEN = 5

  internal val fabTypes = M3ExpressiveCatalog.fabNames
  internal val modalTypes = M3ExpressiveCatalog.modalNames

  /** The component sets the rules read, derived once per catalog. */
  private class Facets(val catalog: Catalog) {
    /**
     * Components that compete with the primary: buttons, icon buttons, FABs, split buttons, chips.
     */
    val actionTypes: Set<String> =
      catalog.components
        .filter {
          it.interactive && it.group in setOf(ComponentGroup.Actions, ComponentGroup.Chips)
        }
        .map { it.name }
        .toSet() - "FabMenuItem"

    val interactiveTypes: Set<String> =
      catalog.components.filter { it.interactive }.map { it.name }.toSet()

    /**
     * Controls whose label must stay on one line (Google: "Don't truncate or wrap label text"):
     * interactive components with a `label` in the action, chip, navigation and overlay groups.
     * Text fields and checkboxes keep ordinary text behavior.
     */
    val labelTypes: Set<String> =
      catalog.components
        .filter {
          it.interactive &&
            it.group in
              setOf(
                ComponentGroup.Actions,
                ComponentGroup.Chips,
                ComponentGroup.Structure,
                ComponentGroup.Overlays,
              ) &&
            it.prop("label") != null
        }
        .map { it.name }
        .toSet()
  }

  private val facets: Map<String, Facets> by lazy {
    Catalogs.all.associate { it.id to Facets(it) }
  }

  private val allGroups = TypeGroup.entries.toSet()

  /** Bundled families (lowercase) and the type groups each may set. */
  private val fontGroups: Map<String, Set<TypeGroup>> =
    BundledFont.entries.associate { it.family.lowercase() to it.groups }

  /** The findings of [tree], under the rules of [catalog], which is its view's by default. */
  fun evaluate(tree: LayoutTree, catalog: Catalog = Catalogs.forView(tree.view)): List<Finding> {
    val view = View(tree, checkNotNull(facets[catalog.id]) { "unknown catalog ${catalog.id}" })
    return touchTargets(view) +
      fonts(view) +
      clippedText(view) +
      truncatedText(view) +
      truncatedLabels(view) +
      contrast(view) +
      primaryLargest(view) +
      oneFab(view) +
      offscreen(view) +
      roundEdgeClipped(view) +
      carouselItems(view) +
      navDestinations(view)
  }

  // region the view

  private class View(val tree: LayoutTree, val facets: Facets) {
    val catalog: Catalog = facets.catalog
    val nodes: List<LayoutNode> = tree.nodes
    private val byId: Map<String, LayoutNode> = buildMap {
      nodes.forEach { putIfAbsent(it.id, it) }
    }

    /** Open modal surfaces: a sheet or dialog node that takes up space. */
    private val modals: Set<String> =
      nodes.filter { it.type in catalog.modalNames && it.isLaidOut }.map { it.id }.toSet()

    val modalOpen: Boolean
      get() = modals.isNotEmpty()

    /** The parents of [node], nearest first. Stops on a cycle or a missing parent. */
    fun ancestors(node: LayoutNode): Sequence<LayoutNode> = sequence {
      val seen = mutableSetOf(node.id)
      var parent = node.parent?.let(byId::get)
      while (parent != null && seen.add(parent.id)) {
        yield(parent)
        parent = parent.parent?.let(byId::get)
      }
    }

    /** True for a node outside every open modal: inert behind its scrim. */
    fun isBehindModal(node: LayoutNode): Boolean =
      modalOpen && node.id !in modals && ancestors(node).none { it.id in modals }
  }

  // endregion

  // region touch-target

  private fun touchTargets(view: View): List<Finding> =
    view.nodes
      .filter { it.type in view.facets.interactiveTypes && it.isLaidOut && !view.isBehindModal(it) }
      .mapNotNull { node ->
        val width = maxOf(node.bounds.width, node.touch?.width ?: 0f)
        val height = maxOf(node.bounds.height, node.touch?.height ?: 0f)
        val min = view.catalog.minTouchTargetDp
        if (width < min - TARGET_SLACK_DP || height < min - TARGET_SLACK_DP) {
          errorFinding(
            node,
            FindingRule.TouchTarget,
            "${dp(width)} x ${dp(height)} dp, needs ${dp(min)} x ${dp(min)}",
          )
        } else {
          null
        }
      }

  // endregion

  // region font

  private fun fonts(view: View): List<Finding> =
    view.nodes.mapNotNull { node ->
      val family = node.text?.font ?: return@mapNotNull null
      val name = displayName(family).lowercase()
      val role = node.roles?.type
      val group = role?.let(view.catalog.typeRoles::get)
      val allowed =
        fontGroups[name]
          ?: allGroups.takeIf { view.catalog.platformFontFamilies.any { it.lowercase() == name } }
      val detail =
        when {
          allowed == null -> "${displayName(family)} is not a bundled font"
          group == null && allowed.size != allGroups.size ->
            "${displayName(family)} is for ${groupsLabel(allowed)} text, not ${role ?: "plain text"}"
          group != null && group !in allowed ->
            "${displayName(family)} is for ${groupsLabel(allowed)} text, not $role"
          else -> null
        }
      detail?.let { errorFinding(node, FindingRule.Font, it) }
    }

  private fun displayName(family: String): String =
    family.substringBefore(',').trim().trim('"', '\'').trim()

  private fun groupsLabel(groups: Set<TypeGroup>): String =
    groups.joinToString(" and ") { it.name.lowercase() }

  // endregion

  // region text-clipped, text-truncated, label-truncated

  /** Overflow that no ellipsis excuses: glyphs cut off with nothing to say so. */
  private fun clippedText(view: View): List<Finding> =
    view.nodes
      .filter { it.text?.let { text -> text.overflow && !text.truncated } == true }
      .map { errorFinding(it, FindingRule.TextClipped, "text is cut off with no ellipsis") }

  private fun truncatedText(view: View): List<Finding> =
    view.nodes
      .filter { it.text?.truncated == true && it.type !in view.facets.labelTypes }
      .map { node ->
        val lines = node.text?.lines ?: 1
        Finding(
          id = node.id,
          rule = FindingRule.TextTruncated,
          severity = Severity.Info,
          detail = "$lines ${if (lines == 1) "line" else "lines"}, ellipsized",
        )
      }

  private fun truncatedLabels(view: View): List<Finding> =
    view.nodes
      .filter { it.type in view.facets.labelTypes }
      .mapNotNull { node ->
        val text = node.text ?: return@mapNotNull null
        val detail =
          when {
            text.truncated -> "label is cut off with an ellipsis"
            text.lines > 1 -> "label wraps to ${text.lines} lines"
            else -> null
          }
        detail?.let { errorFinding(node, FindingRule.LabelTruncated, it) }
      }

  // endregion

  // region contrast

  private fun contrast(view: View): List<Finding> =
    view.nodes
      .filter { it.text != null && !view.isBehindModal(it) }
      .mapNotNull { node ->
        val content = node.colors?.content ?: return@mapNotNull null
        val backdrop =
          node.colors?.container
            ?: view.ancestors(node).firstNotNullOfOrNull { it.colors?.container }
            ?: return@mapNotNull null
        val ratio = Contrast.ratio(content, backdrop) ?: return@mapNotNull null
        val spec = node.roles?.type?.let { TypeScale.of(view.catalog, it) }
        val need =
          if (spec == null) Contrast.NORMAL_RATIO
          else Contrast.requiredRatio(spec.sizeSp, spec.weight >= BOLD_WEIGHT)
        if (ratio < need) {
          errorFinding(node, FindingRule.Contrast, "${number(ratio)}:1, needs ${number(need)}:1")
        } else {
          null
        }
      }

  private const val BOLD_WEIGHT = 700

  // endregion

  // region primary-largest, per pane

  private fun primaryLargest(view: View): List<Finding> {
    if (view.modalOpen) return emptyList()
    val actions =
      view.nodes.filter { node ->
        node.type in view.facets.actionTypes &&
          node.isLaidOut &&
          // Navigation chrome is not compared with the primary action.
          view.ancestors(node).none { it.type in view.catalog.navigationNames }
      }
    val largestByPane =
      actions.groupBy(::pane).mapValues { (_, group) -> group.maxBy { it.bounds.height } }
    return view.nodes
      .filter { it.primary && it.isLaidOut }
      .mapNotNull { primary ->
        val largest = largestByPane[pane(primary)] ?: return@mapNotNull null
        if (primary.bounds.height >= largest.bounds.height - PRIMARY_SLACK_DP) {
          null
        } else {
          errorFinding(
            primary,
            FindingRule.PrimaryLargest,
            "${dp(primary.bounds.height)} dp tall, shorter than ${largest.id} (${dp(largest.bounds.height)} dp)",
          )
        }
      }
  }

  /**
   * The pane a node belongs to: the screen named by its `<screen>/` id prefix, or the empty string
   * for the requested screen, whose ids are plain. A list-detail view draws more than one screen,
   * and each keeps its own primary action and floating action button.
   */
  private fun pane(node: LayoutNode): String = node.id.substringBefore('/', "")

  // endregion

  // region one-fab, per pane

  private fun oneFab(view: View): List<Finding> =
    view.nodes
      .filter { it.type in view.catalog.fabNames && it.isLaidOut }
      .groupBy(::pane)
      .values
      .filter { it.size > 1 }
      .flatMap { fabs ->
        val detail =
          "${fabs.size} floating action buttons (${fabs.joinToString { it.id }}), keep one"
        fabs.drop(1).map { errorFinding(it, FindingRule.OneFab, detail) }
      }

  // endregion

  // region offscreen

  /**
   * Content wider than the view: Android has no horizontal page scroll. A node is flagged when it
   * ends past the right edge or starts before the left edge while still reaching into the view. A
   * node entirely off the left edge is hidden on purpose. A child of a flagged node is left out, so
   * the cause is named before its children, and at most five are reported.
   */
  private fun offscreen(view: View): List<Finding> {
    val width = view.tree.view.width.toFloat()
    val flagged =
      view.nodes.filter { node ->
        node.isLaidOut &&
          (node.right() > width + EDGE_SLACK_DP ||
            (node.bounds.x < -EDGE_SLACK_DP && node.right() > EDGE_SLACK_DP))
      }
    val flaggedIds = flagged.map { it.id }.toSet()
    return flagged
      .filter { node -> node.parent == null || node.parent !in flaggedIds }
      .take(MAX_OFFSCREEN)
      .map { node ->
        val detail =
          if (node.right() > width + EDGE_SLACK_DP) {
            "ends at ${dp(node.right())} dp, the view is ${dp(width)} dp wide"
          } else {
            "starts at ${dp(node.bounds.x)} dp, before the view"
          }
        errorFinding(node, FindingRule.Offscreen, detail)
      }
  }

  private fun LayoutNode.right(): Float = bounds.x + bounds.width

  // endregion

  // region round-edge-clipped

  /**
   * On a round view, a control or a text whose bounds reach past the circle inscribed in the view:
   * the display cuts off what sticks out. A scrolling list is exempt (its items scroll clear, and
   * Wear's list transforms them at the edge), found by a `scrolls` ancestor. So are the components
   * the catalog makes for the edge, and anything behind an open modal. The worst corner is
   * reported.
   */
  private fun roundEdgeClipped(view: View): List<Finding> {
    if (!view.tree.view.round) return emptyList()
    val width = view.tree.view.width.toFloat()
    val height = view.tree.view.height.toFloat()
    val centerX = width / 2
    val centerY = height / 2
    val radius = minOf(width, height) / 2
    return view.nodes
      .filter { node ->
        node.isLaidOut &&
          (node.type in view.facets.actionTypes || node.text != null) &&
          node.type !in view.catalog.roundEdgeNames &&
          !view.isBehindModal(node) &&
          view.ancestors(node).none { it.scrolls }
      }
      .mapNotNull { node ->
        // A text is judged by the lines it drew; a control by its bounds.
        val b = node.text?.ink?.takeIf { node.type !in view.facets.actionTypes } ?: node.bounds
        val right = b.x + b.width
        val reach =
          listOf(b.x to b.y, right to b.y, b.x to b.y + b.height, right to b.y + b.height).maxOf {
            (x, y) ->
            hypot(x - centerX, y - centerY)
          }
        val outside = reach - radius
        if (outside > EDGE_SLACK_DP) {
          errorFinding(
            node,
            FindingRule.RoundEdgeClipped,
            "reaches ${dp(outside)} dp outside the round edge",
          )
        } else {
          null
        }
      }
  }

  // endregion

  // region carousel-items

  /**
   * Material: "At compact breakpoints, only show more than three items if the items are easy to
   * understand and recognize." A carousel at a compact view is flagged when more than three of its
   * items that hold text show inside the view. That is a caution in Material, so it is info.
   *
   * The carousel keeps every item it composes laid out, and the ones past the last visible item sit
   * inside that item's bounds, so an item that lies within an earlier item's bounds is hidden and
   * not counted. A centered hero shows one more small item than this counts, which cannot reach
   * four.
   */
  private fun carouselItems(view: View): List<Finding> {
    val width = view.tree.view.width.toFloat()
    if (width >= SizeClass.MEDIUM_MIN_DP) return emptyList()
    val children = view.nodes.filter { it.parent != null }.groupBy { it.parent }
    return view.nodes
      .filter { it.type == CAROUSEL && it.isLaidOut }
      .mapNotNull { carousel ->
        val inView =
          children[carousel.id].orEmpty().filter { item ->
            item.isLaidOut && item.bounds.x < width - EDGE_SLACK_DP && item.right() > EDGE_SLACK_DP
          }
        val shown = inView.filterIndexed { i, item ->
          holdsText(item, children) && inView.take(i).none { it.contains(item) }
        }
        if (shown.size > MAX_COMPACT_CAROUSEL_ITEMS) {
          Finding(
            id = carousel.id,
            rule = FindingRule.CarouselItems,
            severity = Severity.Info,
            detail =
              "${shown.size} items with text show at ${dp(width)} dp; Material shows at most " +
                "$MAX_COMPACT_CAROUSEL_ITEMS at compact widths unless they are easy to recognize",
          )
        } else {
          null
        }
      }
  }

  /** Whether [other] lies inside this node's bounds. */
  private fun LayoutNode.contains(other: LayoutNode): Boolean =
    other.bounds.x >= bounds.x - EDGE_SLACK_DP &&
      other.right() <= right() + EDGE_SLACK_DP &&
      other.bounds.y >= bounds.y - EDGE_SLACK_DP &&
      other.bounds.y + other.bounds.height <= bounds.y + bounds.height + EDGE_SLACK_DP

  /** Whether [node] or anything under it draws text. */
  private fun holdsText(node: LayoutNode, children: Map<String?, List<LayoutNode>>): Boolean =
    node.text != null || children[node.id].orEmpty().any { holdsText(it, children) }

  private const val CAROUSEL = "Carousel"
  private const val MAX_COMPACT_CAROUSEL_ITEMS = 3

  // endregion

  // region nav-destinations

  /**
   * Material: "Don't use a navigation bar for fewer than three destinations. Instead, use tabs."
   */
  private fun navDestinations(view: View): List<Finding> {
    val children = view.nodes.filter { it.parent != null }.groupBy { it.parent }
    return view.nodes
      .filter { it.type == NAVIGATION_BAR && it.isLaidOut }
      .mapNotNull { bar ->
        val destinations = children[bar.id].orEmpty().count { it.type == NAVIGATION_BAR_ITEM }
        if (destinations < MIN_DESTINATIONS) {
          errorFinding(
            bar,
            FindingRule.NavDestinations,
            "$destinations ${if (destinations == 1) "destination" else "destinations"}; " +
              "a navigation bar needs at least $MIN_DESTINATIONS, use tabs for fewer",
          )
        } else {
          null
        }
      }
  }

  private const val NAVIGATION_BAR = "NavigationBar"
  private const val NAVIGATION_BAR_ITEM = "NavigationBarItem"
  private const val MIN_DESTINATIONS = 3

  // endregion

  private fun errorFinding(node: LayoutNode, rule: FindingRule, detail: String) =
    Finding(id = node.id, rule = rule, severity = Severity.Error, detail = detail)

  /** Whole numbers without a decimal point, others to one place. */
  private fun dp(value: Float): String = number(value.toDouble(), places = 1)

  private fun number(value: Double, places: Int = 2): String {
    val scale = Math.pow(10.0, places.toDouble())
    val rounded = round(value * scale) / scale
    return if (rounded == round(rounded)) rounded.toLong().toString() else rounded.toString()
  }
}
