package bot.charlies.androiddesign.preview.domain.export.wear

import bot.charlies.androiddesign.preview.domain.export.Arg
import bot.charlies.androiddesign.preview.domain.export.Assign
import bot.charlies.androiddesign.preview.domain.export.Call
import bot.charlies.androiddesign.preview.domain.export.IconName
import bot.charlies.androiddesign.preview.domain.export.Raw
import bot.charlies.androiddesign.preview.domain.export.Stmt
import bot.charlies.androiddesign.preview.domain.export.Sym
import bot.charlies.androiddesign.preview.domain.export.arg
import bot.charlies.androiddesign.preview.domain.export.lambda
import bot.charlies.androiddesign.preview.domain.export.lambdaOf
import bot.charlies.androiddesign.preview.domain.export.one
import bot.charlies.androiddesign.preview.domain.export.stmt
import bot.charlies.androiddesign.preview.model.ComponentNode
import kotlinx.serialization.json.JsonObject

private fun WearScreenEmitter.onClick(node: ComponentNode, p: JsonObject): Arg =
  arg("onClick", actionLambda(p["action"], node.id))

private fun WearScreenEmitter.defaults(name: String) = wear(name)

/** The button's size modifier, e.g. `size(IconButtonDefaults.DefaultButtonSize)`. */
private fun WearScreenEmitter.sizeCall(constant: String) = "${useModifier("size")}($constant)"

// region buttons

/** The `colors` of a Button or CompactButton variant, null for `filled`, which is the default. */
private fun WearScreenEmitter.buttonColors(variant: String): Arg? {
  val defaults = defaults("ButtonDefaults")
  return when (variant) {
    "filledTonal" -> "$defaults.filledTonalButtonColors()"
    "filledVariant" -> "$defaults.filledVariantButtonColors()"
    "outlined" -> "$defaults.outlinedButtonColors()"
    "child" -> "$defaults.childButtonColors()"
    else -> null
  }?.let { arg("colors", it) }
}

private fun WearScreenEmitter.outlinedBorder(enabled: String): Arg =
  arg("border", "${defaults("ButtonDefaults")}.outlinedButtonBorder(enabled = $enabled)")

internal fun WearScreenEmitter.button(
  node: ComponentNode,
  p: JsonObject,
  place: WearPlace,
): List<Stmt> {
  val variant = p.choice("Button", "variant")
  // Tonal, outlined and child have a button of their own with those colors (and border) built in.
  val callee =
    when (variant) {
      "filledTonal" -> "FilledTonalButton"
      "outlined" -> "OutlinedButton"
      "child" -> "ChildButton"
      else -> "Button"
    }
  return one(
    wear(callee),
    listOf(
      onClick(node, p),
      modifier(node, p, place, describe = true, item = true),
      textSlot(p, "secondaryLabel", place),
      iconSlot(p.iconName("icon", place), "${defaults("ButtonDefaults")}.IconSize"),
      enabledArg(p, place),
      if (variant == "filledVariant") buttonColors(variant) else null,
      transformation(place),
    ),
    listOf(stmt(textCall(p.textOrEmpty("label", place)))),
  )
}

internal fun WearScreenEmitter.compactButton(
  node: ComponentNode,
  p: JsonObject,
  place: WearPlace,
): List<Stmt> {
  val variant = p.choice("CompactButton", "variant")
  val label = p.text("label", place)
  return one(
    wear("CompactButton"),
    listOf(
        onClick(node, p),
        modifier(node, p, place, describe = true, item = true),
        iconSlot(p.iconName("icon", place), "${defaults("ButtonDefaults")}.IconSize"),
        enabledArg(p, place),
        buttonColors(variant),
        if (variant == "outlined") outlinedBorder(p.boolExpr("enabled", place) ?: "true") else null,
        label?.let { arg("label", lambdaOf(textCall(it))) },
      )
      .let { args ->
        // The label is the trailing lambda when there is one, as in the other buttons.
        args.filter { it?.name != "label" }
      },
    label?.let { listOf(stmt(textCall(it))) },
  )
}

private fun iconButtonSize(size: String): String =
  when (size) {
    "extra-small" -> "ExtraSmallButtonSize"
    "small" -> "SmallButtonSize"
    "large" -> "LargeButtonSize"
    else -> "DefaultButtonSize"
  }

