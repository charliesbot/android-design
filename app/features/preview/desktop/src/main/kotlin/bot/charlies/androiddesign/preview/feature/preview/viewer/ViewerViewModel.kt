package bot.charlies.androiddesign.preview.feature.preview.viewer

import bot.charlies.androiddesign.preview.domain.ProposalSnapshot
import bot.charlies.androiddesign.preview.domain.design.DesignSystemResolver
import bot.charlies.androiddesign.preview.feature.preview.render.ProposalHostState
import bot.charlies.androiddesign.preview.model.Catalog
import bot.charlies.androiddesign.preview.model.Catalogs
import bot.charlies.androiddesign.preview.model.DesignSystem
import bot.charlies.androiddesign.preview.model.Device
import bot.charlies.androiddesign.preview.model.M3ExpressiveCatalog
import bot.charlies.androiddesign.preview.model.PaletteVariant
import bot.charlies.androiddesign.preview.model.ValidationError
import bot.charlies.androiddesign.preview.model.ViewSpec
import bot.charlies.androiddesign.preview.model.Views
import bot.charlies.androiddesign.preview.model.located
import java.nio.file.Path
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** The viewer's side panel presets: a seed color the viewer can try on the proposal. */
data class SeedPreset(val name: String, val seed: String)

object ViewerPresets {
  val seeds: List<SeedPreset> =
    listOf(
      SeedPreset("Purple", "#6750A4"),
      SeedPreset("Blue", "#0061A4"),
      SeedPreset("Green", "#386A20"),
      SeedPreset("Coral", "#984061"),
      SeedPreset("Amber", "#8B5000"),
      SeedPreset("Teal", "#006A6A"),
    )

  /** The color variants the Style section offers. */
  val variants: List<PaletteVariant> =
    listOf(
      PaletteVariant.TonalSpot,
      PaletteVariant.Neutral,
      PaletteVariant.Vibrant,
      PaletteVariant.Expressive,
      PaletteVariant.Monochrome,
    )

  fun label(variant: PaletteVariant): String =
    variant.wire.replace('-', ' ').replaceFirstChar { it.uppercase() }
}

/**
 * What the viewer shows.
 *
 * The viewer keeps the last good render: [host], [design] and [screens] only change when a valid
 * proposal arrives, while [errors] always follows the newest file. A viewer choice (device,
 * brightness, seed, variant) is never written to the proposal.
 */
data class ViewerState(
  val path: Path? = null,
  val name: String? = null,
  val screens: List<String> = emptyList(),
  /**
   * The proposal's own design system (DESIGN.md merged with its theme), from the last good load.
   */
  val design: DesignSystem? = null,
  /** The running proposal of the last good load; null until a valid proposal arrives. */
  val host: ProposalHostState? = null,
  val errors: List<ValidationError> = emptyList(),
  val device: Device = Device.Phone,
  val dark: Boolean = false,
  /** Which watch view the window shows while [device] is a watch: the large one, or the small. */
  val watchLarge: Boolean = false,
  /** `#RRGGBB` the viewer overrides the proposal's seed with, or null. */
  val seed: String? = null,
  val variant: PaletteVariant? = null,
) {
  /** The catalog of the open proposal, whose views the window offers; the phone's before one. */
  val catalog: Catalog
    get() = host?.proposal?.let(Catalogs::of) ?: M3ExpressiveCatalog

  val view: ViewSpec
    get() =
      when {
        device != Device.Watch -> Views.of(device, dark)
        watchLarge -> Views.WatchLarge
        else -> Views.Watch
      }

  /** The design system the viewer draws with: the proposal's, with the viewer's color choices. */
  val effectiveDesign: DesignSystem?
    get() =
      design?.copy(
        seed = seed ?: design.seed,
        // A seed the viewer picks themes a Wear proposal that had none.
        seeded = design.seeded || seed != null,
        variant = variant ?: design.variant,
      )

  /** The variant the viewer shows: its own choice, else the proposal's, else the default. */
  val effectiveVariant: PaletteVariant
    get() = variant ?: design?.variant ?: PaletteVariant.TonalSpot

  val hasProposal: Boolean
    get() = host != null

  val valid: Boolean
    get() = errors.isEmpty()
}

