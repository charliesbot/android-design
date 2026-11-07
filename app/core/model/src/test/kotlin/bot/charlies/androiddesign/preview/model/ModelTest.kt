package bot.charlies.androiddesign.preview.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.put

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
  fun `every catalog default is a value its prop accepts`() {
    for (def in M3ExpressiveCatalog.components) {
      for (prop in def.props) {
        val default = prop.default ?: continue
        val label = "${def.name}.${prop.name}"
        when (val kind = prop.kind) {
          is PropKind.Enum -> assertTrue(default.isString && default.content in kind.values, label)
          PropKind.Bool -> assertNotNull(default.booleanOrNull, label)
          PropKind.Icon -> assertTrue(default.isString && default.content.isNotEmpty(), label)
          is PropKind.Number -> {
            val number = assertNotNull(default.doubleOrNull, label)
            assertTrue(kind.bounds == null || number in kind.bounds, label)
          }
          else -> error("$label: a ${kind::class.simpleName} prop has no default")
        }
      }
    }
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
  }

  @Test
  fun `the registry has unique component names and resolves every allowed child`() {
    val names = M3ExpressiveCatalog.components.map { it.name }
    assertEquals(names.size, names.toSet().size)
    for (def in M3ExpressiveCatalog.components) {
      assertEquals(def.props.size, def.props.map { it.name }.toSet().size, def.name)
      for (prop in def.props) {
        prop.allowedChildren?.forEach {
          assertNotNull(M3ExpressiveCatalog.find(it), "${def.name}.${prop.name} -> $it")
        }
      }
    }
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
      M3ExpressiveCatalog.overlayNames,
    )
  }

  @Test
  fun `the layout tree encodes the way the brief shows`() {
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
  fun `wear large overrides apply from 225 dp, so at 227 and not at 204`() {
    val node =
      ComponentNode(
        "t",
        "Text",
        buildJsonObject {
          put("sizeClass", buildJsonObject { put("large", buildJsonObject { put("type", "big") }) })
        },
      )
    val breakpoints = WearCatalog.sizeBreakpoints
    assertEquals(emptyMap(), node.sizeOverridesAt(breakpoints, Views.Watch.width))
    assertEquals(setOf("type"), node.sizeOverridesAt(breakpoints, Views.WatchLarge.width).keys)
    assertTrue(WearCatalog.views.all { it.round && it.dark && it.sizeClass == SizeClass.Compact })
  }

  @Test
  fun `phone size overrides are unchanged`() {
    val node =
      ComponentNode(
        "t",
        "Text",
        buildJsonObject {
          put(
            "sizeClass",
            buildJsonObject {
              put("medium", buildJsonObject { put("a", 1) })
              put("expanded", buildJsonObject { put("b", 2) })
            },
          )
        },
      )
    assertEquals(emptyMap(), node.sizeOverridesAt(SizeClass.Compact))
    assertEquals(setOf("a"), node.sizeOverridesAt(SizeClass.Medium).keys)
    assertEquals(setOf("a", "b"), node.sizeOverridesAt(SizeClass.Expanded).keys)
  }
}
