package bot.charlies.androiddesign.preview.domain.catalog

import bot.charlies.androiddesign.preview.domain.design.DesignSystemResolver
import bot.charlies.androiddesign.preview.domain.validation.ScreenValidator
import bot.charlies.androiddesign.preview.model.ActionFunction
import bot.charlies.androiddesign.preview.model.ColorRole
import bot.charlies.androiddesign.preview.model.ComponentDef
import bot.charlies.androiddesign.preview.model.M3ExpressiveCatalog
import bot.charlies.androiddesign.preview.model.MaterialShape
import bot.charlies.androiddesign.preview.model.PaletteVariant
import bot.charlies.androiddesign.preview.model.PaneRole
import bot.charlies.androiddesign.preview.model.PropDef
import bot.charlies.androiddesign.preview.model.PropKind
import bot.charlies.androiddesign.preview.model.Proposal
import bot.charlies.androiddesign.preview.model.ShapeToken
import bot.charlies.androiddesign.preview.model.SizeClass
import bot.charlies.androiddesign.preview.model.TypeRole
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject

/**
 * Generates the A2UI v1.0 catalog JSON and the proposal file's JSON Schema from the component
 * registry, so the registry stays the one place a component is declared.
 */
object CatalogJson {
  private const val RAW =
    "https://raw.githubusercontent.com/charliesbot/android-design/main/app/schemas"
  const val CATALOG_FILE = "m3-expressive.catalog.json"
  const val PROPOSAL_SCHEMA_FILE = "proposal.schema.json"
  const val CATALOG_URL = "$RAW/$CATALOG_FILE"
  const val PROPOSAL_SCHEMA_URL = "$RAW/$PROPOSAL_SCHEMA_FILE"
  private const val DRAFT = "https://json-schema.org/draft/2020-12/schema"

  private val pretty = Json {
    prettyPrint = true
    prettyPrintIndent = "  "
  }

  /** Two-space pretty JSON with a trailing newline. */
  fun render(json: JsonObject): String = pretty.encodeToString(JsonObject.serializer(), json) + "\n"

  /** The catalog an agent reads through `get_catalog`, written to `m3-expressive.catalog.json`. */
  fun catalog(): JsonObject =
    obj(
      "\$schema" to DRAFT,
      "\$id" to CATALOG_URL,
      "protocolVersion" to M3ExpressiveCatalog.PROTOCOL_VERSION,
      "title" to M3ExpressiveCatalog.TITLE,
      "description" to
        "Material 3 Expressive components that map one to one onto Jetpack Compose material3.",
      "catalogId" to M3ExpressiveCatalog.ID,
      "instructions" to INSTRUCTIONS,
      "components" to
        JsonObject(
          M3ExpressiveCatalog.components.associate { it.name to component(it, Refs.Catalog) },
        ),
      "functions" to JsonObject(ActionFunction.entries.associate { it.wire to function(it) }),
      "\$defs" to
        JsonObject(
          mapOf(
            "anyComponent" to anyComponent { "#/components/$it" },
            "anyFunction" to anyFunction("#/functions/"),
            "Action" to actionDef(),
            "ComponentCommon" to componentCommon(Refs.Catalog),
          ) + tokenDefs(),
        ),
    )

  /** The JSON Schema of a `design/<slug>.proposal.json` file; self-contained. */
  fun proposalSchema(): JsonObject =
    obj(
      "\$schema" to DRAFT,
      "\$id" to PROPOSAL_SCHEMA_URL,
      "title" to "Android design proposal",
      "description" to
        "A proposal file: a navigation graph of screens, each an A2UI v1.0 createSurface body " +
          "against the m3-expressive catalog. The component definitions below are generated from " +
          "the same registry as $CATALOG_FILE.",
      "type" to "object",
      "properties" to
        obj(
          "\$schema" to obj("type" to "string"),
          "format" to obj("const" to Proposal.FORMAT),
          "name" to obj("type" to "string"),
          "theme" to ref("#/\$defs/Theme"),
          "navigation" to ref("#/\$defs/Navigation"),
          "screens" to
            obj(
              "type" to "object",
              "propertyNames" to obj("pattern" to ScreenValidator.IDENTIFIER.pattern),
              "additionalProperties" to ref("#/\$defs/Screen"),
            ),
        ),
      "required" to strings("format", "navigation", "screens"),
      "additionalProperties" to false,
      "\$defs" to
        JsonObject(
          mapOf(
            "Theme" to themeDef(),
            "Navigation" to navigationDef(),
            "Screen" to screenDef(),
            "anyComponent" to anyComponent { "#/\$defs/component.$it" },
            "anyFunction" to anyFunction("#/\$defs/function."),
            "Action" to actionDef(),
            "ComponentCommon" to componentCommon(Refs.Proposal),
          ) +
            commonTypes() +
            tokenDefs() +
            themeSubDefs() +
            M3ExpressiveCatalog.components.associate {
              "component.${it.name}" to component(it, Refs.Proposal)
            } +
            ActionFunction.entries.associate { "function.${it.wire}" to function(it) },
        ),
    )

