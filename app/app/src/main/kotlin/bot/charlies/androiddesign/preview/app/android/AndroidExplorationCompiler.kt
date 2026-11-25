package bot.charlies.androiddesign.preview.app.android

import bot.charlies.androiddesign.preview.app.cli.DesignMdLoader
import bot.charlies.androiddesign.preview.app.server.ContentHasher
import bot.charlies.androiddesign.preview.domain.bootstrap.BootstrapMeta
import bot.charlies.androiddesign.preview.domain.sandbox.BytecodeGuard
import bot.charlies.androiddesign.preview.domain.sandbox.SourceTokenLinter
import bot.charlies.androiddesign.preview.model.ThemeOverride
import bot.charlies.androiddesign.preview.model.ValidationError
import java.io.File
import java.lang.reflect.Proxy
import java.net.URLClassLoader
import java.nio.ByteBuffer
import java.nio.charset.CharacterCodingException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.concurrent.ConcurrentHashMap
import org.jetbrains.kotlin.cli.common.ExitCode as KotlinExitCode
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSourceLocation
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.cli.jvm.K2JVMCompiler
import org.jetbrains.kotlin.config.Services

data class CompiledAndroidExploration(
  val sourcePath: Path,
  val sourceFile: String,
  val sourceText: String,
  val designPath: Path?,
  val designTheme: ThemeOverride?,
  val entryClass: String,
  val classBytes: Map<String, ByteArray>,
  val dexBytes: ByteArray,
  val dexHash: String,
  val assetMap: Map<String, String>,
  val assetBlobs: Map<String, ByteArray>,
  val compileMs: Long,
)

sealed interface AndroidCompileOutcome {
  data class Success(val compiled: CompiledAndroidExploration) : AndroidCompileOutcome

  data class Failed(
    val errors: List<ValidationError>,
    val compileMs: Long,
    val designPath: Path? = null,
    val designTheme: ThemeOverride? = null,
  ) : AndroidCompileOutcome
}

/**
 * In-process Kotlin + Compose + R8 D8 compiler targeting Android (`minApi = 34`) behind
 * [SourceTokenLinter] and [BytecodeGuard].
 */
object AndroidExplorationCompiler {
  private const val NANOS_PER_MS = 1_000_000L
  private const val MIN_API_LEVEL = 34
  private const val MAX_ASSET_WALK_DEPTH = 4
  private const val MAX_ASSET_FILE_BYTES = 16L * 1024L * 1024L

  private val DESIGN_PAINTER_CALL = Regex("""\bdesignPainter\s*\(\s*"([^"]*)"\s*\)""")

  private data class D8Key(val androidJar: Path, val bundleJar: Path, val d8Jar: Path)

  private val compiler = K2JVMCompiler()
  private val compilerLock = Any()
  private val d8Sessions = ConcurrentHashMap<D8Key, D8Session>()

  fun compile(
    sourcePath: Path,
    explicitDesign: Path?,
    meta: BootstrapMeta,
  ): AndroidCompileOutcome {
    val started = System.nanoTime()
    fun elapsedMs(): Long = (System.nanoTime() - started) / NANOS_PER_MS

    val stage =
      when (val outcome = compileClassStage(sourcePath, explicitDesign, meta)) {
        is ClassStageOutcome.Failed ->
          return AndroidCompileOutcome.Failed(
            errors = outcome.errors,
            compileMs = elapsedMs(),
            designPath = outcome.designPath,
            designTheme = outcome.designTheme,
          )
        is ClassStageOutcome.Ready -> outcome.stage
      }
    val dexBytes =
      when (val lowered = lowerToDex(stage.sourceFile, stage.classBytes, meta)) {
        is DexLowering.Failed ->
          return AndroidCompileOutcome.Failed(
            errors = lowered.errors,
            compileMs = elapsedMs(),
            designPath = stage.designPath,
            designTheme = stage.designTheme,
          )
        is DexLowering.Ready -> lowered.dexBytes
      }
    return AndroidCompileOutcome.Success(
      CompiledAndroidExploration(
        sourcePath = stage.sourcePath,
        sourceFile = stage.sourceFile,
        sourceText = stage.sourceText,
        designPath = stage.designPath,
        designTheme = stage.designTheme,
        entryClass = stage.entryClass,
        classBytes = stage.classBytes,
        dexBytes = dexBytes,
        dexHash = ContentHasher.sha256Hex(dexBytes),
        assetMap = stage.assetMap,
        assetBlobs = stage.assetBlobs,
        compileMs = elapsedMs(),
      ),
    )
  }

