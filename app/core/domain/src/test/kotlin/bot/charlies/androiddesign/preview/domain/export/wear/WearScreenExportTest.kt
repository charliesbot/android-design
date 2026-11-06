package bot.charlies.androiddesign.preview.domain.export.wear

import bot.charlies.androiddesign.preview.domain.PreviewException
import bot.charlies.androiddesign.preview.domain.export.wear.WearExportFixtures.has
import bot.charlies.androiddesign.preview.domain.export.wear.WearExportFixtures.lacks
import bot.charlies.androiddesign.preview.domain.export.wear.WearExportFixtures.root
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

/** The Screen frame: scaffolds, the scrolling list, the fixed body and the edge button. */
class WearScreenExportTest {
  private val text = """{"id":"t","component":"Text","text":"Hi"}"""

  @Test
  fun `a scrolling screen is a ScreenScaffold around a TransformingLazyColumn`() {
    val source = WearExportFixtures.export(root("t"), text)
    has(source, "@Composable fun MainScreen(modifier: Modifier = Modifier) {")
    has(source, "val columnState = rememberTransformingLazyColumnState()")
    has(
      source,
      "ScreenScaffold(scrollState = columnState, modifier = modifier) { contentPadding ->",
    )
    has(source, "val transformationSpec = rememberTransformationSpec()")
    has(source, "TransformingLazyColumn(state = columnState, contentPadding = contentPadding) {")
    has(
      source,
      "item { Text(\"Hi\", modifier = Modifier.transformedHeight(this, transformationSpec)) }",
    )
    has(source, "import androidx.wear.compose.foundation.lazy.TransformingLazyColumn")
    has(source, "import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState")
    has(source, "import androidx.wear.compose.material3.lazy.rememberTransformationSpec")
    has(source, "import androidx.wear.compose.material3.lazy.transformedHeight")
    has(source, "import androidx.wear.compose.material3.ScreenScaffold")
    lacks(source, "AppScaffold(timeText = { TimeText() }) { ScreenScaffold")
  }

  @Test
  fun `a single screen has a preview at each watch view inside an AppScaffold`() {
    val source = WearExportFixtures.export(root("t"), text)
    has(
      source,
      "@Preview(name = \"watch\", device = \"spec:width=204dp,height=204dp,dpi=320,isRound=true\", showSystemUi = true)",
    )
    has(
      source,
      "@Preview(name = \"watch-large\", device = \"spec:width=227dp,height=227dp,dpi=320,isRound=true\", showSystemUi = true)",
    )
    lacks(source, "Devices")
    has(
      source,
      "private fun MainScreenPreview() { WearAppTheme { AppScaffold(timeText = { TimeText() }) { MainScreen() } } }",
    )
  }

  @Test
  fun `the screen with a hidden time text turns it off for that screen`() {
    val source = WearExportFixtures.export(root("t", props = """"timeText":false"""), text)
    has(source, "ScreenScaffold(scrollState = columnState, modifier = modifier, timeText = {})")
  }