  // region components

  private enum class Refs(val common: String) {
    /**
     * The catalog file refers to A2UI's shared definitions by file, as Google's basic catalog does,
     * using the copy vendored beside it in `schemas/a2ui-v1_0/`.
     */
    Catalog("a2ui-v1_0/common_types.json#/\$defs/"),
    Proposal("#/\$defs/"),
  }

  private fun component(def: ComponentDef, refs: Refs): JsonObject {
    val properties = linkedMapOf<String, JsonElement>("component" to obj("const" to def.name))
    for (prop in def.props) properties[prop.name] = property(prop, refs)
    val required = listOf("component") + def.props.filter { it.required }.map { it.name }
    val fields =
      linkedMapOf<String, JsonElement>(
        "type" to JsonPrimitive("object"),
        "description" to JsonPrimitive(def.description),
        "x-compose" to JsonPrimitive(def.compose),
      )
    if (def.interactive) fields["x-interactive"] = JsonPrimitive(true)
    if (def.overlay) fields["x-overlay"] = JsonPrimitive(true)
    fields["allOf"] = JsonArray(listOf(ref("#/\$defs/ComponentCommon")))
    fields["properties"] = JsonObject(properties)
    fields["required"] = strings(*required.toTypedArray())
    fields["unevaluatedProperties"] = JsonPrimitive(false)
    return JsonObject(fields)
  }

  private fun componentCommon(refs: Refs): JsonObject =
    obj(
      "type" to "object",
      "description" to "Props every component accepts.",
      "properties" to
        JsonObject(
          linkedMapOf<String, JsonElement>(
            "id" to ref(refs.common + "ComponentId"),
            M3ExpressiveCatalog.SHOW_IN to
              obj(
                "type" to "array",
                "items" to ref("#/\$defs/SizeClass"),
                "description" to "Size classes the component shows in; all by default.",
              ),
            M3ExpressiveCatalog.SIZE_CLASS to
              obj(
                "type" to "object",
                "description" to
                  "Prop overrides per size class (medium 600 to 839 dp, expanded 840 dp and up).",
                "properties" to
                  obj("medium" to obj("type" to "object"), "expanded" to obj("type" to "object")),
                "additionalProperties" to false,
              ),
            M3ExpressiveCatalog.STYLE to
              obj("type" to "string", "description" to "A style name from material.styles."),
          ) + M3ExpressiveCatalog.commonProps.associate { it.name to property(it, refs) },
        ),
      "required" to strings("id"),
    )

