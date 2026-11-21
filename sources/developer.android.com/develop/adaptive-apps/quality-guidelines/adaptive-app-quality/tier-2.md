# Tier 2 — Adaptive optimized
Source: https://developer.android.com/develop/adaptive-apps/quality-guidelines/adaptive-app-quality/tier-2

Optimized apps fully support all screen types and device states, including state transitions.

[IMAGE] Depiction of the three quality tiers as layers stacked vertically with the middle tier highlighted.

## Guidelines

Build your app to adapt to all display sizes and device states.

### User interface
| Guideline ID  | Test IDs  | Description
| Responsive_adaptive_layouts  | T-Layout_Flow  |

App has responsive and adaptive layouts designed for all screen sizes. All layouts are responsive (see Migrate your UI to responsive layouts). Implementation of adaptive layouts is determined by window size classes.

The app UI can include the following:
- Leading‑edge navigation rails that expand on larger window sizes into full navigation panels
- Grid layouts that scale the number of columns to accommodate window size changes
- Columns of text on large screens
- Trailing‑edge panels that are open by default on desktop screen sizes; closed, on smaller screens

Create multi-pane layouts (where appropriate) to take advantage of large screen space. See Canonical layouts.

For Jetpack Compose apps, use adaptive layouts (such as `ListDetailPaneScaffold`) to build multi-pane UIs. For legacy activity-based apps, Activity embedding enables multi‑pane layouts by displaying activities side by side.
| UI_Secondary_Elements  | T-Layout_Flow  |

Modals, context menus, and other secondary elements are properly formatted on all screen types and device states, for example:
- Bottom sheets are not full width on large screens. (Apply a maximum width to avoid stretching.) See Responsive layout in Bottom sheets.
- Buttons are not full width on large screens. See Adaptive design in Buttons.
- Text fields and boxes don't stretch to full width on large screens. See Adaptive design in Text fields.
- Small edit menus or modals don't cover the entire screen and maintain context for the user as much as possible. See Menus.
- Context menus appear next to the item the user selected. See the "Context menus" topic in Menus.
- Navigation rails replace navigation bars for better ergonomics on large screens. See Navigation rail.
- Navigation drawers are updated to expanded navigation rails. See Navigation drawer.
- Dialog boxes are updated to the latest material component. See Dialogs.
- Images are displayed at a proper resolution and are not stretched or cropped.

| Touch_Targets  | T-Touch_Targets  | Touch targets are at least 48dp. See the Material Design Target sizes guidelines.
| Drawable_Focus  | T-Drawable_Focus  | A focused state is created for custom drawables that are interactive. A custom drawable is any visual UI element not provided by the Android framework. If users can interact with a custom drawable, the drawable must be focusable when the device is not in Touch Mode, and a visual indication of the focused state must be apparent.

### Keyboard, mouse, and trackpad
| Guideline ID  | Test IDs  | Description
| Keyboard_Navigation  | T-Keyboard_Navigation  | The main task flows in the app support keyboard navigation, including Tab and arrow key navigation. See Build more accessible apps.
| Keyboard_Shortcuts  | T-Keyboard_Shortcuts  | App supports keyboard shortcuts for commonly used actions such as select, cut, copy, paste, undo, and redo. See Keyboard, mouse, and trackpad.
| Keyboard_Media_Playback  | T-Keyboard_Media_Playback  | Keyboard can be used to control media playback; for example, the Spacebar plays and pauses media.
| Keyboard_Send  | T-Keyboard_Send  | Keyboard Enter key performs a _send_ function in communication apps.
| Keyboard_Exit  | T-Keyboard_Exit  | Keyboard Esc key performs an _exit_ function that terminates or undoes an action, for example:
- Closes modals, dialogs, pop-ups, and menus
- Clears search text or removes search focus
- Cancels keyboard focus
- Exits full-screen video, PiP, full-screen mode, or slideshows
- Dismisses on-screen controls, such as progress bars, or menus
- Cancels Up Next / autoplay timers
- Deselects all selected items
- Aborts renaming/editing without saving
- Exits cropping/transform tools and discards changes

| Context_Menus  | T-Context_Menus  | Context menus are accessible by typical mouse and trackpad right‑click (secondary mouse button or secondary tap) behavior.
| Content_Zoom  | T-Content_Zoom  | App content can be zoomed using the mouse scroll wheel (in conjunction with pressing the Control, or Ctrl, key) and trackpad pinch gestures.
| Hover_States  | T-Hover_States  | Actionable UI elements have hover states (where appropriate) to indicate to mouse and trackpad users that the elements are interactive.

## Tests

To ensure your app is optimized and responsive to all display configurations, perform the following tests.

### User interface
| Test ID  | Guideline IDs  | Description
| T-Layout_Flow  |  Responsive_adaptive_layouts,
UI_Secondary_Elements   |

Run the app on devices that have a wide variety of screen sizes, including phones, foldable phones, small and large tablets, and desktop devices. Run the app in multi-window mode on the devices.

Verify that the app layout responds and adapts to different screen and window sizes. Check whether the app expands and contracts navigation rails, scales the number of columns in grid layouts, flows text into columns, and so forth. Observe whether UI elements are formatted for both aesthetics and function.

For legacy apps using activity embedding, test whether activities are displayed side by side on large screens, stacked on small screens.
| T-Touch_Targets  | Touch_Targets  | Verify that touch targets maintain a consistent, accessible size and position and are not hidden or obscured by other UI elements for all display sizes and configurations. For information on accessibility, see the Accessibility Scanner.
| T-Drawable_Focus  | Drawable_Focus  | On each app screen that contains an interactive custom drawable, verify that the drawable can be focused using an external keyboard, D‑pad, or other device that enables UI elements to be focused. Verify that a visual indication of the focused state is apparent. For related information, see the Compose focus documentation.

### Keyboard, mouse, and trackpad
| Test ID  | Guideline IDs  | Description
| T-Keyboard_Navigation  | Keyboard_Navigation  | Navigate through the app's focusable components using the Tab and arrow keys of an external keyboard.
| T-Keyboard_Shortcuts  | Keyboard_Shortcuts  | Use keyboard shortcuts on an external keyboard to perform select, cut, copy, paste, undo, and redo actions.
| T-Keyboard_Media_Playback  | Keyboard_Media_Playback  | Use an external keyboard to start, stop, pause, rewind, and fast forward media playback.
| T-Keyboard_Send  | Keyboard_Send  | Use the Enter key of an external keyboard to send or submit data.
| T-Keyboard_Exit  | Keyboard_Exit  |

Use the Esc key of an external keyboard to perform an _exit_ function. Verify that the key does the following (where applicable):
- Closes modals, dialogs, pop-ups, and menus
- Clears search text or removes search focus
- Cancels keyboard focus
- Exits full-screen video, PiP, full-screen mode, or slideshows
- Dismisses on-screen controls
- Cancels Up Next / autoplay timers
- Deselects all selected items
- Aborts renaming/editing without saving
- Exits cropping/transform tools and discards changes

| T-Context_Menus  | Context_Menus  | Use the secondary mouse button or trackpad secondary tap capability to access the context menu of interactive elements.
| T-Content_Zoom  | Content_Zoom  | Use the mouse scroll wheel (in conjunction with the Control, or Ctrl, key) and trackpad pinch gestures to zoom content in and out.
| T-Hover_States  | Hover_States  | Hover the mouse or trackpad cursor over actionable UI elements to activate the element's hover state.
