import java.io.FileOutputStream
import java.security.MessageDigest
import java.util.Properties
import java.util.zip.CRC32
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream
import org.gradle.api.DefaultTask
import org.gradle.api.attributes.Attribute
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.ClasspathNormalizer
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction

plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.kotlin.compose)
  alias(libs.plugins.kotlin.serialization)
}

abstract class SyncFontsTask : DefaultTask() {
  @get:InputFiles
  @get:PathSensitive(PathSensitivity.RELATIVE)
  abstract val fontFiles: ConfigurableFileCollection

  @get:OutputDirectory abstract val outputDir: DirectoryProperty

  @TaskAction
  fun sync() {
    val fontsOut = outputDir.get().asFile.resolve("fonts")
    fontsOut.deleteRecursively()
    fontsOut.mkdirs()
    for (src in fontFiles.files) {
      if (src.isFile) {
        src.copyTo(fontsOut.resolve(src.name), overwrite = true)
      }
    }
  }
}

val syncBundledFonts =
  tasks.register<SyncFontsTask>("syncBundledFonts") {
    fontFiles.from(
      rootProject.fileTree("features/preview/desktop/src/main/resources/fonts") {
        include("*.ttf")
      },
      rootProject.file("wear/compose/src/main/resources/fonts/roboto_flex.ttf"),
    )
    outputDir.set(layout.buildDirectory.dir("generated/assets/bundledFonts"))
  }

val composeCompilerPlugin = configurations.create("composeCompilerPlugin") { isTransitive = false }

android {
  namespace = "bot.charlies.androiddesign.preview.host"
  compileSdk {
    version =
      release(37) {
        minorApiLevel = 1
      }
  }

  defaultConfig {
    applicationId = "bot.charlies.androiddesign.preview.host"
    minSdk = 34
    targetSdk = 36
    versionCode = 1
    versionName = "0.1.0"
  }

  buildTypes {
    create("previewRelease") {
      initWith(getByName("release"))
      isDebuggable = false
      isMinifyEnabled = false
      signingConfig = signingConfigs.getByName("debug")
      matchingFallbacks += listOf("release")
    }
  }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
  }

  testOptions {
    unitTests {
      isIncludeAndroidResources = true
      all {
        it.maxHeapSize = "3072m"
        it.testLogging {
          events("passed", "skipped", "failed", "standardOut", "standardError")
          showStandardStreams = true
        }
      }
    }
  }

  lint {
    abortOnError = true
    warningsAsErrors = false
  }
}

androidComponents {
  beforeVariants(selector().withBuildType("previewRelease")) { variantBuilder ->
    variantBuilder.hostTests.forEach { (_, hostTest) -> hostTest.enable = false }
  }

  onVariants { variant ->
    variant.sources.assets?.addGeneratedSourceDirectory(syncBundledFonts, SyncFontsTask::outputDir)
  }
}

composeCompiler { includeSourceInformation = true }

kotlin { jvmToolchain(17) }

dependencies {
  implementation(project(":core:model"))
  implementation(project(":core:domain"))
  implementation(platform(libs.androidx.compose.bom))
  implementation(libs.androidx.compose.ui)
  implementation(libs.androidx.compose.ui.tooling.data)
  implementation(libs.androidx.compose.foundation)
  implementation(libs.androidx.compose.material3)
  implementation(libs.androidx.compose.material.icons.core)
  implementation(libs.androidx.activity.compose)
  implementation(libs.androidx.navigationevent)
  implementation(libs.androidx.navigationevent.compose)
  implementation(libs.androidx.navigation3.runtime)
  implementation(libs.androidx.navigation3.ui)
  implementation(libs.androidx.adaptive)
  implementation(libs.androidx.adaptive.layout)
  implementation(libs.androidx.adaptive.navigation3)
  implementation(libs.androidx.wear.compose.foundation)
  implementation(libs.androidx.wear.compose.material3)
  implementation(libs.androidx.wear.compose.navigation3)
  implementation(libs.material.kolor)
  implementation(libs.kotlinx.coroutines.android)
  implementation(libs.kotlinx.serialization.json)
  implementation(libs.lz4.java)

  composeCompilerPlugin(libs.kotlin.compose.compiler.embeddable)

  testImplementation(project(":core:data"))
  testImplementation(libs.junit4)
  testImplementation(libs.robolectric)
  testImplementation(libs.kotlin.compiler.embeddable)
  testImplementation(libs.kaml)
}

