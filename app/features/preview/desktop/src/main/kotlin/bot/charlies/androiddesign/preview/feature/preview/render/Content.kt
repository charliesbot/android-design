package bot.charlies.androiddesign.preview.feature.preview.render

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.carousel.CarouselDefaults
import androidx.compose.material3.carousel.CarouselItemDrawInfo
import androidx.compose.material3.carousel.CarouselItemScope
import androidx.compose.material3.carousel.HorizontalCenteredHeroCarousel
import androidx.compose.material3.carousel.HorizontalMultiBrowseCarousel
import androidx.compose.material3.carousel.HorizontalUncontainedCarousel
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import bot.charlies.androiddesign.preview.domain.CarouselSpec
import bot.charlies.androiddesign.preview.model.TypeRole

@Composable
internal fun RenderText(ctx: NodeContext) {
  val align =
    when (ctx.string("align")) {
      "center" -> TextAlign.Center
      "end" -> TextAlign.End
      "start" -> TextAlign.Start
      else -> null
    }
  val color = ctx.colorRole()?.let(ctx.env::color) ?: Color.Unspecified
  Text(
    text = ctx.string("text").orEmpty(),
    modifier = ctx.modifier(container = true),
    color = color,
    style = ctx.textStyle(TypeRole.BodyLarge),
    textAlign = align,
    maxLines = ctx.whole("maxLines") ?: Int.MAX_VALUE,
    overflow =
      if (ctx.string("overflow") == "ellipsis") TextOverflow.Ellipsis else TextOverflow.Clip,
    onTextLayout = ctx.textReporter(),
  )
}

/** Text paints its style's container behind itself, so that is its backdrop. */
internal fun NodeContext.textLook(ambient: Ambient) =
  Look(
    backdropRole(ambient),
    colorRole() ?: ambient.content,
    typeRole(TypeRole.BodyLarge),
    text = string("text"),
  )

@Composable
internal fun RenderIcon(ctx: NodeContext) {
  val color = ctx.colorRole()?.let(ctx.env::color) ?: Color.Unspecified
  SymbolIcon(
    name = ctx.string("name").orEmpty(),
    modifier = ctx.modifier(),
    size = ctx.numberOrDefault("size").dp,
    tint = color,
    filled = ctx.bool("filled"),
    contentDescription = ctx.string("contentDescription"),
  )
}

internal fun NodeContext.iconLook(ambient: Ambient) =
  Look(ambient.container, colorRole() ?: ambient.content, a11yRole = "Image")

@Composable
internal fun RenderImage(ctx: NodeContext) {
  val bitmap = ctx.string("src")?.let(ctx.env.assets::image)
  val shape = ctx.shapeName()?.let { shapeFor(it) }
  val aspectRatio = ctx.number("aspectRatio")
  var modifier = ctx.modifier()
  if (aspectRatio != null) modifier = modifier.aspectRatio(aspectRatio)
  if (shape != null) modifier = modifier.clip(shape)
  val description = ctx.string("contentDescription")
  val semantics = Modifier.semantics {
    role = Role.Image
    if (description != null) contentDescription = description
  }
  if (bitmap == null) {
    // The file is missing or unreadable: a neutral block keeps the layout honest.
    Box(
      modifier
        .then(semantics)
        .background(ctx.env.neutralFill())
        .then(
          if (aspectRatio == null) Modifier.aspectRatio(1f).fillMaxWidth(0.5f) else Modifier,
        ),
    )
    return
  }
  Image(
    bitmap = bitmap,
    contentDescription = description,
    modifier = modifier,
    contentScale =
      when (ctx.string("fit")) {
        "crop" -> ContentScale.Crop
        "fillBounds" -> ContentScale.FillBounds
        "fillWidth" -> ContentScale.FillWidth
        "fillHeight" -> ContentScale.FillHeight
        "inside" -> ContentScale.Inside
        else -> ContentScale.Fit
      },
  )
}

internal fun NodeContext.imageLook(ambient: Ambient) = Look(ambient, a11yRole = "Image")

