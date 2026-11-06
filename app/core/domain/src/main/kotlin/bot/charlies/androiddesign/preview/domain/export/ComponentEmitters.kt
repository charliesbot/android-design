package bot.charlies.androiddesign.preview.domain.export

import bot.charlies.androiddesign.preview.domain.CarouselSpec
import bot.charlies.androiddesign.preview.model.ComponentNode
import bot.charlies.androiddesign.preview.model.M3ExpressiveCatalog
import bot.charlies.androiddesign.preview.model.SizeClass
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

// The Compose call for each catalog component, from compose-mapping.md. Each function returns the
// statements that draw one component; a component with state declares it first.

private fun ScreenEmitter.m3(name: String): String = file.use(Sym.m3(name))

private fun ScreenEmitter.expressive() = file.optIn(Sym.ExperimentalMaterial3ExpressiveApi)

private fun ScreenEmitter.experimental() = file.optIn(Sym.ExperimentalMaterial3Api)

private fun opt(name: String, value: String?): Arg? = value?.let { arg(name, it) }

private fun opt(name: String, value: Expr?): Arg? = value?.let { arg(name, it) }

private fun ScreenEmitter.calls(callee: String, args: List<Arg?>, body: List<Stmt>? = null): Expr =
  Call(callee, args.filterNotNull(), body?.let { lambda(it) })

/** `Component(args) { body }` as a one-statement list. */
private fun ScreenEmitter.one(callee: String, args: List<Arg?>, body: List<Stmt>? = null) =
  listOf(stmt(calls(callee, args, body)))

internal fun ScreenEmitter.emitComponent(
  node: ComponentNode,
  props: JsonObject,
  place: Place,
): List<Stmt> {
  val menu = placement.menuAt(node.id)?.let(byId::get)
  // The same rule the previewer uses: the catalog says who stretches and who lets its children,
  // read from the props with their bindings resolved against the screen's sample data.
  val fillWidth =
    place.parentStretches &&
      M3ExpressiveCatalog.find(node.component)?.fillsWidth { props.sampleValue(it, place) } == true
  val placed = place.copy(fillWidth = fillWidth)
  val own = dispatch(node, props, if (menu != null) placed.copy(parentType = "Box") else placed)
  if (menu == null || menu.id in placedOverlays) return own
  return besideMenu(menu, own)
}

/** [own] and the [menu] anchored to it, wrapped in a `Box` so the menu opens beside them. */
private fun ScreenEmitter.besideMenu(menu: ComponentNode, own: List<Stmt>): List<Stmt> {
  placedOverlays += menu.id
  return one(file.use(Sym.Box), emptyList(), own + menuStatements(menu))
}

private fun ScreenEmitter.dispatch(node: ComponentNode, p: JsonObject, place: Place): List<Stmt> =
  when (node.component) {
    "Column" -> column(p, place)
    "Row" -> row(p, place)
    "Box" -> box(p, place)
    "Grid" -> grid(p, place)
    "FlexBox" -> flexBox(p, place)
    "Spacer" -> spacer(p, place)
    "Divider" -> divider(p, place)
    "Shape" -> shape(p, place)
    "Text" -> textComponent(p, place)
    "Icon" -> iconComponent(p, place)
    "Image" -> image(node, p, place)
    "Carousel" -> carousel(node, p, place)
    "Scaffold" -> scaffold(p, place)
    "TopAppBar" -> topAppBar(p, place)
    "NavigationBar" -> navigationBar(p, place)
    "NavigationBarItem" -> navigationItem(node, p, place, "ShortNavigationBarItem", false)
    "Tabs" -> tabs(p, place)
    "Tab" -> tab(node, p, place)
    "NavigationRail" -> navigationRail(p, place)
    "NavigationRailItem" -> navigationItem(node, p, place, "WideNavigationRailItem", true)
    "FloatingToolbar" -> floatingToolbar(p, place)
    "Card" -> card(node, p, place)
    "List" -> list(p, place)
    "ListItem" -> listItem(node, p, place)
    "Button" -> button(node, p, place)
    "ToggleButton" -> toggleButton(node, p, place)
    "ButtonGroup" -> buttonGroup(p, place)
    "IconButton" -> iconButton(node, p, place)
    "SplitButton" -> splitButton(node, p, place)
    "FloatingActionButton" -> floatingActionButton(node, p, place)
    "FabMenu" -> fabMenu(node, p, place)
    "FabMenuItem" -> fabMenuItem(node, p, place)
    "AssistChip",
    "FilterChip",
    "InputChip",
    "SuggestionChip" -> chip(node, p, place)
    "ChipGroup" -> chipGroup(node, p, place)
    "Switch" -> switch(node, p, place)
    "Checkbox" -> checkbox(node, p, place)
    "Slider" -> slider(node, p, place)
    "RangeSlider" -> rangeSlider(node, p, place)
    "TextField" -> textField(node, p, place)
    "LoadingIndicator" -> loadingIndicator(p, place)
    "ProgressIndicator" -> progressIndicator(p, place)
    "MenuItem" -> menuItem(node, p, place)
    else -> emptyList() // BottomSheet, Dialog and Menu are written at the end of the screen.
  }

// region shared pieces

/**
 * A style's container as modifier calls: a background, clipped first to the style's shape like the
 * previewer draws it. Empty without a container.
 */
private fun ScreenEmitter.containerBackground(p: JsonObject): Array<String> {
  val container = p.literal(CONTAINER_KEY) ?: return emptyArray()
  val clip = p.literal(STYLE_SHAPE_KEY)?.let { "clip(${shapeToken(it)})" }
  return listOfNotNull(clip, "background(${colorRole(container)})").toTypedArray()
}

private fun ScreenEmitter.painter(name: IconName, filled: Boolean = false): String =
  "${file.use(Sym.painterResource)}(${drawable(name, filled)})"

/** The drawable [name] is: `R.drawable.star`, or a call of the lookup for a bound name. */
private fun ScreenEmitter.drawable(name: IconName, filled: Boolean = false): String =
  when (name) {
    is IconName.Literal -> file.drawable(Naming.snake(name.name) + if (filled) "_filled" else "")
    is IconName.Bound -> "${file.symbolLookup(name.names, filled)}(${name.expr})"
  }

/** `painterResource(if (cond) on else off)`; [on] is the `_filled` drawable when [onFilled]. */
private fun ScreenEmitter.togglePainter(
  cond: String,
  on: IconName,
  off: IconName,
  onFilled: Boolean = false,
): String =
  "${file.use(Sym.painterResource)}(if ($cond) ${drawable(on, onFilled)} else ${drawable(off)})"

/** `Icon(painter, contentDescription = ...)`. */
private fun ScreenEmitter.iconOf(
  painter: String,
  description: Expr? = null,
  vararg extra: Arg?,
): Expr =
  Call(
    file.use(Sym.Icon),
    listOf(pos(painter), arg("contentDescription", description ?: Raw("null"))) +
      extra.filterNotNull(),
  )

/** `Icon(painterResource(R.drawable.name), contentDescription = ...)`. */
private fun ScreenEmitter.icon(
  symbol: IconName,
  description: Expr? = null,
  vararg extra: Arg?,
): Expr = iconOf(painter(symbol), description, *extra)

private fun ScreenEmitter.sizeModifier(size: String): Arg =
  arg("modifier", Chain(file.use(Sym.Modifier), listOf("${useModifier("size")}($size)")))

private fun ScreenEmitter.textOf(value: Expr, vararg named: Arg?): Expr =
  Call(file.use(Sym.Text), listOf(pos(value)) + named.filterNotNull())

/** `argName = { Text(...) }` for the text at [key], or null when there is none. */
private fun ScreenEmitter.textSlot(
  p: JsonObject,
  key: String,
  place: Place,
  argName: String = key,
): Arg? = p.text(key, place)?.let { arg(argName, lambdaOf(textOf(it))) }

private fun ScreenEmitter.semantics(p: JsonObject): String? =
  p.literal("contentDescription")?.let {
    file.use(Sym.contentDescription)
    "${useModifier("semantics")} { contentDescription = ${kotlinString(it)} }"
  }

/**
 * `argName = value` for a Boolean prop passed on as it is: a literal or the bound expression.
 * Nothing when the prop is absent or a literal [omitted], the value Compose already has.
 */
