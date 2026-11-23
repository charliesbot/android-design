package bot.charlies.androiddesign.preview.app.cli

import bot.charlies.androiddesign.preview.data.DesignMdLocator
import bot.charlies.androiddesign.preview.data.DesignMdParse
import bot.charlies.androiddesign.preview.data.DesignMdParser
import bot.charlies.androiddesign.preview.model.ThemeOverride
import bot.charlies.androiddesign.preview.model.ValidationError
import java.nio.file.Files
import java.nio.file.Path

/** Loads and validates an optional paired `DESIGN.md` for an `.explore.kt` source file. */
internal object DesignMdLoader {
  data class Result(
    val path: Path?,
    val theme: ThemeOverride?,
    val errors: List<ValidationError>,
  )

  fun load(sourcePath: Path, explicitDesign: Path?): Result {
    val designPath =
      if (explicitDesign != null) {
        val norm = explicitDesign.toAbsolutePath().normalize()
        if (!Files.isRegularFile(norm)) {
          return Result(norm, null, listOf(ValidationError(norm.toString(), "no such file")))
        }
        norm
      } else {
        DesignMdLocator.find(sourcePath)
      }
    if (designPath == null) return Result(null, null, emptyList())
    val text =
      try {
        Files.readString(designPath)
      } catch (e: Exception) {
        return Result(
          designPath,
          null,
          listOf(ValidationError(designPath.toString(), e.message ?: "cannot read DESIGN.md")),
        )
      }
    return when (val parsed = DesignMdParser.parse(text)) {
      is DesignMdParse.Parsed -> Result(designPath, parsed.theme, emptyList())
      is DesignMdParse.Failed ->
        Result(designPath, null, listOf(ValidationError(designPath.toString(), parsed.message)))
    }
  }
}
