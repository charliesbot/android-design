package bot.charlies.androiddesign.preview.domain.export.wear

import bot.charlies.androiddesign.preview.domain.export.wear.WearExportFixtures.root

/**
 * One Wear export per catalog component: the proposal's components (the root `Screen` first) and
 * the pieces of code its export must contain or must not contain. The tests fold whitespace, so a
 * line break does not matter.
 */
internal class WearCase(
  val covers: List<String>,
  val components: List<String>,
  val expect: List<String>,
  val reject: List<String> = emptyList(),
)

private const val ITEM = "Modifier.fillMaxWidth().transformedHeight(this, transformationSpec)"
private const val TRANSFORM = "transformation = SurfaceTransformation(transformationSpec)"
private const val SIZE_ONLY = "Modifier.transformedHeight(this, transformationSpec)"

private fun icon(drawable: String, size: String) =
  "Icon(painterResource(R.drawable.$drawable), contentDescription = null, modifier = Modifier.size($size))"

internal val wearCases: List<WearCase> =
  listOf(
    WearCase(
      covers = listOf("Column", "Spacer"),
      components =
        listOf(
          root("col"),
          """{"id":"col","component":"Column","spacing":"sm","horizontalAlignment":"center","children":["t","gap"]}""",
          """{"id":"t","component":"Text","text":"Hi"}""",
          """{"id":"gap","component":"Spacer","height":"md"}""",
        ),
      expect =
        listOf(
          "Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp), horizontalAlignment = Alignment.CenterHorizontally)",
          "Spacer(Modifier.height(12.dp))",
          "Text(\"Hi\")",
        ),
      // A layout is not a list item: only the components the upstream scope transforms are.
      reject = listOf("Column(modifier = Modifier.fillMaxWidth().transformedHeight"),
    ),
    WearCase(
      covers = listOf("Row", "Box"),
      components =
        listOf(
          root("row"),
          """{"id":"row","component":"Row","verticalAlignment":"center","arrangement":"spaceBetween","children":["box"]}""",
          """{"id":"box","component":"Box","contentAlignment":"center","children":["t"]}""",
          """{"id":"t","component":"Text","text":"Hi"}""",
        ),
      expect =
        listOf(
          "Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically)",
          "Box(contentAlignment = Alignment.Center)",
        ),
    ),
    WearCase(
      covers = listOf("ListHeader"),
      components = listOf(root("h"), """{"id":"h","component":"ListHeader","text":"Timer"}"""),
      expect = listOf("ListHeader(modifier = $ITEM, $TRANSFORM) { Text(\"Timer\") }"),
    ),
    WearCase(
      covers = listOf("ListSubHeader"),
      components =
        listOf(
          root("h"),
          """{"id":"h","component":"ListSubHeader","text":"Sub","icon":"timer"}""",
        ),
      expect =
        listOf(
          "ListSubHeader(modifier = $ITEM, $TRANSFORM, icon = { ${icon("timer", "ButtonDefaults.IconSize")} }) { Text(\"Sub\") }",
        ),
    ),
    WearCase(
      covers = listOf("Button"),
      components =
        listOf(
          root("a", "b", "c", "d", "e"),
          """{"id":"a","component":"Button","label":"Go","secondaryLabel":"Now","icon":"play_arrow","contentDescription":"Start the timer"}""",
          """{"id":"b","component":"Button","variant":"filledTonal","label":"Tonal","enabled":false}""",
          """{"id":"c","component":"Button","variant":"filledVariant","label":"Variant"}""",
          """{"id":"d","component":"Button","variant":"outlined","label":"Outlined"}""",
          """{"id":"e","component":"Button","variant":"child","label":"Child"}""",
        ),
      expect =
        listOf(
          "Button(onClick = {}, modifier = Modifier.fillMaxWidth().semantics { contentDescription = \"Start the timer\" }.transformedHeight(this, transformationSpec), secondaryLabel = { Text(\"Now\") }, icon = { ${icon("play_arrow", "ButtonDefaults.IconSize")} }, $TRANSFORM) { Text(\"Go\") }",
          "FilledTonalButton(onClick = {}, modifier = $ITEM, enabled = false, $TRANSFORM) { Text(\"Tonal\") }",
          "Button(onClick = {}, modifier = $ITEM, colors = ButtonDefaults.filledVariantButtonColors(), $TRANSFORM) { Text(\"Variant\") }",
          "OutlinedButton(onClick = {}, modifier = $ITEM, $TRANSFORM) { Text(\"Outlined\") }",
          "ChildButton(onClick = {}, modifier = $ITEM, $TRANSFORM) { Text(\"Child\") }",
        ),
    ),
    WearCase(
      covers = listOf("CompactButton"),
      components =
        listOf(
          root("a", "b"),
          """{"id":"a","component":"CompactButton","label":"Add","icon":"add"}""",
          """{"id":"b","component":"CompactButton","variant":"outlined","label":"Skip"}""",
        ),
      expect =
        listOf(
          "CompactButton(onClick = {}, modifier = $SIZE_ONLY, icon = { ${icon("add", "ButtonDefaults.IconSize")} }) { Text(\"Add\") }",
          "CompactButton(onClick = {}, modifier = $SIZE_ONLY, colors = ButtonDefaults.outlinedButtonColors(), border = ButtonDefaults.outlinedButtonBorder(enabled = true)) { Text(\"Skip\") }",
        ),
    ),
    WearCase(
      covers = listOf("IconButton"),
      components =
        listOf(
          root("a", "b", "c", "d"),
          """{"id":"a","component":"IconButton","icon":"add"}""",
          """{"id":"b","component":"IconButton","variant":"filled","icon":"add","size":"large"}""",
          """{"id":"c","component":"IconButton","variant":"filledTonal","icon":"add","size":"small"}""",
          """{"id":"d","component":"IconButton","variant":"outlined","icon":"add","size":"extra-small"}""",
        ),
      expect =
        listOf(
          "IconButton(onClick = {}, modifier = $SIZE_ONLY.size(IconButtonDefaults.DefaultButtonSize)) { Icon(painterResource(R.drawable.add), contentDescription = null, modifier = Modifier.size(IconButtonDefaults.iconSizeFor(IconButtonDefaults.DefaultButtonSize))) }",
          "FilledIconButton(onClick = {}, modifier = $SIZE_ONLY.size(IconButtonDefaults.LargeButtonSize))",
          "FilledTonalIconButton(onClick = {}, modifier = $SIZE_ONLY.size(IconButtonDefaults.SmallButtonSize))",
          "OutlinedIconButton(onClick = {}, modifier = $SIZE_ONLY.size(IconButtonDefaults.ExtraSmallButtonSize))",
        ),
    ),
    WearCase(
      covers = listOf("TextButton"),
      components =
        listOf(
          root("a", "b", "c"),
          """{"id":"a","component":"TextButton","label":"5"}""",
          """{"id":"b","component":"TextButton","variant":"filled","size":"large","label":"AB"}""",
          """{"id":"c","component":"TextButton","variant":"outlined","size":"small","label":"7"}""",
        ),
      expect =
        listOf(
          "TextButton(onClick = {}, modifier = $SIZE_ONLY.size(TextButtonDefaults.DefaultButtonSize)) { Text(\"5\") }",
          "TextButton(onClick = {}, modifier = $SIZE_ONLY.size(TextButtonDefaults.LargeButtonSize), colors = TextButtonDefaults.filledTextButtonColors()) { Text(\"AB\") }",
          "colors = TextButtonDefaults.outlinedTextButtonColors(), border = ButtonDefaults.outlinedButtonBorder(enabled = true)",
        ),
    ),
    WearCase(
      covers = listOf("EdgeButton"),
      components =
        listOf(
          root("t", props = """"timeText":true,"edgeButton":"go""""),
          """{"id":"t","component":"Text","text":"Hi"}""",
          """{"id":"go","component":"EdgeButton","size":"medium","label":"Start","icon":"play_arrow"}""",
        ),
      expect =
        listOf(
          "edgeButton = { EdgeButton(onClick = {}, buttonSize = EdgeButtonSize.Medium) { Icon(painterResource(R.drawable.play_arrow), contentDescription = null, modifier = Modifier.size(EdgeButtonDefaults.iconSizeFor(EdgeButtonSize.Medium))) Text(\"Start\") } }",
        ),
      // The button sits in the scaffold's slot, not in the list.
      reject = listOf("item { EdgeButton"),
    ),
    WearCase(
      covers = listOf("ButtonGroup"),
      components =
        listOf(
          root("g"),
          """{"id":"g","component":"ButtonGroup","children":["minus","plus"]}""",
          """{"id":"minus","component":"IconButton","variant":"filledTonal","icon":"remove"}""",
          """{"id":"plus","component":"IconButton","variant":"filled","icon":"add"}""",
        ),
      expect =
        listOf(
          "ButtonGroup(modifier = $ITEM) {",
          "FilledTonalIconButton(onClick = {}, modifier = Modifier.weight(1f).size(IconButtonDefaults.DefaultButtonSize))",
          "FilledIconButton(onClick = {}, modifier = Modifier.weight(1f).size(IconButtonDefaults.DefaultButtonSize))",
        ),
    ),
    WearCase(
      covers = listOf("IconToggleButton"),
      components =
        listOf(
          root("t"),
          """{"id":"t","component":"IconToggleButton","icon":"star","checked":true,"size":"large"}""",
        ),
      expect =
        listOf(
          "var tChecked by remember { mutableStateOf(true) }",
          "IconToggleButton(checked = tChecked, onCheckedChange = { tChecked = it }, modifier = $SIZE_ONLY.size(IconButtonDefaults.LargeButtonSize)) { Icon(painterResource(if (tChecked) R.drawable.star_filled else R.drawable.star), contentDescription = null, modifier = Modifier.size(IconToggleButtonDefaults.iconSizeFor(IconButtonDefaults.LargeButtonSize))) }",
        ),
    ),
    WearCase(
      covers = listOf("TextToggleButton"),
      components =
        listOf(
          root("t", "u"),
          """{"id":"t","component":"TextToggleButton","label":"A"}""",
          """{"id":"u","component":"TextToggleButton","label":"B","size":"small","checked":true}""",
        ),
      expect =
        listOf(
          "var tChecked by remember { mutableStateOf(false) }",
          "TextToggleButton(checked = tChecked, onCheckedChange = { tChecked = it }, modifier = $SIZE_ONLY.size(TextToggleButtonDefaults.Size)) { Text(\"A\") }",
          "modifier = $SIZE_ONLY.size(IconToggleButtonDefaults.SmallSize)) { Text(\"B\") }",
        ),
    ),
    WearCase(
      covers = listOf("SwitchButton"),
      components =
        listOf(
          root("s"),
          """{"id":"s","component":"SwitchButton","label":"Vibrate","secondaryLabel":"On finish","icon":"vibration","checked":true,"action":{"functionCall":{"@call":"back"}}}""",
        ),
      expect =
        listOf(
          "var sChecked by remember { mutableStateOf(true) }",
          "SwitchButton(checked = sChecked, onCheckedChange = { sChecked = it onBack() }, modifier = $ITEM, $TRANSFORM, icon = { ${icon("vibration", "ButtonDefaults.IconSize")} }, secondaryLabel = { Text(\"On finish\") }) { Text(\"Vibrate\") }",
        ),
    ),
    WearCase(
      covers = listOf("CheckboxButton"),
      components =
        listOf(
          root("s"),
          """{"id":"s","component":"CheckboxButton","label":"Sound"}""",
        ),
      expect =
        listOf(
          "var sChecked by remember { mutableStateOf(false) }",
          "CheckboxButton(checked = sChecked, onCheckedChange = { sChecked = it }, modifier = $ITEM, $TRANSFORM) { Text(\"Sound\") }",
        ),
    ),
    WearCase(
      covers = listOf("RadioButton"),
      components =
        listOf(
          root("a", "b"),
          """{"id":"a","component":"RadioButton","label":"Short"}""",
          """{"id":"b","component":"RadioButton","label":"Long","selected":true}""",
        ),
      expect =
        listOf(
          "var rootSelected by remember { mutableIntStateOf(1) }",
          "RadioButton(selected = rootSelected == 0, onSelect = { rootSelected = 0 }, modifier = $ITEM, $TRANSFORM) { Text(\"Short\") }",
          "RadioButton(selected = rootSelected == 1, onSelect = { rootSelected = 1 }, modifier = $ITEM, $TRANSFORM) { Text(\"Long\") }",
        ),
    ),
    WearCase(
      covers = listOf("Text"),
      components =
        listOf(
          root("t", "u"),
          """{"id":"t","component":"Text","text":"05:00","type":"numeralLarge","color":"primaryDim","align":"center","maxLines":1,"overflow":"ellipsis"}""",
          """{"id":"u","component":"Text","text":"Plain","padding":"sm"}""",
        ),
      expect =
        listOf(
          "Text(\"05:00\", modifier = $ITEM, color = MaterialTheme.colorScheme.primaryDim, style = MaterialTheme.typography.numeralLarge, textAlign = TextAlign.Center, overflow = TextOverflow.Ellipsis, maxLines = 1)",
          "Text(\"Plain\", modifier = Modifier.padding(8.dp).transformedHeight(this, transformationSpec))",
        ),
    ),
    WearCase(
      covers = listOf("Icon"),
      components =
        listOf(
          root("a", "b"),
          """{"id":"a","component":"Icon","name":"timer"}""",
          """{"id":"b","component":"Icon","name":"star","size":32,"color":"primary","filled":true,"contentDescription":"Favorite"}""",
        ),
      expect =
        listOf(
          "Icon(painterResource(R.drawable.timer), contentDescription = null, modifier = $SIZE_ONLY.size(24.dp))",
          "Icon(painterResource(R.drawable.star_filled), contentDescription = \"Favorite\", modifier = $SIZE_ONLY.size(32.dp), tint = MaterialTheme.colorScheme.primary)",
        ),
    ),
    WearCase(
      covers = listOf("Image"),
      components =
        listOf(
          root("i"),
          """{"id":"i","component":"Image","src":"cover.png","fit":"crop","aspectRatio":1.5,"contentDescription":"Cover"}""",
        ),
      expect =
        listOf(
          "Image(painter = painterResource(R.drawable.cover), contentDescription = \"Cover\", modifier = Modifier.fillMaxWidth().aspectRatio(1.5f), contentScale = ContentScale.Crop)",
        ),
    ),
    WearCase(
      covers = listOf("Card"),
      components =
        listOf(
          root("a", "b"),
          """{"id":"a","component":"Card","children":["t"],"action":{"functionCall":{"@call":"back"}}}""",
          """{"id":"t","component":"Text","text":"In card"}""",
          """{"id":"b","component":"Card","children":["u"]}""",
          """{"id":"u","component":"Text","text":"Static"}""",
        ),
      expect =
        listOf(
          "Card(onClick = { onBack() }, modifier = $ITEM, $TRANSFORM) { Text(\"In card\", style = MaterialTheme.typography.bodyLarge) }",
          "Card(modifier = $ITEM, $TRANSFORM) { Text(\"Static\", style = MaterialTheme.typography.bodyLarge) }",
        ),
    ),
    WearCase(
      covers = listOf("TitleCard"),
      components =
        listOf(
          root("a", "b"),
          """{"id":"a","component":"TitleCard","title":"Weekly goal","time":"Now","subtitle":"Sub","body":"3 of 5","action":{"functionCall":{"@call":"back"}}}""",
          """{"id":"b","component":"TitleCard","title":"Quiet"}""",
        ),
      expect =
        listOf(
          "TitleCard(onClick = { onBack() }, title = { Text(\"Weekly goal\") }, modifier = $ITEM, time = { Text(\"Now\") }, subtitle = { Text(\"Sub\") }, $TRANSFORM, content = { Text(\"3 of 5\") })",
          "TitleCard(title = { Text(\"Quiet\") }, modifier = $ITEM, $TRANSFORM)",
        ),
    ),
    WearCase(
      covers = listOf("AppCard"),
      components =
        listOf(
          root("a"),
          """{"id":"a","component":"AppCard","appName":"Fit","appIcon":"timer","title":"Run done","time":"2m","body":"5 km","action":{"functionCall":{"@call":"back"}}}""",
        ),
      expect =
        listOf(
          "AppCard(onClick = { onBack() }, appName = { Text(\"Fit\") }, title = { Text(\"Run done\") }, modifier = $ITEM, $TRANSFORM, appImage = { ${icon("timer", "CardDefaults.AppImageSize")} }, time = { Text(\"2m\") }) { Text(\"5 km\") }",
        ),
    ),
    WearCase(
      covers = listOf("Slider"),
      components =
        listOf(
          root("s", "t"),
          """{"id":"s","component":"Slider","value":3,"range":[0,10],"steps":4,"enabled":false}""",
          """{"id":"t","component":"Slider","steps":3}""",
        ),
      expect =
        listOf(
          "var sValue by remember { mutableFloatStateOf(3f) }",
          "Slider(value = sValue, onValueChange = { sValue = it }, steps = 4, modifier = $ITEM, enabled = false, valueRange = 0f..10f)",
          "var tValue by remember { mutableFloatStateOf(0f) }",
          "valueRange = 0f..1f",
        ),
    ),
    WearCase(
      covers = listOf("Stepper"),
      components =
        listOf(
          root("s"),
          """{"id":"s","component":"Stepper","value":5,"range":[1,30],"steps":28,"content":"m"}""",
          """{"id":"m","component":"Text","text":"5 min","type":"numeralSmall"}""",
        ),
      expect =
        listOf(
          "var sValue by remember { mutableFloatStateOf(5f) }",
          "Stepper(value = sValue, onValueChange = { sValue = it }, steps = 28, decreaseIcon = { SliderDefaults.DecreaseIcon() }, increaseIcon = { SliderDefaults.IncreaseIcon() }, modifier = $ITEM, valueRange = 1f..30f) { Text(\"5 min\", style = MaterialTheme.typography.numeralSmall) }",
        ),
    ),
    WearCase(
      covers = listOf("CircularProgressIndicator", "SegmentedCircularProgressIndicator"),
      components =
        listOf(
          root("a", "b", "c", props = """"scroll":false"""),
          """{"id":"a","component":"CircularProgressIndicator","progress":0.6}""",
          """{"id":"b","component":"SegmentedCircularProgressIndicator","segmentCount":5,"progress":0.4}""",
          """{"id":"c","component":"CircularProgressIndicator"}""",
        ),
      expect =
        listOf(
          "CircularProgressIndicator(progress = { 0.6f }, modifier = Modifier.fillMaxSize().padding(CircularProgressIndicatorDefaults.FullScreenPadding), strokeWidth = CircularProgressIndicatorDefaults.largeStrokeWidth)",
          "SegmentedCircularProgressIndicator(segmentCount = 5, progress = { 0.4f }, modifier = Modifier.fillMaxSize().padding(CircularProgressIndicatorDefaults.FullScreenPadding), strokeWidth = CircularProgressIndicatorDefaults.largeStrokeWidth)",
          "CircularProgressIndicator(modifier = Modifier.fillMaxSize().padding(CircularProgressIndicatorDefaults.FullScreenPadding), strokeWidth = CircularProgressIndicatorDefaults.IndeterminateStrokeWidth)",
        ),
    ),
    WearCase(
      covers = listOf("CircularProgressIndicator"),
      components =
        listOf(
          root("r"),
          """{"id":"r","component":"CircularProgressIndicator","progress":0.5}""",
        ),
      // Inside a list the ring is a small spinner, not the display's edge.
      expect =
        listOf(
          "CircularProgressIndicator(progress = { 0.5f }, modifier = Modifier.size(48.dp), strokeWidth = CircularProgressIndicatorDefaults.largeStrokeWidth)",
        ),
      reject = listOf("FullScreenPadding"),
    ),
    WearCase(
      covers = listOf("LinearProgressIndicator"),
      components =
        listOf(
          root("l"),
          """{"id":"l","component":"LinearProgressIndicator","progress":0.25}""",
        ),
      expect = listOf("LinearProgressIndicator(progress = { 0.25f }, modifier = $ITEM)"),
    ),
    WearCase(
      covers = listOf("AlertDialog"),
      components =
        listOf(
          root("open"),
          """{"id":"open","component":"Button","label":"Reset","action":{"functionCall":{"@call":"open","args":{"overlay":"sure"}}}}""",
          """{"id":"sure","component":"AlertDialog","title":"Reset?","text":"Starts over.","icon":"restart_alt","confirm":"yes","dismiss":"no"}""",
          """{"id":"yes","component":"IconButton","variant":"filled","icon":"check","action":{"functionCall":{"@call":"dismiss"}}}""",
          """{"id":"no","component":"IconButton","variant":"filledTonal","icon":"close","action":{"functionCall":{"@call":"dismiss"}}}""",
        ),
      expect =
        listOf(
          "var showSure by remember { mutableStateOf(false) }",
          "onClick = { showSure = true }",
          "AlertDialog(visible = showSure, onDismissRequest = { showSure = false }, confirmButton = { FilledIconButton(onClick = { showSure = false }, modifier = Modifier.weight(1f).size(IconButtonDefaults.DefaultButtonSize))",
          "title = { Text(\"Reset?\") }",
          "dismissButton = { FilledTonalIconButton(onClick = { showSure = false }, modifier = Modifier.weight(1f).size(IconButtonDefaults.DefaultButtonSize))",
          "icon = { ${icon("restart_alt", "32.dp")} }",
          "text = { Text(\"Starts over.\") }",
        ),
    ),
    WearCase(
      covers = listOf("AlertDialog"),
      components =
        listOf(
          root("open"),
          """{"id":"open","component":"Button","label":"Reset","action":{"functionCall":{"@call":"open","args":{"overlay":"sure"}}}}""",
          """{"id":"sure","component":"AlertDialog","title":"Reset?"}""",
        ),
      // With no buttons of its own the dialog uses the upstream confirm and dismiss buttons.
      expect =
        listOf(
          "confirmButton = { AlertDialogDefaults.ConfirmButton(onClick = { showSure = false }) }",
        ),
      reject = listOf("dismissButton ="),
    ),
    WearCase(
      covers = listOf("ConfirmationDialog"),
      components =
        listOf(
          root("open"),
          """{"id":"open","component":"Button","label":"Done","action":{"functionCall":{"@call":"open","args":{"overlay":"ok"}}}}""",
          """{"id":"ok","component":"ConfirmationDialog","icon":"check","text":"Saved"}""",
        ),
      expect =
        listOf(
          "ConfirmationDialog(visible = showOk, onDismissRequest = { showOk = false }, text = { Text(\"Saved\") }) { Icon(painterResource(R.drawable.check), contentDescription = null, modifier = Modifier.size(ConfirmationDialogDefaults.IconSize)) }",
        ),
    ),
  )
