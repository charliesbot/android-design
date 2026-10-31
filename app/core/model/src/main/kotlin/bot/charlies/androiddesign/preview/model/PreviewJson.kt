package bot.charlies.androiddesign.preview.model

import kotlinx.serialization.json.Json

/**
 * The JSON settings shared by everything that writes model types: defaults and nulls are left out,
 * and output is pretty printed with two spaces.
 */
object PreviewJson {
  val pretty: Json = Json {
    prettyPrint = true
    prettyPrintIndent = "  "
    encodeDefaults = false
    explicitNulls = false
  }

  /** The same settings on one line, for tool results. */
  val compact: Json = Json {
    encodeDefaults = false
    explicitNulls = false
  }
}
