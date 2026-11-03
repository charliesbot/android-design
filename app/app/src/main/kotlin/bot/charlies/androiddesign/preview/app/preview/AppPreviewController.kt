package bot.charlies.androiddesign.preview.app.preview

import bot.charlies.androiddesign.preview.domain.OpenedProposal
import bot.charlies.androiddesign.preview.domain.PreviewController
import bot.charlies.androiddesign.preview.domain.PreviewException
import bot.charlies.androiddesign.preview.domain.PreviewStatus
import bot.charlies.androiddesign.preview.domain.ProposalCheck
import bot.charlies.androiddesign.preview.domain.ProposalRepository
import bot.charlies.androiddesign.preview.domain.ProposalSnapshot
import bot.charlies.androiddesign.preview.domain.ViewChange
import bot.charlies.androiddesign.preview.domain.ViewCheck
import bot.charlies.androiddesign.preview.domain.ViewState
import bot.charlies.androiddesign.preview.domain.catalog.CatalogJson
import bot.charlies.androiddesign.preview.domain.catalog.CatalogSummary
import bot.charlies.androiddesign.preview.domain.design.DesignSystemResolver
import bot.charlies.androiddesign.preview.domain.export.KotlinExporter
import bot.charlies.androiddesign.preview.feature.preview.headless.RenderSource
import bot.charlies.androiddesign.preview.feature.preview.viewer.ViewerViewModel
import bot.charlies.androiddesign.preview.model.DesignSystem
import bot.charlies.androiddesign.preview.model.LayoutTree
import bot.charlies.androiddesign.preview.model.M3ExpressiveCatalog
import bot.charlies.androiddesign.preview.model.PreviewJson
import bot.charlies.androiddesign.preview.model.Proposal
import bot.charlies.androiddesign.preview.model.ValidationError
import bot.charlies.androiddesign.preview.model.ViewSpec
import bot.charlies.androiddesign.preview.model.Views
import bot.charlies.androiddesign.preview.model.located
import java.nio.file.Files
import java.nio.file.Path
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.JsonObject

/**
 * Glues the proposal file, the viewer and the headless renderer into what the MCP tools call.
 *
 * The file on disk is the source of truth. A watcher keeps the window current, and every call that
 * reads the proposal reads the file again first, so a tool call right after an edit never sees the
 * old version, whatever the watcher's timing.
 *
 * @param scope Runs the file watcher.
 */
