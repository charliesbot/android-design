package androidx.compose.ui.res

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.wear.compose.material3.WearStrings

@Composable
@ReadOnlyComposable
fun stringResource(id: Int): String = WearStrings.strings[id] ?: error("no string $id")

@Composable
@ReadOnlyComposable
fun stringResource(id: Int, vararg formatArgs: Any): String =
  String.format(WearStrings.strings[id] ?: error("no string $id"), *formatArgs)

@Composable
@ReadOnlyComposable
fun pluralStringResource(id: Int, count: Int, vararg formatArgs: Any): String {
  val (one, other) = WearStrings.plurals[id] ?: error("no plural $id")
  return String.format(if (count == 1) one else other, *formatArgs)
}
