package bot.charlies.androiddesign.preview.kit

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.Posture
import androidx.compose.material3.adaptive.WindowAdaptiveInfo
import androidx.compose.material3.adaptive.layout.PaneScaffoldDirective
import androidx.compose.material3.adaptive.layout.calculatePaneScaffoldDirective
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.material3.adaptive.navigation3.SupportingPaneSceneStrategy
import androidx.compose.material3.adaptive.navigation3.rememberListDetailSceneStrategy
import androidx.compose.material3.adaptive.navigation3.rememberSupportingPaneSceneStrategy
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.SemanticsPropertyKey
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.NavDisplay
import androidx.wear.compose.WearDevice
import androidx.wear.compose.navigation3.rememberSwipeDismissableSceneStrategy
import androidx.window.core.layout.WindowSizeClass
import androidx.window.core.layout.computeWindowSizeClass
import bot.charlies.androiddesign.preview.domain.design.DesignSystemResolver
import bot.charlies.androiddesign.preview.feature.preview.theme.ProposalTheme
import bot.charlies.androiddesign.preview.feature.preview.theme.wear.ProvideWearTheme
import bot.charlies.androiddesign.preview.model.DesignSystem
import bot.charlies.androiddesign.preview.model.MaterialOverride
import bot.charlies.androiddesign.preview.model.PaletteVariant
import bot.charlies.androiddesign.preview.model.PhoneSurface
import bot.charlies.androiddesign.preview.model.SchemeOverride
import bot.charlies.androiddesign.preview.model.SizeClass
import bot.charlies.androiddesign.preview.model.SurfaceSpec
import bot.charlies.androiddesign.preview.model.ThemeOverride
import bot.charlies.androiddesign.preview.model.TypeRole
import bot.charlies.androiddesign.preview.model.TypographyOverride
import bot.charlies.androiddesign.preview.model.ViewSpec
import bot.charlies.androiddesign.preview.model.WearSurface
import java.nio.file.Path
import kotlin.math.roundToInt

internal val ExplorePaneKey = SemanticsPropertyKey<String>("ExplorePane")

enum class BoardSurface {
  Phone,
  Wear,
}

internal enum class PaneStrategyKind {
  Single,
  ListDetail,
  SupportingPane,
}

data class ExplorationOverlay(
  val id: String,
  val screen: String? = null,
  val content: @Composable () -> Unit,
)

data class ExplorationScreen(
  val id: String,
  val backStack: List<String>,
  val hasPlaceholder: Boolean = false,
  val isListPane: Boolean = false,
  val paneRole: PaneKind = PaneKind.Single,
  val content: @Composable (onPush: (String) -> Unit) -> Unit,
)

enum class PaneKind {
  Single,
  List,
  Detail,
  Main,
  Supporting,
}

class DirectionSpec
internal constructor(
  val name: String,
  val surface: BoardSurface,
  internal val screens: Map<String, ExplorationScreen>,
  val startScreen: String,
  val overlays: List<ExplorationOverlay>,
  internal val strategyKind: PaneStrategyKind,
) {
  val screenIds: List<String>
    get() = screens.keys.toList()

  fun overlaysFor(screenId: String): List<ExplorationOverlay> = overlays.filter {
    (it.screen ?: startScreen) == screenId
  }
}