  /** A compiled exploration before DEX lowering. */
  data class ClassStage(
    val sourcePath: Path,
    val sourceFile: String,
    val sourceText: String,
    val designPath: Path?,
    val designTheme: ThemeOverride?,
    val entryClass: String,
    val classBytes: Map<String, ByteArray>,
    val assetMap: Map<String, String>,
    val assetBlobs: Map<String, ByteArray>,
    val compileMs: Long,
  )

  /**
   * Everything [compile] does except D8: design load, asset collection with its confinement errors,
   * then lint, kotlinc with the Compose plugin, and [BytecodeGuard].
   */
  fun compileClassStage(
    sourcePath: Path,
    explicitDesign: Path?,
    meta: BootstrapMeta,
  ): ClassStageOutcome {
    val started = System.nanoTime()
    fun elapsedMs(): Long = (System.nanoTime() - started) / NANOS_PER_MS

    val normalized = sourcePath.toAbsolutePath().normalize()
    if (!Files.isRegularFile(normalized)) {
      return ClassStageOutcome.Failed(
        errors = listOf(ValidationError(normalized.toString(), "No such file: $normalized")),
        compileMs = elapsedMs(),
      )
    }
    val design = DesignMdLoader.load(normalized, explicitDesign)
    if (design.errors.isNotEmpty()) {
      return ClassStageOutcome.Failed(design.errors, elapsedMs(), design.path, design.theme)
    }
    val sourceText =
      try {
        Files.readString(normalized)
      } catch (e: CharacterCodingException) {
        return ClassStageOutcome.Failed(
          listOf(ValidationError(normalized.toString(), "File is not valid UTF-8: ${e.message}")),
          elapsedMs(),
          design.path,
          design.theme,
        )
      }
    val fileName = normalized.fileName.toString()
    val (assetMap, assetBlobs, assetErrors) = collectAssets(normalized.parent, fileName, sourceText)
    if (assetErrors.isNotEmpty()) {
      return ClassStageOutcome.Failed(assetErrors, elapsedMs(), design.path, design.theme)
    }
    return when (val classes = compileSourceToClasses(fileName, sourceText, meta)) {
      is ClassesOutcome.Failed ->
        ClassStageOutcome.Failed(classes.errors, elapsedMs(), design.path, design.theme)
      is ClassesOutcome.Ready ->
        ClassStageOutcome.Ready(
          ClassStage(
            sourcePath = normalized,
            sourceFile = fileName,
            sourceText = sourceText,
            designPath = design.path,
            designTheme = design.theme,
            entryClass = classes.entryClass,
            classBytes = classes.classBytes,
            assetMap = assetMap,
            assetBlobs = assetBlobs,
            compileMs = elapsedMs(),
          ),
        )
    }
  }

  sealed interface ClassStageOutcome {
    data class Ready(val stage: ClassStage) : ClassStageOutcome

    data class Failed(
      val errors: List<ValidationError>,
      val compileMs: Long,
      val designPath: Path? = null,
      val designTheme: ThemeOverride? = null,
    ) : ClassStageOutcome
  }

  sealed interface ClassesOutcome {
    data class Ready(
      val entryClass: String,
      val classBytes: Map<String, ByteArray>,
      val compileMs: Long,
    ) : ClassesOutcome

    data class Failed(
      val errors: List<ValidationError>,
      val compileMs: Long,
    ) : ClassesOutcome
  }