  private fun property(prop: PropDef, refs: Refs): JsonObject {
    val fields = linkedMapOf<String, JsonElement>()
    when (val kind = prop.kind) {
      PropKind.Child -> fields["\$ref"] = JsonPrimitive(refs.common + "Child")
      PropKind.ChildList -> fields["\$ref"] = JsonPrimitive(refs.common + "ChildList")
      PropKind.Text,
      PropKind.Icon,
      PropKind.Image -> fields["\$ref"] = JsonPrimitive(refs.common + "DynamicString")
      PropKind.Bool -> fields["\$ref"] = JsonPrimitive(refs.common + "DynamicBoolean")
      is PropKind.Number -> fields["\$ref"] = JsonPrimitive(refs.common + "DynamicNumber")
      PropKind.NumberRange -> {
        fields["type"] = JsonPrimitive("array")
        fields["items"] = obj("type" to "number")
        fields["minItems"] = JsonPrimitive(2)
        fields["maxItems"] = JsonPrimitive(2)
      }
      is PropKind.Enum -> {
        fields["type"] = JsonPrimitive("string")
        fields["enum"] = strings(*kind.values.toTypedArray())
      }
      PropKind.ColorRole -> fields["\$ref"] = JsonPrimitive("#/\$defs/ColorRole")
      PropKind.TypeRole -> fields["\$ref"] = JsonPrimitive("#/\$defs/TypeRole")
      PropKind.Shape -> fields["\$ref"] = JsonPrimitive("#/\$defs/ShapeValue")
      PropKind.Spacing -> {
        fields["type"] = JsonPrimitive("string")
        fields["x-token"] = JsonPrimitive("spacing")
      }
      PropKind.Action -> fields["\$ref"] = JsonPrimitive("#/\$defs/Action")
      PropKind.Binding -> fields["\$ref"] = JsonPrimitive(refs.common + "DataBinding")
    }
    prop.allowedChildren?.let { fields["x-allowedComponents"] = strings(*it.toTypedArray()) }
    val description = describe(prop)
    if (description.isNotEmpty()) fields["description"] = JsonPrimitive(description)
    return JsonObject(fields)
  }

  private fun describe(prop: PropDef): String =
    when (val kind = prop.kind) {
      PropKind.Spacing ->
        listOf(
            prop.description,
            "A name from the design system's spacing scale (default: ${DesignSystemResolver.DEFAULT_SPACING.keys.joinToString()}).",
          )
          .filter { it.isNotEmpty() }
          .joinToString(" ")
      is PropKind.Number ->
        listOfNotNull(
            prop.description.takeIf { it.isNotEmpty() },
            kind.bounds?.let { "Must be ${it.description}." },
          )
          .joinToString(" ")
      else -> prop.description
    }

  // endregion

  // region functions and actions

  private fun function(function: ActionFunction): JsonObject {
    val args = function.argument
    val properties = linkedMapOf<String, JsonElement>("@call" to obj("const" to function.wire))
    val required = mutableListOf("@call")
    if (args != null) {
      properties["args"] =
        obj(
          "type" to "object",
          "properties" to obj(args to obj("type" to "string")),
          "required" to strings(args),
          "unevaluatedProperties" to false,
        )
      required += "args"
    }
    return obj(
      "type" to "object",
      "description" to function.description,
      "returnType" to "void",
      "properties" to JsonObject(properties),
      "required" to strings(*required.toTypedArray()),
    )
  }

  private fun anyFunction(prefix: String): JsonObject =
    obj("oneOf" to JsonArray(ActionFunction.entries.map { ref("$prefix${it.wire}") }))

  private fun anyComponent(target: (String) -> String): JsonObject =
    obj(
      "oneOf" to JsonArray(M3ExpressiveCatalog.components.map { ref(target(it.name)) }),
      "discriminator" to obj("propertyName" to "component"),
    )

  private fun actionDef(): JsonObject =
    obj(
      "description" to "A renderer-side function call. Event actions are not supported.",
      "type" to "object",
      "properties" to obj("functionCall" to ref("#/\$defs/anyFunction")),
      "required" to strings("functionCall"),
      "additionalProperties" to false,
    )

  // endregion

  // region shared definitions

  private fun tokenDefs(): Map<String, JsonObject> =
    mapOf(
      "ColorRole" to enumDef("A ColorScheme role.", ColorRole.entries.map { it.wire }),
      "TypeRole" to enumDef("A Typography role.", TypeRole.entries.map { it.wire }),
      "ShapeToken" to enumDef("A Material corner token.", ShapeToken.entries.map { it.wire }),
      "MaterialShapeName" to
        enumDef("A MaterialShapes polygon.", MaterialShape.entries.map { it.wire }),
      "ShapeValue" to
        obj(
          "description" to "A corner token or a MaterialShapes polygon.",
          "oneOf" to
            JsonArray(listOf(ref("#/\$defs/ShapeToken"), ref("#/\$defs/MaterialShapeName"))),
        ),
      "SizeClass" to enumDef("A window width class.", SizeClass.entries.map { it.wire }),
    )

