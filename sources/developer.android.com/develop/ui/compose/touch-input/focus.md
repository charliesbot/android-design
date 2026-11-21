# Focus in Compose
Source: https://developer.android.com/develop/ui/compose/touch-input/focus

When users interact with your app, they often do so using non-touch input mechanisms:
- **Desktop environments**: Users navigate and input data with physical **hardware keyboards** and trackpads.
- **Android TV**: Users navigate the interface using **D-pad**.
- **Automotive**: Drivers and passengers interact using **rotary controllers**.
- **Foldables and tablets**: Users attach keyboard covers and external input accessories.

In all these scenarios, your app must track which interactive element is active on screen—this active state is called _focus_.

## Key factors for intuitive keyboard navigation

To deliver a great user experience across large screens and non-touch devices, design your app with the following principles:
- **Logical initial focus**: Place the initial focus on the element users are most likely to interact with when entering a screen.
- **Consistent navigation order**: Provide predictable one-dimensional (Tab) and two-dimensional (arrow keys or D-pad) focus traversal.
- **Persistent focus**: Restore focus seamlessly after interruptions, dialog dismissals, or configuration changes.
- **Clear focus state**: Display prominent, accessible visual cues (such as focus rings or ripples) indicating which element currently has focus.
- **Intuitive scrolling**: Ensure the focused element and its surrounding context smoothly scroll into the visible viewport.

## Focus guides

Explore the following guides to learn how to manage and customize focus in Compose:
- **Focus targets**: Learn how interactive components become focus targets and how to make custom composables focusable.
- **Focus traversal order**: Understand 1D appearance order and 2D spatial navigation, and how to customize traversal paths.
- **Focus in text fields**: Learn how single-line and multi-line text fields handle Tab and arrow keys, and how TV IME interaction works.
- **Group focus targets**: Group components with `focusGroup` and intercept entry or exit redirection.
- **Restore focus**: Restore focus to previously active children in containers, handle navigation components, and manage focus across screen transitions.
- **Indicate focus state**: Apply visual cues and custom indication to highlight focused components.
- **React to focus**: Observe focus changes and query focus state properties using `onFocusChanged`.
- **Programmatically request focus**: Explicitly move focus in response to user actions or validation errors.
- **Move and clear focus**: Programmatically advance or clear focus using `FocusManager`.
- **Capture and release focus**: Capture focus for modal sub-interactions and safely release it.
- **Test focus navigation**: Write automated Compose tests to verify 1D, 2D, and text field keyboard interactions.

## Recommended for you
- Note: link text is displayed when JavaScript is off
- Focus targets
- Focus traversal order
- Focus in text fields
- Group focus targets
- Test focus navigation

Next  Focus targets   arrow_forward