class ExplorationBoard
internal constructor(
  val name: String,
  val surface: BoardSurface,
  val seedHex: String?,
  val variant: PaletteVariant?,
  val contrast: Double?,
  val brandFont: BundledFont?,
  val directions: List<DirectionSpec>,
) {
  val surfaceSpec: SurfaceSpec
    get() = if (surface == BoardSurface.Wear) WearSurface else PhoneSurface

  /** Resolves board, DESIGN.md, and optional viewer overrides into a merged [DesignSystem]. */
  fun resolveDesignSystem(
    designMd: ThemeOverride? = null,
    seedOverride: String? = null,
    variantOverride: PaletteVariant? = null,
  ): DesignSystem {
    val effectiveSeed = seedOverride ?: seedHex
    val effectiveVariant = variantOverride ?: variant
    val typographyOverrides =
      if (surface == BoardSurface.Phone && brandFont != null && !brandFont.setsAnyText) {
        buildMap {
          for (role in TypeRole.entries) {
            if (brandFont.allows(role.group, role)) {
              put(role.kebab, TypographyOverride(fontFamily = brandFont.family))
            }
          }
        }
      } else {
        emptyMap()
      }
    val hasBoardTheme =
      effectiveSeed != null ||
        effectiveVariant != null ||
        contrast != null ||
        typographyOverrides.isNotEmpty()
    val boardOverride =
      if (!hasBoardTheme) {
        null
      } else {
        ThemeOverride(
          name = name,
          colors = effectiveSeed?.let { mapOf("primary" to it) } ?: emptyMap(),
          typography = typographyOverrides,
          material =
            if (effectiveSeed != null || effectiveVariant != null || contrast != null) {
              MaterialOverride(
                scheme =
                  SchemeOverride(
                    seed = effectiveSeed,
                    variant = effectiveVariant?.wire,
                    contrast = contrast,
                  ),
              )
            } else {
              null
            },
        )
      }
    return DesignSystemResolver.resolve(designMd, boardOverride, surfaceSpec)
  }
}

class DirectionScope internal constructor() {
  private val screens = linkedMapOf<String, ExplorationScreen>()
  private val overlays = mutableListOf<ExplorationOverlay>()
  private var strategyKind: PaneStrategyKind = PaneStrategyKind.Single
  private var placeholderBlock: (@Composable () -> Unit)? = null

  fun screen(id: String = "main", content: @Composable () -> Unit) {
    require(id.isNotBlank()) { "Screen id cannot be blank" }
    screens[id] =
      ExplorationScreen(
        id = id,
        backStack = listOf(id),
        content = { content() },
      )
  }

  fun listDetail(
    id: String = "list",
    detailId: String = "detail",
    list: @Composable (onOpenDetail: () -> Unit) -> Unit,
    detail: @Composable () -> Unit,
    placeholder: (@Composable () -> Unit)? = null,
  ) {
    strategyKind = PaneStrategyKind.ListDetail
    placeholderBlock = placeholder
    screens[id] =
      ExplorationScreen(
        id = id,
        backStack = listOf(id),
        hasPlaceholder = placeholder != null,
        isListPane = true,
        paneRole = PaneKind.List,
        content = { push -> list { push(detailId) } },
      )
    screens[detailId] =
      ExplorationScreen(
        id = detailId,
        backStack = listOf(id, detailId),
        hasPlaceholder = placeholder != null,
        paneRole = PaneKind.Detail,
        content = { detail() },
      )
  }

  fun supportingPane(
    id: String = "main",
    supportingId: String = "supporting",
    main: @Composable (onOpenSupporting: () -> Unit) -> Unit,
    supporting: @Composable () -> Unit,
  ) {
    strategyKind = PaneStrategyKind.SupportingPane
    screens[id] =
      ExplorationScreen(
        id = id,
        backStack = listOf(id),
        paneRole = PaneKind.Main,
        content = { push -> main { push(supportingId) } },
      )
    screens[supportingId] =
      ExplorationScreen(
        id = supportingId,
        backStack = listOf(id, supportingId),
        paneRole = PaneKind.Supporting,
        content = { supporting() },
      )
  }

  fun overlay(id: String, screen: String? = null, content: @Composable () -> Unit) {
    require(id.isNotBlank()) { "Overlay id cannot be blank" }
    overlays += ExplorationOverlay(id = id, screen = screen, content = content)
  }

  internal fun build(name: String): DirectionSpec {
    require(screens.isNotEmpty()) { "Direction '$name' must declare at least one screen" }
    return DirectionSpec(
      name = name,
      surface = BoardSurface.Phone,
      screens = screens.toMap(),
      startScreen = screens.keys.first(),
      overlays = overlays.toList(),
      strategyKind = strategyKind,
    )
  }

  internal fun placeholder(): (@Composable () -> Unit)? = placeholderBlock

