# Semantics
Source: https://developer.android.com/develop/ui/compose/testing/semantics

UI tests in Compose use _semantics_ to interact with the UI hierarchy. Semantics, as the name implies, give meaning to a piece of UI. In this context, a "piece of UI" (or element) can mean anything from a single composable to a full screen. The _semantics tree_ is generated alongside the UI hierarchy and describes the hierarchy.

You can learn more about semantics generally in Semantics in Compose.

[IMAGE] Diagram showing a typical UI layout, and the way that layout would map to a corresponding semantic tree

**Figure 1.** A typical UI hierarchy and its semantics tree.

The semantics framework is primarily used for accessibility, so tests take advantage of the information exposed by semantics about the UI hierarchy. Developers decide what and how much to expose.

[IMAGE] A button containing a graphic and text

**Figure 2.** A typical button containing an icon and text.

For example, given a button like this that consists of an icon and a text element, the default semantics tree only contains the text label "Like". This is because some composables, such as `Text`, already expose some properties to the semantics tree. You can add properties to the semantics tree by using a `Modifier`.

```
MyButton(
modifier = Modifier.semantics { contentDescription = "Add to favorites" }
)
```

## Additional Resources
- **Test apps on Android**: The main Android testing landing page provides a broader view of testing fundamentals and techniques.
- **Fundamentals of testing:** Learn more about the core concepts behind testing an Android app.
- **Local tests:** You can run some tests locally, on your own workstation.
- **Instrumented tests:** It is good practice to also run instrumented tests. That is, tests that run directly on-device.
- **Continuous integration:** Continuous integration lets you integrate your tests into your deployment pipeline.
- **Test different screen sizes:** With some many devices available to users, you should test for different screen sizes.
- **Espresso**: While intended for View-based UIs, Espresso knowledge can still be helpful for some aspects of Compose testing.
