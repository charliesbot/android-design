package bot.charlies.androiddesign.preview.domain.validation

import bot.charlies.androiddesign.preview.domain.design.DesignSystemResolver
import bot.charlies.androiddesign.preview.model.ColorRole
import bot.charlies.androiddesign.preview.model.DesignSystem
import bot.charlies.androiddesign.preview.model.MaterialShape
import bot.charlies.androiddesign.preview.model.PaletteVariant
import bot.charlies.androiddesign.preview.model.ShapeToken
import bot.charlies.androiddesign.preview.model.StyleDef
import bot.charlies.androiddesign.preview.model.ThemeOverride
import bot.charlies.androiddesign.preview.model.TypeGroup
import bot.charlies.androiddesign.preview.model.TypeRole

/**
 * Checks one theme layer: DESIGN.md front matter or a proposal's `theme`. A strict layer rejects
 * names it does not know; DESIGN.md may carry names for other tools, so it only has the values it
 * shares with us checked.
 */
internal class ThemeValidator(private val sink: ErrorSink, private val design: DesignSystem) {
  fun validate(layer: ThemeOverride?, root: String, strict: Boolean) {
    if (layer == null) return
    validateColors(layer, root, strict)
    validateTypography(layer, root, strict)
    validateDimensions(layer.rounded, root.child("rounded"), strict, "corner name") {
      ShapeToken.fromKebab(it) != null
    }
    validateDimensions(
      layer.spacing,
      root.child("spacing"),
      strict = false,
      "spacing name",
      maxSize = true,
    ) {
      true
    }
    validateScheme(layer, root)
    layer.material?.styles?.forEach { (name, style) ->
      validateStyle(style, root.child("material").child("styles").child(name))
    }
  }

  private fun validateColors(layer: ThemeOverride, root: String, strict: Boolean) {
    for ((key, value) in layer.colors) {
      val known = key == "primary" || ColorRole.fromKebab(key) != null
      val path = root.child("colors").child(key)
      if (!known) {
        if (strict)
          sink.add(path, "'$key' is not a color role (kebab case, such as surface-container-high)")
        continue
      }
      if (DesignSystemResolver.parseHex(value) == null) {
        sink.add(path, "'$value' must be a #RRGGBB color")
      }
    }
  }

  private fun validateTypography(layer: ThemeOverride, root: String, strict: Boolean) {
    for ((key, spec) in layer.typography) {
      val path = root.child("typography").child(key)
      val role = TypeRole.fromKebab(key)
      if (role == null) {
        if (strict) sink.add(path, "'$key' is not a type role (kebab case, such as display-large)")
        continue
      }
      spec.fontWeight?.let {
        if (it !in 1..1000) {
          sink.add(path.child("fontWeight"), "fontWeight must be a whole number from 1 to 1000")
        }
      }
      spec.fontVariation?.let { validateVariation(it, path.child("fontVariation")) }
      val family = spec.fontFamily ?: continue
      if (!familyAllowed(role, family)) {
        sink.add(
          path.child("fontFamily"),
          "'$family' is not allowed for $key: ${allowedFamilies(role)}",
        )
      }
    }
  }

  /** Every axis value is a finite number, and `wght` is a weight Compose can build. */
  private fun validateVariation(text: String, path: String) {
    for (match in AXIS.findAll(text)) {
      val tag = match.groupValues[1]
      val value = match.groupValues[2].toFloat()
      when {
        !value.isFinite() -> sink.add(path, "'$tag' must be a finite number")
        tag == "wght" && value !in 1f..1000f -> sink.add(path, "'wght' must be from 1 to 1000")
      }
    }
  }

  private fun validateDimensions(
    values: Map<String, String>,
    path: String,
    strict: Boolean,
    nameKind: String,
    maxSize: Boolean = false,
    knownName: (String) -> Boolean,
  ) {
    for ((key, value) in values) {
      val at = path.child(key)
      if (!knownName(key)) {
        if (strict) sink.add(at, "'$key' is not a $nameKind")
        continue
      }
      val size = DesignSystemResolver.parseDimension(value)
      when {
        size == null -> sink.add(at, "'$value' must be a dimension such as 16px")
        size < 0f -> sink.add(at, "'$value' is negative; a size cannot be less than 0")
        size > DesignSystemResolver.MAX_SPACING_DP && maxSize ->
          sink.add(
            at,
            "'$value' is too large; a spacing is at most ${DesignSystemResolver.MAX_SPACING_DP.toInt()}px",
          )
      }
    }
  }

  private fun validateScheme(layer: ThemeOverride, root: String) {
    val scheme = layer.material?.scheme ?: return
    val path = root.child("material").child("scheme")
    scheme.variant?.let {
      if (PaletteVariant.fromWire(it) == null) {
        sink.add(
          path.child("variant"),
          "'$it' is not a variant: ${PaletteVariant.entries.joinToString { v -> v.wire }}",
        )
      }
    }
    scheme.contrast?.let {
      if (it < -1.0 || it > 1.0)
        sink.add(path.child("contrast"), "contrast must be between -1.0 and 1.0")
    }
    scheme.seed?.let {
      val reference = Regex("^\\{colors\\.[A-Za-z0-9-]+}$").matches(it)
      if (!reference && DesignSystemResolver.parseHex(it) == null) {
        sink.add(
          path.child("seed"),
          "seed must be a #RRGGBB color or a reference such as {colors.primary}",
        )
      }
    }
  }

  private fun validateStyle(style: StyleDef, path: String) {
    style.type?.let {
      if (TypeRole.fromWire(it) == null) sink.add(path.child("type"), "'$it' is not a type role")
    }
    style.color?.let {
      if (ColorRole.fromWire(it) == null) sink.add(path.child("color"), "'$it' is not a color role")
    }
    style.container?.let {
      if (ColorRole.fromWire(it) == null)
        sink.add(path.child("container"), "'$it' is not a color role")
    }
    style.shape?.let {
      if (!isShape(it))
        sink.add(path.child("shape"), "'$it' is not a shape token or MaterialShapes name")
    }
    style.padding?.let {
      if (it !in design.spacing)
        sink.add(path.child("padding"), "'$it' is not a spacing name of the design system")
    }
  }

  companion object {
    /** One axis of a CSS `font-variation-settings` string, such as `'opsz' 72`. */
    private val AXIS = Regex("""['"]([A-Za-z0-9]{4})['"]\s*(-?\d+(?:\.\d+)?)""")

    fun isShape(value: String): Boolean =
      ShapeToken.fromWire(value) != null || MaterialShape.fromWire(value) != null

    /**
     * Google Sans Flex and Google Sans Code serve every role. Brand faces take display and
     * headline; Doto is for display sizes only.
     */
    fun familyAllowed(role: TypeRole, family: String): Boolean =
      when (family) {
        "Google Sans Flex",
        "Google Sans Code" -> true
        "Fraunces",
        "Bricolage Grotesque",
        "Big Shoulders Display" ->
          role.group == TypeGroup.Display || role.group == TypeGroup.Headline
        "Doto" -> role.group == TypeGroup.Display
        else -> false
      }

    private fun allowedFamilies(role: TypeRole): String =
      listOf(
          "Google Sans Flex",
          "Google Sans Code",
          "Fraunces",
          "Bricolage Grotesque",
          "Big Shoulders Display",
          "Doto",
        )
        .filter { familyAllowed(role, it) }
        .joinToString(prefix = "use ")
  }
}
