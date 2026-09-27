# 10 Steps to Translate Your iOS Designs for Android
Source: https://m3.material.io/blog/ten-steps-ios-android-design

> How to bring your iOS experiences to Android in record time

Published: 2021-04-16

As designers we are always working toward a deadline, under tight pressure and observant of business goals. Often, this means having to cut down scope, whether features, formats, or even entire platforms. Most often it’s the latter, especially if there is less familiarity with a platform or a severe time crunch. But I’m here to help you make translating a design from iOS to Android less intimidating and show how you can have a functional Android design, using Material Design, in minimal time.

This guide is for design parity and ordered for efficiency. If you are following a shared base design system, then you should have a translated design deliverable in just a few hours.

Both Android and iOS adhere to the idea that content comes first. After that, branding can come through as color, type, and shape. Not only does this allow for content legibility, but creating cohesion becomes easier.

_Helpful Tip before you start: have a user flow map available to help visualize, document, and sketch out any differences that would affect Android._

       

## Starting with your iOS designs

Before getting started, make a copy of your iOS app. iOS apps are broken down into 3 areas: Bars, Views, and Controls. We can utilize this structure to work through to translation, with styling last.

**1. Delete the iOS system UI. **(status bar and home indicator) It’s just easier to do this now.

**2. Resize your prototype artboard or frames.** A 360dp width is useful to accommodate the range of Android phones; there are lots of small Android devices out there. This can double as a smaller screen resource, too.

**3. Replace with Android System Bars.** Android system UI can vary depending on the user’s device and settings, but showing a stock system UI can help give your designs more context. Place the notification bar at the top and either a gestural navigation or three button navigation bar at the bottom.

**4. Depending on your navigation… swap the Tabbar (bottom navigation) for the Bottom Navigation Bar. **

Take a look back at your flow map. Is your iOS app utilizing a more menu? (HIG best practices suggest not to use this pattern). Stick to five or less items, keep the bottom navigation bar to primary navigation, evaluate if anything secondary, like profile or settings, can be moved into the Top App Bar, or maybe you have a primary action that can translate to a FAB?

Your primary navigation should always be present on parent views (the top level for a section in your flow map). Child views (anything under the parent view) can include primary navigation if they are higher up in the navigation hierarchy and not a modal.

Update the Bottom Navigation Bar with the appropriate icons and labels. Both platforms avoid lateral motion between navigation destinations.

**5. Navbars to Top App Bars.** This is where things get real Android-y.

Break these down section by section; parent views first, then child views. The Top App Bar (toolbar) is composed of the left side: navigation and title, and right side: action items.

If your app utilizes a nav drawer menu instead of a bottom nav bar, on all parent views, a drawer – or “hamburger” – icon will be shown).

If your app does not utilize a drawer, then parent views do not show a navigation icon.

The title is left aligned by default without theming in the Top App Bar for Android.

Child views hold an up arrow in the navigation icon spot. Not to be confused with back. The up arrow moves the user up a level through an app’s navigation hierarchy,in a user flow. while back or edge swipe lives in the system navigation, moving the user backwards and even taking the user out of the app. Android also has the option for gestural navigation on some devices.

Sidenote: If you are using large title navbars on iOS these translate well to Prominent Top App Bars on Android. The Top App Bar starts in an expanded state with a large title, and collapses to a default state on scroll. Typically large titles are reserved for parent views, where collapsing Top App Bars can be utilized in both parent and child views. Use your best judgement here as collapsing Top App Bars can take up a lot of space.

What about modal views? For full-screen modals (like video players and images), this will be similar to the child view Top App Bar, except the navigation icon should change to close… which dismisses the modal.

_Quick tip: iOS navbars will use text (Cancel or done) while Android keeps to icons in this space for dismissal. _

**6. A bit more modality. **Let’s start with large modal views, best used to focus the user’s attention on a task. On iOS these are often seen as sheets, where the user can swipe them down.

Finish up swapping out Top App Bars. For iOS sheet modals, swap out the top sheet portion and background peek with a fullscreen dialog Top App Bar. Bonus: take a look at whether any of your iOS modal sheets can translate to bottom sheets!
- Action and Activity sheets to Bottom sheets. (The share sheets can be translated now too).
- Alerts! Open up those system alerts. If you are using them for important information that you need the user to acknowledge in some way, swap them for system dialogs. Remember to swap any inputs and pickers at this point too!
- If you are utilizing an iOS Top App Bar for feedback, move that feedback into a snackbar. Give your dev some snackbar specs, and document best practices for giving the user feedback in your app.

**7. Let’s talk tabs.** Tabs or view pagers or swipey tabs… If you are using segmented controls on iOS these translate over to tabs on Android. They both act as a way to filter between views of information that is similar, but not the same. Android tabs are typically attached to the Top App Bar, and come with the added benefit of being able to swipe between content.

**8. Content & Controls.** Content comes first, except when quickly translating 😛

Depending how you have constraints or resizing behavior setup, most of your content possibly resized already. But take this time to go through and set your margins. 16dp is a good standard on small screens.
- Grids and Spacing. The baseline grid is based on an 8dp grid for components and 4dp for type and icons. An 8pt grid functions well on iOS, so possibly consider it as a starting point for both platforms.
- Controls. Switch those toggles to Android switches. Check off the Android checkboxes, and dial in the radio buttons. Android comes with all of these.
- If you have forms, swap the iOS text fields for Android ones. Material comes with a few fun options like outline or filled, so pick which one fits your branding best.
- Material Lists have some differences when compared to iOS table cells:
  - Divider lines are used sparingly
  - Indicators are not used on lists to help keep visual noise to a minimum
  - Dimensions adhere to the 8dp grid

**9. Stylezzz**
- Shadows: Android uses a system of elevation, measured in dp. Elevation is from one point of perspective (remember the paper thing). Update your iOS shadows to adhere to the elevation spec).
- Type: if using a system font, replace San Francisco! Roboto is the default System font for Android. That said, we encourage you to express your brand’s unique style with theming and downloadable Google fonts. (Find a font > Generate a Type Scale > Implement)
- Icons: Same here. If utilizing SF symbols, double check that all have been converted to Material Icons. Pick the variation that is right for your brand. Did you know you can use material icons on any platform?
- Motion: Android and iOS have distinct motion design, which should be respected for each platform. Material motion is informative, focused, and expressed. The Ripple is a distinct highlight used in components to provide touch feedback. The Motion System is a set of transition patterns to take advantage of container transform, shared axis, fade through, and fade animations. Consider if elements in your design have persistent containers, the relationship between elements, and how they need to enter or exit.

**10. Go back through and tidy up. **If you are translating a prototype, this is a good point to rewire things. Go back through your primary navigation. Then your Top App Bars, remembering the difference between up and back, and making sure to select page transitions that are Android appropriate (mentioned in step 9).

You should have a fully functioning prototype ready, and since you resized it, it’s ready for handoff.

## Style and component guide

If you have an existing design system or style guide you may have your own foundational styles (color, type, icons, shape) that can be used along with Material Design, just as you would use them alongside iOS guidance. Using Material Theming you can customize Material components with your brand’s unique style with color, type, and shape.

Having platform specific guidelines is not uncommon among multi platform products and can make your own design system more user-centered.

Finally, if you’re working without one, know that not every app or product needs a full custom design system! Consider creating a one-sheet style guide. A style guide is a document that outlines the foundational specifications for designs. Branding guidelines will often contain a style guide within them.

This can be easily copied for Android and used as the source for updating styles (or symbols, components, or a library).

[IMAGE] iOS mockups arranged into a user flow, next to a similar version for Android: 

[IMAGE] an iOS design without iOS system UI elements: 

[IMAGE] A UI diagram indicating a 360dp screen width: 

[IMAGE] UI showing Android system navigation bars: 

[IMAGE] Two UIs showing iOS vs Android bottom navigation bars: 

[IMAGE] Side by side iOS and Android versions of the same checklist UI: 

[IMAGE] An iOS sheet next to an Android full-screen dialog with app bar: 

[IMAGE] iOS tab bar next to Android tabs: 

[IMAGE] An iOS and Android version of the same form: 

[IMAGE] Cards on iOS vs Android: 

[IMAGE] A full UI on iOS and Android following the steps outlined above: 

[IMAGE] An example style guide: 

[IMAGE] iOS mockups arranged into a flow map: 

[IMAGE] Android mockups arranged into a flow map: 

[VIDEO] The finished product
