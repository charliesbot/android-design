/*
 * Copyright 2021 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package androidx.wear.compose.foundation

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path as ComposePath
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.platform.LocalFontFamilyResolver
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontSynthesis
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.platform.FontLoadResult
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.isSpecified
import androidx.compose.ui.unit.isUnspecified
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sqrt
import org.jetbrains.skia.Font as SkiaFont
import org.jetbrains.skia.Paint as SkiaPaint
import org.jetbrains.skia.RSXform
import org.jetbrains.skia.TextBlob
import org.jetbrains.skia.Typeface as SkiaTypeface

/**
 * [basicCurvedText] is a component allowing developers to easily write curved text following the
 * curvature of a circle (usually at the edge of a circular screen). [basicCurvedText] can be only
 * created within a [CurvedLayout] since it's not a composable.
 *
 * @sample androidx.wear.compose.foundation.samples.CurvedAndNormalText
 * @param text The text to display
 * @param modifier The [CurvedModifier] to apply to this curved text.
 * @param angularDirection Specify if the text is laid out clockwise or anti-clockwise, and if those
 *   need to be reversed in a Rtl layout. If not specified, it will be inherited from the enclosing
 *   [curvedRow] or [CurvedLayout] See [CurvedDirection.Angular].
 * @param overflow How visual overflow should be handled. Note that this takes into account only
 *   explicit size curved modifiers in this element, to size this element matching the parent's, add
 *   a CurvedModifier.weight here.
 * @param style A @Composable factory to provide the style to use. This composable SHOULDN'T
 *   generate any compose nodes.
 */
public fun CurvedScope.basicCurvedText(
  text: String,
  modifier: CurvedModifier = CurvedModifier,
  angularDirection: CurvedDirection.Angular? = null,
  overflow: TextOverflow = TextOverflow.Clip,
  style: @Composable () -> CurvedTextStyle = { CurvedTextStyle() },
): Unit =
  add(
    CurvedTextChild(
      text,
      curvedLayoutDirection.copy(overrideAngular = angularDirection).absoluteClockwise(),
      style,
      overflow,
    ),
    modifier,
  )

/**
 * [basicCurvedText] is a component allowing developers to easily write curved text following the
 * curvature of a circle (usually at the edge of a circular screen). [basicCurvedText] can be only
 * created within a [CurvedLayout] since it's not a composable.
 *
 * @sample androidx.wear.compose.foundation.samples.CurvedAndNormalText
 * @param text The text to display
 * @param style A style to use.
 * @param modifier The [CurvedModifier] to apply to this curved text.
 * @param angularDirection Specify if the text is laid out clockwise or anti-clockwise, and if those
 *   need to be reversed in a Rtl layout. If not specified, it will be inherited from the enclosing
 *   [curvedRow] or [CurvedLayout] See [CurvedDirection.Angular].
 * @param overflow How visual overflow should be handled.
 */
public fun CurvedScope.basicCurvedText(
  text: String,
  style: CurvedTextStyle,
  modifier: CurvedModifier = CurvedModifier,
  angularDirection: CurvedDirection.Angular? = null,
  overflow: TextOverflow = TextOverflow.Clip,
): Unit = basicCurvedText(text, modifier, angularDirection, overflow) { style }