  @Test
  fun `an edge button goes into the scaffold slot of a scrolling screen`() {
    val source =
      WearExportFixtures.export(
        root("t", props = """"edgeButton":"go""""),
        text,
        """{"id":"go","component":"EdgeButton","label":"Done"}""",
      )
    has(
      source,
      "ScreenScaffold(scrollState = columnState, modifier = modifier, edgeButton = { EdgeButton(onClick = {}) { Text(\"Done\") } }) { contentPadding ->",
    )
    lacks(source, "item { EdgeButton")
  }

  @Test
  fun `a fixed screen is a Box and ends above its edge button`() {
    val source =
      WearExportFixtures.export(
        root("t", props = """"scroll":false,"edgeButton":"go""""),
        text,
        """{"id":"go","component":"EdgeButton","label":"Done"}""",
      )
    has(
      source,
      "ScreenScaffold(scrollInfoProvider = FixedScrollInfoProvider, modifier = modifier, edgeButton = { EdgeButton(onClick = {}) { Text(\"Done\") } }) { contentPadding ->",
    )
    has(
      source,
      "Box(Modifier.fillMaxSize().padding(bottom = contentPadding.calculateBottomPadding())) { Text(\"Hi\") }",
    )
    has(source, "private object FixedScrollInfoProvider : ScrollInfoProvider {")
    has(source, "override val lastItemOffset = 4096f")
    lacks(source, "TransformingLazyColumn")
  }

  @Test
  fun `a fixed screen without an edge button is a full Box and needs no scroll provider`() {
    val source = WearExportFixtures.export(root("t", props = """"scroll":false"""), text)
    has(
      source,
      "ScreenScaffold(modifier = modifier) { Box(Modifier.fillMaxSize()) { Text(\"Hi\") } }",
    )
    lacks(source, "FixedScrollInfoProvider")
  }

  @Test
  fun `a full export has one AppScaffold with the time text at the root`() {
    val source = WearExportFixtures.export(WearExportFixtures.proposal(root("t"), text))
    assertTrue(Regex("AppScaffold\\(").findAll(source).count() == 2, "root and the preview")
    has(source, "fun WearTestApp() { WearAppTheme { AppScaffold(timeText = { TimeText() }) {")
  }

  @Test
  fun `size overrides pick by the screen width`() {
    val source =
      WearExportFixtures.export(
        root("t"),
        """{"id":"t","component":"Text","text":"05:00","type":"numeralLarge","sizeClass":{"large":{"type":"numeralExtraLarge"}}}""",
      )
    has(source, "val isLarge = isLargeDisplay()")
    has(
      source,
      "if (isLarge) { Text(\"05:00\", modifier = Modifier.transformedHeight(this, transformationSpec), style = MaterialTheme.typography.numeralExtraLarge) } else { Text(\"05:00\", modifier = Modifier.transformedHeight(this, transformationSpec), style = MaterialTheme.typography.numeralLarge) }",
    )
    has(
      source,
      "private fun isLargeDisplay(): Boolean = LocalConfiguration.current.screenWidthDp >= 225",
    )
    has(source, "import androidx.compose.ui.platform.LocalConfiguration")
  }

  @Test
  fun `an override that changes nothing writes the component once`() {
    val source =
      WearExportFixtures.export(
        root("t"),
        """{"id":"t","component":"Text","text":"Hi","sizeClass":{"large":{"text":"Hi"}}}""",
      )
    lacks(source, "isLarge")
  }

  @Test
  fun `a style fills the type color and padding the component leaves out`() {
    val proposal =
      WearExportFixtures.proposal(
        root("t"),
        """{"id":"t","component":"Text","text":"Hi","style":"note"}""",
      )
    val design =
      WearExportFixtures.design()
        .copy(
          styles =
            mapOf(
              "note" to
                bot.charlies.androiddesign.preview.model.StyleDef(
                  type = "titleSmall",
                  color = "primaryDim",
                  container = "surfaceContainer",
                  padding = "sm",
                ),
            ),
        )
    val source =
      bot.charlies.androiddesign.preview.domain.export.KotlinExporter.export(
        proposal,
        design,
        "main",
        WearExportFixtures.scheme,
      )
    has(
      source,
      "Text(\"Hi\", modifier = Modifier.padding(8.dp).background(MaterialTheme.colorScheme.surfaceContainer).transformedHeight(this, transformationSpec), color = MaterialTheme.colorScheme.primaryDim, style = MaterialTheme.typography.titleSmall)",
    )
  }

  @Test
  fun `an unknown screen is reported with the screens there are`() {
    val error =
      assertFailsWith<PreviewException> {
        WearExportFixtures.export(WearExportFixtures.proposal(root("t"), text), "nope")
      }
    assertTrue("main" in error.message.orEmpty())
  }

  @Test
  fun `the header lists symbols and images on separate lines`() {
    val source =
      WearExportFixtures.export(
        root("i", "a"),
        """{"id":"i","component":"Image","src":"art/pic.png"}""",
        """{"id":"a","component":"Icon","name":"timer"}""",
      )
    val header = source.substringBefore("\npackage ")
    has(header, "// Add these Material Symbols (Rounded, weight 400, grade 0) as vector drawables.")
    has(header, "// R.drawable.timer")
    has(header, "// Add these images as drawables:")
    has(header, "// R.drawable.pic")
    val symbols = header.substringBefore("Add these images")
    assertTrue("R.drawable.pic" !in symbols, header)
  }

  @Test
  fun `a screen with the time text off previews without the clock`() {
    val off = WearExportFixtures.export(root("t", props = """"timeText":false"""), text)
    has(
      off,
      "private fun MainScreenPreview() { WearAppTheme { AppScaffold(timeText = {}) { MainScreen() } } }",
    )
    val on = WearExportFixtures.export(root("t"), text)
    has(on, "AppScaffold(timeText = { TimeText() }) { MainScreen() }")
  }

  @Test
  fun `progress rings pass the stroke width the previewer draws with`() {
    val source =
      WearExportFixtures.export(
        root("a", "b", "c", props = """"scroll":false"""),
        """{"id":"a","component":"CircularProgressIndicator","progress":0.6}""",
        """{"id":"b","component":"SegmentedCircularProgressIndicator","segmentCount":5,"progress":0.4}""",
        """{"id":"c","component":"CircularProgressIndicator"}""",
      )
    has(
      source,
      "progress = { 0.6f }, modifier = Modifier.fillMaxSize().padding(CircularProgressIndicatorDefaults.FullScreenPadding), strokeWidth = CircularProgressIndicatorDefaults.largeStrokeWidth",
    )
    has(
      source,
      "segmentCount = 5, progress = { 0.4f }, modifier = Modifier.fillMaxSize().padding(CircularProgressIndicatorDefaults.FullScreenPadding), strokeWidth = CircularProgressIndicatorDefaults.largeStrokeWidth",
    )
    has(source, "strokeWidth = CircularProgressIndicatorDefaults.IndeterminateStrokeWidth")
  }
}
