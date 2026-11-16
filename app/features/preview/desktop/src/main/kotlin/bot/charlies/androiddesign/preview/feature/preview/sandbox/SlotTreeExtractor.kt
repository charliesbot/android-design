package bot.charlies.androiddesign.preview.feature.preview.sandbox

import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.runtime.tooling.ComposeToolingApi
import androidx.compose.runtime.tooling.CompositionData
import androidx.compose.runtime.tooling.CompositionGroup
import androidx.compose.runtime.tooling.parseSourceInformation
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.LayoutInfo
import androidx.compose.ui.platform.InspectableValue
import androidx.compose.ui.platform.isDebugInspectorInfoEnabled
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsNode
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.Density
import bot.charlies.androiddesign.preview.domain.DatePickerSpec
import bot.charlies.androiddesign.preview.domain.findings.Contrast
import bot.charlies.androiddesign.preview.domain.findings.FindingRules
import bot.charlies.androiddesign.preview.domain.layout.LayoutSummary
import bot.charlies.androiddesign.preview.feature.preview.theme.ProposalFonts
import bot.charlies.androiddesign.preview.feature.preview.theme.wear.WearThemeResolver
import bot.charlies.androiddesign.preview.kit.AD_PRIMARY_TAG
import bot.charlies.androiddesign.preview.kit.BoardSurface
import bot.charlies.androiddesign.preview.kit.ExplorePaneKey
import bot.charlies.androiddesign.preview.model.A11yInfo
import bot.charlies.androiddesign.preview.model.Axis
import bot.charlies.androiddesign.preview.model.Bounds
import bot.charlies.androiddesign.preview.model.CornerClip
import bot.charlies.androiddesign.preview.model.LayoutNode
import bot.charlies.androiddesign.preview.model.LayoutTree
import bot.charlies.androiddesign.preview.model.M3Component
import bot.charlies.androiddesign.preview.model.NodeColors
import bot.charlies.androiddesign.preview.model.NodeRoles
import bot.charlies.androiddesign.preview.model.NodeWeight
import bot.charlies.androiddesign.preview.model.RingInset
import bot.charlies.androiddesign.preview.model.SurfaceSpec
import bot.charlies.androiddesign.preview.model.SurfaceSpecs
import bot.charlies.androiddesign.preview.model.TextInfo
import bot.charlies.androiddesign.preview.model.TypeRole
import bot.charlies.androiddesign.preview.model.ViewSpec
import bot.charlies.androiddesign.preview.model.WearComponent
import java.util.IdentityHashMap
import org.jetbrains.skia.Bitmap

/**
 * Extracts a [LayoutTree] (preserving source file/line metadata, semantics bounds, modifiers, text
 * layout metrics, and 2 px perimeter pixel contrast) from Compose slot inspection tables and
 * semantics snapshots.
 */
@OptIn(ComposeToolingApi::class)
internal object SlotTreeExtractor {
  private const val WEAR_RING_COVERAGE = 0.85f
  private const val WEAR_RING_PADDING_DP = 2f
  private const val WEAR_RING_STROKE_DP = 4f
  private const val SIZE_TOLERANCE_PX = 1f

  init {
    isDebugInspectorInfoEnabled = true
  }

  private data class CallSite(
    val rawName: String,
    val mappedType: String,
    val sourceFile: String?,
    val sourceLine: Int?,
    val order: Int,
  )

  private enum class LayerKind {
    Dialog,
    Popup,
  }

  private data class FrameContext(
    val sourceFile: String?,
    val lineNumber: Int?,
    val rawName: String?,
    val inCrossLayerCall: Boolean = false,
  )