internal class CurvedTextChild(
  val text: String,
  val clockwise: Boolean = true,
  val style: @Composable () -> CurvedTextStyle = { CurvedTextStyle() },
  val overflow: TextOverflow,
) : CurvedChild() {
  private lateinit var delegate: CurvedTextDelegate
  private lateinit var actualStyle: CurvedTextStyle

  // We create a compose-ui node so that we can attach a11y info.
  private lateinit var placeable: Placeable

  @Composable
  override fun SubComposition(semanticProperties: CurvedSemanticProperties) {
    actualStyle = DefaultCurvedTextStyles + style()

    // Avoid recreating the delegate if possible, as it's expensive
    delegate = remember { CurvedTextDelegate() }
    delegate.UpdateFontIfNeeded(
      actualStyle.fontFamily,
      actualStyle.fontWeight,
      actualStyle.fontStyle,
      actualStyle.fontSynthesis,
    )

    val mergedSemantics =
      semanticProperties.merge(CurvedSemanticProperties(contentDescription = text))

    // Empty compose-ui node to attach a11y info.
    Box(Modifier.semantics { with(mergedSemantics) { applySemantics() } })
  }

  override fun CurvedMeasureScope.initializeMeasure(
    measurables: Iterator<Measurable>,
  ): (Placeable.PlacementScope).() -> Unit {
    if (isLookingAhead) {
      // TODO(b/486792667): Investigate properly supporting Lookahead animations.
      val lookaheadPlaceable = measurables.next().measure(Constraints())
      return { lookaheadPlaceable.place(0, 0) }
    }
    delegate.updateIfNeeded(
      text,
      clockwise,
      actualStyle.fontSize.toPx(),
      if (clockwise || actualStyle.letterSpacingCounterClockwise.isUnspecified)
        actualStyle.letterSpacing
      else actualStyle.letterSpacingCounterClockwise,
      density,
      if (actualStyle.lineHeight.isSpecified) actualStyle.lineHeight.toPx() else -1f,
      actualStyle.warpOffset,
    )

    // Size the compose-ui node reasonably.

    // Heuristic calculations of maxWidth:
    // 1. Find the text's center point. This is offset from the circle's center by a distance
    //    equal to:
    //    radius - (textHeight / 2)
    // 2. Construct a perpendicular line from the text's center point, extending it until it
    //    intersects the circle's circumference.
    // 3. Using the Pythagorean theorem, we can determine half of the desired width. We know:
    //    * The circle's radius
    //    * The distance of the text from the circle's center (calculated in step 1)
    // 4. The Pythagorean equation in this context is:
    //    radius^2 = (radius - textHeight/2)^2 + (maxWidth/2)^2
    // 5. Solving for maxWidth, we get:
    //    maxWidth = 2 * sqrt( textHeight * (radius - textHeight/4) )

    val height = delegate.textHeight.roundToInt()
    val maxWidth = 2 * sqrt(height * (radius - height / 4))
    val width = delegate.textWidth.coerceAtMost(maxWidth).roundToInt()

    // Measure the corresponding measurable.
    placeable =
      measurables
        .next()
        .measure(
          Constraints(
            minWidth = width,
            maxWidth = width,
            minHeight = height,
            maxHeight = height,
          ),
        )
    return {
      // clockwise doesn't matter, we have no content in placeable.
      place(placeable, layoutInfo!!, parentSweepRadians, clockwise = false)
    }
  }

  override fun doEstimateThickness(maxRadius: Float): Float = delegate.textHeight

  override fun doRadialPosition(
    parentOuterRadius: Float,
    parentThickness: Float,
  ): PartialLayoutInfo {
    val baselineRadius = parentOuterRadius - delegate.baseLinePosition
    // getMeasureOffset is 0 when there is no warping, and the warping offset when there is
    // this defines the horizontal line at which the text will maintain its width (lines closer
    // to the center will be shrunk and lines further away will be stretched).
    val realMeasureRadius =
      baselineRadius + delegate.getMeasureOffset() * (if (clockwise) 1f else -1f)
    return PartialLayoutInfo(
      delegate.textWidth / realMeasureRadius,
      parentOuterRadius,
      delegate.textHeight,
      baselineRadius,
    )
  }

  private var parentSweepRadians: Float = 0f

  override fun doAngularPosition(
    parentStartAngleRadians: Float,
    parentSweepRadians: Float,
    centerOffset: Offset,
  ): Float {
    this.parentSweepRadians = parentSweepRadians
    return super.doAngularPosition(parentStartAngleRadians, parentSweepRadians, centerOffset)
  }

  override fun DrawScope.draw() {
    with(delegate) {
      doDraw(
        layoutInfo!!,
        parentSweepRadians,
        overflow,
        actualStyle.color,
        actualStyle.background,
      )
    }
  }
}

// DESKTOP REPLACEMENT of the tail of upstream BasicCurvedText.kt (everything below the curved text
// element). Upstream draws with android.graphics.TextPaint and Canvas.drawTextOnPath; this version
// places each glyph on the arc with Skia RSXform transforms. Not ported: kerning (glyphs advance by
// their plain widths) and the "warped" renderer (glyph outlines bent along the arc).

