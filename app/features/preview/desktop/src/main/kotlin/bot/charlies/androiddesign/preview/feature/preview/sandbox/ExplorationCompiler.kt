package bot.charlies.androiddesign.preview.feature.preview.sandbox

import bot.charlies.androiddesign.preview.kit.BundledFont
import bot.charlies.androiddesign.preview.kit.ExplorationBoard
import bot.charlies.androiddesign.preview.model.PaletteVariant
import bot.charlies.androiddesign.preview.model.ValidationError
import java.io.File
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Modifier
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import kotlinx.coroutines.CoroutineScope
import org.jetbrains.kotlin.cli.common.ExitCode as KotlinExitCode
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSourceLocation
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.cli.jvm.K2JVMCompiler
import org.jetbrains.kotlin.config.Services

sealed interface CompileResult {
  data class Compiled(
    val board: ExplorationBoard,
    val sourceFile: String,
    val sourceText: String,
    val compileMs: Long,
    val explorationDir: Path,
  ) : CompileResult

  data class Failed(val errors: List<ValidationError>, val compileMs: Long) : CompileResult
}

/**
 * Compiles `.explore.kt` files in memory behind [SourceTokenLinter], [BytecodeGuard], and
 * [SandboxClassLoader].
 */
object ExplorationCompiler {
  private const val EMBEDDED_COMPOSE_PLUGIN =
    "META-INF/embedded/kotlin-compose-compiler-plugin-embeddable.jar"
  private const val NANOS_PER_MS = 1_000_000L

  private val composePluginJar: Path by lazy { locateComposePluginJar() }
  private val compilerClasspath: String by lazy { buildCompilerClasspath() }

  fun compile(sourcePath: Path): CompileResult {
    val normalized = sourcePath.toAbsolutePath().normalize()
    if (!Files.isRegularFile(normalized)) {
      return CompileResult.Failed(
        listOf(ValidationError(normalized.toString(), "No such exploration file: $normalized")),
        compileMs = 0L,
      )
    }
    val sourceText =
      try {
        Files.readString(normalized)
      } catch (e: java.nio.charset.CharacterCodingException) {
        return CompileResult.Failed(
          listOf(ValidationError(normalized.toString(), "File is not valid UTF-8: ${e.message}")),
          compileMs = 0L,
        )
      }
    return compileSource(
      fileName = normalized.fileName.toString(),
      sourceText = sourceText,
      explorationDir = normalized.parent,
    )
  }

  fun compileSource(
    fileName: String,
    sourceText: String,
    explorationDir: Path,
  ): CompileResult {
    val started = System.nanoTime()
    fun elapsedMs(): Long = (System.nanoTime() - started) / NANOS_PER_MS

    val lintErrors = SourceTokenLinter.lint(fileName, sourceText)
    if (lintErrors.isNotEmpty()) {
      return CompileResult.Failed(lintErrors, elapsedMs())
    }

    val workDir = Files.createTempDirectory("explore-compile-")
    return try {
      val srcDir = workDir.resolve("src").also { Files.createDirectories(it) }
      val classesDir = workDir.resolve("classes").also { Files.createDirectories(it) }
      val kotlinName = if (fileName.endsWith(".kt")) fileName else "$fileName.kt"
      val tempSource = srcDir.resolve(kotlinName)
      Files.writeString(tempSource, sourceText)

      val collector = DiagnosticCollector(tempSource, fileName)
      val args = buildCompilerArgs(tempSource, classesDir)
      val exit = K2JVMCompiler().exec(collector, Services.EMPTY, args)
      if (exit != KotlinExitCode.OK || collector.errors.isNotEmpty()) {
        val errors =
          collector.errors.ifEmpty {
            listOf(ValidationError(fileName, "Kotlin compilation failed ($exit)"))
          }
        return CompileResult.Failed(errors, elapsedMs())
      }

      val classBytes = readClassFiles(classesDir)
      if (classBytes.isEmpty()) {
        return CompileResult.Failed(
          listOf(ValidationError(fileName, "Exploration produced no compiled classes")),
          elapsedMs(),
        )
      }

      val securityErrors = verifyAllBytecode(fileName, classBytes)
      if (securityErrors.isNotEmpty()) {
        return CompileResult.Failed(securityErrors, elapsedMs())
      }

      loadAndExtractBoard(fileName, sourceText, explorationDir, classBytes, ::elapsedMs)
    } finally {
      workDir.toFile().deleteRecursively()
    }
  }

  private fun buildCompilerArgs(source: Path, classesDir: Path) =
    K2JVMCompiler().createArguments().apply {
      val cli =
        arrayOf(
          source.toString(),
          "-d",
          classesDir.toString(),
          "-classpath",
          compilerClasspath,
          "-jvm-target",
          "17",
          "-no-stdlib",
          "-no-reflect",
          "-opt-in=kotlin.RequiresOptIn",
          "-opt-in=androidx.compose.material3.ExperimentalMaterial3Api",
          "-opt-in=androidx.compose.material3.ExperimentalMaterial3ExpressiveApi",
          "-opt-in=androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi",
          "-opt-in=androidx.wear.compose.material3.ExperimentalWearMaterial3Api",
          "-Xplugin=$composePluginJar",
          "-P",
          "plugin:androidx.compose.compiler.plugins.kotlin:sourceInformation=true",
        )
      K2JVMCompiler().parseArguments(cli, this)
    }

