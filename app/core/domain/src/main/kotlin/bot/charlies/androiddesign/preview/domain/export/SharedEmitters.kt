package bot.charlies.androiddesign.preview.domain.export

import kotlinx.serialization.json.JsonObject

// The emitters the phone and Wear exporters share: argument helpers and the props that map to the
// same Compose code in both, written once on the shared emitter core.

internal fun opt(name: String, value: String?): Arg? = value?.let { arg(name, it) }

internal fun opt(name: String, value: Expr?): Arg? = value?.let { arg(name, it) }

/** `Component(args) { body }` as a one-statement list. */
internal fun one(callee: String, args: List<Arg?>, body: List<Stmt>? = null): List<Stmt> =
  listOf(stmt(Call(callee, args.filterNotNull(), body?.let { lambda(it) })))

/** `Arrangement` for a column's `verticalArrangement` or a row's `horizontalArrangement`. */
internal fun ScreenEmitterBase.arrangement(p: JsonObject, vertical: Boolean): String? {
  val arrangement = file.use(Sym.Arrangement)
  val spacing = p.literal("spacing")?.let(::spacing)
  val kind = p.literal("arrangement")
  val distribution =
    mapOf(
      "spaceBetween" to "SpaceBetween",
      "spaceAround" to "SpaceAround",
      "spaceEvenly" to "SpaceEvenly",
    )
  distribution[kind]?.let {
    return "$arrangement.$it"
  }
  val alignment = file.use(Sym.Alignment)
  val end = if (vertical) "Bottom" else "End"
  val centered = if (vertical) "CenterVertically" else "CenterHorizontally"
  return when {
    spacing != null && kind == "center" -> "$arrangement.spacedBy($spacing, $alignment.$centered)"
    spacing != null && kind == "end" -> "$arrangement.spacedBy($spacing, $alignment.$end)"
    spacing != null -> "$arrangement.spacedBy($spacing)"
    kind == "center" -> "$arrangement.Center"
    kind == "end" -> "$arrangement.$end"
    else -> null
  }
}

/** A `Spacer` sized by the `width` and `height` spacing tokens. */
internal fun ScreenEmitterBase.spacer(p: JsonObject, place: Placement): List<Stmt> {
  val width = p.literal("width")?.let { "${useModifier("width")}(${spacing(it)})" }
  val height = p.literal("height")?.let { "${useModifier("height")}(${spacing(it)})" }
  val chain = modifierChain(place, listOfNotNull(width, height))
  return one(file.use(Sym.Spacer), listOf(pos(chain)))
}

/** A column's `horizontalAlignment` as `Alignment.CenterHorizontally` or `Alignment.End`. */
internal fun ScreenEmitterBase.horizontalAlignment(p: JsonObject): String? =
  when (p.literal("horizontalAlignment")) {
    "center" -> "${file.use(Sym.Alignment)}.CenterHorizontally"
    "end" -> "${file.use(Sym.Alignment)}.End"
    else -> null
  }

/** A row's `verticalAlignment` as `Alignment.CenterVertically` or `Alignment.Bottom`. */
internal fun ScreenEmitterBase.verticalAlignment(p: JsonObject): String? =
  when (p.literal("verticalAlignment")) {
    "center" -> "${file.use(Sym.Alignment)}.CenterVertically"
    "bottom" -> "${file.use(Sym.Alignment)}.Bottom"
    else -> null
  }

/** A text's `align` as `TextAlign.Start`, `Center` or `End`. */
internal fun ScreenEmitterBase.textAlign(p: JsonObject): String? =
  when (p.literal("align")) {
    "start" -> "${file.use(Sym.TextAlign)}.Start"
    "center" -> "${file.use(Sym.TextAlign)}.Center"
    "end" -> "${file.use(Sym.TextAlign)}.End"
    else -> null
  }

/** An image's `fit` as the bare `ContentScale` name, `Fit` by default. */
internal fun ScreenEmitterBase.contentScale(p: JsonObject): String =
  when (p.choice("Image", "fit")) {
    "crop" -> "Crop"
    "fillBounds" -> "FillBounds"
    "fillWidth" -> "FillWidth"
    "fillHeight" -> "FillHeight"
    "inside" -> "Inside"
    else -> "Fit"
  }