private fun ScreenEmitter.boolArg(
  p: JsonObject,
  key: String,
  place: Place,
  argName: String,
  omitted: Boolean,
): Arg? =
  when (val value = p.boolExpr(key, place)) {
    null,
    omitted.toString() -> null
    else -> arg(argName, value)
  }

private fun ScreenEmitter.enabled(p: JsonObject, place: Place): Arg? =
  boolArg(p, "enabled", place, "enabled", omitted = true)

private fun ScreenEmitter.alignmentName(wire: String): String =
  "${file.use(Sym.Alignment)}.${Naming.pascal(wire)}"

/** `{ name = it }`, followed by [then]: the lambda for a control that changes a state variable. */
private fun assignIt(name: String, then: List<Stmt> = emptyList()): Lambda =
  lambda(listOf<Stmt>(Assign(name, "=", Raw("it"))) + then)

/** The state variable and initial value for something the user can change. */
private fun ScreenEmitter.state(
  node: ComponentNode,
  suffix: String,
  initial: String,
  factory: String = Sym.mutableStateOf,
): Pair<String, Stmt> {
  val name = stateName(node.id, suffix)
  return name to stateVar(name, initial, factory)
}

// endregion

// region layout

private fun ScreenEmitter.column(p: JsonObject, place: Place): List<Stmt> {
  val scroll =
    if (p.sampleFlag("scroll", place) == true)
      "verticalScroll(${file.use(Sym.rememberScrollState)}())"
    else null
  val alignment =
    when (p.literal("horizontalAlignment")) {
      "center" -> "${file.use(Sym.Alignment)}.CenterHorizontally"
      "end" -> "${file.use(Sym.Alignment)}.End"
      else -> null
    }
  return one(
    file.use(Sym.Column),
    listOf(
      modifier(place, p, *containerBackground(p), scroll, fillHeight = place.fillHeight),
      opt("verticalArrangement", arrangement(p, vertical = true)),
      opt("horizontalAlignment", alignment),
    ),
    children(p, "children", place, "Column"),
  )
}

private fun ScreenEmitter.row(p: JsonObject, place: Place): List<Stmt> {
  val alignment =
    when (p.literal("verticalAlignment")) {
      "center" -> "${file.use(Sym.Alignment)}.CenterVertically"
      "bottom" -> "${file.use(Sym.Alignment)}.Bottom"
      else -> null
    }
  return one(
    file.use(Sym.Row),
    listOf(
      modifier(place, p, *containerBackground(p), fillHeight = place.fillHeight),
      opt("horizontalArrangement", arrangement(p, vertical = false)),
      opt("verticalAlignment", alignment),
    ),
    // Stretch is the row's height offered to its Column and Box children, the full-height panes.
    children(p, "children", place, "Row") { child, _, _ ->
      child.copy(fillHeight = p.literal("verticalAlignment") == "stretch")
    },
  )
}

/** `Arrangement` for a column's `verticalArrangement` or a row's `horizontalArrangement`. */
private fun ScreenEmitter.arrangement(p: JsonObject, vertical: Boolean): String? {
  val arrangement = file.use(Sym.Arrangement)
  val spacing = p.literal("spacing")?.let(::spacing)
  val kind = p.literal("arrangement")
  val distribution =
    mapOf(
      "spaceBetween" to "SpaceBetween",
      "spaceAround" to "SpaceAround",
      "spaceEvenly" to "SpaceEvenly",
    )
  distribution[kind]?.let {
    return "$arrangement.$it"
  }
  val alignment = file.use(Sym.Alignment)
  val end = if (vertical) "Bottom" else "End"
  val centered = if (vertical) "CenterVertically" else "CenterHorizontally"
  return when {
    spacing != null && kind == "center" -> "$arrangement.spacedBy($spacing, $alignment.$centered)"
    spacing != null && kind == "end" -> "$arrangement.spacedBy($spacing, $alignment.$end)"
    spacing != null -> "$arrangement.spacedBy($spacing)"
    kind == "center" -> "$arrangement.Center"
    kind == "end" -> "$arrangement.$end"
    else -> null
  }
}

private fun ScreenEmitter.box(p: JsonObject, place: Place): List<Stmt> {
  val alignment = p.nonDefault("Box", "contentAlignment")?.let(::alignmentName)
  return one(
    file.use(Sym.Box),
    listOf(
      modifier(place, p, *containerBackground(p), fillHeight = place.fillHeight),
      opt("contentAlignment", alignment),
    ),
    children(p, "children", place, "Box"),
  )
}

private fun ScreenEmitter.grid(p: JsonObject, place: Place): List<Stmt> {
  file.optIn(Sym.ExperimentalGridApi)
  // `fr` is a member extension of the Grid configuration scope, so it needs no import.
  val config = mutableListOf<Stmt>()
  val columns = p.sampleNumber("Grid", "columns", place)?.toInt()
  val minWidth = p.sampleNumber("Grid", "minColumnWidth", place)
  when {
    columns != null ->
      config += stmt(Call("repeat", listOf(pos("$columns")), lambda(stmt(Raw("column(1.fr)")))))
    minWidth != null -> {
      // The previewer's count: the gap is added to the width and to each column's width.
      val gap = p.literal("spacing")?.let(::spacing)
      val fit =
        if (gap == null) "constraints.maxWidth.toDp() / ${dp(minWidth.toFloat())}"
        else "(constraints.maxWidth.toDp() + $gap) / (${dp(minWidth.toFloat())} + $gap)"
      val most = M3ExpressiveCatalog.MAX_GRID_COLUMNS
      config += Assign("val columns", "=", Raw("($fit).toInt().coerceIn(1, $most)"))
      config += stmt(Call("repeat", listOf(pos("columns")), lambda(stmt(Raw("column(1.fr)")))))
    }
    else -> config += stmt(Raw("column(1.fr)"))
  }
  p.literal("spacing")?.let { config += stmt(Raw("gap(${spacing(it)})")) }
  return one(
    file.use(Sym.Grid),
    listOf(arg("config", lambda(config)), modifier(place, p, *containerBackground(p))),
    children(p, "children", place, "Grid"),
  )
}

private fun ScreenEmitter.flexBox(p: JsonObject, place: Place): List<Stmt> {
  val config = mutableListOf<Stmt>()
  if (p.option("FlexBox", "wrap")) config += stmt(Raw("wrap(${file.use(Sym.FlexWrap)}.Wrap)"))
  p.literal("gap")?.let { config += stmt(Raw("gap(${spacing(it)})")) }
  return one(
    file.use(Sym.FlexBox),
    listOf(
      if (config.isEmpty()) null else arg("config", lambda(config)),
      modifier(place, p, *containerBackground(p)),
    ),
    children(p, "children", place, "FlexBox"),
  )
}

private fun ScreenEmitter.spacer(p: JsonObject, place: Place): List<Stmt> {
  val width = p.literal("width")?.let { "${useModifier("width")}(${spacing(it)})" }
  val height = p.literal("height")?.let { "${useModifier("height")}(${spacing(it)})" }
  val chain = modifierChain(place, listOfNotNull(width, height))
  return one(file.use(Sym.Spacer), listOf(pos(chain)))
}

private fun ScreenEmitter.divider(p: JsonObject, place: Place): List<Stmt> =
  one(
    m3(if (p.sampleFlag("vertical", place) == true) "VerticalDivider" else "HorizontalDivider"),
    listOf(modifier(place, p)),
  )

private fun ScreenEmitter.shape(p: JsonObject, place: Place): List<Stmt> {
  expressive()
  val shape = shapeToken(p.literal("shape") ?: "circle")
  val clip = "${useModifier("clip")}($shape)"
  val fill = p.literal("color")?.let { "background(${colorRole(it)})" }
  return one(
    file.use(Sym.Box),
    listOf(modifier(place, p, clip, fill)),
    child(p, "content", place, "Box"),
  )
}

// endregion

// region content