  private fun readClassFiles(classesDir: Path): Map<String, ByteArray> {
    val result = linkedMapOf<String, ByteArray>()
    Files.walk(classesDir).use { paths ->
      paths
        .filter { Files.isRegularFile(it) && it.fileName.toString().endsWith(".class") }
        .sorted()
        .forEach { file ->
          val relative = classesDir.relativize(file).toString().removeSuffix(".class")
          val className = relative.replace(File.separatorChar, '.')
          result[className] = Files.readAllBytes(file)
        }
    }
    return result
  }

  private fun verifyAllBytecode(
    fileName: String,
    classBytes: Map<String, ByteArray>,
  ): List<ValidationError> {
    val siblings = classBytes.keys
    return classBytes.entries.flatMap { (name, bytes) ->
      BytecodeGuard.verify(name, bytes, siblings).map { msg -> ValidationError(fileName, msg) }
    }
  }

  private fun loadAndExtractBoard(
    fileName: String,
    sourceText: String,
    explorationDir: Path,
    classBytes: Map<String, ByteArray>,
    elapsedMs: () -> Long,
  ): CompileResult {
    val loader = SandboxClassLoader(classBytes, ExplorationCompiler::class.java.classLoader)
    val candidates = mutableListOf<java.lang.reflect.Method>()
    for (className in classBytes.keys) {
      val loaded =
        try {
          loader.loadClass(className)
        } catch (t: Throwable) {
          return CompileResult.Failed(
            listOf(ValidationError(fileName, t.message ?: t::class.simpleName.orEmpty())),
            elapsedMs(),
          )
        }
      for (method in loaded.declaredMethods) {
        if (
          Modifier.isStatic(method.modifiers) &&
            method.parameterCount == 0 &&
            ExplorationBoard::class.java.isAssignableFrom(method.returnType)
        ) {
          candidates += method
        }
      }
    }
    val getter =
      candidates.firstOrNull { it.name == "getPreview" }
        ?: candidates.firstOrNull()
        ?: return CompileResult.Failed(
          listOf(
            ValidationError(
              fileName,
              "No top-level `val preview = designBoard(...) { ... }` or wearBoard found",
            ),
          ),
          elapsedMs(),
        )

    return try {
      val board = getter.invoke(null) as ExplorationBoard
      CompileResult.Compiled(board, fileName, sourceText, elapsedMs(), explorationDir)
    } catch (e: InvocationTargetException) {
      val cause = e.cause ?: e
      CompileResult.Failed(
        listOf(ValidationError(fileName, cause.message ?: cause::class.simpleName.orEmpty())),
        elapsedMs(),
      )
    } catch (t: Throwable) {
      CompileResult.Failed(
        listOf(ValidationError(fileName, t.message ?: t::class.simpleName.orEmpty())),
        elapsedMs(),
      )
    }
  }

  private class DiagnosticCollector(
    private val tempSource: Path,
    private val displayName: String,
  ) : MessageCollector {
    val errors = mutableListOf<ValidationError>()

    override fun clear() {
      errors.clear()
    }

    override fun hasErrors(): Boolean = errors.isNotEmpty()

    override fun report(
      severity: CompilerMessageSeverity,
      message: String,
      location: CompilerMessageSourceLocation?,
    ) {
      if (!severity.isError) return
      val where =
        if (location == null) {
          displayName
        } else {
          val file =
            if (location.path == tempSource.toString()) displayName
            else Path.of(location.path).fileName.toString()
          "$file:${location.line}:${location.column}"
        }
      errors += ValidationError(where, message)
    }
  }

  private fun buildCompilerClasspath(): String {
    val entries = LinkedHashSet<String>()
    System.getProperty("java.class.path")
      .orEmpty()
      .split(File.pathSeparator)
      .filter { it.isNotBlank() }
      .map { Path.of(it).toAbsolutePath().normalize() }
      .filter { Files.exists(it) }
      .forEach { entries += it.toString() }

    val markers =
      listOf(
        ExplorationBoard::class.java,
        BundledFont::class.java,
        PaletteVariant::class.java,
        CoroutineScope::class.java,
        Unit::class.java,
      )
    for (marker in markers) {
      val location = marker.protectionDomain?.codeSource?.location ?: continue
      try {
        val path = Path.of(location.toURI()).toAbsolutePath().normalize()
        if (Files.exists(path)) entries += path.toString()
      } catch (_: Exception) {}
    }
    return entries.joinToString(File.pathSeparator)
  }

  private fun locateComposePluginJar(): Path {
    System.getProperty("java.class.path")
      .orEmpty()
      .split(File.pathSeparator)
      .filter { it.contains("kotlin-compose-compiler-plugin") && it.endsWith(".jar") }
      .map { Path.of(it).toAbsolutePath().normalize() }
      .firstOrNull { Files.isRegularFile(it) }
      ?.let {
        return it
      }

    ExplorationCompiler::class.java.classLoader.getResourceAsStream(EMBEDDED_COMPOSE_PLUGIN)?.use {
      input ->
      val dir = Files.createTempDirectory("adp-compose-plugin-")
      dir.toFile().deleteOnExit()
      val target = dir.resolve("kotlin-compose-compiler-plugin-embeddable.jar")
      target.toFile().deleteOnExit()
      Files.copy(input, target, StandardCopyOption.REPLACE_EXISTING)
      return target
    }

    val registrar = runCatching {
      Class.forName("androidx.compose.compiler.plugins.kotlin.ComposePluginRegistrar")
    }
      .getOrNull()
    val codeSource = registrar?.protectionDomain?.codeSource?.location
    if (codeSource != null) {
      val candidate = Path.of(codeSource.toURI()).toAbsolutePath().normalize()
      if (Files.isRegularFile(candidate)) return candidate
    }
    error("Cannot locate kotlin-compose-compiler-plugin-embeddable jar on classpath or resources")
  }
}
