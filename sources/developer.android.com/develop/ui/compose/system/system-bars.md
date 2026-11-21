# About system bar protection
Source: https://developer.android.com/develop/ui/compose/system/system-bars

Once your app targets SDK 35 or later, edge-to-edge is enforced. The system status bar and gesture navigation bars are transparent, but the three-button navigation bar is translucent. Call `enableEdgeToEdge` to make this backward compatible.

However, the system defaults might not work for all use cases. Consult the Android system bars design guidance and edge-to-edge design guidance for an overview of when to consider having transparent or translucent system bars.

## Create transparent system bars

Create a transparent gesture navigation bar by targeting Android 15 or later or by calling `enableEdgeToEdge()` with default arguments for earlier versions. For three-button navigation bar, set `Window.setNavigationBarContrastEnforced` to `false` otherwise there will be a translucent scrim applied.

## Create translucent system bars

To create a translucent status bar, create a custom composable that overlaps the main content and draws a gradient in the area covered by insets.

```
class SystemBarProtectionSnippets : ComponentActivity() {

override fun onCreate(savedInstanceState: Bundle?) {
super.onCreate(savedInstanceState)

// enableEdgeToEdge sets window.isNavigationBarContrastEnforced = true
// which is used to add a translucent scrim to three-button navigation
enableEdgeToEdge()

setContent {
MyTheme {
// Main content
MyContent()

// After drawing main content, draw status bar protection
StatusBarProtection()
}
}
}
}

@Composable
private fun StatusBarProtection(
color: Color = MaterialTheme.colorScheme.surfaceContainer,
) {
Spacer(
modifier = Modifier
.fillMaxWidth()
.height(
with(LocalDensity.current) {
(WindowInsets.statusBars.getTop(this) * 1.2f).toDp()
}
)
.background(
brush = Brush.verticalGradient(
colors = listOf(
color.copy(alpha = 1f),
color.copy(alpha = 0.8f),
Color.Transparent
)
)
)
)
}
```

**Figure 1.** A translucent status bar.

For adaptive apps, insert a custom composable that matches the colors of each pane, as seen in the Edge-to-edge design. To create a translucent navigation bar, set `Window.setNavigationBarContrastEnforced` to true.

Previous  arrow_back   Use insets in Views and Compose

Next  About cutouts   arrow_forward
