# Using Compose in Views
Source: https://developer.android.com/develop/ui/compose/migrate/interoperability-apis/compose-in-views

**Note:** Interoperability is supported for hybrid apps that use Compose and Views. For apps that are only in Compose, use the recommended Compose-only architecture with a single Activity and latest navigation libraries, like Navigation 3.

You can add Compose-based UI into an existing app that uses a View-based design.

To create a new, entirely Compose-based screen, have your activity call the `setContent()` method, and pass whatever composable functions you like.

```
class ExampleActivity : ComponentActivity() {
override fun onCreate(savedInstanceState: Bundle?) {
super.onCreate(savedInstanceState)

setContent { // In here, we can call composables!
MaterialTheme {
Greeting(name = "compose")
}
}
}
}

@Composable
fun Greeting(name: String) {
Text(text = "Hello $name!")
}
```

This code looks just like what you'd find in a Compose-only app.

**Caution:** To use the `ComponentActivity.setContent` method, add the `androidx.activity:activity-compose:$latestVersion` dependency to your `build.gradle` file.

See the Activity releases page to find out the latest version.

## `ViewCompositionStrategy` for `ComposeView`

`ViewCompositionStrategy` defines when the Composition should be disposed. The default, `ViewCompositionStrategy.Default`, disposes the Composition when the underlying `ComposeView` detaches from the window, unless it is part of a pooling container such as a `RecyclerView`. In a single-Activity Compose-only app, this default behavior is what you would want, however, if you are incrementally adding Compose in your codebase, this behavior may cause state loss in some scenarios.

To change the `ViewCompositionStrategy`, call the `setViewCompositionStrategy()` method and provide a different strategy.

The table below summarizes the different scenarios you can use `ViewCompositionStrategy` in:
| `ViewCompositionStrategy`  | Description and Interop Scenario
| `DisposeOnDetachedFromWindow`  | The Composition will be disposed when the underlying `ComposeView` is detached from the window. Has since been superseded by `DisposeOnDetachedFromWindowOrReleasedFromPool`.

Interop scenario:

* `ComposeView` whether it’s the sole element in the View hierarchy, or in the context of a mixed View/Compose screen (not in Fragment).
| `DisposeOnDetachedFromWindowOrReleasedFromPool` (**Default**)  | Similar to `DisposeOnDetachedFromWindow`, when the Composition is not in a pooling container, such as a `RecyclerView`. If it is in a pooling container, it will dispose when either the pooling container itself detaches from the window, or when the item is being discarded (i.e. when the pool is full).

Interop scenario:

* `ComposeView` whether it's the sole element in the View hierarchy, or in the context of a mixed View/Compose screen (not in Fragment).
* `ComposeView` as an item in a pooling container such as `RecyclerView`.
| `DisposeOnLifecycleDestroyed`  | The Composition will be disposed when the provided `Lifecycle` is destroyed.

Interop scenario

* `ComposeView` in a Fragment's View.
| `DisposeOnViewTreeLifecycleDestroyed`  | The Composition will be disposed when the `Lifecycle` owned by the `LifecycleOwner` returned by `ViewTreeLifecycleOwner.get` of the next window the View is attached to is destroyed.

Interop scenario:

* `ComposeView` in a Fragment's View.
* `ComposeView` in a View wherein the Lifecycle is not known yet.

## `ComposeView` in Fragments (Transitionary Step) **Note:** For hybrid apps with Views and Compose, that use Fragments, use `ComposeView` to wrap composables content and add to a Fragment during migration. For Compose-only apps, do not use Fragments and instead use the recommended Compose-only architecture with a single Activity and latest navigation libraries, like Navigation 3.

If you want to incorporate Compose UI content in a fragment or an existing View layout, use `ComposeView` and call its `setContent()` method. `ComposeView` is an Android `View`.

You can put the `ComposeView` in your XML layout just like any other `View`:

```
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
android:orientation="vertical"
android:layout_width="match_parent"
android:layout_height="match_parent">

<TextView
android:id="@+id/text"
android:layout_width="wrap_content"
android:layout_height="wrap_content" />

<androidx.compose.ui.platform.ComposeView
android:id="@+id/compose_view"
android:layout_width="match_parent"
android:layout_height="match_parent" />
</LinearLayout>
```

