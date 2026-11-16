package bot.charlies.androiddesign.preview.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ComponentIdsTest {
  @Test
  fun `every surface component trait name belongs to its component enum or navigation nodes`() {
    val phoneNames = M3Component.entries.map { it.wire }.toSet()
    val wearNames = WearComponent.entries.map { it.wire }.toSet()

    assertTrue(
      (PhoneSurface.overlayNames +
          PhoneSurface.modalNames +
          PhoneSurface.fabNames +
          PhoneSurface.horizontalScrollers +
          PhoneSurface.actionTypes)
        .all { it in phoneNames },
    )
    assertTrue(
      (PhoneSurface.interactiveTypes + PhoneSurface.labelTypes).all {
        it in phoneNames || it in NavigationNodes.itemTypes
      },
    )
    assertTrue(
      (WearSurface.overlayNames +
          WearSurface.modalNames +
          WearSurface.interactiveTypes +
          WearSurface.actionTypes +
          WearSurface.labelTypes)
        .all { it in wearNames },
    )
    assertTrue(
      WearSurface.roundEdgeNames.filter { it != "TimeText" }.all { it in wearNames },
    )
  }

  @Test
  fun `the two enums share exactly these names`() {
    assertEquals(
      setOf(
        "Slider",
        "ButtonGroup",
        "Column",
        "Button",
        "Row",
        "Image",
        "Icon",
        "Box",
        "RadioButton",
        "IconButton",
        "Text",
        "Spacer",
        "Card",
      ),
      M3Component.entries.map { it.wire }.toSet() intersect
        WearComponent.entries.map { it.wire }.toSet(),
    )
  }

  @Test
  fun `an unknown wire name is null from fromWire and an error from of`() {
    assertNull(M3Component.fromWire("TabRow"))
    assertFailsWith<IllegalStateException> { WearComponent.of("Carousel") }
  }
}
