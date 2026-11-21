# About window insets
Source: https://developer.android.com/develop/ui/compose/system/insets

The Android platform is responsible for drawing the system UI, such as the status bar and navigation bar. This system UI is displayed regardless of which app the user is using.

`WindowInsets` provides information about the system UI to ensure that your app draws in the correct area and your UI isn't obscured by the system UI.
[IMAGE] Going edge-to-edge to draw behind the system bars

**Figure 1.** Going edge-to-edge to draw behind the system bars.

On Android 14 (API level 34) and lower, your app's UI does not draw underneath the system bars and display cutouts by default.

On Android 15 (API level 35) and higher, your app draws underneath the system bars and display cutouts once your app targets SDK 35. This results in a more seamless user experience and allows your app to take full advantage of the window space available to it.

Displaying content behind the system UI is called _going edge-to-edge_. On this page, you learn about the different types of insets, how to go edge-to-edge, and how to use the inset APIs to animate your UI and ensure your app's content isn't obscured by system UI elements. **Important:** Edge-to-edge is enforced on Android 15 and higher once your app targets SDK 35. If your app is not already edge-to-edge, portions of your app may be hidden and you must handle insets. Depending on the app, this work may or may not be significant. The Material 3 `Scaffold` component can reduce the work required to be compatible with the Android 15 edge-to-edge enforcement. See Create a scaffold component to hold the UI together.

## Inset fundamentals

When an app goes edge-to-edge, you need to ensure that important content and interactions are not obscured by the system UI. For example, if a button is placed behind the navigation bar, the user may not be able to click it.

The size of the system UI and information about where it is placed is specified using _insets_.

Each portion of the system UI has a corresponding type of inset that describes its size and where it is placed. For example, status bar insets provide the size and position of the status bar, whereas the navigation bar insets provide the size and position of the navigation bar. Each type of inset consists of four pixel dimensions: top, left, right, and bottom. These dimensions specify how far the system UI extends from the corresponding sides of the app's window. To avoid overlapping with that type of system UI, therefore, app UI must be inset by that amount.

These built-in Android inset types are available through `WindowInsets`:
|

`WindowInsets.statusBars`  |

The insets describing the status bars. These are the top system UI bars containing notification icons and other indicators.
|

`WindowInsets.statusBarsIgnoringVisibility`  |

The status bar insets for when they are visible. If the status bars are hidden (due to entering immersive full screen mode), then the main status bar insets will be empty, but these insets will be non-empty.
|

`WindowInsets.navigationBars`  |

The insets describing the navigation bars. These are the system UI bars on the left, right, or bottom side of the device, describing the taskbar or navigation icons. These can change at runtime based on the user's preferred navigation method and interacting with the taskbar.
|

`WindowInsets.navigationBarsIgnoringVisibility`  |

The navigation bar insets for when they are visible. If the navigation bars are hidden (due to entering immersive full screen mode), then the main navigation bar insets will be empty, but these insets will be non-empty.
|

`WindowInsets.captionBar`  |

The inset describing the system UI window decoration if in a freeform window, like top title bar.
|

`WindowInsets.captionBarIgnoringVisibility`  |

The caption bar insets for when they are visible. If the caption bars are hidden, then the main caption bar insets will be empty, but these insets will be non-empty.
|

`WindowInsets.systemBars`  |

The union of the system bar insets, which include the status bars, navigation bars, and caption bar.
|

`WindowInsets.systemBarsIgnoringVisibility`  |

The system bar insets for when they are visible. If the system bars are hidden (due to entering immersive full screen mode), then the main system bar insets will be empty, but these insets will be non-empty.
|

`WindowInsets.ime`  |

The insets describing the amount of space on the bottom that the software keyboard occupies.
|

`WindowInsets.imeAnimationSource`  |

The insets describing the amount of space that the software keyboard occupied _before_ the current keyboard animation.
|

`WindowInsets.imeAnimationTarget`  |

The insets describing the amount of space that the software keyboard will occupy _after_ the current keyboard animation.
|

`WindowInsets.tappableElement`  |

A type of insets describing more detailed information about the navigation UI, giving the amount of space where "taps" will be handled by the system, and not the app. For transparent navigation bars with gesture navigation, some app elements can be tappable through the system navigation UI.
|

`WindowInsets.tappableElementIgnoringVisibility`  |

The tappable element insets for when they are visible. If the tappable elements are hidden (due to entering immersive full screen mode), then the main tappable element insets will be empty, but these insets will be non-empty.
|

`WindowInsets.systemGestures`  |

The insets representing the amount of insets where the system will intercept gestures for navigation. Apps can manually specify handling a limited amount of these gestures using `Modifier.systemGestureExclusion`.
|

`WindowInsets.mandatorySystemGestures`  |

A subset of the system gestures that will always be handled by the system, and which can't be opted out using `Modifier.systemGestureExclusion`.
|

`WindowInsets.displayCutout`  |

The insets representing the amount of spacing needed to avoid overlapping with a display cutout (notch or pinhole).
|

`WindowInsets.waterfall`  |

The insets representing the curved areas of a waterfall display. A waterfall display has curved areas along the edges of the screen where the screen begins to wrap along the sides of the device.

These types are summarized by three "safe" inset types that ensure content isn't obscured:
- `WindowInsets.safeDrawing`
- `WindowInsets.safeGestures`
- `WindowInsets.safeContent`

These "safe" inset types protect content in different ways, based on the underlying platform insets:
- Use `WindowInsets.safeDrawing` to protect content that shouldn't be drawn underneath any system UI. This is the most common usage of insets: to prevent drawing content that is obscured by the system UI (either partially or completely).
- Use `WindowInsets.safeGestures` to protect content with gestures. This avoids system gestures clashing with app gestures (such as those for bottom sheets, carousels, or in games).
- Use `WindowInsets.safeContent` as a combination of `WindowInsets.safeDrawing` and `WindowInsets.safeGestures` to ensure content has no visual overlap and no gesture overlap.

## Recommended for you
- Note: link text is displayed when JavaScript is off
- Material Components and layouts
- Migrate `CoordinatorLayout` to Compose
- Other considerations

Previous  arrow_back   About WindowInsetsRulers

Next  Set up window insets   arrow_forward
