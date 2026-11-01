package bot.charlies.androiddesign.preview.data

import bot.charlies.androiddesign.preview.model.ThemeOverride
import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlConfiguration
import com.charleskorn.kaml.YamlException

sealed interface DesignMdParse {
  data class Parsed(val theme: ThemeOverride) : DesignMdParse

  data class Failed(val message: String) : DesignMdParse
}

/**
 * Reads the YAML front matter of a DESIGN.md. The prose below it is preserved on disk and ignored
 * here. Keys this app does not know are skipped, since the format allows custom ones.
 */
object DesignMdParser {
  private val yaml = Yaml(configuration = YamlConfiguration(strictMode = false))
  private const val FENCE = "---"

  fun parse(text: String): DesignMdParse {
    val lines = text.removePrefix("﻿").lines().map { it.trimEnd() }
    if (lines.firstOrNull() != FENCE) return DesignMdParse.Parsed(ThemeOverride())
    val end = lines.drop(1).indexOfFirst { it == FENCE }
    if (end < 0) return DesignMdParse.Failed("the front matter starts with --- but is never closed")
    val frontMatter = lines.subList(1, end + 1).joinToString("\n")
    if (frontMatter.isBlank()) return DesignMdParse.Parsed(ThemeOverride())
    return try {
      DesignMdParse.Parsed(yaml.decodeFromString(ThemeOverride.serializer(), frontMatter))
    } catch (e: YamlException) {
      // Front matter lines start at line 2 of the file.
      DesignMdParse.Failed("front matter line ${e.line + 1}: ${e.message.lineSequence().first()}")
    } catch (e: IllegalArgumentException) {
      DesignMdParse.Failed("front matter: ${e.message?.lineSequence()?.first()}")
    }
  }
}
