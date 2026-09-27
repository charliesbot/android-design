# Loading indicator
Source: https://m3.material.io/components/loading-indicator

## Overview

- Recommended as a replacement for indeterminate circular progress indicators
- Always reflect an ongoing process and are never simply decorative
- Used for pull-to-refresh interactions
- Not used for processes that transition from indeterminate to determinate
- Capture attention through motion

[VIDEO] A looping sequence of morphs between 7 different shapes in a darker color, sitting on a circular background container in lighter color.

_- Loading indicator

- Contained loading indicator_

#### Availability & resources

#### M3 Expressive update

**May 2025**

The loading indicator is designed to show progress that loads in under five seconds. It should replace most uses of the indeterminate circular progress indicator. More on M3 Expressive

New component added to catalog.

Loading indicators:

- Are used in pull-to-refresh functionality

- Can be contained or uncontained

- Use shape and motion to capture attention

- Can scale in size

[VIDEO] Loading indicator in news app with pull-to-refresh.

_Loading indicators are used in the pull-to-refresh behavior_

## Specs

#### Variants

[IMAGE] 2 elements of a loading indicator.

_- Loading indicator_

|

Variant
|

M3
|

M3 Expressive

|

Loading indicator
|

--
|

Available

#### Configurations

[IMAGE] 2 configurations of loading indicators.

_- Default

- Contained_

|

Category
|

Configuration
|

M3
|

M3 Expressive

|

Containment
|

Default
|

--
|

Available

|

Contained
|

--
|

Available

#### Tokens & specs

Loading indicators have a single token set.

#### Anatomy

[IMAGE] 2 elements of a loading indicator.

_- Active indicator

- Container_

#### Color

##### Default

Color values are implemented through design tokens. For designers, this means working with color values that correspond with tokens; in implementation, a color value will be a token that references a value.

[IMAGE] 2 color roles of a loading progress indicator in light and dark themes.: the active indicator is primary and the container is secondary container.

_Loading indicator color roles used for light and dark schemes:

- Primary_

##### Contained

[IMAGE] 2 color roles of a loading progress indicator in light and dark themes. The active indicator is primary and the container is secondary container.

_Contained loading indicator color roles used for light and dark schemes:

- On primary container

- Primary container_

#### Measurements

[IMAGE] Loading progress indicator measurements.

_To ensure sufficient margins, the size is 48dp while the shape container is 38dp_

## Guidelines

[VIDEO] Loading indicator on media player.

_Loading indicators are best for indicating a short, indeterminate wait time_

#### Usage

Loading indicators use animation to grab attention, mitigate perceived latency, and indicate that an activity is in progress.

They should be used when progress isn’t detectable, or when it’s not necessary to indicate how long an activity will take.

While similar in function to circular progress indicators, loading indicators are a better alternative for short processes between 200ms and 5s.

[VIDEO] Loading indicator in loading state with “Getting your device ready...”.

_Use a loading indicator when a background process is running_

Choose a loading or progress indicator that corresponds to the expected wait time and type of process.

If the wait is very long, consider allowing users to navigate away from the page while the process finishes up.

| Expected wait time
| Recommendation

| Instant (under 200ms) | No indicator
| Short (between 200ms and 5s) | Loading indicator
| Long (Over 5s) | Progress indicator

[VIDEO] Content appearing on a screen quickly without a loading indicator.

_**Instant (under 200ms):** Display the content immediately_

[VIDEO] Content appearing on a screen with a slight lag and a loading indicator.

_**Short (between 200ms and 5s):** Use a loading indicator_

[VIDEO] Content appearing on a screen with a long lag and a progress indicator.

_**Long (over 5s):** Use a progress indicator_

When a process can transition from indeterminate (unknown progress) to determinate (known remaining progress), transition between the corresponding progress indicators.

Don’t transition a loading indicator into a progress indicator.

[VIDEO DO] A screen transitioning from an indeterminate progress indicator to a determinate progress indicator.

**DO:** _Transition from an indeterminate progress indicator to a determinate progress indicator_

