package android.provider

import android.content.ContentResolver
import android.net.Uri

/** Every setting reads as its default, unless the resolver was given a value for it. */
object Settings {
  object Global {
    fun getUriFor(name: String): Uri = Uri()

    fun getInt(resolver: ContentResolver, name: String, default: Int): Int =
      resolver.settings[name] ?: default
  }

  object System {
    const val USER_ROTATION = "user_rotation"

    fun getInt(resolver: ContentResolver, name: String, default: Int): Int = default
  }
}
