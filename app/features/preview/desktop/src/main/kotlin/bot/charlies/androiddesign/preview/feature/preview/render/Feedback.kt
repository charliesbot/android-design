package bot.charlies.androiddesign.preview.feature.preview.render

import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.LoadingIndicator
import androidx.compose.runtime.Composable
import bot.charlies.androiddesign.preview.model.ColorRole

internal fun NodeContext.loadingIndicatorLook(ambient: Ambient) =
  if (bool("contained")) {
    Look(ColorRole.SecondaryContainer, ColorRole.Primary, shape = "full")
  } else {
    Look(ambient.container, ColorRole.Primary)
  }

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun RenderLoadingIndicator(ctx: NodeContext) {
  if (ctx.bool("contained")) ContainedLoadingIndicator(ctx.modifier())
  else LoadingIndicator(ctx.modifier())
}

internal fun NodeContext.progressIndicatorLook(ambient: Ambient) =
  Look(ambient.container, ColorRole.Primary)

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun RenderProgressIndicator(ctx: NodeContext) {
  val progress = ctx.number("progress")
  val wavy = ctx.bool("wavy")
  if (ctx.enum("shape") == "circular") {
    val modifier = ctx.modifier()
    when {
      wavy && progress != null -> CircularWavyProgressIndicator({ progress }, modifier)
      wavy -> CircularWavyProgressIndicator(modifier)
      progress != null -> CircularProgressIndicator({ progress }, modifier)
      else -> CircularProgressIndicator(modifier)
    }
  } else {
    val modifier = ctx.modifier()
    when {
      wavy && progress != null -> LinearWavyProgressIndicator({ progress }, modifier)
      wavy -> LinearWavyProgressIndicator(modifier)
      progress != null -> LinearProgressIndicator({ progress }, modifier)
      else -> LinearProgressIndicator(modifier)
    }
  }
}
