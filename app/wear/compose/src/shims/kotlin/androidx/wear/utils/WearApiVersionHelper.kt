package androidx.wear.utils

/** The desktop pretends to be an older Wear platform, so version-gated paths stay off. */
object WearApiVersionHelper {
  const val WEAR_CINNAMON_BUN_0 = 0
  const val WEAR_CINNAMON_BUN_2 = 2

  fun isApiVersionAtLeast(version: Int): Boolean = false
}