  /** The slice of A2UI's common types that proposals use, for the self-contained schema. */
  private fun commonTypes(): Map<String, JsonObject> =
    mapOf(
      "ComponentId" to
        obj(
          "type" to "string",
          "pattern" to ScreenValidator.IDENTIFIER.pattern,
          "description" to "A component id, unique within its screen.",
        ),
      "Child" to
        obj(
          "\$ref" to "#/\$defs/ComponentId",
          "description" to "A reference to one component by id.",
        ),
      "ChildList" to
        obj(
          "oneOf" to
            JsonArray(
              listOf(
                obj("type" to "array", "items" to ref("#/\$defs/ComponentId")),
                obj(
                  "type" to "object",
                  "description" to "A template repeated for each item of a data model list.",
                  "properties" to
                    obj(
                      "componentId" to ref("#/\$defs/ComponentId"),
                      "path" to obj("type" to "string"),
                    ),
                  "required" to strings("componentId", "path"),
                  "additionalProperties" to false,
                ),
              ),
            ),
        ),
      "DataBinding" to
        obj(
          "type" to "object",
          "properties" to obj("@path" to obj("type" to "string")),
          "required" to strings("@path"),
          "additionalProperties" to false,
        ),
      "IndexCall" to
        obj(
          "type" to "object",
          "properties" to
            obj("@call" to obj("const" to "@index"), "args" to obj("type" to "object")),
          "required" to strings("@call"),
        ),
      "DynamicString" to dynamic(obj("type" to "string")),
      "DynamicNumber" to dynamic(obj("type" to "number")),
      "DynamicBoolean" to dynamic(obj("type" to "boolean")),
    )

  private fun dynamic(literal: JsonObject): JsonObject =
    obj(
      "oneOf" to JsonArray(listOf(literal, ref("#/\$defs/DataBinding"), ref("#/\$defs/IndexCall"))),
    )

  private fun themeDef(): JsonObject {
    val dimension =
      obj("type" to JsonArray(listOf(JsonPrimitive("string"), JsonPrimitive("number"))))
    return obj(
      "description" to "Overrides DESIGN.md front matter; same keys, merged per key.",
      "type" to "object",
      "properties" to
        obj(
          "version" to obj("type" to "string"),
          "name" to obj("type" to "string"),
          "colors" to
            obj(
              "type" to "object",
              "propertyNames" to
                obj(
                  "enum" to strings("primary", *ColorRole.entries.map { it.kebab }.toTypedArray()),
                ),
              "additionalProperties" to obj("type" to "string", "pattern" to "^#[0-9a-fA-F]{6}$"),
            ),
          "typography" to
            obj(
              "type" to "object",
              "propertyNames" to
                obj("enum" to strings(*TypeRole.entries.map { it.kebab }.toTypedArray())),
              "additionalProperties" to ref("#/\$defs/Typography"),
            ),
          "rounded" to
            obj(
              "type" to "object",
              "propertyNames" to
                obj("enum" to strings(*ShapeToken.entries.map { it.kebab }.toTypedArray())),
              "additionalProperties" to dimension,
            ),
          "spacing" to obj("type" to "object", "additionalProperties" to dimension),
          "material" to
            obj(
              "type" to "object",
              "properties" to
                obj(
                  "scheme" to
                    obj(
                      "type" to "object",
                      "properties" to
                        obj(
                          "seed" to
                            obj(
                              "type" to "string",
                              "description" to "#RRGGBB or {colors.primary}.",
                            ),
                          "variant" to
                            obj(
                              "enum" to
                                strings(*PaletteVariant.entries.map { it.wire }.toTypedArray()),
                            ),
                          "contrast" to obj("type" to "number", "minimum" to -1, "maximum" to 1),
                        ),
                      "additionalProperties" to false,
                    ),
                  "styles" to
                    obj("type" to "object", "additionalProperties" to ref("#/\$defs/Style")),
                ),
              "additionalProperties" to false,
            ),
        ),
      "additionalProperties" to false,
    )
  }