/** Used to cache computations and objects with expensive construction (Skia font and paths). */
internal class CurvedTextDelegate {
  private var text: String = ""
  private var clockwise: Boolean = true
  private var fontSizePx: Float = 0f
  private var letterSpacing: TextUnit = TextUnit.Unspecified
  private var density: Float = 0f
  private var lastLineHeightPx: Float = 0f

  var textWidth by mutableFloatStateOf(0f)
  var textHeight by mutableFloatStateOf(0f)
  var baseLinePosition = 0f

  private var typeFace: State<SkiaTypeface?> = mutableStateOf(null)
  private var currentTypeface: SkiaTypeface? = null
  private var font = SkiaFont(null, 14f)
  private var letterSpacingPx = 0f

  private val backgroundPath = ComposePath()

  var lastLayoutInfo: CurvedLayoutInfo? = null
  var lastParentSweepRadians: Float = 0f

  private var blob: TextBlob? = null

  fun updateIfNeeded(
    text: String,
    clockwise: Boolean,
    fontSizePx: Float,
    letterSpacing: TextUnit,
    density: Float,
    lineHeightPx: Float,
    warpOffset: CurvedTextStyle.WarpOffset,
  ) {
    var needsUpdate = false
    if (
      text != this.text ||
        clockwise != this.clockwise ||
        fontSizePx != this.fontSizePx ||
        letterSpacing != this.letterSpacing ||
        density != this.density ||
        lineHeightPx != lastLineHeightPx
    ) {
      this.text = text
      this.clockwise = clockwise
      this.fontSizePx = fontSizePx
      this.letterSpacing = letterSpacing
      this.density = density
      this.lastLineHeightPx = lineHeightPx
      font.size = fontSizePx
      letterSpacingPx =
        when (letterSpacing.type) {
          TextUnitType.Em -> letterSpacing.value * fontSizePx
          TextUnitType.Sp -> letterSpacing.value * density
          else -> 0f
        }
      needsUpdate = true
    }
    if (needsUpdate) {
      updateMeasures()
      lastLayoutInfo = null
    }
  }

  /** Always 0: the desktop renderer does not warp. */
  fun getMeasureOffset() = 0f

  @Composable
  fun UpdateFontIfNeeded(
    fontFamily: FontFamily?,
    fontWeight: FontWeight?,
    fontStyle: FontStyle?,
    fontSynthesis: FontSynthesis?,
  ) {
    val fontFamilyResolver = LocalFontFamilyResolver.current
    typeFace =
      remember(fontFamily, fontWeight, fontStyle, fontSynthesis, fontFamilyResolver) {
        derivedStateOf {
          val resolved =
            fontFamilyResolver
              .resolve(
                fontFamily,
                fontWeight ?: FontWeight.Normal,
                fontStyle ?: FontStyle.Normal,
                fontSynthesis ?: FontSynthesis.All,
              )
              .value
          (resolved as? FontLoadResult)?.typeface
        }
      }
    updateTypeFace()
  }

  private fun glyphs(s: String) = font.getStringGlyphs(s)

  private fun measure(s: String): Float {
    val widths = font.getWidths(glyphs(s))
    return widths.sum() + letterSpacingPx * s.length
  }

  private fun updateMeasures() {
    textWidth = measure(text)
    val metrics = font.metrics
    // Note that ascent is negative, since it's above the baseline (which is at 0).
    val height = metrics.descent - metrics.ascent
    val diff = if (lastLineHeightPx >= 0f) (lastLineHeightPx - height) else 0f
    val actualAscent = -metrics.ascent + diff / 2
    val actualDescent = metrics.descent + diff / 2
    textHeight = actualAscent + actualDescent
    baseLinePosition = if (clockwise) actualAscent else actualDescent
  }

  private fun updateTypeFace() {
    val resolved = typeFace.value
    if (resolved != null && resolved != currentTypeface) {
      currentTypeface = resolved
      font = SkiaFont(resolved, fontSizePx.takeIf { it > 0f } ?: 14f)
      updateMeasures()
      lastLayoutInfo = null
    }
  }

