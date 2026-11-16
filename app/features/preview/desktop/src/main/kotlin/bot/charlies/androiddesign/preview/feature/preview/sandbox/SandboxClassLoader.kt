package bot.charlies.androiddesign.preview.feature.preview.sandbox

import java.io.InputStream
import java.net.URL
import java.util.Collections
import java.util.Enumeration

/**
 * Isolated classloader that defines compiled exploration classes in memory and blocks resolution of
 * any disallowed JDK, reflection, thread-creation, or I/O class at link time.
 */
internal class SandboxClassLoader(
  private val compiledClasses: Map<String, ByteArray>,
  parent: ClassLoader,
) : ClassLoader(parent) {
  override fun loadClass(name: String, resolve: Boolean): Class<*> {
    findLoadedClass(name)?.let {
      if (resolve) resolveClass(it)
      return it
    }
    if (!SandboxPolicy.isAllowedClass(name, compiledClasses.keys)) {
      throw SecurityException("Disallowed class in exploration sandbox: $name")
    }
    val bytes = compiledClasses[name]
    if (bytes != null) {
      val defined = defineClass(name, bytes, 0, bytes.size)
      if (resolve) resolveClass(defined)
      return defined
    }
    return super.loadClass(name, resolve)
  }

  override fun getResource(name: String?): URL? = null

  override fun getResources(name: String?): Enumeration<URL> = Collections.emptyEnumeration()

  override fun getResourceAsStream(name: String?): InputStream? = null
}
