# Layout basics
Source: https://developer.android.com/design/ui/mobile/guides/layout-and-content/layout-basics

[IMAGE] Hero layout basics illustration

A layout defines the visual structure for a user to interface with your app, such as in a composable. Android provides a range of libraries, canonical starting points, and techniques to display and position content.

## Get Started

Start designing Android layouts by learning app anatomy then how to structure your app's content.

## Takeaways

**Layout orientation**

Consider different aspect ratios, size classes, and resolutions that users might encounter. Verify that your app provides a good user experience on both landscape and portrait orientation as well as different screen sizes and form factors.

For more information, see the guidance on adapting your layout and canonical layouts.
**Device safe areas**

Honor device safe areas, which includes parts of the UI such as display cutouts, edge-to-edge insets, edge displays, software keyboards, and system bars. Provide a flexible layout for users to interact with the keyboard.     Alas, your browser doesn't support HTML5 video. That's OK! You can still download the video and watch it with a video player.

check_circle

###### Do
Focus user inputs. If the keyboard is present, move the input up into a focused state or consider attaching the text input to the keyboard.

cancel

###### Don't
Hide inputs. Even on smaller screens, the user might not know or be able to scroll the screen.

**Interaction ergonomics**

Keep essential interactions, like primary navigation, in a reachable screen area. Floating action buttons (FABs) provide a prominent and reachable interaction point
**Containment groups**

Use containment to group related content to guide the user through content and actions. Cards using explicit containment to group content with related actions.

**Alignment**

Provide consistent alignment between similar content and UI elements.

check_circle

###### Do
Establish consistent spacing between like elements.

cancel

###### Don't
Disrupt readability by inconsistently spacing like elements, which can make designs appear haphazard.

**Essential interactions**

Don't overwhelm your user with too many actions per view.
**Notate layout specs**

When building custom layouts, notate how content should sit within the layout using alignment, constraints, or gravity terms. Include how images should respond to their container to display properly.
