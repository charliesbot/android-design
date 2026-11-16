package bot.charlies.androiddesign.preview.kit

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.Clipboard
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.text.AnnotatedString
import java.nio.file.Path

internal object InertUriHandler : UriHandler {
  override fun openUri(uri: String) = Unit
}

internal object InertClipboard : Clipboard {
  override suspend fun getClipEntry(): ClipEntry? = null

  override suspend fun setClipEntry(clipEntry: ClipEntry?) = Unit

  override val nativeClipboard: Any
    get() = Unit
}

internal object InertClipboardManager : ClipboardManager {
  override fun setText(annotatedString: AnnotatedString) = Unit

  override fun getText(): AnnotatedString? = null

  override fun hasText(): Boolean = false
}

/**
 * Replaces side-effectful Compose UI locals ([LocalUriHandler], [LocalClipboard],
 * [LocalClipboardManager]) with no-op implementations and binds [explorationDir] for
 * [designPainter].
 */
@Composable
internal fun ProvideInertLocals(explorationDir: Path?, content: @Composable () -> Unit) {
  CompositionLocalProvider(
    LocalExplorationDir provides explorationDir,
    LocalUriHandler provides InertUriHandler,
    LocalClipboard provides InertClipboard,
    LocalClipboardManager provides InertClipboardManager,
    content = content,
  )
}