private fun ScreenEmitter.textComponent(p: JsonObject, place: Place): List<Stmt> {
  val overflow =
    when (p.literal("overflow")) {
      "ellipsis" -> "${file.use(Sym.TextOverflow)}.Ellipsis"
      "clip" -> "${file.use(Sym.TextOverflow)}.Clip"
      else -> null
    }
  val align =
    when (p.literal("align")) {
      "start" -> "${file.use(Sym.TextAlign)}.Start"
      "center" -> "${file.use(Sym.TextAlign)}.Center"
      "end" -> "${file.use(Sym.TextAlign)}.End"
      else -> null
    }
  val text = p.text("text", place) ?: Raw("\"\"")
  return listOf(
    stmt(
      textOf(
        text,
        modifier(place, p),
        opt("color", p.literal("color")?.let(::colorRole)),
        opt("textAlign", align),
        opt("overflow", overflow),
        opt("maxLines", p.sampleNumber("Text", "maxLines", place)?.toInt()?.toString()),
        opt("style", p.literal("type")?.let(::typeRole)),
      ),
    ),
  )
}

private fun ScreenEmitter.iconComponent(p: JsonObject, place: Place): List<Stmt> {
  val size = p.sampleNumber("Icon", "size", place)?.let { dp(it.toFloat()) }
  val parts = listOfNotNull(size?.let { "${useModifier("size")}($it)" })
  val description = p.text("contentDescription", place)
  val name = p.iconName("name", place) ?: IconName.Literal("help")
  val filled = p.boolExpr("filled", place)
  val painter =
    when {
      filled == null || filled == "false" -> painter(name)
      filled == "true" -> painter(name, filled = true)
      else -> togglePainter(filled, name, name, onFilled = true)
    }
  return listOf(
    stmt(
      iconOf(
        painter,
        description,
        modifier(place, p, *parts.toTypedArray()),
        opt("tint", p.literal("color")?.let(::colorRole)),
      ),
    ),
  )
}

/**
 * The drawable of an image: named after the file, or after the node for a `data:` URI. A bound
 * `src` is a call of the lookup over every source the sample data gives it; when several distinct
 * data URIs occur, the node's name is numbered in the order they first appear.
 */
private fun ScreenEmitter.imageDrawable(node: ComponentNode, p: JsonObject, place: Place): String {
  fun symbol(src: String) =
    if (src.startsWith("data:")) node.id else src.substringAfterLast('/').substringBeforeLast('.')
  val bound =
    p.boundText("src", place)
      ?: return file.drawable(Naming.snake(symbol(p.literal("src").orEmpty())))
  val data = bound.values.filter { it.startsWith("data:") }
  val symbols =
    bound.values.associateWith { src ->
      if (src.startsWith("data:") && data.size > 1) "${node.id}_${data.indexOf(src) + 1}"
      else symbol(src)
    }
  return "${file.imageLookup(symbols)}(${bound.expr})"
}

private fun ScreenEmitter.image(node: ComponentNode, p: JsonObject, place: Place): List<Stmt> {
  val drawable = imageDrawable(node, p, place)
  val scale =
    when (p.choice("Image", "fit")) {
      "crop" -> "Crop"
      "fillBounds" -> "FillBounds"
      "fillWidth" -> "FillWidth"
      "fillHeight" -> "FillHeight"
      "inside" -> "Inside"
      else -> "Fit"
    }
  val shape = p.literal("shape")?.let { "${useModifier("clip")}(${shapeToken(it)})" }
  val aspect = p.floatExpr("aspectRatio", place)?.let { "${useModifier("aspectRatio")}($it)" }
  return one(
    file.use(Sym.Image),
    listOf(
      arg("painter", "${file.use(Sym.painterResource)}($drawable)"),
      arg("contentDescription", p.text("contentDescription", place) ?: Raw("null")),
      modifier(place, p, aspect, shape),
      arg("contentScale", "${file.use(Sym.ContentScale)}.$scale"),
    ),
  )
}

private fun ScreenEmitter.carousel(node: ComponentNode, p: JsonObject, place: Place): List<Stmt> {
  experimental()
  val variant = p.choice("Carousel", "variant")
  val items =
    childrenByIndex(p, "children", place, "Carousel") {
      // maskClip is a member of the item scope, so the children clip to the carousel's shape.
      val shape = "${file.use(Sym.MaterialTheme)}.shapes.extraLarge"
      it.copy(leading = listOf("maskClip($shape)"))
    }
  val itemWidth = p.number("Carousel", "itemWidth", place)
  val state = stateName(node.id, "State")
  val remember =
    Assign(
      "val $state",
      "=",
      Call(
        file.use(Sym.carousel("rememberCarouselState")),
        lambda = lambda(stmt(Raw(items.count))),
      ),
    )
  val defaults = file.use(Sym.carousel("CarouselDefaults"))
  val gap = arg("itemSpacing", dp(CarouselSpec.GAP_DP))
  fun padding(horizontal: Float?): Arg {
    val values =
      if (horizontal == null) "${file.use(Sym.PaddingValues)}(${dp(0f)})"
      else "${file.use(Sym.PaddingValues)}(horizontal = ${dp(horizontal)})"
    return arg("contentPadding", values)
  }
  fun carousel(callee: String, modifier: Arg?, vararg args: Arg?): List<Stmt> =
    listOf(
      stmt(
        Call(
          file.use(Sym.carousel(callee)),
          (listOf(arg("state", state), modifier) + args).filterNotNull(),
          Lambda(items.param, items.body),
        ),
      ),
    )
  val modifier = modifier(place, p)
  val body =
    when (variant) {
      "centered-hero" ->
        carousel("HorizontalCenteredHeroCarousel", modifier, gap, padding(CarouselSpec.PADDING_DP))
      "uncontained" ->
        carousel(
          "HorizontalUncontainedCarousel",
          modifier,
          arg("itemWidth", dp(itemWidth.toFloat())),
          gap,
          arg("flingBehavior", "$defaults.noSnapFlingBehavior()"),
          padding(CarouselSpec.PADDING_DP),
        )
      "hero" ->
        wrapWidth(
          modifier,
          carousel(
            "HorizontalMultiBrowseCarousel",
            null,
            arg("preferredItemWidth", "maxWidth - ${dp(CarouselSpec.HERO_RESERVED_DP)}"),
            gap,
            arg("flingBehavior", "$defaults.multiBrowseFlingBehavior($state)"),
            padding(CarouselSpec.PADDING_DP),
          ),
        )
      "full-screen" ->
        wrapWidth(
          modifier,
          carousel(
            "HorizontalUncontainedCarousel",
            null,
            arg("itemWidth", "maxWidth"),
            arg("itemSpacing", dp(CarouselSpec.FULL_SCREEN_GAP_DP)),
            arg("flingBehavior", "$defaults.singleAdvanceFlingBehavior($state)"),
            padding(null),
          ),
        )
      else ->
        carousel(
          "HorizontalMultiBrowseCarousel",
          modifier,
          arg("preferredItemWidth", dp(itemWidth.toFloat())),
          gap,
          arg("flingBehavior", "$defaults.multiBrowseFlingBehavior($state)"),
          padding(CarouselSpec.PADDING_DP),
        )
    }
  return listOf<Stmt>(remember) + body
}

/** [content] inside a `BoxWithConstraints`, whose `maxWidth` is the width the carousel sizes by. */
private fun ScreenEmitter.wrapWidth(modifier: Arg?, content: List<Stmt>): List<Stmt> =
  one(file.use(Sym.BoxWithConstraints), listOf(modifier), content)

// endregion

// region structure

private val LAYOUT_TYPES = setOf("Column", "Row", "Box", "Grid", "FlexBox", "List")

