package androidx.compose.ui.res

import androidx.compose.ui.graphics.vector.ImageVector
import androidx.wear.compose.material3.R
import androidx.wear.compose.material3.WearDrawables

/** The desktop twin of Android's `ImageVector.vectorResource`: the few drawables Wear M3 ships. */
fun ImageVector.Companion.vectorResource(id: Int): ImageVector =
  when (id) {
    R.drawable.wear_m3c_error -> WearDrawables.error
    else -> error("no vector drawable $id")
  }