/** The default design system with [seed] and [variant] swapped in where given. */
internal fun defaultDesign(seed: String?, variant: PaletteVariant?): DesignSystem =
  DesignSystemResolver.resolve(null, null).let { base ->
    base.copy(seed = seed ?: base.seed, variant = variant ?: base.variant)
  }

/** What the viewer's controls can do. [ViewerViewModel] is the implementation. */
interface ViewerActions {
  fun setDevice(device: Device)

  /** Shows [view], one of the views of the open proposal's catalog. */
  fun setView(view: ViewSpec)

  fun setDark(dark: Boolean)

  /** [seed] is `#RRGGBB`, or null to go back to the proposal's own. */
  fun setSeed(seed: String?)

  fun setVariant(variant: PaletteVariant?)

  /** Goes to [screen] the way a navigation from the start would. */
  fun selectScreen(screen: String)
}

/**
 * State of the live viewer: the proposal being shown and the viewer's own choices. Methods are
 * plain and synchronous, so it is driven the same way from the window and from the MCP tools.
 */
class ViewerViewModel : ViewerActions {
  companion object {
    /** The path of an error that is about drawing the proposal, not about the file's content. */
    const val RENDER_ERROR_PATH = "render"
  }

  private val mutableState = MutableStateFlow(ViewerState())
  val state: StateFlow<ViewerState> = mutableState.asStateFlow()

  /** The screen the live preview is on, or null before a proposal loaded. */
  val currentScreen: String?
    get() = mutableState.value.host?.current

  /**
   * Shows a new reading of the proposal file. A valid one replaces the picture (keeping the
   * navigation and local state when it is the same file); an invalid one only reports its errors.
   */
  @Synchronized
  fun show(snapshot: ProposalSnapshot) {
    val state = mutableState.value
    val base =
      if (state.path == snapshot.path) state
      else ViewerState(device = state.device, dark = state.dark)
    val proposal = snapshot.proposal
    if (proposal == null || snapshot.errors.isNotEmpty()) {
      mutableState.value = base.copy(path = snapshot.path, errors = snapshot.errors)
      return
    }
    val dir = snapshot.path.parent
    val host = base.host?.also { it.update(proposal, dir) } ?: ProposalHostState(proposal, dir)
    // A proposal draws in the views of its own catalog, so a phone proposal never lands on the
    // watch the previous one was on, nor the reverse.
    val views = Catalogs.of(proposal).views
    val device = if (views.any { it.device == base.device }) base.device else views.first().device
    mutableState.value =
      base.copy(
        device = device,
        path = snapshot.path,
        name = proposal.name ?: snapshot.designSystem.name,
        screens = proposal.screens.keys.toList(),
        design = snapshot.designSystem,
        host = host,
        errors = emptyList(),
      )
  }

  /**
   * The preview frame threw while drawing [error]. Reports it as an error like a validation one,
   * which takes the dead frame down. The next valid proposal clears it and the frame is built
   * again, on the same host, so the owner's place in the proposal survives the fix.
   */
  @Synchronized
  fun frameFailed(error: Throwable) {
    // The full stack goes to stderr, which MCP leaves free; the error names the top frames.
    error.printStackTrace()
    val message = "the window failed to draw the proposal: ${error.located()}"
    mutableState.value =
      mutableState.value.copy(errors = listOf(ValidationError(RENDER_ERROR_PATH, message)))
  }

  override fun setDevice(device: Device) = change { state ->
    if (state.catalog.views.any { it.device == device }) state.copy(device = device) else state
  }

  override fun setView(view: ViewSpec) = change { state ->
    when {
      state.catalog.views.none { it.name == view.name } -> state
      view.device == Device.Watch ->
        state.copy(device = view.device, watchLarge = view.name == Views.WatchLarge.name)
      else -> state.copy(device = view.device)
    }
  }

  override fun setDark(dark: Boolean) = change { it.copy(dark = dark) }

  override fun setSeed(seed: String?) = change { it.copy(seed = seed) }

  override fun setVariant(variant: PaletteVariant?) = change { it.copy(variant = variant) }

  @Synchronized
  private fun change(transform: (ViewerState) -> ViewerState) {
    mutableState.value = transform(mutableState.value)
  }

  override fun selectScreen(screen: String) {
    mutableState.value.host?.showScreen(screen)
  }
}