  /** Definitions the theme refers to; they live at the schema root so `#/$defs/...` resolves. */
  private fun themeSubDefs(): Map<String, JsonObject> {
    val dimension =
      obj("type" to JsonArray(listOf(JsonPrimitive("string"), JsonPrimitive("number"))))
    return mapOf(
      "Typography" to
        obj(
          "type" to "object",
          "properties" to
            obj(
              "fontFamily" to
                obj(
                  "enum" to
                    strings(
                      "Google Sans Flex",
                      "Google Sans Code",
                      "Fraunces",
                      "Bricolage Grotesque",
                      "Big Shoulders Display",
                      "Doto",
                    ),
                ),
              "fontVariation" to obj("type" to "string"),
              "fontWeight" to obj("type" to "integer"),
              "letterSpacing" to dimension,
            ),
          "additionalProperties" to false,
        ),
      "Style" to
        obj(
          "type" to "object",
          "description" to "Token names only.",
          "properties" to
            obj(
              "type" to ref("#/\$defs/TypeRole"),
              "color" to ref("#/\$defs/ColorRole"),
              "container" to ref("#/\$defs/ColorRole"),
              "shape" to ref("#/\$defs/ShapeValue"),
              "padding" to obj("type" to "string"),
            ),
          "additionalProperties" to false,
        ),
    )
  }

  private fun navigationDef(): JsonObject =
    obj(
      "type" to "object",
      "properties" to
        obj(
          "start" to obj("type" to "string", "description" to "The screen the proposal opens on."),
          "panes" to
            obj(
              "type" to "object",
              "description" to "Screen id to pane role; unlisted screens are a single pane.",
              "additionalProperties" to
                obj(
                  "enum" to strings(*PaneRole.entries.map { it.name.lowercase() }.toTypedArray()),
                ),
            ),
        ),
      "required" to strings("start"),
      "additionalProperties" to false,
    )

  private fun screenDef(): JsonObject =
    obj(
      "type" to "object",
      "description" to "The body of an A2UI createSurface message.",
      "properties" to
        obj(
          "catalogId" to obj("const" to M3ExpressiveCatalog.ID),
          "dataModel" to obj("description" to "Sample data that bindings read."),
          "components" to
            obj(
              "type" to "array",
              "description" to "A flat list with exactly one component whose id is root.",
              "minItems" to 1,
              "items" to ref("#/\$defs/anyComponent"),
            ),
        ),
      "required" to strings("catalogId", "components"),
      "additionalProperties" to false,
    )

  private fun enumDef(description: String, values: List<String>): JsonObject =
    obj("type" to "string", "description" to description, "enum" to strings(*values.toTypedArray()))

  // endregion

  private fun obj(vararg pairs: Pair<String, Any?>): JsonObject = buildJsonObject {
    pairs.forEach { (key, value) -> put(key, json(value)) }
  }

  private fun json(value: Any?): JsonElement =
    when (value) {
      null -> JsonNull
      is JsonElement -> value
      is String -> JsonPrimitive(value)
      is Boolean -> JsonPrimitive(value)
      is Number -> JsonPrimitive(value)
      else -> error("unsupported ${value::class}")
    }

  private fun ref(target: String): JsonObject = obj("\$ref" to target)

  private fun strings(vararg values: String): JsonArray =
    JsonArray(values.map { JsonPrimitive(it) })

  private val INSTRUCTIONS =
    """
    Design guidelines for the m3-expressive catalog.

    1. Every prop that names a color, type, shape or spacing takes a token name, never a hex value or a raw dimension. Use a `style` from the design system's `material.styles` to bundle tokens.
    2. Components reference children by id. Named slots (`topBar`, `content`, `floatingActionButton`) take one id; lists (`children`, `actions`, `items`) take an array of ids.
    3. A screen is a flat component list with exactly one `root`. Overlays (`BottomSheet`, `Dialog`, `Menu`) stay closed until an `open` action names them.
    4. Interaction is renderer-only: `navigate`, `back`, `open` and `dismiss`. Event actions do not exist.
    5. Mark the screen's main action with `primary: true` on one component, and give every icon-only control a `contentDescription`.
    6. Use `showIn` and `sizeClass` to adapt a component at 600 dp (medium) and 840 dp (expanded).
    """
      .trimIndent()
}
