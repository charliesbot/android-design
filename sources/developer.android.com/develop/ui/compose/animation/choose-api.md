# Choose an animation API
Source: https://developer.android.com/develop/ui/compose/animation/choose-api

The following diagram helps you decide what API to use to implement your animation.

[IMAGE] Flowchart describing the decision tree for choosing the appropriate animation API

**Figure 1.** Decision tree describing how to choose the appropriate animation API.

[IMAGE] Flowchart describing the decision tree for choosing the appropriate animation API

Use the following decision tree to choose the most appropriate animation API for your use case:
- Is your animation art-based (that is, SVGs or images)?
  - Yes: Does it use simple SVGs (that is, an icon with micro-animations)?
    - Yes: `AnimatedVectorDrawable`.
    - No: Third-party animation framework, for example, `Lottie`.

  - No: Does the animation need to repeat infinitely?
    - Yes: `rememberInfiniteTransition`.
    - No: Are you animating a layout?
      - Yes: Are you switching between composables with different content?
        - Yes: Are you using Navigation-Compose?
          - Yes: `composable()` with `enterTransition` and `exitTransition` set.
          - No: `AnimatedContent`, `Crossfade`, or `Pager`.

        - No: Are you animating the appearance or disappearance of content?
          - Yes: `CapturedAnimatedVisibility` (when content isn't live - snapshot of the state at exit), `AnimatedVisibility` (when exiting content stays live or uses `Modifier.animateEnterExit`), or `animateFloatAsState` with `Modifier.alpha()`.
          - No: Are you animating a size change?
            - Yes: `Modifier.animateContentSize`.
            - No: Are you animating another layout property (for example, offset or padding)?
              - Yes: See "Are the properties completely independent of each other?".
              - No: Are you animating list items?
                - Yes: `animateItem()`.

      - No: Are you animating multiple properties?
        - Yes: Are the properties completely independent of each other?
          - Yes: `animate*AsState`. For Text, use `TextMotion.Animated`.
          - No: Do they need to start at the same time?
            - Yes: `updateTransition` with `AnimatedVisibility`, `animateFloat`, `animateInt`, etc.
            - No: `Animatable` with `animateTo`, called with different timings using suspend functions.

        - No: Does the animation have predefined target values?
          - Yes: `animate*AsState`. For Text, use `TextMotion.Animated`.
          - No: Is the animation gesture-driven and the single source of truth?
            - Yes: `Animatable` with `animateTo` / `snapTo`.
            - No: Is it a one-shot animation without state management?
              - Yes: `AnimationState` or `animate`.
              - No: Answer not here? File a feature request.

Download the PDF version of the diagram.

Previous  arrow_back   Overview

Next  Quick guide   arrow_forward
