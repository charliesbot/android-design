# Dialogs
Source: https://m3.material.io/components/dialogs

## Overview

- Use dialogs to make sure users act on information
- Two variants: basic and full-screen
- Should be dedicated to completing a single task
- Can also display information relevant to the task
- Commonly used to confirm high-risk actions like deleting progress

[IMAGE] Basic and full-screen dialog.

_- Basic dialog
- Full-screen dialog_

#### Availability & resources

#### Differences from M2

- Color: New color mappings and compatibility with dynamic color
- Layout: Greater padding to account for the increased corner-radius and title size
- Position: Option for custom basic dialog positioning
- Shape: Increased corner-radius
- Typography: Larger and darker headline

[IMAGE] Basic dialog with rounded corner, larger headline.

_New updates to color, layout, position, shape, and typography_

## Specs

#### Tokens & specs

Select a component variant below to see its elements, attributes, tokens, and their values.

#### Basic dialogs

[IMAGE] Anatomy diagram numbering dialog elements.

_- Container
- Icon (optional)
- Headline (optional)
- Supporting text
- Divider (optional)
- Button label text
- Scrim_

##### Basic dialog color

Color values are implemented through design tokens. For design, this means working with color values that correspond with tokens. For implementation, a color value will be a token that references a value. Learn more about design tokens

[IMAGE] Color mapping diagram labeling 6 color roles across the dialog and scrim.

_Basic dialog color roles used for light and dark themes:
- Surface container high
- Secondary
- On surface
- On surface variant
- Primary
- Scrim_

##### Basic dialog measurements

[IMAGE] Annotated diagram showing padding values.

_Basic dialog padding and size measurements_

| Attribute | Value
| Container shape
| 28dp corner radius
| Container height
| Dynamic
| Container width
| Min 280dp; Max 560dp
| Divider height
| 1dp
| Icon size
| 24dp
| Minimum width
| 280dp
| Maximum width
| 560dp
| Alignment with icon
| Center-aligned
| Alignment without icon
| Start-aligned
| Top/Left/right/bottom padding
| 24dp
| Padding between buttons
| 8dp
| Padding between title and body
| 16dp
| Padding between icon and title
| 16dp
| Padding between body and actions
| 24dp

#### Full-screen dialogs

[IMAGE] Diagram numbering 6 full-screen dialog elements.

_- Container
- Header
- Icon (close affordance)
- Headline (optional)
- Text button
- Divider (optional)_

##### Full-screen dialog color

Color values are implemented through design tokens. For design, this means working with color values that correspond with tokens. For implementation, a color value will be a token that references a value.

[IMAGE] Color mapping diagram shows 5 callout markers across the dialog.

_Full-screen dialog color roles used for light and dark themes:
- Surface container high
- On surface
- On surface
- Primary
- On surface variant_

##### Full-screen dialog measurements

[IMAGE] Diagram noting layout measurements for padding values, title, height, and action regions.

_Full-screen dialog padding and size measurements_

| Attribute | Value
| Container shape
| 0dp corner radius
| Container height
| Dynamic
| Container width
| Container width; Max 560dp
| Header height
| 56dp
| Header width
| Container width
| Headline text alignment
| Start-aligned
| Divider height
| 1dp
| Icon (close affordance) size
| 24dp
| Bottom action bar height
| 56dp
| Bottom action bar width
| Container width
| Top/left/right padding
| 24dp
| Padding between elements
| 8dp

## Guidelines

[IMAGE] Basic dialog in isolation

_A basic dialog_

#### Usage

A dialog is a modal window that appears in front of app content to provide critical information or ask for a decision. Dialogs disable all app functionality when they appear, and remain on screen until confirmed, dismissed, or a required action has been taken.

Dialogs are purposefully interruptive, so they should be used sparingly. A less disruptive alternative is to use a dropdown menu, which provides options without interrupting a user’s experience.

[IMAGE] Diagram of basic and full-screen dialogs.

_There are two variants of dialogs:
- Basic dialog
- Full-screen dialog_

[IMAGE DO] Dialog in front of app content.

**DO:** _Use dialogs for prompts that block an app’s normal operation, and for critical information that requires a specific user task, decision, or acknowledgement_

