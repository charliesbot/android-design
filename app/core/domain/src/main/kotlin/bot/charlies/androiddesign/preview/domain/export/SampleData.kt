package bot.charlies.androiddesign.preview.domain.export

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

/** One top-level key of a screen's `dataModel`, as a parameter of the screen function. */
internal class SampleParam(val name: String, val type: String, val sample: Expr)

/** A screen's `dataModel` as Kotlin: its parameters and the object that holds the sample values. */
internal class ScreenData(
  val screenId: String,
  val root: JsonObject?,
  val params: List<SampleParam>,
) {
  /** `PlatesSample`: the `private object` holding one value per parameter. */
  val objectName: String = Naming.pascal(screenId) + "Sample"
}

/** A written file, with what [SampleModel.writeAvoidingClashes] needs to check its names. */
internal class BuiltSource(
  val source: String,
  val importedNames: Set<String>,
  val sampleClassNames: Set<String>,
)

/**
 * Turns each screen's `dataModel` into data classes and sample values. Objects become public data
 * classes named after their key (`plates` gives `Plate`), because the screen functions take them as
 * parameters. Two shapes that want the same name, or a name that is in [reserved] (a simple name
 * the file imports, like `Row`), get the screen name in front. Arrays become lists of their first
 * element's type.
 */
internal class SampleModel(private val reserved: Set<String> = emptySet()) {
  private class FieldInfo(val name: String, val type: String)

  private class ClassInfo(val name: String, val fields: List<FieldInfo>) {
    val signature: String = fields.joinToString(";") { "${it.name}:${it.type}" }
  }

  private class Typed(val type: String, val sample: Expr)

  private val classes = linkedMapOf<String, ClassInfo>()

  companion object {
    /**
     * Writes a file with [build], given the simple names the sample classes must not take. A sample
     * class named like an imported symbol (`Row`) would shadow the import, so the names the first
     * pass imported are kept free and the file is built again.
     */
    fun writeAvoidingClashes(build: (reserved: Set<String>) -> BuiltSource): String {
      val first = build(emptySet())
      val clash = first.sampleClassNames.intersect(first.importedNames)
      return if (clash.isEmpty()) first.source else build(first.importedNames).source
    }
  }

  fun forScreen(screenId: String, dataModel: JsonElement?): ScreenData {
    val root = dataModel as? JsonObject
    val params =
      root?.map { (key, value) ->
        val typed = typed(value, key, screenId)
        SampleParam(Naming.camel(key), typed.type, typed.sample)
      } ?: emptyList()
    return ScreenData(screenId, root, params)
  }

  fun classNames(): Set<String> = classes.keys.toSet()

  fun classDecls(): List<ClassDecl> =
    classes.values.map { info ->
      ClassDecl(
        annotations = emptyList(),
        header = "data class ${info.name}",
        fields = info.fields.map { "val ${it.name}: ${it.type}" },
      )
    }

  private fun typed(value: JsonElement, hint: String, screenId: String): Typed =
    when (value) {
      is JsonObject -> obj(value, hint, screenId)
      is JsonArray -> array(value, hint, screenId)
      is JsonNull -> Typed("String", Raw("\"\""))
      is JsonPrimitive -> primitive(value)
    }

  private fun obj(value: JsonObject, hint: String, screenId: String): Typed {
    val fields = value.map { (key, child) ->
      Triple(key, Naming.camel(key), typed(child, key, screenId))
    }
    val info =
      register(hint, screenId, fields.map { (_, name, typed) -> FieldInfo(name, typed.type) })
    val sample = Call(info.name, fields.map { (_, name, typed) -> arg(name, typed.sample) })
    return Typed(info.name, sample)
  }

  private fun register(hint: String, screenId: String, fields: List<FieldInfo>): ClassInfo {
    val base = Naming.pascal(Naming.singular(hint))
    val prefixed = Naming.pascal(screenId) + base
    val candidates =
      sequenceOf(base, prefixed) + generateSequence(2) { it + 1 }.map { "$prefixed$it" }
    for (name in candidates.filter { it !in reserved }) {
      val existing = classes[name]
      if (existing == null) {
        return ClassInfo(name, fields).also { classes[name] = it }
      }
      if (existing.signature == ClassInfo(name, fields).signature) return existing
    }
    error("unreachable")
  }

  private fun array(value: JsonArray, hint: String, screenId: String): Typed {
    if (value.isEmpty()) return Typed("List<String>", Raw("emptyList()"))
    val items = value.map { typed(it, hint, screenId) }
    val numbers = value.all {
      it is JsonPrimitive && !it.isString && it.content.toDoubleOrNull() != null
    }
    val mixed = numbers && value.any { (it as JsonPrimitive).content.toIntOrNull() == null }
    val elementType = if (mixed) "Double" else items.first().type
    val samples =
      if (mixed) value.map { Raw((it as JsonPrimitive).content.toDouble().toString()) }
      else items.map { it.sample }
    return Typed("List<$elementType>", Call("listOf", samples.map { pos(it) }))
  }

  private fun primitive(value: JsonPrimitive): Typed =
    when {
      value.isString -> Typed("String", Raw(kotlinString(value.content)))
      value.content == "true" || value.content == "false" -> Typed("Boolean", Raw(value.content))
      value.content.toIntOrNull() != null -> Typed("Int", Raw(value.content))
      value.content.toLongOrNull() != null -> Typed("Long", Raw("${value.content}L"))
      else -> Typed("Double", Raw(value.content.toDouble().toString()))
    }
}
