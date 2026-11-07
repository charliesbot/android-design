package bot.charlies.androiddesign.preview.domain.validation

import bot.charlies.androiddesign.preview.domain.BadgeContent
import bot.charlies.androiddesign.preview.domain.DatePickerSpec
import bot.charlies.androiddesign.preview.domain.DialogSpec
import bot.charlies.androiddesign.preview.domain.MenuSpec
import bot.charlies.androiddesign.preview.domain.OverlayPlacement
import bot.charlies.androiddesign.preview.domain.SearchBars
import bot.charlies.androiddesign.preview.domain.SearchSpec
import bot.charlies.androiddesign.preview.domain.TimePickerSpec
import bot.charlies.androiddesign.preview.domain.TooltipSpec
import bot.charlies.androiddesign.preview.domain.export.ChildItems
import bot.charlies.androiddesign.preview.domain.export.childItems
import bot.charlies.androiddesign.preview.domain.validation.Dynamic.asString
import bot.charlies.androiddesign.preview.domain.validation.Dynamic.isBoolean
import bot.charlies.androiddesign.preview.domain.validation.Dynamic.isDynamic
import bot.charlies.androiddesign.preview.domain.validation.Dynamic.isNumber
import bot.charlies.androiddesign.preview.domain.validation.Dynamic.number
import bot.charlies.androiddesign.preview.domain.validation.Dynamic.string
import bot.charlies.androiddesign.preview.model.ActionFunction
import bot.charlies.androiddesign.preview.model.Catalog
import bot.charlies.androiddesign.preview.model.Catalogs
import bot.charlies.androiddesign.preview.model.ComponentDef
import bot.charlies.androiddesign.preview.model.ComponentNode
import bot.charlies.androiddesign.preview.model.DesignSystem
import bot.charlies.androiddesign.preview.model.PropDef
import bot.charlies.androiddesign.preview.model.PropKind
import bot.charlies.androiddesign.preview.model.Proposal
import bot.charlies.androiddesign.preview.model.Screen
import bot.charlies.androiddesign.preview.model.SizeClass
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

/**
 * Validates one screen of a proposal against [catalog], the one it names, and the merged design
 * system. [proposalCatalog] is the one the proposal as a whole draws from: a screen that names
 * another is reported.
 */
