package bot.charlies.androiddesign.preview.app.server

import bot.charlies.androiddesign.preview.model.BoardMeta
import bot.charlies.androiddesign.preview.model.DirectionMeta
import bot.charlies.androiddesign.preview.model.SurfaceSpec
import bot.charlies.androiddesign.preview.model.ViewSpec

private val SANITIZE_SEGMENT_REGEX = Regex("""[^A-Za-z0-9._-]+""")

internal fun sanitizeSegment(name: String): String =
  name.trim().replace(SANITIZE_SEGMENT_REGEX, "_").ifBlank { "direction" }

internal data class MetaScreenTarget(
  val direction: DirectionMeta,
  val screenId: String,
  val prefix: String,
)

/**
 * Outcome of resolving requested direction, screen, overlay, and view filters against [BoardMeta].
 */
internal sealed interface TargetResolution<out T> {
  data class Ok<T>(val value: T) : TargetResolution<T>

  data class Error(val code: String, val message: String) : TargetResolution<Nothing>
}

internal data class SingleTargetSelection(
  val direction: DirectionMeta,
  val screenId: String,
  val overlayId: String?,
  val view: ViewSpec,
  val prefix: String,
)

internal data class CheckTargetSelection(
  val targets: List<MetaScreenTarget>,
  val views: List<ViewSpec>,
  val planned: List<CheckTargetSpec>,
)

internal object TargetResolver {
  fun prefixFor(meta: BoardMeta, directionName: String, screenId: String): String =
    if (meta.directions.size == 1) screenId else "${directionName}__$screenId"

  fun resolveSingleTarget(
    meta: BoardMeta,
    direction: String?,
    screen: String?,
    overlay: String?,
    view: String?,
  ): TargetResolution<SingleTargetSelection> {
    val dir =
      direction
        ?.takeIf { it.isNotBlank() }
        ?.let { name ->
          meta.directions.firstOrNull { it.name == name }
            ?: return TargetResolution.Error("unknown_direction", "Unknown direction '$name'")
        } ?: meta.directions.first()
    val screenId = screen?.takeIf { it.isNotBlank() } ?: dir.startScreen
    if (screenId !in dir.screens) {
      return TargetResolution.Error("unknown_screen", "No screen '$screenId' in '${dir.name}'")
    }
    val overlayId = overlay?.takeIf { it.isNotBlank() }
    if (overlayId != null && dir.overlays.none { it.id == overlayId }) {
      return TargetResolution.Error("unknown_overlay", "No overlay '$overlayId' in '${dir.name}'")
    }
    val resolvedView =
      view
        ?.takeIf { it.isNotBlank() }
        ?.let { name ->
          meta.views.firstOrNull { it.name == name }
            ?: return TargetResolution.Error("unknown_view", "Unknown view '$name'")
        } ?: meta.views.first()
    return TargetResolution.Ok(
      SingleTargetSelection(
        direction = dir,
        screenId = screenId,
        overlayId = overlayId,
        view = resolvedView,
        prefix = prefixFor(meta, dir.name, screenId),
      ),
    )
  }

  fun resolveCheckTargets(
    meta: BoardMeta,
    direction: String?,
    screen: String?,
    view: String?,
    scoped: Boolean,
  ): TargetResolution<CheckTargetSelection> {
    val directionFilter = direction?.takeIf { it.isNotBlank() }?.let { setOf(it.trim()) }
    if (directionFilter != null && meta.directions.none { it.name in directionFilter }) {
      val known = meta.directions.joinToString { it.name }
      return TargetResolution.Error(
        "unknown_direction",
        "Unknown direction '$direction' (available: $known)",
      )
    }
    val screenFilter = screen?.takeIf { it.isNotBlank() }?.let { listOf(it.trim()) }
    val targets = resolveMetaTargets(meta.directions, screenFilter, directionFilter)
    if (targets.isEmpty()) {
      return TargetResolution.Error("unknown_screen", "No matching directions or screens selected")
    }
    val resolvedViews =
      if (!view.isNullOrBlank()) {
        val wanted = view.trim()
        val match =
          meta.views.firstOrNull { it.name == wanted }
            ?: run {
              val known = meta.views.joinToString { it.name }
              return TargetResolution.Error(
                "unknown_view",
                "Unknown view '$wanted' (available: $known)",
              )
            }
        listOf(match)
      } else {
        meta.views
      }
    val planned = planBatchTargets(targets, resolvedViews, includeOverlays = !scoped)
    return TargetResolution.Ok(CheckTargetSelection(targets, resolvedViews, planned))
  }

