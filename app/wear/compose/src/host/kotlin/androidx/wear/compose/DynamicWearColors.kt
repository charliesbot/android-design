package androidx.wear.compose

import android.R
import android.content.ContentResolver
import android.content.Context
import android.content.res.Resources
import android.os.Build
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.wear.compose.material3.ColorScheme
import androidx.wear.compose.material3.dynamicColorScheme

/**
 * The names of the Android system color resources that Wear's dynamic color scheme reads, which a
 * caller supplies by name to [dynamicWearColorScheme].
 */
val SystemColorNames: List<String> =
  listOf(
    "system_background_dark",
    "system_error_container_dark",
    "system_error_dark",
    "system_on_background_dark",
    "system_on_error_container_dark",
    "system_on_error_dark",
    "system_on_primary_container_dark",
    "system_on_primary_fixed",
    "system_on_secondary_container_dark",
    "system_on_secondary_fixed",
    "system_on_surface_dark",
    "system_on_surface_variant_dark",
    "system_on_tertiary_container_dark",
    "system_on_tertiary_fixed",
    "system_outline_dark",
    "system_outline_variant_dark",
    "system_primary_container_dark",
    "system_primary_fixed",
    "system_primary_fixed_dim",
    "system_secondary_container_dark",
    "system_secondary_fixed",
    "system_secondary_fixed_dim",
    "system_surface_container_dark",
    "system_surface_container_high_dark",
    "system_surface_container_low_dark",
    "system_tertiary_container_dark",
    "system_tertiary_fixed",
    "system_tertiary_fixed_dim",
  )

private val SystemColorIds: Map<String, Int> =
  mapOf(
    "system_background_dark" to R.color.system_background_dark,
    "system_error_container_dark" to R.color.system_error_container_dark,
    "system_error_dark" to R.color.system_error_dark,
    "system_on_background_dark" to R.color.system_on_background_dark,
    "system_on_error_container_dark" to R.color.system_on_error_container_dark,
    "system_on_error_dark" to R.color.system_on_error_dark,
    "system_on_primary_container_dark" to R.color.system_on_primary_container_dark,
    "system_on_primary_fixed" to R.color.system_on_primary_fixed,
    "system_on_secondary_container_dark" to R.color.system_on_secondary_container_dark,
    "system_on_secondary_fixed" to R.color.system_on_secondary_fixed,
    "system_on_surface_dark" to R.color.system_on_surface_dark,
    "system_on_surface_variant_dark" to R.color.system_on_surface_variant_dark,
    "system_on_tertiary_container_dark" to R.color.system_on_tertiary_container_dark,
    "system_on_tertiary_fixed" to R.color.system_on_tertiary_fixed,
    "system_outline_dark" to R.color.system_outline_dark,
    "system_outline_variant_dark" to R.color.system_outline_variant_dark,
    "system_primary_container_dark" to R.color.system_primary_container_dark,
    "system_primary_fixed" to R.color.system_primary_fixed,
    "system_primary_fixed_dim" to R.color.system_primary_fixed_dim,
    "system_secondary_container_dark" to R.color.system_secondary_container_dark,
    "system_secondary_fixed" to R.color.system_secondary_fixed,
    "system_secondary_fixed_dim" to R.color.system_secondary_fixed_dim,
    "system_surface_container_dark" to R.color.system_surface_container_dark,
    "system_surface_container_high_dark" to R.color.system_surface_container_high_dark,
    "system_surface_container_low_dark" to R.color.system_surface_container_low_dark,
    "system_tertiary_container_dark" to R.color.system_tertiary_container_dark,
    "system_tertiary_fixed" to R.color.system_tertiary_fixed,
    "system_tertiary_fixed_dim" to R.color.system_tertiary_fixed_dim,
  )

/**
 * Wear's real dynamic color scheme (`dynamicColorScheme(context)`, the watch face theming) fed with
 * the system colors [systemColor] gives for each of [SystemColorNames], so the mapping from system
 * colors to Wear roles is Google's and not a copy.
 */
fun dynamicWearColorScheme(systemColor: (name: String) -> Color): ColorScheme {
  val resources =
    Resources(SystemColorIds.entries.associate { (name, id) -> id to systemColor(name).toArgb() })
  val resolver = ContentResolver(mapOf("dynamic_color_theme_enabled" to 1))
  val context =
    object : Context() {
      override val resources: Resources = resources
      override val contentResolver: ContentResolver = resolver
    }
  return Build.VERSION.withSdk(Build.VERSION_CODES.VANILLA_ICE_CREAM) {
    checkNotNull(dynamicColorScheme(context)) { "dynamic color is unavailable" }
  }
}
