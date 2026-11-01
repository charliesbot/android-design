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
          "modifier = modifier.verticalScroll(rememberScrollState()).padding(12.dp)",
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
          "Box(modifier = modifier, contentAlignment = Alignment.BottomEnd) {",
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
          "Modifier.aspectRatio(1.5f).clip(MaterialTheme.shapes.large)",
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
          "modifier = Modifier.clickable { onBack() }",
          "supportingContent = { Text(\"Sup\") }",
          ") { Text(\"One\") }",
        ),
      reject = listOf("SegmentedListItem", "headlineContent"),
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
          "LinearProgressIndicator(progress = { 0.4f })",
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
  )
