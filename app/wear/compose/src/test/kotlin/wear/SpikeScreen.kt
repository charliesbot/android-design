// Test fixture: a Wear M3 screen built from upstream APIs only, with a fixed clock (10:09) so
// renders
// are comparable. It exercises scaffolds, TransformingLazyColumn, buttons, cards and the curved
// TimeText.
package wear

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.TransformingLazyColumnState
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.AppScaffold
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.EdgeButton
import androidx.wear.compose.material3.FilledTonalButton
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.ListHeader
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.SurfaceTransformation
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.TimeSource
import androidx.wear.compose.material3.TimeText
import androidx.wear.compose.material3.TitleCard
import androidx.wear.compose.material3.lazy.rememberTransformationSpec
import androidx.wear.compose.material3.lazy.transformedHeight

@Composable
fun SpikeScreen(
  state: TransformingLazyColumnState = rememberTransformingLazyColumnState(),
) {
  MaterialTheme {
    AppScaffold(
      timeText = {
        TimeText(
          timeSource =
            object : TimeSource {
              @Composable override fun currentTime() = "10:09"
            },
        )
      },
    ) {
      ScreenScaffold(
        scrollState = state,
        edgeButton = { EdgeButton(onClick = {}) { Text("More") } },
      ) { contentPadding ->
        val spec = rememberTransformationSpec()
        TransformingLazyColumn(state = state, contentPadding = contentPadding) {
          item {
            ListHeader(Modifier.fillMaxWidth().transformedHeight(this, spec)) { Text("Workouts") }
          }
          item {
            Button(
              onClick = {},
              modifier = Modifier.fillMaxWidth().transformedHeight(this, spec),
              transformation = SurfaceTransformation(spec),
              icon = { Icon(Icons.Filled.Favorite, contentDescription = null) },
              label = { Text("Outdoor run") },
              secondaryLabel = { Text("Last: 5.2 km") },
            )
          }
          item {
            FilledTonalButton(
              onClick = {},
              modifier = Modifier.fillMaxWidth().transformedHeight(this, spec),
              transformation = SurfaceTransformation(spec),
              label = { Text("Indoor cycle") },
            )
          }
          item {
            Button(
              onClick = {},
              modifier = Modifier.fillMaxWidth().transformedHeight(this, spec),
              transformation = SurfaceTransformation(spec),
              colors = ButtonDefaults.filledTonalButtonColors(),
              label = { Text("Strength") },
            )
          }
          item {
            TitleCard(
              onClick = {},
              title = { Text("Weekly goal") },
              time = { Text("Now") },
              modifier = Modifier.fillMaxWidth().transformedHeight(this, spec),
              transformation = SurfaceTransformation(spec),
            ) {
              Text("3 of 5 workouts done")
            }
          }
          item {
            Button(
              onClick = {},
              modifier = Modifier.fillMaxWidth().transformedHeight(this, spec),
              transformation = SurfaceTransformation(spec),
              label = { Text("Swim") },
            )
          }
        }
      }
    }
  }
}
