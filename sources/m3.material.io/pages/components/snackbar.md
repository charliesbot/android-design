# Snackbar
Source: https://m3.material.io/components/snackbar

## Overview

- Snackbars shouldn’t interrupt the user’s experience
- Usually appear at the bottom of the UI
- Can disappear on their own or remain on screen until the user takes action

[IMAGE] Diagram of snackbar placement

#### Availability & resources

#### Differences from M2

- Color: New color mappings and compatibility with dynamic color
- Behavior: Clarified that snackbars can either appear temporarily (dismissive) or persist until the user takes an action (non-dismissive)

[IMAGE] Example of snackbar on screen bottom

_Snackbars have new color mappings_

## Specs

[IMAGE] Diagram of snackbar indicating the four parts of its anatomy

_- Container
- Icon (optional close affordance)
- Action (optional)
- Supporting text_

#### Tokens and specs

Browse the component elements, attributes, tokens, and their values. Learn more about design tokens

#### Color

Color values are implemented through design tokens. For design, this means working with color values that correspond with tokens. For implementation, a color value will be a token that references a value. Learn more about design tokens

[IMAGE] Diagram of snackbar indicating color and inverse text and labels

_Snackbar color roles used for light and dark schemes:
- Inverse surface
- Inverse on surface
- Inverse primary
- Inverse on surface_

#### Measurements

[IMAGE] Diagram of snackbar with action

_Snackbar padding and size measurements_

#### Configurations

[IMAGE] 5 snackbar configurations.

_- Single line
- Single line with action
- Two lines
- Two lines with action
- Two lines with longer action_

## Guidelines

[IMAGE] Snackbar at the bottom of a mobile device.

#### Usage

Snackbars inform users of a process that an app has performed or will perform. They appear temporarily, towards the bottom of the screen.

They shouldn't interrupt the user experience. People can browse the page content without being required to interact with the snackbar.

**Frequency**
Only one snackbar may be displayed at a time.

**Actions**
A snackbar can contain a single action. "Dismiss" or "cancel" actions are optional.

[IMAGE] Snackbar showing 'Email archived' text with an 'Undo' text button.

##### Similar components

Dialogs are also designed to show important messages.

Choose the right component based on the importance of the message. This component messaging strategy can help avoid overusing snackbars.

[IMAGE] Dialog on a phone requiring the user to sign in to continue. Dismissing prevents them from progressing.

_Dialogs require immediate action_

**When to use snackbars**
Snackbars communicate messages that are minimally interruptive and don’t require user action.

| Component | Priority | User action
| Snackbar | Low priority | Optional: Snackbars disappear automatically
| Dialog | High priority | Required: Dialogs block app usage until the user takes a dialog action or exits the dialog (if available)

##### Accessibility requirements for web

On web, auto-dismissing snackbars are inaccessible for people with low vision or who require additional time to perceive information. This can be solved in 2 ways:

###### 1. Add inline feedback

Information in auto-dismissing snackbars must also be communicated using another accessible method inline or near the action that triggered the snackbar.

For example, update the label on a "Save" button to “Saved”, and trigger an auto-dismissing snackbar that communicates the same message.

###### 2. Make the snackbar actionable

Alternatively, add actions to the snackbar so it doesn't dismiss until acted on.

[IMAGE] A button labelled "Save" changes to "Saved" after a moment. A snackbar confirms all changes are saved.

_Also communicate snackbar information near the action that triggered the snackbar_

#### Anatomy

[IMAGE] 4 elements of a snackbar.

_- Container
- Supporting text
- Action (optional)
- Close button (optional)_

##### Text label

Snackbars contain a text label that directly relates to the process being performed. In compact breakpoints, the text label can contain up to two lines of text.

[IMAGE] Snackbar on a mobile device reading: "Saved in Vacation album".

_Text labels are short, clear updates on processes that have been performed_

[IMAGE DO] Snackbar on mobile with one line of content.

**DO:** _Keep the snackbar text label to one line long when possible_

[IMAGE DO] Snackbar on mobile with two lines of content.

**DO:** _On mobile, the text label can be up to two lines long_

[IMAGE CAUTION] Snackbar on mobile with an icon and one line of content.

**CAUTION:** _Avoid adding icons to snackbars. If your message needs an icon, consider using a different component such as a dialog._