  fun resolveReportTargets(
    meta: BoardMeta,
    surface: SurfaceSpec,
    requestedViews: List<String>?,
    requestedScreens: List<String>?,
  ): TargetResolution<List<CheckTargetSpec>> {
    val views =
      if (requestedViews == null) {
        surface.views
      } else {
        requestedViews.map { name ->
          surface.views.firstOrNull { it.name == name }
            ?: return TargetResolution.Error(
              "unknown_view",
              "unknown view '$name' (the ${surface.key} views: ${surface.views.joinToString { it.name }})",
            )
        }
      }
    if (requestedScreens != null) {
      val known =
        meta.directions
          .flatMap { dir ->
            dir.screens.flatMap { sid ->
              listOf(sid, "${dir.name}/$sid", "${sanitizeSegment(dir.name)}__$sid")
            }
          }
          .toSet()
      val unknown = requestedScreens.filter { it !in known }
      if (unknown.isNotEmpty()) {
        val allNames = meta.directions.flatMap { d -> d.screens.map { "${d.name}/$it" } }
        return TargetResolution.Error(
          "unknown_screen",
          "no such screen: ${unknown.joinToString()} (screens: ${allNames.joinToString()})",
        )
      }
    }
    val targets = resolveMetaTargets(meta.directions, requestedScreens, directionFilter = null)
    return TargetResolution.Ok(planBatchTargets(targets, views, includeOverlays = true))
  }

  fun enumerateBoardTargets(meta: BoardMeta): List<CheckTargetSpec> {
    val targets = resolveMetaTargets(meta.directions, null, directionFilter = null)
    return buildList {
      for (t in targets) {
        for (view in meta.views) {
          add(CheckTargetSpec(t.direction.name, t.screenId, t.prefix, null, view))
        }
      }
    }
  }
}

internal fun resolveMetaTargets(
  directions: List<DirectionMeta>,
  screenFilter: List<String>?,
  directionFilter: Set<String>? = null,
): List<MetaScreenTarget> {
  val activeDirections =
    if (directionFilter.isNullOrEmpty()) directions
    else directions.filter { it.name in directionFilter }
  val singleDirection = activeDirections.size == 1 && directions.size == 1
  val filterSet = screenFilter?.toSet()
  return buildList {
    for (dir in activeDirections) {
      val sanitizedDir = sanitizeSegment(dir.name)
      for (screenId in dir.screens) {
        val prefix = if (singleDirection) screenId else "${sanitizedDir}__$screenId"
        val slashName = "${dir.name}/$screenId"
        if (
          filterSet == null ||
            screenId in filterSet ||
            prefix in filterSet ||
            slashName in filterSet
        ) {
          add(MetaScreenTarget(dir, screenId, prefix))
        }
      }
    }
  }
}

/**
 * Expands [targets] x [views] (plus overlays when [includeOverlays] is true) in deterministic
 * canonical order (`direction/screen -> base views -> overlays -> views`).
 */
internal fun planBatchTargets(
  targets: List<MetaScreenTarget>,
  views: List<ViewSpec>,
  includeOverlays: Boolean,
): List<CheckTargetSpec> = buildList {
  for (target in targets) {
    for (view in views) {
      add(
        CheckTargetSpec(
          direction = target.direction.name,
          screenId = target.screenId,
          prefix = target.prefix,
          overlayId = null,
          view = view,
        ),
      )
    }
    if (includeOverlays) {
      for (overlay in target.direction.overlaysFor(target.screenId)) {
        for (view in views) {
          add(
            CheckTargetSpec(
              direction = target.direction.name,
              screenId = target.screenId,
              prefix = target.prefix,
              overlayId = overlay.id,
              view = view,
            ),
          )
        }
      }
    }
  }
}

/**
 * Orders batch render targets by `(view.width, view.height, view.name)` so consecutive renders
 * reuse `ComposeView` and `ImageReader` across directions, while keeping every base screen ahead of
 * its overlays and preserving deterministic relative order within each group.
 */
internal fun orderTargetsForBatchRender(targets: List<CheckTargetSpec>): List<CheckTargetSpec> =
  targets
    .withIndex()
    .sortedWith(
      compareBy<IndexedValue<CheckTargetSpec>>(
        { it.value.view.width },
        { it.value.view.height },
        { it.value.view.name },
        { it.value.overlayId != null },
        { it.index },
      ),
    )
    .map { it.value }