  sealed interface DexCompileOutcome {
    data class Ready(
      val entryClass: String,
      val classBytes: Map<String, ByteArray>,
      val dexBytes: ByteArray,
      val dexHash: String,
      val compileMs: Long,
    ) : DexCompileOutcome

    data class Failed(
      val errors: List<ValidationError>,
      val compileMs: Long,
    ) : DexCompileOutcome
  }

  /** Lint, kotlinc with the Compose plugin, and [BytecodeGuard]; stops before D8. */
  fun compileSourceToClasses(
    fileName: String,
    sourceText: String,
    meta: BootstrapMeta,
    skipGuards: Boolean = false,
  ): ClassesOutcome {
    val started = System.nanoTime()
    fun elapsedMs(): Long = (System.nanoTime() - started) / NANOS_PER_MS

    if (!skipGuards) {
      val lintErrors = SourceTokenLinter.lint(fileName, sourceText)
      if (lintErrors.isNotEmpty()) {
        return ClassesOutcome.Failed(lintErrors, elapsedMs())
      }
    }

    val workDir = Files.createTempDirectory("adp-android-compile-")
    return try {
      val srcDir = workDir.resolve("src").also { Files.createDirectories(it) }
      val classesDir = workDir.resolve("classes").also { Files.createDirectories(it) }
      val kotlinName = if (fileName.endsWith(".kt")) fileName else "$fileName.kt"
      val tempSource = srcDir.resolve(kotlinName)
      Files.writeString(tempSource, sourceText)

      val collector = DiagnosticCollector(tempSource, fileName)
      val exit =
        synchronized(compilerLock) {
          val args = buildCompilerArgs(tempSource, classesDir, meta)
          compiler.exec(collector, Services.EMPTY, args)
        }
      if (exit != KotlinExitCode.OK || collector.errors.isNotEmpty()) {
        val errors =
          collector.errors.ifEmpty {
            listOf(ValidationError(fileName, "Kotlin compilation failed ($exit)"))
          }
        return ClassesOutcome.Failed(errors, elapsedMs())
      }

      val classBytes = readClassFiles(classesDir)
      if (classBytes.isEmpty()) {
        return ClassesOutcome.Failed(
          listOf(ValidationError(fileName, "Exploration produced no compiled classes")),
          elapsedMs(),
        )
      }

      if (!skipGuards) {
        val siblings = classBytes.keys
        val secErrors =
          classBytes.entries.flatMap { (clsName, bytes) ->
            BytecodeGuard.verify(clsName, bytes, siblings).map { msg ->
              ValidationError(fileName, msg)
            }
          }
        if (secErrors.isNotEmpty()) {
          return ClassesOutcome.Failed(secErrors, elapsedMs())
        }
      }

      val entryClass =
        classBytes.keys.firstOrNull { it.endsWith("Kt") && !it.contains('$') }
          ?: classBytes.keys.first()
      ClassesOutcome.Ready(entryClass, classBytes, elapsedMs())
    } finally {
      workDir.toFile().deleteRecursively()
    }
  }

  fun compileSourceToDex(
    fileName: String,
    sourceText: String,
    meta: BootstrapMeta,
    skipGuards: Boolean = false,
  ): DexCompileOutcome {
    val started = System.nanoTime()
    fun elapsedMs(): Long = (System.nanoTime() - started) / NANOS_PER_MS

    val classes =
      when (val outcome = compileSourceToClasses(fileName, sourceText, meta, skipGuards)) {
        is ClassesOutcome.Failed -> return DexCompileOutcome.Failed(outcome.errors, elapsedMs())
        is ClassesOutcome.Ready -> outcome
      }
    val dexBytes =
      when (val lowered = lowerToDex(fileName, classes.classBytes, meta)) {
        is DexLowering.Failed -> return DexCompileOutcome.Failed(lowered.errors, elapsedMs())
        is DexLowering.Ready -> lowered.dexBytes
      }
    return DexCompileOutcome.Ready(
      entryClass = classes.entryClass,
      classBytes = classes.classBytes,
      dexBytes = dexBytes,
      dexHash = ContentHasher.sha256Hex(dexBytes),
      compileMs = elapsedMs(),
    )
  }

