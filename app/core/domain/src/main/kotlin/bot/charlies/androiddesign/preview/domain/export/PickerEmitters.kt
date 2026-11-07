package bot.charlies.androiddesign.preview.domain.export

import bot.charlies.androiddesign.preview.domain.DatePickerSpec
import bot.charlies.androiddesign.preview.domain.FullScreenSpec
import bot.charlies.androiddesign.preview.domain.TimePickerSpec
import bot.charlies.androiddesign.preview.model.ComponentNode
import kotlinx.serialization.json.JsonObject

// Dialogs and pickers: the overlays whose window the exporter composes from `Dialog`,
// `DatePickerDialog` and `TimePickerDialog`. The previewer draws the same compositions
// (`Pickers.kt`, `Overlays.kt`), and the numbers and labels come from `PickerSpec.kt`. Each is
// written for the props in effect at one size class ([ScreenEmitter.perSize] picks them).

private fun ScreenEmitter.modifierOf(vararg calls: String): Arg =
  arg("modifier", Chain(file.use(Sym.Modifier), calls.toList()))

private fun ScreenEmitter.textButton(label: String, onClick: Lambda): Stmt =
  stmt(
    Call(
      m3("TextButton"),
      listOf(arg("onClick", onClick)),
      lambdaOf(textOf(Raw(kotlinString(label)))),
    ),
  )

private fun ScreenEmitter.closeLambda(node: ComponentNode): Lambda =
  lambda(Assign(overlayVar(node.id), "=", Raw("false")))

private fun ScreenEmitter.guarded(node: ComponentNode, vararg body: Stmt): Stmt =
  IfStmt(listOf(overlayVar(node.id) to body.toList()), null)

internal fun ScreenEmitter.dialogStatements(node: ComponentNode, p: JsonObject): List<Stmt> =
  if (p.choice("Dialog", "variant") == FullScreenSpec.VARIANT) fullScreenDialog(node, p)
  else basicDialog(node, p)

private fun ScreenEmitter.basicDialog(node: ComponentNode, p: JsonObject): List<Stmt> {
  val place = Place()
  val title = p.text("title", place) ?: Raw("\"\"")
  val confirm = slot(p, "confirm", place, "Dialog") ?: lambda()
  val dismiss = slot(p, "dismiss", place, "Dialog")
  val dialog =
    Call(
      m3("AlertDialog"),
      listOfNotNull(
        arg("onDismissRequest", closeLambda(node)),
        arg("confirmButton", confirm),
        dismiss?.let { arg("dismissButton", it) },
        arg("title", lambdaOf(textOf(title))),
        textSlot(p, "text", place),
      ),
    )
  return listOf(guarded(node, stmt(dialog)))
}

/**
 * Compose has no full-screen dialog, so it is composed like the previewer draws it: a full-window
 * `Dialog` holding a `Scaffold` whose `TopAppBar` has the close icon, the headline and the confirm
 * button, over the content, which scrolls with [FullScreenSpec.CONTENT_PADDING_DP] at the sides.
 */
private fun ScreenEmitter.fullScreenDialog(node: ComponentNode, p: JsonObject): List<Stmt> {
  val place = Place()
  val title = p.text("title", place) ?: Raw("\"\"")
  val content =
    Call(
      file.use(Sym.Column),
      listOf(
        modifierOf(
          useModifier("fillMaxSize()"),
          "${useModifier("padding")}(innerPadding)",
          "${useModifier("verticalScroll")}(${file.use(Sym.rememberScrollState)}())",
          "${useModifier("padding")}(horizontal = ${dp(FullScreenSpec.CONTENT_PADDING_DP)})",
        ),
      ),
      lambda(children(p, "content", place, "Dialog")),
    )
  return listOf(
    guarded(
      node,
      fullScreenWindow(
        node,
        title = lambdaOf(textOf(title)),
        confirm = lambda(child(p, "confirm", place, "Dialog")),
        content = lambda(listOf(stmt(content)), params = "innerPadding"),
      ),
    ),
  )
}

