package bot.charlies.androiddesign.preview.feature.preview.render

import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import bot.charlies.androiddesign.preview.domain.BadgeContent
import kotlinx.serialization.json.JsonPrimitive

/** The badge the `badge` prop asks for, or null for none. */
internal fun NodeContext.badgeContent(): BadgeContent? =
  BadgeContent.of(props.value("badge") as? JsonPrimitive)

/** Draws [content] in a `BadgedBox` holding the node's badge, or alone when it has none. */
@Composable
internal fun NodeContext.WithBadge(content: @Composable () -> Unit) =
  WithBadge(badgeContent(), content)

/** Draws [content] in a `BadgedBox` holding [badge], or alone when it is null. */
@Composable
internal fun WithBadge(badge: BadgeContent?, content: @Composable () -> Unit) {
  when (badge) {
    null -> content()
    BadgeContent.Small -> BadgedBox(badge = { Badge() }) { content() }
    is BadgeContent.Large -> BadgedBox(badge = { Badge { Text(badge.label) } }) { content() }
  }
}