  private sealed interface DexLowering {
    data class Ready(val dexBytes: ByteArray) : DexLowering

    data class Failed(val errors: List<ValidationError>) : DexLowering
  }

  private fun lowerToDex(
    fileName: String,
    classBytes: Map<String, ByteArray>,
    meta: BootstrapMeta,
  ): DexLowering {
    val d8Session =
      d8Sessions.computeIfAbsent(D8Key(meta.androidJar, meta.bundleJar, meta.d8Jar)) {
        D8Session(it.androidJar, it.bundleJar, it.d8Jar)
      }
    return try {
      DexLowering.Ready(d8Session.compileToDex(classBytes))
    } catch (t: Throwable) {
      DexLowering.Failed(listOf(ValidationError(fileName, t.message ?: "D8 DEX lowering failed")))
    }
  }

  private data class AssetCollection(
    val assetMap: Map<String, String>,
    val assetBlobs: Map<String, ByteArray>,
    val errors: List<ValidationError>,
  )

  private fun collectAssets(
    baseDir: Path?,
    fileName: String,
    sourceText: String,
  ): AssetCollection {
    if (baseDir == null || !Files.isDirectory(baseDir)) {
      return AssetCollection(emptyMap(), emptyMap(), emptyList())
    }
    val baseReal = runCatching { baseDir.toRealPath() }.getOrNull() ?: baseDir
    val assetMap = linkedMapOf<String, String>()
    val assetBlobs = linkedMapOf<String, ByteArray>()
    val errors = mutableListOf<ValidationError>()

    fun recordAsset(relKey: String, file: Path) {
      if (!Files.isRegularFile(file) || Files.isSymbolicLink(file)) return
      val fileReal = runCatching { file.toRealPath() }.getOrNull() ?: return
      if (!fileReal.startsWith(baseReal)) return
      val size = runCatching { Files.size(fileReal) }.getOrDefault(-1L)
      if (size !in 0..MAX_ASSET_FILE_BYTES) return
      val bytes = Files.readAllBytes(fileReal)
      val sha = ContentHasher.sha256Hex(bytes)
      assetMap[relKey] = sha
      assetBlobs.putIfAbsent(sha, bytes)
    }

    val assetsDir = baseDir.resolve("assets")
    if (Files.isDirectory(assetsDir) && !Files.isSymbolicLink(assetsDir)) {
      val assetsReal = runCatching { assetsDir.toRealPath() }.getOrNull()
      if (assetsReal != null && assetsReal.startsWith(baseReal)) {
        Files.walk(assetsDir, MAX_ASSET_WALK_DEPTH).use { stream ->
          stream
            .filter { Files.isRegularFile(it) && !Files.isSymbolicLink(it) }
            .sorted()
            .forEach { file ->
              val rel = baseDir.relativize(file).toString().replace('\\', '/')
              recordAsset(rel, file)
            }
        }
      }
    }

    val sanitized = blankCommentsExceptStrings(sourceText)
    for (match in DESIGN_PAINTER_CALL.findAll(sanitized)) {
      val rawRel = match.groupValues[1].trim()
      val normalizedRel = validateRelativeAssetPath(rawRel)
      if (normalizedRel == null) {
        errors +=
          ValidationError(
            path = fileName,
            message = "Asset path cannot escape the exploration folder: $rawRel",
          )
        continue
      }
      if (normalizedRel in assetMap) continue
      val resolved = baseDir.resolve(normalizedRel).normalize()
      if (!resolved.startsWith(baseDir.normalize())) {
        errors +=
          ValidationError(
            path = fileName,
            message = "Asset path cannot escape the exploration folder: $rawRel",
          )
        continue
      }
      recordAsset(normalizedRel, resolved)
    }

    return AssetCollection(assetMap, assetBlobs, errors)
  }

