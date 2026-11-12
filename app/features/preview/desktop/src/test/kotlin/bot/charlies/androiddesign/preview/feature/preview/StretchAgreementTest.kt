package bot.charlies.androiddesign.preview.feature.preview

import bot.charlies.androiddesign.preview.domain.export.KotlinExporter
import bot.charlies.androiddesign.preview.feature.preview.headless.HeadlessRenderer
import bot.charlies.androiddesign.preview.feature.preview.headless.RenderSource
import bot.charlies.androiddesign.preview.model.ComponentDef
import bot.charlies.androiddesign.preview.model.M3ExpressiveCatalog
import bot.charlies.androiddesign.preview.model.PropKind
import bot.charlies.androiddesign.preview.model.Proposal
import bot.charlies.androiddesign.preview.model.Views
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/**
 * The previewer and the exporter stretch the same components. Both read the catalog (`width` and
 * `stretchesChildren`), and this checks that the rule they read holds: a component is as wide as
 * its parent in the render exactly when the export gives it `Modifier.fillMaxWidth()`.
 *
 * Each case is drawn twice. In the first the subject sits in a Column that fills the screen; in the
 * second it sits in a Shape, which never stretches its child, so the subject's own `fillMaxWidth()`
 * is what the first export has more of, plus each descendant the catalog says stretches in one case
 * and not the other (a container that fills also stretches its children, which the wrapped copy in
 * the Shape does not). The count is exact, worked out from the catalog and the case, not from the
 * export.
 */
class StretchAgreementTest {
  /**
   * Components that only make sense inside another one (a Tab is as wide as its row gives it), or
   * open on an action.
   */
  private val skipped =
    M3ExpressiveCatalog.overlayNames +
      setOf(
        "Scaffold",
        "Tab",
        "FabMenuItem",
        "MenuItem",
        "MenuGroup",
      )

  /** Props that change whether a component stretches, besides its required ones. */
  private val variants: Map<String, List<Map<String, JsonElement>>> =
    mapOf(
      "Text" to listOf(mapOf("align" to JsonPrimitive("center"))),
      "Image" to listOf(mapOf("aspectRatio" to JsonPrimitive(1.5))),
      "Divider" to listOf(mapOf("vertical" to JsonPrimitive(true))),
      "ProgressIndicator" to listOf(mapOf("shape" to JsonPrimitive("circular"))),
    )

  /**
   * A one pixel image. The previewer draws a placeholder for a missing file that is as wide as its
   * parent whatever the proposal says, which is not the rule under test.
   */
  private val pixel =
    JsonPrimitive(
      "data:image/png;base64," +
        "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8z8BQDwAEhQGAhKmMIQAAAABJRU5ErkJggg==",
    )

  private val text = buildJsonObject {
    put("id", "t1")
    put("component", "Text")
    put("text", "Hi")
  }

  private fun node(id: String, component: String, vararg props: Pair<String, JsonElement>) =
    JsonObject(
      mapOf("id" to JsonPrimitive(id), "component" to JsonPrimitive(component)) + props.toMap(),
    )

  private fun ids(vararg ids: String) = JsonArray(ids.map(::JsonPrimitive))

  private fun screenWith(dataModel: String? = null, vararg nodes: JsonObject): Proposal =
    singleScreen(nodes.joinToString(","), dataModel)

  private fun treeOf(proposal: Proposal) =
    HeadlessRenderer.tree(
      RenderSource(proposal, designOf(proposal.theme), null),
      "home",
      Views.Light,
    )

  private fun fillMaxWidths(proposal: Proposal): Int {
    val source = KotlinExporter.export(proposal, designOf(proposal.theme), "home")
    val body = source.substringAfter("fun HomeScreen(").substringBefore("@Preview")
    return Regex("""fillMaxWidth\(\)""").findAll(body).count()
  }

  /** A proposal whose Scaffold content is a Column `stage` holding [stageChild]. */
  private fun staged(stageChild: String, vararg rest: JsonObject): Proposal =
    screenWith(
      nodes =
        arrayOf(
          node("root", "Scaffold", "content" to JsonPrimitive("stage")),
          node("stage", "Column", "children" to ids(stageChild)),
          *rest,
          text,
        ),
    )

  /** The width of a node and of the `stage` column that holds it, from one render. */
  private class Widths(val node: Float, val stage: Float) {
    val stretches: Boolean
      get() = abs(node - stage) < 0.5f

    override fun toString() = "$node of $stage dp"
  }

  private fun widthsIn(proposal: Proposal, id: String): Widths {
    val tree = treeOf(proposal)
    return Widths(tree.node(id).bounds.width, tree.node("stage").bounds.width)
  }

