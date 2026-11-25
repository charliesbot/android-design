import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension

plugins {
  alias(libs.plugins.kotlin.jvm) apply false
  alias(libs.plugins.kotlin.compose) apply false
  alias(libs.plugins.kotlin.serialization) apply false
  alias(libs.plugins.android.application) apply false
  alias(libs.plugins.spotless)
}

spotless {
  kotlin {
    target("**/*.kt")
    targetExclude("**/build/**", ".gradle/**")
    ktfmt().googleStyle()
  }
  kotlinGradle {
    target("**/*.gradle.kts")
    targetExclude("**/build/**", ".gradle/**")
    ktfmt().googleStyle()
  }
}

subprojects {
  plugins.withId("org.jetbrains.kotlin.jvm") {
    if (path != ":render") {
      extensions.configure<KotlinJvmProjectExtension> { jvmToolchain(17) }
    }

    dependencies {
      add("testImplementation", platform(rootProject.libs.junit.bom))
      add("testImplementation", kotlin("test"))
      add("testRuntimeOnly", "org.junit.platform:junit-platform-launcher")
    }

    tasks.withType<Test>().configureEach {
      useJUnitPlatform()
      testLogging { events("failed") }
    }
  }
}
