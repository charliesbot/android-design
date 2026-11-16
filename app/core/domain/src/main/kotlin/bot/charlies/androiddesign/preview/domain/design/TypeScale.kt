package bot.charlies.androiddesign.preview.domain.design

import bot.charlies.androiddesign.preview.model.SurfaceSpec
import bot.charlies.androiddesign.preview.model.TypeGroup
import bot.charlies.androiddesign.preview.model.TypeRole
import bot.charlies.androiddesign.preview.model.WearSurface

/** Size, weight and tracking of one type role. */
internal data class TypeSpec(val sizeSp: Float, val weight: Int, val trackingSp: Float)

/** Material 3 and Wear Material 3 type scales for WCAG large-text contrast thresholds. */
internal object TypeScale {
  /**
   * The size and weight of the type role named [wire] in [surface], or null if it has no such role.
   */
  fun of(surface: SurfaceSpec, wire: String): TypeSpec? =
    if (surface === WearSurface) wear[wire] else TypeRole.fromWire(wire)?.let(::of)

  /**
   * The Wear Material 3 type scale, read from `TypeScaleTokens` of the Wear Compose sources
   * (`app/wear/compose/upstream/material3/.../tokens/TypeScaleTokens.kt`); a test compares it with
   * the library's `Typography()`. The weights are variable font weights, so none reaches the bold
   * weight (700) that makes a smaller text count as large.
   */
  private val wear: Map<String, TypeSpec> =
    mapOf(
      "displayLarge" to TypeSpec(40f, 500, 0f),
      "displayMedium" to TypeSpec(30f, 520, 0f),
      "displaySmall" to TypeSpec(24f, 550, 0f),
      "titleLarge" to TypeSpec(18f, 500, 0f),
      "titleMedium" to TypeSpec(16f, 550, 0f),
      "titleSmall" to TypeSpec(14f, 550, 0f),
      "labelLarge" to TypeSpec(20f, 500, 0f),
      "labelMedium" to TypeSpec(15f, 500, 0f),
      "labelSmall" to TypeSpec(13f, 500, 0f),
      "bodyLarge" to TypeSpec(16f, 450, 0f),
      "bodyMedium" to TypeSpec(14f, 450, 0f),
      "bodySmall" to TypeSpec(12f, 500, 0f),
      "bodyExtraSmall" to TypeSpec(10f, 500, 0f),
      "numeralExtraLarge" to TypeSpec(60f, 560, 0f),
      "numeralLarge" to TypeSpec(50f, 580, 0f),
      "numeralMedium" to TypeSpec(40f, 580, 0f),
      "numeralSmall" to TypeSpec(30f, 550, 0f),
      "numeralExtraSmall" to TypeSpec(24f, 550, 0f),
    )

  fun of(role: TypeRole): TypeSpec {
    val base = baseline(role)
    return if (role.isEmphasized)
      base.copy(weight = emphasizedWeight(role), trackingSp = emphasizedTracking(role))
    else base
  }

  val TypeRole.isEmphasized: Boolean
    get() = name.endsWith("Emphasized")

  /** The role without `Emphasized`, which is the library style an emphasized one copies. */
  val TypeRole.baseRole: TypeRole
    get() = if (isEmphasized) TypeRole.valueOf(name.removeSuffix("Emphasized")) else this

  private fun baseline(role: TypeRole): TypeSpec =
    when (role.baseRole) {
      TypeRole.DisplayLarge -> TypeSpec(57f, 400, -0.2f)
      TypeRole.DisplayMedium -> TypeSpec(45f, 400, 0f)
      TypeRole.DisplaySmall -> TypeSpec(36f, 400, 0f)
      TypeRole.HeadlineLarge -> TypeSpec(32f, 400, 0f)
      TypeRole.HeadlineMedium -> TypeSpec(28f, 400, 0f)
      TypeRole.HeadlineSmall -> TypeSpec(24f, 400, 0f)
      TypeRole.TitleLarge -> TypeSpec(22f, 400, 0f)
      TypeRole.TitleMedium -> TypeSpec(16f, 500, 0.2f)
      TypeRole.TitleSmall -> TypeSpec(14f, 500, 0.1f)
      TypeRole.BodyLarge -> TypeSpec(16f, 400, 0.5f)
      TypeRole.BodyMedium -> TypeSpec(14f, 400, 0.2f)
      TypeRole.BodySmall -> TypeSpec(12f, 400, 0.4f)
      TypeRole.LabelLarge -> TypeSpec(14f, 500, 0.1f)
      TypeRole.LabelMedium -> TypeSpec(12f, 500, 0.5f)
      TypeRole.LabelSmall -> TypeSpec(11f, 500, 0.5f)
      else -> error("not a baseline role: $role")
    }

  private fun emphasizedWeight(role: TypeRole): Int =
    when (role.group) {
      TypeGroup.Display,
      TypeGroup.Headline -> 500
      TypeGroup.Title -> if (role.baseRole == TypeRole.TitleLarge) 500 else 700
      TypeGroup.Body -> 500
      TypeGroup.Label -> 700
    }

  private fun emphasizedTracking(role: TypeRole): Float =
    when (role.baseRole) {
      TypeRole.TitleMedium -> 0.15f
      TypeRole.TitleSmall -> 0.1f
      TypeRole.BodyLarge -> 0.15f
      TypeRole.BodyMedium -> 0.25f
      TypeRole.BodySmall -> 0.4f
      TypeRole.LabelLarge -> 0.1f
      TypeRole.LabelMedium,
      TypeRole.LabelSmall -> 0.5f
      else -> 0f
    }
}
