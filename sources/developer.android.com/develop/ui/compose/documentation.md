# Get started with Jetpack Compose
Source: https://developer.android.com/develop/ui/compose/documentation

Jetpack Compose is the modern toolkit for building Android UI, simplifying the development of apps that adapt to any display size.
- Overview: See the resources available to Compose developers.
- Tutorial: Get started with Compose by building a basic UI.
- Quick Guides: Try out our fast and focused guides, designed to get you to your goal as quickly as possible.

## Foundation
- Thinking in Compose: How the declarative approach of Compose is different from the view-based approach you may have used in the past. Build a mental model of working with Compose.
- Managing state: Setting and using state in your Compose app.
- Lifecycle of composables: Lifecycle of a composable, and how Compose determines whether it needs to be redrawn.
- Modifiers: Use modifiers to augment or decorate your composables.
- Side-effects in Compose: Ways to manage side-effects.
- Jetpack Compose Phases: The steps Compose goes through to render your app's UI, and how to use that information to write efficient code.
- Architectural layering: The architectural layers that make up Jetpack Compose and the core principles that informed the design of Compose.
- Performance: Avoid the common programming pitfalls that can degrade app performance.
- Semantics in Compose: The semantics tree, which organizes your UI in a way that can be used by accessibility services and testing frameworks.
- Locally scoped data with CompositionLocal: Use `CompositionLocal` to pass data through the composition.

## Adaptive UI
- Build adaptive apps: Learn the core principles of creating layouts optimized for any display size, including phones, tablets, foldables, and more.
- Apply proven layouts: Use canonical layouts like list-detail and supporting pane for optimized apps on large screens.
- Adaptive navigation: Implement navigation patterns that automatically adjust to the available display space.

## Development environment
- Android Studio with Compose: How to set up your development environment to use Compose.
- Tooling for Compose: Android Studio's new features to support Compose.
- Kotlin for Compose: Kotlin-specific idioms work with Compose.
- Compare Compose and view metrics: How migrating to Compose can affect your app's APK size and runtime performance.
- Bill of Materials: Manage all your Compose dependencies by specifying only the BOM's version.

## Design
- Layouts: Compose layout components and how to design your own.
  - Layout basics: The building blocks for a straightforward app UI.
  - Material Components and layouts: Material components and layouts in Compose.
  - Custom layouts: Take control of your app's layout and design a custom layout of your own.
  - Alignment lines: Create custom alignment guides to precisely align and position your UI elements.
  - Intrinsic measurements: How to query for information about child elements before measuring them because Compose measures UI elements only once per pass.
  - ConstraintLayout: Use `ConstraintLayout` in your Compose UI.

- Design Systems: Implement a design system and give your app a consistent look and feel.
  - Material Design 3: Implement Material You with the Compose implementation of Material Design 3.
  - Migrating from Material 2 to Material 3: Migrate your app from Material Design 2 to Material Design 3 in Compose.
  - Material Design 2: Customize the Compose implementation of Material Design 2 to fit your product's brand.
  - Custom design systems: Implement a custom design system in Compose and adapt existing Material Design composables for the new design system.
  - Anatomy of a theme: Lower-level constructs and APIs used by `MaterialTheme` and custom design systems.

- Lists and grids: Compose options for managing and displaying lists and grids of data.
- Text: Main options in Compose for displaying and editing text.
- Graphics: Compose features for building and working with custom graphics.
- Animation: Compose options for animating your UI elements.
- Gestures: Build a Compose UI that detects and interacts with user gestures.
- Handling user interactions: How Compose abstracts low-level input into higher-level interactions so you can customize how your components respond to user actions.

## Adopting Compose
- Migrate view-based apps: Migrate your view-based app to Compose.
  - Migration strategy: How to safely and incrementally introduce Compose into your codebase.
  - Interoperability APIs: Compose APIs to help you combine Compose with a view-based UI.
  - Other considerations: Theming, architecture, testing, and other considerations while migrating your view-based app to Compose.

- Compose and other libraries: How to use view-based libraries in your Compose content.
- Compose architecture: Implement the unidirectional flow pattern in Compose, implement events and state holders, and work with `ViewModel` in Compose.
- Navigation: Use `NavController` to integrate the Navigation component with your Compose UI.
- Resources: Work with your app's resources in your Compose code.
- Accessibility: Accommodate users with accessibility requirements.
- Testing: Test your Compose code.
  - Testing cheat sheet: A quick reference of useful Compose testing APIs.

## Additional resources
- Get setup
- Curated learning pathway
- Compose API guidelines
- API reference
- Codelabs
- Sample apps
- Videos

## Recommended for you
- Note: link text is displayed when JavaScript is off
- Locally scoped data with CompositionLocal
- Other considerations
- Anatomy of a theme in Compose