In the Kotlin source code, inflate the layout from the layout resource defined in XML. Then get the `ComposeView` using the XML ID, set a Composition strategy that works best for the host `View`, and call `setContent()` to use Compose.

```
class ExampleFragmentXml : Fragment() {

override fun onCreateView(
inflater: LayoutInflater,
container: ViewGroup?,
savedInstanceState: Bundle?
): View {
val view = inflater.inflate(R.layout.fragment_example, container, false)
val composeView = view.findViewById<ComposeView>(R.id.compose_view)
composeView.apply {
// Dispose of the Composition when the view's LifecycleOwner
// is destroyed
setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
setContent {
// In Compose world
MaterialTheme {
Text("Hello Compose!")
}
}
}
return view
}
}
```

Alternatively, you can also use view binding to obtain references to the `ComposeView` by referencing the generated binding class for your XML layout file:

```
class ExampleFragment : Fragment() {

private var _binding: FragmentExampleBinding? = null

// This property is only valid between onCreateView and onDestroyView.
private val binding get() = _binding!!

override fun onCreateView(
inflater: LayoutInflater,
container: ViewGroup?,
savedInstanceState: Bundle?
): View {
_binding = FragmentExampleBinding.inflate(inflater, container, false)
val view = binding.root
binding.composeView.apply {
// Dispose of the Composition when the view's LifecycleOwner
// is destroyed
setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
setContent {
// In Compose world
MaterialTheme {
Text("Hello Compose!")
}
}
}
return view
}

override fun onDestroyView() {
super.onDestroyView()
_binding = null
}
}
```

[IMAGE] Two slightly different text elements, one above the other

**Figure 1.** This shows the output of the code that adds Compose elements in a View UI hierarchy. The "Hello Android!" text is displayed by a `TextView` widget. The "Hello Compose!" text is displayed by a Compose text element.

You can also include a `ComposeView` directly in a fragment if your full screen is built with Compose, which lets you avoid using an XML layout file entirely.

```
class ExampleFragmentNoXml : Fragment() {

override fun onCreateView(
inflater: LayoutInflater,
container: ViewGroup?,
savedInstanceState: Bundle?
): View {
return ComposeView(requireContext()).apply {
// Dispose of the Composition when the view's LifecycleOwner
// is destroyed
setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
setContent {
MaterialTheme {
// In Compose world
Text("Hello Compose!")
}
}
}
}
}
```

## Multiple `ComposeView` instances in the same layout

If there are multiple `ComposeView` elements in the same layout, each one must have a unique ID for `savedInstanceState` to work.

```
class ExampleFragmentMultipleComposeView : Fragment() {

override fun onCreateView(
inflater: LayoutInflater,
container: ViewGroup?,
savedInstanceState: Bundle?
): View = LinearLayout(requireContext()).apply {
addView(
ComposeView(requireContext()).apply {
setViewCompositionStrategy(
ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
)
id = R.id.compose_view_x
// ...
}
)
addView(TextView(requireContext()))
addView(
ComposeView(requireContext()).apply {
setViewCompositionStrategy(
ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
)
id = R.id.compose_view_y
// ...
}
)
}
}
```

The `ComposeView` IDs are defined in the `res/values/ids.xml` file:

```
<resources>
<item name="compose_view_x" type="id" />
<item name="compose_view_y" type="id" />
</resources>
```

## Preview composables in Layout Editor

You can also preview composables within the Layout Editor for your XML layout containing a `ComposeView`. Doing so lets you see how your composables look within a mixed Views and Compose layout.

Say you want to display the following composable in the Layout Editor. Note that composables annotated with `@Preview` are good candidates to preview in the Layout Editor.

```
@Preview
@Composable
fun GreetingPreview() {
Greeting(name = "Android")
}
```

To display this composable, use the `tools:composableName` tools attribute and set its value to the fully qualified name of the composable to preview in the layout.

```
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
xmlns:tools="http://schemas.android.com/tools"
android:orientation="vertical"
android:layout_width="match_parent"
android:layout_height="match_parent">

<androidx.compose.ui.platform.ComposeView
android:id="@+id/my_compose_view"
tools:composableName="com.example.compose.snippets.interop.InteroperabilityAPIsSnippetsKt.GreetingPreview"
android:layout_height="match_parent"
android:layout_width="match_parent"/>

</LinearLayout>
```

[IMAGE] Composable displayed within layout editor
