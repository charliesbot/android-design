package androidx.core.util

fun interface Predicate<T> {
  fun test(t: T): Boolean
}