internal class ScreenValidator(
  private val sink: ErrorSink,
  private val proposal: Proposal,
  private val design: DesignSystem,
  private val screenId: String,
  private val screen: Screen,
  private val catalog: Catalog,
  private val proposalCatalog: Catalog,
) {
  private val root = "/screens".child(screenId)
  private val componentsPath = root.child("components")
  private val byId: Map<String, ComponentNode> =
    screen.components.filter { it.id.isNotEmpty() }.reversed().associateBy { it.id }

  fun validate() {
    if (!IDENTIFIER.matches(screenId)) {
      sink.add(root, "screen id '$screenId' must match ${IDENTIFIER.pattern}")
    }
    validateCatalogId()
    validateIdentity()
    screen.components.forEachIndexed { index, node ->
      val def = catalog.find(node.component) ?: return@forEachIndexed
      val path = componentsPath.child(index)
      validateProps(path, def, node.props, overrides = false)
    }
    validateSnackbarHost()
    validateSearch()
    validateSelections()
    validateDialogsAndPickers()
    detectCycles()
  }

  /**
   * The selection rules of radio groups and single-select menu items ([SelectionRules]), checked on
   * the props in effect at each size class: a child hidden at a size does not count there, and a
   * `sizeClass` override of `selected`, `role` or the child list counts where it applies. A group
   * with a child whose value comes from data cannot be judged, so it is skipped.
   */
  private fun validateSelections() {
    screen.components.forEachIndexed { index, node ->
      val path = componentsPath.child(index)
      when (node.component) {
        "RadioGroup" ->
          reportBySize(path.child("children")) { size ->
            val members = membersAt(node, "children", size) ?: return@reportBySize null
            val selected = members.map { selectedAt(it, size) ?: return@reportBySize null }
            SelectionRules.radioGroupProblem(selected.count { it })
          }
        "Menu",
        "MenuGroup" ->
          reportBySize(path.child("items")) { size ->
            val members = membersAt(node, "items", size) ?: return@reportBySize null
            // A menu's own items are one group; its MenuGroups are checked on their own.
            val singles = members.filter {
              it.component == "MenuItem" && roleAt(it, size) == MenuSpec.Role.Single
            }
            if (singles.isEmpty()) return@reportBySize null
            val selected = singles.map { selectedAt(it, size) ?: return@reportBySize null }
            SelectionRules.singleSelectProblem(selected.count { it })
          }
      }
      if (node.component == "Menu") validateMenuLayout(path, node)
    }
  }

  /**
   * A menu holds items or groups, not both, at any of its size classes together: items beside
   * groups would sit outside any group, and the menu would change layout with the width.
   */
  private fun validateMenuLayout(path: String, node: ComponentNode) {
    if (MenuSpec.layout(node, byId) == MenuSpec.Layout.Mixed) {
      sink.add(path.child("items"), "a Menu holds MenuItems or MenuGroups, not both")
    }
  }

  /** Runs [problem] at every size class and reports each distinct message once at [path]. */
  private fun reportBySize(path: String, problem: (SizeClass) -> String?) {
    val sizes = linkedMapOf<String, MutableList<SizeClass>>()
    SizeClass.entries.forEach { size ->
      problem(size)?.let { sizes.getOrPut(it) { mutableListOf() } += size }
    }
    for ((message, at) in sizes) {
      val where =
        if (at.size == SizeClass.entries.size) "" else " at ${at.joinToString { it.wire }}"
      sink.add(path, message + where)
    }
  }

  /** The props of [node] at [size]: the base ones with the size class overrides laid on top. */
  private fun propsAt(node: ComponentNode, size: SizeClass): Map<String, JsonElement> =
    node.props + node.sizeOverridesAt(size)

  /**
   * The children of [key] of [node] that show at [size], or null when the list is a template or
   * names something that is not a component.
   */
  private fun membersAt(node: ComponentNode, key: String, size: SizeClass): List<ComponentNode>? {
    val items = propsAt(node, size)[key].childItems() as? ChildItems.Listed ?: return null
    return items.ids.map { byId[it] ?: return null }.filter { it.isShownAt(size) }
  }

  /** Whether [node] is selected at [size], or null when its `selected` comes from data. */
  private fun selectedAt(node: ComponentNode, size: SizeClass): Boolean? {
    val value = propsAt(node, size)["selected"] ?: return false
    return (value as? JsonPrimitive)?.takeIf { !it.isString }?.content?.toBooleanStrictOrNull()
  }

  private fun roleAt(node: ComponentNode, size: SizeClass): MenuSpec.Role =
    MenuSpec.role((propsAt(node, size)["role"] as? JsonPrimitive)?.takeIf { it.isString }?.content)

  /** How many components a listed `ChildList` prop holds; null for a template or no value. */
  private fun listedCount(props: JsonObject, key: String): Int? =
    (props[key].childItems() as? ChildItems.Listed)?.ids?.size

  /**
   * Material's search rules. A search bar has "one or two trailing icons or icon buttons", and an
   * avatar combines "with up to one other trailing icon button". A search app bar's actions are
   * checked per size class by [validateCountLimits]. A search view opens from the one bar that
   * names it.
   */
  private fun validateSearch() {
    val holders = mutableMapOf<String, MutableList<String>>()
    screen.components.forEachIndexed { index, node ->
      val path = componentsPath.child(index)
      val props = node.props
      when (node.component) {
        "TopAppBar" -> validateSearchOnlyProps(path, node)
      }
      if (node.component == "SearchBar" || node.component == "TopAppBar") {
        props.string("expanded")?.let { holders.getOrPut(it) { mutableListOf() }.add(node.id) }
      }
    }
    screen.components.forEachIndexed { index, node ->
      if (node.component != "SearchView") return@forEachIndexed
      val bars = holders[node.id].orEmpty()
      val path = componentsPath.child(index).child("component")
      when {
        bars.isEmpty() ->
          sink.add(
            path,
            "a SearchView opens from the expanded prop of a SearchBar or a search app bar, " +
              "and nothing names '${node.id}'",
          )
        bars.size > 1 ->
          sink.add(path, "'${node.id}' is the expanded view of ${bars.joinToString(" and ")}")
      }
    }
  }

  /** `avatar` and `expanded` belong to a search app bar, at some size class at least. */
  private fun validateSearchOnlyProps(path: String, node: ComponentNode) {
    if (SearchBars.isSearchAppBar(node)) return
    for (key in listOf("avatar", "expanded")) {
      if (key in node.props) sink.add(path.child(key), "$key is for a TopAppBar of size search")
    }
  }

  /**
   * Material's limits that count children, checked on the props in effect at one size class, so a
   * `sizeClass` override cannot step around them. [changed] is the keys the object at [path] sets,
   * or null for the base props, which hold at compact: only a check that involves a changed key is
   * reported, where it was written.
   *
   * A search bar has "one or two trailing icons or icon buttons", and an avatar combines "with up
   * to one other trailing icon button". A search app bar has "up to two trailing icons on mobile",
   * an avatar in addition, and "up to four trailing icons on larger screens". A rich tooltip has
   * "up to two text buttons", and only a rich tooltip has a subhead or buttons.
   */
  private fun validateCountLimits(
    path: String,
    def: ComponentDef,
    effective: JsonObject,
    size: SizeClass,
    changed: Set<String>?,
  ) {
    fun touched(vararg keys: String) = changed == null || keys.any { it in changed }
    when {
      def.name == "SearchBar" && touched("trailing", "avatar") -> {
        val trailing =
          (listedCount(effective, "trailing") ?: 0) + if ("avatar" in effective) 1 else 0
        if (trailing > SearchSpec.MAX_BAR_TRAILING) {
          sink.add(
            path.child(if (touched("trailing")) "trailing" else "avatar"),
            "a search bar has at most ${SearchSpec.MAX_BAR_TRAILING} trailing elements, " +
              "an avatar counting as one, got $trailing",
          )
        }
      }
      SearchBars.isSearchAppBar(def.name, effective) && touched("actions", "size") -> {
        val count = listedCount(effective, "actions") ?: return
        val large = size != SizeClass.Compact
        val limit =
          if (large) SearchSpec.MAX_APP_BAR_ACTIONS_LARGE
          else SearchSpec.MAX_APP_BAR_ACTIONS_COMPACT
        if (count > limit) {
          sink.add(
            path.child(if (touched("actions")) "actions" else "size"),
            "a search app bar has at most $limit trailing icon buttons " +
              (if (large) "on larger screens" else "on compact screens, four only on larger ones") +
              ", got $count",
          )
        }
      }
      def.name == "Tooltip" -> {
        if (effective.string("variant") != "rich") {
          for (key in listOf("title", "actions")) {
            if (key in effective && touched(key, "variant")) {
              sink.add(
                path.child(if (touched(key)) key else "variant"),
                "$key is for a rich tooltip",
              )
            }
          }
        }
        val actions = listedCount(effective, "actions") ?: 0
        if (actions > TooltipSpec.MAX_ACTIONS && touched("actions")) {
          sink.add(
            path.child("actions"),
            "a rich tooltip has at most ${TooltipSpec.MAX_ACTIONS} text buttons, got $actions",
          )
        }
      }
    }
  }

  /** A snackbar draws in a Scaffold's snackbar slot, so the screen needs a Scaffold to hold it. */
  private fun validateSnackbarHost() {
    if (OverlayPlacement(screen).snackbarScaffold != null) return
    screen.components.forEachIndexed { index, node ->
      if (node.component == "Snackbar") {
        sink.add(
          componentsPath.child(index).child("component"),
          "a Snackbar draws in a Scaffold's snackbar slot, and this screen has no Scaffold",
        )
      }
    }
  }

  /**
   * The rules of dialogs and pickers, checked on the props in effect at each size class where the
   * component is shown, so a `sizeClass` override cannot step around them and the previewer, the
   * exporter and this agree on what is drawn.
   *
   * Material: full-screen dialogs "are for compact breakpoints only", and "for medium and expanded
   * breakpoints, use a basic dialog". A full-screen date picker is only "recommended" on compact
   * windows, so at medium and wider it is a note from the finding rules, not an error here. A date
   * or time is written out in the proposal (never bound to data), because a picker needs the date
   * it opens on and a preview must not read the clock. This is a validator rule, not a finding: a
   * finding reads the rendered tree, which does not say which variant a node is.
   */
  private fun validateDialogsAndPickers() {
    screen.components.forEachIndexed { index, node ->
      if (node.component !in PICKER_OVERLAYS) return@forEachIndexed
      val path = componentsPath.child(index)
      val problems = linkedMapOf<Pair<String, String>, MutableList<SizeClass>>()
      SizeClass.entries.filter(node::isShownAt).forEach { size ->
        pickerProblems(node, propsAt(node, size), size).forEach { (key, message) ->
          problems.getOrPut(path.child(key) to message) { mutableListOf() } += size
        }
      }
      for ((where, sizes) in problems) {
        val at =
          if (sizes.size == SizeClass.entries.size) "" else " at ${sizes.joinToString { it.wire }}"
        sink.add(where.first, where.second + at)
      }
    }
  }

  /** The problems of [node] with [props] at [size], each with the prop it is about. */
  private fun pickerProblems(
    node: ComponentNode,
    props: Map<String, JsonElement>,
    size: SizeClass,
  ): List<Pair<String, String>> = buildList {
    fun has(key: String) = key in props
    fun text(key: String) = (props[key] as? JsonPrimitive)?.takeIf { it.isString }?.content
    val variant = text("variant")
    val compact = size == SizeClass.Compact
    when (node.component) {
      "Dialog" -> {
        val full = DialogSpec.variant(variant) == DialogSpec.Variant.FullScreen
        if (full && !compact) {
          add(
            "variant" to
              "a full-screen dialog is for compact windows only; use a basic dialog from medium " +
                "up (override variant in sizeClass)",
          )
        }
        if (full) {
          for (key in listOf("text", "dismiss")) {
            if (has(key)) add(key to "$key is for a basic dialog")
          }
        } else if (has("content")) {
          add("content" to "content is for a full-screen dialog")
        }
      }
      "DatePicker" -> {
        val range = (props["range"] as? JsonPrimitive)?.content == "true"
        if (variant == DatePickerSpec.Variant.Docked.wire && !has("anchor")) {
          add("variant" to "a docked date picker opens below a TextField, named in anchor")
        }
        val own = if (range) listOf("start", "end") else listOf("selected")
        val other = if (range) listOf("selected") else listOf("start", "end")
        other.filter(::has).forEach {
          add(it to "$it is for a ${if (range) "single date" else "date range"} picker")
        }
        if (own.first() !in props) {
          add(own.first() to "a ${if (range) "date range" else "date"} picker needs ${own.first()}")
        }
        val dates = own.filter(::has).associateWith { key -> pickedDate(props[key]) }
        dates.forEach { (key, date) ->
          if (date == null) add(key to DATE_MESSAGE)
        }
        val start = dates["start"]
        val end = dates["end"]
        if (range && start != null && end != null && end < start) {
          add("end" to "end must not be before start")
        }
      }
      "TimePicker" -> {
        val value = props["time"]
        if (
          value != null &&
            TimePickerSpec.time((value as? JsonPrimitive)?.takeIf { it.isString }?.content) == null
        ) {
          add("time" to "time must be written as HH:mm (00:00 to 23:59), not bound to data")
        }
      }
    }
  }

  /** The date [value] writes, or null when it is not a date literal. */
  private fun pickedDate(value: JsonElement?): java.time.LocalDate? =
    DatePickerSpec.date((value as? JsonPrimitive)?.takeIf { it.isString }?.content)

  private fun validateCatalogId() {
    val path = root.child("catalogId")
    when {
      Catalogs.byId(screen.catalogId) == null ->
        sink.add(
          path,
          "catalogId must be a catalog: ${Catalogs.all.joinToString(" or ") { "${it.key} (${it.id})" }}",
        )
      screen.catalogId != proposalCatalog.id ->
        sink.add(
          path,
          "a proposal draws from one catalog: this screen uses the ${catalog.key} catalog, " +
            "but the rest of the proposal uses ${proposalCatalog.key} (${proposalCatalog.id})",
        )
    }
  }

  private fun validateIdentity() {
    val seen = mutableSetOf<String>()
    screen.components.forEachIndexed { index, node ->
      val path = componentsPath.child(index)
      when {
        node.id.isEmpty() -> sink.add(path.child("id"), "component is missing an id")
        !IDENTIFIER.matches(node.id) ->
          sink.add(
            path.child("id"),
            "id '${node.id}' must be an identifier matching ${IDENTIFIER.pattern}",
          )
        !seen.add(node.id) -> sink.add(path.child("id"), "duplicate id '${node.id}'")
      }
      when {
        node.component.isEmpty() ->
          sink.add(path.child("component"), "component is missing its type")
        catalog.find(node.component) == null ->
          sink.add(path.child("component"), "unknown component '${node.component}'")
      }
    }
    if (screen.components.none { it.id == ComponentNode.ROOT_ID }) {
      sink.add(componentsPath, "a screen needs exactly one component with id \"root\"")
    }
  }

  private fun validateProps(
    path: String,
    def: ComponentDef,
    props: JsonObject,
    overrides: Boolean,
  ) {
    val declared = (def.props + catalog.commonProps).associateBy { it.name }
    val showIn = catalog.showInSizes.isNotEmpty()
    for ((key, value) in props) {
      when {
        key == Catalog.SHOW_IN && showIn && !overrides -> validateShowIn(path.child(key), value)
        key == Catalog.SIZE_CLASS && !overrides ->
          validateSizeClass(path.child(key), def, value, props)
        key == Catalog.STYLE -> validateStyleName(path.child(key), value)
        else -> {
          val prop = declared[key]
          if (prop == null) {
            val known =
              (declared.keys +
                  listOfNotNull(Catalog.SHOW_IN.takeIf { showIn }) +
                  Catalog.SIZE_CLASS +
                  Catalog.STYLE)
                .sorted()
                .joinToString()
            sink.add(path.child(key), "unknown prop '$key' on ${def.name} (known: $known)")
          } else {
            if (overrides && key == ANCHOR && def.name in ANCHORED_OVERLAYS) {
              sink.add(
                path.child(key),
                "the anchor of a ${def.name} is part of where it is drawn, so it goes in the " +
                  "base props, not in a size class",
              )
            }
            validateValue(path.child(key), prop, value)
          }
        }
      }
    }
    if (!overrides) {
      for (prop in def.props) {
        if (prop.required && prop.name !in props) {
          sink.add(path, "${def.name} requires prop '${prop.name}'")
        }
      }
      validateRelations(path, def, props, changed = null)
      validateCountLimits(path, def, props, SizeClass.Compact, changed = null)
    }
  }

  /**
   * Checks props of one component against each other: a slider's range runs upward and holds its
   * value. [props] are the ones in effect (the size class overrides laid over the base props),
   * [changed] are the keys the object at [path] sets, or null for the base props. Only a check that
   * involves a changed key is reported, so one mistake is reported once, where it is written.
   */
  private fun validateRelations(
    path: String,
    def: ComponentDef,
    props: JsonObject,
    changed: Set<String>?,
  ) {
    fun touched(vararg keys: String) = changed == null || keys.any { it in changed }
    /** Where to report a problem between [first] and the range: the first if it is set here. */
    fun at(first: String) = path.child(if (touched(first)) first else "range")
    if (def.name == "EdgeButton") validateEdgeButtonContent(path, props, changed)
    if (def.name !in RANGE_COMPONENTS) return
    val (min, max) = rangeOf(props) ?: return
    if (min >= max) {
      if (touched("range")) sink.add(path.child("range"), rangeOrderMessage(min, max))
      return
    }
    if (def.name != "RangeSlider") {
      val value = props["value"]?.number() ?: return
      if (value !in min..max && touched("value", "range")) {
        sink.add(
          at("value"),
          "value ${format(value)} is outside the range [${format(min)}, ${format(max)}]",
        )
      }
    } else {
      val (start, end) = numbersOf(props["value"]) ?: return
      if (!touched("value", "range")) return
      when {
        start > end ->
          sink.add(path.child("value"), "start ${format(start)} must not exceed end ${format(end)}")
        start < min || end > max ->
          sink.add(
            at("value"),
            "value [${format(start)}, ${format(end)}] is outside the range [${format(min)}, ${format(max)}]",
          )
      }
    }
  }

  /**
   * The Wear API doc's content slot of an edge button is "Either an Icon or Text": exactly one of
   * `icon` and `label`, never both, and a button with neither draws nothing.
   */
  private fun validateEdgeButtonContent(path: String, props: JsonObject, changed: Set<String>?) {
    if (changed != null && "icon" !in changed && "label" !in changed) return
    if (("icon" in props) == ("label" in props)) {
      sink.add(
        path,
        "an EdgeButton takes exactly one of icon or label: its content is \"Either an Icon or " +
          "Text\" in the Wear Compose API",
      )
    }
  }

  /** The `range` prop as two numbers; [0, 1] when absent; null when it is not two numbers. */
  private fun rangeOf(props: JsonObject): Pair<Double, Double>? =
    if ("range" !in props) 0.0 to 1.0 else numbersOf(props["range"])

  private fun numbersOf(value: JsonElement?): Pair<Double, Double>? {
    val array = value as? JsonArray ?: return null
    if (array.size != 2) return null
    val first = array[0].number() ?: return null
    val second = array[1].number() ?: return null
    return first to second
  }

  private fun rangeOrderMessage(min: Double, max: Double) =
    "range min must be less than max, got [${format(min)}, ${format(max)}]"

  private fun format(value: Double): String =
    if (value == Math.rint(value) && kotlin.math.abs(value) < 1e15) value.toLong().toString()
    else value.toString()

  private fun validateShowIn(path: String, value: JsonElement) {
    val array = value as? JsonArray
    if (array == null) {
      sink.add(path, "showIn must be an array of size classes")
      return
    }
    array.forEachIndexed { index, item ->
      val name = item.asString()
      if (name == null || SizeClass.fromWire(name) == null) {
        sink.add(
          path.child(index),
          "'${name ?: item}' is not a size class (compact, medium, expanded)",
        )
      }
    }
  }

  private fun validateSizeClass(
    path: String,
    def: ComponentDef,
    value: JsonElement,
    base: JsonObject,
  ) {
    val obj = value as? JsonObject
    val keys = catalog.sizeBreakpoints.map { it.key }
    if (obj == null) {
      sink.add(path, "sizeClass must be an object keyed by ${keys.joinToString(" or ")}")
      return
    }
    for ((key, overrides) in obj) {
      val breakpoint = catalog.sizeBreakpoints.firstOrNull { it.key == key }
      if (breakpoint == null) {
        sink.add(
          path.child(key),
          "'$key' is not an overridable size class (${keys.joinToString(", ")})",
        )
        continue
      }
      val props = overrides as? JsonObject
      if (props == null) {
        sink.add(path.child(key), "a size class override must be an object of props")
        continue
      }
      validateProps(path.child(key), def, props, overrides = true)
      // The props at this size are the base ones with the overrides up to it laid on top.
      val effective =
        LinkedHashMap(base).apply {
          putAll(
            ComponentNode("", def.name, base)
              .sizeOverridesAt(catalog.sizeBreakpoints, breakpoint.minWidthDp),
          )
        }
      validateRelations(path.child(key), def, JsonObject(effective), changed = props.keys)
      SizeClass.fromWire(key)?.let {
        validateCountLimits(path.child(key), def, JsonObject(effective), it, props.keys)
      }
    }
  }

  private fun validateStyleName(path: String, value: JsonElement) {
    val name = value.asString()
    val style = name?.let(design.styles::get)
    if (name == null || style == null) {
      sink.add(path, "style '${name ?: value}' is not defined in material.styles")
      return
    }
    // A style may come from a DESIGN.md shared with another surface: its roles must exist here.
    for ((kind, role, known) in
      listOf(
        Triple("type", style.type, catalog.typeRoles.keys),
        Triple("color", style.color, catalog.colorRoles.toSet()),
        Triple("container", style.container, catalog.colorRoles.toSet()),
      )) {
      if (role != null && role !in known) {
        sink.add(
          path,
          "style '$name' sets $kind '$role', which is not a ${catalog.key} role; " +
            "use a style written for this catalog",
        )
      }
    }
  }

  private fun validateValue(path: String, prop: PropDef, value: JsonElement) {
    when (val kind = prop.kind) {
      PropKind.Child -> validateReference(path, prop, value)
      PropKind.ChildList -> validateChildList(path, prop, value)
      PropKind.Text,
      PropKind.Icon,
      PropKind.Image -> validateText(path, kind, value)
      PropKind.Bool ->
        if (!value.isBoolean() && !isDynamic(value)) sink.add(path, "must be true or false")
      is PropKind.Number -> validateNumber(path, kind, value)
      PropKind.NumberRange -> validateRange(path, value)
      is PropKind.Enum ->
        if (value.asString() !in kind.values) {
          sink.add(
            path,
            "'${value.asString() ?: value}' is not one of: ${kind.values.joinToString()}",
          )
        }
      PropKind.ColorRole ->
        if (value.asString() !in catalog.colorRoles) {
          sink.add(
            path,
            "'${value.asString() ?: value}' is not a color role (such as primaryContainer)",
          )
        }
      PropKind.TypeRole ->
        if (value.asString() !in catalog.typeRoles) {
          sink.add(path, "'${value.asString() ?: value}' is not a type role (such as bodyLarge)")
        }
      PropKind.Shape ->
        if (value.asString()?.let(ThemeValidator::isShape) != true) {
          sink.add(
            path,
            "'${value.asString() ?: value}' is not a shape token (such as large) or a MaterialShapes name",
          )
        }
      PropKind.Spacing ->
        if (value.asString() !in design.spacing) {
          sink.add(
            path,
            "'${value.asString() ?: value}' is not a spacing name: ${design.spacing.keys.joinToString()}",
          )
        }
      PropKind.Badge -> validateBadge(path, value)
      PropKind.Action -> validateAction(path, value)
      PropKind.Binding ->
        if (!Dynamic.isBinding(value))
          sink.add(path, "must be a binding such as {\"@path\": \"/items\"}")
    }
  }

  private fun validateBadge(path: String, value: JsonElement) {
    if (isDynamic(value)) return
    val primitive = value as? JsonPrimitive
    if (
      primitive == null || (!value.isBoolean() && !value.isNumber() && value.asString() == null)
    ) {
      sink.add(path, "must be true, false, a number, a string or a binding")
      return
    }
    val label = (BadgeContent.of(primitive) as? BadgeContent.Large)?.label ?: return
    if (label.length > PropKind.BADGE_MAX_CHARACTERS) {
      sink.add(
        path,
        "a badge label holds at most ${PropKind.BADGE_MAX_CHARACTERS} characters, " +
          "a + included (such as \"99+\"), got '$label'",
      )
    }
  }

  private fun validateText(path: String, kind: PropKind, value: JsonElement) {
    val text = value.asString()
    when {
      text == null && !isDynamic(value) -> sink.add(path, "must be a string or a binding")
      text != null && kind == PropKind.Icon && !ICON_NAME.matches(text) ->
        sink.add(
          path,
          "'$text' is not a Material Symbols name (lowercase letters, digits and underscores)",
        )
      text != null && kind == PropKind.Image && text.isEmpty() ->
        sink.add(path, "must not be empty")
    }
  }

  private fun validateNumber(path: String, kind: PropKind.Number, value: JsonElement) {
    if (isDynamic(value)) return
    val number = value.number()
    val bounds = kind.bounds
    when {
      number == null -> sink.add(path, "must be a number")
      bounds != null && number !in bounds ->
        sink.add(path, "must be ${bounds.description}, got ${format(number)}")
    }
  }

  private fun validateRange(path: String, value: JsonElement) {
    val array = value as? JsonArray
    if (array == null || array.size != 2 || !array.all { it.isNumber() }) {
      sink.add(path, "must be an array of two numbers")
    }
  }

  private fun validateReference(path: String, prop: PropDef, value: JsonElement) {
    val id = value.asString()
    if (id == null) {
      sink.add(path, "must be the id of a component")
      return
    }
    val target = byId[id]
    val allowed = prop.allowedChildren
    when {
      target == null -> sink.add(path, "references unknown component '$id'")
      allowed != null && target.component !in allowed ->
        sink.add(
          path,
          "'$id' is a ${target.component}, but ${prop.name} takes ${allowed.joinToString(" or ")}",
        )
    }
  }

  private fun validateChildList(path: String, prop: PropDef, value: JsonElement) {
    when (value) {
      is JsonArray ->
        value.forEachIndexed { index, item -> validateReference(path.child(index), prop, item) }
      is JsonObject -> {
        validateReference(
          path.child("componentId"),
          prop,
          value["componentId"] ?: JsonObject(emptyMap()),
        )
        if (value.string("path") == null)
          sink.add(path.child("path"), "a template needs a data model path")
        value.keys
          .filter { it != "componentId" && it != "path" }
          .forEach {
            sink.add(path.child(it), "unknown key '$it' in a child template")
          }
      }
      else -> sink.add(path, "must be an array of component ids or a template {componentId, path}")
    }
  }

  private fun validateAction(path: String, value: JsonElement) {
    val action = value as? JsonObject
    if (action == null) {
      sink.add(path, "must be an object {\"functionCall\": {...}}")
      return
    }
    if ("event" in action) {
      sink.add(
        path.child("event"),
        "event actions are not supported: the renderer never calls the agent; use functionCall",
      )
    }
    val call = action["functionCall"] as? JsonObject
    if (call == null) {
      if ("event" !in action) sink.add(path, "an action needs a functionCall")
      return
    }
    val callPath = path.child("functionCall")
    val name = call.string("@call")
    val function = name?.let(ActionFunction::fromWire)
    if (function == null) {
      sink.add(
        callPath.child("@call"),
        "unknown function '${name ?: ""}' (use ${ActionFunction.entries.joinToString { it.wire }})",
      )
      return
    }
    val argName = function.argument ?: return
    val argsPath = callPath.child("args").child(argName)
    val argValue = (call["args"] as? JsonObject)?.string(argName)
    if (argValue == null) {
      sink.add(argsPath, "${function.wire} needs the $argName as a string")
      return
    }
    when (function) {
      ActionFunction.Navigate ->
        if (argValue !in proposal.screens) sink.add(argsPath, "screen '$argValue' does not exist")
      ActionFunction.Open -> {
        val target = byId[argValue]
        when {
          target == null ->
            sink.add(argsPath, "overlay '$argValue' is not a component of this screen")
          target.component !in catalog.overlayNames ->
            sink.add(
              argsPath,
              "'$argValue' is a ${target.component}; an overlay must be a " +
                catalog.overlayNames.joinToString(", "),
            )
        }
      }
      ActionFunction.Back,
      ActionFunction.Dismiss -> Unit
    }
  }

  /**
   * A component that contains itself would make the renderer recurse forever. A `Menu.anchor` names
   * the component the menu hangs off; it is a reference, not a child, so it never forms a cycle.
   */
  private fun detectCycles() {
    val childIds = screen.components.associate { node -> node.id to referencedIds(node) }
    val done = mutableSetOf<String>()
    val reported = mutableSetOf<String>()
    fun visit(id: String, stack: List<String>) {
      if (id in done) return
      val onStack = stack.indexOf(id)
      if (onStack >= 0) {
        val current = stack.last()
        if (reported.add(current)) {
          val index = screen.components.indexOfFirst { it.id == current }
          sink.add(
            componentsPath.child(index),
            "cycle: ${(stack.drop(onStack) + id).joinToString(" -> ")}",
          )
        }
        return
      }
      for (next in childIds[id].orEmpty()) visit(next, stack + id)
      done += id
    }
    screen.components.forEach { visit(it.id, emptyList()) }
  }

  private fun referencedIds(node: ComponentNode): List<String> {
    val def = catalog.find(node.component) ?: return emptyList()
    val sources = buildList {
      add(node.props)
      (node.props[Catalog.SIZE_CLASS] as? JsonObject)?.values?.forEach {
        (it as? JsonObject)?.let(::add)
      }
    }
    return sources
      .flatMap { props ->
        def.props.flatMap { prop ->
          if ((node.component == MENU || node.component == TOOLTIP) && prop.name == ANCHOR) {
            return@flatMap emptyList()
          }
          val value = props[prop.name] ?: return@flatMap emptyList()
          when (prop.kind) {
            PropKind.Child -> listOfNotNull(value.asString())
            PropKind.ChildList ->
              when (value) {
                is JsonArray -> value.mapNotNull { it.asString() }
                is JsonObject -> listOfNotNull(value.string("componentId"))
                else -> emptyList()
              }
            else -> emptyList()
          }
        }
      }
      .filter { it in byId }
  }

  companion object {
    val IDENTIFIER = Regex("^[A-Za-z_][A-Za-z0-9_]*$")
    private val ICON_NAME = Regex("^[a-z0-9_]+$")
    /** Components whose `value` and `range` props must agree. */
    private val RANGE_COMPONENTS = setOf("Slider", "RangeSlider", "Stepper")
    /**
     * Overlays drawn beside the component their `anchor` names, which the tree records as parent.
     */
    val ANCHORED_OVERLAYS = setOf("Menu", "Tooltip", "DatePicker")
    private val PICKER_OVERLAYS = setOf("Dialog", "DatePicker", "TimePicker")
    private const val DATE_MESSAGE =
      "a date must be written as YYYY-MM-DD, a real day from 1900 to 2100, not bound to data"
    private const val MENU = "Menu"
    private const val TOOLTIP = "Tooltip"
    private const val ANCHOR = "anchor"
  }
}
