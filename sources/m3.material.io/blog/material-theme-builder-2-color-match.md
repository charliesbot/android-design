# An updated theming experience with Material Theme Builder 2.0
Source: https://m3.material.io/blog/material-theme-builder-2-color-match

> Celebrating two years of MTB with a major update including Color Match

Published: 2023-12-19

Material Theme Builder (MTB) turned two recently, and with that birthday we’re excited to announce the release of an updated MTB for Figma!

## Tooling journey

Working on the Material Theme Builder has been an exciting journey starting from a simple prototype to help display the, at the time, upcoming Material You dynamic color for designers in a usable format into a tool on multiple platforms and features.

Along the way there have been architecture changes. The very first prototypes were two separate modules: one to visualize colors and export to the now defunct Design System Package format and a separate project to create Android XML theming files from a DSP archive. The projects were merged, more platforms were added, and export was re-written. In a couple cases, we outgrew the framework and had to move to another, justifying the pain of a rewrite if the change was better for our users. You also get to benefit from the lessons you've learned along the way to fix the technical debt.Featured in a few conferences, seeing MTB featured in the I/O keynote was a highlight moment. Showing how important small experiences can make a big impact.

And plenty of listening to our users! We learned how users were understanding dynamic color, wanted to express their branding, what new platforms they wanted to use, and more. Y’all have guided the direction of Material Theme Builder the most and our 2.0 features reflect that.

As part of Material DevRel, our goal has always been about empowering app makers to create wonderful experiences. MTB started with the premise to make Material 3 easier to implement and we hope to continue on that mission.

## On to the updates

First, and possibly the biggest change: no more switching between dynamic and color tabs. The color features all live together in one panel.

This creates an overall faster experience as a result, and allows the creation of a Material compatible color scheme through any Material Color Utilities, MCU, features.

Theme control is still up top, but now has the overall theme swap button next to the Theme menu dropdown.

Speaking of the theme menu, it now contains theme management actions included to rename and delete themes.

Swapping to other theme variants is now within the color panel. Light and dark themes are still there. You can now future proof your themes. MTB creates new contrast themes, announced at I/O 2023, for even more accessible apps. Support will vary across platform code export.

Generating a color scheme from an image or one source color is still possible, using the wallpaper and content setting.

Or override the all key color sources, which now include error and neutral variants.

You’ll also find the latest tonal surface color roles to use in your color schemes.

## And a few other UX changes

Brand new color picker gives greater look at HCT, along with confirmation buttons.

It’s easier to play around with color schemes in the plugin without losing your current scheme. When done editing your theme, click **Update** to apply changes. **Cancel** will stop the process of updating for any last minute changes.

## What about brand colors?

Color fidelity is here. Turn on **Color match** in settings to have resulting schemes more closely match your key color inputs. The resulting scheme is still produced by the MCU algorithm to produce tonal values and assign color roles, with your input colors landing into container slots.

## Plus Variable support

After Figma’s variable announcement, we heard plenty of requests to bring variables in the Material Theme Builder. So with 2.0, variables will be generated alongside styles with some options, like generating reference values as variables. Variables do require access to additional modes, but styles are still available.

## Looking forward

It’s been a fun and exciting couple years for the theme builder and we look forward to providing more updates, including the planned 2.0 update for web.

Check out Material Theme Builder and M3 Design Kit on the Figma Community and happy theming.

[IMAGE] MTB screenshots: 

[IMAGE] Screenshot highlighting the "swap to theme" button: 

[IMAGE] Contrast theme examples: 

[IMAGE] Screenshots of source image and core colors: 

[IMAGE] HCT color picker: 

[IMAGE] : Color match diagram

[IMAGE] Screenshot of figma variables: 

[IMAGE] Illustration of the number 2.0:
