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
import bot.charlies.androiddesign.preview.domain.findings.FindingRules
import bot.charlies.androiddesign.preview.feature.preview.theme.ProposalFonts
import bot.charlies.androiddesign.preview.feature.preview.theme.wear.WearThemeResolver
import bot.charlies.androiddesign.preview.kit.AD_PRIMARY_TAG
import bot.charlies.androiddesign.preview.kit.BoardSurface
import bot.charlies.androiddesign.preview.model.A11yInfo
import bot.charlies.androiddesign.preview.model.Axis
import bot.charlies.androiddesign.preview.model.Bounds
import bot.charlies.androiddesign.preview.model.Catalog
import bot.charlies.androiddesign.preview.model.Catalogs
import bot.charlies.androiddesign.preview.model.CornerClip
import bot.charlies.androiddesign.preview.model.LayoutNode
import bot.charlies.androiddesign.preview.model.LayoutTree
import bot.charlies.androiddesign.preview.model.M3Component
import bot.charlies.androiddesign.preview.model.NavigationNodes
import bot.charlies.androiddesign.preview.model.NodeColors
import bot.charlies.androiddesign.preview.model.NodeRoles
import bot.charlies.androiddesign.preview.model.NodeWeight
import bot.charlies.androiddesign.preview.model.RingInset
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

  private data class FrameContext(
    val sourceFile: String?,
    val lineNumber: Int?,
    val rawName: String?,
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

      if (isTargetCall && fnName != null) {
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
          ),
        )
      }
    }

    tables.forEach { table ->
      table.compositionGroups.forEach { root -> walkGroup(root, null) }
    }

    val semanticsByLayout = IdentityHashMap<LayoutInfo, MutableList<SemanticsNode>>()
    val mergedByLayout = IdentityHashMap<LayoutInfo, SemanticsNode>()
    indexSemantics(unmergedRoots, semanticsByLayout)
    mergedRoots.forEach { root -> indexMerged(root, mergedByLayout) }

    val orderedLayouts = claimed.entries.sortedBy { it.value.order }.map { it.key }
    val idByLayout = IdentityHashMap<LayoutInfo, String>()
    val usedIds = HashSet<String>()
    for (layout in orderedLayouts) {
      val site = claimed.getValue(layout)
      val typeDecap = site.mappedType.replaceFirstChar { it.lowercaseChar() }
      val sig = semanticSignature(layout, claimed, semanticsByLayout, mergedByLayout)
      val suffix =
        if (!sig.isNullOrBlank()) {
          (sig.hashCode().toLong() and 0xFFFFFL).toString(16)
        } else {
          "s${layout.semanticsId}"
        }
      val lineSuffix = site.sourceLine?.let { "@L$it" }.orEmpty()
      val baseId = "${typeDecap}_$suffix"
      var unique = "$baseId$lineSuffix"
      var dup = 2
      while (!usedIds.add(unique)) {
        unique = "${baseId}_${dup++}$lineSuffix"
      }
      idByLayout[layout] = unique
    }

    val catalog = Catalogs.forView(view)
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
        catalog = catalog,
        surface = surface,
        view = view,
        density = density,
      )
    }

    val rawTree =
      LayoutTree(screen = screen, view = view, nodes = layoutNodes, findings = emptyList())
    val findings =
      FindingRules.evaluate(rawTree, catalog = catalog, emptyDetailLists = emptyDetailLists)
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
    catalog: Catalog,
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
    val ownedSemantics = collectOwnedSemantics(layout, claimed, semanticsByLayout)
    val mergedNode = mergedByLayout[layout] ?: ownedSemantics.firstOrNull()
    val primary = ownedSemantics.any {
      it.config.getOrNull(SemanticsProperties.TestTag) == AD_PRIMARY_TAG
    }
    val scrollsVertically = ownedSemantics.any {
      it.config.contains(SemanticsProperties.VerticalScrollAxisRange)
    }
    val scrollsHorizontally = ownedSemantics.any {
      it.config.contains(SemanticsProperties.HorizontalScrollAxisRange)
    }
    val interactive = isInteractive(site.mappedType, catalog, ownedSemantics)

    val visibleBoundsPx =
      ownedSemantics.firstOrNull()?.boundsInRoot?.let { sRect -> clipWithin(sRect, layoutRectPx) }
        ?: layoutRectPx
    val touchPx =
      if (!interactive) null
      else {
        val semTouch =
          ownedSemantics
            .map { it.touchBoundsInRoot }
            .fold<Rect, Rect?>(null) { acc, r -> if (acc == null) r else unionRect(acc, r) }
        semTouch?.let { unionRect(it, layoutRectPx) } ?: layoutRectPx
      }
    val boundsDp = visibleBoundsPx.toDp(density)

    val textData =
      extractTextAndContrast(
        type = site.mappedType,
        ownedSemantics = ownedSemantics,
        bitmap = bitmap,
        surface = surface,
        catalog = catalog,
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
      if (candidateLayout === rootLayout) continue
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
    bitmap: Bitmap?,
    surface: BoardSurface,
    catalog: Catalog,
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
      if (isLabelControl(type, catalog)) {
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

    val (firstNode, firstRes) = textLayouts.first()
    val rawText = firstRes.layoutInput.text.text
    val style = firstRes.layoutInput.style
    val family = resolveFontFamilyName(style.fontFamily, surface)
    val inferredRole = inferTypeRole(style.fontSize.value, style.fontWeight?.weight ?: 400, surface)

    val lineRectsPx = ArrayList<Rect>()
    var truncated = false
    var overflow = false
    var undrawn = false
    var maxLines = 1
    var combinedInk: Rect? = null

    for ((semNode, res) in textLayouts) {
      maxLines = maxOf(maxLines, res.lineCount)
      if (hasActualTextOverflow(res)) overflow = true
      if ((0 until res.lineCount).any { res.isLineEllipsized(it) }) truncated = true
      val origin = semNode.positionInRoot
      var nodeInk: Rect? = null
      for (line in 0 until res.lineCount) {
        val left = res.getLineLeft(line)
        val top = res.getLineTop(line)
        val right = res.getLineRight(line)
        val bottom = res.getLineBottom(line)
        val r = Rect(origin.x + left, origin.y + top, origin.x + right, origin.y + bottom)
        if (r.width > 0f && r.height > 0f) {
          lineRectsPx += r
          nodeInk = if (nodeInk == null) r else unionRect(nodeInk, r)
        }
      }
      if (nodeInk == null && res.layoutInput.text.text.isNotBlank()) undrawn = true
      if (nodeInk != null) {
        combinedInk = if (combinedInk == null) nodeInk else unionRect(combinedInk, nodeInk)
      }
    }

    val sampled =
      if (bitmap != null && lineRectsPx.isNotEmpty()) {
        PixelContrastSampler.sample(bitmap, style.color, lineRectsPx)
      } else {
        null
      }

    return ExtractedTextAndColor(
      textInfo =
        TextInfo(
          value = rawText,
          lines = maxLines,
          overflow = overflow,
          truncated = truncated,
          font = family,
          ink = combinedInk?.toDp(density),
          undrawn = undrawn,
        ),
      roles = NodeRoles(type = inferredRole),
      colors = sampled?.let { NodeColors(container = it.bgHex, content = it.fgHex) },
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

  private fun isLabelControl(type: String, catalog: Catalog): Boolean =
    type in NavigationNodes.itemTypes ||
      catalog.find(type)?.let { it.interactive && it.prop("label") != null } == true

  private fun isInteractive(
    type: String,
    catalog: Catalog,
    semantics: List<SemanticsNode>,
  ): Boolean =
    type in NavigationNodes.itemTypes ||
      catalog.find(type)?.interactive == true ||
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

  private fun indexSemantics(
    nodes: List<SemanticsNode>,
    out: IdentityHashMap<LayoutInfo, MutableList<SemanticsNode>>,
  ) {
    for (node in nodes) {
      out.getOrPut(node.layoutInfo) { mutableListOf() } += node
      indexSemantics(node.children, out)
    }
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
      "ListItem",
      "ModalBottomSheet",
      "AlertDialog",
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

  private val CHROME_CALL_NAMES =
    setOf(
      "ShortNavigationBar",
      "NavigationBar",
      "ShortNavigationBarItem",
      "NavigationBarItem",
      "WideNavigationRail",
      "NavigationRail",
      "WideNavigationRailItem",
      "NavigationRailItem",
      "TimeText",
      "ModalBottomSheet",
      "AlertDialog",
      "ConfirmationDialog",
    )
}
