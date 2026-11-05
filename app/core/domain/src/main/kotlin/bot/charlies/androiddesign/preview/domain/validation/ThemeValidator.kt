package bot.charlies.androiddesign.preview.domain.validation

import bot.charlies.androiddesign.preview.domain.design.DesignSystemResolver
import bot.charlies.androiddesign.preview.model.BundledFont
import bot.charlies.androiddesign.preview.model.Catalog
import bot.charlies.androiddesign.preview.model.Catalogs
import bot.charlies.androiddesign.preview.model.DesignSystem
import bot.charlies.androiddesign.preview.model.MaterialShape
import bot.charlies.androiddesign.preview.model.PaletteVariant
import bot.charlies.androiddesign.preview.model.ShapeToken
import bot.charlies.androiddesign.preview.model.StyleDef
import bot.charlies.androiddesign.preview.model.ThemeOverride
import bot.charlies.androiddesign.preview.model.TypeGroup
import bot.charlies.androiddesign.preview.model.camelToKebab

/**
 * Checks one theme layer: DESIGN.md front matter or a proposal's `theme`. A strict layer rejects
 * names it does not know; DESIGN.md may carry names for other tools, so it only has the values it
 * shares with us checked.
 */
internal class ThemeValidator(
  private val sink: ErrorSink,
  private val design: DesignSystem,
  private val catalog: Catalog,
) {
  private val colorKebabs: Set<String> = catalog.colorRoles.map { it.camelToKebab() }.toSet()
  private val typeKebabs: Map<String, TypeGroup> =
    catalog.typeRoles.mapKeys { (wire, _) -> wire.camelToKebab() }

  private val allTypeRoles: Set<String> = Catalogs.all.flatMap { it.typeRoles.keys }.toSet()
  private val allColorRoles: Set<String> = Catalogs.all.flatMap { it.colorRoles }.toSet()

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
      validateStyle(style, root.child("material").child("styles").child(name), strict)
    }
  }

  private fun validateColors(layer: ThemeOverride, root: String, strict: Boolean) {
    for ((key, value) in layer.colors) {
      val known = key == "primary" || key in colorKebabs
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
    // A catalog with a platform typeface sets all its text in it: DESIGN.md typography, written
    // for another surface, does not apply, and a proposal may only name that typeface.
    if (catalog.platformFontFamilies.isNotEmpty()) {
      if (strict) validatePlatformTypography(layer, root)
      return
    }
    for ((key, spec) in layer.typography) {
      val path = root.child("typography").child(key)
      val group = typeKebabs[key]
      if (group == null) {
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
      if (!familyAllowed(group, family)) {
        sink.add(
          path.child("fontFamily"),
          "'$family' is not allowed for $key: ${allowedFamilies(group)}",
        )
      }
    }
  }

  private fun validatePlatformTypography(layer: ThemeOverride, root: String) {
    for ((key, spec) in layer.typography) {
      val path = root.child("typography").child(key)
      if (key !in typeKebabs) {
        sink.add(path, "'$key' is not a type role (kebab case, such as display-large)")
        continue
      }
      spec.fontWeight?.let {
        if (it !in 1..1000) {
          sink.add(path.child("fontWeight"), "fontWeight must be a whole number from 1 to 1000")
        }
      }
      spec.fontVariation?.let { validateVariation(it, path.child("fontVariation")) }
      val family = spec.fontFamily ?: continue
      if (catalog.platformFontFamilies.none { it.equals(family.trim(), ignoreCase = true) }) {
        sink.add(
          path.child("fontFamily"),
          "'$family' is not allowed for $key: ${catalog.key} text is set in " +
            catalog.platformFontFamilies.joinToString(),
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
      if (!DesignSystemResolver.isColorReference(it) && DesignSystemResolver.parseHex(it) == null) {
        sink.add(
          path.child("seed"),
          "seed must be a #RRGGBB color or a reference such as {colors.primary}",
        )
      }
    }
  }

  /**
   * DESIGN.md is shared by every surface of an app, so its styles (`strict` is false) may use the
   * roles of any catalog; a proposal's own `theme` is held to its catalog's roles.
   */
  private fun validateStyle(style: StyleDef, path: String, strict: Boolean) {
    val typeRoles = if (strict) catalog.typeRoles.keys else allTypeRoles
    val colorRoles = if (strict) catalog.colorRoles.toSet() else allColorRoles
    style.type?.let {
      if (it !in typeRoles) sink.add(path.child("type"), "'$it' is not a type role")
    }
    style.color?.let {
      if (it !in colorRoles) sink.add(path.child("color"), "'$it' is not a color role")
    }
    style.container?.let {
      if (it !in colorRoles) sink.add(path.child("container"), "'$it' is not a color role")
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

    fun familyAllowed(group: TypeGroup, family: String): Boolean =
      BundledFont.byFamily(family)?.let { group in it.groups } ?: false

    private fun allowedFamilies(group: TypeGroup): String =
      BundledFont.entries.filter { group in it.groups }.joinToString(prefix = "use ") { it.family }
  }
}