  fun extract(
    tables: Set<CompositionData>,
    mergedRoots: List<SemanticsNode>,
    unmergedRoots: List<SemanticsNode>,
    rootCoordinates: LayoutCoordinates?,
    bitmap: Bitmap?,
    screen: String,
    view: ViewSpec,
    surface: BoardSurface,
    explorationFile: String,
    sourceText: String,
    density: Float,
    emptyDetailLists: List<String> = emptyList(),
  ): LayoutTree {
    val lineOffsets = computeLineStarts(sourceText)
    val claimed = IdentityHashMap<LayoutInfo, CallSite>()
    val pendingByKind =
      mapOf(
        LayerKind.Dialog to ArrayDeque<CallSite?>(),
        LayerKind.Popup to ArrayDeque<CallSite?>(),
      )
    val layerTables =
      mapOf(
        LayerKind.Dialog to ArrayList<CompositionData>(),
        LayerKind.Popup to ArrayList<CompositionData>(),
      )
    var orderCounter = 0

    data class SourceCallSite(val ident: String?, val line: Int)

    fun walkGroup(group: CompositionGroup, caller: FrameContext?) {
      val parsed = group.sourceInfo?.takeIf { it.isNotEmpty() }?.let(::parseSourceInformation)
      val fnName = parsed?.functionName
      val ownFile = parsed?.sourceFile?.substringAfterLast('/')
      val locations = parsed?.locations.orEmpty()
      val ownFirstLoc = locations.firstOrNull()
      val rawLine = ownFirstLoc?.lineNumber
      val resolvedLine =
        when {
          caller != null && caller.sourceFile == explorationFile && caller.lineNumber != null ->
            caller.lineNumber
          ownFirstLoc != null &&
            ownFile == explorationFile &&
            ownFirstLoc.offset in sourceText.indices ->
            lineFromOffset(lineOffsets, ownFirstLoc.offset)
          rawLine != null && rawLine >= 0 -> rawLine + 1
          else -> caller?.lineNumber
        }
      val resolvedFile =
        when {
          caller?.sourceFile == explorationFile -> explorationFile
          ownFile == explorationFile -> explorationFile
          else -> ownFile ?: caller?.sourceFile
        }

      val isKnownComponent = CallNameMapper.isKnown(fnName, surface)
      val isDirectLayout = group.node is LayoutInfo
      val isTargetCall =
        ((resolvedFile == explorationFile && isEmitWorthyCall(fnName, caller?.rawName)) ||
          isAlwaysTrackedChrome(fnName)) && (isKnownComponent || isDirectLayout)
      val isCrossLayer = fnName in CROSS_LAYER_CALL_NAMES

      if (isTargetCall && fnName != null) {
        if (isCrossLayer) {
          if (caller?.inCrossLayerCall != true) {
            findLayerHostKind(group, explorationFile)?.let { kind ->
              pendingByKind
                .getValue(kind)
                .addLast(
                  CallSite(
                    rawName = fnName,
                    mappedType = CallNameMapper.map(fnName, surface),
                    sourceFile = resolvedFile,
                    sourceLine = resolvedLine,
                    order = orderCounter++,
                  ),
                )
            }
          }
        } else {
          val layoutInfo =
            if (isDirectLayout) group.node as LayoutInfo else findPrimaryLayoutInfo(group)
          if (layoutInfo != null && !claimed.containsKey(layoutInfo)) {
            val mapped = CallNameMapper.map(fnName, surface)
            claimed[layoutInfo] =
              CallSite(
                rawName = fnName,
                mappedType = mapped,
                sourceFile = resolvedFile,
                sourceLine = resolvedLine,
                order = orderCounter++,
              )
          }
        }
      } else if (caller?.inCrossLayerCall != true) {
        // Reserve FIFO alignment for untracked Popup/Dialog hosts (e.g. TooltipPopup).
        when {
          fnName == "DialogLayout" && ownFile == "Dialog.skiko.kt" ->
            pendingByKind.getValue(LayerKind.Dialog).addLast(null)
          fnName == "PopupLayout" && ownFile == "Popup.skiko.kt" ->
            pendingByKind.getValue(LayerKind.Popup).addLast(null)
        }
      }

      val children = group.compositionGroups.toList()
      val callSites =
        if (ownFile == explorationFile) {
          locations.mapNotNull { loc ->
            val line =
              when {
                loc.offset in sourceText.indices -> lineFromOffset(lineOffsets, loc.offset)
                loc.lineNumber >= 0 -> loc.lineNumber + 1
                else -> null
              } ?: return@mapNotNull null
            val ident =
              if (loc.offset in sourceText.indices) {
                CallNameMapper.identifierAt(sourceText, loc.offset)
              } else {
                null
              }
            SourceCallSite(ident = ident, line = line)
          }
        } else {
          emptyList()
        }

      var callCursor = 0
      val fallbackChildLine = callSites.firstOrNull()?.line ?: resolvedLine
      val effectiveChildFile = ownFile ?: caller?.sourceFile
      val childInCrossLayer = (caller?.inCrossLayerCall == true) || (isCrossLayer && isTargetCall)
      children.forEach { child ->
        val matchedLine =
          if (ownFile == explorationFile && callSites.isNotEmpty()) {
            val childParsed =
              child.sourceInfo?.takeIf { it.isNotEmpty() }?.let(::parseSourceInformation)
            val childFn = childParsed?.functionName
            var foundIdx = -1
            if (childFn != null) {
              for (i in callCursor until callSites.size) {
                if (callSites[i].ident == childFn) {
                  foundIdx = i
                  break
                }
              }
            }
            if (foundIdx >= 0) {
              callCursor = foundIdx + 1
              callSites[foundIdx].line
            } else {
              fallbackChildLine
            }
          } else {
            caller?.lineNumber
          }
        walkGroup(
          child,
          FrameContext(
            sourceFile = effectiveChildFile,
            lineNumber = matchedLine,
            rawName = fnName,
            inCrossLayerCall = childInCrossLayer,
          ),
        )
      }
    }

    tables.forEach { table ->
      detectLayerTableKind(table, explorationFile)?.let { kind ->
        layerTables.getValue(kind) += table
      }
      table.compositionGroups.forEach { root -> walkGroup(root, null) }
    }
    for (kind in LayerKind.entries) {
      val pending = pendingByKind.getValue(kind)
      val spawned = layerTables.getValue(kind)
      for ((idx, table) in spawned.withIndex()) {
        val site = pending.getOrNull(idx) ?: continue
        val containerLayout = findLayerContainerLayoutInfo(table, explorationFile, claimed)
        if (containerLayout != null && !claimed.containsKey(containerLayout)) {
          claimed[containerLayout] = site
        }
      }
    }
    // ToggleFloatingActionButton inside FloatingActionButtonMenu and DatePicker/DateRangePicker
    // inside DatePickerDialog are outer and inner wrappers of the same component; drop the inner
    // entry so the outer wrapper owns its primary tag, text, and touch bounds and is counted once.
    val redundantNested =
      claimed.entries
        .filter { (layout, site) ->
          val parentAncestors = generateSequence(layout.parentInfo) { it.parentInfo }
          when (site.rawName) {
            "ToggleFloatingActionButton" ->
              parentAncestors.any { anc -> claimed[anc]?.mappedType == M3Component.FabMenu.wire }
            "DatePicker",
            "DateRangePicker" ->
              parentAncestors.any { anc -> claimed[anc]?.rawName == "DatePickerDialog" }
            else -> false
          }
        }
        .map { it.key }
    redundantNested.forEach(claimed::remove)

    val semanticsByLayout = IdentityHashMap<LayoutInfo, MutableList<SemanticsNode>>()
    val mergedByLayout = IdentityHashMap<LayoutInfo, SemanticsNode>()
    val paneRootsByLayout = IdentityHashMap<LayoutInfo, String>()
    indexSemantics(unmergedRoots, semanticsByLayout, paneRootsByLayout)
    mergedRoots.forEach { root -> indexMerged(root, mergedByLayout) }

    val orderedLayouts = claimed.entries.sortedBy { it.value.order }.map { it.key }
    val paneByLayout = IdentityHashMap<LayoutInfo, String>()
    val distinctPanes = LinkedHashSet<String>()
    for (layout in orderedLayouts) {
      val paneId = resolvePaneId(layout, paneRootsByLayout)
      if (paneId != null) {
        paneByLayout[layout] = paneId
        distinctPanes += paneId
      }
    }
    val multiPane = distinctPanes.size > 1

    val idByLayout = IdentityHashMap<LayoutInfo, String>()
    val usedIds = HashSet<String>()
    val fallbackCounts = HashMap<String, Int>()
    for (layout in orderedLayouts) {
      val site = claimed.getValue(layout)
      val typeDecap = site.mappedType.replaceFirstChar { it.lowercaseChar() }
      val sig = semanticSignature(layout, claimed, semanticsByLayout, mergedByLayout)
      val lineSuffix = site.sourceLine?.let { "@L$it" }.orEmpty()
      val panePrefix =
        if (multiPane) paneByLayout[layout]?.let { "${LayoutSummary.PANE_PREFIX}$it/" }.orEmpty()
        else ""
      val suffix =
        if (!sig.isNullOrBlank()) {
          (sig.hashCode().toLong() and 0xFFFFFL).toString(16)
        } else {
          val bucketKey = "$panePrefix$typeDecap$lineSuffix"
          val next = (fallbackCounts[bucketKey] ?: 0) + 1
          fallbackCounts[bucketKey] = next
          next.toString()
        }
      val baseId = "${typeDecap}_$suffix"
      var unique = "$panePrefix$baseId$lineSuffix"
      var dup = 2
      while (!usedIds.add(unique)) {
        unique = "$panePrefix${baseId}_${dup++}$lineSuffix"
      }
      idByLayout[layout] = unique
    }

    val surfaceSpec = SurfaceSpecs.forView(view)
    val rootWindowPos =
      rootCoordinates?.takeIf { it.isAttached }?.localToWindow(Offset.Zero) ?: Offset.Zero
    val layoutNodes = orderedLayouts.mapNotNull { layout ->
      buildLayoutNode(
        layout = layout,
        site = claimed.getValue(layout),
        id = idByLayout.getValue(layout),
        claimed = claimed,
        idByLayout = idByLayout,
        semanticsByLayout = semanticsByLayout,
        mergedByLayout = mergedByLayout,
        rootCoordinates = rootCoordinates,
        rootWindowPos = rootWindowPos,
        bitmap = bitmap,
        surfaceSpec = surfaceSpec,
        surface = surface,
        view = view,
        density = density,
      )
    }

    val rawTree =
      LayoutTree(screen = screen, view = view, nodes = layoutNodes, findings = emptyList())
    val findings =
      FindingRules.evaluate(
        rawTree,
        surface = surfaceSpec,
        emptyDetailLists = emptyDetailLists,
      )
    return rawTree.copy(findings = findings)
  }

