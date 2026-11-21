# Create a chip to represent complex entities
Source: https://developer.android.com/develop/ui/compose/quick-guides/content/create-chip

The `Chip` component is a compact, interactive UI element. It represents complex entities like a contact or tag, often with an icon and label. It can be checkable, dismissible, or clickable.

The five types of chips and where you might use them are as follows:
- Assist: Guides the user during a task. Often appears as a temporary UI element in response to user input.
- Filter: Lets users refine content from a set of options. They can be selected or deselected, and may include a checkmark icon when selected.
- Input: Represents user-provided information, such as selections in a menu. They can contain an icon and text, and provide an 'X' for removal.
- Suggestion: Provides recommendations to the user based on their recent activity or input. Typically appear beneath an input field to prompt user actions.
- Elevated: Has an elevated appearance instead of looking flat.

## Version compatibility

This implementation requires that your project minSDK be set to API level 21 or higher.

### Dependencies

## Create an assist chip

The `AssistChip` composable provides a straightforward way to create an assist chip that nudges the user in a particular direction. One distinguishing feature is its `leadingIcon` parameter that lets you display an icon on the left side of the chip, as shown in figure 1. The following example demonstrates how you can implement it:
[IMAGE] A simple assist chip.

**Figure 1.** Assist chip.

## Create a filter chip

The `FilterChip` composable requires you to track whether or not the chip is selected. The following example demonstrates how you can show a leading checked icon only when the user has selected the chip:

### Results
[IMAGE] An unselected filter chip, with no check and a plan background.

**Figure 2.** Unselected filter chip.
[IMAGE] Selected filter chip, with a check and a coloured background.

**Figure 3.** Selected filter chip.

## Create an input chip

You can use the `InputChip` composable to create chips that result from user interaction. For example, in an email client, when the user is writing an email, an input chip might represent a person whose address the user has entered into the "to:" field.

The following implementation demonstrates an input chip that is in a selected state. The user dismisses the chip when they press it. **Note:** Consider how you might use a chip like this in the preceding email use case, with a name passed in for the `text` parameter, and a function that performs the necessary network calls for `onDismiss`.

### Results
[IMAGE] An input chip with an avatar and a trailing icon.

**Figure 4.** Input chip.

## Create a suggestion chip

The `SuggestionChip` composable is the most basic of the composables listed on this page, both in its API definition and its common use cases. Suggestion chips present dynamically generated hints. For example, in an AI chat app, you might use suggestion chips to present possible responses to the most recent message.

Consider this implementation of `SuggestionChip`:

### Results
[IMAGE] A simple assist chip.

**Figure 5.** Assist chip.  **Note:** Although the suggestion chip component is intended for informational purposes, it does still take an `onClick` lambda that you can use to create interactivity.

## Create an elevated chip

All the examples in this document use the base composables that take a flat appearance. If you want a chip that has an elevated appearance, use one of the three following composables:
- `ElevatedAssistChip`
- `ElevatedFilterChip`
- `ElevatedSuggestionChip`

## Key points

Four composables correspond to the four types of chips, and they share the following parameters:
- **`label`**: The string that appears on the chip.
- **`icon`**: The icon displayed at the start of the chip. Some composables have a separate `leadingIcon` and `trailingIcon` parameter.
- **`onClick`**: The lambda that the chip calls when the user clicks it.

## Collections that contain this guide

This guide is part of these curated Quick Guide collections that cover broader Android development goals:

###### Display interactive components
Learn how composable functions can enable you to easily create beautiful UI components based on the Material Design design system.
Quick guide collection
