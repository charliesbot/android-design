package com.google.wear

/** The Wear SDK is not on the desktop: no feature is available, so no manager is ever created. */
object Sdk {
  const val FEATURE_WEAR_GESTURE_DETECTION = 1

  fun hasApiFeature(feature: Int): Boolean = false

  fun <T : Any> getWearManager(context: android.content.Context, cls: Class<T>): Any? = null
}