  private fun isEmitWorthyCall(fnName: String?, callerName: String?): Boolean {
    if (fnName.isNullOrBlank()) return false
    if (fnName in SKIPPED_FRAMEWORK_NAMES) return false
    if (fnName.startsWith("remember") || fnName.startsWith("Provide")) return false
    if (fnName == "Surface" && callerName in MATERIAL_SURFACE_WRAPPERS) return false
    return true
  }

  private fun isAlwaysTrackedChrome(fnName: String?): Boolean = fnName in CHROME_CALL_NAMES

  private fun findPrimaryLayoutInfo(group: CompositionGroup): LayoutInfo? {
    (group.node as? LayoutInfo)?.let {
      return it
    }
    for (child in group.compositionGroups) {
      val found = findPrimaryLayoutInfo(child)
      if (found != null) return found
    }
    return null
  }

  private fun detectLayerTableKind(table: CompositionData, explorationFile: String): LayerKind? {
    fun visit(group: CompositionGroup, depth: Int): LayerKind? {
      val parsed = group.sourceInfo?.takeIf { it.isNotEmpty() }?.let(::parseSourceInformation)
      val fnName = parsed?.functionName
      val ownFile = parsed?.sourceFile?.substringAfterLast('/')
      if (ownFile == explorationFile) return null
      if (fnName == "DialogLayout" || fnName == "PopupLayout") return null
      if (fnName == "rememberDialogMeasurePolicy") return LayerKind.Dialog
      if (fnName == "rememberPopupMeasurePolicy") return LayerKind.Popup
      if (depth <= 4 && fnName == null && ownFile == "Dialog.skiko.kt") return LayerKind.Dialog
      if (depth <= 4 && fnName == null && ownFile == "Popup.skiko.kt") return LayerKind.Popup
      for (child in group.compositionGroups) {
        visit(child, depth + 1)?.let {
          return it
        }
      }
      return null
    }
    for (root in table.compositionGroups) {
      visit(root, 0)?.let {
        return it
      }
    }
    return null
  }

  private fun findLayerHostKind(group: CompositionGroup, explorationFile: String): LayerKind? {
    for (child in group.compositionGroups) {
      val parsed = child.sourceInfo?.takeIf { it.isNotEmpty() }?.let(::parseSourceInformation)
      val fnName = parsed?.functionName
      val ownFile = parsed?.sourceFile?.substringAfterLast('/')
      if (ownFile == explorationFile) continue
      when {
        fnName == "DialogLayout" || (fnName == "Dialog" && ownFile == "Dialog.skiko.kt") ->
          return LayerKind.Dialog
        fnName == "PopupLayout" || (fnName == "Popup" && ownFile == "Popup.skiko.kt") ->
          return LayerKind.Popup
      }
      findLayerHostKind(child, explorationFile)?.let {
        return it
      }
    }
    return null
  }