val exportCompileBundle =
  tasks.register("exportCompileBundle") {
    group = "bootstrap"
    description =
      "Exports uncompressed androidx-compile-bundle.jar, host-preview.apk, and SDK metadata."
    val compileKotlin = tasks.named("compilePreviewReleaseKotlin")
    val assembleApk = tasks.named("assemblePreviewRelease")
    dependsOn(compileKotlin, assembleApk)

    val compileClasspath = configurations.getByName("previewReleaseCompileClasspath")
    val composePluginFiles = composeCompilerPlugin
    val outDir = layout.buildDirectory.dir("bootstrap")
    val sdkComponents =
      project.extensions
        .getByType(com.android.build.api.variant.AndroidComponentsExtension::class.java)
        .sdkComponents
    val builtApk = layout.buildDirectory.file("outputs/apk/previewRelease/host-previewRelease.apk")
    val compileTaskOutputs = compileKotlin.map { it.outputs.files }
    val compileArtifactView =
      compileClasspath.incoming
        .artifactView {
          attributes {
            attribute(Attribute.of("artifactType", String::class.java), "android-classes-jar")
          }
        }
        .files
    val outBundleJar = layout.buildDirectory.file("bootstrap/androidx-compile-bundle.jar")
    val outHostApk = layout.buildDirectory.file("bootstrap/host-preview.apk")
    val outMetaFile = layout.buildDirectory.file("bootstrap/bootstrap-meta.properties")
    val outPathsFile = layout.buildDirectory.file("bootstrap/bootstrap-paths.txt")

    inputs.file(builtApk).withPropertyName("builtPreviewReleaseApk")
    inputs
      .files(compileTaskOutputs)
      .withPropertyName("compilePreviewReleaseOutputs")
      .withNormalizer(ClasspathNormalizer::class.java)
    inputs
      .files(compileArtifactView)
      .withPropertyName("previewReleaseCompileJars")
      .withNormalizer(ClasspathNormalizer::class.java)
    inputs
      .files(composePluginFiles)
      .withPropertyName("composeCompilerPlugin")
      .withNormalizer(ClasspathNormalizer::class.java)
    inputs.files(sdkComponents.bootClasspath).withPropertyName("sdkBootClasspath")
    inputs.property("sdkDirectoryPath", sdkComponents.sdkDirectory.map { it.asFile.absolutePath })

    outputs.file(outBundleJar)
    outputs.file(outHostApk)
    outputs.file(outMetaFile)
    outputs.file(outPathsFile)

    doLast {
      outDir.get().asFile.mkdirs()
      val bundleFile = outBundleJar.get().asFile
      val copiedApk = outHostApk.get().asFile
      val sourceApk = builtApk.get().asFile
      check(sourceApk.isFile) { "Built APK missing at ${sourceApk.absolutePath}" }
      sourceApk.copyTo(copiedApk, overwrite = true)

      val artifactFiles = compileArtifactView.files

      val seenEntries = HashSet<String>()
      var jarCount = 0
      var classEntries = 0
      var kotlinModuleEntries = 0
      var kotlinBuiltinEntries = 0

      fun writeStoredEntry(zos: ZipOutputStream, name: String, bytes: ByteArray) {
        if (!seenEntries.add(name)) return
        val entry =
          ZipEntry(name).apply {
            method = ZipEntry.STORED
            size = bytes.size.toLong()
            compressedSize = bytes.size.toLong()
            val crcCalc = CRC32()
            crcCalc.update(bytes)
            crc = crcCalc.value
          }
        zos.putNextEntry(entry)
        zos.write(bytes)
        zos.closeEntry()
        if (name.endsWith(".class")) classEntries++
        if (name.startsWith("META-INF/") && name.endsWith(".kotlin_module")) kotlinModuleEntries++
        if (name.startsWith("kotlin/") && name.endsWith(".kotlin_builtins")) kotlinBuiltinEntries++
      }

      ZipOutputStream(FileOutputStream(bundleFile).buffered(1024 * 1024)).use { zos ->
        for (file in artifactFiles) {
          if (!file.exists()) continue
          if (file.isDirectory) {
            file
              .walkTopDown()
              .filter { it.isFile }
              .forEach { child ->
                val rel = child.relativeTo(file).invariantSeparatorsPath
                writeStoredEntry(zos, rel, child.readBytes())
              }
          } else if (file.extension == "jar") {
            jarCount++
            ZipFile(file).use { zip ->
              val entries = zip.entries()
              while (entries.hasMoreElements()) {
                val e = entries.nextElement()
                if (e.isDirectory) continue
                val n = e.name
                if (
                  n == "META-INF/MANIFEST.MF" ||
                    n.endsWith(".SF") ||
                    n.endsWith(".DSA") ||
                    n.endsWith(".RSA") ||
                    n == "module-info.class" ||
                    n.startsWith("META-INF/versions/")
                ) {
                  continue
                }
                writeStoredEntry(zos, n, zip.getInputStream(e).use { it.readBytes() })
              }
            }
          }
        }

        for (root in compileTaskOutputs.get().files) {
          if (!root.exists()) continue
          if (root.isDirectory) {
            root
              .walkTopDown()
              .filter { it.isFile }
              .forEach { child ->
                val rel = child.relativeTo(root).invariantSeparatorsPath
                if (rel != "META-INF/MANIFEST.MF") {
                  writeStoredEntry(zos, rel, child.readBytes())
                }
              }
          } else if (root.extension == "jar") {
            ZipFile(root).use { zip ->
              val entries = zip.entries()
              while (entries.hasMoreElements()) {
                val e = entries.nextElement()
                if (e.isDirectory || e.name == "META-INF/MANIFEST.MF") continue
                writeStoredEntry(zos, e.name, zip.getInputStream(e).use { it.readBytes() })
              }
            }
          }
        }
      }

      val bootClasspath = sdkComponents.bootClasspath.get().map { it.asFile.absolutePath }
      val androidJar = bootClasspath.firstOrNull { it.endsWith("android.jar") } ?: ""
      val sdkDir = sdkComponents.sdkDirectory.get().asFile
      val d8Jar =
        sdkDir
          .resolve("build-tools")
          .listFiles()
          ?.filter { it.isDirectory }
          ?.sortedByDescending { it.name }
          ?.map { it.resolve("lib/d8.jar") }
          ?.firstOrNull { it.isFile }
          ?.absolutePath ?: ""
      val composePluginJar = composePluginFiles.files.singleOrNull()?.absolutePath ?: ""
      val apkDigest = MessageDigest.getInstance("SHA-256")
      copiedApk.inputStream().buffered().use { input ->
        val buf = ByteArray(8192)
        while (true) {
          val n = input.read(buf)
          if (n <= 0) break
          apkDigest.update(buf, 0, n)
        }
      }
      val apkSha256 = apkDigest.digest().joinToString("") { "%02x".format(it) }

      val props = Properties()
      props.setProperty("androidJar", androidJar)
      props.setProperty("d8Jar", d8Jar)
      props.setProperty("bundleJar", bundleFile.absolutePath)
      props.setProperty("apkPath", copiedApk.absolutePath)
      props.setProperty("apkSha256", apkSha256)
      props.setProperty("composeCompilerPluginJar", composePluginJar)
      props.setProperty("jarCount", jarCount.toString())
      props.setProperty("classEntries", classEntries.toString())
      props.setProperty("kotlinModuleEntries", kotlinModuleEntries.toString())
      props.setProperty("kotlinBuiltinEntries", kotlinBuiltinEntries.toString())
      props.setProperty("bundleSizeBytes", bundleFile.length().toString())
      outMetaFile.get().asFile.bufferedWriter().use { writer ->
        props.store(writer, "android-design-preview bootstrap metadata")
      }
      outPathsFile
        .get()
        .asFile
        .writeText(
          listOf(
              androidJar,
              d8Jar,
              bundleFile.absolutePath,
              copiedApk.absolutePath,
              composePluginJar,
            )
            .filter { it.isNotEmpty() }
            .joinToString("\n") + "\n",
        )

      logger.lifecycle(
        "Exported compile bundle: ${bundleFile.length() / (1024 * 1024)} MiB from $jarCount jars ($classEntries classes, $kotlinModuleEntries .kotlin_module, $kotlinBuiltinEntries .kotlin_builtins)",
      )
    }
  }