  internal fun validateRelativeAssetPath(raw: String): String? {
    val trimmed = raw.trim()
    if (
      trimmed.isEmpty() ||
        trimmed.startsWith("/") ||
        trimmed.contains('\\') ||
        trimmed.contains(':')
    ) {
      return null
    }
    val rel = runCatching { Paths.get(trimmed) }.getOrNull() ?: return null
    if (rel.isAbsolute || rel.any { it.toString() == ".." }) return null
    return rel.normalize().toString().replace('\\', '/')
  }

  private fun blankCommentsExceptStrings(source: String): String {
    val out = CharArray(source.length) { source[it] }
    var i = 0
    val n = source.length
    while (i < n) {
      val c = source[i]
      val next = if (i + 1 < n) source[i + 1] else '\u0000'
      when {
        c == '/' && next == '/' -> {
          while (i < n && source[i] != '\n') out[i++] = ' '
        }
        c == '/' && next == '*' -> {
          var depth = 1
          out[i++] = ' '
          out[i++] = ' '
          while (i < n && depth > 0) {
            if (source[i] == '/' && i + 1 < n && source[i + 1] == '*') {
              depth++
              out[i++] = ' '
              out[i++] = ' '
            } else if (source[i] == '*' && i + 1 < n && source[i + 1] == '/') {
              depth--
              out[i++] = ' '
              out[i++] = ' '
            } else {
              if (source[i] != '\n') out[i] = ' '
              i++
            }
          }
        }
        c == '"' -> {
          i++
          while (i < n) {
            val sc = source[i]
            if (sc == '\\' && i + 1 < n) {
              i += 2
            } else {
              i++
              if (sc == '"' || sc == '\n') break
            }
          }
        }
        else -> i++
      }
    }
    return String(out)
  }

  private fun buildCompilerArgs(source: Path, classesDir: Path, meta: BootstrapMeta) =
    compiler.createArguments().apply {
      val cp =
        listOf(meta.androidJar.toString(), meta.bundleJar.toString())
          .joinToString(File.pathSeparator)
      val cli =
        arrayOf(
          source.toString(),
          "-d",
          classesDir.toString(),
          "-classpath",
          cp,
          "-jvm-target",
          "17",
          "-no-stdlib",
          "-no-reflect",
          "-opt-in=kotlin.RequiresOptIn",
          "-opt-in=androidx.compose.material3.ExperimentalMaterial3Api",
          "-opt-in=androidx.compose.material3.ExperimentalMaterial3ExpressiveApi",
          "-opt-in=androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi",
          "-opt-in=androidx.wear.compose.material3.ExperimentalWearMaterial3Api",
          "-Xplugin=${meta.composeCompilerPluginJar}",
          "-P",
          "plugin:androidx.compose.compiler.plugins.kotlin:sourceInformation=true",
        )
      compiler.parseArguments(cli, this)
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
            else Paths.get(location.path).fileName.toString()
          "$file:${location.line}:${location.column}"
        }
      errors += ValidationError(where, message)
    }
  }
}

