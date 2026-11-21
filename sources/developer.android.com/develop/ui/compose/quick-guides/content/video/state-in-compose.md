# State in Compose
Source: https://developer.android.com/develop/ui/compose/quick-guides/content/video/state-in-compose

Learn how to establish and manage state in your Compose-based app and how to configure the UI to react to changes in state. See how to create observable states, how to retain state across recompositions or configuration changes, and how to structure your composables for optimal data flow.

## Key points
- If your app's state is internal to a composable, use `remember` to persist the state across recomposition.
- Use `rememberSaveable` to persist the state across configuration changes.
- _State hoisting_ is a programming pattern where you move the state to the caller of a composable. Where possible, use state hoisting to make the composable more reusable and testable.
- Use the `ViewModel` class to hold an exposed state in an observable state holder, better encapsulating the state and creating a single source of truth for the UI.

## Resources
- Codelab: State in Jetpack Compose

## Collections that contain this guide

This guide is part of these curated Quick Guide collections that cover broader Android development goals:

###### Compose basics (video collection)
This series of videos introduces various Compose APIs, quickly showing you what’s available and how to use them.
Quick guide collection