  private fun findLayerContainerLayoutInfo(
    table: CompositionData,
    explorationFile: String,
    claimed: IdentityHashMap<LayoutInfo, CallSite>,
  ): LayoutInfo? {
    var layerRootLayout: LayoutInfo? = null
    var childAfterRoot: LayoutInfo? = null

    fun LayoutInfo.isUsable(): Boolean =
      isAttached && isPlaced && width > 0 && height > 0 && !claimed.containsKey(this)

    fun visit(group: CompositionGroup): LayoutInfo? {
      val parsed = group.sourceInfo?.takeIf { it.isNotEmpty() }?.let(::parseSourceInformation)
      val fnName = parsed?.functionName
      val ownFile = parsed?.sourceFile?.substringAfterLast('/')
      if (ownFile == explorationFile) return null
      if (fnName == "DialogLayout" || fnName == "PopupLayout") return null

      val direct = (group.node as? LayoutInfo)?.takeIf { it.isUsable() }
      if (direct != null) {
        if (layerRootLayout == null) {
          layerRootLayout = direct
        } else if (childAfterRoot == null && direct !== layerRootLayout) {
          childAfterRoot = direct
        }
      }

      if (fnName in LAYER_CONTAINER_FUNCTIONS) {
        val surfaceLayout = findPrimaryLayoutInfo(group)?.takeIf { it.isUsable() }
        if (surfaceLayout != null) return surfaceLayout
      }

      for (child in group.compositionGroups) {
        visit(child)?.let {
          return it
        }
      }
      return null
    }

    for (root in table.compositionGroups) {
      visit(root)?.let {
        return it
      }
    }
    return childAfterRoot ?: layerRootLayout
  }

  private fun buildLayoutNode(
    layout: LayoutInfo,
    site: CallSite,
    id: String,
    claimed: IdentityHashMap<LayoutInfo, CallSite>,
    idByLayout: IdentityHashMap<LayoutInfo, String>,
    semanticsByLayout: IdentityHashMap<LayoutInfo, MutableList<SemanticsNode>>,
    mergedByLayout: IdentityHashMap<LayoutInfo, SemanticsNode>,
    rootCoordinates: LayoutCoordinates?,
    rootWindowPos: Offset,
    bitmap: Bitmap?,
    surfaceSpec: SurfaceSpec,
    surface: BoardSurface,
    view: ViewSpec,
    density: Float,
  ): LayoutNode? {
    val parentLayout = nearestClaimedAncestor(layout, claimed)
    val parentId = parentLayout?.let(idByLayout::get)
    val weightInfo = extractWeight(layout, parentLayout?.let { claimed[it] }, density)
    if (!layout.isAttached || !layout.isPlaced || layout.width <= 0 || layout.height <= 0) {
      if (weightInfo == null) return null
      return LayoutNode(
        id = id,
        type = site.mappedType,
        parent = parentId,
        bounds = Bounds(0f, 0f, 0f, 0f),
        weight = weightInfo.copy(size = 0f),
        sourceFile = site.sourceFile,
        sourceLine = site.sourceLine,
      )
    }

    val layoutRectPx = measureLayoutRectPx(layout, rootCoordinates, rootWindowPos)
    val unclippedDp = layoutRectPx.toDp(density)
    val directSemantics = semanticsByLayout[layout].orEmpty()
    val ownedSemantics = collectOwnedSemantics(layout, claimed, semanticsByLayout)
    val mergedNode =
      mergedByLayout[layout] ?: directSemantics.firstOrNull() ?: ownedSemantics.firstOrNull()
    val isInteractiveType = site.mappedType in surfaceSpec.interactiveTypes
    val interactive = isInteractive(site.mappedType, surfaceSpec, directSemantics)
    val primary =
      (isInteractiveType || directSemantics.any { it.config.contains(SemanticsActions.OnClick) }) &&
        (directSemantics + if (isInteractiveType) ownedSemantics else emptyList()).any {
          it.config.getOrNull(SemanticsProperties.TestTag) == AD_PRIMARY_TAG
        }
    val scrollsVertically =
      (directSemantics +
          if (surface == BoardSurface.Wear && site.mappedType == WearComponent.AlertDialog.wire) {
            ownedSemantics
          } else {
            emptyList()
          })
        .any { it.config.contains(SemanticsProperties.VerticalScrollAxisRange) }
    val scrollsHorizontally = directSemantics.any {
      it.config.contains(SemanticsProperties.HorizontalScrollAxisRange)
    }

    val visibleBoundsPx =
      directSemantics.firstOrNull()?.boundsInFrame(rootWindowPos)?.let { sRect ->
        clipWithin(sRect, layoutRectPx)
      } ?: layoutRectPx
    val touchPx =
      if (!interactive) null
      else {
        val touchSemantics =
          if (site.mappedType == M3Component.FabMenu.wire) directSemantics + ownedSemantics
          else directSemantics.ifEmpty { ownedSemantics }
        val semTouch =
          touchSemantics
            .map { it.touchBoundsInFrame(rootWindowPos) }
            .fold<Rect, Rect?>(null) { acc, r -> if (acc == null) r else unionRect(acc, r) }
        semTouch?.let { unionRect(it, layoutRectPx) } ?: layoutRectPx
      }
    val boundsDp = visibleBoundsPx.toDp(density)

    val textData =
      extractTextAndContrast(
        type = site.mappedType,
        ownedSemantics = ownedSemantics,
        rootWindowPos = rootWindowPos,
        bitmap = bitmap,
        surfaceSpec = surfaceSpec,
        surface = surface,
        layout = layout,
        claimed = claimed,
        semanticsByLayout = semanticsByLayout,
        density = density,
      )

    val isWholeBox =
      visibleBoundsPx.width >= layout.width - SIZE_TOLERANCE_PX &&
        visibleBoundsPx.height >= layout.height - SIZE_TOLERANCE_PX
    val cornerClip =
      if (!isWholeBox || site.mappedType !in CONTAINER_CLIP_TYPES) null
      else
        extractCornerClip(
          layout,
          Size(layout.width.toFloat(), layout.height.toFloat()),
          boundsDp,
          density,
        )

    val ringInset =
      if (
        surface == BoardSurface.Wear &&
          site.mappedType in WEAR_RING_TYPES &&
          minOf(boundsDp.width, boundsDp.height) >= view.width * WEAR_RING_COVERAGE
      ) {
        RingInset(WEAR_RING_PADDING_DP, WEAR_RING_STROKE_DP)
      } else {
        null
      }

    val reachesPast =
      layoutRectPx.left < visibleBoundsPx.left - 0.5f * density ||
        layoutRectPx.right > visibleBoundsPx.right + 0.5f * density

    return LayoutNode(
      id = id,
      type = site.mappedType,
      parent = parentId,
      primary = primary,
      scrolls = scrollsVertically,
      scrollsHorizontally = scrollsHorizontally,
      bounds = boundsDp,
      layout = unclippedDp.takeIf { reachesPast },
      touch = touchPx?.toDp(density),
      text = textData.textInfo,
      ring = ringInset,
      clip = cornerClip,
      variant =
        if (site.mappedType == M3Component.DatePicker.wire && site.rawName != "DatePickerDialog") {
          if (
            generateSequence(layout.parentInfo) { it.parentInfo }
              .any { anc -> claimed[anc]?.mappedType == M3Component.Dialog.wire }
          ) {
            DatePickerSpec.Variant.FullScreen.wire
          } else {
            DatePickerSpec.Variant.Docked.wire
          }
        } else {
          null
        },
      weight = weightInfo,
      roles = textData.roles,
      colors = textData.colors,
      a11y = extractA11y(interactive, mergedNode),
      sourceFile = site.sourceFile,
      sourceLine = site.sourceLine,
    )
  }