  @Test
  fun `a component stretches in the export exactly when it does in the previewer`() {
    val disagree = mutableListOf<String>()
    var stretching = 0
    var wrapping = 0
    M3ExpressiveCatalog.components
      .filter { it.name !in skipped }
      .forEach { def ->
        val cases = listOf(emptyMap<String, JsonElement>()) + variants[def.name].orEmpty()
        cases.forEach { stated ->
          val extra = mutableListOf<JsonObject>()
          val fixed = if (def.name == "Image") mapOf("src" to pixel) else emptyMap()
          val subject = JsonObject(requiredNode(def, "subject", extra) + fixed + stated)
          val inColumn = staged("subject", subject, *extra.toTypedArray())
          val inShape =
            staged(
              "wrapper",
              node(
                "wrapper",
                "Shape",
                "shape" to JsonPrimitive("circle"),
                "content" to JsonPrimitive("subject"),
              ),
              subject,
              *extra.toTypedArray(),
            )
          val widths = widthsIn(inColumn, "subject")
          val rendered = widths.stretches
          // The Shape's copy holds the same subject wrapped, so the exports differ by the
          // subject's own `fillMaxWidth()` and by every descendant the catalog stretches in one
          // case and not the other.
          val nodes = (listOf(subject, text) + extra).associateBy { it.id() }
          val ownFill = if (def.fillsWidth { subject[it]?.takeIf { v -> v !is JsonNull } }) 1 else 0
          val childrenDelta =
            stretchedBelow("subject", nodes, parentStretches = true) -
              stretchedBelow("subject", nodes, parentStretches = false) -
              ownFill
          val gain = fillMaxWidths(inColumn) - fillMaxWidths(inShape)
          val own = gain - childrenDelta
          val exported = own == 1
          if (own !in 0..1) {
            disagree +=
              "${def.name} $stated: export gains $gain, the catalog says $childrenDelta (children) + 0 or 1 (itself)"
          }
          if (rendered) stretching++ else wrapping++
          if (rendered != exported) {
            disagree +=
              "${def.name} $stated: previewer stretches=$rendered ($widths), export stretches=$exported"
          }
        }
      }
    assertEquals(emptyList(), disagree)
    assertTrue(stretching >= 10 && wrapping >= 10, "stretching=$stretching wrapping=$wrapping")
  }

  private fun JsonObject.id() = (getValue("id") as JsonPrimitive).content

  /**
   * How many nodes at or below [id] stretch to their parent's width, from the catalog alone: a node
   * fills when its parent stretches its children and its `width` rule says so, and it stretches its
   * own children unless it wraps ([ComponentDef.wrapsAt]). [parentStretches] is whether the parent
   * of [id] stretches its children.
   */
  private fun stretchedBelow(
    id: String,
    nodes: Map<String, JsonObject>,
    parentStretches: Boolean,
  ): Int {
    val node = nodes.getValue(id)
    val def =
      checkNotNull(M3ExpressiveCatalog.find((node.getValue("component") as JsonPrimitive).content))
    val valueOf = { prop: String -> node[prop]?.takeIf { it !is JsonNull } }
    val fills = parentStretches && def.fillsWidth(valueOf)
    val childrenStretch = def.stretchesChildrenWhen(def.wrapsAt(parentStretches, false, valueOf))
    val childIds =
      def.props
        .filter { it.kind == PropKind.Child || it.kind == PropKind.ChildList }
        .flatMap { prop ->
          when (val value = node[prop.name]) {
            is JsonArray -> value.map { (it as JsonPrimitive).content }
            is JsonPrimitive -> listOf(value.content)
            else -> emptyList()
          }
        }
    return (if (fills) 1 else 0) + childIds.sumOf { stretchedBelow(it, nodes, childrenStretch) }
  }

  /** Components that hold any child, so a progress bar can stand in for the child. */
  private val parents: List<ComponentDef> =
    M3ExpressiveCatalog.components
      .filter { it.name !in skipped }
      // A carousel gives each child the width of an item, not of the carousel.
      .filter { it.name != "Carousel" }
      .filter { def ->
        val extra = mutableListOf<JsonObject>()
        val node = requiredNode(def, "parent", extra)
        extra.isEmpty() &&
          def.props.any { it.required && node[it.name].toString().contains("\"t1\"") }
      }

  @Test
  fun `a parent stretches its children in the export exactly when it does in the previewer`() {
    // A linear progress bar is 240 dp wide by itself; a Slider or TextField fills any width.
    val bar = node("t1", "ProgressIndicator", "progress" to JsonPrimitive(0.5))
    val disagree = mutableListOf<String>()
    val stretching = mutableListOf<String>()
    parents.forEach { def ->
      val parent = requiredNode(def, "parent", mutableListOf())
      fun withChild(child: JsonObject) =
        screenWith(
          nodes =
            arrayOf(
              node("root", "Scaffold", "content" to JsonPrimitive("stage")),
              node("stage", "Column", "children" to ids("parent")),
              parent,
              child,
            ),
        )
      val probed = withChild(bar)
      val control = withChild(text)
      val widths = widthsIn(probed, "t1")
      val rendered = widths.stretches
      val exported = fillMaxWidths(probed) - fillMaxWidths(control) == 1
      if (rendered) stretching += def.name
      if (rendered != exported) {
        disagree +=
          "${def.name}: previewer stretches=$rendered ($widths), export stretches=$exported"
      }
    }
    assertEquals(emptyList(), disagree)
    assertTrue("Column" in stretching && "Row" !in stretching, "stretching=$stretching")
  }

  @Test
  fun `a prop bound to the data model stretches in the export exactly when it does in the previewer`() {
    // A vertical divider keeps its width; a horizontal one stretches.
    listOf(true, false).forEach { vertical ->
      val proposal =
        screenWith(
          dataModel = """{"vertical":$vertical}""",
          node("root", "Scaffold", "content" to JsonPrimitive("stage")),
          node("stage", "Column", "children" to ids("subject")),
          node(
            "subject",
            "Divider",
            "vertical" to JsonObject(mapOf("@path" to JsonPrimitive("/vertical"))),
          ),
        )
      val rendered = widthsIn(proposal, "subject").stretches
      // The stage Column fills the Scaffold's content with `fillMaxSize()`, so a
      // `fillMaxWidth()` in the export is the divider's.
      val exported = fillMaxWidths(proposal) == 1
      assertEquals(!vertical, rendered, "previewer, vertical=$vertical")
      assertEquals(rendered, exported, "export, vertical=$vertical")
    }
  }
}
