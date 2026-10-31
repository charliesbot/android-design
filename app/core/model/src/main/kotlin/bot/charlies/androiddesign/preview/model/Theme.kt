package bot.charlies.androiddesign.preview.model

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonPrimitive

/**
 * Reads a scalar as text whether the file wrote it quoted or not, so `16`, `"16"` and `16px` all
 * arrive as strings. YAML (kaml) and JSON both go through this.
 */
object ScalarStringSerializer : KSerializer<String> {
  override val descriptor: SerialDescriptor =
    PrimitiveSerialDescriptor("ScalarString", PrimitiveKind.STRING)

  override fun deserialize(decoder: Decoder): String =
    if (decoder is JsonDecoder) {
      (decoder.decodeJsonElement() as? JsonPrimitive)?.content
        ?: throw kotlinx.serialization.SerializationException("Expected a scalar value")
    } else {
      decoder.decodeString()
    }

  override fun serialize(encoder: Encoder, value: String) = encoder.encodeString(value)
}

/**
 * A theme layer as written in DESIGN.md front matter or in a proposal's `theme`: raw, optional
 * values that are validated and merged later. Both files use the same keys.
 */
@Serializable
data class ThemeOverride(
  val version: String? = null,
  val name: String? = null,
  /** Material role in kebab case to `#RRGGBB`. `primary` is the seed. */
  val colors: Map<String, @Serializable(ScalarStringSerializer::class) String> = emptyMap(),
  /** Type role in kebab case to its overrides. */
  val typography: Map<String, TypographyOverride> = emptyMap(),
  /** Corner name in kebab case to a dimension such as `28px` (read as dp). */
  val rounded: Map<String, @Serializable(ScalarStringSerializer::class) String> = emptyMap(),
  /** Spacing name to a dimension such as `16px` (read as dp). */
  val spacing: Map<String, @Serializable(ScalarStringSerializer::class) String> = emptyMap(),
  val material: MaterialOverride? = null,
)

@Serializable
data class TypographyOverride(
  val fontFamily: String? = null,
  /** A CSS-like axis list such as `'opsz' 72, 'SOFT' 50`. */
  val fontVariation: String? = null,
  val fontWeight: Int? = null,
  @Serializable(ScalarStringSerializer::class) val letterSpacing: String? = null,
)

@Serializable
data class MaterialOverride(
  val scheme: SchemeOverride? = null,
  val styles: Map<String, StyleDef> = emptyMap(),
)

@Serializable
data class SchemeOverride(
  /** A `#RRGGBB` color or a reference such as `{colors.primary}`. */
  val seed: String? = null,
  /** A [PaletteVariant] wire name. */
  val variant: String? = null,
  /** Contrast level from -1.0 to 1.0. */
  val contrast: Double? = null,
)

/** A named style. Every value is a token name, never a raw value. */
@Serializable
data class StyleDef(
  /** A [TypeRole] wire name. */
  val type: String? = null,
  /** A [ColorRole] wire name for the content color. */
  val color: String? = null,
  /** A [ColorRole] wire name for the container color. */
  val container: String? = null,
  /** A [ShapeToken] or [MaterialShape] wire name. */
  val shape: String? = null,
  /** A spacing name. */
  val padding: String? = null,
)

/** The resolved design system: DESIGN.md merged with a proposal's `theme`, with defaults filled. */
data class DesignSystem(
  val name: String?,
  /** `#RRGGBB`. */
  val seed: String,
  val variant: PaletteVariant,
  val contrast: Double,
  /** Explicit role colors (`#RRGGBB`) that replace generated ones. The seed is not listed. */
  val colorOverrides: Map<ColorRole, String>,
  val typography: Map<TypeRole, TypographyOverride>,
  /** Corner scale in dp. */
  val rounded: Map<ShapeToken, Float>,
  /** Spacing scale in dp. */
  val spacing: Map<String, Float>,
  val styles: Map<String, StyleDef>,
)
