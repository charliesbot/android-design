package android.os

object Build {
  object VERSION {
    // Low on purpose: every `SDK_INT >= ...` branch upstream guards an Android-only API.
    private const val DEFAULT_SDK = 30

    /** Raised on one thread, for the length of one call, by [withSdk]. */
    private val raised = ThreadLocal<Int?>()

    val SDK_INT: Int
      get() = raised.get() ?: DEFAULT_SDK

    /**
     * Runs [block] as if on API [sdk], for the one upstream function that is only available from
     * there and has no Android-only calls on its path (`dynamicColorScheme`).
     */
    fun <T> withSdk(sdk: Int, block: () -> T): T {
      val before = raised.get()
      raised.set(sdk)
      try {
        return block()
      } finally {
        raised.set(before)
      }
    }
  }

  object VERSION_CODES {
    const val R = 30
    const val S = 31
    const val TIRAMISU = 33
    const val UPSIDE_DOWN_CAKE = 34
    const val VANILLA_ICE_CREAM = 35
    const val BAKLAVA = 36
  }

  const val MANUFACTURER = "desktop"
  const val MODEL = "desktop"
}

class Looper private constructor() {
  companion object {
    private val main = Looper()

    fun getMainLooper(): Looper = main
  }
}

class Handler

class Bundle
