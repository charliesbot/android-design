package bot.charlies.androiddesign.preview.host.compiler

import bot.charlies.androiddesign.preview.domain.sandbox.BytecodeGuard
import bot.charlies.androiddesign.preview.domain.sandbox.SourceTokenLinter
import bot.charlies.androiddesign.preview.model.ValidationError
import java.io.File
import java.lang.reflect.Proxy
import java.net.URLClassLoader
import java.nio.ByteBuffer
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.Properties
import java.util.concurrent.ConcurrentHashMap
import org.jetbrains.kotlin.cli.common.ExitCode as KotlinExitCode
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSourceLocation
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.cli.jvm.K2JVMCompiler
import org.jetbrains.kotlin.config.Services

data class BootstrapMeta(
  val bootstrapDir: Path,
  val androidJar: Path,
  val bundleJar: Path,
  val d8Jar: Path,
  val composeCompilerPluginJar: Path,
  val apkPath: Path,
) {
  companion object {
    fun locate(): BootstrapMeta {
      val explicitDir = System.getProperty("adp.bootstrap.dir")?.trim()?.takeIf { it.isNotEmpty() }
      val cpBootstrapDir =
        System.getProperty("java.class.path")
          .orEmpty()
          .split(File.pathSeparator)
          .firstNotNullOfOrNull { entry ->
            val p = runCatching { Paths.get(entry).toAbsolutePath().normalize() }.getOrNull()
            when {
              p == null -> null
              p.fileName?.toString() == "robolectric-config" ->
                p.parent?.resolve("bootstrap-meta.properties")?.takeIf { Files.isRegularFile(it) }
              Files.isDirectory(p) ->
                p.resolve("bootstrap-meta.properties").takeIf { Files.isRegularFile(it) }
              else -> null
            }
          }
      val candidates = buildList {
        if (explicitDir != null) {
          add(Paths.get(explicitDir).resolve("bootstrap-meta.properties"))
        }
        if (cpBootstrapDir != null) {
          add(cpBootstrapDir)
        }
        add(Paths.get("build/bootstrap/bootstrap-meta.properties"))
        add(Paths.get("host/build/bootstrap/bootstrap-meta.properties"))
        add(Paths.get("app/host/build/bootstrap/bootstrap-meta.properties"))
      }
      val metaFile =
        candidates.firstOrNull { Files.isRegularFile(it) }
          ?: error(
            "Cannot find bootstrap-meta.properties; searched: ${candidates.joinToString()}",
          )
      return fromFile(metaFile)
    }

    fun fromFile(metaFile: Path): BootstrapMeta {
      val props = Properties()
      Files.newBufferedReader(metaFile).use { props.load(it) }
      fun reqPath(key: String): Path {
        val raw =
          requireNotNull(props.getProperty(key)?.trim()?.takeIf { it.isNotEmpty() }) {
            "Missing '$key' in $metaFile"
          }
        val path = Paths.get(raw).toAbsolutePath().normalize()
        require(Files.exists(path)) { "Bootstrap entry '$key' missing on disk: $path" }
        return path
      }
      return BootstrapMeta(
        bootstrapDir = metaFile.toAbsolutePath().normalize().parent,
        androidJar = reqPath("androidJar"),
        bundleJar = reqPath("bundleJar"),
        d8Jar = reqPath("d8Jar"),
        composeCompilerPluginJar = reqPath("composeCompilerPluginJar"),
        apkPath = reqPath("apkPath"),
      )
    }
  }
}

sealed interface HostCompileOutcome {
  data class Classes(
    val classes: Map<String, ByteArray>,
    val entryClass: String,
    val compileMs: Long,
  ) : HostCompileOutcome

  data class Failed(
    val errors: List<ValidationError>,
    val compileMs: Long,
  ) : HostCompileOutcome
}

/**
 * In-process Kotlin + Compose compiler producing JVM `.class` maps (for Robolectric) and in-memory
 * `.dex` bytecode via R8 D8 (for host emulator / device verification), behind [SourceTokenLinter]
 * and [BytecodeGuard].
 */
object HostClassCompiler {
  private const val NANOS_PER_MS = 1_000_000L

  val bootstrapMeta: BootstrapMeta by lazy { BootstrapMeta.locate() }

  private val compiler = K2JVMCompiler()
  private val d8Sessions = ConcurrentHashMap<Path, D8Session>()

  fun compileToClasses(
    fileName: String,
    sourceText: String,
    skipGuards: Boolean = false,
  ): HostCompileOutcome {
    val started = System.nanoTime()
    fun elapsedMs(): Long = (System.nanoTime() - started) / NANOS_PER_MS

    if (!skipGuards) {
      val lintErrors = SourceTokenLinter.lint(fileName, sourceText)
      if (lintErrors.isNotEmpty()) {
        return HostCompileOutcome.Failed(lintErrors, elapsedMs())
      }
    }

    val workDir = Files.createTempDirectory("adp-host-compile-")
    return try {
      val srcDir = workDir.resolve("src").also { Files.createDirectories(it) }
      val classesDir = workDir.resolve("classes").also { Files.createDirectories(it) }
      val kotlinName = if (fileName.endsWith(".kt")) fileName else "$fileName.kt"
      val tempSource = srcDir.resolve(kotlinName)
      Files.writeString(tempSource, sourceText)

      val collector = DiagnosticCollector(tempSource, fileName)
      val args = buildCompilerArgs(tempSource, classesDir)
      val exit = compiler.exec(collector, Services.EMPTY, args)
      if (exit != KotlinExitCode.OK || collector.errors.isNotEmpty()) {
        val errors =
          collector.errors.ifEmpty {
            listOf(ValidationError(fileName, "Kotlin compilation failed ($exit)"))
          }
        return HostCompileOutcome.Failed(errors, elapsedMs())
      }

      val classBytes = readClassFiles(classesDir)
      if (classBytes.isEmpty()) {
        return HostCompileOutcome.Failed(
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
          return HostCompileOutcome.Failed(secErrors, elapsedMs())
        }
      }

      val entryClass =
        classBytes.keys.firstOrNull { it.endsWith("Kt") && !it.contains('$') }
          ?: classBytes.keys.first()
      HostCompileOutcome.Classes(classBytes, entryClass, elapsedMs())
    } finally {
      workDir.toFile().deleteRecursively()
    }
  }

  fun compileToDex(
    compiled: HostCompileOutcome.Classes,
    meta: BootstrapMeta = bootstrapMeta,
  ): ByteArray {
    val session =
      d8Sessions.computeIfAbsent(meta.d8Jar) {
        D8Session(meta.androidJar, meta.bundleJar, meta.d8Jar)
      }
    return session.compileToDex(compiled.classes)
  }

  private fun buildCompilerArgs(source: Path, classesDir: Path) =
    compiler.createArguments().apply {
      val cp =
        listOf(
            bootstrapMeta.androidJar.toString(),
            bootstrapMeta.bundleJar.toString(),
          )
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
          "-Xplugin=${bootstrapMeta.composeCompilerPluginJar}",
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
