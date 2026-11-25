package bot.charlies.androiddesign.preview.host

import org.junit.runners.model.FrameworkMethod
import org.robolectric.RobolectricTestRunner
import org.robolectric.internal.bytecode.InstrumentationConfiguration

/**
 * Custom [RobolectricTestRunner] keeping Kotlin, `:core:*`, and serialization packages
 * uninstrumented so Compose board unit tests share domain models with the JVM test classpath.
 */
class SandboxExcludingRobolectricTestRunner(testClass: Class<*>) :
  RobolectricTestRunner(testClass) {
  override fun createClassLoaderConfig(method: FrameworkMethod): InstrumentationConfiguration {
    val base = super.createClassLoaderConfig(method)
    return InstrumentationConfiguration.Builder(base)
      .doNotAcquirePackage("org.jetbrains.kotlin.")
      .doNotAcquirePackage("org.jetbrains.annotations.")
      .doNotAcquirePackage("com.intellij.")
      .doNotAcquirePackage("javax.xml.")
      .doNotAcquirePackage("org.w3c.")
      .doNotAcquirePackage("org.xml.")
      .doNotAcquirePackage("kotlinx.serialization.")
      .doNotAcquirePackage("com.charleskorn.kaml.")
      .doNotAcquirePackage("bot.charlies.androiddesign.preview.model.")
      .doNotAcquirePackage("bot.charlies.androiddesign.preview.domain.")
      .doNotAcquirePackage("bot.charlies.androiddesign.preview.data.")
      .doNotAcquirePackage("bot.charlies.androiddesign.preview.app.")
      .build()
  }
}
