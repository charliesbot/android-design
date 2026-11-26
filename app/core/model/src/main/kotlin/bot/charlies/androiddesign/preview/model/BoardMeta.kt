package bot.charlies.androiddesign.preview.model

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable

/** Overlay declared inside an exploration direction and attached to a target screen. */
@Serializable data class OverlayMeta(val id: String, val screen: String? = null)

/** Metadata for one design direction, its screens, and its overlays. */
@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class DirectionMeta(
  val name: String,
  val surface: String,
  val startScreen: String,
  val screens: List<String>,
  @EncodeDefault(EncodeDefault.Mode.ALWAYS) val overlays: List<OverlayMeta> = emptyList(),
) {
  fun overlaysFor(screenId: String): List<OverlayMeta> = overlays.filter {
    (it.screen ?: startScreen) == screenId
  }
}

/** Canonical board structure metadata shared by the render worker, server, and browser board. */
@Serializable
data class BoardMeta(
  val surface: String = "phone",
  val boardName: String? = null,
  val directions: List<DirectionMeta>,
  val views: List<ViewSpec>,
)
