> Archived from `skills/android-design/references/` while widgets are out of the skill's scope. Restore it when widgets get eval cases and a widget catalog.

# Widgets

Home screen widget design from developer.android.com/design/ui/mobile/guides/widgets and Google's widget quality tiers, with Jetpack Glance APIs (glance-appwidget 1.2.0 stable, checked September 2026). Glance renders RemoteViews, so Compose Material components and `MaterialTheme` don't apply inside a widget; use Glance's own theme and components.

## Purpose

- **One primary use case per widget.** Distinct jobs get separate widgets (a steps widget and a streak widget), never one widget per color or style variant.
- A widget shows glanceable information, offers quick actions, or both.

## Canonical layouts

Pick one before designing; Google publishes a Glance sample for each: text only, text and image, search toolbar, toolbar, expressive toolbar, text and image list, checklist, action list, full-bleed image (snap-scrolling on Android 17+), image grid, image and text grid.

## Fill the bounds

Misaligned widgets are a main reason people remove them.

- Rectangular widgets touch **all four** grid edges with no custom outer padding. Custom shapes touch at least two opposing edges.
- Use the system corner radius (`android.R.dimen.system_app_widget_background_radius`); Glance's `Scaffold` applies it and `appWidgetBackground()`.
- Inner elements follow optical roundness: inner radius = outer radius minus padding.

## Style

- **Color:** `GlanceTheme` defaults to dynamic color on Android 12+; pass brand `ColorProviders` for a fallback. Support light and dark. The same emphasis rules as the app apply: one strong fill for the primary action, containers for the rest.
- **Type:** "Get bolder with headlines, labels, and data." A big number or headline is the widget's hero; labels whisper.
- **Shape:** minimal-content widgets (photo, weather, now playing) can take a whole expressive shape; data-heavy widgets use expressive shapes only for hierarchy or the call to action.
- **Header:** app icon always, title when space allows, consistent across the app's widgets.
- Touch targets 48dp; add buttons only when the size has room for them.

## Sizes

Cell sizes vary by device and orientation, so design for several size classes, not one fixed size. Set `targetCellWidth`/`targetCellHeight` (separately for handheld and tablet) to the default grid span, plus min and max sizes, then serve layouts per size with `SizeMode.Responsive(setOf(...))` and read `LocalSize.current`. Adding a column should reveal content (a second list column, a forecast row), not enlarge the same content. Rearrange the layout as the widget grows; never scale text and icons linearly.

Cell estimate on a typical 5x4 handset (Pixel 4, dp, width by height): n x m cells is (73n - 16) x (118m - 16) in portrait and (142n - 15) x (66m - 15) in landscape. For example 2x1 is 130x102 portrait and 269x51 landscape; 4x2 is 276x220 portrait and 554x117 landscape. Portrait widths set `minWidth`, `minResizeWidth`, `maxResizeWidth`; landscape heights set the height attributes.

The minimum size must show a clear use case with 48dp targets. Set a maximum only when a larger size just adds blank space; when it shows more content (a taller scrolling list), leave resizing unrestricted. Differentiated widgets resize to 2x1 and at least two of 1x1, 2x2, 4x2, 4x4.

## Configuration and picker

- Open configuration on placement only when the widget is empty without it; otherwise ship a good default and allow reconfiguring later (long press, or the system configuration entry).
- One or two screens, a live preview of the result, progressive disclosure, no dead ends.
- **Picker previews** are accurate: real layout at real size, in dynamic colors (generated previews via `GlanceAppWidget.providePreview` on Android 15+). A clear name and one-line description. At most 6 to 8 variations.
- Promote pinning in the app at relevant moments, subtly, never blocking the main task.

## States

Design each: loading (a placeholder layout), empty (a clear call to action that still fills larger bounds without sparse areas), signed out, error (shown on the widget itself), and stale data. Update after in-app actions; add manual refresh when data changes faster than the widget updates.

## Quality tiers

| Tier | Meets |
| --- | --- |
| Low quality (fails) | Doesn't fill bounds, poor contrast, no name or preview, stale or cropped content, doesn't update after actions |
| Standard | Aligned; sensible min and max sizes; rearranges instead of scaling; accurate previews; intentional empty and signed-out states; manual refresh when needed |
| Differentiated (aim here) | Fills all four edges; resizes to 2x1 and at least two of 1x1, 2x2, 4x2, 4x4; consistent header; collection and feed widgets scroll vertically and show more items and detail at 4x2 and 4x4; dynamic or app theming in light and dark; previews at the target form factor's proportions with the user's content or system theme; unique name and description; system corner radius; loading state, with errors shown on the widget; system configuration; system launch transition |

## Glance skeleton

```kotlin
class StreakWidget : GlanceAppWidget() {
    override val sizeMode = SizeMode.Responsive(setOf(DpSize(110.dp, 110.dp), DpSize(250.dp, 110.dp)))

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            GlanceTheme { // dynamic color on Android 12+
                Scaffold(titleBar = { TitleBar(startIcon = ImageProvider(R.drawable.ic_logo), title = "Streak") }) {
                    val wide = LocalSize.current.width >= 250.dp
                    // hero number, then a row of history days when wide
                }
            }
        }
    }
}
```

Glance components: `Scaffold`, `TitleBar`, `FilledButton`, `OutlineButton`, `CircleIconButton`, `SquareIconButton` (`androidx.glance.appwidget.components`).
