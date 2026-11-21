# Animation Preview
Source: https://developer.android.com/develop/ui/compose/tooling/animation-preview

Android Studio lets you inspect animations from Animation Preview. If an animation is described in a composable preview, you can inspect the exact value of each animated value at a given time, pause the animation, loop it, fast-forward it, or slow it, to help you debug the animation throughout its transitions:

[IMAGE] Play back, scrub through, and slow down the
AnimatedVisibility

You can also use Animation Preview to graph visualize animation curves, which is useful for making sure that the animation values are choreographed properly:

[IMAGE] Visualization of an animation
curve

Animation Preview automatically detects inspectable animations, which are indicated by the **Start Animation Preview** icon
[IMAGE] Play button to start animation preview
.

[IMAGE] Start Animation Preview icon in Design
window

If you have multiple animations, you can use Animation Preview to inspect and coordinate them all at once. You can also freeze a specific animation.

[IMAGE] Gif showing inspection with All Animations
UI

Use pickers to set non-enum or boolean states to debug your Compose animation using precise inputs. For all supported Compose Animation APIs, you can play, pause, scrub, control speed, and coordinate.

[IMAGE] Pick precise values for animation previews

Animation Preview supports the `updateTransition`, `AnimatedVisibility`, `animate*AsState`, `CrossFade`, `rememberInfiniteTransition`, and `AnimatedContent` APIs. To access the latest features, use Animation Preview with Android Studio Rabbit 2 and `compose.animation` 1.12.1 and higher.

## Recommended for you
- Note: link text is displayed when JavaScript is off
- Animations in Compose
- Animation tooling support {:#tooling}
- Value-based animations

Previous  arrow_back   Preview your UI

Next  Develop code iteratively   arrow_forward
