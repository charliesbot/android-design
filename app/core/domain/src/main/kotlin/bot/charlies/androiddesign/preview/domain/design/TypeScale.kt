package bot.charlies.androiddesign.preview.domain.design

import bot.charlies.androiddesign.preview.model.TypeGroup
import bot.charlies.androiddesign.preview.model.TypeRole

/** Size, weight and tracking of one type role. */
internal data class TypeSpec(val sizeSp: Float, val weight: Int, val trackingSp: Float)

/**
 * The Material 3 type scale as `compose-mapping.md` records it. The baseline weight and tracking
 * match the library's `Typography()`. The emphasized values are Google's tokens, which the pinned
 * library does not apply yet, so the exporter writes them out.
 */
internal object TypeScale {
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
