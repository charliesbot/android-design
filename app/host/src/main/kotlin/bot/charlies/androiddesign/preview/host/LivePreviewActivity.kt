package bot.charlies.androiddesign.preview.host

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import bot.charlies.androiddesign.preview.feature.preview.theme.ProposalFonts
import bot.charlies.androiddesign.preview.host.sandbox.GuardedRecomposer
import bot.charlies.androiddesign.preview.host.sandbox.LivePreviewState
import bot.charlies.androiddesign.preview.host.sandbox.LiveSession
import bot.charlies.androiddesign.preview.host.sandbox.RenderForegroundService
import bot.charlies.androiddesign.preview.kit.BoardContent
import bot.charlies.androiddesign.preview.kit.BoardSurface
import bot.charlies.androiddesign.preview.model.WearSurface
import java.lang.ref.WeakReference

/**
 * Zero-chrome full-bleed interactive viewer running inside `:sandbox`. Hoists [LivePreviewState]
 * outside `key(session.dexHash)` so shell controls survive edits without cross-classloader casts.
 */
class LivePreviewActivity : ComponentActivity() {
  private var guard: GuardedRecomposer? = null
  private val failure = mutableStateOf<LiveFailure?>(null)
  private val mainHandler = Handler(Looper.getMainLooper())

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    instanceRef = WeakReference(this)
    enableEdgeToEdge()
    ProposalFonts.init(assets)
    startForegroundService(Intent(this, RenderForegroundService::class.java))
    installContent()
  }

  /**
   * Hosts the live view in its own [GuardedRecomposer] so a throwing user composable shows
   * [FailurePlaceholder] instead of killing `:sandbox`. A cancelled recomposer cannot recompose, so
   * a failure reinstalls the content with a fresh one; the placeholder stays until the next session
   * (a new DEX hash) arrives.
   */
  private fun installContent() {
    val previous = guard
    val next = GuardedRecomposer { t -> mainHandler.post { onLiveFailure(t) } }
    guard = next
    val view =
      ComposeView(this).apply {
        setParentCompositionContext(next.recomposer)
        setContent { LiveRoot(failure.value) }
      }
    setContentView(view)
    previous?.cancel()
  }

  private fun onLiveFailure(t: Throwable) {
    if (isDestroyed) return
    failure.value =
      LiveFailure(
        dexHash = LivePreviewState.session.value?.dexHash,
        title = t::class.java.name,
        message = t.message.orEmpty(),
      )
    installContent()
  }

  override fun onDestroy() {
    guard?.cancel()
    guard = null
    mainHandler.removeCallbacksAndMessages(null)
    if (instanceRef?.get() === this) {
      instanceRef = null
    }
    super.onDestroy()
  }

  companion object {
    @Volatile private var instanceRef: WeakReference<LivePreviewActivity>? = null

    internal fun currentDecorView(): View? = instanceRef?.get()?.window?.decorView

    internal fun currentWindowToken(): IBinder? = instanceRef?.get()?.window?.decorView?.windowToken
  }
}

/** A live-view failure pinned to the session (DEX hash) that caused it. */
internal class LiveFailure(val dexHash: String?, val title: String, val message: String) {
  fun appliesTo(session: LiveSession): Boolean = dexHash == session.dexHash
}

@Composable
private fun LiveRoot(failure: LiveFailure?) {
  val current = LivePreviewState.session.value
  when {
    current == null ->
      Box(
        modifier = Modifier.fillMaxSize().background(Color.Black),
        contentAlignment = Alignment.Center,
      ) {
        Text("Host Preview Ready", color = Color.White)
      }
    failure != null && failure.appliesTo(current) -> FailurePlaceholder(failure)
    else ->
      key(current.dexHash) {
        LiveBoardCanvas(current)
      }
  }
}

@Composable
private fun FailurePlaceholder(failure: LiveFailure) {
  Box(
    modifier = Modifier.fillMaxSize().background(Color(0xFF3B0A0A)).padding(24.dp),
    contentAlignment = Alignment.Center,
  ) {
    Column {
      Text("Preview failed", color = Color.White, fontWeight = FontWeight.Bold)
      Text(failure.title, color = Color.White)
      if (failure.message.isNotBlank()) {
        Text(failure.message, color = Color.White)
      }
    }
  }
}

@Composable
private fun LiveBoardCanvas(session: LiveSession) {
  val baseDensity = LocalDensity.current
  val baseConfig = LocalConfiguration.current
  val effectiveDensity = Density(baseDensity.density, fontScale = 1f)
  val effectiveConfig =
    remember(baseConfig, session.dark, session.board.surface) {
      Configuration(baseConfig).apply {
        uiMode =
          (uiMode and Configuration.UI_MODE_NIGHT_MASK.inv()) or
            if (session.dark) Configuration.UI_MODE_NIGHT_YES else Configuration.UI_MODE_NIGHT_NO
        if (session.board.surface == BoardSurface.Wear) {
          screenLayout =
            (screenLayout and Configuration.SCREENLAYOUT_ROUND_MASK.inv()) or
              Configuration.SCREENLAYOUT_ROUND_YES
        }
      }
    }
  val widthDp =
    if (session.board.surface == BoardSurface.Wear) {
      WearSurface.views.first().width
    } else {
      baseConfig.screenWidthDp.coerceAtLeast(320)
    }
  val heightDp =
    if (session.board.surface == BoardSurface.Wear) {
      WearSurface.views.first().height
    } else {
      baseConfig.screenHeightDp.coerceAtLeast(480)
    }

  CompositionLocalProvider(
    LocalDensity provides effectiveDensity,
    LocalConfiguration provides effectiveConfig,
  ) {
    BoardContent(
      board = session.board,
      direction = session.direction,
      screenId = session.screenId,
      frameWidthDp = widthDp,
      frameHeightDp = heightDp,
      fillContainer = true,
      dark = session.dark,
      roundClip = false,
      designSystem = session.designSystem,
      explorationDir = null,
      overlayId = session.overlayId,
      assetResolver = session.assetResolver,
      tagPanesForInspection = false,
    )
  }
}