  internal fun mergeInto(
    extraDirections: MutableList<DirectionSpec>,
    fallbackName: String,
  ) {
    if (screens.isNotEmpty()) {
      extraDirections += build(fallbackName)
    }
  }
}

class WearDirectionScope internal constructor() {
  private val screens = linkedMapOf<String, ExplorationScreen>()
  private val overlays = mutableListOf<ExplorationOverlay>()

  fun screen(id: String = "main", content: @Composable () -> Unit) {
    require(id.isNotBlank()) { "Screen id cannot be blank" }
    screens[id] = ExplorationScreen(id = id, backStack = listOf(id), content = { content() })
  }

  fun overlay(id: String, screen: String? = null, content: @Composable () -> Unit) {
    require(id.isNotBlank()) { "Overlay id cannot be blank" }
    overlays += ExplorationOverlay(id = id, screen = screen, content = content)
  }

  internal fun build(name: String): DirectionSpec {
    require(screens.isNotEmpty()) { "Wear direction '$name' must declare at least one screen" }
    return DirectionSpec(
      name = name,
      surface = BoardSurface.Wear,
      screens = screens.toMap(),
      startScreen = screens.keys.first(),
      overlays = overlays.toList(),
      strategyKind = PaneStrategyKind.Single,
    )
  }

  internal fun hasScreens(): Boolean = screens.isNotEmpty()
}

class DesignBoardScope internal constructor() {
  private val namedDirections = mutableListOf<DirectionSpec>()
  private val defaultScope = DirectionScope()
  internal val placeholders = HashMap<DirectionSpec, (@Composable () -> Unit)?>()

  fun direction(name: String, block: DirectionScope.() -> Unit) {
    val scope = DirectionScope().apply(block)
    val spec = scope.build(name)
    namedDirections += spec
    placeholders[spec] = scope.placeholder()
  }

  fun screen(id: String = "main", content: @Composable () -> Unit) =
    defaultScope.screen(id, content)

  fun listDetail(
    id: String = "list",
    detailId: String = "detail",
    list: @Composable (onOpenDetail: () -> Unit) -> Unit,
    detail: @Composable () -> Unit,
    placeholder: (@Composable () -> Unit)? = null,
  ) = defaultScope.listDetail(id, detailId, list, detail, placeholder)

  fun supportingPane(
    id: String = "main",
    supportingId: String = "supporting",
    main: @Composable (onOpenSupporting: () -> Unit) -> Unit,
    supporting: @Composable () -> Unit,
  ) = defaultScope.supportingPane(id, supportingId, main, supporting)

  fun overlay(id: String, screen: String? = null, content: @Composable () -> Unit) =
    defaultScope.overlay(id, screen, content)

  internal fun finish(
    defaultDirectionName: String,
  ): Pair<List<DirectionSpec>, Map<String, (@Composable () -> Unit)?>> {
    val all = mutableListOf<DirectionSpec>()
    all.addAll(namedDirections)
    val defaultPlaceholder = defaultScope.placeholder()
    val placeholderMap = linkedMapOf<String, (@Composable () -> Unit)?>()
    namedDirections.forEach { placeholderMap[it.name] = placeholders[it] }
    defaultScope.mergeInto(all, defaultDirectionName)
    if (all.size > namedDirections.size) {
      placeholderMap[defaultDirectionName] = defaultPlaceholder
    }
    require(all.isNotEmpty()) { "designBoard must declare at least one direction or screen" }
    return all to placeholderMap
  }
}

class WearBoardScope internal constructor() {
  private val namedDirections = mutableListOf<DirectionSpec>()
  private val defaultScope = WearDirectionScope()

  fun direction(name: String, block: WearDirectionScope.() -> Unit) {
    namedDirections += WearDirectionScope().apply(block).build(name)
  }

  fun screen(id: String = "main", content: @Composable () -> Unit) =
    defaultScope.screen(id, content)

  fun overlay(id: String, screen: String? = null, content: @Composable () -> Unit) =
    defaultScope.overlay(id, screen, content)

