# Android is Compose-first
Source: https://developer.android.com/develop/ui/compose/why-adopt

Jetpack Compose is Android's declarative UI toolkit, built for modern user interfaces, with dynamic data, rich graphics, and beautiful animations. It's replacing the View toolkit, which has served Android development well for years, but was not designed for the latest demands and best practices.

## Why Compose-first

We first announced Jetpack Compose in 2019 and have been adding to its features, performance, and tooling since then. Jetpack Compose now has everything you need to build premium, native Android applications.
- **Rich feature set**
With a powerful library of layouts, input, graphics, animation APIs, and the latest Material Design components, Compose empowers you to build anything that you can imagine.
- **Highly performant**
Out of the box, Compose offers native performance, delivering a delightful experience to your users.
- **Adaptive**
Compose offers the easiest way to build adaptive apps that work across the range of Android form factors.
- **Productive**
With powerful tools like Previews and Live Edit and the full expressiveness of Kotlin, teams tell us that they move much faster when building with Jetpack Compose, reducing the time to market.

Learn more about how Compose can accelerate development.

## What does Compose-first mean?

When building new Android development tools and content, such as Jetpack libraries, samples, documentation, and training content, we'll design them with Jetpack Compose users in mind. We understand that adopting a new UI framework takes some time, so we'll continue to support traditional Views for some time. We'll also continue supporting interop APIs to allow you to adopt Compose at your own pace.

### Android Views

We now consider the View toolkit (for example, classes in `android.widget` such as `TextView` and `ListView`) to be in **maintenance mode** — this means that it will only receive highly critical fixes. The `android.view` package is still supported as the plumbing necessary for Compose and other UI toolkits.

### View-based Jetpack libraries

The following View-based libraries are also in **maintenance mode** and will not receive significant updates beyond critical fixes:
- `CardView`
- `ConstraintLayout`
- `CoordinatorLayout`
- `Cursoradapter`
- `CustomView`
- `Databinding`
- `DragAndDrop`
- `DrawerLayout`
- `DynamicAnimation`
- `Emoji`
- `Fragment`
- `GridLayout`
- `Interpolator`
- `Loader`
- `Navigation`
- `PercentLayout`
- `Preference`
- `RecyclerView`
- `SlidingPaneLayout`
- `SwipeRefreshLayout`
- `Transition`
- `VectorDrawable`
- `ViewPager`
- `ViewPager2`
- Material Design Components (Views)

### Tools

Any new Android Studio UI tools will be built for Jetpack Compose only. Existing tools (such as the Navigation Editor and Layout Editor) are now in **maintenance mode** and will not receive new features.

### Guidance

Documentation, codelabs, and samples will focus on building UI with Jetpack Compose. You can still find Views-specific documentation linked from pages that contain generic and Compose information, where relevant.

We're currently updating all relevant documentation across developer.android.com to be Compose-first.

## Happy Composing

Follow the quick start guide to add Jetpack Compose to your applications, migrate your apps to Compose using our Android migration skill on GitHub, and, finally, take a look at our sample apps on GitHub to see Compose in action.