internal fun WearScreenEmitter.iconButton(
  node: ComponentNode,
  p: JsonObject,
  place: WearPlace,
): List<Stmt> {
  val defaults = defaults("IconButtonDefaults")
  val size = "$defaults.${iconButtonSize(p.choice("IconButton", "size"))}"
  val callee =
    when (p.choice("IconButton", "variant")) {
      "filled" -> "FilledIconButton"
      "filledTonal" -> "FilledTonalIconButton"
      "outlined" -> "OutlinedIconButton"
      else -> "IconButton"
    }
  return one(
    wear(callee),
    listOf(
      onClick(node, p),
      modifier(node, p, place, true, true, sizeCall(size)),
      enabledArg(p, place),
    ),
    listOf(
      stmt(
        iconCall(
          painter(p.iconName("icon", place) ?: IconName.Literal("")),
          "$defaults.iconSizeFor($size)",
        ),
      ),
    ),
  )
}

internal fun WearScreenEmitter.textButton(
  node: ComponentNode,
  p: JsonObject,
  place: WearPlace,
): List<Stmt> {
  val defaults = defaults("TextButtonDefaults")
  val size =
    when (p.choice("TextButton", "size")) {
      "small" -> "SmallButtonSize"
      "large" -> "LargeButtonSize"
      else -> "DefaultButtonSize"
    }
  val variant = p.choice("TextButton", "variant")
  val colors =
    when (variant) {
      "filled" -> "filledTextButtonColors"
      "filledTonal" -> "filledTonalTextButtonColors"
      "outlined" -> "outlinedTextButtonColors"
      else -> null
    }
  return one(
    wear("TextButton"),
    listOf(
      onClick(node, p),
      modifier(node, p, place, true, true, sizeCall("$defaults.$size")),
      enabledArg(p, place),
      colors?.let { arg("colors", "$defaults.$it()") },
      if (variant == "outlined") outlinedBorder(p.boolExpr("enabled", place) ?: "true") else null,
    ),
    listOf(stmt(textCall(p.textOrEmpty("label", place)))),
  )
}

/** `EdgeButtonSize.Small`; the default size is left out. */
private fun WearScreenEmitter.edgeSize(p: JsonObject): String {
  val name =
    when (p.choice("EdgeButton", "size")) {
      "extra-small" -> "ExtraSmall"
      "medium" -> "Medium"
      "large" -> "Large"
      else -> "Small"
    }
  return "${wear("EdgeButtonSize")}.$name"
}

internal fun WearScreenEmitter.edgeButton(
  node: ComponentNode,
  p: JsonObject,
  place: WearPlace,
): List<Stmt> {
  val size = edgeSize(p)
  val content = buildList {
    p.iconName("icon", place)?.let {
      add(stmt(iconCall(painter(it), "${wear("EdgeButtonDefaults")}.iconSizeFor($size)")))
    }
    p.text("label", place)?.let { add(stmt(textCall(it))) }
  }
  return one(
    wear("EdgeButton"),
    listOf(
      onClick(node, p),
      modifier(node, p, place, describe = true, item = false),
      if (p.choice("EdgeButton", "size") == "small") null else arg("buttonSize", size),
      enabledArg(p, place),
    ),
    content,
  )
}

internal fun WearScreenEmitter.buttonGroup(
  node: ComponentNode,
  p: JsonObject,
  place: WearPlace,
): List<Stmt> {
  // The group's scope gives each child its weight, so every member shares the width.
  val children =
    children(node, p, "children", place.inSlot("ButtonGroup", p).copy(scopeWeight = true))
  return children.prelude +
    one(
      wear("ButtonGroup"),
      listOf(modifier(node, p, place, item = true)),
      children.body,
    )
}

// endregion

// region toggles

/** `var xChecked by remember { mutableStateOf(false) }` and its name. */
private fun WearScreenEmitter.checkedState(
  node: ComponentNode,
  p: JsonObject,
  place: WearPlace,
): Pair<String, Stmt> {
  val name = stateName(node.id, "Checked")
  return name to stateVar(name, p.boolValue(node.component, "checked", place))
}

private fun WearScreenEmitter.onCheckedChange(
  node: ComponentNode,
  p: JsonObject,
  state: String,
): Arg =
  arg(
    "onCheckedChange",
    lambda(listOf<Stmt>(Assign(state, "=", Raw("it"))) + actionStatements(p["action"], node.id)),
  )

internal fun WearScreenEmitter.iconToggleButton(
  node: ComponentNode,
  p: JsonObject,
  place: WearPlace,
): List<Stmt> {
  val (state, declaration) = checkedState(node, p, place)
  val button = defaults("IconButtonDefaults")
  val size = "$button.${iconButtonSize(p.choice("IconToggleButton", "size"))}"
  val name = p.iconName("icon", place) ?: IconName.Literal("")
  val painter =
    "${file.use(Sym.painterResource)}(if ($state) ${drawable(name, filled = true)} else ${drawable(name)})"
  return listOf(declaration) +
    one(
      wear("IconToggleButton"),
      listOf(
        arg("checked", state),
        onCheckedChange(node, p, state),
        modifier(node, p, place, true, true, sizeCall(size)),
        enabledArg(p, place),
      ),
      listOf(
        stmt(iconCall(painter, "${defaults("IconToggleButtonDefaults")}.iconSizeFor($size)")),
      ),
    )
}