  internal fun finish(defaultDirectionName: String): List<DirectionSpec> {
    val all = mutableListOf<DirectionSpec>()
    all.addAll(namedDirections)
    if (defaultScope.hasScreens()) {
      all += defaultScope.build(defaultDirectionName)
    }
    require(all.isNotEmpty()) { "wearBoard must declare at least one direction or screen" }
    return all
  }
}

internal fun seedLongToHex(seed: Long?): String? = seed?.let { "#%06X".format(it and 0xFFFFFFL) }

/** Declares a phone/tablet/desktop Compose exploration board with one or more [direction]s. */
fun designBoard(
  name: String = "Exploration",
  seed: Long? = null,
  variant: PaletteVariant? = null,
  contrast: Double? = null,
  brandFont: BundledFont? = null,
  block: DesignBoardScope.() -> Unit,
): ExplorationBoard {
  val scope = DesignBoardScope().apply(block)
  val (directions, placeholders) = scope.finish(name)
  DirectionPlaceholders.register(directions, placeholders)
  return ExplorationBoard(
    name = name,
    surface = BoardSurface.Phone,
    seedHex = seedLongToHex(seed),
    variant = variant,
    contrast = contrast?.coerceIn(-1.0, 1.0),
    brandFont = brandFont,
    directions = directions,
  )
}

/** Declares a Wear OS Compose exploration board with one or more [direction]s. */
fun wearBoard(
  name: String = "Wear exploration",
  seed: Long? = null,
  block: WearBoardScope.() -> Unit,
): ExplorationBoard {
  val scope = WearBoardScope().apply(block)
  val directions = scope.finish(name)
  return ExplorationBoard(
    name = name,
    surface = BoardSurface.Wear,
    seedHex = seedLongToHex(seed),
    variant = null,
    contrast = null,
    brandFont = null,
    directions = directions,
  )
}

internal object DirectionPlaceholders {
  private val store = java.util.WeakHashMap<DirectionSpec, (@Composable () -> Unit)?>()

  @Synchronized
  fun register(directions: List<DirectionSpec>, byName: Map<String, (@Composable () -> Unit)?>) {
    directions.forEach { dir -> store[dir] = byName[dir.name] }
  }

  @Synchronized
  operator fun get(direction: DirectionSpec): (@Composable () -> Unit)? = store[direction]
}

/**
 * Renders one [screenId] (and optional open [overlayId]) from [direction] inside [board]'s resolved
 * theme and Navigation 3 adaptive scene strategy, with inert locals overriding URI/clipboard APIs.
 */