  private fun updatePathsIfNeeded(
    layoutInfo: CurvedLayoutInfo,
    parentSweepRadians: Float,
    drawnText: String,
  ) {
    if (layoutInfo != lastLayoutInfo || abs(lastParentSweepRadians - parentSweepRadians) > 1e-4) {
      lastLayoutInfo = layoutInfo
      lastParentSweepRadians = parentSweepRadians
    }
    with(layoutInfo) {
      val sweepDegree = min(sweepRadians, parentSweepRadians).toDegrees().coerceAtMost(360f)
      val centerX = centerOffset.x
      val centerY = centerOffset.y
      backgroundPath.reset()
      backgroundPath.arcTo(
        Rect(
          centerX - outerRadius,
          centerY - outerRadius,
          centerX + outerRadius,
          centerY + outerRadius,
        ),
        startAngleRadians.toDegrees(),
        sweepDegree,
        false,
      )
      backgroundPath.arcTo(
        Rect(
          centerX - innerRadius,
          centerY - innerRadius,
          centerX + innerRadius,
          centerY + innerRadius,
        ),
        startAngleRadians.toDegrees() + sweepDegree,
        -sweepDegree,
        false,
      )
      backgroundPath.close()

      blob = buildBlob(drawnText, centerX, centerY, measureRadius, sweepDegree)
    }
  }

  /** One RSXform per glyph: the glyph's midpoint sits on the arc, rotated to the tangent. */
  private fun CurvedLayoutInfo.buildBlob(
    s: String,
    cx: Float,
    cy: Float,
    radius: Float,
    sweepDegree: Float,
  ): TextBlob? {
    if (s.isEmpty()) return null
    val ids = glyphs(s)
    val widths = font.getWidths(ids)
    val startDeg = startAngleRadians.toDegrees() + (if (clockwise) 0f else sweepDegree)
    var advance = 0f
    val xforms =
      Array(ids.size) { i ->
        val mid = advance + widths[i] / 2
        advance += widths[i] + letterSpacingPx
        val theta = (startDeg.toRadians() + (if (clockwise) 1 else -1) * mid / radius).toDouble()
        val px = cx + radius * kotlin.math.cos(theta).toFloat()
        val py = cy + radius * kotlin.math.sin(theta).toFloat()
        val rotation = theta.toFloat() + (if (clockwise) PI.toFloat() / 2 else -PI.toFloat() / 2)
        // Anchor (half the glyph width, baseline) lands on the arc point.
        RSXform.makeFromRadians(1f, rotation, px, py, widths[i] / 2, 0f)
      }
    return TextBlob.makeFromRSXform(ids, xforms, font)
  }

  fun DrawScope.doDraw(
    layoutInfo: CurvedLayoutInfo,
    parentSweepRadians: Float,
    overflow: TextOverflow,
    color: Color,
    background: Color,
  ) {
    updateTypeFace()
    val actualText =
      if (
        layoutInfo.sweepRadians <= parentSweepRadians + 0.001f || overflow == TextOverflow.Visible
      ) {
        text
      } else {
        ellipsize(
          text,
          overflow == TextOverflow.Ellipsis,
          parentSweepRadians * layoutInfo.measureRadius,
        )
      }
    updatePathsIfNeeded(layoutInfo, parentSweepRadians, actualText)

    drawIntoCanvas { canvas ->
      val native = canvas.nativeCanvas
      if (background.isSpecified && background != Color.Transparent) {
        drawPath(backgroundPath, background)
      }
      val b = blob ?: return@drawIntoCanvas
      SkiaPaint().use { p ->
        p.isAntiAlias = true
        p.color = color.toArgb()
        native.drawTextBlob(b, 0f, 0f, p)
      }
    }
  }

  private fun ellipsize(s: String, addEllipsis: Boolean, maxWidth: Float): String {
    val ellipsis = "…"
    var end = s.length
    while (
      end > 0 && measure(s.substring(0, end) + (if (addEllipsis) ellipsis else "")) > maxWidth
    ) {
      end--
    }
    return s.substring(0, end) + (if (addEllipsis && end < s.length) ellipsis else "")
  }
}