internal fun WearScreenEmitter.textToggleButton(
  node: ComponentNode,
  p: JsonObject,
  place: WearPlace,
): List<Stmt> {
  val (state, declaration) = checkedState(node, p, place)
  val size =
    when (p.choice("TextToggleButton", "size")) {
      "small" -> "${defaults("IconToggleButtonDefaults")}.SmallSize"
      "large" -> "${defaults("TextToggleButtonDefaults")}.LargeSize"
      else -> "${defaults("TextToggleButtonDefaults")}.Size"
    }
  return listOf(declaration) +
    one(
      wear("TextToggleButton"),
      listOf(
        arg("checked", state),
        onCheckedChange(node, p, state),
        modifier(node, p, place, true, true, sizeCall(size)),
        enabledArg(p, place),
      ),
      listOf(stmt(textCall(p.textOrEmpty("label", place)))),
    )
}

internal fun WearScreenEmitter.rowToggle(
  node: ComponentNode,
  p: JsonObject,
  place: WearPlace,
  callee: String,
): List<Stmt> {
  val (state, declaration) = checkedState(node, p, place)
  return listOf(declaration) +
    one(
      wear(callee),
      listOf(
        arg("checked", state),
        onCheckedChange(node, p, state),
        modifier(node, p, place, describe = true, item = true),
        enabledArg(p, place),
        transformation(place),
        iconSlot(p.iconName("icon", place), "${defaults("ButtonDefaults")}.IconSize"),
        textSlot(p, "secondaryLabel", place),
      ),
      listOf(stmt(textCall(p.textOrEmpty("label", place)))),
    )
}

/** A radio button is one of its parent's group: tapping it picks it and unpicks the others. */
internal fun WearScreenEmitter.radioButton(
  node: ComponentNode,
  p: JsonObject,
  place: WearPlace,
): List<Stmt> {
  val radio = place.radio
  // A radio button no parent groups (one nested in a template's Row, say) keeps its own state.
  val (state, index, declaration) =
    if (radio != null) Triple(radio.state, radio.index, null)
    else {
      val own = stateName(node.id, "Selected")
      Triple(
        own,
        "0",
        stateVar(
          own,
          when (val selected = p.boolValue("RadioButton", "selected", place)) {
            "true" -> "0"
            "false" -> "-1"
            else -> "if ($selected) 0 else -1"
          },
          WearSym.mutableIntStateOf,
        ),
      )
    }
  val selectAction =
    listOf<Stmt>(Assign(state, "=", Raw(index))) + actionStatements(p["action"], node.id)
  return listOfNotNull(declaration) +
    one(
      wear("RadioButton"),
      listOf(
        arg("selected", "$state == $index"),
        arg("onSelect", lambda(selectAction)),
        modifier(node, p, place, describe = true, item = true),
        enabledArg(p, place),
        transformation(place),
        iconSlot(p.iconName("icon", place), "${defaults("ButtonDefaults")}.IconSize"),
        textSlot(p, "secondaryLabel", place),
      ),
      listOf(stmt(textCall(p.textOrEmpty("label", place)))),
    )
}

// endregion

// region inputs

/** The value range of a Slider or Stepper as `min..max`, `0f..1f` unless the proposal gives one. */
private fun WearScreenEmitter.valueRange(p: JsonObject): Pair<Float, Float> {
  val (min, max) = p.range("range") ?: return 0f to 1f
  return if (min < max) min to max else 0f to 1f
}

private fun WearScreenEmitter.valueState(
  node: ComponentNode,
  p: JsonObject,
  range: Pair<Float, Float>,
  place: WearPlace,
): Pair<String, Stmt> {
  val initial =
    p.boundFloat("value", place)?.let {
      "$it.coerceIn(${floatLiteral(range.first)}, ${floatLiteral(range.second)})"
    }
      ?: floatLiteral(
        (p.numberProp(node.component, "value") ?: range.first).coerceIn(range.first, range.second),
      )
  val name = stateName(node.id, "Value")
  return name to stateVar(name, initial, Sym.mutableFloatStateOf)
}

internal fun WearScreenEmitter.slider(
  node: ComponentNode,
  p: JsonObject,
  place: WearPlace,
): List<Stmt> {
  val range = valueRange(p)
  val (state, declaration) = valueState(node, p, range, place)
  return listOf(declaration) +
    one(
      wear("Slider"),
      listOf(
        arg("value", state),
        arg("onValueChange", lambda(Assign(state, "=", Raw("it")))),
        arg("steps", (p.numberProp("Slider", "steps")?.toInt() ?: 0).toString()),
        modifier(node, p, place, describe = true, item = true),
        enabledArg(p, place),
        arg("valueRange", "${floatLiteral(range.first)}..${floatLiteral(range.second)}"),
      ),
    )
}

