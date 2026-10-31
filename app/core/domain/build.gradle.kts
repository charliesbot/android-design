plugins {
  alias(libs.plugins.kotlin.jvm)
  alias(libs.plugins.kotlin.serialization)
}

dependencies {
  api(project(":core:model"))
  api(libs.kotlinx.coroutines.core)
  testImplementation(libs.kotlinx.coroutines.test)
  // Reads the DESIGN.md front matter of the shared fixtures for the export golden test.
  testImplementation(libs.kaml)
}

val schemasDir = rootProject.layout.projectDirectory.dir("schemas")

// Regenerates app/schemas/*.json from the catalog registry. A test fails when they drift.
tasks.register<JavaExec>("writeSchemas") {
  group = "schemas"
  description = "Regenerates the committed catalog and proposal JSON schemas."
  classpath = sourceSets.main.get().runtimeClasspath
  mainClass = "bot.charlies.androiddesign.preview.domain.catalog.WriteSchemasKt"
  args(schemasDir.asFile.absolutePath)
}

// The export golden test reads the fixtures that :core:data keeps.
val fixturesDir = rootProject.layout.projectDirectory.dir("core/data/src/test/resources/fixtures")

// The verify golden test exports the proposal that exercises every catalog component.
val verifyDir = rootProject.layout.projectDirectory.dir("../evals/verify")

tasks.test {
  inputs.dir(schemasDir)
  inputs.dir(fixturesDir)
  inputs.dir(verifyDir)
  systemProperty("verify.dir", verifyDir.asFile.absolutePath)
  systemProperty("schemas.dir", schemasDir.asFile.absolutePath)
  systemProperty("fixtures.dir", fixturesDir.asFile.absolutePath)
}
