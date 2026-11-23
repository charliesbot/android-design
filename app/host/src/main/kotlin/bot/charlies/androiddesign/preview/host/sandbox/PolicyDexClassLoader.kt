package bot.charlies.androiddesign.preview.host.sandbox

import bot.charlies.androiddesign.preview.domain.sandbox.SandboxPolicy
import dalvik.system.InMemoryDexClassLoader
import java.io.InputStream
import java.net.URL
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.Collections
import java.util.Enumeration

/**
 * In-memory DEX loader backed by [InMemoryDexClassLoader] and a policy-enforcing parent classloader
 * that rejects any external class disallowed by [SandboxPolicy].
 */
internal class PolicyDexClassLoader(
  dexBytes: ByteArray,
  hostParent: ClassLoader,
) {
  private val definedClasses = parseDexClassNames(dexBytes)
  private val delegate =
    InMemoryDexClassLoader(
      ByteBuffer.wrap(dexBytes),
      FilterClassLoader(hostParent, definedClasses),
    )

  fun loadClass(name: String): Class<*> = delegate.loadClass(name)

  private class FilterClassLoader(
    parent: ClassLoader,
    private val definedInDex: Set<String>,
  ) : ClassLoader(parent) {
    override fun loadClass(name: String, resolve: Boolean): Class<*> {
      findLoadedClass(name)?.let {
        if (resolve) resolveClass(it)
        return it
      }
      if (name in definedInDex && !isReservedPackage(name)) {
        // Returning ClassNotFoundException hands resolution back to
        // InMemoryDexClassLoader.findClass.
        throw ClassNotFoundException(name)
      }
      if (!SandboxPolicy.isAllowedClass(name)) {
        throw SecurityException("Disallowed class in exploration DEX sandbox: $name")
      }
      return super.loadClass(name, resolve)
    }

    override fun getResource(name: String?): URL? = null

    override fun getResources(name: String?): Enumeration<URL> = Collections.emptyEnumeration()

    override fun getResourceAsStream(name: String?): InputStream? = null
  }

  private companion object {
    val RESERVED_PREFIXES =
      listOf(
        "java.",
        "javax.",
        "sun.",
        "jdk.",
        "dalvik.",
        "android.",
        "androidx.",
        "kotlin.",
        "kotlinx.",
        "com.android.",
        "com.google.",
        "com.materialkolor.",
        "bot.charlies.",
        "org.jetbrains.",
      )

    fun isReservedPackage(dotName: String): Boolean = RESERVED_PREFIXES.any {
      dotName.startsWith(it)
    }

    fun parseDexClassNames(dexBytes: ByteArray): Set<String> {
      if (dexBytes.size < 112) return emptySet()
      val buf = ByteBuffer.wrap(dexBytes).order(ByteOrder.LITTLE_ENDIAN)
      val stringIdsSize = buf.getInt(56)
      val stringIdsOff = buf.getInt(60)
      val typeIdsSize = buf.getInt(64)
      val typeIdsOff = buf.getInt(68)
      val classDefsSize = buf.getInt(96)
      val classDefsOff = buf.getInt(100)
      val names = HashSet<String>(classDefsSize.coerceAtLeast(0))
      for (i in 0 until classDefsSize) {
        val classDefOff = classDefsOff + i * 32
        if (classDefOff + 4 > dexBytes.size) break
        val typeIdx = buf.getInt(classDefOff)
        if (typeIdx < 0 || typeIdx >= typeIdsSize) continue
        val stringIdx = buf.getInt(typeIdsOff + typeIdx * 4)
        if (stringIdx < 0 || stringIdx >= stringIdsSize) continue
        val stringDataOff = buf.getInt(stringIdsOff + stringIdx * 4)
        val descriptor = readDexString(dexBytes, stringDataOff) ?: continue
        if (descriptor.startsWith("L") && descriptor.endsWith(";")) {
          names += descriptor.substring(1, descriptor.length - 1).replace('/', '.')
        }
      }
      return names
    }

    fun readDexString(bytes: ByteArray, offset: Int): String? {
      if (offset < 0 || offset >= bytes.size) return null
      var pos = offset
      // Skip ULEB128 UTF-16 code unit count header.
      while (pos < bytes.size) {
        val b = bytes[pos++].toInt() and 0xFF
        if ((b and 0x80) == 0) break
      }
      val start = pos
      while (pos < bytes.size && bytes[pos].toInt() != 0) {
        pos++
      }
      return String(bytes, start, pos - start, Charsets.UTF_8)
    }
  }
}
