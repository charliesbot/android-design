# Debug tests
Source: https://developer.android.com/develop/ui/compose/testing/debug

The main way to solve problems in your tests is to look at the semantics tree. Print the tree by calling `composeTestRule.onRoot().printToLog()` at any point in your test. This function prints a log like this:

```
Node #1 at (...)px
|-Node #2 at (...)px
OnClick = '...'
MergeDescendants = 'true'
|-Node #3 at (...)px
| Text = 'Hi'
|-Node #5 at (83.0, 86.0, 191.0, 135.0)px
Text = 'There'

```

These logs contain valuable information for tracking bugs down.

## Additional Resources
- **Test apps on Android**: The main Android testing landing page provides a broader view of testing fundamentals and techniques.
- **Fundamentals of testing:** Learn more about the core concepts behind testing an Android app.
- **Local tests:** You can run some tests locally, on your own workstation.
- **Instrumented tests:** It is good practice to also run instrumented tests. That is, tests that run directly on-device.
- **Continuous integration:** Continuous integration lets you integrate your tests into your deployment pipeline.
- **Test different screen sizes:** With some many devices available to users, you should test for different screen sizes.
- **Espresso**: While intended for View-based UIs, Espresso knowledge can still be helpful for some aspects of Compose testing.