  private fun nearestClaimedAncestor(
    layout: LayoutInfo,
    claimed: IdentityHashMap<LayoutInfo, CallSite>,
  ): LayoutInfo? {
    var cursor = layout.parentInfo
    while (cursor != null) {
      if (claimed.containsKey(cursor)) return cursor
      cursor = cursor.parentInfo
    }
    return null
  }

  /**
   * Collects semantics nodes belonging directly to [rootLayout] plus any internal unmerged child
   * layout subtrees that were NOT separately claimed as their own component nodes (e.g. a
   * `Button`'s internal `Text` label when `Text` wasn't already claimed by a child `LayoutNode`, OR
   * text nodes directly owned by [rootLayout]).
   */
  private fun collectOwnedSemantics(
    rootLayout: LayoutInfo,
    claimed: IdentityHashMap<LayoutInfo, CallSite>,
    semanticsByLayout: IdentityHashMap<LayoutInfo, MutableList<SemanticsNode>>,
  ): List<SemanticsNode> {
    val result = mutableListOf<SemanticsNode>()
    semanticsByLayout[rootLayout]?.let(result::addAll)
    for ((candidateLayout, semNodes) in semanticsByLayout) {
      if (candidateLayout === rootLayout || claimed.containsKey(candidateLayout)) continue
      if (nearestClaimedAncestor(candidateLayout, claimed) === rootLayout) {
        result.addAll(semNodes)
      }
    }
    return result
  }

  private data class ExtractedTextAndColor(
    val textInfo: TextInfo? = null,
    val roles: NodeRoles? = null,
    val colors: NodeColors? = null,
  )

  private fun extractTextAndContrast(
    type: String,
    ownedSemantics: List<SemanticsNode>,
    rootWindowPos: Offset,
    bitmap: Bitmap?,
    surfaceSpec: SurfaceSpec,
    surface: BoardSurface,
    layout: LayoutInfo,
    claimed: IdentityHashMap<LayoutInfo, CallSite>,
    semanticsByLayout: IdentityHashMap<LayoutInfo, MutableList<SemanticsNode>>,
    density: Float,
  ): ExtractedTextAndColor {
    if (type == M3Component.Icon.wire || type == WearComponent.Icon.wire) {
      return ExtractedTextAndColor()
    }
    val textLayouts = mutableListOf<Pair<SemanticsNode, TextLayoutResult>>()
    // First look in directly owned semantics, then include internal child Text layouts for label
    // controls (e.g. Button/Tab).
    val candidates =
      if (isLabelControl(type, surfaceSpec)) {
        val all = ArrayList(ownedSemantics)
        for ((childLayout, semNodes) in semanticsByLayout) {
          if (
            claimed[childLayout]?.mappedType in
              setOf(M3Component.Text.wire, WearComponent.Text.wire) &&
              nearestClaimedAncestor(childLayout, claimed) === layout
          ) {
            all.addAll(semNodes)
          }
        }
        all
      } else {
        ownedSemantics
      }

    for (node in candidates) {
      val action = node.config.getOrNull(SemanticsActions.GetTextLayoutResult)?.action ?: continue
      val out = mutableListOf<TextLayoutResult>()
      if (action(out) && out.isNotEmpty()) {
        out.forEach { res ->
          if (!ProposalFonts.isSymbolFont(res.layoutInput.style.fontFamily)) {
            textLayouts += node to res
          }
        }
      }
    }
    if (textLayouts.isEmpty()) return ExtractedTextAndColor()

    data class SubTextEntry(
      val res: TextLayoutResult,
      val rectsPx: List<Rect>,
      val family: String?,
      val role: String,
    )

    val allLineRectsPx = ArrayList<Rect>()
    var truncated = false
    var overflow = false
    var undrawn = false
    var maxLines = 1
    var combinedInk: Rect? = null

    val entries = textLayouts.map { (semNode, res) ->
      val style = res.layoutInput.style
      val family = resolveFontFamilyName(style.fontFamily, surface)
      val role = inferTypeRole(style.fontSize.value, style.fontWeight?.weight ?: 400, surface)
      maxLines = maxOf(maxLines, res.lineCount)
      if (hasActualTextOverflow(res)) overflow = true
      if ((0 until res.lineCount).any { res.isLineEllipsized(it) }) truncated = true
      val origin = semNode.positionInFrame(rootWindowPos)
      val rectsPx = ArrayList<Rect>()
      var nodeInk: Rect? = null
      for (line in 0 until res.lineCount) {
        val left = res.getLineLeft(line)
        val top = res.getLineTop(line)
        val right = res.getLineRight(line)
        val bottom = res.getLineBottom(line)
        val r = Rect(origin.x + left, origin.y + top, origin.x + right, origin.y + bottom)
        if (r.width > 0f && r.height > 0f) {
          rectsPx += r
          allLineRectsPx += r
          nodeInk = if (nodeInk == null) r else unionRect(nodeInk, r)
        }
      }
      if (nodeInk == null && res.layoutInput.text.text.isNotBlank()) undrawn = true
      if (nodeInk != null) {
        combinedInk = if (combinedInk == null) nodeInk else unionRect(combinedInk, nodeInk)
      }
      SubTextEntry(res = res, rectsPx = rectsPx, family = family, role = role)
    }

    var worstEntry: SubTextEntry? = null
    var worstSample: SampledContrast? = null
    var worstMargin = Double.POSITIVE_INFINITY
    if (bitmap != null && allLineRectsPx.isNotEmpty()) {
      for (entry in entries) {
        if (entry.rectsPx.isEmpty()) continue
        val sample =
          PixelContrastSampler.sample(
            bitmap = bitmap,
            fgColor = entry.res.layoutInput.style.color,
            lineRectsPx = entry.rectsPx,
            excludeRectsPx = allLineRectsPx,
          ) ?: continue
        val margin = sample.ratio - Contrast.requiredRatio(surfaceSpec, entry.role)
        if (margin < worstMargin) {
          worstMargin = margin
          worstEntry = entry
          worstSample = sample
        }
      }
    }

    val chosenEntry = worstEntry ?: entries.first()
    val rawText = textLayouts.first().second.layoutInput.text.text

    return ExtractedTextAndColor(
      textInfo =
        TextInfo(
          value = rawText,
          lines = maxLines,
          overflow = overflow,
          truncated = truncated,
          font = chosenEntry.family,
          ink = combinedInk?.toDp(density),
          undrawn = undrawn,
        ),
      roles = NodeRoles(type = chosenEntry.role),
      colors = worstSample?.let { NodeColors(container = it.bgHex, content = it.fgHex) },
    )
  }

