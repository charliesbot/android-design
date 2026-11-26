package bot.charlies.androiddesign.preview.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject

class ModelTest {
  @Test
  fun `a failure reads as its first line, or as its class when that line is blank`() {
    assertEquals("first", IllegalStateException("first\nsecond").firstLine())
    assertEquals("IllegalStateException", IllegalStateException().firstLine())
    assertEquals("IllegalStateException", IllegalStateException("").firstLine())
    assertEquals("IllegalStateException", IllegalStateException("\nsecond").firstLine())
  }

  @Test
  fun `a located failure names the frames it was thrown from`() {
    val error = NullPointerException()
    error.stackTrace =
      arrayOf(
        StackTraceElement(
          "androidx.compose.ui.node.LookaheadDelegate",
          "owner",
          "LookaheadDelegate.kt",
          629,
        ),
        StackTraceElement(
          "androidx.compose.material3.ListItemPolicy",
          "measure",
          "ListItem.kt",
          1744,
        ),
        StackTraceElement("a.B", "c", null, -1),
        StackTraceElement("a.D", "e", "D.kt", 1),
      )
    assertEquals(
      "NullPointerException at LookaheadDelegate.owner(LookaheadDelegate.kt:629) < " +
        "ListItemPolicy.measure(ListItem.kt:1744) < B.c",
      error.located(frames = 3),
    )
    assertEquals(
      "boom",
      IllegalStateException("boom").apply { stackTrace = emptyArray() }.located(),
    )
  }

  @Test
  fun `wire names are the Compose property names`() {
    assertEquals("surfaceContainerHigh", ColorRole.SurfaceContainerHigh.wire)
    assertEquals("displayLargeEmphasized", TypeRole.DisplayLargeEmphasized.wire)
    assertEquals("extraExtraLarge", ShapeToken.ExtraExtraLarge.wire)
    assertEquals("cookie9Sided", MaterialShape.Cookie9Sided.wire)
  }

  @Test
  fun `kebab names are what DESIGN dot md uses`() {
    assertEquals("surface-container-high", ColorRole.SurfaceContainerHigh.kebab)
    assertEquals(TypeRole.LabelSmallEmphasized, TypeRole.fromKebab("label-small-emphasized"))
    assertEquals(ShapeToken.LargeIncreased, ShapeToken.fromKebab("large-increased"))
    assertNull(ColorRole.fromKebab("surfaceContainerHigh"))
  }

  @Test
  fun `the type scale has 15 roles and 15 emphasized`() {
    assertEquals(30, TypeRole.entries.size)
    assertEquals(15, TypeRole.entries.count { it.wire.endsWith("Emphasized") })
    assertEquals(TypeGroup.Display, TypeRole.DisplaySmallEmphasized.group)
  }

  @Test
  fun `palette variants use kebab names`() {
    assertEquals(PaletteVariant.FruitSalad, PaletteVariant.fromWire("fruit-salad"))
  }

  @Test
  fun `size classes break at 600 and 840`() {
    assertEquals(SizeClass.Compact, SizeClass.forWidth(599))
    assertEquals(SizeClass.Medium, SizeClass.forWidth(600))
    assertEquals(SizeClass.Medium, SizeClass.forWidth(839))
    assertEquals(SizeClass.Expanded, SizeClass.forWidth(840))
  }

  @Test
  fun `the five views match the brief`() {
    assertEquals(
      listOf("light", "dark", "foldable", "tablet", "desktop"),
      Views.all.map { it.name },
    )
    assertEquals(
      ViewSpec("foldable", Device.Foldable, 704, 933, false, SizeClass.Medium),
      Views.Foldable,
    )
    assertTrue(Views.Dark.dark && Views.Dark.device == Device.Phone)
    assertEquals(Views.Dark, Views.of(Device.Phone, dark = true))
    assertEquals("tablet-dark", Views.of(Device.Tablet, dark = true).name)
  }

  /** A layout that adapts at 600 and 840 dp is checked in every size class it can take. */
  @Test
  fun `every size class has a view`() {
    assertEquals(SizeClass.entries.toSet(), Views.all.map { it.sizeClass }.toSet())
    Views.all.forEach { assertEquals(SizeClass.forWidth(it.width), it.sizeClass, it.name) }
    assertTrue(WearSurface.views.all { it.round && it.dark && it.sizeClass == SizeClass.Compact })
  }