/** The window both full-screen overlays share; see `FullScreenWindow` in the previewer. */
private fun ScreenEmitter.fullScreenWindow(
  node: ComponentNode,
  title: Lambda,
  confirm: Lambda,
  content: Lambda,
): Stmt {
  experimental()
  val container = colorRole("surfaceContainerHigh")
  val topBar =
    Call(
      m3("TopAppBar"),
      listOf(
        arg("title", title),
        arg(
          "navigationIcon",
          lambdaOf(
            Call(
              m3("IconButton"),
              listOf(arg("onClick", closeLambda(node))),
              lambdaOf(icon(IconName.Literal("close"), Raw(kotlinString("Close")))),
            ),
          ),
        ),
        arg("actions", confirm),
        arg("expandedHeight", dp(FullScreenSpec.HEADER_HEIGHT_DP)),
        arg(
          "colors",
          Call(
            "${m3("TopAppBarDefaults")}.topAppBarColors",
            listOf(arg("containerColor", container)),
          ),
        ),
      ),
    )
  val scaffold =
    Call(
      file.use(Sym.Scaffold),
      listOf(
        modifierOf(useModifier("fillMaxSize()")),
        arg("topBar", lambdaOf(topBar)),
        arg("containerColor", container),
      ),
      content,
    )
  return stmt(
    Call(
      file.use(Sym.Dialog),
      listOf(
        arg("onDismissRequest", closeLambda(node)),
        arg("properties", "${file.use(Sym.DialogProperties)}(usePlatformDefaultWidth = false)"),
      ),
      lambdaOf(scaffold),
    ),
  )
}

/**
 * The date picker for the props in effect at one size class: `DatePickerDialog` for modal and modal
 * input, a `Popup` in an empty box at the bottom start of the anchored field for docked, and the
 * full-screen window for full-screen. The state is declared outside the `if`, so a reopened picker
 * keeps what was picked. The dates are the proposal's, as sample data. Unlike the previewer, which
 * never marks today, the picker marks today as Material does.
 */
internal fun ScreenEmitter.datePickerStatements(node: ComponentNode, p: JsonObject): List<Stmt> {
  experimental()
  val variant = DatePickerSpec.variant(p.choice("DatePicker", "variant"))
  val range = p.option("DatePicker", "range")
  val state = stateName(node.id, "DatePickerState")
  fun millis(key: String) =
    DatePickerSpec.date(p.literal(key))?.let { "${DatePickerSpec.millis(it)}L" }
  val mode =
    if (variant == DatePickerSpec.Variant.ModalInput) "${m3("DisplayMode")}.Input" else null
  val remembered =
    if (range) {
      Call(
        m3("rememberDateRangePickerState"),
        listOfNotNull(
          millis("start")?.let { arg("initialSelectedStartDateMillis", it) },
          millis("end")?.let { arg("initialSelectedEndDateMillis", it) },
          mode?.let { arg("initialDisplayMode", it) },
        ),
      )
    } else {
      Call(
        m3("rememberDatePickerState"),
        listOfNotNull(
          millis("selected")?.let { arg("initialSelectedDateMillis", it) },
          mode?.let { arg("initialDisplayMode", it) },
        ),
      )
    }
  val declaration = Assign("val $state", "=", remembered)
  fun picker(modifier: Arg? = null): Stmt {
    val extras =
      when (variant) {
        DatePickerSpec.Variant.Docked ->
          listOf(arg("title", "null"), arg("headline", "null"), arg("showModeToggle", "false"))
        DatePickerSpec.Variant.FullScreen ->
          listOf(arg("title", "null"), arg("showModeToggle", "false"))
        else -> emptyList()
      }
    return stmt(
      Call(
        m3(if (range) "DateRangePicker" else "DatePicker"),
        listOfNotNull(arg("state", state), modifier) + extras,
      ),
    )
  }
  val frame: Stmt =
    when (variant) {
      DatePickerSpec.Variant.Modal,
      DatePickerSpec.Variant.ModalInput ->
        stmt(
          Call(
            m3("DatePickerDialog"),
            listOf(
              arg("onDismissRequest", closeLambda(node)),
              arg(
                "confirmButton",
                lambda(textButton(DatePickerSpec.CONFIRM, closeLambda(node))),
              ),
              arg(
                "dismissButton",
                lambda(textButton(DatePickerSpec.DISMISS, closeLambda(node))),
              ),
            ),
            lambda(picker()),
          ),
        )
      DatePickerSpec.Variant.Docked -> {
        val surface =
          Call(
            m3("Surface"),
            listOf(
              modifierOf("${useModifier("width")}(${dp(DatePickerSpec.DOCKED_WIDTH_DP)})"),
              arg("shape", "${m3("DatePickerDefaults")}.shape"),
              arg("color", "${m3("DatePickerDefaults")}.colors().containerColor"),
            ),
            lambda(picker()),
          )
        val popup =
          Call(
            file.use(Sym.Popup),
            listOf(
              arg("alignment", "${file.use(Sym.Alignment)}.TopStart"),
              arg("onDismissRequest", closeLambda(node)),
            ),
            lambdaOf(surface),
          )
        // The popup opens at the top start of its parent: an empty box at the field's bottom start.
        stmt(
          Call(
            file.use(Sym.Box),
            listOf(modifierOf("align(${file.use(Sym.Alignment)}.BottomStart)")),
            lambdaOf(popup),
          ),
        )
      }
      DatePickerSpec.Variant.FullScreen ->
        fullScreenWindow(
          node,
          title =
            lambdaOf(
              Call(
                "${m3(if (range) "DateRangePickerDefaults" else "DatePickerDefaults")}." +
                  (if (range) "DateRangePickerTitle" else "DatePickerTitle"),
                listOf(arg("displayMode", "${m3("DisplayMode")}.Picker")),
              ),
            ),
          confirm = lambda(textButton(DatePickerSpec.SAVE, closeLambda(node))),
          content =
            lambda(
              listOf(picker(modifierOf("${useModifier("padding")}(innerPadding)"))),
              params = "innerPadding",
            ),
        )
    }
  return listOf(declaration, guarded(node, frame))
}