private class D8Session(
  androidJar: Path,
  bundleJar: Path,
  d8Jar: Path,
) {
  private val loader =
    URLClassLoader(arrayOf(d8Jar.toUri().toURL()), D8Session::class.java.classLoader)
  private val d8Class = loader.loadClass("com.android.tools.r8.D8")
  private val d8CommandClass = loader.loadClass("com.android.tools.r8.D8Command")
  private val builderClass = loader.loadClass("com.android.tools.r8.D8Command\$Builder")
  private val compilationModeClass = loader.loadClass("com.android.tools.r8.CompilationMode")
  private val originClass = loader.loadClass("com.android.tools.r8.origin.Origin")
  private val dexIndexedConsumerClass = loader.loadClass("com.android.tools.r8.DexIndexedConsumer")
  private val programConsumerClass = loader.loadClass("com.android.tools.r8.ProgramConsumer")
  private val classFileProviderClass =
    loader.loadClass("com.android.tools.r8.ClassFileResourceProvider")
  private val archiveProviderClass =
    loader.loadClass("com.android.tools.r8.ArchiveClassFileProvider")
  private val diagnosticsHandlerClass = loader.loadClass("com.android.tools.r8.DiagnosticsHandler")

  private val modeDebug = compilationModeClass.getField("DEBUG").get(null)
  private val originUnknown = originClass.getMethod("unknown").invoke(null)

  private val androidLibProxy =
    nonClosingProvider(
      archiveProviderClass.getConstructor(Path::class.java).newInstance(androidJar),
    )
  private val bundleLibProxy =
    nonClosingProvider(archiveProviderClass.getConstructor(Path::class.java).newInstance(bundleJar))

  private fun nonClosingProvider(delegate: Any): Any =
    Proxy.newProxyInstance(loader, arrayOf(classFileProviderClass)) { _, method, args ->
      when (method.name) {
        "finished",
        "close" -> null
        else -> method.invoke(delegate, *(args ?: emptyArray()))
      }
    }

  @Synchronized
  fun compileToDex(classes: Map<String, ByteArray>): ByteArray {
    val d8Errors = mutableListOf<String>()
    val diagnostics =
      Proxy.newProxyInstance(loader, arrayOf(diagnosticsHandlerClass)) { _, method, mArgs ->
        when (method.name) {
          "error" -> {
            val msg =
              mArgs?.getOrNull(0)?.let {
                it.javaClass.getMethod("getDiagnosticMessage").invoke(it)?.toString()
              } ?: "D8 error"
            d8Errors += msg
            null
          }
          "modifyDiagnosticsLevel" -> mArgs?.getOrNull(0)
          else -> null
        }
      }

    val dexChunks = mutableListOf<Pair<Int, ByteArray>>()
    val consumer =
      Proxy.newProxyInstance(loader, arrayOf(dexIndexedConsumerClass)) { _, method, mArgs ->
        when (method.name) {
          "accept" -> {
            val index = mArgs[0] as Int
            val dataArg = mArgs[1]
            val bytes =
              if (dataArg is ByteArray) {
                dataArg.copyOf()
              } else {
                dataArg.javaClass.getMethod("copyByteData").invoke(dataArg) as ByteArray
              }
            dexChunks += index to bytes
            null
          }
          "acquireByteBuffer" -> ByteBuffer.allocate(mArgs[0] as Int)
          "releaseByteBuffer",
          "finished" -> null
          else -> null
        }
      }

    val builder =
      d8CommandClass.getMethod("builder", diagnosticsHandlerClass).invoke(null, diagnostics)
    builderClass.getMethod("setMinApiLevel", Int::class.javaPrimitiveType).invoke(builder, 34)
    builderClass.getMethod("setMode", compilationModeClass).invoke(builder, modeDebug)
    builderClass.getMethod("setProgramConsumer", programConsumerClass).invoke(builder, consumer)
    builderClass
      .getMethod("addLibraryResourceProvider", classFileProviderClass)
      .invoke(builder, androidLibProxy)
    builderClass
      .getMethod("addLibraryResourceProvider", classFileProviderClass)
      .invoke(builder, bundleLibProxy)

    val addProgram =
      builderClass.getMethod("addClassProgramData", ByteArray::class.java, originClass)
    for ((_, bytes) in classes) {
      addProgram.invoke(builder, bytes, originUnknown)
    }

    val cmd = builderClass.getMethod("build").invoke(builder)
    d8Class.getMethod("run", d8CommandClass).invoke(null, cmd)

    check(d8Errors.isEmpty()) { "D8 failed:\n${d8Errors.joinToString("\n")}" }
    check(dexChunks.size == 1) { "Expected 1 classes.dex chunk, got ${dexChunks.size}" }
    return dexChunks.single().second
  }
}
