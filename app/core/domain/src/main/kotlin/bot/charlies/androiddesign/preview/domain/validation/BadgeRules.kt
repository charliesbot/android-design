package bot.charlies.androiddesign.preview.domain.validation

import bot.charlies.androiddesign.preview.domain.BadgeContent
import bot.charlies.androiddesign.preview.domain.validation.Dynamic.asString
import bot.charlies.androiddesign.preview.domain.validation.Dynamic.isBoolean
import bot.charlies.androiddesign.preview.domain.validation.Dynamic.isNumber
import bot.charlies.androiddesign.preview.model.PropKind
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive

/** What a `badge` literal may be: the rule of a component's `badge` prop and of a destination's. */
internal object BadgeRules {
  /**
   * Why [value] is not a badge, or null when it is one: `true`, `false`, a number, or text of at
   * most [PropKind.BADGE_MAX_CHARACTERS] characters. [bindingNote] ends the message for a value
   * that is none of those, naming what else the caller accepts.
   */
  fun problem(value: JsonElement, bindingNote: String = " or a binding"): String? {
    val primitive = value as? JsonPrimitive
    if (
      primitive == null || (!value.isBoolean() && !value.isNumber() && value.asString() == null)
    ) {
      return "must be true, false, a number, a string$bindingNote"
    }
    val label = (BadgeContent.of(primitive) as? BadgeContent.Large)?.label ?: return null
    if (label.length > PropKind.BADGE_MAX_CHARACTERS) {
      return "a badge label holds at most ${PropKind.BADGE_MAX_CHARACTERS} characters, " +
        "a + included (such as \"99+\"), got '$label'"
    }
    return null
  }
}