private fun ScreenEmitter.scaffold(p: JsonObject, place: Place): List<Stmt> {
  val statements = mutableListOf<Stmt>()
  val topBar = p.literal("topBar")?.let(byId::get)
  val collapsing =
    topBar?.component == "TopAppBar" && (topBar.props.literal("size") in setOf("medium", "large"))
  if (collapsing) {
    experimental()
    statements +=
      Assign(
        "val scrollBehavior",
        "=",
        Raw("${m3("TopAppBarDefaults")}.exitUntilCollapsedScrollBehavior()"),
      )
  }
  val bottomId = p.literal("bottomBar")
  val bottom = bottomId?.let(byId::get)
  val toolbarOverContent = bottom?.component == "FloatingToolbar"
  val rail = p.literal("navigationRail")
  val inRow = rail != null

  val nested = if (collapsing) "nestedScroll(scrollBehavior.nestedScrollConnection)" else null
  val scaffoldModifier =
    when {
      inRow ->
        modifierArg(
          place.copy(modifierBase = "Modifier"),
          listOfNotNull("weight(1f)", nested?.let(::useModifier)),
        )
      nested != null -> modifierArg(place, listOf(useModifier(nested)))
      else -> modifierArg(place, emptyList())
    }

  val args =
    listOf(
      scaffoldModifier,
      p.literal("topBar")?.let {
        arg(
          "topBar",
          lambda(emit(it, place.forChild("Scaffold").copy(scrollBehavior = collapsing))),
        )
      },
      bottomId
        ?.takeIf { !toolbarOverContent }
        ?.let { arg("bottomBar", lambda(emit(it, place.forChild("Scaffold")))) },
      p.literal("floatingActionButton")?.let {
        arg("floatingActionButton", lambda(emit(it, place.forChild("Scaffold"))))
      },
    )
  val scaffold =
    Call(
      m3("Scaffold"),
      args.filterNotNull(),
      Lambda("innerPadding", scaffoldContent(p, place, if (toolbarOverContent) bottomId else null)),
    )
  if (!inRow) return statements + stmt(scaffold)
  return statements +
    one(
      file.use(Sym.Row),
      listOf(modifierArg(place, emptyList())),
      emit(rail!!, place.forChild("Row").copy(railExpanded = false)) + stmt(scaffold),
    )
}

private fun ScreenEmitter.scaffoldContent(
  p: JsonObject,
  place: Place,
  toolbar: String?,
): List<Stmt> {
  val contentId = p.literal("content") ?: return emptyList()
  val content = byId[contentId] ?: return emptyList()
  val inner = place.forChild("Scaffold")
  if (toolbar != null) {
    val body =
      emit(contentId, inner.copy(leading = listOf("fillMaxSize()"))) +
        emit(toolbar, inner.copy(floating = true))
    return one(
      file.use(Sym.Box),
      listOf(
        arg(
          "modifier",
          Chain(
            file.use(Sym.Modifier),
            listOf(useModifier("fillMaxSize()"), useModifier("padding(innerPadding)")),
          ),
        ),
      ),
      body,
    )
  }
  if (content.component in LAYOUT_TYPES) {
    return emit(contentId, inner.copy(leading = listOf("fillMaxSize()", "padding(innerPadding)")))
  }
  return one(
    file.use(Sym.Box),
    listOf(
      arg("modifier", Chain(file.use(Sym.Modifier), listOf(useModifier("padding(innerPadding)")))),
    ),
    // The previewer stretches the content of a Scaffold, which the Box would otherwise wrap.
    emit(contentId, inner.copy(parentStretches = true)),
  )
}

private fun ScreenEmitter.topAppBar(p: JsonObject, place: Place): List<Stmt> {
  val size = p.choice("TopAppBar", "size")
  val flexible = size == "medium" || size == "large"
  if (flexible) expressive() else experimental()
  val callee =
    when (size) {
      "center" -> "CenterAlignedTopAppBar"
      "medium" -> "MediumFlexibleTopAppBar"
      "large" -> "LargeFlexibleTopAppBar"
      else -> "TopAppBar"
    }
  val title = p.text("title", place) ?: Raw("\"\"")
  val subtitle = if (flexible) textSlot(p, "subtitle", place) else null
  val actions = children(p, "actions", place, "TopAppBar")
  return one(
    m3(callee),
    listOf(
      arg("title", lambdaOf(textOf(title))),
      subtitle,
      modifier(place, p),
      slot(p, "navigationIcon", place, "TopAppBar")?.let { arg("navigationIcon", it) },
      if (p["actions"] == null) null else arg("actions", lambda(actions)),
      if (place.scrollBehavior) arg("scrollBehavior", "scrollBehavior") else null,
    ),
  )
}

private fun ScreenEmitter.navigationBar(p: JsonObject, place: Place): List<Stmt> {
  expressive()
  val arrangement = "${m3("ShortNavigationBarArrangement")}.EqualWeight"
  return one(
    m3("ShortNavigationBar"),
    listOf(modifier(place, p), arg("arrangement", arrangement)),
    children(p, "children", place, "NavigationBar"),
  )
}

private fun ScreenEmitter.tabs(p: JsonObject, place: Place): List<Stmt> {
  val secondary = p.literal("variant") == "secondary"
  val scrollable = p.sampleFlag("scrollable", place) == true
  val callee =
    (if (secondary) "Secondary" else "Primary") + if (scrollable) "ScrollableTabRow" else "TabRow"
  return one(
    m3(callee),
    listOf(arg("selectedTabIndex", selectedTab(p, place)), modifier(place, p)),
    children(p, "children", place, "Tabs"),
  )
}

/**
 * The index of the selected tab, which the row needs for its indicator. Listed tabs give the index
 * of the first whose `selected` is set in the sample data, else the first. A template whose
 * `selected` is bound gives the same search over its list, written with the expression each tab's
 * `selected` uses, so the indicator follows the data.
 */
private fun ScreenEmitter.selectedTab(p: JsonObject, place: Place): String {
  fun selected(tab: ComponentNode, at: Place) = tab.props.sampleFlag("selected", at) == true
  return when (val items = p["children"].childItems()) {
    is ChildItems.Listed ->
      items.ids
        .mapNotNull(byId::get)
        .indexOfFirst { selected(it, place) }
        .coerceAtLeast(0)
        .toString()
    is ChildItems.Template -> {
      val tab = byId[items.componentId]
      if (tab?.props?.get("selected") !is JsonObject) {
        "0"
      } else {
        val template = templateScope(items.componentId, items.path, place, indexed = false)
        val scope = template.scope
        val condition = tab.props.boolExpr("selected", place.copy(scope = scope)) ?: "false"
        "${template.list.expr}.indexOfFirst { ${scope.variable} -> $condition }.coerceAtLeast(0)"
      }
    }
    null -> "0"
  }
}

private fun ScreenEmitter.tab(node: ComponentNode, p: JsonObject, place: Place): List<Stmt> {
  val label = p.text("label", place) ?: Raw("\"\"")
  val iconName = p.iconName("icon", place)
  return one(
    m3("Tab"),
    listOf(
      arg("selected", p.boolExpr("selected", place) ?: "false"),
      arg("onClick", actionLambda(p["action"], node.id)),
      modifier(place, p),
      arg("text", lambdaOf(textOf(label))),
      iconName?.let { arg("icon", lambdaOf(icon(it))) },
      // Material's inactive tab token; the selected color is the row's own.
      arg("unselectedContentColor", colorRole("onSurfaceVariant")),
    ),
  )
}

private fun ScreenEmitter.navigationRail(p: JsonObject, place: Place): List<Stmt> {
  expressive()
  val expanded = p.sampleFlag("expanded", place) == true
  val state =
    if (expanded)
      "${m3("rememberWideNavigationRailState")}(${m3("WideNavigationRailValue")}.Expanded)"
    else "${m3("rememberWideNavigationRailState")}()"
  return one(
    m3("WideNavigationRail"),
    listOf(modifier(place, p), arg("state", state)),
    children(p, "children", place.copy(railExpanded = expanded), "NavigationRail"),
  )
}

private fun ScreenEmitter.navigationItem(
  node: ComponentNode,
  p: JsonObject,
  place: Place,
  callee: String,
  rail: Boolean,
): List<Stmt> {
  expressive()
  val selected = p.boolExpr("selected", place) ?: "false"
  val symbol = p.iconName("icon", place) ?: IconName.Literal("help")
  val selectedSymbol = p.iconName("selectedIcon", place)
  val label = p.text("label", place) ?: Raw("\"\"")
  val painter =
    when {
      selectedSymbol == null -> painter(symbol)
      selected == "true" -> painter(selectedSymbol)
      selected == "false" -> painter(symbol)
      else -> togglePainter(selected, selectedSymbol, symbol)
    }
  val icon = iconOf(painter)
  return one(
    m3(callee),
    listOf(
      if (rail) arg("railExpanded", place.railExpanded.toString()) else null,
      arg("selected", selected),
      arg("onClick", actionLambda(p["action"], node.id)),
      arg("icon", lambdaOf(icon)),
      arg("label", lambdaOf(textOf(label))),
      modifier(place, p),
    ),
  )
}

