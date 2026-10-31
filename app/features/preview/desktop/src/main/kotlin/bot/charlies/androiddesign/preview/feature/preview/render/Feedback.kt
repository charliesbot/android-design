package bot.charlies.androiddesign.preview.feature.preview.render

import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.LoadingIndicator
import androidx.compose.runtime.Composable

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun RenderLoadingIndicator(ctx: NodeContext) {
  if (ctx.bool("contained")) ContainedLoadingIndicator(ctx.modifier())
  else LoadingIndicator(ctx.modifier())
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun RenderProgressIndicator(ctx: NodeContext) {
  val progress = ctx.number("progress")
  val wavy = ctx.bool("wavy")
  if (ctx.enum("shape", "linear") == "circular") {
    val modifier = ctx.modifier()
    when {
      wavy && progress != null -> CircularWavyProgressIndicator({ progress }, modifier)
      wavy -> CircularWavyProgressIndicator(modifier)
      progress != null -> CircularProgressIndicator({ progress }, modifier)
      else -> CircularProgressIndicator(modifier)
    }
  } else {
    val modifier = ctx.modifier(fillWidth = true)
    when {
      wavy && progress != null -> LinearWavyProgressIndicator({ progress }, modifier)
      wavy -> LinearWavyProgressIndicator(modifier)
      progress != null -> LinearProgressIndicator({ progress }, modifier)
      else -> LinearProgressIndicator(modifier)
    }
  }
}
