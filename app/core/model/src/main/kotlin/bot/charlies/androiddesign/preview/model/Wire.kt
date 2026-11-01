package bot.charlies.androiddesign.preview.model

/** Converts `surfaceContainerHigh` to `surface-container-high`, the form DESIGN.md keys use. */
fun String.camelToKebab(): String = buildString {
  for (c in this@camelToKebab) {
    if (c.isUpperCase()) {
      append('-')
      append(c.lowercaseChar())
    } else {
      append(c)
    }
  }
}

/** Lower camel case of an enum entry name, which is the name the Compose API uses. */
internal fun Enum<*>.lowerCamelName(): String = name.replaceFirstChar { it.lowercaseChar() }