  /**
   * Checks real visual overflow without tripping `didOverflowWidth` on `ParagraphLayoutCache`
   * snapshots whose `MultiParagraph.width` equals the parent `constraints.maxWidth` rather than the
   * tight measured `res.size.width`.
   */
  private fun hasActualTextOverflow(res: TextLayoutResult): Boolean {
    if (res.didOverflowHeight || res.multiParagraph.didExceedMaxLines) return true
    val maxWidthPx = res.size.width + 0.5f
    if ((0 until res.lineCount).any { res.getLineRight(it) - res.getLineLeft(it) > maxWidthPx }) {
      return true
    }
    return !res.layoutInput.softWrap && res.multiParagraph.maxIntrinsicWidth > maxWidthPx
  }

  private fun isLabelControl(type: String, surfaceSpec: SurfaceSpec): Boolean =
    type in surfaceSpec.labelTypes

  private fun isInteractive(
    type: String,
    surfaceSpec: SurfaceSpec,
    semantics: List<SemanticsNode>,
  ): Boolean =
    type in surfaceSpec.interactiveTypes ||
      semantics.any {
        it.config.contains(SemanticsActions.OnClick) ||
          it.config.contains(SemanticsProperties.ToggleableState)
      }

  private fun resolveFontFamilyName(
    fontFamily: FontFamily?,
    surface: BoardSurface,
  ): String? {
    ProposalFonts.faceOf(fontFamily)?.let {
      return it.family
    }
    if (
      fontFamily == null ||
        fontFamily == FontFamily.Default ||
        (surface == BoardSurface.Wear && fontFamily in WEAR_DEFAULT_FAMILIES)
    ) {
      return if (surface == BoardSurface.Wear) WearThemeResolver.DEFAULT_FAMILY
      else "PlatformDefault"
    }
    return "CustomFont"
  }

  private val WEAR_DEFAULT_FAMILIES: Set<FontFamily> by lazy {
    val t = androidx.wear.compose.material3.Typography()
    setOfNotNull(
      t.displayLarge.fontFamily,
      t.displayMedium.fontFamily,
      t.displaySmall.fontFamily,
      t.titleLarge.fontFamily,
      t.titleMedium.fontFamily,
      t.titleSmall.fontFamily,
      t.labelLarge.fontFamily,
      t.labelMedium.fontFamily,
      t.labelSmall.fontFamily,
      t.bodyLarge.fontFamily,
      t.bodyMedium.fontFamily,
      t.bodySmall.fontFamily,
      t.bodyExtraSmall.fontFamily,
      t.numeralExtraLarge.fontFamily,
      t.numeralLarge.fontFamily,
      t.numeralMedium.fontFamily,
      t.numeralSmall.fontFamily,
      t.numeralExtraSmall.fontFamily,
    )
  }

  private fun inferTypeRole(sizeSp: Float, weight: Int, surface: BoardSurface): String {
    if (surface == BoardSurface.Wear) {
      return when {
        sizeSp >= 36f -> "displayLarge"
        sizeSp >= 28f -> "displayMedium"
        sizeSp >= 22f -> "displaySmall"
        sizeSp >= 18f -> if (weight >= 700) "headlineSmall" else "titleLarge"
        sizeSp >= 15.5f -> "bodyLarge"
        else -> "labelMedium"
      }
    }
    return when {
      sizeSp >= 50f -> TypeRole.DisplayLarge.wire
      sizeSp >= 40f -> TypeRole.DisplayMedium.wire
      sizeSp >= 34f -> TypeRole.DisplaySmall.wire
      sizeSp >= 30f -> TypeRole.HeadlineLarge.wire
      sizeSp >= 26f -> TypeRole.HeadlineMedium.wire
      sizeSp >= 23f -> TypeRole.HeadlineSmall.wire
      sizeSp >= 20f -> TypeRole.TitleLarge.wire
      sizeSp >= 15f && weight >= 500 -> TypeRole.TitleMedium.wire
      sizeSp >= 15f -> TypeRole.BodyLarge.wire
      else -> TypeRole.BodyMedium.wire
    }
  }

