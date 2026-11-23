package bot.charlies.androiddesign.preview.kit

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.Clipboard
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import java.nio.file.Path

internal object InertUriHandler : UriHandler {
  override fun openUri(uri: String) = Unit
}

internal object InertClipboard : Clipboard {
  override suspend fun getClipEntry(): ClipEntry? = null

  override suspend fun setClipEntry(clipEntry: ClipEntry?) = Unit

  override val nativeClipboard: android.content.ClipboardManager
    get() = throw UnsupportedOperationException("Inert clipboard in preview")
}

@Composable
internal fun ProvideInertLocals(
  explorationDir: Path?,
  assetResolver: ((String) -> ImageBitmap?)? = null,
  content: @Composable () -> Unit,
) {
  CompositionLocalProvider(
    LocalExplorationDir provides explorationDir,
    LocalAssetResolver provides assetResolver,
    LocalUriHandler provides InertUriHandler,
    LocalClipboard provides InertClipboard,
    content = content,
  )
}
