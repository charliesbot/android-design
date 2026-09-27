# Edge-to-edge design
Source: https://developer.android.com/design/ui/mobile/guides/layout-and-content/edge-to-edge

An edge-to-edge app takes advantage of the entire screen by drawing UI under the system bars.
**Figure 1.** Left. An app that isn't edge-to-edge. Right. An app that is edge-to-edge.

## Takeaways
- Draw background and scrolling content underneath system bars for an edge-to-edge experience.
- Avoid adding tap gestures or drag targets under system insets; these conflict with edge-to-edge and gesture navigation.

**Figure 2.** An app with gesture insets highlighted green.

### Draw your content behind the system bars

The edge-to-edge feature lets you draw the UI under the system bars for an immersive experience.

An app can address overlaps in content by reacting to _insets_. Insets describe how much the content of your app needs to be padded to avoid overlapping with system bars or with physical device features such as display cutouts. Read about how to support edge-to-edge and handle insets in Compose and Views.

Be aware of the following types of insets when designing edge-to-edge use cases:
- _System bar insets_ apply to UI that is both tappable and shouldn't be visually obscured by the system bars.
- _System gesture insets_ apply to gesture-navigational areas used by the OS that take priority over your app.
- _Display cutout insets_ apply to device areas that extend into the display surface, such as the camera cutout.

### Status bar considerations

See the Android System Bars for fundamental system bar design guidance. The following section discusses additional status bar considerations.

#### Scrolling content

Top app bars should collapse while scrolling. Learn how to collapse the Material 3 TopAppBar. In Material 3, small top app bars can collapse to status bar height or scroll offscreen. Medium and large top app bars can collapse to a smaller app bar. See the Material guidance.

check_circle

###### Do
Collapse the small top app bar to status bar height if the app bar is sticky.

check_circle

###### Do
Add a matching background color gradient if the small top app bar is not sticky.

Status bars should be translucent when the UI scrolls underneath, so that the status bar icons don't look cluttered. To accomplish this, first make a scrollable UI edge-to-edge by implementing the steps in the LazyColumn or RecyclerView documentation. Then, ensure the system bar is translucent by doing one of the following:
- Rely on the Material 3 TopAppBar automatic protection when scrolling, if applicable.
- Create a custom gradient composable or use GradientProtection for Views. For more information on doing this in compose, see System bar protection.

**Figure 3.** An app with gesture insets highlighted green.

For adaptive layouts, ensure there are separate protections for panes with different background colors.

cancel

###### Don´t
Have gradient protection that mismatches each pane's background

check_circle

###### Do
Have gradient protection that matches each pane's background.

Likewise, navigation drawers should also have a separate protection from the rest of the app.
**Figure 4.** A translucent status bar for the navigation drawer. This image shows status bar protection for the navigation drawer but not the app.

Don't stack status bar protections, for example by using both the Material 3 TopAppBar built-in protection and a custom protection.

### Navigation bar considerations

See the Android System Bars for fundamental navigation bar design guidance. The following section includes additional navigation bar considerations.

#### Scrolling content

Bottom app bars should collapse while scrolling.

check_circle

###### Do
Add system bar scrim for three-button navigation when the bottom app bar animates away.

check_circle

###### Do
Keep gesture navigation transparent and don't add an additional scrim.

### Display cutouts

Display cutouts can affect the appearance of your UI. Apps must handle display cutout insets so that important parts of the UI don't draw underneath the display cutout.

check_circle

###### Do
Inset critical UI using display cutout insets.

cancel

###### Don´t
Place critical UI at the very edge of the screen.

However, solid app bar backgrounds should draw into the display cutout as shown in the following image.
**Figure 5.** Solid app bar backgrounds draw into the display cutout while important UI is inset.

Ensure horizontal carousels draw into the display cutout.
**Figure 6.** An edge-to-edge horizontal display, where the carousel scrolls through the display cutout.

Read about how to support display cutouts in Compose and Views.

### Other guidance

In general, backgrounds and divider lines should also draw edge-to-edge while content like text and buttons should be inset to avoid the system UI and hardware elements.
