package bot.charlies.androiddesign.preview.domain.catalog

import bot.charlies.androiddesign.preview.domain.PreviewException
import bot.charlies.androiddesign.preview.domain.design.DesignSystemResolver
import bot.charlies.androiddesign.preview.domain.validation.ScreenValidator
import bot.charlies.androiddesign.preview.model.ActionFunction
import bot.charlies.androiddesign.preview.model.BundledFont
import bot.charlies.androiddesign.preview.model.Catalog
import bot.charlies.androiddesign.preview.model.Catalogs
import bot.charlies.androiddesign.preview.model.ComponentDef
import bot.charlies.androiddesign.preview.model.M3ExpressiveCatalog
import bot.charlies.androiddesign.preview.model.M3Omission
import bot.charlies.androiddesign.preview.model.MaterialShape
import bot.charlies.androiddesign.preview.model.PaletteVariant
import bot.charlies.androiddesign.preview.model.PaneRole
import bot.charlies.androiddesign.preview.model.PreviewJson
import bot.charlies.androiddesign.preview.model.PropDef
import bot.charlies.androiddesign.preview.model.PropKind
import bot.charlies.androiddesign.preview.model.Proposal
import bot.charlies.androiddesign.preview.model.ShapeToken
import bot.charlies.androiddesign.preview.model.SizeClass
import bot.charlies.androiddesign.preview.model.camelToKebab
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
  /** The largest number a badge holds: [PropKind.BADGE_MAX_CHARACTERS] digits. */
  private const val BADGE_MAX_NUMBER = 9999

  private const val RAW =
    "https://raw.githubusercontent.com/charliesbot/android-design/main/app/schemas"
  const val CATALOG_FILE = "m3-expressive.catalog.json"
  const val PROPOSAL_SCHEMA_FILE = "proposal.schema.json"
  const val CATALOG_URL = "$RAW/$CATALOG_FILE"
  const val PROPOSAL_SCHEMA_URL = "$RAW/$PROPOSAL_SCHEMA_FILE"

  /** The committed file of [catalog]'s JSON. */
  fun catalogFile(catalog: Catalog): String = "${catalog.key}.catalog.json"

  private fun catalogUrl(catalog: Catalog): String = "$RAW/${catalogFile(catalog)}"

  private const val DRAFT = "https://json-schema.org/draft/2020-12/schema"

  /** Two-space pretty JSON with a trailing newline. */
  fun render(json: JsonObject): String =
    PreviewJson.pretty.encodeToString(JsonObject.serializer(), json) + "\n"

  /** The catalog an agent reads through `get_catalog`, written to its [catalogFile]. */
  fun catalog(catalog: Catalog = M3ExpressiveCatalog): JsonObject {
    val scope = Scope(catalog, Refs.Catalog, prefix = "")
    return obj(
      "\$schema" to DRAFT,
      "\$id" to catalogUrl(catalog),
      "protocolVersion" to Catalog.PROTOCOL_VERSION,
      "title" to catalog.title,
      "description" to catalog.description,
      "catalogId" to catalog.id,
      "instructions" to instructions(catalog),
      "components" to JsonObject(catalog.components.associate { it.name to component(it, scope) }),
      "x-m3-omissions" to JsonArray(catalog.m3Omissions.map(::omission)),
      "functions" to JsonObject(ActionFunction.entries.associate { it.wire to function(it) }),
      "\$defs" to
        JsonObject(
          mapOf(
            "anyComponent" to anyComponent(catalog) { "#/components/$it" },
            "anyFunction" to anyFunction("#/functions/"),
            "Action" to actionDef(),
            "ComponentCommon" to componentCommon(scope),
          ) + tokenDefs(catalog, prefix = ""),
        ),
    )
  }

  /**
   * The full catalog entries of the components named in [names] of [catalog], keyed by name, in the
   * order asked. An unknown name is a [PreviewException] that lists the known ones.
   */
  fun components(names: List<String>, catalog: Catalog = M3ExpressiveCatalog): JsonObject {
    val unknown = names.filter { catalog.find(it) == null }
    if (unknown.isNotEmpty()) {
      throw PreviewException(
        "no component ${unknown.joinToString { "'$it'" }} in the ${catalog.key} catalog; " +
          "the components are " +
          catalog.components.joinToString { it.name },
      )
    }
    val scope = Scope(catalog, Refs.Catalog, prefix = "")
    return JsonObject(
      names.distinct().associateWith { component(checkNotNull(catalog.find(it)), scope) },
    )
  }

  /**
   * The JSON Schema of a `design/<slug>.proposal.json` file; self-contained. A screen names one of
   * the catalogs; the first catalog's definitions keep their plain names and every other catalog's
   * are prefixed with its key.
   */
  fun proposalSchema(): JsonObject =
    obj(
      "\$schema" to DRAFT,
      "\$id" to PROPOSAL_SCHEMA_URL,
      "title" to "Android design proposal",
      "description" to
        "A proposal file: a navigation graph of screens, each an A2UI v1.0 createSurface body " +
          "against one catalog (${Catalogs.all.joinToString(" or ") { it.key }}); every screen " +
          "of a proposal uses the same one. The component definitions below are generated from " +
          "the same registries as the catalog files (${Catalogs.all.joinToString { catalogFile(it) }}).",
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
            "Screen" to
              obj(
                "oneOf" to JsonArray(Catalogs.all.map { ref("#/\$defs/${screenDefName(it)}") }),
                "discriminator" to obj("propertyName" to "catalogId"),
              ),
            "Action" to actionDef(),
            "anyFunction" to anyFunction("#/\$defs/function."),
            "AnyColorRole" to anyOfDefs(Catalogs.all.map { "${defPrefix(it)}ColorRole" }),
            "AnyTypeRole" to anyOfDefs(Catalogs.all.map { "${defPrefix(it)}TypeRole" }),
          ) +
            commonTypes() +
            themeSubDefs() +
            ActionFunction.entries.associate { "function.${it.wire}" to function(it) } +
            Catalogs.all.flatMap { catalogSchemaDefs(it).toList() },
        ),
    )

  /** The prefix of a catalog's definition names in the proposal schema; the first has none. */
  private fun defPrefix(catalog: Catalog): String =
    if (catalog === Catalogs.all.first()) "" else "${catalog.key}."

  private fun screenDefName(catalog: Catalog): String = "${defPrefix(catalog)}Screen"

  private fun anyOfDefs(names: List<String>): JsonObject =
    obj("anyOf" to JsonArray(names.map { ref("#/\$defs/$it") }))

  /** Everything the proposal schema holds for one catalog. */
  private fun catalogSchemaDefs(catalog: Catalog): Map<String, JsonObject> {
    val prefix = defPrefix(catalog)
    val scope = Scope(catalog, Refs.Proposal, prefix)
    return mapOf(
      screenDefName(catalog) to screenDef(catalog, prefix),
      "${prefix}anyComponent" to anyComponent(catalog) { "#/\$defs/${prefix}component.$it" },
      "${prefix}ComponentCommon" to componentCommon(scope),
    ) +
      tokenDefs(catalog, prefix) +
      catalog.components.associate { "${prefix}component.${it.name}" to component(it, scope) }
  }

  // region components

  private enum class Refs(val common: String) {
    /**
     * The catalog file refers to A2UI's shared definitions by file, as Google's basic catalog does,
     * using the copy vendored beside it in `schemas/a2ui-v1_0/`.
     */
    Catalog("a2ui-v1_0/common_types.json#/\$defs/"),
    Proposal("#/\$defs/"),
  }

  /**
   * Where a catalog's definitions are written: [refs] says how to name A2UI's shared types, and
   * [prefix] is put before the names of the definitions that differ per catalog.
   */
  private class Scope(val catalog: Catalog, val refs: Refs, val prefix: String) {
    fun def(name: String) = "#/\$defs/$prefix$name"
  }

  private fun component(def: ComponentDef, scope: Scope): JsonObject {
    val properties = linkedMapOf<String, JsonElement>("component" to obj("const" to def.name))
    for (prop in def.props) properties[prop.name] = property(prop, scope)
    val required = listOf("component") + def.props.filter { it.required }.map { it.name }
    val fields =
      linkedMapOf<String, JsonElement>(
        "type" to JsonPrimitive("object"),
        "description" to JsonPrimitive(def.description),
        "x-compose" to JsonPrimitive(def.compose),
      )
    if (def.m3Pages.isNotEmpty()) fields["x-m3-pages"] = strings(*def.m3Pages.toTypedArray())
    if (def.interactive) fields["x-interactive"] = JsonPrimitive(true)
    if (def.overlay) fields["x-overlay"] = JsonPrimitive(true)
    fields["allOf"] = JsonArray(listOf(ref(scope.def("ComponentCommon"))))
    fields["properties"] = JsonObject(properties)
    fields["required"] = strings(*required.toTypedArray())
    fields["unevaluatedProperties"] = JsonPrimitive(false)
    return JsonObject(fields)
  }

  private fun omission(omission: M3Omission): JsonObject {
    val fields =
      linkedMapOf<String, JsonElement>(
        "page" to JsonPrimitive(omission.page),
        "reason" to JsonPrimitive(omission.reason.wire),
        "why" to JsonPrimitive(omission.why),
      )
    omission.waitsFor?.let { fields["waitsFor"] = JsonPrimitive(it) }
    return JsonObject(fields)
  }

  private fun componentCommon(scope: Scope): JsonObject {
    val catalog = scope.catalog
    val fields = linkedMapOf<String, JsonElement>("id" to ref(scope.refs.common + "ComponentId"))
    if (catalog.showInSizes.isNotEmpty()) {
      fields[Catalog.SHOW_IN] =
        obj(
          "type" to "array",
          "items" to ref("#/\$defs/SizeClass"),
          "description" to "Size classes the component shows in; all by default.",
        )
    }
    fields[Catalog.SIZE_CLASS] =
      obj(
        "type" to "object",
        "description" to sizeClassDescription(catalog),
        "properties" to
          JsonObject(catalog.sizeBreakpoints.associate { it.key to obj("type" to "object") }),
        "additionalProperties" to false,
      )
    fields[Catalog.STYLE] =
      obj("type" to "string", "description" to "A style name from material.styles.")
    for (prop in catalog.commonProps) fields[prop.name] = property(prop, scope)
    return obj(
      "type" to "object",
      "description" to "Props every component accepts.",
      "properties" to JsonObject(fields),
      "required" to strings("id"),
    )
  }

  /** `Prop overrides per size class (medium 600 to 839 dp, expanded 840 dp and up).` */
  private fun sizeClassDescription(catalog: Catalog): String {
    val breakpoints = catalog.sizeBreakpoints.sortedBy { it.minWidthDp }
    val ranges = breakpoints.mapIndexed { index, breakpoint ->
      val next = breakpoints.getOrNull(index + 1)
      if (next == null) {
        "${breakpoint.key} ${breakpoint.minWidthDp} dp and up"
      } else {
        "${breakpoint.key} ${breakpoint.minWidthDp} to ${next.minWidthDp - 1} dp"
      }
    }
    return "Prop overrides per size class (${ranges.joinToString(", ")})."
  }

  private fun property(prop: PropDef, scope: Scope): JsonObject {
    val refs = scope.refs
    val fields = linkedMapOf<String, JsonElement>()
    when (val kind = prop.kind) {
      PropKind.Child -> fields["\$ref"] = JsonPrimitive(refs.common + "Child")
      PropKind.ChildList -> fields["\$ref"] = JsonPrimitive(refs.common + "ChildList")
      PropKind.Text,
      PropKind.Icon,
      PropKind.Image -> fields["\$ref"] = JsonPrimitive(refs.common + "DynamicString")
      PropKind.Bool -> fields["\$ref"] = JsonPrimitive(refs.common + "DynamicBoolean")
      PropKind.Badge ->
        fields["oneOf"] =
          JsonArray(
            listOf(
              obj("type" to "boolean"),
              obj("type" to "number", "maximum" to BADGE_MAX_NUMBER),
              obj("type" to "string", "maxLength" to PropKind.BADGE_MAX_CHARACTERS),
              ref(refs.common + "DataBinding"),
              ref("#/\$defs/IndexCall"),
            ),
          )
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
      PropKind.ColorRole -> fields["\$ref"] = JsonPrimitive(scope.def("ColorRole"))
      PropKind.TypeRole -> fields["\$ref"] = JsonPrimitive(scope.def("TypeRole"))
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
    prop.default?.let { fields["default"] = it }
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

  private fun anyComponent(catalog: Catalog, target: (String) -> String): JsonObject =
    obj(
      "oneOf" to JsonArray(catalog.components.map { ref(target(it.name)) }),
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

  /**
   * The token vocabularies of [catalog]. The phone's include the shape and size class tokens, which
   * only it has; the names of the others get [prefix].
   */
  private fun tokenDefs(catalog: Catalog, prefix: String): Map<String, JsonObject> {
    val roles =
      mapOf(
        "${prefix}ColorRole" to enumDef("A ColorScheme role.", catalog.colorRoles),
        "${prefix}TypeRole" to enumDef("A Typography role.", catalog.typeRoles.keys.toList()),
      )
    if (catalog !== M3ExpressiveCatalog) return roles
    return roles +
      mapOf(
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
  }

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
                obj("enum" to strings("primary", *roleKebabs { it.colorRoles }.toTypedArray())),
              "additionalProperties" to
                obj("type" to "string", "pattern" to DesignSystemResolver.HEX.pattern),
            ),
          "typography" to
            obj(
              "type" to "object",
              "propertyNames" to
                obj("enum" to strings(*roleKebabs { it.typeRoles.keys.toList() }.toTypedArray())),
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

  /**
   * The kebab-case role names of every catalog, each once: a theme serves whichever one it meets.
   */
  private fun roleKebabs(roles: (Catalog) -> List<String>): List<String> =
    Catalogs.all.flatMap(roles).map { it.camelToKebab() }.distinct()

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
                  "enum" to JsonArray(BundledFont.entries.map { JsonPrimitive(it.family) }),
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
              "type" to ref("#/\$defs/AnyTypeRole"),
              "color" to ref("#/\$defs/AnyColorRole"),
              "container" to ref("#/\$defs/AnyColorRole"),
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

  private fun screenDef(catalog: Catalog, prefix: String): JsonObject =
    obj(
      "type" to "object",
      "description" to
        "The body of an A2UI createSurface message, against the ${catalog.key} catalog.",
      "properties" to
        obj(
          "catalogId" to obj("const" to catalog.id),
          "dataModel" to obj("description" to "Sample data that bindings read."),
          "components" to
            obj(
              "type" to "array",
              "description" to "A flat list with exactly one component whose id is root.",
              "minItems" to 1,
              "items" to ref("#/\$defs/${prefix}anyComponent"),
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

  /** The design guidelines an agent reads with [catalog]. */
  fun instructions(catalog: Catalog): String =
    if (catalog === M3ExpressiveCatalog) INSTRUCTIONS else WEAR_INSTRUCTIONS

  internal val INSTRUCTIONS =
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

  internal val WEAR_INSTRUCTIONS =
    """
    Design guidelines for the wear catalog.

    1. A proposal draws from one catalog: every screen names the wear catalog id, and a watch proposal has no `navigation.panes`.
    2. Every prop that names a color or type takes a Wear role name, never a hex value. Wear's colors are its own ColorScheme (it has `primaryDim` and `surfaceContainerHigh`, but no `surface`), and its type roles include `numeral*` for glanceable numbers and `display*`. Use a `style` from the design system's `material.styles` to bundle tokens.
    3. Components reference children by id. A screen's root is usually a `Screen`: its `children` are the rows of a scrolling list (or a fixed stack with `scroll: false`), and its `edgeButton` is the one EdgeButton at the bottom edge. Overlays (`AlertDialog`, `ConfirmationDialog`) stay closed until an `open` action names them.
    4. Interaction is renderer-only: `navigate`, `back`, `open` and `dismiss`. Event actions do not exist.
    5. The display is round and always dark. Keep content inside the circle: a full-width `Button` in a `Screen` list scales and fades toward the edge, but a fixed `Column` that reaches the corners is clipped.
    6. Every tappable control needs a 48 dp touch target. Give every icon-only control a `contentDescription`.
    7. Use `sizeClass` with the `large` key to adapt a component on displays 225 dp and wider.
    """
      .trimIndent()
}