private fun ScreenEmitter.floatingToolbar(p: JsonObject, place: Place): List<Stmt> {
  expressive()
  val vertical = p.literal("orientation") == "vertical"
  val colors =
    if (p.literal("colors") == "vibrant")
      "${m3("FloatingToolbarDefaults")}.vibrantFloatingToolbarColors()"
    else null
  val over =
    if (place.floating)
      listOf(
        "align(${file.use(Sym.Alignment)}.BottomCenter)",
        "offset(y = -${m3("FloatingToolbarDefaults")}.ScreenOffset)",
      )
    else emptyList()
  over.forEach(::useModifier)
  val callee = if (vertical) "VerticalFloatingToolbar" else "HorizontalFloatingToolbar"
  return one(
    m3(callee),
    listOf(
      arg("expanded", "true"),
      modifierArg(place, over + listOfNotNull(p.floatExpr("weight", place)?.let { "weight($it)" })),
      opt("colors", colors),
    ),
    children(p, "children", place, "FloatingToolbar"),
  )
}

private fun ScreenEmitter.card(node: ComponentNode, p: JsonObject, place: Place): List<Stmt> {
  val callee =
    when (p.literal("variant")) {
      "elevated" -> "ElevatedCard"
      "outlined" -> "OutlinedCard"
      else -> "Card"
    }
  val container =
    p.literal(CONTAINER_KEY)?.let {
      Call("${m3("CardDefaults")}.cardColors", listOf(arg("containerColor", colorRole(it))))
    }
  val hasAction = p["action"] != null
  return one(
    m3(callee),
    listOf(
      if (hasAction) arg("onClick", actionLambda(p["action"], node.id)) else null,
      modifier(place, p),
      opt("shape", p.literal("shape")?.let(::shapeToken)),
      opt("colors", container),
    ),
    children(p, "content", place, "Card"),
  )
}

// endregion

// region lists

private fun ScreenEmitter.list(p: JsonObject, place: Place): List<Stmt> {
  expressive()
  val segmented = p.literal("variant") == "segmented"
  val gap =
    if (segmented) "${file.use(Sym.Arrangement)}.spacedBy(${m3("ListItemDefaults")}.SegmentedGap)"
    else null
  return one(
    file.use(Sym.Column),
    listOf(modifier(place, p, *containerBackground(p)), opt("verticalArrangement", gap)),
    children(p, "children", place, "List", indexed = segmented) { pl, i, n ->
      pl.copy(listPosition = ListPosition(segmented, i, n))
    },
  )
}

private fun ScreenEmitter.listItem(node: ComponentNode, p: JsonObject, place: Place): List<Stmt> {
  val position = place.listPosition
  val headline = p.text("headline", place) ?: Raw("\"\"")
  val overline = textSlot(p, "overline", place, "overlineContent")
  val supporting = textSlot(p, "supporting", place, "supportingContent")
  val leading = slot(p, "leading", place, "ListItem")
  val trailing = slot(p, "trailing", place, "ListItem")
  val hasAction = p["action"] != null
  val container =
    p.literal(CONTAINER_KEY)?.let {
      val kind = if (position?.segmented == true) "segmentedColors" else "colors"
      Call("${m3("ListItemDefaults")}.$kind", listOf(arg("containerColor", colorRole(it))))
    }
  if (position?.segmented == true) {
    expressive()
    val list = m3("ListItemDefaults")
    return one(
      m3("SegmentedListItem"),
      listOf(
        boolArg(p, "selected", place, "selected", omitted = false),
        arg("onClick", actionLambda(p["action"], node.id)),
        arg("shapes", "$list.segmentedShapes(${position.index}, ${position.count})"),
        modifier(place, p),
        arg("colors", container ?: Raw("$list.segmentedColors()")),
        leading?.let { arg("leadingContent", it) },
        trailing?.let { arg("trailingContent", it) },
        overline,
        supporting,
      ),
      listOf(stmt(textOf(headline))),
    )
  }
  val click =
    if (hasAction) {
      val body = actionStatements(p["action"], node.id).mapNotNull { it.flat() }
      "${useModifier("clickable")} { ${body.joinToString("; ")} }"
    } else null
  return one(
    m3("ListItem"),
    listOf(
      modifier(place, p, click),
      overline,
      supporting,
      leading?.let { arg("leadingContent", it) },
      trailing?.let { arg("trailingContent", it) },
      opt("colors", container),
    ),
    listOf(stmt(textOf(headline))),
  )
}

// endregion

// region actions

/** Button size steps: the `ButtonDefaults` height constant, or null for the default size. */
private fun buttonHeight(size: String?): String? =
  when (size) {
    "extra-small" -> "ExtraSmallContainerHeight"
    "medium" -> "MediumContainerHeight"
    "large" -> "LargeContainerHeight"
    "extra-large" -> "ExtraLargeContainerHeight"
    else -> null
  }

/**
 * The icon, gap and label inside a button. [unsized] is the defaults object for the default size
 * (`ButtonDefaults` or `ToggleButtonDefaults`); sized buttons take everything from `ButtonDefaults`
 * via [heightRef].
 */
private fun ScreenEmitter.buttonContent(
  p: JsonObject,
  place: Place,
  unsized: String,
  heightRef: String?,
): List<Stmt> {
  val sized = m3("ButtonDefaults")
  val body = mutableListOf<Stmt>()
  p.iconName("icon", place)?.let { symbol ->
    val size = if (heightRef == null) "$unsized.IconSize" else "$sized.iconSizeFor($heightRef)"
    val gap = if (heightRef == null) "$unsized.IconSpacing" else "$sized.iconSpacingFor($heightRef)"
    body += stmt(icon(symbol, null, sizeModifier(size)))
    body +=
      stmt(
        Call(
          file.use(Sym.Spacer),
          listOf(pos(Chain(file.use(Sym.Modifier), listOf("${useModifier("width")}($gap)")))),
        ),
      )
  }
  val label = p.text("label", place) ?: Raw("\"\"")
  body += stmt(textOf(label, opt("style", heightRef?.let { "$sized.textStyleFor($it)" })))
  return body
}

private fun ScreenEmitter.button(node: ComponentNode, p: JsonObject, place: Place): List<Stmt> {
  val defaults = m3("ButtonDefaults")
  val height = buttonHeight(p.literal("size"))
  val heightRef = height?.let { "$defaults.$it" }
  val callee =
    when (p.literal("variant")) {
      "tonal" -> "FilledTonalButton"
      "elevated" -> "ElevatedButton"
      "outlined" -> "OutlinedButton"
      "text" -> "TextButton"
      else -> "Button"
    }
  val square = p.literal("shape") == "square"
  if (height != null || square) expressive()
  val shapes =
    place.groupShapes
      ?: when {
        square -> "$defaults.shapes(shape = $defaults.squareShape)"
        heightRef != null -> "$defaults.shapesFor($heightRef)"
        else -> null
      }
  val min = heightRef?.let { "${useModifier("heightIn")}(min = $it)" }
  return one(
    m3(callee),
    listOf(
      arg("onClick", actionLambda(p["action"], node.id)),
      modifier(place, p, min, semantics(p)),
      enabled(p, place),
      opt("shapes", shapes),
      opt("contentPadding", heightRef?.let { "$defaults.contentPaddingFor($it)" }),
    ),
    buttonContent(p, place, defaults, heightRef),
  )
}

private fun ScreenEmitter.toggleButton(
  node: ComponentNode,
  p: JsonObject,
  place: Place,
): List<Stmt> {
  expressive()
  val defaults = m3("ToggleButtonDefaults")
  val button = m3("ButtonDefaults")
  val height = buttonHeight(p.literal("size"))
  val heightRef = height?.let { "$button.$it" }
  val callee =
    when (p.literal("variant")) {
      "tonal" -> "FilledTonalToggleButton"
      "elevated" -> "ElevatedToggleButton"
      "outlined" -> "OutlinedToggleButton"
      else -> "ToggleButton"
    }
  val (name, declaration) = state(node, "Checked", p.boolExpr("checked", place) ?: "false")
  val shapes = place.groupShapes ?: "$defaults.shapesFor(${heightRef ?: "$defaults.MinHeight"})"
  val min = heightRef?.let { "${useModifier("heightIn")}(min = $it)" }
  val change = assignIt(name, actionStatements(p["action"], node.id))
  return listOf(declaration) +
    one(
      m3(callee),
      listOf(
        arg("checked", name),
        arg("onCheckedChange", change),
        modifier(place, p, min, semantics(p)),
        enabled(p, place),
        arg("shapes", shapes),
        opt("contentPadding", heightRef?.let { "$defaults.contentPaddingFor($it)" }),
      ),
      buttonContent(p, place, defaults, heightRef),
    )
}