evaluationDependsOn(":app")

val exportBootstrap =
  tasks.register("exportBootstrap") {
    group = "bootstrap"
    description =
      "Exports host-preview.apk, androidx-compile-bundle.jar, metadata, and @server.cp, @client.cp, @robolectric.cp argfiles."

    val compileDebugKotlin = tasks.named("compileDebugKotlin")
    val bundleDebugJar = tasks.named("bundleDebugClassesToRuntimeJar")
    val compileDebugUnitTestKotlin = tasks.named("compileDebugUnitTestKotlin")
    val genTestConfig = tasks.named("generateDebugUnitTestConfig")
    val appProject = project(":app")
    val appJarTask = appProject.tasks.named("jar")

    dependsOn(
      exportCompileBundle,
      compileDebugKotlin,
      bundleDebugJar,
      compileDebugUnitTestKotlin,
      genTestConfig,
      appJarTask,
    )

    val unitTestConfig = configurations.getByName("debugUnitTestRuntimeClasspath")
    val sdkComponents =
      project.extensions
        .getByType(com.android.build.api.variant.AndroidComponentsExtension::class.java)
        .sdkComponents
    val appRuntimeConfig = appProject.configurations.getByName("runtimeClasspath")
    val debugMainOutputs = compileDebugKotlin.map { it.outputs.files }
    val debugBundleOutputs = bundleDebugJar.map { it.outputs.files }
    val debugUnitTestOutputs = compileDebugUnitTestKotlin.map { it.outputs.files }
    val testConfigOutputs = genTestConfig.map { it.outputs.files }
    val appJarOutputs = appJarTask.map { it.outputs.files }
    val unitTestArtifactView =
      unitTestConfig.incoming
        .artifactView {
          attributes {
            attribute(Attribute.of("artifactType", String::class.java), "android-classes-jar")
          }
        }
        .files
    val outRobolectricConfigDir = layout.buildDirectory.dir("bootstrap/robolectric-config")
    val outRobolectricCp = layout.buildDirectory.file("bootstrap/robolectric.cp")
    val outServerCp = layout.buildDirectory.file("bootstrap/server.cp")
    val outClientCp = layout.buildDirectory.file("bootstrap/client.cp")

    inputs
      .files(unitTestArtifactView)
      .withPropertyName("debugUnitTestArtifactJars")
      .withNormalizer(ClasspathNormalizer::class.java)
    inputs
      .files(debugMainOutputs)
      .withPropertyName("debugMainOutputs")
      .withNormalizer(ClasspathNormalizer::class.java)
    inputs
      .files(debugBundleOutputs)
      .withPropertyName("debugBundleOutputs")
      .withNormalizer(ClasspathNormalizer::class.java)
    inputs
      .files(debugUnitTestOutputs)
      .withPropertyName("debugUnitTestOutputs")
      .withNormalizer(ClasspathNormalizer::class.java)
    inputs
      .files(testConfigOutputs)
      .withPropertyName("testConfigOutputs")
      .withPathSensitivity(PathSensitivity.RELATIVE)
    inputs
      .files(appJarOutputs)
      .withPropertyName("appJarOutputs")
      .withNormalizer(ClasspathNormalizer::class.java)
    inputs
      .files(appRuntimeConfig)
      .withPropertyName("appRuntimeClasspath")
      .withNormalizer(ClasspathNormalizer::class.java)
    inputs.files(sdkComponents.bootClasspath).withPropertyName("sdkBootClasspath")
    inputs.property("projectDirPath", project.projectDir.absolutePath)

    outputs.dir(outRobolectricConfigDir)
    outputs.file(outRobolectricCp)
    outputs.file(outServerCp)
    outputs.file(outClientCp)

    doLast {
      val unitTestClasspath = unitTestArtifactView.files

      val robolectricConfigDir = outRobolectricConfigDir.get().asFile
      robolectricConfigDir.deleteRecursively()
      val rawConfigFile =
        testConfigOutputs
          .get()
          .files
          .map { it.resolve("com/android/tools/test_config.properties") }
          .firstOrNull { it.isFile }
      if (rawConfigFile != null) {
        val rawProps = Properties().apply { rawConfigFile.reader().use { load(it) } }
        val absProps = Properties()
        for (key in rawProps.stringPropertyNames()) {
          val valStr = rawProps.getProperty(key)
          absProps.setProperty(
            key,
            if (key == "android_custom_package") valStr else project.file(valStr).absolutePath,
          )
        }
        val outConfig = robolectricConfigDir.resolve("com/android/tools/test_config.properties")
        outConfig.parentFile.mkdirs()
        outConfig.bufferedWriter().use { absProps.store(it, "absolute robolectric config") }
      }

      val robolectricEntries = linkedSetOf<String>()
      fun addExisting(files: Iterable<java.io.File>) {
        for (f in files) {
          if (f.exists()) robolectricEntries.add(f.absolutePath)
        }
      }
      if (robolectricConfigDir.isDirectory) {
        robolectricEntries.add(robolectricConfigDir.absolutePath)
      }
      addExisting(debugUnitTestOutputs.get().files)
      addExisting(debugBundleOutputs.get().files)
      addExisting(debugMainOutputs.get().files)
      addExisting(sdkComponents.bootClasspath.get().map { it.asFile })
      addExisting(unitTestClasspath)
      outRobolectricCp.get().asFile.apply {
        parentFile.mkdirs()
        writeText("-cp\n${robolectricEntries.joinToString(":")}\n")
      }

      val appEntries = linkedSetOf<String>()
      for (f in appJarOutputs.get().files) {
        if (f.exists()) appEntries.add(f.absolutePath)
      }
      for (f in appRuntimeConfig.files) {
        if (f.exists()) appEntries.add(f.absolutePath)
      }
      val appCpContent = "-cp\n${appEntries.joinToString(":")}\n"
      outServerCp.get().asFile.writeText(appCpContent)
      outClientCp.get().asFile.writeText(appCpContent)
    }
  }

tasks.withType<Test>().configureEach {
  dependsOn(exportBootstrap)
  systemProperty(
    "adp.bootstrap.dir",
    layout.buildDirectory.dir("bootstrap").get().asFile.absolutePath,
  )
  systemProperty(
    "adp.runEmulatorTests",
    System.getProperty("adp.runEmulatorTests", "false"),
  )
}
