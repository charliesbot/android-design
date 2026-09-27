# Progress indicators
Source: https://m3.material.io/components/progress-indicators

## Overview

- Two variants: linear and circular
- Use the same configuration for all instances of a process (like loading)
- They capture attention through motion
- Option to apply a wave to the active track for use cases that would benefit from increased expressiveness

[IMAGE] 8 progress indicators configured to show different thickness and shape.

_Linear and circular progress indicators have visual configurations for shape and thickness_

#### Availability & resources

#### M3 Expressive update

**Aug 2024**

The progress indicators have configurations for height and wavy shape. Choose the visual style that best fits your product. More on M3 Expressive

- Track height: Configurable
- Shape: Wavy

[VIDEO] Progress indicators used when loading a page and for processing a payment.

_Progress indicators have a new rounded, colorful style, and more configurations to choose from, including a wavy shape and variable track height_

#### Previous updates

**Dec 2023: Non-text contrast (NTC)**
- Anatomy: Added an end stop indicator to improve accessibility
- Contrast: Higher contrast between track and active indicator to enhance the perception of progress
- Motion: New motion behavior
- Shape: Rounded corners

[IMAGE] GM3 linear and circular progress indicators

_Progress indicators have a new rounded, colorful style_

#### Differences from M2

**July 2022: Added to Material 3**
- **Color:** New color mappings and compatibility with dynamic color

[IMAGE] M2 linear and circular progress indicators.

_M2: Progress indicators have a boxier, neutral style_

[IMAGE] M3 linear and circular progress indicators.

_M3: Progress indicators are compatible with dynamic color_

## Specs

#### Variants

[IMAGE] 2 variant of progress indicators.

_- Linear progress indicator
- Circular progress indicator_

|

Variant |

M3 |

M3 Expressive
|

Linear progress indicator |

Available |

Available
|

Circular progress indicator |

Available |

Available

#### Configurations

[IMAGE] 4 configurations of the linear determinate progress indicator.

_- Behavior: Determinate and indeterminate
- Thickness: Default (4dp) and variable
- Shape: Flat and wavy_

|

Category |

Configuration |

M3 |

M3 Expressive
|

Behavior |

Determinate (default), Indeterminate |

Available |

Available
|

Track thickness |

Fixed (4dp)  |

Available |

Available
|

Configurable |

-- |

Available
|

Shape |

Flat (default) |

Available |

Available
|

Wavy |

-- |

Available

#### Tokens & specs

Browse the component elements, attributes, tokens, and their values. View baseline tokens

#### Anatomy

[IMAGE] 3 elements of a progress indicator.

_- Active indicator
- Track
- Stop indicator_

#### Color

[IMAGE] 2 color roles of a linear progress indicator in light and dark themes: the active indicator and stop indicator are primary and the track is secondary container.

_Progress indicator color roles used for light and dark schemes:
- Primary
- Secondary container_

#### Measurements

Wavy indicators use **amplitude** and **wavelength** to determine the shape of the wave. The height is the overall container height.

[IMAGE] Definitions of wave measurements for height and amplitude.

_**Amplitude** measures from the center of the resting position to the center of the peak_

[IMAGE] Definitions of wave measurements for wavelength.

_**Wavelength** measures the distance between two adjacent peaks_

[IMAGE] Linear progress indicator measurements.

_Size measurements for linear progress indicators. The thicker variants are provided as sample measurement for makers to adjust the default version based on their use cases._

[IMAGE] Circular progress indicator measurements.

_Size measurements for circular progress indicators. The thicker variants are provided as sample measurement for makers to adjust the default version based on their use cases._

[IMAGE] 4dp padding on the left and right of the linear progress indicator.

_The linear progress indicator is inset from the edge of the screen by 4dp_

#### Baseline tokens

The circular and linear progress indicator had separate token sets. These are no longer recommended.

## Guidelines

[VIDEO] Primary colored horizontal line fills a contrasting track from left to right and reaches the end when the app is loaded.

_Progress indicators communicate the status of an ongoing process_

#### Usage

Use progress indicators to show the status of ongoing processes, such as loading an app, submitting a form, or saving updates.

When multiple items are loading, use a single progress indicator to show progress for the group. Don’t add progress indicators to every activity.

[VIDEO DO] One progress indicator showing the loading progress for a page of podcasts.

**DO:** _Indicate overall progress of a group of items_

[VIDEO DON'T] Two progress indicators showing the loading progress of two podcasts.

**DON'T:** _Don’t show the progress of each activity in a group_

Choose a loading or progress indicator that corresponds to the expected wait time and kind of process.

If the wait is very long, consider allowing people to navigate away from the page while the process finishes up.

| **Expected wait time**
| **Recommendation**
| Instant (under 200ms) | No indicator
| Short (between 200ms and 5s) | Loading indicator
| Long (Over 5s) | Progress indicator

[VIDEO] Screen without latency without progress indicator.

_**Instant (under 200ms):** Display the content immediately_

[VIDEO] Screen with slight lag with loading indicator.

_**Short (between 200ms and 5s):** Use a loading indicator_

[VIDEO] Screen with long lag and progress indicator.

_**Long (over 5s): **Use a progress indicator_

There are two variants of progress indicators:
- Linear
- Circular

**Linear** indicators are best when placed on the edge of a container.

**Circular** indicators are best when centered in an element.

A process should be represented by the same variant of progress indicator throughout the product. For example, if refreshing uses a circular indicator in one place, it should use circular indicators everywhere.

[IMAGE] 1. A primary colored horizontal line fills a contrasting track from left to right. 2. A circle appears from 0 to 360 degrees.

_- Linear indicator
- Circular indicator_

Progress indicators behave differently based on the time of progress being tracked:
- **Determinate**: Known progress and wait time
- **Indeterminate**: Unknown progress and wait time

When using a **determinate** indicator, the indicator must accurately represent the progress of what it's measuring.

Use **indeterminate** indicators to show that a process is happening, but the wait time is unknown.

[VIDEO] Linear and circular progress indicators show both determinate and indeterminate progress.

_- Determinate progress indicators fill from 0% to 100%
- Indeterminate progress indicators move along a fixed track, growing and shrinking in size_

As more information about a process becomes available, a progress indicator should change from **indeterminate** to **determinate**.

[VIDEO] On a track, a contrasting primary-colored horizontal line grows and shrinks, then fills it from left to right as app loads.

_A linear progress indicator changes from indeterminate to determinate while loading a screen_

#### Anatomy

[VIDEO] 1. The track is a horizontal line. 2. A primary colored line over the contrasting colored track is the progress indicator.

_- Active indicator
- Track
- Stop indicator_

##### Active indicator

The active indicator shows the progress that has been made so far.

In indeterminate processes, it grows and shrinks along the track repeatedly.

[VIDEO] A primary colored horizontal line fills a contrasting line from left to right.

_Linear indicators animate from the leading to the trailing edge along the track. Circular indicators animate from the top of the track, clockwise by default._

The active indicator appears as soon as progress begins. At low percentages where space is limited, this should appear as a dot to help people understand that there’s progress underway.

[IMAGE] A linear and circular progress indicator at 1% progress, where the active indicator has only just appeared.

_When progress first begins, the active indicator appears as a dot_

The active indicator has two shape options: **flat** and **wavy**. Use the shape that best fits the product’s tone.

The wavy shape can make longer processes feel less static and is best used when a more expressive style is appropriate.

When using the wavy shape, the overall height of the component changes. At very small sizes, the wavy shape may not be as visible.

[VIDEO] Examples of flat and wavy active indicator.

_Wavy linear indicators increase the height of the overall container_

##### Stop indicator

The stop indicator is a 4dp circle that marks the end of a linear determinate progress indicator to meet Material's accessibility standards.

It's not used for indeterminate or circular progress indicators.

The stop indicator is required if the track has a contrast below 3:1 with its container or the surface behind the container.

[IMAGE DO] A primary colored horizontal  line fills a contrasting line from left to right.

**DO:** _Use a stop indicator when placing the progress indicator inside a container with low contrast_

[IMAGE CAUTION] An invisible circular track fills with color from 0 to 360 degrees.

**CAUTION:** _Only remove the end stop indicator if there's a visual contrast of at least 3:1 with surrounding surfaces_

#### Placement

Place a linear progress indicator along the edge of a container that’s loading. If the container changes shape, place it on the edge that animates. It can also be placed in the middle of a container.

Use a single progress indicator at the top of a page to show progress of the whole group. Don’t add one for every element unless they’re activated independently.

[VIDEO] Progress indicator at top of screen, indicating page content is loading.

_When at the top of a screen, a progress indicator shows that all of the page content is loading_

[VIDEO] Progress indicator on a card, indicating that the card's content is loading.

_When attached to a card, a progress indicator shows that just the card content is loading_

[VIDEO] Linear indicator shows the loading progress of a selected news article in a list.

_A progress indicator on the expanding edge of a card shows that the edge may expand to show the loaded content_

Circular progress indicators should be centered directly on the container or page that's loading, such as a button or card.

When loading more items on a page, place the circular progress indicator in the empty space where the new content will appear, not overlapping existing content.

However, if the content does not take long to load, consider using a loading indicator instead.

[VIDEO] As content loads, the screen is blank except for a circular progress indicator.

_A circular progress indicator can show that the page is loading_

[VIDEO] As container content loads, the container is blank except for a circular progress indicator.

_A circular progress indicator can show where new items will appear on a page. A loading indicator also works well in this space._

##### Progress indicators in buttons

A circular indicator can be placed in a button to show that the button’s action is currently in progress.

In very small buttons, use the flat shape since the wavy shape is not as visible at that size.

To ensure a minimum 3:1 contrast ratio, change the active indicator color to be the same color as the button’s icon or label text, and remove the track.

[VIDEO DO] Determinate circular indicator active for 2 seconds after download button is selected, reflecting download progress.

**DO:** _Use circular indicators for short, indeterminate activities under 5 seconds_

[VIDEO DON'T] Shows idea of indeterminate circular indicators active on multiple buttons as  cluttered and confusing.

**DON'T:** _Avoid applying progress indicators to every button in a list_

#### Responsive layout

##### Right-to-left languages

Linear progress indicators should be mirrored horizontally for products using right-to-left (RTL) languages.

Circular progress indicators don’t need to be mirrored.

[IMAGE] Mirrored right-to-left progress indicator.

_Linear progress indicators can flow from right to left in right-to-left (RTL) languages_

##### Large screens

Circular progress indicators have flexible sizes. They can range from 24dp to 240dp, depending on the placement and the breakpoint. Avoid exceeding the minimum and maximum sizes.

Reserve very large progress indicators for large and extra-large windows, such as desktop.

[IMAGE] Circular progress indicators can range in size from 24dps to 240dps.

_The waveform should scale with the size so the proportions look the same across sizes_

Linear progress indicators dynamically adjust to fit the width of the window or element they’re placed within, such as a card. They shouldn’t be used in any elements smaller than 40dp.

The padding on each end should be 4dp minimum, but can be modified.

[IMAGE] Linear progress indicators can dynamically adjust to any width.

_The linear progress indicator should always span the width of the UI element it’s placed within_

## Accessibility

#### Use cases

People should be able to do the following using the assistive technology:
- Navigate to the progress indicator
- Understand what progress the indicator is communicating

#### Interaction & style

The active indicator, which displays progress, provides visual contrast of at least 3:1 against most background colors.

[IMAGE] Dark line of progress indicator stands out against the lighter colored track.

_The progress indicator and stop indicator provide visual contrast of at least 3:1 against most background colors_

When integrated into another component, such as a button, make sure that the active indicator provides visual contrast of at least 3:1 against the other component.

For the active indicator, use the same color as the label text or icon. The track should be removed.

[IMAGE DO] Circular indicator on button passes 3 to 1 contrast test.

**DO:** _Ensure the indicator’s color provides at least 3:1 contrast against the surface it's on_

[IMAGE DON'T] Circular indicator on button fails 3 to 1 contrast test.

**DON'T:** _Avoid using a color below 3:1 contrast_

For linear progress indicators, the stop indicator is required if the track has a contrast below 3:1 with its container or the surface behind the container.

Essentially, the end of the track must be easy to identify.

[IMAGE DO] Bright container holding the progress bar is on a dark surface, passing the 3:1 color contrast.

**DO:** _Only remove the stop indicator when the linear progress indicator has at least a 3:1 color contrast with surrounding containers and surfaces_

[IMAGE DON'T] Bright container holding progress indicator is on a bright surface, failing the 3:1 color contrast.

**DON'T:** _Avoid removing the stop indicator if any adjacent containers or surfaces are below the 3:1 color contrast_

#### Labeling elements

Since the progress indicator is a visual cue, it needs an accessibility label to describe the kind and amount of progress made.

Use the **progress bar **accessibility role, and write an accessibility label that describes the purpose of the progress indicator. The label should include the process, such as "loading,” and the affected content, such as a page, article, or episode. For example: "Loading news article" or "Refreshing page."

[IMAGE] Determinate linear progress indicator has an accessibility label of “loading news article” and role of “progressbar”.

_Progress indicator labels should explain which items are loading_

[IMAGE] Indeterminate linear progress indicator has an accessibility label of “loading my episodes” and role of “progressbar.”

_A label on an intedeterminate progress indicator on a screen which is loading a set of podcast episodes_
