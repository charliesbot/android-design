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
 * The typefaces the app bundles, in schema order. [family] is the name a DESIGN.md uses, [groups]
 * the type groups the face may set and [roles] the single roles it may set beyond them. Google Sans
 * Flex and Google Sans Code serve every role, brand faces take display, headline and the large
 * title roles (a small top app bar or side sheet title draws in title large), and Doto is for
 * display sizes only. [scope] names what the face may set, for messages.
 */
enum class BundledFont(
  val family: String,
  private val groups: Set<TypeGroup>,
  private val roles: Set<TypeRole>,
  val scope: String,
) {
  GoogleSansFlex("Google Sans Flex", TypeGroup.entries.toSet(), emptySet(), "any"),
  GoogleSansCode("Google Sans Code", TypeGroup.entries.toSet(), emptySet(), "any"),
  Fraunces("Fraunces", BRAND_GROUPS, BRAND_ROLES, BRAND_SCOPE),
  BricolageGrotesque("Bricolage Grotesque", BRAND_GROUPS, BRAND_ROLES, BRAND_SCOPE),
  BigShouldersDisplay("Big Shoulders Display", BRAND_GROUPS, BRAND_ROLES, BRAND_SCOPE),
  Doto("Doto", setOf(TypeGroup.Display), emptySet(), "display");

  /** Whether the face may set text that has no type role, which only a face for every role may. */
  val setsAnyText: Boolean
    get() = groups.size == TypeGroup.entries.size

  /**
   * Whether the face may set a role of [group]. [role] is the Material role itself when the
   * catalog's roles are `TypeRole`s, which lets a face take a role its group does not; it is null
   * for a role only another catalog has.
   */
  fun allows(group: TypeGroup, role: TypeRole? = null): Boolean =
    group in groups || (role != null && role in roles)

  companion object {
    /** The face named [family], matching the exact case unless [ignoreCase]. */
    fun byFamily(family: String, ignoreCase: Boolean = false): BundledFont? = entries.firstOrNull {
      it.family.equals(family, ignoreCase)
    }
  }
}

private val BRAND_GROUPS = setOf(TypeGroup.Display, TypeGroup.Headline)
private val BRAND_ROLES = setOf(TypeRole.TitleLarge, TypeRole.TitleLargeEmphasized)
private const val BRAND_SCOPE = "display, headline and large title"

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
