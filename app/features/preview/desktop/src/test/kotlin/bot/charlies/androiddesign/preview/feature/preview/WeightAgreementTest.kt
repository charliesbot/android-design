package bot.charlies.androiddesign.preview.feature.preview

import bot.charlies.androiddesign.preview.data.ProposalCodec
import bot.charlies.androiddesign.preview.data.ProposalParse
import bot.charlies.androiddesign.preview.domain.design.DesignSystemResolver
import bot.charlies.androiddesign.preview.domain.export.KotlinExporter
import bot.charlies.androiddesign.preview.domain.export.wear.WearColors
import bot.charlies.androiddesign.preview.feature.preview.headless.HeadlessRenderer
import bot.charlies.androiddesign.preview.feature.preview.headless.RenderSource
import bot.charlies.androiddesign.preview.feature.preview.theme.wear.WearThemeResolver
import bot.charlies.androiddesign.preview.model.Catalog
import bot.charlies.androiddesign.preview.model.ComponentDef
import bot.charlies.androiddesign.preview.model.LayoutScope
import bot.charlies.androiddesign.preview.model.LayoutTree
import bot.charlies.androiddesign.preview.model.M3ExpressiveCatalog
import bot.charlies.androiddesign.preview.model.PropDef
import bot.charlies.androiddesign.preview.model.PropKind
import bot.charlies.androiddesign.preview.model.Proposal
import bot.charlies.androiddesign.preview.model.ViewSpec
import bot.charlies.androiddesign.preview.model.Views
import bot.charlies.androiddesign.preview.model.WearCatalog
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/**
 * `weight` applies where the Compose lambda that holds a child gives it a `RowScope` or a
 * `ColumnScope`. The previewer and the exporter both read where that is from the catalog (a child
 * slot's `childScope`), and this checks that what they do with it agrees: in each child slot of
 * each catalog, a child with a weight is drawn weighted exactly when the export gives it
 * `Modifier.weight(...)`, and along the axis the catalog names.
 *
 * "Drawn weighted" is measured. The same child is rendered twice, with and without a weight, and a
 * weight shows as a change in its width (a row's main axis) or its height (a column's).
 */
class WeightAgreementTest {
  /** A slot of a component that holds other components, and how to build a screen around it. */
  private class Slot(val def: ComponentDef, val prop: PropDef, val variant: Pair<String, String>?) {
    override fun toString() =
      "${def.name}.${prop.name}" + (variant?.let { " (${it.first} ${it.second})" } ?: "")
  }

  /** Props that change which scope a slot's lambda has, besides the slot itself. */
  private val variants: Map<String, List<Pair<String, String>>> =
    mapOf(
      "FloatingToolbar.children" to listOf("orientation" to "vertical"),
      "TopAppBar.actions" to listOf("size" to "search"),
      "Dialog.content" to listOf("variant" to "full-screen"),
      "Tooltip.actions" to listOf("variant" to "rich"),
    )

  /** Slots that draw nothing unless the variant is set. */
  private val onlyWithVariant = setOf("Dialog.content", "Tooltip.actions")

  /** Slots that point at an overlay another component opens: it is drawn by its own parent. */
  private fun PropDef.pointsAtOverlay(catalog: Catalog) =
    allowedChildren?.all { catalog.find(it)?.overlay == true } == true

  private fun slotsOf(catalog: Catalog): List<Slot> =
    catalog.components.flatMap { def ->
      def.props
        .filter { it.kind == PropKind.Child || it.kind == PropKind.ChildList }
        .filterNot { it.pointsAtOverlay(catalog) }
        .flatMap { prop ->
          val key = "${def.name}.${prop.name}"
          val extra = variants[key].orEmpty()
          (if (key in onlyWithVariant) emptyList() else listOf(Slot(def, prop, null))) +
            extra.map { Slot(def, prop, it) }
        }
    }

  private fun JsonObject.with(vararg props: Pair<String, JsonElement>) =
    JsonObject(this + props.toMap())

  private fun text(id: String) = buildJsonObject {
    put("id", id)
    put("component", "Text")
    put("text", id)
  }

  /** The components of a screen holding [slot], its child `w` weighted by [weight] when given. */
  private fun components(catalog: Catalog, slot: Slot, weight: Double?): List<JsonObject> {
    val extra = mutableListOf<JsonObject>()
    val leafDef = slot.prop.allowedChildren?.minOrNull()?.let { checkNotNull(catalog.find(it)) }
    fun leaf(id: String) =
      if (leafDef == null) text(id) else requiredNode(leafDef, id, extra, catalog)
    val weighted =
      leaf("w").let { if (weight == null) it else it.with("weight" to JsonPrimitive(weight)) }
    val sibling = leaf("s")
    val value =
      if (slot.prop.kind == PropKind.ChildList) JsonArray(listOf("s", "w").map(::JsonPrimitive))
      else JsonPrimitive("w")
    val isRoot = slot.def.name == "Scaffold" || slot.def.name == "Screen"
    val hostId = if (isRoot) "root" else "h"
    var host = requiredNode(slot.def, hostId, extra, catalog).with(slot.prop.name to value)
    slot.variant?.let { (key, v) -> host = host.with(key to JsonPrimitive(v)) }
    if (slot.def.name == "Menu") host = host.with("anchor" to JsonPrimitive("t1"))
    val anchors = mutableListOf<String>()
    if (slot.def.name == "SearchView") {
      anchors += "bar"
      extra += buildJsonObject {
        put("id", "bar")
        put("component", "SearchBar")
        put("placeholder", "Search")
        put("expanded", "h")
      }
    }
    val stage =
      if (catalog === WearCatalog) {
        buildJsonObject {
          put("id", "root")
          put("component", "Screen")
          // A scrolling list measures its rows without a bound, where a weight collapses.
          put("scroll", false)
          put("children", JsonArray((listOf("h", "t1") + anchors).map(::JsonPrimitive)))
        }
      } else {
        buildJsonObject {
          put("id", "stage")
          put("component", "Column")
          put("children", JsonArray((listOf("h", "t1") + anchors).map(::JsonPrimitive)))
        }
      }
    val frame =
      when {
        isRoot -> emptyList()
        catalog === WearCatalog -> listOf(stage)
        else ->
          listOf(
            buildJsonObject {
              put("id", "root")
              put("component", "Scaffold")
              put("content", "stage")
            },
            stage,
          )
      }
    return frame + host + extra + weighted + sibling + text("t1")
  }

