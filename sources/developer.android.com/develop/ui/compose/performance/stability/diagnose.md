# Diagnose stability issues
Source: https://developer.android.com/develop/ui/compose/performance/stability/diagnose

If you are experiencing performance issues that result from unnecessary or excessive recomposition, you should debug the stability of your app. This guide outlines several methods for doing so. **Important:** You should aim to resist unnecessary or premature optimization. Before optimizing for stability, verify you are following our best practices for Compose performance.**Note:** If you would like more information about how to measure performance properly, see the Macrobenchmark guide.

## Layout Inspector

The Layout Inspector in Android Studio lets you see which composables are recomposing in your app. It displays counts of how many times Compose has recomposed or skipped a component.

[IMAGE] Recomposition and skips counts in the Layout Inspector
**Note:** Don't rely on recomposition counts alone to judge performance. Skipping a composable isn't free—Compose stores each parameter in the slot table and compares parameters on every recomposition pass, and an expensive `.equals()` check on a skipped composable can take longer than recomposing a lightweight composable. Always verify real frame timing and CPU impact using Macrobenchmark or composition tracing.

## Compose compiler reports

The Compose compiler can output the results of its stability inference for inspection. Using this output, you can determine which of your composables are skippable, and which are not. The follow subsections summarize how to use these reports, but for more detailed information see the technical documentation. **Warning:** You should only use this technique if you are actually experiencing performance issues related to stability. Trying to make your entire UI skippable is a premature optimization that could lead to maintenance difficulties in the future.

### Setup

Compose compiler reports are not enabled by default. You can activate them with a compiler flag. The exact setup varies depending on your project, but for projects using the Compose compiler gradle plugin you can add the following in each module's `build.gradle` file.

```
android { ... }

composeCompiler {
reportsDestination = layout.buildDirectory.dir("compose_compiler")
metricsDestination = layout.buildDirectory.dir("compose_compiler")
}

```

Compose compiler reports will now be generated when building your project. **Important:** Make sure to always run this on a release build to ensure accurate results.

#### Example output

The `reportsDestination` outputs three files. The following are example outputs from JetSnack.
- **`<modulename>-classes.txt`:** A report on the stability of classes in this module. Sample.
- **`<modulename>-composables.txt`:** A report on how restartable and skippable the composables are in the module. Sample.
- **`<modulename>-composables.csv`:**A `CSV` version of the composables report that you can import into a spreadsheet or processing using a script. Sample
**Note:** The `metricsDestination` configuration produces overall statistics on the number of composables in your project and other similar info.**Note:** These reports make use of the compiler tags described in the Compose stability guide.

#### Composables report

The `composables.txt` file details each composable functions for the given module, including the stability of their parameters, and whether they are restartable or skippable. The following is a hypothetical example from JetSnack:

```
restartable skippable scheme("[androidx.compose.ui.UiComposable]") fun SnackCollection(
stable snackCollection: SnackCollection
stable onSnackClick: Function1<Long, Unit>
stable modifier: Modifier? = @static Companion
stable index: Int = @static 0
stable highlight: Boolean = @static true
)

```

In this example, every parameter of `SnackCollection` is marked `stable`, so Compose compares each parameter using structural equality (`Object.equals()`). While `stable` parameters are ideal for primitive values and small, flat data classes with cheap `.equals()` checks, forcing collection-holding models (such as a `SnackCollection` that wraps a `List<Snack>`) to be `stable` causes Compose to call `.equals()`on each item on every recomposition. With Strong skipping mode enabled, keeping models that hold collections `unstable` is preferable so Compose compares them using instance equality (`===`) instead.

Alternatively, consider another example generated with Strong skipping mode disabled:

```
restartable scheme("[androidx.compose.ui.UiComposable]") fun HighlightedSnacks(
stable index: Int
unstable snacks: List<Snack>
stable onSnackClick: Function1<Long, Unit>
stable modifier: Modifier? = @static Companion
)

```

When strong skipping mode is disabled (prior to Kotlin 2.0.20), the `HighlightedSnacks` composable is not skippable—Compose never skips it during recomposition, even if none of its parameters have changed, because of the `unstable` parameter `snacks`.

With Strong skipping mode enabled (the default in Kotlin 2.0.20 and higher), `HighlightedSnacks` is marked `skippable`, and Compose compares the `unstable` parameter `snacks` using instance equality (`===`). As a result, Compose skips `HighlightedSnacks` if the same `List` reference is passed, and only recomposes it if a newly allocated `List` instance is passed.

#### Classes report

The file `classes.txt` contains a similar report on the classes in the given module. The following snippet is the output for the class `Snack`:

```
unstable class Snack {
stable val id: Long
stable val name: String
stable val imageUrl: String
stable val price: Long
stable val tagline: String
unstable val tags: Set<String>
<runtime stability> = Unstable
}

```

For reference, the following is the definition of `Snack`:

```
data class Snack(
val id: Long,
val name: String,
val imageUrl: String,
val price: Long,
val tagline: String = "",
val tags: Set<String> = emptySet()
)

```

The Compose compiler has marked `Snack` as unstable. This is because the type of the `tags` parameter is `Set<String>`. This is an immutable type, given that it is not a `MutableSet`. However, standard collection classes such as `Set`, `List`, and `Map` are ultimately interfaces. As such, the underlying implementation may still be mutable.

For example, you could write `val set: Set<String> = mutableSetOf("foo")`. The variable is constant and its declared type is not mutable, but its implementation is _still_ mutable. The Compose compiler cannot be sure of the immutability of this class as it only sees the declared type. It therefore marks `tags` as unstable. **Note:** Under Strong skipping mode, an `unstable` class or collection property isn't automatically a problem that needs to be fixed. Unstable types skip recomposition using instance equality (`===`), which can be faster than running structural `.equals()` checks on large collections. Only modify stability if a data source frequently allocates new instances of the class with identical values and benchmarks confirm a performance bottleneck. For more information, see the Fix stability issues guide.