  private fun extractWeight(
    layout: LayoutInfo,
    parentSite: CallSite?,
    density: Float,
  ): NodeWeight? {
    val weightMod =
      layout.getModifierInfo().firstOrNull { info ->
        (info.modifier as? InspectableValue)?.nameFallback == "weight"
      } ?: return null
    val iv = weightMod.modifier as? InspectableValue ?: return null
    val rawWeight =
      (iv.valueOverride as? Float)
        ?: iv.inspectableElements.firstOrNull { it.name == "weight" }?.value as? Float
        ?: 1f
    val axis =
      when (parentSite?.mappedType) {
        M3Component.Row.wire,
        WearComponent.Row.wire -> Axis.Width
        else -> Axis.Height
      }
    val measuredPx = if (axis == Axis.Width) layout.width else layout.height
    return NodeWeight(value = rawWeight, axis = axis, size = measuredPx / density)
  }

  private fun extractCornerClip(
    layout: LayoutInfo,
    sizePx: Size,
    boundsDp: Bounds,
    density: Float,
  ): CornerClip? {
    val composeDensity = Density(density)
    for (info in layout.getModifierInfo()) {
      val iv = info.modifier as? InspectableValue ?: continue
      if (iv.nameFallback != "graphicsLayer" && iv.nameFallback != "clip") continue
      val elements = iv.inspectableElements.associate { it.name to it.value }
      val clips = (elements["clip"] as? Boolean) ?: (iv.nameFallback == "clip")
      if (!clips) continue
      val shape = (elements["shape"] as? Shape) ?: (iv.valueOverride as? Shape) ?: continue
      val corner = shape as? CornerBasedShape ?: continue
      val rPx =
        maxOf(
          corner.topStart.toPx(sizePx, composeDensity),
          corner.topEnd.toPx(sizePx, composeDensity),
          corner.bottomStart.toPx(sizePx, composeDensity),
          corner.bottomEnd.toPx(sizePx, composeDensity),
        )
      if (rPx <= 0f) continue
      val capped = minOf(rPx / density, boundsDp.width / 2f, boundsDp.height / 2f)
      return CornerClip(capped)
    }
    return null
  }

  private fun extractA11y(interactive: Boolean, node: SemanticsNode?): A11yInfo? {
    if (node == null) return if (interactive) A11yInfo(null, null, emptyList()) else null
    val cfg = node.config
    val role = cfg.getOrNull(SemanticsProperties.Role)?.toString()
    val label =
      cfg.getOrNull(SemanticsProperties.ContentDescription)?.joinToString(" ")?.ifBlank { null }
        ?: cfg.getOrNull(SemanticsProperties.Text)?.joinToString(" ") { it.text }?.ifBlank { null }
    val actions = buildList {
      if (cfg.contains(SemanticsActions.OnClick)) add("click")
      if (cfg.contains(SemanticsActions.OnLongClick)) add("longClick")
      if (cfg.contains(SemanticsProperties.ToggleableState) && !contains("click")) add("toggle")
      if (cfg.contains(SemanticsActions.SetProgress)) add("setProgress")
      if (cfg.contains(SemanticsActions.SetText)) add("setText")
    }
    if (role == null && label == null && actions.isEmpty()) return null
    return A11yInfo(role, label, actions)
  }

  private fun measureLayoutRectPx(
    layout: LayoutInfo,
    rootCoordinates: LayoutCoordinates?,
    rootWindowPos: Offset,
  ): Rect {
    val coords = layout.coordinates
    if (rootCoordinates != null && rootCoordinates.isAttached) {
      try {
        return rootCoordinates.localBoundingBoxOf(coords, clipBounds = false)
      } catch (_: IllegalArgumentException) {
        val pos = coords.localToWindow(Offset.Zero) - rootWindowPos
        return Rect(pos.x, pos.y, pos.x + layout.width, pos.y + layout.height)
      }
    }
    val pos = coords.localToWindow(Offset.Zero) - rootWindowPos
    return Rect(pos.x, pos.y, pos.x + layout.width, pos.y + layout.height)
  }

  private fun clipWithin(semanticsRect: Rect, layoutRect: Rect): Rect {
    val left =
      if (semanticsRect.width > layoutRect.width + 0.5f) layoutRect.left else semanticsRect.left
    val top =
      if (semanticsRect.height > layoutRect.height + 0.5f) layoutRect.top else semanticsRect.top
    val right =
      if (semanticsRect.width > layoutRect.width + 0.5f) layoutRect.right else semanticsRect.right
    val bottom =
      if (semanticsRect.height > layoutRect.height + 0.5f) layoutRect.bottom
      else semanticsRect.bottom
    return Rect(left, top, right, bottom)
  }

  private fun unionRect(a: Rect, b: Rect): Rect =
    Rect(
      minOf(a.left, b.left),
      minOf(a.top, b.top),
      maxOf(a.right, b.right),
      maxOf(a.bottom, b.bottom),
    )

  private fun Rect.toDp(density: Float): Bounds =
    Bounds(left / density, top / density, width / density, height / density)

  private fun SemanticsNode.boundsInFrame(rootWindowPos: Offset): Rect =
    boundsInWindow.translate(-rootWindowPos)

  private fun SemanticsNode.positionInFrame(rootWindowPos: Offset): Offset =
    positionInWindow - rootWindowPos

  private fun SemanticsNode.touchBoundsInFrame(rootWindowPos: Offset): Rect =
    touchBoundsInRoot.translate(positionInWindow - positionInRoot - rootWindowPos)

  private fun indexSemantics(
    nodes: List<SemanticsNode>,
    out: IdentityHashMap<LayoutInfo, MutableList<SemanticsNode>>,
    paneRoots: IdentityHashMap<LayoutInfo, String>,
  ) {
    for (node in nodes) {
      out.getOrPut(node.layoutInfo) { mutableListOf() } += node
      node.config.getOrNull(ExplorePaneKey)?.let { paneId ->
        paneRoots[node.layoutInfo] = paneId
      }
      indexSemantics(node.children, out, paneRoots)
    }
  }

  private fun resolvePaneId(
    layout: LayoutInfo,
    paneRoots: IdentityHashMap<LayoutInfo, String>,
  ): String? {
    var cursor: LayoutInfo? = layout
    while (cursor != null) {
      paneRoots[cursor]?.let {
        return it
      }
      cursor = cursor.parentInfo
    }
    return null
  }

  private fun indexMerged(node: SemanticsNode, out: IdentityHashMap<LayoutInfo, SemanticsNode>) {
    out.putIfAbsent(node.layoutInfo, node)
    node.children.forEach { indexMerged(it, out) }
  }

  private fun semanticSignature(
    layout: LayoutInfo,
    claimed: IdentityHashMap<LayoutInfo, CallSite>,
    semanticsByLayout: IdentityHashMap<LayoutInfo, MutableList<SemanticsNode>>,
    mergedByLayout: IdentityHashMap<LayoutInfo, SemanticsNode>,
  ): String? {
    val mergedText =
      mergedByLayout[layout]?.let { sem ->
        val txt = sem.config.getOrNull(SemanticsProperties.Text)?.joinToString(" ") { it.text }
        val desc = sem.config.getOrNull(SemanticsProperties.ContentDescription)?.joinToString(" ")
        listOfNotNull(txt?.ifBlank { null }, desc?.ifBlank { null }).joinToString("|").ifBlank {
          null
        }
      }
    if (mergedText != null) return mergedText
    val owned = collectOwnedSemantics(layout, claimed, semanticsByLayout)
    val parts = owned.mapNotNull { sem ->
      val txt = sem.config.getOrNull(SemanticsProperties.Text)?.joinToString(" ") { it.text }
      val desc = sem.config.getOrNull(SemanticsProperties.ContentDescription)?.joinToString(" ")
      listOfNotNull(txt?.ifBlank { null }, desc?.ifBlank { null }).joinToString("|").ifBlank {
        null
      }
    }
    return parts.joinToString("|").ifBlank { null }
  }

  private fun computeLineStarts(text: String): IntArray {
    val starts = ArrayList<Int>()
    starts += 0
    for (i in text.indices) {
      if (text[i] == '\n') starts += i + 1
    }
    return starts.toIntArray()
  }

  private fun lineFromOffset(lineStarts: IntArray, offset: Int): Int {
    val idx = lineStarts.binarySearch(offset)
    return if (idx >= 0) idx + 1 else (-idx - 1)
  }

  private val CONTAINER_CLIP_TYPES =
    setOf(
      M3Component.Card.wire,
      M3Component.Box.wire,
      M3Component.Column.wire,
      M3Component.Row.wire,
      M3Component.Text.wire,
      WearComponent.Card.wire,
      WearComponent.TitleCard.wire,
      WearComponent.AppCard.wire,
    )

  private val WEAR_RING_TYPES =
    setOf(
      WearComponent.CircularProgressIndicator.wire,
      WearComponent.SegmentedCircularProgressIndicator.wire,
    )

  private val MATERIAL_SURFACE_WRAPPERS =
    setOf(
      "Card",
      "ElevatedCard",
      "OutlinedCard",
      "Button",
      "ElevatedButton",
      "FilledTonalButton",
      "OutlinedButton",
      "TextButton",
      "FloatingActionButton",
      "ExtendedFloatingActionButton",
      "MediumFloatingActionButton",
      "LargeFloatingActionButton",
      "SmallFloatingActionButton",
      "ToggleFloatingActionButton",
      "ListItem",
      "BottomAppBar",
      "FlexibleBottomAppBar",
      "ModalBottomSheet",
      "AlertDialog",
      "BasicAlertDialog",
      "DropdownMenuPopup",
    )

  private val SKIPPED_FRAMEWORK_NAMES =
    setOf(
      "InspectedSubcomposition",
      "ExploreBoardFrame",
      "ProvideInertLocals",
      "ProposalTheme",
      "ProvideWearTheme",
      "WearDevice",
      "NavDisplay",
      "LocalInspectionTables",
      "CompositionLocalProvider",
      "LaunchedEffect",
      "DisposableEffect",
      "SideEffect",
      "SubcomposeLayout",
    )

  private val CROSS_LAYER_CALL_NAMES =
    setOf(
      "Dialog",
      "AlertDialog",
      "BasicAlertDialog",
      "ConfirmationDialog",
      "ModalBottomSheet",
      "DatePickerDialog",
      "TimePickerDialog",
      "VibrantTimePickerDialog",
      "DropdownMenu",
      "DropdownMenuPopup",
      "ExposedDropdownMenu",
      "ExpandedFullScreenSearchBar",
      "ExpandedFullScreenContainedSearchBar",
      "ExpandedDockedSearchBar",
      "ExpandedDockedSearchBarWithGap",
    )

  private val LAYER_CONTAINER_FUNCTIONS =
    setOf(
      "Surface",
      "AlertDialogContent",
      "ConfirmationDialogContent",
      "DropdownMenuPopupContent",
      "DropdownMenuContent",
    )

  private val CHROME_CALL_NAMES =
    setOf(
      "ShortNavigationBar",
      "NavigationBar",
      "ShortNavigationBarItem",
      "NavigationBarItem",
      "AnimatedNavigationItem",
      "WideNavigationRail",
      "ModalWideNavigationRail",
      "NavigationRail",
      "WideNavigationRailItem",
      "NavigationRailItem",
      "NavigationDrawerItem",
      "TimeText",
      "Dialog",
      "ModalBottomSheet",
      "AlertDialog",
      "BasicAlertDialog",
      "ConfirmationDialog",
      "DatePickerDialog",
      "TimePickerDialog",
      "VibrantTimePickerDialog",
      "DropdownMenu",
      "DropdownMenuPopup",
      "ExposedDropdownMenu",
      "ExpandedFullScreenSearchBar",
      "ExpandedFullScreenContainedSearchBar",
      "ExpandedDockedSearchBar",
      "ExpandedDockedSearchBarWithGap",
      "PlainTooltip",
      "RichTooltip",
    )
}
