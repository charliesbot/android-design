# Migrate Jetpack Navigation to Navigation Compose
Source: https://developer.android.com/develop/ui/compose/migrate/migration-scenarios/navigation

The Navigation Compose API lets you navigate between composables in a Compose app, while taking advantage of the Jetpack Navigation's component, infrastructure, and features.

This page describes how to migrate from a Fragment-based Jetpack Navigation to Navigation Compose, as part of the larger, View-based UI migration to Jetpack Compose. **Note:** If you are new to Navigation Compose, see Navigating with Compose first. This doc describes the basics of Navigation Compose, such as how to pass navigation events or complex data and set up composables with arguments.

## Migration prerequisites

You can migrate to Navigation Compose once you're able to _replace all of your Fragments with corresponding screen composables_. Screen composables can contain a mix of Compose and View content, but _all navigation destinations must be composables_ to enable Navigation Compose migration. Until then, you should continue using Fragment-based Navigation component in your interop View and Compose codebase. See the navigation interop documentation for more information. **Note:** Using Navigation Compose in a Compose-only app is not a prerequisite. You can _continue using_ _Fragment-based Navigation component_, as long as you keep Fragments for hosting your composable content. The recommended end goal for a Compose-first architecture is to replace Fragments entirely with screen-level composables managed by Navigation Compose.**Note:** Currently, it's not possible to use any single form of the Navigation component to navigate between a mix of Fragment and composable destinations. All destinations must be uniformly either Fragments (using Jetpack Navigation) or composables (using Navigation Compose).

## Migration steps

Whether you are following our recommended migration strategy or taking another approach, you'll reach a point where all navigation destinations are screen composables, with Fragments acting only as composable containers. At this stage, you can migrate to Navigation Compose.

If your app is already following a UDF design pattern and our guide to architecture, migrating to Jetpack Compose and Navigation Compose shouldn't require major refactors of other layers of your app, apart from the UI layer.

To migrate to Navigation Compose, follow these steps:
- Add the Navigation Compose dependency to your app.
- Create an `App-level` composable and add it to your `Activity` as your Compose entry point, replacing the setup of the View layout:

```
class SampleActivity : ComponentActivity() {

override fun onCreate(savedInstanceState: Bundle?) {
super.onCreate(savedInstanceState)
// setContentView<ActivitySampleBinding>(this, R.layout.activity_sample)
setContent {
SampleApp(/* ... */)
}
}
}
```

- Create types for each navigation destination. Use a `data object` for destinations which don't require any data and `data class` or `class` for destinations which require data.

```
@Serializable data object First
@Serializable data class Second(val id: String)
@Serializable data object Third

```

- Set up the `NavController` in a place where all composables that need to reference it have access to it (this is usually inside your `App` composable). This approach follows the principles of state hoisting and lets you use the `NavController` as the source of truth for navigating between composable screens and maintaining the back stack:

```
@Composable
fun SampleApp() {
val navController = rememberNavController()
// ...
}
```

- Create your app's `NavHost` inside the `App` composable and pass the `navController`:

```
@Composable
fun SampleApp() {
val navController = rememberNavController()

SampleNavHost(navController = navController)
}

@Composable
fun SampleNavHost(
navController: NavHostController
) {
NavHost(navController = navController, startDestination = First) {
// ...
}
}
```

- Add the `composable` destinations to build your navigation graph. If each screen has been previously migrated to Compose, this step only consists of extracting these screen composables from your Fragments into the `composable` destinations:

```
class FirstFragment : Fragment() {

override fun onCreateView(
inflater: LayoutInflater,
container: ViewGroup?,
savedInstanceState: Bundle?
): View {
return ComposeView(requireContext()).apply {
setContent {
// FirstScreen(...) EXTRACT FROM HERE
}
}
}
}

@Composable
fun SampleNavHost(
navController: NavHostController
) {
NavHost(navController = navController, startDestination = First) {
composable<First> {
FirstScreen(/* ... */) // EXTRACT TO HERE
}
composable<Second> {
SecondScreen(/* ... */)
}
// ...
}
}
```

