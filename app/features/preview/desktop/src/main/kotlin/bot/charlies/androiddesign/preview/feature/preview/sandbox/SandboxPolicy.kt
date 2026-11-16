package bot.charlies.androiddesign.preview.feature.preview.sandbox

/**
 * Shared class and member allowlist/denylist enforced both statically in [BytecodeGuard] (before
 * defining any class) and dynamically in [SandboxClassLoader].
 */
internal object SandboxPolicy {
  private val ALLOWED_PREFIXES =
    listOf(
      "bot.charlies.androiddesign.preview.kit.",
      "androidx.compose.",
      "androidx.navigation3.",
      "androidx.wear.compose.",
      "androidx.graphics.shapes.",
      "androidx.window.core.layout.",
      "androidx.lifecycle.",
      "com.materialkolor.",
      "kotlinx.collections.immutable.",
      "kotlinx.coroutines.",
      "kotlin.",
      "org.jetbrains.annotations.",
      "java.util.function.",
      "java.util.concurrent.atomic.",
    )

  private val ALLOWED_EXACT =
    setOf(
      "bot.charlies.androiddesign.preview.model.BundledFont",
      "bot.charlies.androiddesign.preview.model.PaletteVariant",
      "java.lang.invoke.LambdaMetafactory",
      "java.lang.invoke.StringConcatFactory",
      "java.lang.invoke.CallSite",
      "java.lang.invoke.MethodType",
      "java.lang.invoke.MethodHandle",
      "java.lang.invoke.MethodHandles",
      "java.lang.invoke.MethodHandles\$Lookup",
      "java.lang.invoke.SerializedLambda",
    )

  val DENIED_CLASS_REFS =
    setOf(
      "java.lang.invoke.MethodHandles",
      "java.lang.invoke.MethodHandles\$Lookup",
      "java.lang.invoke.MethodHandle",
      "java.lang.invoke.VarHandle",
      "java.lang.invoke.CallSite",
      "java.lang.invoke.MethodType",
    )

  private val ALLOWED_JAVA_LANG =
    setOf(
      "Object",
      "String",
      "CharSequence",
      "StringBuilder",
      "StringBuffer",
      "Boolean",
      "Byte",
      "Short",
      "Integer",
      "Long",
      "Float",
      "Double",
      "Number",
      "Character",
      "Void",
      "Enum",
      "Math",
      "StrictMath",
      "Comparable",
      "Iterable",
      "Class",
      "SuppressWarnings",
      "Deprecated",
      "Override",
      "SafeVarargs",
      "FunctionalInterface",
      "AutoCloseable",
      "Cloneable",
      "Throwable",
      "Exception",
      "RuntimeException",
      "Error",
      "AssertionError",
      "LinkageError",
      "IncompatibleClassChangeError",
      "NoSuchFieldError",
      "NoSuchMethodError",
      "AbstractMethodError",
      "NoClassDefFoundError",
      "ExceptionInInitializerError",
      "IllegalArgumentException",
      "IllegalStateException",
      "IndexOutOfBoundsException",
      "NoSuchElementException",
      "NullPointerException",
      "NumberFormatException",
      "UnsupportedOperationException",
      "ArithmeticException",
      "ClassCastException",
      "ArrayIndexOutOfBoundsException",
      "StringIndexOutOfBoundsException",
      "CloneNotSupportedException",
    )

  private val ALLOWED_JAVA_UTIL =
    setOf(
      "List",
      "Map",
      "Map\$Entry",
      "Set",
      "SortedSet",
      "SortedMap",
      "NavigableSet",
      "NavigableMap",
      "Queue",
      "Deque",
      "Collection",
      "Iterable",
      "Iterator",
      "ListIterator",
      "Spliterator",
      "Enumeration",
      "RandomAccess",
      "AbstractCollection",
      "AbstractList",
      "AbstractMap",
      "AbstractSet",
      "ArrayList",
      "LinkedList",
      "ArrayDeque",
      "HashMap",
      "LinkedHashMap",
      "TreeMap",
      "EnumMap",
      "HashSet",
      "LinkedHashSet",
      "TreeSet",
      "EnumSet",
      "Collections",
      "Arrays",
      "Objects",
      "Optional",
      "OptionalInt",
      "OptionalLong",
      "OptionalDouble",
      "Comparator",
      "Locale",
      "UUID",
      "Random",
      "NoSuchElementException",
      "ConcurrentModificationException",
    )

  private val DENIED_PREFIXES =
    listOf(
      "kotlin.io.",
      "kotlin.system.",
      "kotlin.reflect.",
      "kotlin.concurrent.",
      "kotlinx.coroutines.ThreadPoolDispatcherKt",
      "kotlinx.coroutines.ExecutorCoroutineDispatcher",
      "kotlinx.coroutines.ExecutorsKt",
      "androidx.compose.ui.text.platform.",
      "androidx.compose.ui.window.WindowKt",
      "androidx.compose.ui.window.ApplicationKt",
      "androidx.compose.ui.window.TrayKt",
      "androidx.compose.ui.window.DialogWindowKt",
      "java.io.",
      "java.nio.",
      "java.net.",
      "java.lang.reflect.",
      "java.security.",
      "javax.",
      "sun.",
      "jdk.",
      "org.jetbrains.skia.",
      "org.jetbrains.skiko.",
    )

  val DENIED_COROUTINE_MEMBERS = setOf("newSingleThreadContext", "newFixedThreadPoolContext")

  val DENIED_INVOKE_DIRECT_OWNERS =
    setOf(
      "java.lang.invoke.MethodHandles",
      "java.lang.invoke.MethodHandles\$Lookup",
      "java.lang.invoke.MethodHandle",
      "java.lang.invoke.VarHandle",
    )

  val ALLOWED_BOOTSTRAP_OWNERS =
    setOf("java.lang.invoke.LambdaMetafactory", "java.lang.invoke.StringConcatFactory")

  val DENIED_CLASS_METHODS =
    setOf(
      "forName",
      "getClassLoader",
      "getResource",
      "getResourceAsStream",
      "getMethod",
      "getMethods",
      "getDeclaredMethod",
      "getDeclaredMethods",
      "getField",
      "getFields",
      "getDeclaredField",
      "getDeclaredFields",
      "getConstructor",
      "getConstructors",
      "getDeclaredConstructor",
      "getDeclaredConstructors",
      "newInstance",
      "getClasses",
      "getDeclaredClasses",
      "getModule",
      "getPackage",
      "getProtectionDomain",
      "getSigners",
      "getNestMembers",
      "getRecordComponents",
      "getAnnotatedInterfaces",
    )

  fun isAllowedClass(dotName: String, siblingClasses: Set<String> = emptySet()): Boolean {
    if (dotName in siblingClasses) return true
    if (DENIED_PREFIXES.any { dotName.startsWith(it) || dotName == it.removeSuffix(".") }) {
      return false
    }
    if (dotName in ALLOWED_EXACT) return true
    if (dotName.startsWith("java.lang.")) {
      val simple = dotName.removePrefix("java.lang.")
      return !simple.contains('.') && simple in ALLOWED_JAVA_LANG
    }
    if (dotName.startsWith("java.util.")) {
      val rest = dotName.removePrefix("java.util.")
      if (!rest.contains('.')) return rest in ALLOWED_JAVA_UTIL
      return dotName.startsWith("java.util.function.") ||
        dotName.startsWith("java.util.concurrent.atomic.")
    }
    return ALLOWED_PREFIXES.any { dotName.startsWith(it) }
  }
}
