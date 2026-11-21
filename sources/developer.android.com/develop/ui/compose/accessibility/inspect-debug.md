# Inspect and debug
Source: https://developer.android.com/develop/ui/compose/accessibility/inspect-debug

## Inspect

Several tools can help you quickly inspect your content from an accessibility point of view:
- Android Accessibility Suite: Includes Accessibility Menu, Select to Speak, Switch Access, and TalkBack, which provide insight into how your app's semantics work for users of these technologies. Testing with Android's assistive technologies is highly recommended as the best way to understand what your users with accessibility needs will experience.
- Layout Inspector: Lets you inspect and debug semantics of each composable, and helps identify any missing or incorrect information.
- Accessibility Scanner app: Scans your screen and provides suggestions to improve its accessibility by identifying some common pitfalls.

## Debug

Between Compose, the semantics system, and Android accessibility services, you might run into unexpected accessibility behaviors that are difficult to trace. Semantic properties can help you understand why your components are behaving the way that they are.

You can debug accessibility behavior issues with the Layout Inspector in Android Studio, TreeDebug in TalkBack developer settings, or `ComposeTestRule`'s `printToLog`. All of these tools can provide information about nodes (and their properties) that are exposed to accessibility services by Compose.

The following example uses Layout Inspector to debug a screen with three elements where, with accessibility services on, the first one isn't being selected, and the second one doesn't have any action feedback associated with it. You can examine the semantic properties to find potential issues.

The component tree in Layout Inspector contains information about an element's bounds, parameters, and other semantic information associated with it. In the tree, all three elements are recognized:
[IMAGE] Layout Inspector with all three elements.

**Figure 2.** Layout Inspector with all three elements..

The first element has the `hideFromAccessibility` property applied. This indicates that the element may be marked as hidden somewhere in the semantics tree, or it is obscured by some decorative overlay.
[IMAGE] Layout Inspector: first element with hideFromAccessibility

**Figure 3.** Layout Inspector: first element with `hideFromAccessibility`.

The second element has a focus property, but no `onClick` like the previous element. Therefore, it might be missing a `clickable` modifier somewhere, which is why an accessibility service like TalkBack may not be announcing some action signal to the user:
[IMAGE] Layout Inspector: second element with focused

**Figure 4.** Layout Inspector: second element with `focused`.

The third text element has all the necessary properties—it is focusable, has an `onClick`, and other additional semantics applied—which is why it's interpreted as expected.
[IMAGE] Layout Inspector: third element with all APIs.

**Figure 5.** Layout Inspector: third element with all APIs.

In this way, you can use debugging tools to investigate why certain announcements or selections aren't performed by accessibility services.

## Recommended for you
- Note: link text is displayed when JavaScript is off
- Accessibility in Compose
- [Material Design 2 in Compose][19]
- Testing your Compose layout