class AppPreviewController(
  private val repository: ProposalRepository,
  private val viewer: ViewerViewModel,
  private val window: PreviewWindow,
  private val renderer: TreeRenderer,
  private val scope: CoroutineScope,
  private val version: String,
) : PreviewController {
  private val applying = Mutex()
  private var watcher: Job? = null

  @Volatile private var current: Path? = null

  /** The newest reading of the file, as the repository gave it. */
  private var latestReading: ProposalSnapshot? = null

  /** What the window was told about [latestReading]: the reading itself, or its render error. */
  @Volatile private var lastShown: ProposalSnapshot? = null

  override suspend fun openProposal(path: String): OpenedProposal {
    val file = Path.of(path).toAbsolutePath().normalize()
    if (!Files.isRegularFile(file)) throw PreviewException("no such file: $file")

    watcher?.cancel()
    current = file
    latestReading = null
    lastShown = null
    val first = CompletableDeferred<ProposalSnapshot>()
    watcher = scope.launch {
      repository.watch(file).collect { snapshot ->
        first.complete(apply(snapshot))
      }
    }
    window.show()
    return opened(first.await())
  }

  override suspend fun getTree(screen: String?, view: String?): LayoutTree {
    val snapshot = validSnapshot()
    val proposal = checkNotNull(snapshot.proposal)
    val state = viewer.state.value
    // No arguments: what the window shows, with its open overlays and selections replayed.
    if (screen == null && view == null) {
      val host = state.host
      return renderer.render(
        sourceOf(proposal, snapshot, state.effectiveDesign ?: snapshot.designSystem),
        viewer.currentScreen ?: proposal.navigation.start,
        state.view,
        live = host,
      )
    }
    val screenId = screen ?: viewer.currentScreen ?: proposal.navigation.start
    if (screenId !in proposal.screens) {
      throw PreviewException(
        "no screen '$screenId'; the proposal has ${proposal.screens.keys.joinToString()}",
      )
    }
    val spec: ViewSpec = if (view == null) state.view else viewNamed(view)
    // The window's color choices go with the window's view; a named view is the proposal's own.
    val design =
      if (view == null) state.effectiveDesign ?: snapshot.designSystem else snapshot.designSystem
    return renderer.render(sourceOf(proposal, snapshot, design), screenId, spec)
  }

  override suspend fun setView(change: ViewChange): ViewState {
    val seed =
      change.seed?.let {
        DesignSystemResolver.parseHex(it)
          ?: throw PreviewException("seed must be #RRGGBB, got '$it'")
      }
    change.screen?.let { screen ->
      val proposal = checkNotNull(validSnapshot().proposal)
      if (screen !in proposal.screens) {
        throw PreviewException(
          "no screen '$screen'; the screens are ${proposal.screens.keys.joinToString()}",
        )
      }
    }
    change.device?.let(viewer::setDevice)
    change.dark?.let(viewer::setDark)
    seed?.let(viewer::setSeed)
    change.variant?.let(viewer::setVariant)
    change.screen?.let(viewer::selectScreen)
    return viewState()
  }

  override suspend fun showPreview() {
    window.show()
  }

  override suspend fun checkProposal(screens: List<String>?, views: List<String>?): ProposalCheck {
    val snapshot = validSnapshot()
    val proposal = checkNotNull(snapshot.proposal)
    val screenIds = screens.orEmpty().distinct().ifEmpty { proposal.screens.keys.toList() }
    screenIds
      .filter { it !in proposal.screens }
      .takeIf { it.isNotEmpty() }
      ?.let { unknown ->
        throw PreviewException(
          "no screen ${unknown.joinToString { "'$it'" }}; the proposal has ${proposal.screens.keys.joinToString()}",
        )
      }
    val specs = views.orEmpty().distinct().ifEmpty { Views.all.map { it.name } }.map(::viewNamed)
    val started = System.nanoTime()
    val results = screenIds.associateWith { screen ->
      specs.associate { view ->
        view.name to check(proposal, snapshot, screen, view)
      }
    }
    return ProposalCheck(
      pass = results.values.all { views -> views.values.all { it.pass } },
      ms = (System.nanoTime() - started) / NANOS_PER_MS,
      screens = results,
    )
  }

  /** One fresh headless render. A render that throws fails its view and not the whole check. */
  private suspend fun check(
    proposal: Proposal,
    snapshot: ProposalSnapshot,
    screen: String,
    view: ViewSpec,
  ): ViewCheck {
    val tree =
      try {
        renderer.render(sourceOf(proposal, snapshot), screen, view)
      } catch (e: CancellationException) {
        throw e
      } catch (e: Exception) {
        return ViewCheck.renderError(e)
      }
    return ViewCheck.of(proposal, tree)
  }

  /** What to draw [proposal] from: [snapshot]'s folder, and its design unless [design] is given. */
  private fun sourceOf(
    proposal: Proposal,
    snapshot: ProposalSnapshot,
    design: DesignSystem = snapshot.designSystem,
  ) = RenderSource(proposal, design, snapshot.path.parent)

  override fun getCatalog(components: List<String>?): String =
    if (components.isNullOrEmpty()) {
      CatalogSummary.render()
    } else {
      PreviewJson.compact.encodeToString(
        JsonObject.serializer(),
        CatalogJson.components(components),
      )
    }

  override suspend fun exportKotlin(screen: String?): String {
    val snapshot = validSnapshot()
    return KotlinExporter.export(checkNotNull(snapshot.proposal), snapshot.designSystem, screen)
  }

  override suspend fun status(): PreviewStatus {
    // A broken file is part of the status, not a failure of it.
    val snapshot = current?.let { reload(it) }
    return PreviewStatus(
      version = version,
      catalogId = M3ExpressiveCatalog.ID,
      proposal = current?.toString(),
      windowVisible = window.visible.value,
      screen = viewer.currentScreen,
      view = viewState(),
      // A draw failure of the window is not in the file's reading, so it comes from the viewer.
      errors = snapshot?.errors?.ifEmpty { viewer.state.value.errors }.orEmpty(),
    )
  }

  private fun viewState(): ViewState {
    val state = viewer.state.value
    return ViewState(state.device, state.dark, state.seed, state.variant?.wire)
  }

  private fun opened(snapshot: ProposalSnapshot): OpenedProposal {
    val proposal = snapshot.proposal
    return OpenedProposal(
      path = snapshot.path.toString(),
      name = proposal?.name ?: snapshot.designSystem.name.takeIf { proposal != null },
      start = proposal?.navigation?.start,
      screens = proposal?.screens?.keys?.toList().orEmpty(),
      designMd = snapshot.designMdPath?.toString(),
      valid = snapshot.isValid,
      errors = snapshot.errors,
    )
  }

  /** Reads the file again and shows it if it changed, whatever the watcher has seen so far. */
  private suspend fun reload(file: Path): ProposalSnapshot = apply(repository.load(file))

  /**
   * Hands [snapshot] to the window and returns what the window was told. A proposal that cannot be
   * drawn is handed over as invalid, so the window keeps the last good one.
   */
  private suspend fun apply(snapshot: ProposalSnapshot): ProposalSnapshot = applying.withLock {
    // A reading of a file that is no longer the open one is stale.
    if (snapshot.path != current) return@withLock snapshot
    val known = lastShown
    if (known != null && snapshot == latestReading) return@withLock known
    val shown = withRenderCheck(snapshot)
    latestReading = snapshot
    lastShown = shown
    viewer.show(shown)
    shown
  }

  /**
   * Draws a valid [snapshot] headlessly the way the window is about to show it, and returns it with
   * a render error instead if that throws. Composing a proposal in the window would stop the window
   * drawing if it threw, so a proposal that throws here is reported, not shown.
   *
   * It draws once: the screen the window is on, at the window's view, with the window's own state
   * on top (its back stack and the overlays it has open, which an edit keeps). Other screens and
   * other devices are not drawn here, because every save would pay for them. A throw there reaches
   * the owner when they go to it: the window's frame contains it and reports it through
   * [ViewerViewModel.frameFailed], which shows in `status` until the next good proposal.
   */
  private suspend fun withRenderCheck(snapshot: ProposalSnapshot): ProposalSnapshot {
    val proposal = snapshot.proposal
    if (proposal == null || snapshot.errors.isNotEmpty()) return snapshot
    val window = viewer.state.value
    val screen =
      viewer.currentScreen?.takeIf { it in proposal.screens } ?: proposal.navigation.start
    val failure =
      try {
        renderer.render(
          sourceOf(proposal, snapshot),
          screen,
          window.view,
          live = window.host,
        )
        null
      } catch (e: CancellationException) {
        throw e
      } catch (e: Exception) {
        ValidationError(
          ViewerViewModel.RENDER_ERROR_PATH,
          "screen '$screen' failed to render at ${window.view.name}: ${e.located()}",
        )
      }
    return if (failure == null) snapshot else snapshot.copy(errors = listOf(failure))
  }

  private suspend fun validSnapshot(): ProposalSnapshot {
    val file = current ?: throw PreviewException("no proposal is open; call open_proposal first")
    val snapshot = reload(file)
    if (!snapshot.isValid) throw PreviewException(invalidMessage(snapshot.errors))
    return snapshot
  }

  private fun viewNamed(name: String): ViewSpec =
    Views.byName(name)
      ?: throw PreviewException(
        "no view '$name'; the views are ${Views.all.joinToString { it.name }}",
      )

  private fun invalidMessage(errors: List<ValidationError>): String = buildString {
    append("the proposal is invalid; fix these and try again:")
    errors.take(MAX_ERRORS).forEach { append("\n- ${it.path}: ${it.message}") }
    if (errors.size > MAX_ERRORS) append("\n(and ${errors.size - MAX_ERRORS} more)")
  }

  private companion object {
    const val MAX_ERRORS = 20
    const val NANOS_PER_MS = 1_000_000L
  }
}
