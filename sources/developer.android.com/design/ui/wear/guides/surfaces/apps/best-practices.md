# Best practices for designing apps
Source: https://developer.android.com/design/ui/wear/guides/surfaces/apps/best-practices

## Optimize for vertical layouts

Simplify your app’s design by using vertical layouts, which allow users to scroll in a single direction to traverse content.

check_circle

###### Do this
This app’s goal is to take the user from point A to B.

cancel

###### Don't do this
Use both vertical and horizontal scrolling, as this can make your app experience confusing.

## Show the time

Display the time (overlay) at the top, as this provides a consistent place for the user to view the time.

check_circle

###### Do this
Display the time at the top of the overlay, as this provides a consistent place for the user to view the time.

cancel

###### Don't do this
Display the time on a temporary dialog, confirmation overlay or a picker, for example, a user is only likely to see a confirmation screen very briefly.

## Accessible inline entry points

Ensure all actions are displayed inline, using clear iconography and labels for accessibility. This includes entry points to settings and preferences.

check_circle

###### Do this
Use both icons and labels when possible.

cancel

###### Don't do this
Rely solely on icons to prompt the user to take action.

## Use labels to orient users

For longer dialogs, help orient the user with labels as they scroll through the content.

check_circle

###### Do this
Use section breaks, labels, and other cues to organize content and help orient users as they scroll through longer views with mixed content.

cancel

###### Don't do this
Add a label for dialogs that contain a single content type.

## Elevate primary actions

Make it easy for users to take action in your app by pulling primary actions to the top of the overlay.

check_circle

###### Do this
Elevate non-ambiguous primary actions to the top.

cancel

###### Don't do this
Put the primary action on a very long page at the bottom.

## Show the scrollbar on scrolling screens

Only use the scroll-indicator on scrolling screens to avoid the wrong interaction expectation. Similarly, remember to add the scroll-indicator on scrolling screens to indicate at what point of the screen you're viewing.

check_circle

###### Do this
Show the scroll-indicator if the entire view scrolls.

cancel

###### Don't do this
Show the scroll-indicator on non-scrolling views, or not show the scrollbar on scrolling views.

## Design responsively for larger screen sizes

Ensure the components you use fill the available width and consider the height on non-scrolling layouts.

All Compose components are build responsively, but any customization to elevate your design and add additional value on larger displays is encouraged.

check_circle

###### Do this
Ensure content fills the available width and height and full screen elements (ProgressIndicators, TimeText, etc.) responsively adapt on non-scrolling layouts.

cancel

###### Don't do this
Use components with a fixed width that don’t fill the screen responsively or not adjust the behavior of content to fill the available space.

## Use responsive (percentage) margins

We recommend using percentage margins so the size of the margins adapts to the growing curve of the display.

check_circle

###### Do this
Use additional percentage margins to ensure content doesn’t get clipped at the top and bottom.

cancel

###### Don't do this
Components shouldn’t just scale to fill the available space without additional margins.
