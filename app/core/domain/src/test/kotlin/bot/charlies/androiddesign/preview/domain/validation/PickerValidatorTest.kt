package bot.charlies.androiddesign.preview.domain.validation

import bot.charlies.androiddesign.preview.domain.TestProposal
import bot.charlies.androiddesign.preview.model.ValidationError
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** Dialogs and pickers: dates and times written out, compact-only full-screen, per size class. */
class PickerValidatorTest {
  private val validator = ProposalValidator()
  private val base = TestProposal.valid()

  private fun errors(p: TestProposal): List<ValidationError> = validator.validate(p.build(), null)

  private fun List<ValidationError>.assertHas(path: String, messagePart: String) {
    val atPath = filter { it.path == path }
    assertTrue(
      atPath.any { messagePart in it.message },
      "no error at $path containing '$messagePart'; got $this",
    )
  }

  private fun path(p: TestProposal, id: String) = "/screens/plates/components/${p.indexOf(id)}"

  private fun date(extra: String) =
    base.with("pick", """{"id":"pick","component":"DatePicker"$extra}""")

  private fun time(extra: String) =
    base.with("pick", """{"id":"pick","component":"TimePicker"$extra}""")

  private fun dialog(extra: String) =
    base
      .with("save", """{"id":"save","component":"Button","variant":"text","label":"Save"}""")
      .with("note", """{"id":"note","component":"Text","text":"Body"}""")
      .with("pick", """{"id":"pick","component":"Dialog","title":"New"$extra}""")

  @Test
  fun `valid pickers and dialogs have no errors`() {
    assertEquals(emptyList(), errors(date(""","selected":"2026-11-06"""")))
    assertEquals(
      emptyList(),
      errors(
        date(""","range":true,"variant":"modal-input","start":"2026-11-06","end":"2026-11-06""""),
      ),
    )
    assertEquals(emptyList(), errors(time(""","time":"23:59","is24Hour":true""")))
    assertEquals(
      emptyList(),
      errors(
        dialog(
          ""","variant":"full-screen","confirm":"save","content":["note"],"showIn":["compact"]""",
        ),
      ),
    )
  }

  @Test
  fun `a date picker needs its date as a real ISO day, never bound to data`() {
    fun bad(value: String) = date(""","selected":$value""")
    for (value in
      listOf("\"2026-02-30\"", "\"11/06/2026\"", "\"1850-01-01\"", "\"2026-1-6\"", "5")) {
      val p = bad(value)
      errors(p).assertHas(path(p, "pick") + "/selected", "YYYY-MM-DD")
    }
    val bound = bad("""{"@path":"/when"}""")
    errors(bound).assertHas(path(bound, "pick") + "/selected", "not bound")
    val none = date("")
    errors(none).assertHas(path(none, "pick") + "/selected", "needs selected")
  }

  @Test
  fun `a range needs start, ends on or after it, and takes no selected`() {
    val none = date(""","range":true""")
    errors(none).assertHas(path(none, "pick") + "/start", "needs start")
    val backwards = date(""","range":true,"start":"2026-11-12","end":"2026-11-06"""")
    errors(backwards).assertHas(path(backwards, "pick") + "/end", "before start")
    val mixed = date(""","range":true,"start":"2026-11-06","selected":"2026-11-06"""")
    errors(mixed).assertHas(path(mixed, "pick") + "/selected", "single date")
    val single = date(""","selected":"2026-11-06","start":"2026-11-06"""")
    errors(single).assertHas(path(single, "pick") + "/start", "date range")
  }

  @Test
  fun `a docked date picker needs an anchor`() {
    val p = date(""","variant":"docked","selected":"2026-11-06"""")
    errors(p).assertHas(path(p, "pick") + "/variant", "anchor")
    val anchored =
      date(""","variant":"docked","selected":"2026-11-06","anchor":"dates"""")
        .with("dates", """{"id":"dates","component":"TextField","label":"Date"}""")
    assertEquals(emptyList(), errors(anchored))
  }

  @Test
  fun `a time is HH mm`() {
    for (value in listOf("\"9:30\"", "\"24:00\"", "\"12:60\"", "\"noon\"", "930")) {
      val p = time(""","time":$value""")
      errors(p).assertHas(path(p, "pick") + "/time", "HH:mm")
    }
  }

  @Test
  fun `a full-screen dialog is for compact windows only, whatever the size class overrides say`() {
    val date = date(""","variant":"full-screen","selected":"2026-11-06"""")
    // Only recommended on compact windows: a note from the finding rules, not an error.
    assertEquals(emptyList(), errors(date))
    val adaptive =
      date(
        ""","variant":"full-screen","selected":"2026-11-06","sizeClass":{"medium":{"variant":"modal"}}""",
      )
    assertEquals(emptyList(), errors(adaptive))
    val dialog =
      dialog(
        ""","variant":"full-screen","confirm":"save","content":["note"],"sizeClass":{"expanded":{"variant":"full-screen"}}""",
      )
    errors(dialog).assertHas(path(dialog, "pick") + "/variant", "at medium, expanded")
    val shownCompactOnly =
      dialog(
        ""","variant":"full-screen","confirm":"save","content":["note"],"showIn":["compact"]""",
      )
    assertEquals(emptyList(), errors(shownCompactOnly))
  }

  @Test
  fun `a full-screen dialog has content, a basic one has text and a dismiss button`() {
    val full =
      dialog(""","variant":"full-screen","text":"Hi","dismiss":"save","showIn":["compact"]""")
    errors(full).assertHas(path(full, "pick") + "/text", "basic dialog")
    errors(full).assertHas(path(full, "pick") + "/dismiss", "basic dialog")
    val basic = dialog(""","content":["save"]""")
    errors(basic).assertHas(path(basic, "pick") + "/content", "full-screen dialog")
  }

  @Test
  fun `a malformed picker is an error, not a crash`() {
    val weird = date(""","selected":[1],"range":"yes","variant":{"a":1},"anchor":5""")
    assertTrue(errors(weird).isNotEmpty())
    val time = time(""","time":null""")
    assertTrue(errors(time).isNotEmpty())
  }

  @Test
  fun `an anchor lives in the base props, not in a size class override`() {
    val field = """{"id":"dates","component":"TextField","label":"Date"}"""
    val picker =
      date(
          ""","selected":"2026-11-06","sizeClass":{"medium":{"variant":"docked","anchor":"dates"}}""",
        )
        .with("dates", field)
    errors(picker).assertHas(path(picker, "pick") + "/sizeClass/medium/anchor", "base props")
    val menu =
      base
        .with("edit", """{"id":"edit","component":"MenuItem","label":"Edit"}""")
        .with(
          "menu",
          """{"id":"menu","component":"Menu","items":["edit"],"sizeClass":{"medium":{"anchor":"more"}}}""",
        )
    errors(menu).assertHas(path(menu, "menu") + "/sizeClass/medium/anchor", "base props")
    val tip =
      base.with(
        "tip",
        """{"id":"tip","component":"Tooltip","anchor":"more","text":"Hi","sizeClass":{"expanded":{"anchor":"more"}}}""",
      )
    errors(tip).assertHas(path(tip, "tip") + "/sizeClass/expanded/anchor", "base props")
    val modalWithAnchor =
      date(
          ""","selected":"2026-11-06","anchor":"dates","sizeClass":{"medium":{"variant":"docked"}}""",
        )
        .with("dates", field)
    assertEquals(emptyList(), errors(modalWithAnchor))
  }
}