- If you followed the guidance on architecting your Compose UI, specifically how `ViewModel`s and navigation events should be passed to composables, the next step is to change how you provide the `ViewModel` to each screen composable. You can often use Hilt injection and its integration point with Compose and Navigation via `hiltViewModel`:

```
@Composable
fun FirstScreen(
// viewModel: FirstViewModel = viewModel(),
viewModel: FirstViewModel = hiltViewModel(),
onButtonClick: () -> Unit = {},
) {
// ...
}
```

- Replace all `findNavController()` navigation calls with the `navController` ones and pass these as navigation events to each composable screen, rather than passing the entire `navController`. This approach follows the best practices of exposing events from composable functions to callers and keeps the `navController` as the single source of truth.
Data can be passed to a destination by creating an instance of the route class defined for that destination. It can then be obtained either directly from the back stack entry at the destination or from a `ViewModel` using `SavedStateHandle.toRoute()`.

```
@Composable
fun SampleNavHost(
navController: NavHostController
) {
NavHost(navController = navController, startDestination = First) {
composable<First> {
FirstScreen(
onButtonClick = {
// findNavController().navigate(firstScreenToSecondScreenAction)
navController.navigate(Second(id = "ABC"))
}
)
}
composable<Second> { backStackEntry ->
val secondRoute = backStackEntry.toRoute<Second>()
SecondScreen(
id = secondRoute.id,
onIconClick = {
// findNavController().navigate(secondScreenToThirdScreenAction)
navController.navigate(Third)
}
)
}
// ...
}
}
```

- Remove all Fragments, relevant XML layouts, unnecessary navigation and other resources, and stale Fragment and Jetpack Navigation dependencies.

You can find the same steps with more Navigation Compose-related details in the Setup documentation.

## Common use cases

No matter which Navigation component you're using, the same principles of navigation apply.

Common use cases when migrating include the following:
- Navigate to a composable
- Navigate with arguments
- Deep links
- Nested navigation
- Integration with the bottom nav bar
- Integration with a custom nav component

For more detailed information about these use cases, see Navigating with Compose.

### Retrieve complex data when navigating

We strongly recommend not passing around complex data objects when navigating. Instead, pass the minimum necessary information, such as a unique identifier or other form of ID, as arguments when performing navigation actions. You should store complex objects as data in a single source of truth, such as the data layer. For more information, see Retrieving complex data when navigating.

If your Fragments are passing complex objects as arguments, consider refactoring your code first, in a way that allows storing and fetching these objects from the data layer. See the Now in Android repository for examples.

## Limitations

This section describes current limitations for Navigation Compose.

### Incremental migration to Navigation Compose

Currently, you cannot use Navigation Compose while still using Fragments as destinations in your code. To start using Navigation Compose, all of your destinations need to be composables. You can track this feature request on the Issue Tracker.

### Transition animations

Starting with Navigation 2.7.0-alpha01, support for setting custom transitions, previously from `AnimatedNavHost`, is now directly supported in `NavHost`. Read through the release notes for more information.

## Learn more

For more information about migrating to Navigation Compose, see the following resources:
- Navigation Compose codelab: Learn the basics of Navigation Compose with a hands-on codelab.
- Now in Android repository: A fully functional Android app built entirely with Kotlin and Jetpack Compose, which follows Android design and development best practices and includes Navigation Compose.
- Migrating Sunflower to Jetpack Compose: A blog post that documents the migration journey of the Sunflower sample app from Views to Compose, which also includes migration to Navigation Compose.
- Jetnews for every screen: A blog post that documents the refactor and migration of the Jetnews sample to support all screens with Jetpack Compose and Navigation Compose.

## Recommended for you
- Note: link text is displayed when JavaScript is off
- Navigating with Compose
- Compose and other libraries
- Other considerations
