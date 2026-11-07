package bot.charlies.androiddesign.preview.feature.preview.render

import androidx.compose.runtime.Composable

/** A float range kept as a concrete class: a generic range cannot be type-checked on read. */
internal class FloatSpan(val start: Float, val end: Float)

/** A value that lives in the [InteractionState] under this node, starting at the prop's value. */
internal class LocalValue<T : Any>(val value: T, val set: (T) -> Unit)

@Composable
internal inline fun <reified T : Any> NodeContext.local(key: String, initial: T): LocalValue<T> {
  val stateKey = env.stateKey("$id:$key")
  val interaction = env.host.interaction
  val current: T = interaction.get<T>(stateKey) ?: initial
  return LocalValue(current) { interaction[stateKey] = it }
}

/** The `checked` state of a toggle, starting at the prop. */
@Composable
internal fun NodeContext.checkedState(key: String = "checked"): LocalValue<Boolean> =
  local(key, bool(key))

/** The index picked in [group] by a tap on one of its items, or null before the first tap. */
internal fun NodeContext.groupSelected(group: String): Int? =
  env.host.interaction.get<Int>(env.stateKey("selection:$group"))

/**
 * Whether this item is the picked one of [group]: after a tap the tapped item, before it the one
 * whose `selected` prop is set.
 */
internal fun NodeContext.isPickedIn(group: String): Boolean =
  groupSelected(group)?.let { it == host.groupIndex } ?: bool("selected")

/** Makes this item the picked one of [group]. */
internal fun NodeContext.selectInGroup(group: String) {
  env.host.interaction[env.stateKey("selection:$group")] = host.groupIndex
}

/** A stored `local` boolean read outside composition, such as when the tree is described. */
internal fun NodeContext.storedBool(key: String): Boolean =
  env.host.interaction.get<Boolean>(env.stateKey("$id:$key")) ?: bool(key)