[IMAGE DON'T] Snackbar on mobile with bolded and hyperlinked words.

**DON'T:** _Avoid using stylized text or inline links in snackbars; they can add unwanted complexity. If your message needs a link, add a button instead, or use a different component._

##### Container

Snackbars are displayed in rectangular containers with a grey background. Containers should be completely opaque, so that text labels remain legible.

[IMAGE] Snackbar showing a light text label on a black color container.

_Snackbar containers use a solid background color with a shadow to stand out against content_

[IMAGE DON'T] A snackbar with button text the same color as supporting text.

**DON'T:** _The text label shouldn’t share the same color as the text button_

[IMAGE DON'T] A snackbar with the action in elevated style.

**DON'T:** _Don’t use a filled or elevated button in a snackbar, as it draws too much attention_

[IMAGE DO] An extended snackbar on tablet with a long text label.

**DO:** _In wide layouts, extend the container width to accommodate longer text labels_

[IMAGE CAUTION] Snackbar with a slightly transparent container and a clearly visible text label.

**CAUTION:** _An app can apply slight transparency to the container background, as long as text remains clearly legible_

[IMAGE DON'T]

**DON'T:** _Avoid significantly altering the shape of a snackbar container_

##### Action

Snackbars can display a single text button that lets users take action on a process performed by the app. Snackbars shouldn’t be the only way to access a core use case, to make an app usable.

[IMAGE] A snackbar container with rounded corners.

_To distinguish the action from the text label, text buttons should display colored text_

[IMAGE DO] Snackbar with a long text button displayed on a third line.

**DO:** _If an action is long, it can be displayed on a third line_

[IMAGE DO] Snackbar with a single text button labeled undo.

**DO:** _To allow users to amend choices, display an "Undo" action_

[IMAGE CAUTION] Snackbar with a single text button labeled dismiss.

**CAUTION:** _A dismiss action is unnecessary, as snackbar disappears on their own by default_

#### Placement

##### At the bottom of a UI

Snackbars should be placed at the bottom of a UI, in front of the main content. In some cases, snackbars can be nudged upwards to avoid overlapping with other UI elements near the bottom, such as FABs or docked toolbars.

Avoid placing a snackbar in front of frequently used touch targets or navigation.

[IMAGE DO] Snackbar appearing in front of photo content.

**DO:** _Place a snackbar in front of the main content_

[IMAGE DON'T] Snackbar placed in front of the navigation components.

**DON'T:** _Avoid placing snackbars in front of navigation components_

To ensure accessibility for keyboard users on the web, avoid positioning the snackbar in a way that completely obscures actionable elements. Blocking elements makes it difficult to know what is being focused and selected.

[IMAGE DO] Thin snackbar in front of a focused element that is still visible.

**DO:** _Adjust the size of the snackbar to avoid blocking elements in focus_

[IMAGE DON'T] Larger snackbar that is obscuring a focused element.

**DON'T:** _Don’t let the snackbar fully cover elements in focus_

Snackbars can span the entire width of the screen only when a UI does not use persistent navigation components like app bars or navigation bars.

Snackbars that span the entire width of a UI can push up FABs when they appear.

[IMAGE CAUTION] Snackbar spanning the width of a mobile device is placed in front of the navigation components and FAB.

**CAUTION:** _Snackbars can span the entire width of a UI. However, they should not appear in front of navigation or other important UI elements like floating action buttons._

**Snackbars and floating action buttons (FABs)**

Snackbars should appear above FABs.

[IMAGE] Snackbar placed above a FAB on a mobile device.

_Snackbar above a FAB_

[IMAGE DON'T] Snackbar placed in front of a FAB on a mobile device.

**DON'T:** _Don’t place a snackbar in front of a FAB_

[IMAGE DON'T] Snackbar placed behind a FAB on a mobile device.

**DON'T:** _Don’t place a snackbar behind a FAB_

#### Responsive layout

##### Compact breakpoint

In compact breakpoints, snackbars should expand vertically from 48dp to 64dp to accommodate one or two lines of text, while maintaining a fixed distance from the leading, trailing, and bottom edges of the screen.

[IMAGE] Snackbar with its label text extending to the second line and maintaining fixed distance from the edges of a mobile device.

##### Medium & expanded breakpoints

On medium and expanded breakpoints, like tablet and desktop, snackbars should scale horizontally to accommodate longer text strings, keeping in mind that the ideal line length for text is typically between 40-60 characters.

Snackbars use a flexible distance from the trailing edge of the screen. Whenever possible, snackbars on medium and large displays should aim for a single line of text with an optional button.

[IMAGE] A horizontally expanded snackbar placed at the bottom of screen on a medium-size device.

In wider layouts, snackbars can be left-aligned or center-aligned if they are consistently placed on the same spot at the bottom of the screen.

[IMAGE] A left-aligned snackbar placed at the bottom of screen on a medium-size device.

_Left-aligned snackbar_

[IMAGE] A center-aligned snackbar placed at the bottom of screen on a medium-size device.

_Center-aligned snackbar_

[IMAGE DON'T] Snackbar displayed at the left edge of the screen, near the bottom, on a medium-sized device.

**DON'T:** _Don’t place snackbars flush to one edge of the layout_

[IMAGE DON'T] 2 snackbars placed side-by-side at the bottom of the screen on a medium-size device.

**DON'T:** _Don’t place consecutive snackbars side by side or next to one another_

#### Behavior

##### Appearing and disappearing

Snackbars appear without warning, but they don’t block users from interacting with page content.

Snackbars without actions can auto-dismiss after 4–10 seconds, depending on platform. Avoid using auto-dismissing snackbars on web unless there's also inline feedback.

[VIDEO] Snackbar without an action button appearing and disappearing within 4 seconds from the screen of a mobile device.

Snackbars with actions should remain on the screen until the user takes an action on the snackbar, or dismisses it.

[VIDEO] Snackbars with actions should remain on the screen until the user takes an action on the snackbar, or dismisses it.

##### Consecutive snackbars

Consecutive snackbars must appear one at a time.

Snackbars without actions appear and disappear automatically, while those with actions remain on screen until dismissed. However, a snackbar with updated information can immediately replace an outdated snackbar.

[VIDEO] Two snackbars appear sequentially on a mobile device. The first includes an action button.

[VIDEO DON'T] 2 snackbars appear sequentially on a mobile device and are stacked on top of one another. The first includes an action button.

**DON'T:** _Don’t stack snackbars on top of one another_

[VIDEO DON'T] FAB's position is displaced when a snackbar enters the screen of a mobile device.

**DON'T:** _Don’t animate other components along with snackbar animations, such as the floating action button_

## Accessibility

##### Use cases

Users should be able to:
- Be alerted, but not disrupted, when a snackbar appears
- Move focus to an actionable snackbar
- Take action on a snackbar using assistive technology

##### Interaction & style

Snackbars with actions shouldn't auto-dismiss. This way, users can read and interact with it at their own pace.

Snackbars without actions can auto-dismiss after a sufficient amount of time, however this can still present difficulties on web without additional feedback.

Each platform has its own requirements for auto-dismiss durations, however common acceptable durations are 4–10 seconds.

[IMAGE] A snackbar saying "Email marked as read" with no button.

_Auto-dismissing snackbars should remain on screen long enough to read the information_

Snackbars use a color intended to stand out against UI elements. Use the default color mapping to avoid color conflict issues.

[IMAGE] Snackbar with a dark container on a UI page in light theme.

_Snackbar should visually stand out_

##### Accessibility requirements on web

On web, auto-dismissing snackbars can be difficult to navigate for people with low vision or who require additional time to perceive information. This information can be made clearer for all users in two ways:

###### 1. Add inline feedback

Information in auto-dismissing snackbars must also be communicated inline or near the action that triggered the snackbar.

For example, update the label on a "Save" button to “Saved”, and trigger an auto-dismissing snackbar that communicates the same message.

###### 2. Make the snackbar actionable

Alternatively, add actions to the snackbar so it doesn't dismiss until acted on. Actionable snackbars shouldn't auto-dismiss.

[IMAGE] A "save" button changes to say "saved", alongside a snackbar that confirms changes were saved.

_Communicate snackbar information near the action that triggered the snackbar_

**Note: Material Web doesn't yet include the snackbar component. This guidance still applies to custom-made snackbars.**

##### Focus

Snackbars have the following focus requirements:
- When a snackbar appears, announce the message but don't move focus.
- Don't automatically move focus.
- Don't trap focus in the snackbar. Users should be able to freely navigate in and out.
- On web, a shortcut should exist for users to move focus to snackbars with actions (like Alt+G). Ensure that this shortcut is clearly documented, like in a help article.

[VIDEO] Taking a menu item action moves focus to a snackbar. Moving focus out of the snackbar returns it to the same menu item.

_Focus returns from the snackbar (1) to the previously focused element (2)_

Focus exits the snackbar differently per platform:
- Ideally, focus should either return to the element that triggered the snackbar, or go to the next most logical element on the page.
- On Android Compose, focus may move to the nearest visible element, or to the first actionable item on the page.

[VIDEO] Confirming in a dialog moves focus to a snackbar. Moving focus out of the snackbar returns it to a page element.

_If the previously focused element is no longer on the page, focus should move from the snackbar (1) to the next most logical element (2)_

##### Keyboard navigation

|
Keys |
Actions
| Tab | Moves focus between interactive elements
| Esc | Dismisses the snackbar when in focus

##### Labeling elements

Snackbars should be announced once they appear on the screen, but shouldn’t grab focus or prevent people from completing their current task.
- On Android and web, use a live region with a polite (queued) announcement instead of an assertive announcement.
- On iOS 17+, snackbars use polite announcements by default.

If a snackbar appears when the app is launched, it should be announced after the page’s title, but not receive focus.

[IMAGE] Snackbar accessibility label examples.

_Snackbars are announced when they appear, but don't trap focus_
