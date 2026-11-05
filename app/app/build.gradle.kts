import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
  alias(libs.plugins.kotlin.jvm)
  alias(libs.plugins.kotlin.compose)
  alias(libs.plugins.compose.multiplatform)
  alias(libs.plugins.kotlin.serialization)
}

dependencies {
  implementation(project(":core:model"))
  implementation(project(":core:domain"))
  implementation(project(":core:data"))
  implementation(project(":features:preview:desktop"))
  implementation(project(":mcp"))
  implementation(compose.desktop.currentOs)
  implementation(libs.compose.material3)
  implementation(libs.koin.core)
  implementation(libs.koin.compose)
  implementation(libs.kotlinx.coroutines.swing)
  testImplementation(libs.compose.ui.test)
  testImplementation(libs.mcp.sdk)
  testImplementation(libs.kotlinx.coroutines.test)
}

// The packaged app's bundle identifier, which is also its preferences domain (see AppNap).
val appBundleId = "bot.charlies.androiddesign.preview.app"

compose.desktop {
  application {
    mainClass = "bot.charlies.androiddesign.preview.app.MainKt"
    // HotSpot prints warnings to stdout by default; stdout carries only JSON-RPC in MCP mode.
    jvmArgs += "-XX:+DisplayVMOutputToStderr"
    // Read by AppNap.BUNDLE_ID_PROPERTY: only the packaged app has a preferences domain.
    jvmArgs += "-Dapp.bundleId=$appBundleId"
    // The title bar follows macOS light or dark mode instead of staying light.
    jvmArgs += "-Dapple.awt.application.appearance=system"
    nativeDistributions {
      targetFormats(TargetFormat.Dmg, TargetFormat.Msi)
      packageName = "AndroidDesignPreview"
      packageVersion = "1.0.0"
      includeAllModules = true
      macOS {
        bundleID = appBundleId
        // Signing and notarization are off unless the build workflow sets these from its
        // secrets (docs/previewer-signing.md). Blank counts as unset, as GitHub passes missing
        // secrets as empty strings.
        fun env(name: String) = providers.environmentVariable(name).filter { it.isNotBlank() }
        val identity = env("MACOS_SIGNING_IDENTITY")
        signing {
          sign.set(identity.map { true }.orElse(false))
          this.identity.set(identity)
          keychain.set(env("MACOS_SIGNING_KEYCHAIN"))
        }
        notarization {
          appleID.set(env("MACOS_NOTARIZATION_APPLE_ID"))
          password.set(env("MACOS_NOTARIZATION_PASSWORD"))
          teamID.set(env("MACOS_NOTARIZATION_TEAM_ID"))
        }
      }
      windows {
        // Installs to %LOCALAPPDATA%\AndroidDesignPreview with no admin prompt, so the MCP
        // binary path is the same for every user.
        perUserInstall = true
        menu = true
        menuGroup = "Android Design Preview"
        shortcut = true
        // Fixed so a newer MSI upgrades the installed app instead of installing beside it.
        upgradeUuid = "5f0c9a52-7c1e-4d0b-9b8e-3a6f2d1c4e71"
      }
    }
  }
}

// `run` gets an empty stdin by default; forward Gradle's own stdin so MCP can talk to it.
tasks.withType<JavaExec>().configureEach { standardInput = System.`in` }

val fixturesDir =
  project(":core:data")
    .layout
    .projectDirectory
    .dir("src/test/resources/fixtures")
    .asFile
    .absolutePath
// The verify proposal lives in the repository's evals folder, above this standalone build.
val repoDir = rootProject.layout.projectDirectory.asFile.parentFile.absolutePath

tasks.test {
  // The CLI tests render the proposal fixture that :core:data keeps.
  systemProperty("fixtures.dir", fixturesDir)
  systemProperty("repo.dir", repoDir)
  useJUnitPlatform { excludeTags("benchmark") }
}

// Reports how long the window takes to adopt a saved change. Numbers, not assertions.
tasks.register<Test>("saveToAdopt") {
  group = "verification"
  description = "Prints the save-to-adopt and get_tree timings for the verify proposal."
  val test = tasks.test.get()
  testClassesDirs = test.testClassesDirs
  classpath = test.classpath
  systemProperty("fixtures.dir", fixturesDir)
  systemProperty("repo.dir", repoDir)
  useJUnitPlatform { includeTags("benchmark") }
  testLogging.showStandardStreams = true
  outputs.upToDateWhen { false }
}

if (System.getProperty("os.name").startsWith("Windows")) {
  // Builds the Windows app and copies it to %LOCALAPPDATA%\AndroidDesignPreview, where the MSI
  // installs it too, replacing any old copy. Quit a running instance first: Windows keeps its
  // files locked.
  tasks.register<Sync>("installApp") {
    group = "distribution"
    description = "Builds AndroidDesignPreview.exe and copies it to %LOCALAPPDATA%."
    dependsOn("createDistributable")
    from(layout.buildDirectory.dir("compose/binaries/main/app/AndroidDesignPreview"))
    into(
      File(
        System.getenv("LOCALAPPDATA") ?: "${System.getProperty("user.home")}\\AppData\\Local",
        "AndroidDesignPreview",
      ),
    )
  }
} else {
  // Builds the macOS app and copies it into ~/Applications, replacing any old copy. `ditto` keeps
  // the bundle's symlinks, permissions and code signature intact, which a Gradle copy would not.
  tasks.register<Exec>("installApp") {
    group = "distribution"
    description = "Builds AndroidDesignPreview.app and copies it to ~/Applications."
    dependsOn("createDistributable")
    val built = layout.buildDirectory.dir("compose/binaries/main/app/AndroidDesignPreview.app")
    val installed = File(System.getProperty("user.home"), "Applications/AndroidDesignPreview.app")
    doFirst {
      installed.deleteRecursively()
      installed.parentFile.mkdirs()
    }
    commandLine("ditto", built.get().asFile.path, installed.path)
  }
}