internal fun WearScreenEmitter.stepper(
  node: ComponentNode,
  p: JsonObject,
  place: WearPlace,
): List<Stmt> {
  val range = valueRange(p)
  val (state, declaration) = valueState(node, p, range, place)
  val defaults = defaults("SliderDefaults")
  val content =
    p.literal("content")
      ?.let { emit(it, place.inSlot("Stepper", p, "content").copy(parentStretches = false)) }
      .orEmpty()
  return listOf(declaration) +
    one(
      wear("Stepper"),
      listOf(
        arg("value", state),
        arg("onValueChange", lambda(Assign(state, "=", Raw("it")))),
        arg("steps", (p.numberProp("Stepper", "steps")?.toInt() ?: 0).toString()),
        arg("decreaseIcon", lambdaOf(Call("$defaults.DecreaseIcon"))),
        arg("increaseIcon", lambdaOf(Call("$defaults.IncreaseIcon"))),
        modifier(node, p, place, describe = true, item = true),
        enabledArg(p, place),
        arg("valueRange", "${floatLiteral(range.first)}..${floatLiteral(range.second)}"),
      ),
      content,
    )
}

// endregion

// region progress

/** The stroke the previewer passes: `largeStrokeWidth`, or the indeterminate ring's own. */
private fun WearScreenEmitter.strokeArg(indeterminate: Boolean): Arg {
  val defaults = defaults("CircularProgressIndicatorDefaults")
  return arg(
    "strokeWidth",
    "$defaults.${if (indeterminate) "IndeterminateStrokeWidth" else "largeStrokeWidth"}",
  )
}

private fun WearScreenEmitter.progressArg(
  p: JsonObject,
  component: String,
  place: WearPlace,
): Arg =
  arg(
    "progress",
    lambdaOf(
      Raw(
        p.boundFloat("progress", place) ?: floatLiteral(p.numberProp(component, "progress") ?: 0f),
      ),
    ),
  )

/** A progress ring with no progress, literal or bound, spins. */
private fun WearScreenEmitter.hasProgress(p: JsonObject, place: WearPlace): Boolean =
  p.numberProp("CircularProgressIndicator", "progress") != null ||
    p.boundFloat("progress", place) != null

/**
 * A ring fills the display when its parent gives it the whole screen (the body of a fixed screen),
 * and is a small spinner else.
 */
private fun WearScreenEmitter.ringModifier(
  node: ComponentNode,
  p: JsonObject,
  place: WearPlace,
): Arg? {
  val parts = modifierParts(node, p, place, describe = false, item = false)
  val ring =
    if (place.fillHeight) {
      listOf(
        useModifier("fillMaxSize()"),
        "${useModifier("padding")}(${defaults("CircularProgressIndicatorDefaults")}.FullScreenPadding)",
      )
    } else {
      listOf("${useModifier("size")}(${dp(RING_FALLBACK_DP)})")
    }
  return modifierArg(place, parts + ring)
}

private const val RING_FALLBACK_DP = 48f

internal fun WearScreenEmitter.circularProgress(
  node: ComponentNode,
  p: JsonObject,
  place: WearPlace,
): List<Stmt> =
  one(
    wear("CircularProgressIndicator"),
    listOf(
      if (!hasProgress(p, place)) null else progressArg(p, "CircularProgressIndicator", place),
      ringModifier(node, p, place),
      strokeArg(indeterminate = !hasProgress(p, place)),
    ),
  )

internal fun WearScreenEmitter.segmentedProgress(
  node: ComponentNode,
  p: JsonObject,
  place: WearPlace,
): List<Stmt> =
  one(
    wear("SegmentedCircularProgressIndicator"),
    listOf(
      arg(
        "segmentCount",
        (p.numberProp("SegmentedCircularProgressIndicator", "segmentCount")?.toInt() ?: 1)
          .toString(),
      ),
      progressArg(p, "SegmentedCircularProgressIndicator", place),
      ringModifier(node, p, place),
      strokeArg(indeterminate = false),
    ),
  )

internal fun WearScreenEmitter.linearProgress(
  node: ComponentNode,
  p: JsonObject,
  place: WearPlace,
): List<Stmt> =
  one(
    wear("LinearProgressIndicator"),
    listOf(
      progressArg(p, "LinearProgressIndicator", place),
      modifier(node, p, place, item = true),
    ),
  )

// endregion