[IMAGE DON'T] Low-priority dialog in front of app content.

**DON'T:** _Don’t use dialogs for low- or medium-priority information. Instead use a snackbar, which can be dismissed or disappear automatically._

##### Similar components

Snackbars are also designed to show important messages.

Choose the right component based on the importance of the message. This component messaging strategy helps avoid overusing dialogs.

[IMAGE] Snackbar on a phone saying that new photos were synced to the device. No buttons exist.

_Snackbars can disappear automatically_

|
**Component** |
**Importance** |
**Action needed**
| Snackbar | Low importance | Optional: Snackbars may not have a button, and can disappear automatically
| Dialog | High importance | Required: Dialogs block the main content until an action is confirmed

#### Anatomy

###### Basic dialog

[IMAGE] Diagram of 7 elements of basic dialog.

_- Container
- Icon (optional)
- Headline (optional)
- Supporting text
- Divider (optional)
- Buttons label text
- Scrim_

##### Full-screen dialog

[IMAGE] 6 elements of full-screen dialog.

_- Container
- Header region
- Icon (close affordance)
- Headline (optional)
- Button label text
- Divider (optional)_

##### Container and scrim

Dialog containers appear above other screen elements and hold the dialog’s headline, text, buttons, and list items.

To focus attention on the dialog, surfaces behind the container are scrimmed with a temporary overlay to make them less prominent.

[IMAGE] Basic dialog shown above a scrim overlay that reduces the prominence of the background elements.

_Basic dialogs appear over a background scrim_

##### Headline (optional)

A dialog’s purpose should be communicated by its headline and buttons or actionable items.

Headlines should:
- Contain a brief, clear statement or question
- Avoid apologies (“Sorry for the interruption”), alarm (“Warning!”), or ambiguity (“Are you sure?”)

[IMAGE DO] Dialog title asking “Use location service?”

**DO:** _This dialog title poses a specific question, concisely explains what’s involved in the request, and provides clear actions_

[IMAGE DON'T] Dialog title asking “Are you sure?”

**DON'T:** _Don’t use dialog titles that pose an ambiguous question_

Headlines should always be succinct. They can wrap to a second line if necessary, and be truncated.

In full-screen dialogs, long headlines or headlines of variable lengths (such as translations), can be placed in the content area instead of the app bar.

[IMAGE CAUTION] Example full-screen dialog with truncated long headline.

**CAUTION:** _Avoid placing long headlines in a full-screen dialog’s app bar (1), as the truncated text may lead to misunderstanding_

[IMAGE DO] Example full-screen dialog with short headline, and longer text in content area.

**DO:** _Find ways to shorten app bar text, and place longer headlines into the content area (1) of a full-screen dialog_

##### Buttons

Dialog actions are most often represented as buttons and allow users to confirm, dismiss, or acknowledge something.

Buttons are aligned to the trailing edge of the dialog for easier interaction. The confirmation button is always closest to the edge.

Button alignment responds automatically for right-to-left languages, where the confirmation button is aligned to the left edge.

[IMAGE DO] Dialog with the confirmation button disabled because a required radio selection is missing.

**DO:** _Disable confirming actions (1) until a choice is made. Dismissive actions are never disabled._

[IMAGE DON'T] Dialog with the dismissing action "Cancel" on the right of the 2 buttons.

**DON'T:** _Don’t place dismissive actions (1) to the right of confirming actions. Instead, place them to the left of confirming actions._

[IMAGE DO] Dialog with a single-action button: “OK”.

**DO:** _A single action may be provided only if it’s an acknowledgement_

[IMAGE DON'T] Dialog with 2 button choices: “Cancel”, “Got it”.

**DON'T:** _Avoid presenting people with unclear choices. **Cancel** doesn't make sense here because no clear action is proposed._

Dialogs should contain a maximum of two actions.
- If a single action is provided, it must be an acknowledgement action
- If two actions are provided, one must be a confirming action, and the other a dismissing action

[IMAGE DO] Dialog with 2 buttons side-by-side: “Disagree”, “Agree”.

**DO:** _Display two text buttons next to one another_

[IMAGE CAUTION] Dialog with 2 stacked buttons: “Turn on speed boost”, “No thanks”.

**CAUTION:** _Stacked buttons accommodate longer button text, but take up more room. Confirming actions appear above dismissive actions._

Providing a third action, such as **Learn more**, is not recommended as it navigates the user away from the dialog, leaving the dialog task unfinished.

Rather than adding a third action, an inline expansion can display more information. If more extensive information is needed, provide it prior to entering the dialog.

[IMAGE CAUTION] Dialog with 3 text buttons: Learn more, Disagree, Agree.

**CAUTION:** _The **Learn more** action (1) navigates away from this dialog, potentially leaving it in an indeterminate state_

#### Basic dialog

Basic dialogs interrupt users with urgent information, details, or actions. Common use cases for basic dialogs include alerts, quick selection, and confirmation.

[IMAGE] Example of basic dialog action request.

_Basic dialogs require a person to take action before it will close_

[IMAGE] Example of basic dialog confirmation.

_Basic dialogs can give people the ability to provide confirmation of a choice before committing to it_

Basic dialogs most often appear as alerts or lists, but can have a variety of layouts and component combinations, including lists, date pickers, and time pickers.

[IMAGE] Date picker dialog.

_Date picker dialogs allow people to tap a date, then confirm it by tapping **OK**_

[IMAGE] Time picker dialog.

_Time picker dialogs allow people to move the clock hand and then confirm by tapping **OK**_

#### Full-screen dialog

Full-screen dialogs fill the entire screen, containing actions that require a series of tasks to complete. One example is creating a calendar entry with the event title, date, location, and time.

Because they take up the entire screen, full-screen dialogs are the only dialogs over which other dialogs can appear.

Use a container transform pattern to transition a FAB into a full-screen dialog.

[VIDEO] A FAB transitioning into a full-screen dialog.

_Full-screen dialogs contain actions that require a series of tasks to complete_

When a full-screen dialog is closed without being saved, a basic dialog appears in front of it to confirm selections should be discarded without saving changes.

[VIDEO] Closing a full-screen dialog triggers a basic dialog asking to discard the changes.

_A basic modal dialog appears when a full-screen dialog is closed without being saved_

Full-screen dialogs may be used for content or tasks that meet any of these criteria:
- Dialogs that include components which require keyboard input, such as form fields
- When changes aren’t saved instantly
- When components within the dialog open additional dialogs

Full-screen dialogs are for compact breakpoints only, like mobile devices. For medium and expanded breakpoints, use a basic dialog.

##### Saving selections

To save a selection in a full-screen dialog, use **Save**. The close icon or dismissive action, such as **Cancel** or **Back**, should close the dialog.

##### Confirmation

The confirmation action should be clear about what happens next, like **Send** or **Create**. Avoid using vague terms like **Done**, **OK**, or **Close**. Only trigger an additional basic dialog if the action fails. Don’t disable the confirmation button.

[IMAGE DO] Full-screen dialog with create button as confirmation action.

**DO:** _A **Create** button is clear that the event will be created_

[IMAGE DON'T] Full-screen dialog with an additional basic dialog asking if you want to create this event.

**DON'T:** _Don’t trigger a basic dialog when the confirming action is selected_

##### Dismissing

When someone dismisses a full-screen dialog, a basic dialog should appear to confirm that they want to discard the unsaved changes.

[IMAGE DO] A basic dialog with options to either keep editing or discard unsaved changes.

**DO:** _Use a basic dialog to confirm that the user wants to discard unsaved changes_

[IMAGE DON'T] A full-screen dialog with a Close button as the confirming action.

**DON'T:** _Don’t use the confirming action to dismiss the full-screen dialog_

##### Error messages

Errors about the dialog fields should always appear inline where they occur. Some components like text fields have built-in error messaging, while others like checkboxes and radio buttons need error messages to be added next to the fields.

General errors such as network issues preventing saving or submitting should appear in a basic dialog when the confirming action fails.

Error messages should clearly but briefly explain the source of the error and how to fix it. Show all errors on the page at once so people can fix everything before trying again.

[IMAGE DO] A full-screen dialog with inline error messages for text fields.

**DO:** _Error messages related to the fields should be displayed inline_

[IMAGE CAUTION] A basic dialog mentioning that entries were not saved due to a connection issue.

**CAUTION:** _Errors unrelated to the fields can be displayed in a basic dialog_

##### Dialog windows

Launching a full-screen dialog temporarily resets the app’s perceived elevation, allowing simple menus or dialogs to appear above the full-screen dialog. They cover the screen and don’t appear as a floating modal window.

###### Navigation

Because full-screen dialogs can only be completed, dismissed, or closed, the close “X” icon button should be the only navigation option in the app bar.

#### Adaptive design

Dialogs can swap variants as the breakpoint changes. For example, a full-screen dialog can change into a basic dialog at larger breakpoints.

[IMAGE] Example of full-screen dialog on left, simple dialog on right

_- Full-screen dialog on mobile
- Dialog on a tablet_

##### Medium breakpoint

Basic dialogs appear in a center position by default.

Their position can be overridden to provide a more ergonomic experience.

[IMAGE] Basic dialog on tablet photos app.

_Dialog custom positioned on the right side of the screen_

##### Expanded breakpoint

Dialogs on expanded breakpoints, like desktop, are modal windows above a scrim. This puts the dialog at the forefront of a person's view, calling attention to the action prompted in the dialog.

[IMAGE] Example of desktop dialog.

_Desktop dialogs call attention to the required action_

Basic dialogs can be custom-positioned anywhere on larger screens, respecting margins to prevent edge collision.

[IMAGE] Basic dialog position diagram.

_Custom placement area for basic dialogs that respects a 56dp margin from the edges of the screen_

#### Behavior

##### Appearing

Dialogs appear without warning, requiring users to stop their current task. They should be used sparingly, as not every choice or setting warrants interruption.

Dialogs use an enter and exit transition pattern to appear on screen.

[VIDEO] Dialog entering and exiting screen using fade transition.

_A dialog appears with an enter and exit transition_

##### Position

Dialogs retain focus until dismissed or an action has been taken, such as choosing a setting. They shouldn’t be obscured by other elements or appear partially on screen, with the exception of full-screen dialogs.

[IMAGE] A basic dialog covering a full-screen dialog.

_Dialogs shouldn’t be obscured by other elements except for full-screen dialogs_

##### Scrolling

Most dialog content should avoid scrolling. Even when scrolling is required, the dialog title is pinned at the top, with buttons pinned at the bottom. This ensures selected content remains visible alongside the title and buttons, even upon scroll.

Dialogs don’t scroll with elements outside of the dialog, such as the background.

[VIDEO] Example of fixed dialog title and buttons.

_When viewing a scrollable list of options, the dialog title and buttons remain fixed_

## Accessibility

#### Use cases

People should be able to use assistive technology to:
- Open and close a dialog
- Provide and submit other inputs if the dialog is interactive, such as a text field or selectable list
- Scroll the dialog to access all of its contents if that content extends beyond the container of the dialog

#### Interaction & style

##### Use sparingly

Dialogs are purposefully interruptive. This means they appear in front of app content and disrupt the flow of content for people who may, for example, be using a screen reader to navigate the page.

As such, dialogs should be used sparingly and only to provide critical information. Less critical information should be presented in a non-blocking way within the flow of app content.

[IMAGE DO] An inline tooltip doesn’t block a photo app’s content on a mobile screen.
A modal dialog blocks the content of a photo app on a mobile screen.

**DO:** _Present non-critical information using other UI within the flow of app content_

[IMAGE DON'T] A modal dialog blocks the content of a photo app on a mobile screen.
A modal dialog blocks the content of a photo app on a mobile screen.

**DON'T:** _Avoid putting non-critical information in a dialog_

##### 200% text size

Avoid excessive text wrapping or truncation by choosing concise strings.

On Android, headlines should be kept concise enough to fit within **four** lines after the text size is increased to 200%. If a headline exceeds this limit and gets truncated, provide an alternative way to access the full content in a single tap.

[IMAGE CAUTION] A dialog with 200% text wraps multiple times in the header and description. It covers most of the mobile screen.

**CAUTION:** _Avoid excessive text wrapping or truncation by choosing concise strings_

##### Elements within dialogs

Because dialogs can contain various elements within them, refer to the relevant accessibility guidelines for each element.

Some common examples include:
- Text fields
- Typography
- Buttons

[IMAGE] 3 elements of a full-screen dialog.

_Full-screen dialogs can contain various elements such as (1) text fields, (2) typography, and (3) buttons, which each may have their own accessibility guidelines_

#### Initial focus

When a dialog appears, focus should automatically land on the first interactive element within the dialog.

[IMAGE] A modal dialog titled “Permanently delete?” whose second interactive element is focused by selecting the Tab key.

_Initial focus lands on the first interactive element within a dialog. The tab key moves focus through the next interactive elements in a cycle._

[IMAGE] A modal dialog titled “Permanently delete?” whose previous interactive element is focused on by selecting both the Shift and Tab keys.

_The shift and tab keys together move focus in the opposite direction. The space or enter key triggers or commits the action of the focused element._

#### Keyboard navigation

| Keys | Actions
| Tab | Focus lands on the next interactive element contained in the dialog, or the first element if focus is currently on the last element
| Shift + Tab
| Focus lands on the previous interactive element contained in the dialog, or the last element if focus is currently on the first element
| Space or Enter
| Triggers or commits the action of the focused element
| Escape
| Closes the dialog

#### Labeling elements

The accessibility label for a dialog is typically the same as the dialog’s title or headline.

On web, basic dialogs should have the **alert dialog** role.

[IMAGE] An alert dialog with a title “Set up traffic updates?”  Its label is “Set up traffic updates?” and its role as “Alert Dialog.”

_Basic dialogs are known as alert dialogs on web_

Components contained within the dialog, such as buttons, should be labeled according to the guidelines specific to those components.

For common examples, see:
- Buttons
- Text fields

[IMAGE] A full-screen dialog titled “New event” containing a “Save” button and a text field, both with their own accessibility labels.

_Elements within a dialog should be labeled according to their guidelines_
