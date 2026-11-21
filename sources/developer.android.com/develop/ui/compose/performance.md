# Jetpack Compose Performance
Source: https://developer.android.com/develop/ui/compose/performance

Jetpack Compose delivers excellent performance out of the box. Configure your app using best practices to avoid common pitfalls and optimize your Compose application's performance.

## Benchmarking Jetpack Compose

To validate Jetpack Compose performance against Views, we use hero benchmarks—benchmarks that focus on important, real-world user journeys, like cold starting an app or scrolling a list or grid with images. We conduct these benchmarks using the open-source Pokedex app, comparing its Views and Compose implementations.

These benchmarks show that Compose matches Views performance for jank while scrolling since Compose 1.9.0 (achieving 0% jank on Pixel 6).
Compose excess jank over Views in percentage points, by Compose version (lower is better)
| Compose version | Device | Excess jank over Views (pp)
| 1.3.3 | Pixel 3a | +0.35
| 1.4.3 | Pixel 3a | +0.41
| 1.5.4 | Pixel 3a | +0.42
| 1.6.8 | Pixel 3a | +0.21
| 1.7.8 | Pixel 3a | +0.36
| 1.8.0 | Pixel 3a | +0.38
| 1.9.0 | Pixel 3a | 0
| 1.10.0 | Pixel 3a | 0
| 1.11 | Pixel 6 | 0
| 1.12 | Pixel 6 | 0
| 1.13 | Pixel 6 | 0

Excess jank over Views
Compose   Views

Excess jank over Views (pp)  Percentage points, lower is better.
0 +0.1 +0.2 +0.3 +0.4 +0.5
0  +0.35  +0.41  +0.42  +0.21  +0.36  +0.38  0  0  0  0  0
1.3.3 1.4.3 1.5.4 1.6.8 1.7.8 1.8.0 1.9.0 1.10.0 1.11 1.12 1.13

Pixel 3a
Pixel 6
Compose version
**Figure 1.** Since Compose 1.9.0, Compose matches Views scroll jank.

For additional metrics and to learn about our methodologies, see Hero benchmarks.

## Key concepts

These are some of the key concepts for performance in Compose:
- **Phases:** Understanding the composition, layout, and drawing phases is crucial for optimizing how Compose updates your UI.
- **Baseline Profiles:** These profiles precompile essential code, leading to faster app launches and smoother interactions.
- **Stability:** Understand how Compose compares parameters during recomposition so you can skip unnecessary work without introducing expensive equality checks.

## Properly configure your app

If your app is performing poorly, there might be a configuration problem. A good first step is to check the following configuration options:
- **Build in Release Mode with R8:** Try running your app in release mode. Debug mode is useful for spotting many problems, but it imposes a performance cost and can make it hard to spot other issues. You should also enable optimizing and shrinking with the R8 compiler to ensure a performant and efficient release build.
- **Use Baseline Profiles:** Baseline Profiles improve performance by precompiling code for critical user journeys. Compose includes a default profile, but ideally, you should create an app-specific one as well. Learn more about Baseline Profiles in the general Android performance docs

## Tools

Familiarize yourself with the suite of tools available to help you measure and analyze your Compose app's performance.

## Best Practices

When developing your app with Compose, keep these best practices in mind:
- **Avoid expensive calculations:** Use `remember` to cache the results of expensive calculations.
- **Help lazy layouts:** Provide stable `key` and `contentType` parameters to lazy layouts to minimize unnecessary recompositions and enable slot reuse.
- **Limit unnecessary recompositions:** Use `computedStateOf` (in Compose 1.13 and higher) or `derivedStateOf` to limit recompositions when reading rapidly changing state.
- **Defer state reads:** Defer state reads as long as possible by wrapping them in lambda functions.
- **Use lambda modifiers for changing state:** Use lambda-based modifiers like `Modifier.offset { ... }` for frequently changing state variables.
- **Avoid backwards writes:** Never write to state that has already been read in a composable.

For more details, see the best practices guide.

## Views

If you are working with views instead of Compose, see the dedicated Improve layout performance guide.

## Additional resources
- **App performance guide:** Discover best practices, libraries, and tools to improve performance on Android.
- **Inspect Performance:** Inspect app performance.
- **Benchmarking:** Benchmark app performance.
- **App startup:** Optimize app startup.
- **Baseline profiles:** Understand baseline profiles.
