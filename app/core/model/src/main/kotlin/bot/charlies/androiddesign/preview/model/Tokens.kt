package bot.charlies.androiddesign.preview.model

/**
 * Token vocabularies. An entry's [wire] name is the name a proposal uses and also the name of the
 * matching Compose property, so the renderer, the validator and the exporter share one spelling.
 */

/** `androidx.compose.material3.ColorScheme` role names. */
enum class ColorRole {
  Primary,
  OnPrimary,
  PrimaryContainer,
  OnPrimaryContainer,
  InversePrimary,
  Secondary,
  OnSecondary,
  SecondaryContainer,
  OnSecondaryContainer,
  Tertiary,
  OnTertiary,
  TertiaryContainer,
  OnTertiaryContainer,
  Background,
  OnBackground,
  Surface,
  OnSurface,
  SurfaceVariant,
  OnSurfaceVariant,
  SurfaceTint,
  InverseSurface,
  InverseOnSurface,
  Error,
  OnError,
  ErrorContainer,
  OnErrorContainer,
  Outline,
  OutlineVariant,
  Scrim,
  SurfaceBright,
  SurfaceDim,
  SurfaceContainer,
  SurfaceContainerHigh,
  SurfaceContainerHighest,
  SurfaceContainerLow,
  SurfaceContainerLowest,
  PrimaryFixed,
  PrimaryFixedDim,
  OnPrimaryFixed,
  OnPrimaryFixedVariant,
  SecondaryFixed,
  SecondaryFixedDim,
  OnSecondaryFixed,
  OnSecondaryFixedVariant,
  TertiaryFixed,
  TertiaryFixedDim,
  OnTertiaryFixed,
  OnTertiaryFixedVariant;

  val wire: String
    get() = lowerCamelName()

  val kebab: String
    get() = wire.camelToKebab()

  companion object {
    fun fromWire(value: String): ColorRole? = entries.firstOrNull { it.wire == value }

    fun fromKebab(value: String): ColorRole? = entries.firstOrNull { it.kebab == value }
  }
}

/** The type scale groups; they decide which brand typefaces a role may use. */
enum class TypeGroup {
  Display,
  Headline,
  Title,
  Body,
  Label,
}

/**
 * The typefaces the app bundles, in schema order. [family] is the name a DESIGN.md uses and
 * [groups] the type groups the face may set: Google Sans Flex and Google Sans Code serve every
 * role, brand faces take display and headline, and Doto is for display sizes only.
 */
enum class BundledFont(val family: String, val groups: Set<TypeGroup>) {
  GoogleSansFlex("Google Sans Flex", TypeGroup.entries.toSet()),
  GoogleSansCode("Google Sans Code", TypeGroup.entries.toSet()),
  Fraunces("Fraunces", setOf(TypeGroup.Display, TypeGroup.Headline)),
  BricolageGrotesque("Bricolage Grotesque", setOf(TypeGroup.Display, TypeGroup.Headline)),
  BigShouldersDisplay("Big Shoulders Display", setOf(TypeGroup.Display, TypeGroup.Headline)),
  Doto("Doto", setOf(TypeGroup.Display));

  companion object {
    /** The face named [family], matching the exact case unless [ignoreCase]. */
    fun byFamily(family: String, ignoreCase: Boolean = false): BundledFont? = entries.firstOrNull {
      it.family.equals(family, ignoreCase)
    }
  }
}

/** `androidx.compose.material3.Typography` roles, including the Emphasized set. */
enum class TypeRole(val group: TypeGroup) {
  DisplayLarge(TypeGroup.Display),
  DisplayMedium(TypeGroup.Display),
  DisplaySmall(TypeGroup.Display),
  HeadlineLarge(TypeGroup.Headline),
  HeadlineMedium(TypeGroup.Headline),
  HeadlineSmall(TypeGroup.Headline),
  TitleLarge(TypeGroup.Title),
  TitleMedium(TypeGroup.Title),
  TitleSmall(TypeGroup.Title),
  BodyLarge(TypeGroup.Body),
  BodyMedium(TypeGroup.Body),
  BodySmall(TypeGroup.Body),
  LabelLarge(TypeGroup.Label),
  LabelMedium(TypeGroup.Label),
  LabelSmall(TypeGroup.Label),
  DisplayLargeEmphasized(TypeGroup.Display),
  DisplayMediumEmphasized(TypeGroup.Display),
  DisplaySmallEmphasized(TypeGroup.Display),
  HeadlineLargeEmphasized(TypeGroup.Headline),
  HeadlineMediumEmphasized(TypeGroup.Headline),
  HeadlineSmallEmphasized(TypeGroup.Headline),
  TitleLargeEmphasized(TypeGroup.Title),
  TitleMediumEmphasized(TypeGroup.Title),
  TitleSmallEmphasized(TypeGroup.Title),
  BodyLargeEmphasized(TypeGroup.Body),
  BodyMediumEmphasized(TypeGroup.Body),
  BodySmallEmphasized(TypeGroup.Body),
  LabelLargeEmphasized(TypeGroup.Label),
  LabelMediumEmphasized(TypeGroup.Label),
  LabelSmallEmphasized(TypeGroup.Label);

  val wire: String
    get() = lowerCamelName()

  val kebab: String
    get() = wire.camelToKebab()

  companion object {
    fun fromWire(value: String): TypeRole? = entries.firstOrNull { it.wire == value }

    fun fromKebab(value: String): TypeRole? = entries.firstOrNull { it.kebab == value }
  }
}

/** The Material corner scale (`Shapes` properties plus none and full). */
enum class ShapeToken {
  None,
  ExtraSmall,
  Small,
  Medium,
  Large,
  LargeIncreased,
  ExtraLarge,
  ExtraLargeIncreased,
  ExtraExtraLarge,
  Full;

  val wire: String
    get() = lowerCamelName()

  val kebab: String
    get() = wire.camelToKebab()

  companion object {
    fun fromWire(value: String): ShapeToken? = entries.firstOrNull { it.wire == value }

    fun fromKebab(value: String): ShapeToken? = entries.firstOrNull { it.kebab == value }
  }
}

/** The `androidx.compose.material3.MaterialShapes` polygons (names read from the desktop jar). */
enum class MaterialShape {
  Circle,
  Square,
  Slanted,
  Arch,
  Fan,
  Arrow,
  SemiCircle,
  Oval,
  Pill,
  Triangle,
  Diamond,
  ClamShell,
  Pentagon,
  Gem,
  Sunny,
  VerySunny,
  Cookie4Sided,
  Cookie6Sided,
  Cookie7Sided,
  Cookie9Sided,
  Cookie12Sided,
  Ghostish,
  Clover4Leaf,
  Clover8Leaf,
  Burst,
  SoftBurst,
  Boom,
  SoftBoom,
  Flower,
  Puffy,
  PuffyDiamond,
  PixelCircle,
  PixelTriangle,
  Bun,
  Heart;

  val wire: String
    get() = lowerCamelName()

  companion object {
    fun fromWire(value: String): MaterialShape? = entries.firstOrNull { it.wire == value }
  }
}

/** MaterialKolor `PaletteStyle` variants, in the kebab case DESIGN.md uses. */
enum class PaletteVariant(val wire: String) {
  TonalSpot("tonal-spot"),
  Neutral("neutral"),
  Vibrant("vibrant"),
  Expressive("expressive"),
  Monochrome("monochrome"),
  Fidelity("fidelity"),
  Content("content"),
  Rainbow("rainbow"),
  FruitSalad("fruit-salad");

  companion object {
    fun fromWire(value: String): PaletteVariant? = entries.firstOrNull { it.wire == value }
  }
}
