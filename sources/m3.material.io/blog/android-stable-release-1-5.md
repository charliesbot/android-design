# Material Design Components for Android 1.5.0
Source: https://m3.material.io/blog/android-stable-release-1-5

> With Material Design 3 refinements and more color utilities

Published: 2022-02-17

Back in October, at Android Dev Summit, we released a preview of Material Design 3 for Material Design Components (MDC).

Now we’re ready to announce the stable release of MDC 1.5.0 with more Material Design 3 support, component fixes and more color utilities. You can check out the release notes here.

If you held off on migrating last year, now is a great time to explore the new design system. Beyond the resources listed above, some areas you should focus your attention on are the Material 3 Catalog App and extended color utilities.

## Material 3 Catalog App

Our engineering team uses the Material Catalog app to verify and show example implementations of new components and features relating to Material Design. It’s a great source for discovering the new system.

If you haven’t yet migrated to Material 3 and dynamic color, the catalog app offers a settings area allowing you to see how a default light, dark, or user-generated theme will look throughout the app.

You can download the apk file here. For each screen, there’s also source code to check out, organized by component.

## Extended Color Utilities

Also at Android Dev Summit, we launched the Material Theme Builder for Figma and the web. It provides designers and developers a means to experiment with dynamic color and Material 3 themes with the option to export as code.

We've been pleasantly surprised by the community adoption of Material Theme Builder. We've also heard from you that you want to incorporate color roles from more than the standard key colors. At present, we only export the seed color for each extended (non-key) color, despite showing a visualization of them.

In anticipation of more color features coming in the library, MDC 1.5.0 has a function in the MaterialColors class named `getColorRoles` that will return:
- `accent`
- `onAccentColor`
- `accentContainer`
- `onAccentContainer`

The full signature is as follows:

With this ColorRoles object, you could retheme a component at runtime. In the following snippet, we change the colorContainer and onColorContainer values for a button. Always remember to reassess the onColorContainer(text) tone when altering the colorContainer tone to ensure text has proper contrast and readability.

## What’s next for MDC?

We’re fast at work on the next major version of MDC. You can follow the progress, file bug reports and feature requests on GitHub. Also feel free to reach out to us on Twitter @materialdesign.

[IMAGE] Home screen of Material Components Catalog App: 

[IMAGE] Theme and Dynamic Color Settings Selection in Material Components Catalog App: 

[IMAGE] Default UI of Material Theme Builder: 

[IMAGE] Material Theme Builder view with extended colors visualized: 

[IMAGE] Android sample project with no modifications applied: 

[IMAGE] : 

[IMAGE] Runtime generation of green light theme color roles applied to a button:
