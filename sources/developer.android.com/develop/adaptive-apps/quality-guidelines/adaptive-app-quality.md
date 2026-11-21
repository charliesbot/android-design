# Adaptive app quality guidelines
Source: https://developer.android.com/develop/adaptive-apps/quality-guidelines/adaptive-app-quality

Google Play Large Screen Badge

Get your app noticed on Google Play by users looking for premium app experiences on tablets, foldables, Googlebooks, and other large screen devices.

Complete Tier 2 of the Adaptive app quality guidelines to optimize your app for all screen sizes; Tier 1 to create a differentiated experience made for large screens. Then, get your Google Play badge!
Get your badge arrow_forward

Devices that can run Android apps come in a variety of form factors—phones, tablets, foldables, desktops, car displays, TVs, XR—which represent a wide range of display sizes. Android supports multiple display modes, including multi-window, multi-display, multi-instance, and picture-in-picture. Foldable devices can be in various folded states, or postures, such as tabletop posture or book posture.

[IMAGE] Depiction of the three quality tiers as layers stacked vertically.

To ensure your app provides a great user experience regardless of device form factor, screen size, display mode, or posture, follow the adaptive app compatibility checklists and complete the compatibility tests.

The checklists and tests define a comprehensive set of quality requirements for most types of Android apps. Tier 3 — Adaptive ready defines the baseline quality standard for apps on foldables and large screens on Google Play, while Tier 2 and Tier 1 help your app qualify for enhanced promotion and store badging.

The adaptive app quality guidelines replace and extend the guidance formerly provided in the large screen app quality guidelines.

As you enhance your app with adaptive capabilities, help users better understand your app's multi-form-factor experience by updating your app listing on Google Play. Upload screenshots that show off the app on tablets and foldables, including both square (1:1) and wide (4:3 landscape or 3:4 portrait) unfolded inner displays as well as folded outer screens (10:16 portrait or 16:10 landscape, and 9:16 or 16:9, respectively). Call attention to XR features in your app description. For more information, see Capture app screenshots for Google Play and Google Play Help.

For examples of optimized and differentiated layouts on screens of all sizes, see the adaptive layout gallery.

## Adaptive app compatibility checklists

The compatibility checklists define criteria to help you assess the level of support your app provides for adaptive design.

Levels of support include the following:

[IMAGE] Icon for Tier 3 Adaptive Ready

### TIER 3 (basic) — Adaptive ready

Your app runs full screen (or full window in multi-window mode) on all devices, but app layout might not be ideal. The app is not letterboxed; it does not run in compatibility mode. Users can complete critical task flows but with a less than optimal user experience. The app provides basic support for external input devices, including keyboard, mouse, trackpad, and stylus.

[IMAGE] Icon for Tier 2 Adaptive Optimized

### TIER 2 (better) — Adaptive optimized

Your app implements layout optimizations for all screen sizes and device configurations along with enhanced support for external input devices.

[IMAGE] Icon for Tier 1 Adaptive Differentiated

### TIER 1 (best) — Adaptive differentiated

Your app provides a user experience designed for the device or display the app is running on. Where applicable, the app supports multitasking, foldable postures, drag and drop, and stylus input.

Complete the Tier 2 requirements to enable your app to provide an excellent user experience on all Android devices. To make your app outstanding on foldables and large screens such as desktops, complete Tier 1.

## Adaptive app compatibility tests

The compatibility tests help you discover quality issues in your app. You can combine the tests or integrate groups of tests together in your own test plans.

For layout and UX purposes, test on at least the following device types:
- Googlebook (160 ppi)
- Foldable with 1:1 inner display (802x802 dp)
- Foldable with 4:3 inner display (1024x768 dp)
- 8-inch tablet (1024x640 dp)
- 10.5-inch tablet (1280x800 dp)
- 13-inch Chromebook (1600x900 dp)

Use the following Android emulators to test adaptive device compatibility and capture store listing screenshots:
- Foldable phone (1:1 inner display) — **Phone > Foldable (1:1 inner display)**
- Foldable phone (4:3 inner display) — **Phone > Foldable (4:3 inner display)**
- Foldable phone — **Phone > Pixel Fold**
- Tablet — **Tablet > Medium Tablet**
- Desktop — **Desktop > Desktop (Preview)** **Note:** To use the Desktop (Preview) emulator, download the Android Studio Canary build. See also Install a preview version of Android Studio.

Use the Android resizable emulator to test a variety of device configurations.

## Get started

Select a quality tier and get started making your app adaptive today!

-    Adaptive ready
-    Adaptive optimized
-    Adaptive differentiated

## Archive

Previous versions of the adaptive app quality guidelines:
- 2026-04-07 — Tier 2
- 2026-01-21 — Large screen app quality guidelines
  - ID map — Large screen app quality guidelines to adaptive app quality guidelines