private fun ScreenEmitter.buttonGroup(p: JsonObject, place: Place): List<Stmt> {
  expressive()
  val connected = p.literal("variant") == "connected"
  val defaults = m3("ButtonGroupDefaults")
  val spacing = if (connected) "$defaults.ConnectedSpaceBetween" else "12.${file.use(Sym.dp)}"
  return one(
    file.use(Sym.Row),
    listOf(
      modifier(place, p),
      arg("horizontalArrangement", "${file.use(Sym.Arrangement)}.spacedBy($spacing)"),
    ),
    children(p, "children", place, "Row") { pl, i, n ->
      if (!connected) pl
      else {
        val index = i.toIntOrNull()
        val count = n.toIntOrNull()
        val kind =
          when {
            index == 0 -> "Leading"
            count != null && index == count - 1 -> "Trailing"
            else -> "Middle"
          }
        pl.copy(groupShapes = "$defaults.connected${kind}ButtonShapes()")
      }
    },
  )
}

private fun ScreenEmitter.iconButton(node: ComponentNode, p: JsonObject, place: Place): List<Stmt> {
  val defaults = m3("IconButtonDefaults")
  val toggle = p.sampleFlag("toggle", place) == true
  val variant = p.choice("IconButton", "variant")
  val callee =
    if (toggle)
      when (variant) {
        "filled" -> "FilledIconToggleButton"
        "tonal" -> "FilledTonalIconToggleButton"
        "outlined" -> "OutlinedIconToggleButton"
        else -> "IconToggleButton"
      }
    else
      when (variant) {
        "filled" -> "FilledIconButton"
        "tonal" -> "FilledTonalIconButton"
        "outlined" -> "OutlinedIconButton"
        else -> "IconButton"
      }
  val size = p.nonDefault("IconButton", "size")
  val sizeName = size?.let { Naming.camel(it) }
  val square = p.literal("shape") == "square"
  if (toggle || square || size != null || p.nonDefault("IconButton", "width") != null) expressive()
  // The buttons take no width parameter: Narrow and Wide go through the container size.
  val width =
    p.literal("width")
      ?.takeIf { it == "narrow" || it == "wide" }
      ?.let { "${m3("IconButtonDefaults.IconButtonWidthOption")}.${Naming.pascal(it)}" }
  val onSurfaceVariant = colorRole("onSurfaceVariant")
  val colors: Expr? =
    when {
      toggle -> null
      variant == "standard" ->
        Call("$defaults.iconButtonColors", listOf(arg("contentColor", onSurfaceVariant)))
      variant == "outlined" ->
        Call("$defaults.outlinedIconButtonColors", listOf(arg("contentColor", onSurfaceVariant)))
      else -> null
    }
  val border: Expr? =
    if (variant == "outlined" && !toggle)
      Call(
        file.use(Sym.BorderStroke),
        listOf(pos("1.${file.use(Sym.dp)}"), pos(colorRole("outlineVariant"))),
      )
    else null
  // A square button takes the shapes of its size, which is small when the size is the default.
  val shapeSize = sizeName ?: "small"
  val shapes =
    when {
      square && toggle ->
        "$defaults.toggleableShapes($defaults.${shapeSize}SquareShape, " +
          "$defaults.${shapeSize}PressedShape, $defaults.${shapeSize}SelectedSquareShape)"
      square ->
        "$defaults.shapes($defaults.${shapeSize}SquareShape, $defaults.${shapeSize}PressedShape)"
      toggle -> "$defaults.toggleableShapes()"
      sizeName != null -> "$defaults.shapes($defaults.${sizeName}RoundShape)"
      else -> null
    }
  val sizeModifier =
    (sizeName ?: width?.let { "small" })?.let {
      "${useModifier("size")}($defaults.${it}ContainerSize(${width.orEmpty()}))"
    }
  val iconSize = sizeName?.let { sizeModifier("$defaults.${it}IconSize") }
  val description = p.text("contentDescription", place)
  val symbol = p.iconName("icon", place) ?: IconName.Literal("help")
  val statements = mutableListOf<Stmt>()
  val content: Expr
  val head: List<Arg?>
  if (toggle) {
    val (name, declaration) = state(node, "Checked", p.boolExpr("checked", place) ?: "false")
    statements += declaration
    content = iconOf(togglePainter(name, symbol, symbol, onFilled = true), description, iconSize)
    val change = assignIt(name, actionStatements(p["action"], node.id))
    head = listOf(arg("checked", name), arg("onCheckedChange", change))
  } else {
    content = icon(symbol, description, iconSize)
    head = listOf(arg("onClick", actionLambda(p["action"], node.id)))
  }
  statements +=
    one(
      m3(callee),
      head +
        listOf(
          modifier(place, p, sizeModifier),
          enabled(p, place),
          opt("shapes", shapes),
          opt("colors", colors),
          opt("border", border),
        ),
      listOf(stmt(content)),
    )
  return statements
}

private fun ScreenEmitter.splitButton(
  node: ComponentNode,
  p: JsonObject,
  place: Place,
): List<Stmt> {
  expressive()
  val defaults = m3("SplitButtonDefaults")
  val prefix =
    when (p.literal("variant")) {
      "tonal" -> "Tonal"
      "elevated" -> "Elevated"
      "outlined" -> "Outlined"
      else -> ""
    }
  val menuId = p.literal("menu")
  val menu = menuId?.let(byId::get)
  val expandedVar = menu?.let { overlayVar(it.id) }
  val leadingBody = mutableListOf<Stmt>()
  p.iconName("icon", place)?.let { symbol ->
    leadingBody += stmt(icon(symbol, null, sizeModifier("$defaults.LeadingIconSize")))
    leadingBody +=
      stmt(
        Call(
          file.use(Sym.Spacer),
          listOf(
            pos(
              Chain(
                file.use(Sym.Modifier),
                listOf("${useModifier("size")}(${m3("ButtonDefaults")}.IconSpacing)"),
              ),
            ),
          ),
        ),
      )
  }
  leadingBody += stmt(textOf(p.text("label", place) ?: Raw("\"\"")))
  val leading =
    Call(
      "$defaults.${prefix}LeadingButton",
      listOf(arg("onClick", actionLambda(p["action"], node.id))),
      lambda(leadingBody),
    )
  val checked = expandedVar ?: "false"
  val change = if (expandedVar != null) assignIt(expandedVar) else lambda()
  val trailing =
    Call(
      "$defaults.${prefix}TrailingButton",
      listOf(arg("checked", checked), arg("onCheckedChange", change)),
      lambda(
        stmt(
          icon(
            IconName.Literal("keyboard_arrow_down"),
            null,
            sizeModifier("$defaults.TrailingIconSize"),
          ),
        ),
      ),
    )
  val layoutCall =
    Call(
      m3("SplitButtonLayout"),
      listOfNotNull(
        arg("leadingButton", lambdaOf(leading)),
        arg("trailingButton", lambdaOf(trailing)),
        modifier(place, p),
      ),
    )
  if (menu == null) return listOf(stmt(layoutCall))
  return besideMenu(menu, listOf(stmt(layoutCall)))
}

private fun ScreenEmitter.floatingActionButton(
  node: ComponentNode,
  p: JsonObject,
  place: Place,
): List<Stmt> {
  val size = p.choice("FloatingActionButton", "size")
  val label = p.text("label", place)
  val extended = p.sampleFlag("extended", place) == true && label != null
  val prefix =
    when (size) {
      "small" -> ""
      "large" -> "Large"
      else -> "Medium"
    }
  if (size != "small") expressive()
  val callee =
    if (extended) "${prefix}ExtendedFloatingActionButton" else "${prefix}FloatingActionButton"
  val colors = fabColors(p.literal("color"))
  val iconSize =
    when (size) {
      "large" -> "${m3("FloatingActionButtonDefaults")}.LargeIconSize"
      "medium" -> "${m3("FloatingActionButtonDefaults")}.MediumIconSize"
      else -> null
    }
  val symbol = p.iconName("icon", place) ?: IconName.Literal("add")
  val description = p.text("contentDescription", place) ?: if (extended) null else label
  val glyph = icon(symbol, description, iconSize?.let(::sizeModifier))
  val common =
    listOf(
      arg("onClick", actionLambda(p["action"], node.id)),
      modifier(place, p, semantics(p)),
      colors?.let { arg("containerColor", it.first) },
      colors?.let { arg("contentColor", it.second) },
    )
  if (extended) {
    return one(
      m3(callee),
      common + listOf(arg("icon", lambdaOf(glyph)), arg("text", lambdaOf(textOf(label!!)))),
    )
  }
  return one(m3(callee), common, listOf(stmt(glyph)))
}

/** Container and content color for a FAB `color`, or null for the default primary container. */
private fun ScreenEmitter.fabColors(color: String?): Pair<String, String>? =
  when (color) {
    null,
    "primary-container" -> null
    "primary" -> colorRole("primary") to colorRole("onPrimary")
    "secondary-container" -> colorRole("secondaryContainer") to colorRole("onSecondaryContainer")
    "secondary" -> colorRole("secondary") to colorRole("onSecondary")
    "tertiary-container" -> colorRole("tertiaryContainer") to colorRole("onTertiaryContainer")
    "tertiary" -> colorRole("tertiary") to colorRole("onTertiary")
    "surface" -> colorRole("surfaceContainerHigh") to colorRole("primary")
    else -> null
  }

private fun ScreenEmitter.fabMenu(node: ComponentNode, p: JsonObject, place: Place): List<Stmt> {
  expressive()
  val (name, declaration) = state(node, "Expanded", p.boolExpr("expanded", place) ?: "false")
  val toggle =
    Call(
      m3("ToggleFloatingActionButton"),
      listOf(
        arg("checked", name),
        arg("onCheckedChange", assignIt(name)),
      ),
      lambda(
        stmt(
          icon(
            p.iconName("icon", place) ?: IconName.Literal("add"),
            p.text("contentDescription", place),
          ),
        ),
      ),
    )
  return listOf(declaration) +
    one(
      m3("FloatingActionButtonMenu"),
      listOf(arg("expanded", name), arg("button", lambdaOf(toggle)), modifier(place, p)),
      children(p, "children", place, "FabMenu"),
    )
}

private fun ScreenEmitter.fabMenuItem(
  node: ComponentNode,
  p: JsonObject,
  place: Place,
): List<Stmt> {
  expressive()
  val label = p.text("label", place) ?: Raw("\"\"")
  return one(
    m3("FloatingActionButtonMenuItem"),
    listOf(
      arg("onClick", actionLambda(p["action"], node.id)),
      arg("text", lambdaOf(textOf(label))),
      arg("icon", lambdaOf(icon(p.iconName("icon", place) ?: IconName.Literal("add")))),
    ),
  )
}

// endregion

// region chips

private fun ScreenEmitter.chip(node: ComponentNode, p: JsonObject, place: Place): List<Stmt> {
  val type = node.component
  val elevated = p.literal("variant") == "elevated" && type != "InputChip"
  val callee = (if (elevated) "Elevated" else "") + type
  val defaultsName = if (type == "InputChip") "InputChipDefaults" else "${type}Defaults"
  val label = p.text("label", place) ?: Raw("\"\"")
  val selectable = type == "FilterChip" || type == "InputChip"
  val statements = mutableListOf<Stmt>()
  val head = mutableListOf<Arg?>()
  var selectedRef = "false"
  val selection = place.chipSelection
  if (selectable) {
    if (selection != null) {
      selectedRef = "${selection.state} == ${kotlinString(node.id)}"
      head += arg("selected", selectedRef)
      head +=
        arg(
          "onClick",
          lambda(
            listOf<Stmt>(Assign(selection.state, "=", Raw(kotlinString(node.id)))) +
              actionStatements(p["action"], node.id),
          ),
        )
    } else {
      val (name, declaration) = state(node, "Selected", p.boolExpr("selected", place) ?: "false")
      statements += declaration
      selectedRef = name
      head += arg("selected", name)
      head +=
        arg(
          "onClick",
          lambda(
            listOf<Stmt>(Assign(name, "=", Raw("!$name"))) + actionStatements(p["action"], node.id),
          ),
        )
    }
  } else {
    head += arg("onClick", actionLambda(p["action"], node.id))
  }
  head += arg("label", lambdaOf(textOf(label)))
  head += modifier(place, p)
  val iconSize = sizeModifier("${m3(defaultsName)}.IconSize")
  val symbol = p.iconName("icon", place)
  val leading: Arg? =
    when {
      type == "SuggestionChip" -> symbol?.let { arg("icon", lambdaOf(icon(it, null, iconSize))) }
      type == "FilterChip" -> {
        val check = IconName.Literal("check")
        if (symbol != null) {
          arg(
            "leadingIcon",
            lambdaOf(iconOf(togglePainter(selectedRef, check, symbol), null, iconSize)),
          )
        } else {
          arg("leadingIcon", IfValue(selectedRef, lambdaOf(icon(check, null, iconSize)), "null"))
        }
      }
      else -> symbol?.let { arg("leadingIcon", lambdaOf(icon(it, null, iconSize))) }
    }
  head += leading
  return statements + one(m3(callee), head + enabled(p, place))
}

private fun ScreenEmitter.chipGroup(node: ComponentNode, p: JsonObject, place: Place): List<Stmt> {
  val multi = p.sampleFlag("multi", place) == true
  val statements = mutableListOf<Stmt>()
  var inner = place
  if (!multi) {
    val ids =
      (p["children"] as? JsonArray)?.mapNotNull { (it as? JsonPrimitive)?.content }.orEmpty()
    val initial = ids.firstOrNull {
      byId[it]?.let { chip -> chip.props.sampleFlag("selected", place) == true } == true
    }
    val (name, declaration) = state(node, "Selected", kotlinString(initial.orEmpty()))
    statements += declaration
    inner = place.copy(chipSelection = ChipSelection(name))
  }
  val firstChip =
    (p["children"] as? JsonArray)?.firstOrNull()?.let { (it as? JsonPrimitive)?.content }
  val defaults =
    m3(if (byId[firstChip]?.component == "InputChip") "InputChipDefaults" else "FilterChipDefaults")
  val row =
    one(
      file.use(Sym.Row),
      listOf(modifier(place, p), arg("horizontalArrangement", "$defaults.horizontalArrangement()")),
      children(p, "children", inner, "Row") { pl, _, _ ->
        pl.copy(chipSelection = inner.chipSelection)
      },
    )
  return statements + row
}

// endregion

// region inputs

private fun ScreenEmitter.switch(node: ComponentNode, p: JsonObject, place: Place): List<Stmt> {
  val (name, declaration) = state(node, "Checked", p.boolExpr("checked", place) ?: "false")
  val change = assignIt(name, actionStatements(p["action"], node.id))
  return listOf(declaration) +
    one(
      m3("Switch"),
      listOf(
        arg("checked", name),
        arg("onCheckedChange", change),
        modifier(place, p, semantics(p)),
        enabled(p, place),
      ),
    )
}

private fun ScreenEmitter.checkbox(node: ComponentNode, p: JsonObject, place: Place): List<Stmt> {
  val (name, declaration) = state(node, "Checked", p.boolExpr("checked", place) ?: "false")
  val change = assignIt(name, actionStatements(p["action"], node.id))
  val label = p.text("label", place)
  val box =
    calls(
      m3("Checkbox"),
      listOf(
        arg("checked", name),
        arg("onCheckedChange", change),
        if (label == null) modifier(place, p, semantics(p)) else null,
        enabled(p, place),
      ),
    )
  if (label == null) return listOf(declaration, stmt(box))
  val alignment = "${file.use(Sym.Alignment)}.CenterVertically"
  return listOf(declaration) +
    one(
      file.use(Sym.Row),
      listOf(modifier(place, p), arg("verticalAlignment", alignment)),
      listOf(stmt(box), stmt(textOf(label))),
    )
}

private fun ScreenEmitter.range(p: JsonObject): Pair<Double, Double> {
  val list =
    (p["range"] as? JsonArray)?.mapNotNull { (it as? JsonPrimitive)?.content?.toDoubleOrNull() }
  return if (list != null && list.size == 2) list[0] to list[1] else 0.0 to 1.0
}

/** The `valueRange` (when the design sets one) and `steps` arguments of a slider. */
private fun ScreenEmitter.rangeArgs(
  component: String,
  p: JsonObject,
  place: Place,
  min: Double,
  max: Double,
): List<Arg?> =
  listOf(
    if (p["range"] != null) arg("valueRange", "${floatLiteral(min)}..${floatLiteral(max)}")
    else null,
    p.sampleNumber(component, "steps", place)?.let { arg("steps", it.toInt().toString()) },
  )

private fun ScreenEmitter.slider(node: ComponentNode, p: JsonObject, place: Place): List<Stmt> {
  val (min, max) = range(p)
  val initial = p.floatExpr("value", place) ?: floatLiteral(min)
  val (name, declaration) = state(node, "Value", initial, Sym.mutableFloatStateOf)
  return listOf(declaration) +
    one(
      m3("Slider"),
      listOf(
        arg("value", name),
        arg("onValueChange", assignIt(name)),
        modifier(place, p),
        enabled(p, place),
      ) + rangeArgs("Slider", p, place, min, max),
    )
}

private fun ScreenEmitter.rangeSlider(
  node: ComponentNode,
  p: JsonObject,
  place: Place,
): List<Stmt> {
  val (min, max) = range(p)
  val value =
    (p["value"] as? JsonArray)?.mapNotNull { (it as? JsonPrimitive)?.content?.toDoubleOrNull() }
  val initial =
    if (value != null && value.size == 2) "${floatLiteral(value[0])}..${floatLiteral(value[1])}"
    else "${floatLiteral(min)}..${floatLiteral(max)}"
  val (name, declaration) = state(node, "Value", initial)
  return listOf(declaration) +
    one(
      m3("RangeSlider"),
      listOf(
        arg("value", name),
        arg("onValueChange", assignIt(name)),
        modifier(place, p),
        enabled(p, place),
      ) + rangeArgs("RangeSlider", p, place, min, max),
    )
}

private fun ScreenEmitter.textField(node: ComponentNode, p: JsonObject, place: Place): List<Stmt> {
  val initial = p.text("value", place) ?: Raw("\"\"")
  val (name, declaration) = state(node, "Value", initial.flat() ?: "\"\"")
  val callee = if (p.literal("variant") == "outlined") "OutlinedTextField" else "TextField"
  val lines = p.sampleNumber("TextField", "lines", place)?.toInt()
  fun iconSlot(key: String) = p.iconName(key, place)?.let { arg(key, lambdaOf(icon(it))) }
  return listOf(declaration) +
    one(
      m3(callee),
      listOf(
        arg("value", name),
        arg("onValueChange", assignIt(name)),
        modifier(place, p),
        enabled(p, place),
        textSlot(p, "label", place),
        textSlot(p, "placeholder", place),
        iconSlot("leadingIcon"),
        iconSlot("trailingIcon"),
        textSlot(p, "prefix", place),
        textSlot(p, "supporting", place, "supportingText"),
        boolArg(p, "error", place, "isError", omitted = false),
        if (lines == null || lines <= 1) arg("singleLine", "true")
        else arg("minLines", lines.toString()),
      ),
    )
}

// endregion

// region feedback

private fun ScreenEmitter.loadingIndicator(p: JsonObject, place: Place): List<Stmt> {
  expressive()
  val callee =
    if (p.sampleFlag("contained", place) == true) "ContainedLoadingIndicator"
    else "LoadingIndicator"
  return one(m3(callee), listOf(modifier(place, p)))
}

private fun ScreenEmitter.progressIndicator(p: JsonObject, place: Place): List<Stmt> {
  val circular = p.literal("shape") == "circular"
  val wavy = p.sampleFlag("wavy", place) == true
  if (wavy) expressive()
  val callee =
    when {
      circular && wavy -> "CircularWavyProgressIndicator"
      circular -> "CircularProgressIndicator"
      wavy -> "LinearWavyProgressIndicator"
      else -> "LinearProgressIndicator"
    }
  val progress = p.floatExpr("progress", place)
  return one(
    m3(callee),
    listOf(progress?.let { arg("progress", lambdaOf(Raw(it))) }, modifier(place, p)),
  )
}

// endregion

// region overlays

/** The statements that draw an overlay when it is not placed beside an anchor. */
internal fun ScreenEmitter.overlayStatements(node: ComponentNode): List<Stmt> {
  val p = effectiveProps(node, SizeClass.Compact)
  val shown = overlayVar(node.id)
  val place = Place()
  return when (node.component) {
    "BottomSheet" -> {
      experimental()
      val sheet =
        Call(
          m3("ModalBottomSheet"),
          listOf(arg("onDismissRequest", lambda(Assign(shown, "=", Raw("false"))))),
          lambda(children(p, "content", place, "BottomSheet")),
        )
      listOf(IfStmt(listOf(shown to listOf(stmt(sheet))), null))
    }
    "Dialog" -> {
      val title = p.text("title", place) ?: Raw("\"\"")
      val confirm = slot(p, "confirm", place, "Dialog") ?: lambda()
      val dismiss = slot(p, "dismiss", place, "Dialog")
      val dialog =
        Call(
          m3("AlertDialog"),
          listOfNotNull(
            arg("onDismissRequest", lambda(Assign(shown, "=", Raw("false")))),
            arg("confirmButton", confirm),
            dismiss?.let { arg("dismissButton", it) },
            arg("title", lambdaOf(textOf(title))),
            textSlot(p, "text", place),
          ),
        )
      listOf(IfStmt(listOf(shown to listOf(stmt(dialog))), null))
    }
    "Menu" -> menuStatements(node)
    else -> emptyList()
  }
}

internal fun ScreenEmitter.menuStatements(menu: ComponentNode): List<Stmt> {
  expressive()
  val p = effectiveProps(menu, SizeClass.Compact)
  val shown = overlayVar(menu.id)
  val items = (p["items"] as? JsonArray)?.mapNotNull { (it as? JsonPrimitive)?.content }.orEmpty()
  val defaults = m3("MenuDefaults")
  val group =
    Call(
      m3("DropdownMenuGroup"),
      listOf(
        arg("shapes", "$defaults.groupShape(0, 1)"),
        arg("containerColor", colorRole("surfaceContainer")),
      ),
      lambda(
        items.flatMapIndexed { i, id ->
          emit(
            id,
            Place(
              parentType = "Menu",
              listPosition = ListPosition(false, i.toString(), items.size.toString()),
            ),
          )
        },
      ),
    )
  val popup =
    Call(
      m3("DropdownMenuPopup"),
      listOf(
        arg("expanded", shown),
        arg("onDismissRequest", lambda(Assign(shown, "=", Raw("false")))),
      ),
      lambda(stmt(group)),
    )
  return listOf(stmt(popup))
}

private fun ScreenEmitter.menuItem(node: ComponentNode, p: JsonObject, place: Place): List<Stmt> {
  val defaults = m3("MenuDefaults")
  val position = place.listPosition
  val label = p.text("label", place) ?: Raw("\"\"")
  val close =
    byId.values
      .firstOrNull { it.component == "Menu" && node.id in childIds(it) }
      ?.let { overlayVar(it.id) }
  val action =
    actionStatements(p["action"], node.id) +
      listOfNotNull(close?.let { Assign(it, "=", Raw("false")) })
  return one(
    m3("DropdownMenuItem"),
    listOf(
      arg("text", lambdaOf(textOf(label))),
      arg("onClick", lambda(action)),
      position?.let { arg("shape", "$defaults.itemShape(${it.index}, ${it.count}).shape") },
      p.iconName("icon", place)?.let { arg("leadingIcon", lambdaOf(icon(it))) },
    ),
  )
}

// endregion
