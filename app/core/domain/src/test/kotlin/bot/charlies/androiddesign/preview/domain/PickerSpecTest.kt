package bot.charlies.androiddesign.preview.domain

import java.time.LocalDate
import java.time.LocalTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class PickerSpecTest {
  @Test
  fun `dates are real ISO days within the year range`() {
    assertEquals(LocalDate.of(2026, 11, 6), DatePickerSpec.date("2026-11-06"))
    assertEquals(LocalDate.of(2028, 2, 29), DatePickerSpec.date("2028-02-29"))
    for (bad in
      listOf(
        null,
        "",
        "2026-02-29",
        "2026-13-01",
        "2026-1-6",
        "06/11/2026",
        "1899-12-31",
        "2101-01-01",
        "2026-11-06T00:00",
      )) {
      assertNull(DatePickerSpec.date(bad), "$bad")
    }
  }

  @Test
  fun `a date round-trips through the milliseconds a picker state holds`() {
    val date = DatePickerSpec.date("2026-11-06")!!
    assertEquals(1793923200000L, DatePickerSpec.millis(date))
    assertEquals("2026-11-06", DatePickerSpec.iso(DatePickerSpec.millis(date)))
  }

  @Test
  fun `times are HH mm`() {
    assertEquals(LocalTime.of(0, 0), TimePickerSpec.time("00:00"))
    assertEquals(LocalTime.of(23, 59), TimePickerSpec.time("23:59"))
    for (bad in listOf(null, "", "9:30", "24:00", "12:60", "09:30:00", "noon")) {
      assertNull(TimePickerSpec.time(bad), "$bad")
    }
    assertEquals("09:05", TimePickerSpec.text(9, 5))
  }

  @Test
  fun `variants fall back to the catalog default`() {
    assertEquals(DatePickerSpec.Variant.Modal, DatePickerSpec.variant(null))
    assertEquals(DatePickerSpec.Variant.FullScreen, DatePickerSpec.variant("full-screen"))
    assertEquals(TimePickerSpec.Variant.Dial, TimePickerSpec.variant("?"))
  }
}
