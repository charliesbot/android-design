package bot.charlies.androiddesign.preview.domain.export

import bot.charlies.androiddesign.preview.domain.design.TypeScale
import bot.charlies.androiddesign.preview.domain.design.TypeScale.baseRole
import bot.charlies.androiddesign.preview.domain.design.TypeScale.isEmphasized
import bot.charlies.androiddesign.preview.model.DesignSystem
import bot.charlies.androiddesign.preview.model.ShapeToken
import bot.charlies.androiddesign.preview.model.TypeRole
import java.math.BigDecimal

/** The `Theme`, `Typography` and `Shapes` of the exported file, from the resolved design system. */
internal class ThemeEmitter(
  private val ctx: FileContext,
  private val design: DesignSystem,
  private val themeName: String,
) {
  private class Axis(val tag: String, val value: Float)

  /** One font file with its variation axes; roles with the same spec share a family value. */
  private class FamilySpec(val family: String, val axes: List<Axis>) {
    val key = family + axes.joinToString("") { "|${it.tag}=${it.value}" }
    val weights = sortedSetOf(400, 500, 700)
    lateinit var valName: String
  }

  private val specs = linkedMapOf<String, FamilySpec>()
  private val roleSpecs = linkedMapOf<TypeRole, FamilySpec>()

  fun declarations(): List<Decl> {
    resolveFamilies()
    return buildList {
      add(ValDecl("private ", "SeedColor", Raw(colorLiteral(design.seed))))
      add(RawDecl(fontHelper()))
      specs.values.forEach { add(familyDecl(it)) }
      add(ValDecl("private ", "BaseTypography", Call(ctx.use(Sym.Typography))))
      add(ValDecl("private ", "AppTypography", typography()))
      add(ValDecl("private ", "AppShapes", shapes()))
      add(themeFunction())
    }
  }

  // region fonts

  private fun resolveFamilies() {
    for (role in TypeRole.entries) {
      val override = design.typography[role]
      val family = override?.fontFamily ?: DEFAULT_FAMILY
      val axes = parseAxes(override?.fontVariation)
      val explicitWeight = override?.fontWeight ?: axes.weight
      val spec = FamilySpec(family, axes.settings)
      val shared = specs.getOrPut(spec.key) { spec }
      explicitWeight?.let { shared.weights += it }
      roleSpecs[role] = shared
    }
    specs.values.forEach { spec ->
      spec.valName =
        if (spec.axes.isEmpty()) Naming.pascal(spec.family)
        else
          Naming.pascal(spec.family) +
            Naming.pascal(roleSpecs.entries.first { it.value === spec }.key.wire)
    }
  }

  private class ParsedAxes(val settings: List<Axis>, val weight: Int?)

  private fun parseAxes(text: String?): ParsedAxes {
    if (text == null) return ParsedAxes(emptyList(), null)
    var weight: Int? = null
    val settings = mutableListOf<Axis>()
    AXIS.findAll(text).forEach { match ->
      val tag = match.groupValues[1]
      val value = match.groupValues[2].toFloat()
      if (tag == "wght") weight = value.toInt() else settings += Axis(tag, value)
    }
    return ParsedAxes(settings, weight)
  }

  private fun fontHelper(): List<String> {
    ctx.use(Sym.FontRes)
    ctx.use(Sym.Font)
    ctx.use(Sym.FontFamily)
    ctx.use(Sym.FontStyle)
    ctx.use(Sym.FontVariation)
    ctx.use(Sym.FontWeight)
    return """
    /** A variable font at each weight in [weights], with the same variation [axes] on all. */
    private fun variableFontFamily(
      @FontRes font: Int,
      weights: List<Int>,
      vararg axes: FontVariation.Setting,
    ): FontFamily =
      FontFamily(
        weights.map { weight ->
          Font(
            font,
            FontWeight(weight),
            variationSettings =
              FontVariation.Settings(
                FontWeight(weight),
                FontStyle.Normal,
                FontVariation.weight(weight),
                *axes,
              ),
          )
        },
      )
    """
      .trimIndent()
      .lines()
  }

  private fun familyDecl(spec: FamilySpec): ValDecl {
    val file = Naming.snake(spec.family)
    val args =
      mutableListOf(
        pos(ctx.font(file)),
        pos(Call("listOf", spec.weights.map { pos(it.toString()) })),
      )
    spec.axes.forEach { args += pos(axisExpr(it)) }
    return ValDecl("private ", spec.valName, Call("variableFontFamily", args))
  }

  private fun axisExpr(axis: Axis): Expr {
    val font = ctx.use(Sym.FontVariation)
    return when (axis.tag) {
      "opsz" -> Raw("$font.opticalSizing(${number(axis.value)}.${ctx.use(Sym.sp)})")
      "wdth" -> Raw("$font.width(${number(axis.value)}f)")
      "slnt" -> Raw("$font.slant(${number(axis.value)}f)")
      "ital" -> Raw("$font.italic(${number(axis.value)}f)")
      else -> Raw("$font.Setting(${kotlinString(axis.tag)}, ${number(axis.value)}f)")
    }
  }

  // endregion

  // region typography and shapes

  private fun typography(): Expr {
    val fontWeight = ctx.use(Sym.FontWeight)
    val sp = ctx.use(Sym.sp)
    val args =
      TypeRole.entries.map { role ->
        val override = design.typography[role]
        val token = TypeScale.of(role)
        val copy = mutableListOf(arg("fontFamily", roleSpecs.getValue(role).valName))
        val weight =
          override?.fontWeight
            ?: parseAxes(override?.fontVariation).weight
            ?: token.weight.takeIf { role.isEmphasized }
        if (weight != null) copy += arg("fontWeight", "$fontWeight($weight)")
        val tracking =
          override?.letterSpacing?.let(::parseNumber)
            ?: token.trackingSp.takeIf { role.isEmphasized }
        if (tracking != null) copy += arg("letterSpacing", spUnit(tracking, sp))
        arg(role.wire, Call("BaseTypography.${role.baseRole.wire}.copy", copy))
      }
    return Call(ctx.use(Sym.Typography), args)
  }

  private fun shapes(): Expr {
    val dp = ctx.use(Sym.dp)
    val rounded = ctx.use(Sym.RoundedCornerShape)
    val tokens =
      listOf(
        ShapeToken.ExtraSmall,
        ShapeToken.Small,
        ShapeToken.Medium,
        ShapeToken.Large,
        ShapeToken.LargeIncreased,
        ShapeToken.ExtraLarge,
        ShapeToken.ExtraLargeIncreased,
        ShapeToken.ExtraExtraLarge,
      )
    return Call(
      ctx.use(Sym.Shapes),
      tokens.map { token ->
        arg(token.wire, "$rounded(${number(design.rounded.getValue(token))}.$dp)")
      },
    )
  }

  // endregion

  // region theme

  private fun themeFunction(): Decl {
    ctx.optIn(Sym.ExperimentalMaterial3ExpressiveApi)
    ctx.use(Sym.Composable)
    val kolor = ctx.use(Sym.rememberDynamicColorScheme)
    val style = ctx.use(Sym.PaletteStyle)
    val spec = ctx.use(Sym.ColorSpec)
    val scheme =
      Call(
        kolor,
        listOf(
          arg("seedColor", "SeedColor"),
          arg("isDark", "darkTheme"),
          arg("style", "$style.${design.variant.name}"),
          arg(
            "contrastLevel",
            number(design.contrast.toFloat()).let { if (it.contains('.')) it else "$it.0" },
          ),
          arg("specVersion", "$spec.SpecVersion.SPEC_2025"),
        ),
      )
    val body = mutableListOf<Stmt>()
    val colorScheme: Expr
    if (design.colorOverrides.isEmpty()) {
      colorScheme = scheme
    } else {
      val color = ctx.use(Sym.Color)
      body += Assign("val dynamicScheme", "=", scheme)
      body +=
        Assign(
          "val colorScheme",
          "=",
          Call(
            "dynamicScheme.copy",
            design.colorOverrides.map { (role, hex) -> arg(role.wire, colorLiteral(hex, color)) },
          ),
        )
      colorScheme = Raw("colorScheme")
    }
    body +=
      stmt(
        Call(
          ctx.use(Sym.MaterialExpressiveTheme),
          listOf(
            arg("colorScheme", colorScheme),
            arg("typography", "AppTypography"),
            arg("shapes", "AppShapes"),
            arg("motionScheme", "${ctx.use(Sym.MotionScheme)}.expressive()"),
            arg("content", "content"),
          ),
        ),
      )
    return FunDecl(
      annotations = listOf("@Composable"),
      modifiers = "",
      name = themeName,
      params =
        listOf(
          "darkTheme: Boolean = ${ctx.use(Sym.isSystemInDarkTheme)}()",
          "content: @Composable () -> Unit",
        ),
      body = body,
    )
  }

  // endregion

  private fun colorLiteral(hex: String, colorName: String = ctx.use(Sym.Color)): String =
    "$colorName(0xFF${hex.removePrefix("#").uppercase()})"

  private fun spUnit(value: Float, sp: String): String =
    if (value < 0f) "(${number(value)}).$sp" else "${number(value)}.$sp"

  private fun parseNumber(text: String): Float? =
    Regex("^\\s*(-?\\d+(?:\\.\\d+)?)\\s*(?:px|dp|sp)?\\s*$")
      .matchEntire(text)
      ?.groupValues
      ?.get(1)
      ?.toFloat()

  private companion object {
    const val DEFAULT_FAMILY = "Google Sans Flex"
    val AXIS = Regex("'([A-Za-z]{4})'\\s+(-?\\d+(?:\\.\\d+)?)")
  }
}

/** A float as Kotlin writes it: `16`, `0.15`; the caller adds `.dp`, `f` and so on. */
internal fun number(value: Float): String =
  BigDecimal(value.toString()).stripTrailingZeros().toPlainString()