/**
 * The time picker for the props in effect at one size class: `TimePickerDialog` holding
 * `TimePicker` or `TimeInput`. The keyboard icon switches between them, as in the previewer.
 */
internal fun ScreenEmitter.timePickerStatements(node: ComponentNode, p: JsonObject): List<Stmt> {
  experimental()
  expressive()
  val time = TimePickerSpec.time(p.literal("time"))
  val input =
    TimePickerSpec.variant(p.choice("TimePicker", "variant")) == TimePickerSpec.Variant.Input
  val state = stateName(node.id, "TimePickerState")
  val mode = stateName(node.id, "DisplayMode")
  val displayMode = m3("TimePickerDisplayMode")
  val declaration =
    Assign(
      "val $state",
      "=",
      Call(
        m3("rememberTimePickerState"),
        listOfNotNull(
          time?.let { arg("initialHour", it.hour.toString()) },
          time?.let { arg("initialMinute", it.minute.toString()) },
          arg("is24Hour", p.option("TimePicker", "is24Hour").toString()),
        ),
      ),
    )
  val modeState = stateVar(mode, "$displayMode.${if (input) "Input" else "Picker"}")
  val dialog =
    Call(
      m3("TimePickerDialog"),
      listOf(
        arg("onDismissRequest", closeLambda(node)),
        arg("confirmButton", lambda(textButton(TimePickerSpec.CONFIRM, closeLambda(node)))),
        arg("dismissButton", lambda(textButton(TimePickerSpec.DISMISS, closeLambda(node)))),
        arg(
          "title",
          lambdaOf(
            Call(
              "${m3("TimePickerDialogDefaults")}.Title",
              listOf(arg("displayMode", mode)),
            ),
          ),
        ),
        arg(
          "modeToggleButton",
          lambdaOf(
            Call(
              "${m3("TimePickerDialogDefaults")}.DisplayModeToggle",
              listOf(
                arg(
                  "onDisplayModeChange",
                  lambda(
                    Assign(
                      mode,
                      "=",
                      Raw(
                        "if ($mode == $displayMode.Picker) $displayMode.Input else $displayMode.Picker",
                      ),
                    ),
                  ),
                ),
                arg("displayMode", mode),
              ),
            ),
          ),
        ),
      ),
      lambda(
        IfStmt(
          listOf(
            "$mode == $displayMode.Picker" to
              listOf(stmt(Call(m3("TimePicker"), listOf(arg("state", state))))),
          ),
          listOf(stmt(Call(m3("TimeInput"), listOf(arg("state", state))))),
        ),
      ),
    )
  return listOf(declaration, modeState, guarded(node, stmt(dialog)))
}
