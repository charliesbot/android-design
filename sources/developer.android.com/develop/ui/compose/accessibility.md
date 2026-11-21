# Accessibility in Jetpack Compose
Source: https://developer.android.com/develop/ui/compose/accessibility

Developing with accessibility in mind means making your apps usable for everyone, including people with accessibility needs, who may use Android devices in many different ways. Compose provides a foundation for building more accessible UIs with its declarative APIs and tools that help make your apps more inclusive.

There are several key and supporting concepts in Compose accessibility:
- **API defaults**: Learn how Compose handles accessibility by default and how to leverage semantics and patterns to support accessibility from the start, and use them for custom components.
- **Semantics**: Understand the system of representing the meaning and role of UI elements for accessibility services, and how to choose appropriate semantics to represent properties like content types, descriptions, and states.
- **Modify traversal order**: Modify the order in which accessibility services navigate through elements on screen, which can be customized for better user experience.
- **Support user-scalable content**: Allow users to adjust the size of text and UI elements in your app to fit their needs.
- **Merging and clearing**: Understand semantic merging and clearing strategies and APIs, and when it is appropriate to hide semantics from accessibility services.
- **Inspect and debug**: Inspect your composables' accessibility semantics with tools and debug unexpected behaviors when using Android's assistive technologies.
- **Testing**: Detect common accessibility issues and automate some aspects of testing with Compose accessibility checks.
**Important:** For more information about accessibility in Android generally, see the accessibility guides.

## Additional resources
- **Accessibility in Jetpack Compose codelab:** Codelab for learning more about supporting accessibility in Compose.
- **What's new in accessibility for developers:** IO '22 talk.
- **Build accessible apps:** Essential concepts and techniques common to all Android app development.
- **Make apps more accessible:** Key steps you can take to make your app more accessible.
- **Principles for improving app accessibility:** Key principles to keep in mind when working to make your app more accessible.
- **Testing for Accessibility:** Testing principles and tools for Android accessibility.
