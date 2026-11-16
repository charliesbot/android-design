package bot.charlies.androiddesign.preview.domain

/** Date picker display variants distinguishing docked dropdown pickers from modal pickers. */
object DatePickerSpec {
  enum class Variant(val wire: String) {
    Modal("modal"),
    ModalInput("modal-input"),
    Docked("docked"),
    FullScreen("full-screen"),
  }
}
