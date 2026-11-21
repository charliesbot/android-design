# Set up Predictive back
Source: https://developer.android.com/develop/ui/compose/system/predictive-back-setup

Predictive back and system animations are enabled by default. If your app intercepts the back event and you haven't migrated to predictive back, update your app to use supported back navigation APIs.

The predictive back-to-home animation.

The predictive cross-activity animation.

The predictive cross-task animation.

## Enable default system animations

The back-to-home, cross-activity, and cross-task system animations are available on Android 15 and later devices for apps that have migrated to the supported back handling APIs.
- **Back-to-home**: Returns the user to the home screen.
- **Cross-activity**: Transitions between activities within the app.
- **Cross-task**: Transitions between tasks.

These animations are enabled by default on Android 15 and higher. On devices running Android 13 or 14, users can enable them through the Developer options. **Note:** Intercepting back at the root activity (e.g. `MainActivity.kt`) disables the back-to-home animation, and intercepting back at an Activity disables the cross-activity animation.

To get the system animations, update your AndroidX `Activity` dependency to 1.6.0 or higher.

## Enable predictive back with Navigation Compose

To use predictive back in Navigation Compose, ensure you're using the `navigation-compose` 2.8.0 library or higher.

Navigation Compose automatically cross-fades between screens when the user swipes back:

**Figure 2.** The default crossfade in-app animation in SociaLite.

When navigating, you can create custom transitions with `popEnterTransition` and `popExitTransition`. When applied to your `NavHost`, these modifiers let you define how the enter and exit screens animate. You can use them to create a variety of effects, such as scaling, fading, or sliding.

In this example, `scaleOut` is used within `popExitTransition` to scale down the exiting screen as the user navigates back. Additionally, the `transformOrigin` parameter determines the point around which the scaling animation occurs. By default, it's the center of the screen (`0.5f, 0.5f`). You can adjust this value to make the scaling originate from a different point.

```
NavHost(
navController = navController,
startDestination = Home,
popExitTransition = {
scaleOut(
targetScale = 0.9f,
transformOrigin = TransformOrigin(pivotFractionX = 0.5f, pivotFractionY = 0.5f)
)
},
popEnterTransition = {
EnterTransition.None
},
modifier = modifier,
)
```

This code produces the following result:

**Figure 3.** A custom in-app animation in SociaLite.

`popEnterTransition` and `popExitTransition` specifically control animations when popping the back stack, with a back gesture, for example. You can also use `enterTransition` and `exitTransition` to define animations for entering and exiting composables in general, not only for predictive back. If you only set `enterTransition` and `exitTransition`, they are used for both regular navigation and popping the back stack. However, using `popEnterTransition` and `popExitTransition` lets you create distinct animations for back navigation.

## Integrate with shared element transitions

Shared element transitions provide a smooth visual connection between composables with shared content, often used for navigation.

**Figure 4.** Shared element transition with predictive back in Navigation Compose.

To use shared elements with Navigation Compose, see Predictive back with shared elements.

## Support predictive back with Material Compose components

Many components in the Material Compose library are designed to work seamlessly with predictive back gestures. To enable predictive back animations in these components, include the latest Material3 dependency (`androidx.compose.material3:material3-*:1.3.0` or higher) in your project.

The Material components that support predictive back animations include:
- `SearchBar`
- `ModalBottomSheet`
- `ModalDrawerSheet/DismissibleDrawerSheet`
- `ModalNavigationDrawer/DismissibleNavigationDrawer`

`SearchBar` and `ModalBottomSheet` automatically animate with predictive back gestures. `ModalNavigationDrawer`, `ModalDrawerSheet`, `DismissibleDrawerSheet`, and `DismissibleNavigationDrawer` require you to pass the `drawerState` to their respective sheet content composables.

## Test the predictive back gesture animation

If you still use Android 13 or Android 14, you can test the back-to-home animation.

To test this animation, follow these steps:
- On your device, go to **Settings > System > Developer options**.
- Select **Predictive back animations**.
- Launch your updated app, and use the back gesture to see it in action.

On Android 15 and later, this feature is enabled by default.

## Additional resources
- Add predictive back animations codelab
- Advanced layout animations in Compose video

Previous  arrow_back   About predictive back

Next  Access progress manually   arrow_forward
