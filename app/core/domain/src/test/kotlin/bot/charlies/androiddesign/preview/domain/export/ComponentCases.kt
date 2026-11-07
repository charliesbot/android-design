package bot.charlies.androiddesign.preview.domain.export

/**
 * One exported component: the proposal components (root first) and what the Kotlin must contain.
 * Cases cover every `M3ExpressiveCatalog` entry; [ComponentExportTest] checks that none is missing.
 */
internal class ComponentCase(
  val covers: Set<String>,
  val components: List<String>,
  val expect: List<String>,
  val reject: List<String> = emptyList(),
)

private const val ROOT_COLUMN = """{"id":"root","component":"Column","children":["x"]}"""

private fun case(
  vararg covers: String,
  components: List<String>,
  expect: List<String>,
  reject: List<String> = emptyList(),
) = ComponentCase(covers.toSet(), components, expect, reject)

internal val componentCases: List<ComponentCase> =
  listOf(
    // Layout
    case(
      "Column",
      components =
        listOf(
          """{"id":"root","component":"Column","spacing":"lg","horizontalAlignment":"center","scroll":true,"padding":"md","children":["t"]}""",
          """{"id":"t","component":"Text","text":"Hi"}""",
        ),
      expect =
        listOf(
          "Column(",
          "modifier = modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(12.dp)",
          "verticalArrangement = Arrangement.spacedBy(16.dp)",
          "horizontalAlignment = Alignment.CenterHorizontally",
          "Text(\"Hi\")",
        ),
    ),
    case(
      "Row",
      components =
        listOf(
          """{"id":"root","component":"Row","spacing":"sm","arrangement":"end","verticalAlignment":"center","children":["a","b"]}""",
          """{"id":"a","component":"Text","text":"A","weight":1}""",
          """{"id":"b","component":"Text","text":"B"}""",
        ),
      expect =
        listOf(
          "Row(",
          "horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)",
          "verticalAlignment = Alignment.CenterVertically",
          "Text(\"A\", modifier = Modifier.weight(1f))",
          "Text(\"B\")",
        ),
    ),
    case(
      "Box",
      components =
        listOf(
          """{"id":"root","component":"Box","contentAlignment":"bottomEnd","children":["t"]}""",
          """{"id":"t","component":"Text","text":"Hi"}""",
        ),
      expect =
        listOf(
          "Box(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.BottomEnd) {",
          "Text(\"Hi\")",
        ),
    ),
    case(
      "Grid",
      components =
        listOf(
          """{"id":"root","component":"Grid","columns":3,"spacing":"sm","children":["t"]}""",
          """{"id":"t","component":"Text","text":"Hi"}""",
        ),
      expect =
        listOf(
          "Grid(",
          "config = {",
          "repeat(3) { column(1.fr) }",
          "gap(8.dp)",
          "ExperimentalGridApi::class",
        ),
    ),
    case(
      "FlexBox",
      components =
        listOf(
          """{"id":"root","component":"FlexBox","wrap":true,"gap":"sm","children":["t"]}""",
          """{"id":"t","component":"Text","text":"Hi"}""",
        ),
      expect = listOf("FlexBox(", "wrap(FlexWrap.Wrap)", "gap(8.dp)"),
    ),
    case(
      "Spacer",
      components =
        listOf(ROOT_COLUMN, """{"id":"x","component":"Spacer","width":"lg","height":"sm"}"""),
      expect = listOf("Spacer(Modifier.width(16.dp).height(8.dp))"),
    ),
    case(
      "Divider",
      components =
        listOf(
          """{"id":"root","component":"Row","children":["x","y"]}""",
          """{"id":"x","component":"Divider"}""",
          """{"id":"y","component":"Divider","vertical":true}""",
        ),
      expect = listOf("HorizontalDivider()", "VerticalDivider()"),
    ),
    case(
      "Shape",
      components =
        listOf(
          """{"id":"root","component":"Shape","shape":"cookie9Sided","color":"primaryContainer","content":"t"}""",
          """{"id":"t","component":"Text","text":"Hi"}""",
        ),
      expect =
        listOf(
          "Box(",
          "modifier.clip(MaterialShapes.Cookie9Sided.toShape())",
          "background(MaterialTheme.colorScheme.primaryContainer)",
          "import androidx.compose.material3.toShape",
        ),
    ),
    // Content
    case(
      "Text",
      components =
        listOf(
          ROOT_COLUMN,
          """{"id":"x","component":"Text","text":"Hello \"world\" ${'$'}5","type":"titleLarge","color":"onSurfaceVariant","maxLines":2,"overflow":"ellipsis","align":"center"}""",
        ),
      expect =
        listOf(
          "Text(",
          "\"Hello \\\"world\\\" \\${'$'}5\"",
          "color = MaterialTheme.colorScheme.onSurfaceVariant",
          "textAlign = TextAlign.Center",
          "overflow = TextOverflow.Ellipsis",
          "maxLines = 2",
          "style = MaterialTheme.typography.titleLarge",
        ),
    ),
    case(
      "Icon",
      components =
        listOf(
          ROOT_COLUMN,
          """{"id":"x","component":"Icon","name":"star","size":32,"color":"primary","filled":true,"contentDescription":"Starred"}""",
        ),
      expect =
        listOf(
          "Icon(",
          "painterResource(R.drawable.star_filled)",
          "contentDescription = \"Starred\"",
          "Modifier.size(32.dp)",
          "tint = MaterialTheme.colorScheme.primary",
          "//   R.drawable.star_filled",
        ),
    ),
    case(
      "Image",
      components =
        listOf(
          ROOT_COLUMN,
          """{"id":"x","component":"Image","src":"images/hero-photo.jpg","fit":"fit","shape":"large","aspectRatio":1.5,"contentDescription":"Hero"}""",
        ),
      expect =
        listOf(
          "Image(",
          "painter = painterResource(R.drawable.hero_photo)",
          "contentDescription = \"Hero\"",
          "Modifier.fillMaxWidth().aspectRatio(1.5f).clip(MaterialTheme.shapes.large)",
          "contentScale = ContentScale.Fit",
        ),
    ),
    // Structure
    case(
      "Scaffold",
      "TopAppBar",
      components =
        listOf(
          """{"id":"root","component":"Scaffold","topBar":"bar","bottomBar":"nav","floatingActionButton":"fab","content":"x"}""",
          """{"id":"bar","component":"TopAppBar","size":"center","title":"Home","subtitle":"Sub","navigationIcon":"back","actions":["more"]}""",
          """{"id":"back","component":"IconButton","icon":"arrow_back","contentDescription":"Back"}""",
          """{"id":"more","component":"IconButton","icon":"more_vert","contentDescription":"More"}""",
          """{"id":"nav","component":"NavigationBar","children":["n1"]}""",
          """{"id":"n1","component":"NavigationBarItem","label":"Home","icon":"home","selected":true}""",
          """{"id":"fab","component":"FloatingActionButton","icon":"add"}""",
          """{"id":"x","component":"Text","text":"Body"}""",
        ),
      expect =
        listOf(
          "Scaffold(",
          "topBar = {",
          "CenterAlignedTopAppBar(",
          "title = { Text(\"Home\") }",
          "navigationIcon = {",
          "actions = {",
          "bottomBar = {",
          "floatingActionButton = {",
          ") { innerPadding ->",
          "Box(modifier = Modifier.padding(innerPadding)) { Text(\"Body\") }",
        ),
    ),
    case(
      "TopAppBar",
      components =
        listOf(
          """{"id":"root","component":"Scaffold","topBar":"x","content":"c"}""",
          """{"id":"x","component":"TopAppBar","size":"medium","title":"Plates","subtitle":"Tonight"}""",
          """{"id":"c","component":"Column","children":[]}""",
        ),
      expect =
        listOf(
          "MediumFlexibleTopAppBar(",
          "title = { Text(\"Plates\") }",
          "subtitle = { Text(\"Tonight\") }",
          "scrollBehavior = scrollBehavior",
          "val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()",
          "nestedScroll(scrollBehavior.nestedScrollConnection)",
        ),
    ),
    case(
      "NavigationBar",
      "NavigationBarItem",
      components =
        listOf(
          """{"id":"root","component":"Scaffold","bottomBar":"x","content":"c"}""",
          """{"id":"x","component":"NavigationBar","children":["a","b"]}""",
          """{"id":"a","component":"NavigationBarItem","label":"Home","icon":"home","selectedIcon":"home_filled","selected":true,"action":{"functionCall":{"@call":"back"}}}""",
          """{"id":"b","component":"NavigationBarItem","label":"Saved","icon":"favorite"}""",
          """{"id":"c","component":"Column","children":[]}""",
        ),
      expect =
        listOf(
          "ShortNavigationBar(arrangement = ShortNavigationBarArrangement.EqualWeight) {",
          "ShortNavigationBarItem(",
          "selected = true",
          "icon = { Icon(painterResource(R.drawable.home_filled), contentDescription = null) }",
          "label = { Text(\"Home\") }",
          "onClick = { onBack() }",
          "selected = false",
        ),
    ),
    case(
      "NavigationRail",
      "NavigationRailItem",
      components =
        listOf(
          """{"id":"root","component":"Scaffold","navigationRail":"x","content":"c"}""",
          """{"id":"x","component":"NavigationRail","expanded":true,"children":["a"]}""",
          """{"id":"a","component":"NavigationRailItem","label":"Home","icon":"home","selected":true}""",
          """{"id":"c","component":"Column","children":[]}""",
        ),
      expect =
        listOf(
          "Row(modifier = modifier) {",
          "WideNavigationRail(state = rememberWideNavigationRailState(WideNavigationRailValue.Expanded)) {",
          "WideNavigationRailItem(",
          "railExpanded = true",
          "Scaffold(modifier = Modifier.weight(1f))",
        ),
    ),
    case(
      "FloatingToolbar",
      components =
        listOf(
          """{"id":"root","component":"Scaffold","bottomBar":"x","content":"c"}""",
          """{"id":"x","component":"FloatingToolbar","colors":"vibrant","children":["t"]}""",
          """{"id":"t","component":"ToggleButton","label":"Home","checked":true}""",
          """{"id":"c","component":"Column","children":[]}""",
        ),
      expect =
        listOf(
          "HorizontalFloatingToolbar(",
          "expanded = true",
          "Modifier.align(Alignment.BottomCenter).offset(y = -FloatingToolbarDefaults.ScreenOffset)",
          "colors = FloatingToolbarDefaults.vibrantFloatingToolbarColors()",
        ),
    ),
    case(
      "FloatingToolbar",
      components =
        listOf(
          ROOT_COLUMN,
          """{"id":"x","component":"FloatingToolbar","orientation":"vertical","children":[]}""",
        ),
      expect = listOf("VerticalFloatingToolbar(expanded = true)"),
    ),
    case(
      "Card",
      components =
        listOf(
          ROOT_COLUMN,
          """{"id":"x","component":"Card","variant":"outlined","shape":"extraLarge","action":{"functionCall":{"@call":"back"}},"content":["t"]}""",
          """{"id":"t","component":"Text","text":"In card"}""",
        ),
      expect =
        listOf(
          "OutlinedCard(",
          "onClick = { onBack() }",
          "shape = MaterialTheme.shapes.extraLarge",
          "Text(\"In card\")",
        ),
    ),
    // Lists
    case(
      "List",
      "ListItem",
      components =
        listOf(
          """{"id":"root","component":"List","variant":"segmented","children":["a","b"]}""",
          """{"id":"a","component":"ListItem","overline":"Over","headline":"One","supporting":"Sup","leading":"lead","trailing":"trail","selected":true,"action":{"functionCall":{"@call":"back"}}}""",
          """{"id":"b","component":"ListItem","headline":"Two"}""",
          """{"id":"lead","component":"Icon","name":"star"}""",
          """{"id":"trail","component":"Text","text":"5"}""",
        ),
      expect =
        listOf(
          "verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)",
          "SegmentedListItem(",
          "selected = true",
          "shapes = ListItemDefaults.segmentedShapes(0, 2)",
          "shapes = ListItemDefaults.segmentedShapes(1, 2)",
          "colors = ListItemDefaults.segmentedColors()",
          "leadingContent = { Icon(painterResource(R.drawable.star), contentDescription = null) }",
          "trailingContent = { Text(\"5\") }",
          "overlineContent = { Text(\"Over\") }",
          "supportingContent = { Text(\"Sup\") }",
          "Text(\"One\")",
        ),
    ),
    case(
      "ListItem",
      components =
        listOf(
          """{"id":"root","component":"List","children":["a"]}""",
          """{"id":"a","component":"ListItem","headline":"One","supporting":"Sup","action":{"functionCall":{"@call":"back"}}}""",
        ),
      expect =
        listOf(
          "ListItem(",
          "modifier = Modifier.fillMaxWidth().clickable { onBack() }",
          "supportingContent = { Text(\"Sup\") }",
          ") { Text(\"One\") }",
        ),
      reject = listOf("SegmentedListItem", "headlineContent"),
    ),
    // Carousel
    case(
      "Carousel",
      components =
        listOf(
          """{"id":"root","component":"Carousel","children":["a","b"]}""",
          """{"id":"a","component":"Card","content":["ta"]}""",
          """{"id":"b","component":"Card","content":["tb"]}""",
          """{"id":"ta","component":"Text","text":"A"}""",
          """{"id":"tb","component":"Text","text":"B"}""",
        ),
      expect =
        listOf(
          "val rootState = rememberCarouselState { 2 }",
          "HorizontalMultiBrowseCarousel(",
          "state = rootState",
          "preferredItemWidth = 186.dp",
          "itemSpacing = 8.dp",
          "flingBehavior = CarouselDefaults.multiBrowseFlingBehavior(rootState)",
          "contentPadding = PaddingValues(horizontal = 16.dp)",
          ") { index ->",
          "if (index == 0) {",
          "} else {",
          "modifier = modifier.fillMaxWidth()",
          "Modifier.maskClip(MaterialTheme.shapes.extraLarge).fillMaxWidth()",
        ),
      reject = listOf("BoxWithConstraints"),
    ),
    case(
      "Carousel",
      components =
        listOf(
          """{"id":"root","component":"Carousel","variant":"hero","children":["a","b"]}""",
          """{"id":"a","component":"Text","text":"A"}""",
          """{"id":"b","component":"Text","text":"B"}""",
        ),
      expect =
        listOf(
          "BoxWithConstraints(modifier = modifier.fillMaxWidth()) {",
          "HorizontalMultiBrowseCarousel(",
          "preferredItemWidth = maxWidth - 96.dp",
          "flingBehavior = CarouselDefaults.multiBrowseFlingBehavior(rootState)",
        ),
    ),
    case(
      "Carousel",
      components =
        listOf(
          """{"id":"root","component":"Carousel","variant":"centered-hero","children":["a"]}""",
          """{"id":"a","component":"Text","text":"A"}""",
        ),
      expect =
        listOf(
          "HorizontalCenteredHeroCarousel(",
          "itemSpacing = 8.dp",
          ") { _ ->",
          "Text(\"A\", modifier = Modifier.maskClip(MaterialTheme.shapes.extraLarge))",
        ),
    ),
    case(
      "Carousel",
      components =
        listOf(
          """{"id":"root","component":"Carousel","variant":"uncontained","itemWidth":140,"children":["a","b"]}""",
          """{"id":"a","component":"Text","text":"A"}""",
          """{"id":"b","component":"Text","text":"B"}""",
        ),
      expect =
        listOf(
          "val rootState = rememberCarouselState { 2 }",
          "HorizontalUncontainedCarousel(",
          "itemWidth = 140.dp",
          "flingBehavior = CarouselDefaults.noSnapFlingBehavior()",
        ),
    ),
    case(
      "Carousel",
      components =
        listOf(
          """{"id":"root","component":"Carousel","variant":"full-screen","children":["a","b"]}""",
          """{"id":"a","component":"Text","text":"A"}""",
          """{"id":"b","component":"Text","text":"B"}""",
        ),
      expect =
        listOf(
          "HorizontalUncontainedCarousel(",
          "itemWidth = maxWidth",
          "itemSpacing = 16.dp",
          "flingBehavior = CarouselDefaults.singleAdvanceFlingBehavior(rootState)",
          "contentPadding = PaddingValues(0.dp)",
        ),
    ),
    // Tabs
    case(
      "Tabs",
      "Tab",
      components =
        listOf(
          """{"id":"root","component":"Tabs","children":["a","b"]}""",
          """{"id":"a","component":"Tab","label":"Plates","icon":"restaurant","action":{"functionCall":{"@call":"back"}}}""",
          """{"id":"b","component":"Tab","label":"Menus","selected":true}""",
        ),
      expect =
        listOf(
          "PrimaryTabRow(selectedTabIndex = 1, modifier = modifier.fillMaxWidth()) {",
          "Tab(",
          "selected = false",
          "selected = true",
          "onClick = { onBack() }",
          "text = { Text(\"Plates\") }",
          "icon = { Icon(painterResource(R.drawable.restaurant), contentDescription = null) }",
          "text = { Text(\"Menus\") }",
          "unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant",
        ),
    ),
    case(
      "Tabs",
      components =
        listOf(
          """{"id":"root","component":"Tabs","variant":"secondary","scrollable":true,"children":["a"]}""",
          """{"id":"a","component":"Tab","label":"Plates"}""",
        ),
      expect = listOf("SecondaryScrollableTabRow(selectedTabIndex = 0"),
      reject = listOf("PrimaryTabRow"),
    ),
    case(
      "Tabs",
      components =
        listOf(
          """{"id":"root","component":"Tabs","scrollable":true,"children":["a"]}""",
          """{"id":"a","component":"Tab","label":"Plates"}""",
        ),
      expect = listOf("PrimaryScrollableTabRow(selectedTabIndex = 0"),
    ),
    case(
      "Tabs",
      components =
        listOf(
          """{"id":"root","component":"Tabs","variant":"secondary","children":["a"]}""",
          """{"id":"a","component":"Tab","label":"Plates"}""",
        ),
      expect = listOf("SecondaryTabRow(selectedTabIndex = 0"),
    ),
    // Actions
    case(
      "Button",
      components =
        listOf(
          ROOT_COLUMN,
          """{"id":"x","component":"Button","variant":"tonal","size":"medium","shape":"square","label":"Save","icon":"check","enabled":false,"contentDescription":"Save it","action":{"functionCall":{"@call":"back"}}}""",
        ),
      expect =
        listOf(
          "FilledTonalButton(",
          "onClick = { onBack() }",
          "Modifier.heightIn(min = ButtonDefaults.MediumContainerHeight).semantics { contentDescription = \"Save it\" }",
          "enabled = false",
          "shapes = ButtonDefaults.shapes(shape = ButtonDefaults.squareShape)",
          "contentPadding = ButtonDefaults.contentPaddingFor(ButtonDefaults.MediumContainerHeight)",
          "Modifier.size(ButtonDefaults.iconSizeFor(ButtonDefaults.MediumContainerHeight))",
          "Spacer(Modifier.width(ButtonDefaults.iconSpacingFor(ButtonDefaults.MediumContainerHeight)))",
          "Text(\"Save\", style = ButtonDefaults.textStyleFor(ButtonDefaults.MediumContainerHeight))",
        ),
    ),
    case(
      "Button",
      components =
        listOf(ROOT_COLUMN, """{"id":"x","component":"Button","label":"Go","icon":"add"}"""),
      expect =
        listOf(
          "Button(onClick = {}) {",
          "Modifier.size(ButtonDefaults.IconSize)",
          "Spacer(Modifier.width(ButtonDefaults.IconSpacing))",
          "Text(\"Go\")",
        ),
      reject = listOf("heightIn", "shapes ="),
    ),
    case(
      "Button",
      components =
        listOf(
          ROOT_COLUMN,
          """{"id":"x","component":"Button","variant":"elevated","label":"A"}""",
          """{"id":"y","component":"Button","variant":"outlined","label":"B"}""",
        ),
      expect = listOf("ElevatedButton(onClick = {}) { Text(\"A\") }"),
    ),
    case(
      "ToggleButton",
      components =
        listOf(
          ROOT_COLUMN,
          """{"id":"x","component":"ToggleButton","variant":"outlined","checked":true,"label":"Veg"}""",
        ),
      expect =
        listOf(
          "var xChecked by remember { mutableStateOf(true) }",
          "OutlinedToggleButton(",
          "checked = xChecked",
          "onCheckedChange = { xChecked = it }",
          "shapes = ToggleButtonDefaults.shapesFor(ToggleButtonDefaults.MinHeight)",
          "Text(\"Veg\")",
        ),
    ),
    case(
      "ButtonGroup",
      components =
        listOf(
          """{"id":"root","component":"ButtonGroup","variant":"connected","children":["a","b","c"]}""",
          """{"id":"a","component":"ToggleButton","label":"A"}""",
          """{"id":"b","component":"ToggleButton","label":"B"}""",
          """{"id":"c","component":"ToggleButton","label":"C"}""",
        ),
      expect =
        listOf(
          "horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween)",
          "ButtonGroupDefaults.connectedLeadingButtonShapes()",
          "ButtonGroupDefaults.connectedMiddleButtonShapes()",
          "ButtonGroupDefaults.connectedTrailingButtonShapes()",
        ),
    ),
    case(
      "IconButton",
      components =
        listOf(
          ROOT_COLUMN,
          """{"id":"x","component":"IconButton","variant":"outlined","icon":"search","contentDescription":"Search"}""",
        ),
      expect =
        listOf(
          "OutlinedIconButton(",
          "OutlinedIconButton",
          "IconButtonDefaults.outlinedIconButtonColors(contentColor = MaterialTheme.colorScheme.onSurfaceVariant)",
          "BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)",
          "Icon(painterResource(R.drawable.search), contentDescription = \"Search\")",
        ),
    ),
    case(
      "IconButton",
      components =
        listOf(
          ROOT_COLUMN,
          """{"id":"x","component":"IconButton","variant":"tonal","size":"extra-small","width":"wide","icon":"star","toggle":true,"checked":true}""",
        ),
      expect =
        listOf(
          "var xChecked by remember { mutableStateOf(true) }",
          "FilledTonalIconToggleButton(",
          "shapes = IconButtonDefaults.toggleableShapes()",
          "Modifier.size(IconButtonDefaults.extraSmallContainerSize(IconButtonWidthOption.Wide))",
          "painterResource(if (xChecked) R.drawable.star_filled else R.drawable.star)",
        ),
    ),
    case(
      "IconButton",
      components =
        listOf(
          ROOT_COLUMN,
          """{"id":"x","component":"IconButton","variant":"filled","shape":"square","icon":"play_arrow","contentDescription":"Play"}""",
        ),
      expect =
        listOf(
          "FilledIconButton(",
          "shapes = IconButtonDefaults.shapes(IconButtonDefaults.smallSquareShape, IconButtonDefaults.smallPressedShape)",
        ),
      reject = listOf("RoundShape", "toggleableShapes"),
    ),
    case(
      "IconButton",
      components =
        listOf(
          ROOT_COLUMN,
          """{"id":"x","component":"IconButton","variant":"tonal","size":"large","shape":"square","icon":"skip_next","contentDescription":"Next"}""",
        ),
      expect =
        listOf(
          "FilledTonalIconButton(",
          "shapes = IconButtonDefaults.shapes(IconButtonDefaults.largeSquareShape, IconButtonDefaults.largePressedShape)",
          "Modifier.size(IconButtonDefaults.largeContainerSize())",
        ),
      reject = listOf("RoundShape"),
    ),
    case(
      "IconButton",
      components =
        listOf(
          ROOT_COLUMN,
          """{"id":"x","component":"IconButton","variant":"filled","size":"extra-small","shape":"square","icon":"favorite","toggle":true,"checked":true}""",
        ),
      expect =
        listOf(
          "FilledIconToggleButton(",
          "shapes = IconButtonDefaults.toggleableShapes(IconButtonDefaults.extraSmallSquareShape, IconButtonDefaults.extraSmallPressedShape, IconButtonDefaults.extraSmallSelectedSquareShape)",
        ),
      reject = listOf("RoundShape", "toggleableShapes()"),
    ),
    case(
      "IconButton",
      components =
        listOf(
          ROOT_COLUMN,
          """{"id":"x","component":"IconButton","variant":"outlined","shape":"square","icon":"shuffle","toggle":true}""",
        ),
      expect =
        listOf(
          "OutlinedIconToggleButton(",
          "shapes = IconButtonDefaults.toggleableShapes(IconButtonDefaults.smallSquareShape, IconButtonDefaults.smallPressedShape, IconButtonDefaults.smallSelectedSquareShape)",
        ),
    ),
    case(
      "IconButton",
      components =
        listOf(
          ROOT_COLUMN,
          """{"id":"x","component":"IconButton","variant":"filled","size":"medium","shape":"round","icon":"star"}""",
        ),
      expect =
        listOf(
          "FilledIconButton(",
          "shapes = IconButtonDefaults.shapes(IconButtonDefaults.mediumRoundShape)",
        ),
      reject = listOf("SquareShape", "PressedShape"),
    ),
    case(
      "SplitButton",
      "Menu",
      "MenuItem",
      components =
        listOf(
          ROOT_COLUMN,
          """{"id":"x","component":"SplitButton","variant":"tonal","label":"Share","icon":"share","menu":"m"}""",
          """{"id":"m","component":"Menu","items":["i1","i2"]}""",
          """{"id":"i1","component":"MenuItem","label":"Copy","icon":"content_copy","action":{"functionCall":{"@call":"back"}}}""",
          """{"id":"i2","component":"MenuItem","label":"Send"}""",
        ),
      expect =
        listOf(
          "var showM by remember { mutableStateOf(false) }",
          "Box {",
          "SplitButtonLayout(",
          "SplitButtonDefaults.TonalLeadingButton(",
          "SplitButtonDefaults.TonalTrailingButton(",
          "checked = showM",
          "onCheckedChange = { showM = it }",
          "DropdownMenuPopup(",
          "expanded = showM",
          "DropdownMenuGroup(",
          "shapes = MenuDefaults.groupShape(0, 1)",
          "containerColor = MaterialTheme.colorScheme.surfaceContainer",
          "DropdownMenuItem(",
          "shape = MenuDefaults.itemShape(0, 2).shape",
          "shape = MenuDefaults.itemShape(1, 2).shape",
          "leadingIcon = { Icon(painterResource(R.drawable.content_copy), contentDescription = null) }",
        ),
    ),
    case(
      "FloatingActionButton",
      components =
        listOf(
          """{"id":"root","component":"Scaffold","floatingActionButton":"x","content":"c"}""",
          """{"id":"x","component":"FloatingActionButton","size":"large","color":"tertiary","icon":"edit","contentDescription":"Edit"}""",
          """{"id":"c","component":"Column","children":[]}""",
        ),
      expect =
        listOf(
          "LargeFloatingActionButton(",
          "containerColor = MaterialTheme.colorScheme.tertiary",
          "contentColor = MaterialTheme.colorScheme.onTertiary",
          "Modifier.size(FloatingActionButtonDefaults.LargeIconSize)",
          "contentDescription = \"Edit\"",
        ),
    ),
    case(
      "FloatingActionButton",
      components =
        listOf(
          ROOT_COLUMN,
          """{"id":"x","component":"FloatingActionButton","size":"small","extended":true,"icon":"add","label":"New"}""",
        ),
      expect = listOf("ExtendedFloatingActionButton(", "text = { Text(\"New\") }"),
      reject = listOf("containerColor", "MediumExtended"),
    ),
    case(
      "FabMenu",
      "FabMenuItem",
      components =
        listOf(
          ROOT_COLUMN,
          """{"id":"x","component":"FabMenu","icon":"add","children":["i1"]}""",
          """{"id":"i1","component":"FabMenuItem","label":"Photo","icon":"photo_camera"}""",
        ),
      expect =
        listOf(
          "var xExpanded by remember { mutableStateOf(false) }",
          "FloatingActionButtonMenu(",
          "expanded = xExpanded",
          "ToggleFloatingActionButton(checked = xExpanded, onCheckedChange = { xExpanded = it })",
          "FloatingActionButtonMenuItem(",
          "text = { Text(\"Photo\") }",
        ),
    ),
    // Chips
    case(
      "AssistChip",
      components =
        listOf(
          ROOT_COLUMN,
          """{"id":"x","component":"AssistChip","variant":"elevated","label":"Directions","icon":"map"}""",
        ),
      expect =
        listOf(
          "ElevatedAssistChip(",
          "onClick = {}",
          "label = { Text(\"Directions\") }",
          "leadingIcon = {",
          "Modifier.size(AssistChipDefaults.IconSize)",
        ),
    ),
    case(
      "FilterChip",
      components =
        listOf(
          ROOT_COLUMN,
          """{"id":"x","component":"FilterChip","label":"Vegan","selected":true}""",
        ),
      expect =
        listOf(
          "var xSelected by remember { mutableStateOf(true) }",
          "FilterChip(",
          "selected = xSelected",
          "onClick = { xSelected = !xSelected }",
          "leadingIcon =",
          "if (xSelected) {",
          "painterResource(R.drawable.check)",
          "FilterChipDefaults.IconSize",
          "} else null",
        ),
    ),
    case(
      "InputChip",
      components =
        listOf(ROOT_COLUMN, """{"id":"x","component":"InputChip","label":"Basil","icon":"eco"}"""),
      expect =
        listOf(
          "InputChip(",
          "var xSelected by remember { mutableStateOf(false) }",
          "InputChipDefaults.IconSize",
        ),
    ),
    case(
      "SuggestionChip",
      components =
        listOf(
          ROOT_COLUMN,
          """{"id":"x","component":"SuggestionChip","variant":"elevated","label":"Soup","icon":"ramen_dining"}""",
        ),
      expect = listOf("ElevatedSuggestionChip(", "icon = {", "SuggestionChipDefaults.IconSize"),
    ),
    case(
      "ChipGroup",
      components =
        listOf(
          """{"id":"root","component":"ChipGroup","children":["a","b"]}""",
          """{"id":"a","component":"FilterChip","label":"A"}""",
          """{"id":"b","component":"FilterChip","label":"B","selected":true}""",
        ),
      expect =
        listOf(
          "var rootSelected by remember { mutableStateOf(\"b\") }",
          "horizontalArrangement = FilterChipDefaults.horizontalArrangement()",
          "selected = rootSelected == \"a\"",
          "onClick = { rootSelected = \"b\" }",
        ),
      reject = listOf("var aSelected"),
    ),
    case(
      "ChipGroup",
      components =
        listOf(
          """{"id":"root","component":"ChipGroup","multi":true,"children":["a","b"]}""",
          """{"id":"a","component":"FilterChip","label":"A"}""",
          """{"id":"b","component":"FilterChip","label":"B"}""",
        ),
      expect =
        listOf(
          "var aSelected by remember { mutableStateOf(false) }",
          "var bSelected by remember { mutableStateOf(false) }",
        ),
      reject = listOf("rootSelected"),
    ),
    // Inputs
    // Radio buttons, indeterminate checkboxes, switch icons, centered and vertical sliders
    case(
      "RadioGroup",
      "RadioButton",
      components =
        listOf(
          """{"id":"root","component":"RadioGroup","children":["a","b"]}""",
          """{"id":"a","component":"RadioButton","label":"Small"}""",
          """{"id":"b","component":"RadioButton","label":"Large","selected":true,"action":{"functionCall":{"@call":"back"}}}""",
        ),
      expect =
        listOf(
          "var rootSelected by remember { mutableStateOf(\"b\") }",
          "Column(modifier = modifier.fillMaxWidth().selectableGroup())",
          "Row(",
          "heightIn(min = 48.dp)",
          "selectable(selected = rootSelected == \"a\"",
          "role = Role.RadioButton",
          "onClick = { rootSelected = \"a\" }",
          "rootSelected = \"b\"",
          "onBack()",
          "RadioButton(selected = rootSelected == \"a\", onClick = null)",
          "horizontalArrangement = Arrangement.spacedBy(12.dp)",
          "Text(\"Small\")",
        ),
      reject = listOf("var aSelected"),
    ),
    case(
      "DockedToolbar",
      components =
        listOf(
          """{"id":"root","component":"Scaffold","bottomBar":"x","content":"c"}""",
          """{"id":"x","component":"DockedToolbar","colors":"vibrant","children":["i"]}""",
          """{"id":"i","component":"IconButton","icon":"edit","contentDescription":"Edit"}""",
          """{"id":"c","component":"Column","children":[]}""",
        ),
      expect =
        listOf(
          "bottomBar = {",
          "FlexibleBottomAppBar(",
          "containerColor = MaterialTheme.colorScheme.primaryContainer",
          "contentColor = MaterialTheme.colorScheme.onPrimaryContainer",
        ),
      reject = listOf("iconButtonColors"),
    ),
    case(
      "DockedToolbar",
      components =
        listOf(
          """{"id":"root","component":"Scaffold","bottomBar":"x","content":"c"}""",
          """{"id":"x","component":"DockedToolbar","children":[]}""",
          """{"id":"c","component":"Column","children":[]}""",
        ),
      expect = listOf("bottomBar = { FlexibleBottomAppBar {} }"),
      reject = listOf("containerColor", "HorizontalFloatingToolbar"),
    ),
    case(
      "Switch",
      components =
        listOf(
          ROOT_COLUMN,
          """{"id":"x","component":"Switch","checked":true,"icons":"both"}""",
        ),
      expect =
        listOf(
          "thumbContent = {",
          "painterResource(if (xChecked) R.drawable.check else R.drawable.close)",
          "Modifier.size(SwitchDefaults.IconSize)",
        ),
    ),
    case(
      "Switch",
      components =
        listOf(
          ROOT_COLUMN,
          """{"id":"x","component":"Switch","icons":"selected"}""",
        ),
      expect = listOf("thumbContent = if (xChecked) {", "R.drawable.check"),
      reject = listOf("R.drawable.close"),
    ),
    case(
      "Checkbox",
      components =
        listOf(
          ROOT_COLUMN,
          """{"id":"x","component":"Checkbox","indeterminate":true,"label":"All"}""",
        ),
      expect =
        listOf(
          "var xIndeterminate by remember { mutableStateOf(true) }",
          "TriStateCheckbox(",
          "state = if (xIndeterminate) ToggleableState.Indeterminate else if (xChecked) ToggleableState.On else ToggleableState.Off",
          "xChecked = xIndeterminate || !xChecked",
          "xIndeterminate = false",
        ),
      reject = listOf("Checkbox(checked"),
    ),
    case(
      "Slider",
      components =
        listOf(
          ROOT_COLUMN,
          """{"id":"x","component":"Slider","variant":"centered","value":0,"range":[-1,1]}""",
        ),
      expect =
        listOf(
          "val xState = rememberSliderState(value = 0f, trackRange = -1f..1f)",
          "Slider(state = xState, onValueChange = { xState.value = it }",
          "track = { SliderDefaults.CenteredTrack(sliderState = it) }",
        ),
    ),
    case(
      "Slider",
      components =
        listOf(
          ROOT_COLUMN,
          """{"id":"x","component":"Slider","orientation":"vertical","value":0.5}""",
        ),
      expect =
        listOf(
          "val xState = rememberSliderState(value = 0.5f)",
          "VerticalSlider(",
          "state = xState",
          "VerticalSlider(state = xState, onValueChange = { xState.value = it }, modifier = Modifier.height(200.dp), topToBottom = false)",
        ),
      reject = listOf("CenteredTrack"),
    ),
    case(
      "Menu",
      "MenuGroup",
      "MenuItem",
      components =
        listOf(
          """{"id":"root","component":"Column","children":["b"]}""",
          """{"id":"b","component":"Button","label":"More","action":{"functionCall":{"@call":"open","args":{"overlay":"m"}}}}""",
          """{"id":"m","component":"Menu","anchor":"b","colors":"vibrant","items":["g1","g2"]}""",
          """{"id":"g1","component":"MenuGroup","items":["s1","s2"]}""",
          """{"id":"g2","component":"MenuGroup","items":["k1","a1"]}""",
          """{"id":"s1","component":"MenuItem","label":"Newest","role":"single","selected":true}""",
          """{"id":"s2","component":"MenuItem","label":"Oldest","role":"single"}""",
          """{"id":"k1","component":"MenuItem","label":"Unread","role":"multi","selected":true}""",
          """{"id":"a1","component":"MenuItem","label":"Settings","icon":"settings"}""",
        ),
      expect =
        listOf(
          "var g1Selected by remember { mutableStateOf(\"s1\") }",
          "var k1Checked by remember { mutableStateOf(true) }",
          "shapes = MenuDefaults.groupShape(0, 2)",
          "shapes = MenuDefaults.groupShape(1, 2)",
          "Spacer(Modifier.height(MenuDefaults.GroupSpacing))",
          "containerColor = MenuDefaults.groupVibrantContainerColor",
          "SelectableDropdownMenuItem(",
          "selected = g1Selected == \"s1\"",
          "onClick = { g1Selected = \"s1\" showM = false }",
          "shapes = MenuDefaults.itemShape(0, 2)",
          "colors = MenuDefaults.selectableItemVibrantColors()",
          "CheckableDropdownMenuItem(",
          "checked = k1Checked",
          "onCheckedChange = { k1Checked = it }",
          "DropdownMenuItem(",
          "colors = MenuDefaults.itemVibrantColors()",
        ),
      reject = listOf("surfaceContainer", "k1Checked = it showM"),
    ),
    case(
      "FloatingToolbar",
      components =
        listOf(
          """{"id":"root","component":"Scaffold","bottomBar":"x","content":"c"}""",
          """{"id":"x","component":"FloatingToolbar","colors":"vibrant","children":["i","o"]}""",
          """{"id":"i","component":"IconButton","icon":"edit","contentDescription":"Edit"}""",
          """{"id":"o","component":"IconButton","variant":"outlined","icon":"share","contentDescription":"Share"}""",
          """{"id":"c","component":"Column","children":[]}""",
        ),
      expect = listOf("HorizontalFloatingToolbar("),
      reject = listOf("iconButtonColors", "outlinedIconButtonColors"),
    ),
    case(
      "MenuItem",
      components =
        listOf(
          """{"id":"root","component":"Column","children":["b"]}""",
          """{"id":"b","component":"Button","label":"More","action":{"functionCall":{"@call":"open","args":{"overlay":"m"}}}}""",
          """{"id":"m","component":"Menu","anchor":"b","items":["i"]}""",
          """{"id":"i","component":"MenuItem","label":"Unread","sizeClass":{"medium":{"role":"multi","selected":true}}}""",
        ),
      expect =
        listOf(
          "var iChecked by remember { mutableStateOf(if (!atLeastMedium) false else true) }",
          "CheckableDropdownMenuItem(",
          "DropdownMenuItem(",
        ),
    ),
    case(
      "MenuGroup",
      components =
        listOf(
          """{"id":"root","component":"Column","children":["b"]}""",
          """{"id":"b","component":"Button","label":"More","action":{"functionCall":{"@call":"open","args":{"overlay":"m"}}}}""",
          """{"id":"m","component":"Menu","anchor":"b","items":["g1","g2","g3"]}""",
          """{"id":"g1","component":"MenuGroup","items":["a1"],"showIn":["medium","expanded"]}""",
          """{"id":"g2","component":"MenuGroup","items":["a2"]}""",
          """{"id":"g3","component":"MenuGroup","items":["a3"]}""",
          """{"id":"a1","component":"MenuItem","label":"One"}""",
          """{"id":"a2","component":"MenuItem","label":"Two"}""",
          """{"id":"a3","component":"MenuItem","label":"Three"}""",
        ),
      expect =
        listOf(
          "groupShape(0, 3)",
          "groupShape(if (!atLeastMedium) 0 else 1, if (!atLeastMedium) 2 else 3)",
          "groupShape(if (!atLeastMedium) 1 else 2, if (!atLeastMedium) 2 else 3)",
          "Text(\"One\")",
          "Text(\"Two\")",
          "Text(\"Three\")",
        ),
    ),
    case(
      "MenuItem",
      components =
        listOf(
          """{"id":"root","component":"Column","children":["b"]}""",
          """{"id":"b","component":"Button","label":"More","action":{"functionCall":{"@call":"open","args":{"overlay":"m"}}}}""",
          """{"id":"m","component":"Menu","anchor":"b","items":["a","c"]}""",
          """{"id":"a","component":"MenuItem","label":"A"}""",
          """{"id":"c","component":"MenuItem","label":"C","showIn":["medium","expanded"],"action":{"functionCall":{"@call":"back"}}}""",
        ),
      expect =
        listOf(
          "itemShape(0, if (!atLeastMedium) 1 else 2)",
          "itemShape(1, 2)",
          "Text(\"C\")",
          "onBack()",
        ),
    ),
    case(
      "MenuItem",
      components =
        listOf(
          """{"id":"root","component":"Column","children":["b"]}""",
          """{"id":"b","component":"Button","label":"More","action":{"functionCall":{"@call":"open","args":{"overlay":"m"}}}}""",
          """{"id":"m","component":"Menu","anchor":"b","items":["i"]}""",
          """{"id":"i","component":"MenuItem","label":"Unread","role":"multi","selected":false,"sizeClass":{"medium":{"selected":true}}}""",
        ),
      expect =
        listOf("var iChecked by remember { mutableStateOf(if (!atLeastMedium) false else true) }"),
    ),
    case(
      "RadioGroup",
      components =
        listOf(
          """{"id":"root","component":"RadioGroup","children":["a","b"]}""",
          """{"id":"a","component":"RadioButton","label":"Small","selected":true,"sizeClass":{"medium":{"selected":false}}}""",
          """{"id":"b","component":"RadioButton","label":"Large","sizeClass":{"medium":{"selected":true}}}""",
        ),
      expect =
        listOf(
          "var rootSelected by remember { mutableStateOf(if (!atLeastMedium) \"a\" else \"b\") }",
        ),
    ),
    case(
      "Switch",
      components = listOf(ROOT_COLUMN, """{"id":"x","component":"Switch","checked":true}"""),
      expect =
        listOf(
          "var xChecked by remember { mutableStateOf(true) }",
          "Switch(checked = xChecked, onCheckedChange = { xChecked = it })",
        ),
    ),
    case(
      "Checkbox",
      components = listOf(ROOT_COLUMN, """{"id":"x","component":"Checkbox","label":"Agree"}"""),
      expect =
        listOf(
          "var xChecked by remember { mutableStateOf(false) }",
          "Row(verticalAlignment = Alignment.CenterVertically) {",
          "Checkbox(checked = xChecked, onCheckedChange = { xChecked = it })",
          "Text(\"Agree\")",
        ),
    ),
    case(
      "Slider",
      components =
        listOf(
          ROOT_COLUMN,
          """{"id":"x","component":"Slider","value":40,"range":[0,100],"steps":4}""",
        ),
      expect =
        listOf(
          "var xValue by remember { mutableFloatStateOf(40f) }",
          "Slider(",
          "valueRange = 0f..100f",
          "steps = 4",
          "import androidx.compose.runtime.mutableFloatStateOf",
        ),
    ),
    case(
      "RangeSlider",
      components =
        listOf(
          ROOT_COLUMN,
          """{"id":"x","component":"RangeSlider","value":[20,80],"range":[0,100]}""",
        ),
      expect =
        listOf(
          "var xValue by remember { mutableStateOf(20f..80f) }",
          "RangeSlider(",
          "valueRange = 0f..100f",
        ),
    ),
    case(
      "TextField",
      components =
        listOf(
          ROOT_COLUMN,
          """{"id":"x","component":"TextField","variant":"outlined","label":"Name","placeholder":"Ada","value":"Grace","supporting":"Required","error":true,"leadingIcon":"person","trailingIcon":"close","prefix":"@","lines":3}""",
        ),
      expect =
        listOf(
          "var xValue by remember { mutableStateOf(\"Grace\") }",
          "OutlinedTextField(",
          "value = xValue",
          "onValueChange = { xValue = it }",
          "label = { Text(\"Name\") }",
          "placeholder = { Text(\"Ada\") }",
          "leadingIcon = {",
          "trailingIcon = {",
          "prefix = { Text(\"@\") }",
          "supportingText = { Text(\"Required\") }",
          "isError = true",
          "minLines = 3",
        ),
      reject = listOf("singleLine"),
    ),
    case(
      "TextField",
      components = listOf(ROOT_COLUMN, """{"id":"x","component":"TextField","label":"Name"}"""),
      expect = listOf("TextField(", "singleLine = true"),
    ),
    // Feedback
    case(
      "LoadingIndicator",
      components =
        listOf(
          """{"id":"root","component":"Row","children":["x","y"]}""",
          """{"id":"x","component":"LoadingIndicator"}""",
          """{"id":"y","component":"LoadingIndicator","contained":true}""",
        ),
      expect = listOf("LoadingIndicator()", "ContainedLoadingIndicator()"),
    ),
    case(
      "ProgressIndicator",
      components =
        listOf(
          """{"id":"root","component":"Column","children":["x","y","z"]}""",
          """{"id":"x","component":"ProgressIndicator","progress":0.4}""",
          """{"id":"y","component":"ProgressIndicator","shape":"circular","wavy":true}""",
          """{"id":"z","component":"ProgressIndicator","shape":"circular","progress":0.5}""",
        ),
      expect =
        listOf(
          "LinearProgressIndicator(progress = { 0.4f }, modifier = Modifier.fillMaxWidth())",
          "CircularWavyProgressIndicator()",
          "CircularProgressIndicator(progress = { 0.5f })",
        ),
    ),
    // Overlays
    case(
      "BottomSheet",
      components =
        listOf(
          """{"id":"root","component":"Column","children":["b"]}""",
          """{"id":"b","component":"Button","label":"Open","action":{"functionCall":{"@call":"open","args":{"overlay":"sheet"}}}}""",
          """{"id":"sheet","component":"BottomSheet","content":["t","close"]}""",
          """{"id":"t","component":"Text","text":"Sheet"}""",
          """{"id":"close","component":"Button","label":"Close","action":{"functionCall":{"@call":"dismiss"}}}""",
        ),
      expect =
        listOf(
          "var showSheet by remember { mutableStateOf(false) }",
          "onClick = { showSheet = true }",
          "if (showSheet) {",
          "ModalBottomSheet(onDismissRequest = { showSheet = false }) {",
          "onClick = { showSheet = false }",
        ),
    ),
    case(
      "Dialog",
      components =
        listOf(
          """{"id":"root","component":"Column","children":["b"]}""",
          """{"id":"b","component":"Button","label":"Open","action":{"functionCall":{"@call":"open","args":{"overlay":"d"}}}}""",
          """{"id":"d","component":"Dialog","title":"Delete?","text":"This cannot be undone.","confirm":"ok","dismiss":"cancel"}""",
          """{"id":"ok","component":"Button","variant":"text","label":"Delete","action":{"functionCall":{"@call":"dismiss"}}}""",
          """{"id":"cancel","component":"Button","variant":"text","label":"Cancel","action":{"functionCall":{"@call":"dismiss"}}}""",
        ),
      expect =
        listOf(
          "if (showD) {",
          "AlertDialog(",
          "onDismissRequest = { showD = false }",
          "confirmButton = {",
          "dismissButton = {",
          "title = { Text(\"Delete?\") }",
          "text = { Text(\"This cannot be undone.\") }",
          "TextButton(onClick = { showD = false }) { Text(\"Delete\") }",
        ),
    ),
    case(
      "Menu",
      components =
        listOf(
          """{"id":"root","component":"Column","children":["b"]}""",
          """{"id":"b","component":"Button","label":"More","action":{"functionCall":{"@call":"open","args":{"overlay":"m"}}}}""",
          """{"id":"m","component":"Menu","anchor":"b","items":["i1"]}""",
          """{"id":"i1","component":"MenuItem","label":"Edit"}""",
        ),
      expect =
        listOf(
          "Box {",
          "DropdownMenuPopup(",
          "expanded = showM",
          "onClick = { showM = false }",
          "Text(\"Edit\")",
        ),
    ),
    case(
      "SideSheet",
      components =
        listOf(
          """{"id":"root","component":"Column","children":["b"]}""",
          """{"id":"b","component":"Button","label":"Filters","action":{"functionCall":{"@call":"open","args":{"overlay":"filters"}}}}""",
          """{"id":"filters","component":"SideSheet","title":"Filters","back":{"functionCall":{"@call":"open","args":{"overlay":"more"}}},"content":["t"]}""",
          """{"id":"t","component":"Text","text":"Sort by"}""",
          """{"id":"more","component":"SideSheet","title":"More","content":[]}""",
        ),
      expect =
        listOf(
          "var showFilters by remember { mutableStateOf(false) }",
          "onClick = { showFilters = true }",
          "if (showFilters) {",
          "Dialog(",
          "onDismissRequest = { showFilters = false }",
          "properties = DialogProperties(usePlatformDefaultWidth = false)",
          "clickable(interactionSource = null, indication = null) { showFilters = false }",
          "Modifier.align(Alignment.CenterEnd).widthIn(max = 400.dp).fillMaxWidth().fillMaxHeight()",
          "shape = RoundedCornerShape(topStart = 16.dp, topEnd = 0.dp, bottomEnd = 0.dp, bottomStart = 16.dp)",
          "color = MaterialTheme.colorScheme.surfaceContainerLow",
          "Modifier.fillMaxSize().padding(vertical = 24.dp)",
          "horizontalArrangement = Arrangement.spacedBy(12.dp)",
          "Modifier.fillMaxWidth().padding(start = 16.dp, end = 24.dp)",
          "contentDescription = \"Back\"",
          "onClick = { showMore = true\n showFilters = false }",
          "Text(\"Filters\", modifier = Modifier.weight(1f), style = MaterialTheme.typography.titleLarge",
          "contentDescription = \"Close\"",
          "Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 24.dp)",
          "Text(\"Sort by\")",
          "Modifier.fillMaxWidth().padding(start = 24.dp, end = 24.dp)",
        ),
    ),
    case(
      "SearchBar",
      "SearchView",
      components =
        listOf(
          """{"id":"root","component":"Column","children":["bar","open"]}""",
          """{"id":"bar","component":"SearchBar","placeholder":"Search mail","trailing":["mic"],"expanded":"results"}""",
          """{"id":"mic","component":"IconButton","icon":"mic","contentDescription":"Voice search"}""",
          """{"id":"open","component":"Button","label":"Search","action":{"functionCall":{"@call":"open","args":{"overlay":"results"}}}}""",
          """{"id":"results","component":"SearchView","content":["r"]}""",
          """{"id":"r","component":"Text","text":"Recent"}""",
        ),
      expect =
        listOf(
          "val coroutineScope = rememberCoroutineScope()",
          "val barSearchState = rememberContainedSearchBarState()",
          "val barQuery = rememberTextFieldState()",
          "val barSearchColors = SearchBarDefaults.containedColors(state = barSearchState)",
          "val barInput = @Composable {",
          "SearchBarDefaults.InputField(",
          "textFieldState = barQuery",
          "searchBarState = barSearchState",
          "placeholder = { Text(\"Search mail\") }",
          "if (barSearchState.currentValue == SearchBarValue.Expanded) {",
          "coroutineScope.launch { barSearchState.animateToCollapsed() }",
          "Icon(painterResource(R.drawable.search), contentDescription = null)",
          "SearchBar(",
          "state = barSearchState",
          "inputField = barInput",
          "colors = barSearchColors",
          "ExpandedFullScreenContainedSearchBar(",
          "onClick = { coroutineScope.launch { barSearchState.animateToExpanded() } }",
          "Text(\"Recent\")",
        ),
      reject = listOf("showResults", "ExpandedFullScreenSearchBar(", "ExpandedDockedSearchBar("),
    ),
    case(
      "TopAppBar",
      components =
        listOf(
          """{"id":"root","component":"Scaffold","topBar":"bar","content":"x"}""",
          """{"id":"bar","component":"TopAppBar","size":"search","title":"Search inbox","navigationIcon":"menu","actions":["cast"],"expanded":"results"}""",
          """{"id":"menu","component":"IconButton","icon":"menu","contentDescription":"Menu"}""",
          """{"id":"cast","component":"IconButton","icon":"cast","contentDescription":"Cast"}""",
          """{"id":"results","component":"SearchView","layout":"docked","content":[]}""",
          """{"id":"x","component":"Column","children":[]}""",
        ),
      expect =
        listOf(
          "val barSearchState = rememberSearchBarWithGapState()",
          "val barSearchColors = SearchBarDefaults.colors()",
          "placeholder = { Text(\"Search inbox\") }",
          "AppBarWithSearch(",
          "navigationIcon = {",
          "actions = {",
          "colors = SearchBarDefaults.appBarWithSearchColors(searchBarColors = barSearchColors)",
          "ExpandedDockedSearchBarWithGap(state = barSearchState, inputField = barInput) {}",
        ),
    ),
    case(
      "Tooltip",
      components =
        listOf(
          """{"id":"root","component":"Column","children":["info","bar"]}""",
          """{"id":"info","component":"IconButton","icon":"info","contentDescription":"Info"}""",
          """{"id":"tip","component":"Tooltip","anchor":"info","variant":"rich","title":"Why","text":"Because.","actions":["ok"]}""",
          """{"id":"ok","component":"Button","variant":"text","label":"Got it","action":{"functionCall":{"@call":"dismiss"}}}""",
          """{"id":"bar","component":"IconButton","icon":"edit","contentDescription":"Edit"}""",
          """{"id":"plain","component":"Tooltip","anchor":"bar","text":"Edit"}""",
        ),
      expect =
        listOf(
          "val tipTooltipState = rememberTooltipState(isPersistent = true)",
          "val plainTooltipState = rememberTooltipState()",
          "TooltipBox(",
          "TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Below)",
          "TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Above)",
          "RichTooltip(",
          "title = { Text(\"Why\") }",
          "Row(horizontalArrangement = Arrangement.spacedBy(8.dp))",
          "coroutineScope.launch { tipTooltipState.dismiss() }",
          "state = tipTooltipState",
          "hasAction = true",
          "Text(\"Because.\")",
          "PlainTooltip { Text(\"Edit\") }",
        ),
      reject = listOf("showTip"),
    ),
    case(
      "Snackbar",
      components =
        listOf(
          """{"id":"root","component":"Scaffold","content":"body","floatingActionButton":"fab"}""",
          """{"id":"body","component":"Column","children":["b"]}""",
          """{"id":"b","component":"Button","label":"Delete","action":{"functionCall":{"@call":"open","args":{"overlay":"undo"}}}}""",
          """{"id":"fab","component":"FloatingActionButton","icon":"add"}""",
          """{"id":"undo","component":"Snackbar","message":"Plate deleted","actionLabel":"Undo","dismiss":true,"action":{"functionCall":{"@call":"open","args":{"overlay":"restored"}}}}""",
          """{"id":"restored","component":"Snackbar","message":"Restored"}""",
        ),
      expect =
        listOf(
          "val snackbarHostState = remember { SnackbarHostState() }",
          "var showUndo by remember { mutableStateOf(false) }",
          "snackbarHost = { SnackbarHost(snackbarHostState) }",
          "LaunchedEffect(showUndo) {",
          "if (showUndo) {",
          "val result = snackbarHostState.showSnackbar(message = \"Plate deleted\", actionLabel = \"Undo\", withDismissAction = true)",
          "showUndo = false",
          "if (result == SnackbarResult.ActionPerformed) {",
          "showRestored = true",
          "LaunchedEffect(showRestored) {",
          "snackbarHostState.showSnackbar(message = \"Restored\")",
        ),
    ),
    case(
      "NavigationBarItem",
      components =
        listOf(
          """{"id":"root","component":"Scaffold","content":"body","bottomBar":"bar"}""",
          """{"id":"body","component":"Column","children":[]}""",
          """{"id":"bar","component":"NavigationBar","children":["a","b","c"]}""",
          """{"id":"a","component":"NavigationBarItem","label":"Inbox","icon":"mail","badge":true}""",
          """{"id":"b","component":"NavigationBarItem","label":"Chat","icon":"chat","badge":12}""",
          """{"id":"c","component":"NavigationBarItem","label":"Saved","icon":"bookmark","badge":false}""",
        ),
      expect =
        listOf(
          "BadgedBox(badge = { Badge() }) { Icon(painterResource(R.drawable.mail), contentDescription = null) }",
          "BadgedBox(badge = { Badge { Text(\"12\") } }) { Icon(painterResource(R.drawable.chat), contentDescription = null) }",
        ),
      reject = listOf("BadgedBox(badge = { Badge() }) { Icon(painterResource(R.drawable.bookmark)"),
    ),
    case(
      "NavigationRailItem",
      components =
        listOf(
          """{"id":"root","component":"Scaffold","content":"body","navigationRail":"rail"}""",
          """{"id":"body","component":"Column","children":[]}""",
          """{"id":"rail","component":"NavigationRail","children":["a"]}""",
          """{"id":"a","component":"NavigationRailItem","label":"Inbox","icon":"mail","badge":"99+"}""",
        ),
      expect =
        listOf(
          "BadgedBox(badge = { Badge { Text(\"99+\") } }) { Icon(painterResource(R.drawable.mail)",
        ),
    ),
    case(
      "Tab",
      components =
        listOf(
          """{"id":"root","component":"Tabs","children":["a","b"]}""",
          """{"id":"a","component":"Tab","label":"Plates","icon":"restaurant","badge":true,"selected":true}""",
          """{"id":"b","component":"Tab","label":"Menus","badge":3}""",
        ),
      expect =
        listOf(
          "icon = { BadgedBox(badge = { Badge() }) { Icon(painterResource(R.drawable.restaurant)",
          "text = { BadgedBox(badge = { Badge { Text(\"3\") } }) { Text(\"Menus\") } }",
          "text = { Text(\"Plates\") }",
        ),
    ),
    case(
      "IconButton",
      components =
        listOf(
          """{"id":"root","component":"Column","children":["a","b"]}""",
          """{"id":"a","component":"IconButton","icon":"notifications","badge":true}""",
          """{"id":"b","component":"IconButton","icon":"mail","toggle":true,"badge":"new"}""",
        ),
      expect =
        listOf(
          "IconButton(onClick = {}",
          "BadgedBox(badge = { Badge() }) { Icon(painterResource(R.drawable.notifications)",
          "BadgedBox(badge = { Badge { Text(\"new\") } }) { Icon(",
        ),
    ),
  )
