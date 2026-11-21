# Navigation with shared elements
Source: https://developer.android.com/develop/ui/compose/animation/shared-elements/navigation

**Figure 1.** Navigation with shared elements.

Shared elements make transitions between screens smoother and more engaging by creating a visual connection that guides the user. This guide demonstrates how to use the shared element APIs with both the Navigation 3 and Navigation 2 Jetpack libraries.

The following snippet includes `DetailsScreen` and `HomeScreen` composables that serve as the destinations users can navigate between. Within each screen, the `sharedElement` modifier is used on both the image and the text so that each of those elements independently animates between screens.

```
@Composable
fun DetailsScreen(
id: Int,
snack: Snack,
sharedTransitionScope: SharedTransitionScope,
animatedVisibilityScope: AnimatedVisibilityScope,
onBackPressed: () -> Unit
) {
with(sharedTransitionScope) {
Column(
modifier = Modifier
.fillMaxSize()
.clickable { onBackPressed() },
) {
Image(
painterResource(id = snack.image),
contentDescription = snack.description,
contentScale = ContentScale.Crop,
modifier = Modifier
.sharedElement(
sharedTransitionScope.rememberSharedContentState(key = "image-$id"),
animatedVisibilityScope = animatedVisibilityScope
)
.aspectRatio(1f)
.fillMaxWidth()
)
Text(
text = snack.name,
fontSize = 18.sp,
modifier = Modifier
.sharedElement(
sharedTransitionScope.rememberSharedContentState(key = "text-$id"),
animatedVisibilityScope = animatedVisibilityScope
)
.fillMaxWidth(),
)
}
}
}

@Composable
fun HomeScreen(
sharedTransitionScope: SharedTransitionScope,
animatedVisibilityScope: AnimatedVisibilityScope,
onItemClick: (Int) -> Unit,
) {
LazyColumn(
modifier = Modifier
.fillMaxSize()
.padding(8.dp),
verticalArrangement = Arrangement.spacedBy(8.dp)
) {
itemsIndexed(listSnacks) { index, item ->
Row(
modifier = Modifier
.fillMaxWidth()
.clickable { onItemClick(index) },
) {
Spacer(modifier = Modifier.width(8.dp))
with(sharedTransitionScope) {
Image(
painterResource(id = item.image),
contentDescription = item.description,
contentScale = ContentScale.Crop,
modifier = Modifier
.sharedElement(
sharedTransitionScope.rememberSharedContentState(key = "image-$index"),
animatedVisibilityScope = animatedVisibilityScope
)
.size(100.dp)
)
Spacer(modifier = Modifier.width(8.dp))
Text(
item.name,
fontSize = 18.sp,
modifier = Modifier
.align(Alignment.CenterVertically)
.sharedElement(
sharedTransitionScope.rememberSharedContentState(key = "text-$index"),
animatedVisibilityScope = animatedVisibilityScope,
)
)
}
}
}
}
}
```

## Navigation 3

To use the shared element APIs with Navigation 3, you must first wrap your app's `NavDisplay` in a `SharedTransitionLayout`. You can then pass the provided `SharedTransitionScope` to the screen composables.

For the `AnimatedVisibilityScope`, use the `LocalNavAnimatedContentScope` composition local that provides the `AnimatedContentScope` from the `AnimatedContent` that `NavDisplay` uses internally to animate between scenes.

```
@Composable
fun SharedElement_Nav3() {
SharedTransitionLayout {
val backStack = rememberNavBackStack(HomeRoute)

// Note: NavDisplay accepts a `sharedTransitionScope` parameter, which is used to animate
// NavEntry instances between scenes. This parameter *isn't* required for shared element
// or shared bounds transitioning elements between different NavEntry, as demonstrated in
// this sample.
// See https://developer.android.com/guide/navigation/navigation-3/animate-destinations#transition-nav-entries
NavDisplay(
modifier = Modifier.safeDrawingPadding(),
backStack = backStack,
entryProvider = entryProvider {
entry<HomeRoute> {
HomeScreen(
sharedTransitionScope = this@SharedTransitionLayout,
animatedVisibilityScope = LocalNavAnimatedContentScope.current,
onItemClick = { backStack.add(DetailsRoute(it)) })
}
entry<DetailsRoute> { detailsRoute ->
val id = detailsRoute.item
val snack = listSnacks[id]

DetailsScreen(
id = id,
snack = snack,
sharedTransitionScope = this@SharedTransitionLayout,
animatedVisibilityScope = LocalNavAnimatedContentScope.current,
onBackPressed = {
backStack.removeLastOrNull()
},
)
}
})
}
}
```

## Navigation 2

To use the shared element APIs with Navigation 2, you must first wrap your app's `NavHost` in a `SharedTransitionLayout`. You can then pass the provided `SharedTransitionScope` to the screen composables.

The `content` parameter of the `composable` builder uses `AnimatedContentScope` as a receiver, so you can use `this@composable` to reference that scope.

```
@Composable
fun SharedElement_Nav2() {
SharedTransitionLayout {
val navController = rememberNavController()
NavHost(
navController = navController,
startDestination = "home",
modifier = Modifier.safeDrawingPadding()
) {
composable("home") {
HomeScreen(
sharedTransitionScope = this@SharedTransitionLayout,
animatedVisibilityScope = this@composable,
onItemClick = { navController.navigate("details/$it") })
}
composable(
"details/{item}", arguments = listOf(navArgument("item") { type = NavType.IntType })
) { backStackEntry ->
val id = backStackEntry.arguments?.getInt("item") ?: 0
val snack = listSnacks[id]
DetailsScreen(
id = id,
snack = snack,
sharedTransitionScope = this@SharedTransitionLayout,
animatedVisibilityScope = this@composable,
onBackPressed = {
navController.popBackStack()
}
)
}
}
}
}
```

## Predictive back with shared elements

To use predictive back with shared elements, follow these steps:
- All versions of Navigation 3 support predictive back. For Navigation 2, use the `2.8.0` release of `navigation-compose` or higher:

```
[versions]
androidx-navigation = "2.8.0" # Or higher

[libraries]
androidx-navigation-compose = { module = "androidx.navigation:navigation-compose", version.ref = "androidx-navigation" }

```

```
dependencies {
implementation(libs.androidx.navigation.compose)
}

```

- Predictive back animations are enabled by default on devices running Android 15 (API level 35) or higher. For devices running Android 14 (API level 34), you need to enable the Predictive back setting in developer options.
- If your app targets Android 14 or lower, you must add `android:enableOnBackInvokedCallback="true"` to the `<application>` or specific `<activity>` elements in your `AndroidManifest.xml` file. You don't need this flag if your app targets Android 15 or higher.

```
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
<application
...
android:enableOnBackInvokedCallback="true">
</application>
</manifest>

```

**Figure 2.** Shared elements with predictive back.

Previous  arrow_back   Common use cases

Next  Additional samples   arrow_forward