  @Test
  fun `the layout tree encodes cleanly`() {
    val tree =
      LayoutTree(
        screen = "plates",
        view = Views.Light,
        nodes =
          listOf(
            LayoutNode(
              id = "open_plate",
              type = "Button",
              parent = "body",
              primary = true,
              bounds = Bounds(16f, 108f, 84f, 40f),
              touch = Bounds(16f, 104f, 84f, 48f),
              text =
                TextInfo("Cook", 1, overflow = false, truncated = false, font = "Google Sans Flex"),
              roles =
                NodeRoles(
                  container = "primary",
                  content = "onPrimary",
                  type = "labelLarge",
                  shape = "full",
                ),
              colors = NodeColors(container = "#B5523B", content = "#FFFFFF"),
              a11y = A11yInfo(role = "Button", label = "Cook", actions = listOf("click")),
            ),
          ),
        findings =
          listOf(Finding("desc", FindingRule.TextTruncated, Severity.Info, "1 line, ellipsized")),
      )
    val text = PreviewJson.compact.encodeToString(LayoutTree.serializer(), tree)
    assertEquals(
      """{"screen":"plates","view":{"name":"light","device":"phone","width":411,"height":923,"dark":false,"sizeClass":"compact"},"nodes":[{"id":"open_plate","type":"Button","parent":"body","primary":true,"bounds":[16,108,84,40],"touch":[16,104,84,48],"text":{"value":"Cook","lines":1,"overflow":false,"truncated":false,"font":"Google Sans Flex"},"roles":{"container":"primary","content":"onPrimary","type":"labelLarge","shape":"full"},"colors":{"container":"#B5523B","content":"#FFFFFF"},"a11y":{"role":"Button","label":"Cook","actions":["click"]}}],"findings":[{"id":"desc","rule":"text-truncated","severity":"info","detail":"1 line, ellipsized"}]}""",
      text,
    )
    assertEquals(tree, PreviewJson.compact.decodeFromString(LayoutTree.serializer(), text))
    assertTrue(tree.pass)
  }

  @Test
  fun `surface specs pin each trait per surface`() {
    assertEquals(
      setOf(
        "BottomSheet",
        "Dialog",
        "Menu",
        "SideSheet",
        "SearchView",
        "Tooltip",
        "Snackbar",
        "DatePicker",
        "TimePicker",
      ),
      PhoneSurface.overlayNames,
    )
    assertEquals(
      setOf("BottomSheet", "Dialog", "SideSheet", "SearchView", "DatePicker", "TimePicker"),
      PhoneSurface.modalNames,
    )
    assertEquals(setOf("FloatingActionButton", "FabMenu"), PhoneSurface.fabNames)
    assertEquals(setOf("Carousel", "Tabs"), PhoneSurface.horizontalScrollers)
    assertTrue(PhoneSurface.overlayNames.containsAll(PhoneSurface.modalNames))

    assertEquals(setOf("AlertDialog", "ConfirmationDialog"), WearSurface.overlayNames)
    assertEquals(setOf("AlertDialog", "ConfirmationDialog"), WearSurface.modalNames)
    assertEquals(emptySet(), WearSurface.fabNames)
    assertEquals(emptySet(), WearSurface.horizontalScrollers)
    assertTrue(WearSurface.overlayNames.containsAll(WearSurface.modalNames))
    assertEquals(WearSurface, SurfaceSpecs.forView(Views.Watch))
    assertEquals(PhoneSurface, SurfaceSpecs.forView(Views.Light))
  }

  @Test
  fun `a tree with an error finding does not pass`() {
    val tree =
      LayoutTree(
        "s",
        Views.Light,
        emptyList(),
        listOf(Finding("b", FindingRule.TouchTarget, Severity.Error)),
      )
    assertTrue(!tree.pass)
  }

  @Test
  fun `bounds read four numbers and nothing else`() {
    fun decode(json: String) = PreviewJson.compact.decodeFromString(Bounds.serializer(), json)
    assertEquals(Bounds(1f, 2.5f, 3f, 4f), decode("[1, 2.5, 3, 4]"))
    for (bad in listOf("[1,2,3]", "[1,2,3,4,5]", "[1,2,\"x\",4]", "[1,2,null,4]", "{}", "7")) {
      val failure = assertFailsWith<SerializationException>(bad) { decode(bad) }
      assertEquals("Bounds must be [x, y, width, height]", failure.message, bad)
    }
  }

  @Test
  fun `direction metadata always encodes overlays array even when empty`() {
    val dir =
      DirectionMeta(
        name = "Editorial",
        surface = "phone",
        startScreen = "home",
        screens = listOf("home"),
      )
    val meta = BoardMeta(directions = listOf(dir), views = listOf(Views.Light))

    val compact = PreviewJson.compact.encodeToString(BoardMeta.serializer(), meta)
    assertTrue("\"overlays\":[]" in compact, "compact JSON omitted empty overlays: $compact")
    assertEquals(meta, PreviewJson.compact.decodeFromString(BoardMeta.serializer(), compact))

    val pretty = PreviewJson.pretty.encodeToString(BoardMeta.serializer(), meta)
    val prettyDir =
      Json.parseToJsonElement(pretty).jsonObject["directions"]!!.jsonArray.single().jsonObject
    assertEquals(JsonArray(emptyList()), prettyDir["overlays"])

    val legacyJson =
      """{"directions":[{"name":"Editorial","surface":"phone","startScreen":"home","screens":["home"]}],"views":[{"name":"light","device":"phone","width":411,"height":923,"dark":false,"sizeClass":"compact"}]}"""
    assertEquals(meta, PreviewJson.compact.decodeFromString(BoardMeta.serializer(), legacyJson))
  }
}