  private fun proposalOf(catalog: Catalog, nodes: List<JsonObject>): Proposal {
    val json =
      """{"format":1,"name":"Test","navigation":{"start":"home"},"screens":{"home":""" +
        """{"catalogId":"${catalog.id}","components":${JsonArray(nodes)}}}}"""
    return assertIs<ProposalParse.Parsed>(ProposalCodec.parse(json)).proposal
  }

  private fun view(catalog: Catalog): ViewSpec =
    if (catalog === WearCatalog) Views.Watch else Views.Light

  private fun treeOf(catalog: Catalog, slot: Slot, weight: Double?): LayoutTree {
    val proposal = proposalOf(catalog, components(catalog, slot, weight))
    val design = DesignSystemResolver.resolve(null, proposal.theme, catalog)
    return HeadlessRenderer.tree(
      RenderSource(proposal, design, null),
      "home",
      view(catalog),
      overlay = if (slot.def.overlay) "h" else null,
    )
  }

  private fun exportedWeight(catalog: Catalog, slot: Slot): Boolean {
    val proposal = proposalOf(catalog, components(catalog, slot, WEIGHT))
    val design = DesignSystemResolver.resolve(null, proposal.theme, catalog)
    val wearColors =
      if (catalog === WearCatalog) WearColors.of(WearThemeResolver.roleColors(design)) else null
    return "weight(${WEIGHT.toInt()}f)" in
      KotlinExporter.export(proposal, design, "home", wearColors)
  }

  /** What one render of the weighted child shows: the axes along which the weight moved it. */
  private enum class Drawn {
    None,
    Row,
    Column,
    /** A weight can change the other axis too, as a text wraps differently in the width it gets. */
    Both;

    fun matches(declared: Drawn) = this == declared || (this == Both && declared != None)
  }

  private fun drawn(catalog: Catalog, slot: Slot): Drawn {
    fun boundsOf(weight: Double?) =
      treeOf(catalog, slot, weight).let { tree ->
        tree.nodes.firstOrNull { it.id == "w" }?.bounds
          ?: error("$slot: no node 'w' in ${tree.nodes.map { it.id }}")
      }
    val plain = boundsOf(null)
    val weighted = boundsOf(WEIGHT)
    // A weight that leaves a child no space on its axis (a scrolling column or an unbounded row
    // measures it with none) collapses it to zero: that is not a weight the previewer draws.
    val wide = abs(plain.width - weighted.width) > TOLERANCE && weighted.width > TOLERANCE
    val tall = abs(plain.height - weighted.height) > TOLERANCE && weighted.height > TOLERANCE
    return when {
      wide && tall -> Drawn.Both
      wide -> Drawn.Row
      tall -> Drawn.Column
      else -> Drawn.None
    }
  }

  /** What the catalog declares for [slot], as the layout a weight should show in. */
  private fun declared(catalog: Catalog, slot: Slot): Drawn {
    val host =
      components(catalog, slot, null).first {
        it.id() == "h" || it.id() == "root" && it.isHost(slot)
      }
    return when (slot.def.childLayout(slot.prop.name) { host[it] }) {
      LayoutScope.None -> Drawn.None
      LayoutScope.Row -> Drawn.Row
      LayoutScope.Column -> Drawn.Column
    }
  }

  private fun JsonObject.id() = (getValue("id") as JsonPrimitive).content

  private fun JsonObject.isHost(slot: Slot) =
    (getValue("component") as JsonPrimitive).content == slot.def.name

  private fun agreement(catalog: Catalog): List<String> {
    val problems = mutableListOf<String>()
    val seen = mutableMapOf<Drawn, Int>()
    slotsOf(catalog).forEach { slot ->
      val drawn =
        try {
          drawn(catalog, slot)
        } catch (e: IllegalStateException) {
          problems += e.message.orEmpty()
          return@forEach
        }
      seen.merge(declared(catalog, slot), 1, Int::plus)
      val exported = exportedWeight(catalog, slot)
      val declared = declared(catalog, slot)
      if (!drawn.matches(declared)) {
        problems += "$slot: the catalog declares $declared, the previewer draws $drawn"
      }
      if ((drawn != Drawn.None) != exported) {
        problems += "$slot: previewer weights it ($drawn), export writes weight=$exported"
      }
    }
    assertTrue(Drawn.Row in seen && Drawn.Column in seen && Drawn.None in seen, "seen=$seen")
    return problems
  }

  @Test
  fun `a child is weighted in the export exactly when it is in the previewer`() {
    assertEquals(emptyList(), agreement(M3ExpressiveCatalog))
  }

  @Test
  fun `a Wear child is weighted in the export exactly when it is in the previewer`() {
    assertEquals(emptyList(), agreement(WearCatalog))
  }

  private companion object {
    const val WEIGHT = 2.0
    const val TOLERANCE = 0.5f
  }
}