[VIDEO DON'T] A screen transitioning from a loading indicator to a determinate progress indicator.

**DON'T:** _Avoid transitioning from a loading indicator to a determinate progress indicator_

#### Anatomy

[IMAGE] 2 parts of an active indicator.

_- Active indicator

- Container (optional)_

##### Active indicator

The active indicator is a looping shape morph sequence composed of seven unique Material 3 shapes.

More about the Material shape library

[IMAGE] Active indicator with shape morph.

_The active indicator morphs shape to capture attention_

##### Container (optional)

When the container is visible, the active indicator should change color from **primary** to **on-primary-container**.

The container should be visible when the loading indicator is placed over other content. This helps it stand out better by giving it a stronger contrast. It’s not needed when the loading indicator is placed directly on a surface.

The container should be used with pull-to-refresh behavior.

[IMAGE] Active indicator with container.

_The container is a circle that provides extra contrast from body content_

#### Placement

While loading a page or container, the loading indicator should be centered on the element.

[VIDEO] Active indicator with shape morph centered on a screen.

_Center the loading indicator in the middle of the page or container_

When loading more items on a page with existing content, place the loading indicator in the empty space where the new content will appear. Avoid overlapping existing content.

[VIDEO] Loading indicator with shape morph underneath photos in a photo app.

_Center the loading indicator in the empty space where content will appear_

Loading indicators can be placed within other components, such as buttons, to indicate that the action is ongoing, such as validating a form or checking for updates.

[VIDEO] Loading indicator in a button.

_Loading indicators can be placed in buttons that take a few seconds to take effect_

[VIDEO] Loading indicator as the icon in a tab.

_Use loading indicators to show progress without taking up much space_

#### Responsive layout

Loading indicators default to 48dp, but the size is flexible. It should be between 24dp to 240dp, depending on the placement and the breakpoint. Avoid exceeding the minimum and maximum sizes.

The ratio between the container and the active indicator stays the same when resizing the loading indicator.

Reserve very large progress indicators for large and extra-large windows, like desktop.

[IMAGE] Loading indicators can range from 24dps to 240dps.

_Loading indicators can range in size from 24–240dp_

##### Larger windows

As the pane or window size grows, consider scaling the loading indicator as well, so it remains proportional in size to the empty space around it. The loading indicator shouldn’t exceed 240dp.

[VIDEO] Loading indicator on mobile and tablet screens.

_The loading indicator’s default size is ideal for mobile and other compact windows. The loading indicator should scale up in larger windows._

#### Behavior

##### Pull-to-refresh

The loading indicator is used in pull-to-refresh on Jetpack Compose only.

Pull-to-refresh is an Android system feature that manually refreshes screen content with an action or gesture. It’s used at the beginning of lists, grid lists, and card collections where the most recent content appears.

It’s best to use pull-to-refresh with dynamic content that can have frequent updates, where people have a high chance of seeing new content after refreshing.

[VIDEO] A loading indicator can appear above content in pull-to-refresh.

_The loading indicator for pull-to-refresh can appear on top of the content or adjacent to it_

##### Threshold requirements

To ensure intentional usage of the pull-to-refresh gesture, the loading indicator must pass a threshold before the app will refresh.

[VIDEO] Loading indicator with screen refresh after threshold time has passed.

_After passing the threshold, completing the gesture initiates a refresh_

[VIDEO] Loading indicator reversed can cancel refresh action.

_Reversing the gesture past the threshold will cancel the refresh action_

The loading indicator remains visible until the refresh activity completes and any new content is visible, or someone navigates away from the refreshing content.

[VIDEO DO] A loading indicator at the top of a screen, which appears until content loads.

**DO:** _Keep the loading indicator in view until the activity is completed to provide status of the refresh activity_

[VIDEO DON'T] A loading indicator that scrolls off screen.

**DON'T:** _Don’t scroll the loading indicator off-screen, as it hides the status of the refresh activity. It could imply that the refresh activity is associated with a specific component, such as a card, instead of the entire screen._

## Accessibility

#### Use cases

People should be able to do the following with assistive technology:
- Navigate to the loading indicator
- Understand what progress the indicator is communicating
- Initiate a content refresh without relying on a gesture

#### Interaction & style

The active indicator, which displays progress, provides visual contrast of at least 3:1 against most container and surface colors.

The indicator itself must have 3:1 contrast with the background, but the container does not.

[IMAGE] Loading indicator with 3:1 color contrast.

_The loading indicator provides visual contrast of at least 3:1 against most background colors_

When integrated into another component, such as a button, make sure that the active indicator provides a visual contrast of at least 3:1 against the other component.

[IMAGE DO] Loading indicator with correct color contrast.

**DO:** _Ensure at least 3:1 contrast between the indicator and the surface it's on_

[IMAGE DON'T] Loading indicator with incorrect color contrast.

**DON'T:** _Avoid using when the contrast is under 3:1_

Pull-to-refresh interactions can’t be accessible by just swiping. Provide an alternate way to refresh the content with a single pointer, such as placing a refresh button in a menu or directly alongside the content.

[VIDEO] Refreshing content initiated by an app bar action.

_The refresh action can be in an app bar_

#### Labeling elements

Since the loading indicator is a visual cue, it needs an accessibility label to assist people who can't rely on visuals.

It should use the **progress bar** accessibility role. Write a label describing the purpose of the loading indicator, such as **loading news article** or **refreshing page**.

[IMAGE] Loading indicator accessibility label and role.

_Loading indicator labels should explain which items are loading_
