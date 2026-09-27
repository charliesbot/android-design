# Material Design Components for Android 1.6.1
Source: https://m3.material.io/blog/android-stable-release-1-6-1

> With color harmony and Material Design 3 refinements

Published: 2022-07-07

After cooling down from Google I/O, we have a pair of Material Design Components(MDC) releases to share with you: 1.6.0 and 1.6.1. As was the case with MDC 1.5.0, color is again a major theme with new APIs targeting dynamic color and color harmony.

Other notable highlights include Material3 style updates of various components and various bug fixes, like resolving issues on Android Oreo(8) and Pie(9), underscoring our commitment to more than just the most recent release.

Alongside the documentation we host on Github, the Material Design 3 site is an additional source for the styling guidance and availability of components. For components available on MDC Views, the implementation page will contain code snippets showing you how to implement that component and common use cases. Below is some of the code you can find on the implementation page for Chips.

## Color Harmony

In the blog post announcing MDC 1.5.0, we discussed using tooling to select colors beyond the core colors that exist in every Material3 theme. Custom or extended color inside of a dynamic theme presents an interesting question: “How do we guarantee that a reserved color will look well alongside a dynamic theme AND still be legible?”

The algorithm we use to create Material3 themes can handle this situation using a concept called color harmony. This harmonization works by shifting a target color towards a source color. Colors that are closer to each other appear more harmonious whereas colors that are further apart appear more discordant.

To read more about color science and the whys and hows of color harmony, check out The Science of Color & Design and Designing Harmony into Dynamic Color.

### Applying Color Harmony On Demand

Using the functions `harmonizeWithPrimary` and `harmonize`, you can harmonize a given color with the primary color for the theme or an arbitrary color.

### Applying Color Harmony Systematically

In most cases, instead of calculating harmonized colors on demand, you’ll want to leverage helper classes in MDC 1.6.0 that can take in a set of attributes or colors and return harmonized versions to your theme seamlessly. HarmonizedColorAttributes and HarmonizedColorOptions are two builder classes you can use to enable harmonization when you retrieve a dynamic color Context.

The snippet below shows how to build a dynamic context that automatically harmonizes to two specified colors.

For a more detailed discussion of how to apply color harmonization in code, check out the Color Harmonization section of our developer documentation on color or the codelab that demonstrates both methods.

## What’s next for MDC ?

We’re fast at work on the next major version of MDC. You can follow the progress, file bug reports and feature requests on GitHub. Also feel free to reach out to us on Twitter @materialdesign.

[IMAGE] Screenshot from material.io: 

[IMAGE] Screenshot from material.io: 

[IMAGE] Two swatches of yellow selected from a spectrum from yellow to green.: 

[IMAGE] Comparison of harmonious and less-harmonious color swatches.: 

[IMAGE] Diagram of color swatches moving toward user-selected color.:
