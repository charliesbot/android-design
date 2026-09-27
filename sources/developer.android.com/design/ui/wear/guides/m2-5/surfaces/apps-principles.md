# App design principles
Source: https://developer.android.com/design/ui/wear/guides/m2-5/surfaces/apps-principles

An app is one of the primary surfaces on Wear OS. Apps are different from complications or tiles, which are glanceable representations of app content. Apps display more information and support richer interactivity. The user often enters an app from another surface, such as a notification, complication, Tile, or voice action.

## Principles

Keep the following principles in mind when designing apps:
- **Focused:** Focus on critical tasks to help people get things done within seconds.
- **Shallow and linear:** Avoid creating hierarchies deeper than two levels. Aim to display content and navigation inline when possible.
- **Scrolling:** Apps can scroll. This is a natural gesture for users to see more content on the watch.

## Guidelines

Follow these guidelines when designing apps.

### Optimize for vertical layouts

Simplify your app's design by using vertical layouts, which allow users to scroll in a single direction to move through content.

check_circle

###### Do
This app's goal is to take the user from point A to point B.

cancel

###### Don't
Don't use both vertical and horizontal scrolling, as this can make your app experience confusing. The exception is some specific use cases, including media playback, which can support both vertical and horizontal scrolling.

### Show the time

Users tend to spend more time in apps, so it's important to provide quick access to the time.

check_circle

###### Do
Display the time at the top of the app, as this provides a consistent place for the user to view the time.

cancel

###### Don't
Display the time in a dialog, confirmation screen, or picker, as users are likely to spend only a few seconds on those screens.

For more information about design and usage, see Time text.

### Accessible inline entry points

Ensure all actions are displayed inline, using clear iconography and labels for accessibility. This includes entry points to settings and preferences.

check_circle

###### Do
Use both icons and labels when possible.

cancel

###### Don't
Rely solely on icons to prompt the user to take action.

### Elevate primary actions

Help users take action in your app by pulling primary actions to the top of the app. Elevate non-ambiguous primary actions to the top of the app.

### Use labels to orient users

For longer apps, help orient the user with labels as they scroll through the content.

check_circle

###### Do
Use section breaks, labels, and other cues to organize content and help orient users as they scroll through longer views with mixed content.

cancel

###### Don't
Add a label for apps that contain a single content type.

### Show the scrollbar

Show the scrollbar if the entire view scrolls, as shown in the following image. For more information, see Position indicator.

## Content containers

See the following examples of content containers.

[IMAGE] Example of Button row layout

**Figure 1.** Container of fixed height.

[IMAGE] Example of Button column layout

**Figure 2.** Container of variable height.

[IMAGE] Example of Button row layout

**Figure 3.** Container of height and width greater than the viewport.

[IMAGE] Example of Button column layout

**Figure 4.** A paginated container.

[IMAGE] Example of Button row layout

**Figure 5a.** Content pages that take the full dimension of the screen and are paginated vertically.
**Note:** Users find vertical layouts much easier to navigate than paginated UI's. Paginated UI's are best for situations when the user needs to navigate content using gross gestures, such as when working out or on the go. Because of this, they are generally used in workout and media app UIs.
