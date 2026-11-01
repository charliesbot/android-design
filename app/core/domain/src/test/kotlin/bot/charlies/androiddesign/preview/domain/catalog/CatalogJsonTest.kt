package bot.charlies.androiddesign.preview.domain.catalog

import bot.charlies.androiddesign.preview.model.ColorRole
import bot.charlies.androiddesign.preview.model.M3ExpressiveCatalog
import bot.charlies.androiddesign.preview.model.MaterialShape
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class CatalogJsonTest {
  private val catalog = CatalogJson.catalog()
  private val schema = CatalogJson.proposalSchema()

  private fun JsonObject.obj(key: String) = getValue(key).jsonObject

  @Test
  fun `catalog declares the A2UI header`() {
    assertEquals("1.0", catalog.getValue("protocolVersion").jsonPrimitive.content)
    assertEquals(M3ExpressiveCatalog.ID, catalog.getValue("catalogId").jsonPrimitive.content)
    assertEquals(
      "https://json-schema.org/draft/2020-12/schema",
      catalog.getValue("\$schema").jsonPrimitive.content,
    )
  }

  @Test
  fun `catalog lists every registry component with a component const`() {
    val components = catalog.obj("components")
    assertEquals(M3ExpressiveCatalog.components.map { it.name }, components.keys.toList())
    M3ExpressiveCatalog.components.forEach { def ->
      val component = components.obj(def.name)
      assertEquals(
        def.name,
        component.obj("properties").obj("component").getValue("const").jsonPrimitive.content,
      )
    }
  }

  @Test
  fun `catalog covers the components the brief names`() {
    val names = M3ExpressiveCatalog.components.map { it.name }.toSet()
    val brief =
      "Column Row Box Grid FlexBox Spacer Divider Shape Text Icon Image Scaffold TopAppBar NavigationBar NavigationBarItem NavigationRail NavigationRailItem FloatingToolbar Card List ListItem Button ToggleButton ButtonGroup IconButton SplitButton FloatingActionButton FabMenu FabMenuItem AssistChip FilterChip InputChip SuggestionChip ChipGroup Switch Checkbox Slider RangeSlider TextField LoadingIndicator ProgressIndicator BottomSheet Dialog Menu MenuItem"
        .split(" ")
    assertEquals(emptyList(), brief.filterNot { it in names })
  }

  @Test
  fun `required props are listed as required`() {
    val button = catalog.obj("components").obj("Button")
    val required = button.getValue("required").jsonArray.map { it.jsonPrimitive.content }
    assertTrue("component" in required && "label" in required && "id" !in required)
  }

  @Test
  fun `an enum prop lists its values`() {
    val variant = catalog.obj("components").obj("Button").obj("properties").obj("variant")
    assertEquals(
      listOf("filled", "tonal", "elevated", "outlined", "text"),
      variant.getValue("enum").jsonArray.map { it.jsonPrimitive.content },
    )
  }

  @Test
  fun `a prop with a catalog default emits it, and a prop without one does not`() {
    val button = catalog.obj("components").obj("Button").obj("properties")
    assertEquals(JsonPrimitive("filled"), button.obj("variant").getValue("default"))
    assertEquals(JsonPrimitive("small"), button.obj("size").getValue("default"))
    assertEquals(JsonPrimitive(true), button.obj("enabled").getValue("default"))
    assertEquals(JsonPrimitive(false), button.obj("primary").getValue("default"))
    val icon = catalog.obj("components").obj("Icon").obj("properties")
    assertEquals(JsonPrimitive(24), icon.obj("size").getValue("default"))
    assertTrue("default" !in button.obj("label"))
    val declared =
      M3ExpressiveCatalog.components.flatMap { it.props }.filter { it.default != null }.size
    val emitted =
      catalog.obj("components").values.sumOf { component ->
        component.jsonObject.obj("properties").values.count { "default" in it.jsonObject }
      }
    assertEquals(declared, emitted)
  }

  @Test
  fun `token props reference the token defs`() {
    val color = catalog.obj("components").obj("Text").obj("properties").obj("color")
    assertEquals("#/\$defs/ColorRole", color.getValue("\$ref").jsonPrimitive.content)
    val roles = catalog.obj("\$defs").obj("ColorRole").getValue("enum").jsonArray
    assertEquals(ColorRole.entries.map { it.wire }, roles.map { it.jsonPrimitive.content })
    val shapes = catalog.obj("\$defs").obj("MaterialShapeName").getValue("enum").jsonArray
    assertEquals(MaterialShape.entries.map { it.wire }, shapes.map { it.jsonPrimitive.content })
  }

  @Test
  fun `allowed children and the Compose counterpart are annotated`() {
    val scaffold = catalog.obj("components").obj("Scaffold")
    val topBar = scaffold.obj("properties").obj("topBar")
    assertEquals(
      JsonArray(listOf(JsonPrimitive("TopAppBar"))),
      topBar.getValue("x-allowedComponents"),
    )
    assertEquals("Scaffold", scaffold.getValue("x-compose").jsonPrimitive.content)
  }

  @Test
  fun `catalog declares the renderer functions`() {
    val functions = catalog.obj("functions")
    assertEquals(listOf("navigate", "back", "open", "dismiss"), functions.keys.toList())
  }

  @Test
  fun `the proposal schema is draft 2020-12 and describes the file`() {
    assertEquals(
      "https://json-schema.org/draft/2020-12/schema",
      schema.getValue("\$schema").jsonPrimitive.content,
    )
    val props = schema.obj("properties")
    assertEquals(setOf("\$schema", "format", "name", "theme", "navigation", "screens"), props.keys)
    assertEquals(
      listOf("format", "navigation", "screens"),
      schema.getValue("required").jsonArray.map { it.jsonPrimitive.content },
    )
  }

  @Test
  fun `the proposal schema carries every catalog component`() {
    val defs = schema.obj("\$defs")
    M3ExpressiveCatalog.components.forEach {
      assertTrue("component.${it.name}" in defs.keys, it.name)
    }
    assertEquals(
      M3ExpressiveCatalog.components.size,
      defs.obj("anyComponent").getValue("oneOf").jsonArray.size,
    )
  }

  @Test
  fun `every catalog ref resolves in the vendored A2UI common types`() {
    val dir = File(requireNotNull(System.getProperty("schemas.dir")) { "schemas.dir not set" })
    val refs =
      Regex("\"\\\$ref\": \"([^\"#]+)#/\\\$defs/([^\"]+)\"")
        .findAll(CatalogJson.render(catalog))
        .map { it.groupValues[1] to it.groupValues[2] }
        .toSet()
    assertTrue(refs.isNotEmpty())
    for ((file, name) in refs) {
      val target = File(dir, file)
      assertTrue(target.isFile, "missing $file")
      val defs = Json.parseToJsonElement(target.readText()).jsonObject.obj("\$defs")
      assertTrue(name in defs.keys, "$file has no \$defs/$name")
    }
  }

  @Test
  fun `committed schema files match the generated output`() {
    val dir = File(requireNotNull(System.getProperty("schemas.dir")) { "schemas.dir not set" })
    assertEquals(
      CatalogJson.render(catalog),
      File(dir, "m3-expressive.catalog.json").readText(),
      "run ./gradlew :core:domain:writeSchemas",
    )
    assertEquals(
      CatalogJson.render(schema),
      File(dir, "proposal.schema.json").readText(),
      "run ./gradlew :core:domain:writeSchemas",
    )
  }
}