@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
internal fun ExploreBoardFrame(
  board: ExplorationBoard,
  direction: DirectionSpec,
  screenId: String,
  view: ViewSpec,
  designSystem: DesignSystem,
  explorationDir: Path?,
  overlayId: String? = null,
  onEmptyDetail: ((String?) -> Unit)? = null,
) {
  val screen =
    requireNotNull(direction.screens[screenId]) {
      "No screen '$screenId' in direction '${direction.name}'"
    }
  val frameSize = DpSize(view.width.dp, view.height.dp)
  val activeOverlay = overlayId?.let { id -> direction.overlays.firstOrNull { it.id == id } }
  ProvideInertLocals(explorationDir) {
    if (board.surface == BoardSurface.Wear) {
      ProvideWearTheme(designSystem) {
        WearDevice(view.width, view.height, view.round) {
          val backStack =
            remember(screen.id, screen.backStack) {
              mutableStateListOf<String>().apply { addAll(screen.backStack) }
            }
          NavDisplay(
            backStack = backStack,
            modifier = Modifier.fillMaxSize(),
            sceneStrategies = listOf(rememberSwipeDismissableSceneStrategy<String>()),
            onBack = { if (backStack.size > 1) backStack.removeAt(backStack.lastIndex) },
            entryProvider = { entryId ->
              val entryScreen = direction.screens.getValue(entryId)
              NavEntry(entryId) {
                Box(Modifier.fillMaxSize()) {
                  entryScreen.content { next ->
                    if (next in direction.screens && backStack.lastOrNull() != next) {
                      backStack.add(next)
                    }
                  }
                }
              }
            },
          )
          activeOverlay?.content?.invoke()
        }
      }
      return@ProvideInertLocals
    }

    val directive = rememberBoardDirective(frameSize)
    val twoPanes = directive.maxHorizontalPartitions >= 2
    val emptyDetailScreen =
      if (
        direction.strategyKind == PaneStrategyKind.ListDetail &&
          screen.isListPane &&
          !screen.hasPlaceholder &&
          twoPanes
      ) {
        screen.id
      } else {
        null
      }
    DisposableEffect(emptyDetailScreen) {
      onEmptyDetail?.invoke(emptyDetailScreen)
      onDispose { onEmptyDetail?.invoke(null) }
    }

    ProposalTheme(designSystem, view.dark) {
      Surface(Modifier.size(frameSize), color = MaterialTheme.colorScheme.background) {
        val sizeClass = SizeClass.forWidth(view.width)
        key(sizeClass) {
          val listDetailStrategy = rememberListDetailSceneStrategy<String>(directive = directive)
          val supportingStrategy =
            rememberSupportingPaneSceneStrategy<String>(directive = directive)
          val strategies =
            when (direction.strategyKind) {
              PaneStrategyKind.ListDetail -> listOf(listDetailStrategy)
              PaneStrategyKind.SupportingPane -> listOf(supportingStrategy)
              PaneStrategyKind.Single -> emptyList()
            }
          val backStack =
            remember(screen.id, screen.backStack) {
              mutableStateListOf<String>().apply { addAll(screen.backStack) }
            }
          val placeholder = DirectionPlaceholders[direction]
          NavDisplay(
            backStack = backStack,
            modifier = Modifier.fillMaxSize(),
            sceneStrategies = strategies,
            onBack = { if (backStack.size > 1) backStack.removeAt(backStack.lastIndex) },
            entryProvider = { entryId ->
              val entryScreen = direction.screens.getValue(entryId)
              val metadata = paneMetadata(entryScreen.paneRole, direction.strategyKind, placeholder)
              NavEntry(entryId, metadata = metadata) {
                Box(Modifier.fillMaxSize().semantics { set(ExplorePaneKey, entryId) }) {
                  entryScreen.content { next ->
                    if (next in direction.screens && backStack.lastOrNull() != next) {
                      backStack.add(next)
                    }
                  }
                }
              }
            },
          )
        }
        activeOverlay?.content?.invoke()
      }
    }
  }
}

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
private fun rememberBoardDirective(frameSize: DpSize): PaneScaffoldDirective =
  remember(frameSize) {
    calculatePaneScaffoldDirective(
      WindowAdaptiveInfo(
        WindowSizeClass.BREAKPOINTS_V1.computeWindowSizeClass(
          frameSize.width.value.roundToInt().toFloat(),
          frameSize.height.value.roundToInt().toFloat(),
        ),
        Posture(),
      ),
    )
  }

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
private fun paneMetadata(
  paneRole: PaneKind,
  strategyKind: PaneStrategyKind,
  placeholder: (@Composable () -> Unit)?,
): Map<String, Any> =
  when (strategyKind) {
    PaneStrategyKind.Single -> emptyMap()
    PaneStrategyKind.ListDetail ->
      when (paneRole) {
        PaneKind.List ->
          if (placeholder == null) ListDetailSceneStrategy.listPane()
          else ListDetailSceneStrategy.listPane(detailPlaceholder = { placeholder() })
        PaneKind.Detail,
        PaneKind.Supporting -> ListDetailSceneStrategy.detailPane()
        PaneKind.Single,
        PaneKind.Main -> emptyMap()
      }
    PaneStrategyKind.SupportingPane ->
      when (paneRole) {
        PaneKind.Main,
        PaneKind.List -> SupportingPaneSceneStrategy.mainPane()
        PaneKind.Supporting,
        PaneKind.Detail -> SupportingPaneSceneStrategy.supportingPane()
        PaneKind.Single -> emptyMap()
      }
  }