private val CarouselPadding = PaddingValues(horizontal = CarouselSpec.PADDING_DP.dp)
private val CarouselNoPadding = PaddingValues(0.dp)
private val CarouselGap = CarouselSpec.GAP_DP.dp
private val CarouselFullScreenGap = CarouselSpec.FULL_SCREEN_GAP_DP.dp
private val CarouselHeroReserved = CarouselSpec.HERO_RESERVED_DP.dp

/** The draw info of the carousel item being composed, or null outside one. */
internal val LocalCarouselItemDrawInfo = compositionLocalOf<CarouselItemDrawInfo?> { null }

internal fun NodeContext.carouselLook(ambient: Ambient) = Look(ambient)

/**
 * Draws each child as an item, clipped with `maskClip` so the carousel's masking and parallax
 * apply. Only the items on screen are composed, as in the app.
 */
private fun NodeContext.carouselItems(
  refs: List<ChildRef>,
): @Composable CarouselItemScope.(Int) -> Unit = { index ->
  // The tree records the item as its mask shows it, not as it is laid out. The mask is relative to
  // the box `maskClip` sits on, outside the item's own padding, so that box is reported with it.
  val itemId = env.idPrefix + refs[index].id + refs[index].scope.instanceSuffix
  val recorder = env.recorder
  var scopeModifier: Modifier = Modifier
  if (recorder != null) {
    val info = carouselItemDrawInfo
    scopeModifier = scopeModifier.onGloballyPositioned { recorder.reportCarouselBox(itemId, it) }
    DisposableEffect(itemId, info) {
      recorder.reportCarouselMask(itemId) { info.maskRect }
      onDispose { recorder.reportCarouselMask(itemId, null) }
    }
  }
  val host =
    ChildHost(
      fillWidth = def.stretchesChildren,
      scopeModifier = scopeModifier.maskClip(MaterialTheme.shapes.extraLarge),
    )
  // The nodes inside the item read its current size from this, to apply `itemSize`.
  CompositionLocalProvider(LocalCarouselItemDrawInfo provides carouselItemDrawInfo) {
    RenderNode(env, refs[index], id, order + listOf(0, index), host.at(index, refs.size))
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun RenderCarousel(ctx: NodeContext) {
  val refs = ctx.props.children("children")
  val state = rememberCarouselState { refs.size }
  val items = ctx.carouselItems(refs)
  val itemWidth = ctx.numberOrDefault("itemWidth").dp
  when (ctx.reader.enum("variant", CarouselSpec::variant)) {
    CarouselSpec.Variant.CenteredHero ->
      HorizontalCenteredHeroCarousel(
        state = state,
        modifier = ctx.modifier(),
        itemSpacing = CarouselGap,
        contentPadding = CarouselPadding,
        content = items,
      )
    CarouselSpec.Variant.Uncontained ->
      HorizontalUncontainedCarousel(
        state = state,
        itemWidth = itemWidth,
        modifier = ctx.modifier(),
        itemSpacing = CarouselGap,
        flingBehavior = CarouselDefaults.noSnapFlingBehavior(),
        contentPadding = CarouselPadding,
        content = items,
      )
    CarouselSpec.Variant.Hero ->
      BoxWithConstraints(ctx.modifier()) {
        HorizontalMultiBrowseCarousel(
          state = state,
          preferredItemWidth = maxWidth - CarouselHeroReserved,
          itemSpacing = CarouselGap,
          flingBehavior = CarouselDefaults.multiBrowseFlingBehavior(state),
          contentPadding = CarouselPadding,
          content = items,
        )
      }
    CarouselSpec.Variant.FullScreen ->
      BoxWithConstraints(ctx.modifier()) {
        HorizontalUncontainedCarousel(
          state = state,
          itemWidth = maxWidth,
          itemSpacing = CarouselFullScreenGap,
          flingBehavior = CarouselDefaults.singleAdvanceFlingBehavior(state),
          contentPadding = CarouselNoPadding,
          content = items,
        )
      }
    CarouselSpec.Variant.MultiBrowse ->
      HorizontalMultiBrowseCarousel(
        state = state,
        preferredItemWidth = itemWidth,
        modifier = ctx.modifier(),
        itemSpacing = CarouselGap,
        flingBehavior = CarouselDefaults.multiBrowseFlingBehavior(state),
        contentPadding = CarouselPadding,
        content = items,
      )
  }
}
