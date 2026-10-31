package bot.charlies.androiddesign.preview.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ModelTest {
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
      ViewSpec("foldable", Device.Foldable, 933, 704, false, SizeClass.Expanded),
      Views.Foldable,
    )
    assertTrue(Views.Dark.dark && Views.Dark.device == Device.Phone)
    assertEquals(Views.Dark, Views.of(Device.Phone, dark = true))
    assertEquals("tablet-dark", Views.of(Device.Tablet, dark = true).name)
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
    assertEquals(setOf("BottomSheet", "Dialog", "Menu"), M3ExpressiveCatalog.overlayNames)
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
}
