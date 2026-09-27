# Design for watches
Source: https://m3.material.io/foundations/watches

> Watches have special design considerations and interaction patterns

## Overview

#### Resources

|

Type |

Resource
|

Design |

About M3 Expressive
|

M3 Expressive on Wear OS
|

Figma Design Kits for Wear OS
|

Implementation |

Android Developers: Wear OS
|

Jetpack Compose for Wear OS

#### New in GM3 Expressive

Design expressive layouts for watches with harmonious shape, animation, color, and typography treatments. All examples shown here are based on Material for Wear OS. More on M3 Expressive on Wear OS

##### Designed for round screens

A new shape system with edge-hugging containers and buttons creates variety and visual balance on round screens.

More on shape system for Wear OS

[IMAGE] Watch screenshots using rounded buttons, progress bars, and page indicators.

_Controls for smartwatches can adapt to the form factor_

##### Expressive motion & springs

A new physics-based system makes interactions and transitions feel more alive, fluid, and natural.

More on M3 motion physics

[VIDEO] A settings screen scrolling through options.

_Use motion to make interactions intuitive_

##### Shape morphing

With shape morphing, controls respond to show interaction. Containers change corner radius.

More on M3 shape morphing

[VIDEO] A number pad with buttons morphing from rounded to squared as they are pressed.

_Selected buttons change shape to show interaction_

##### Rich color

Dynamic color and deep tonal palettes are applied in a system of color roles to create depth and variety.

More on color for Wear OS

[IMAGE] A media player on a smartwatch displaying a song title, artist, and playback controls.

_The color system includes three main colors and specific color roles to create depth and variety_

##### New type roles

Along with an updated and optimized type scale, new styles serve specific use cases for watches:
- Arc text for curved titles
- Numeral text for bigger, stylized text

More on typography for Wear OS

[IMAGE] 3 smartwatch screens showing a fitness tracker, a timer, and a calendar notification using Roboto Flex.

_The baseline type scale is optimized for round screens to keep text legible in a compact space_

## Foundations

#### Resources

|

Type |

Resource
|

Design |

Get Started with M3 Expressive
|

UI Design for Wear OS
|

Figma Design Kits for Wear OS
|

Implementation |

Android Developers: Wear OS
|

Jetpack Compose for Wear OS

#### Anatomy

##### The watch face

Watch faces display the time, as well as other information and can provide access to other functions through apps and tiles.

They can also include complications, self-contained details that can show contextual info like heart rate or progress.

Ongoing activities on the watch face show in-progress actions, like a stopwatch countdown or a workout timer.

[IMAGE] A watch face with hands showing analog time readout, and complications showing the date in a calendar, heart rate, water consumption, and weather.

_**Complications** are details on the watch face that can be customized for style or function_

[IMAGE] A watch face with an entry point for an ongoing exercise activity.

_**1. Ongoing activities**, like timers, media players, or workouts, can be accessed from the watch face_

##### Physical buttons

Wearable devices can have a variety input surfaces, which include physical buttons and controls.

[IMAGE] 3 watch faces with indications showing the locations of a rotating side button, system button, and multifuction button.

_- Rotating side buttons: Used for volume control, or to scroll through options or lists
- System buttons: Dedicated to OS functions like powering on and off, and cannot be customized
- Multifunction buttons: Used by apps for custom actions like starting and stopping a stopwatch_

#### Design principles
- Tailor layouts for different screen sizes with adaptive design
- Design for short interactions to conserve battery
- Focus on one or two tasks at a time rather than a full app experience
- Test designs in situations that involve movement to make sure the design is usable at a glance

[IMAGE DO] Calendar screen with date, time of dentist appointment, and more button.

**DO:** _At-a-glance views allow people to quickly see calendar events_

[IMAGE DON'T] 3 columns with times, dates, and appointment names cut off.

**DON'T:** _Don't create complex and detailed apps such as a calendar grid_

##### Always relevant

Watches are always with people. Consider how to update app content based on context, such as time, place, and activity.

[IMAGE] A map search for a garden’s location on a watch and on a phone.

_Navigation on a watch complements the experience on a phone_

##### Works offline

Design for slow connections and offline use, such as exercising and commuting.

[IMAGE] 2 watches: 1 with offline music downloads list. 1 with a dialog about no internet connection with dismiss and accept buttons.

_The network state can be communicated through:
- An offline icon
- A dialog_

#### Interaction patterns

##### Cross-device experiences

Watches are often dependent on connected phones for functionality or complex interactions. In some cases, a watch and a phone are used together to accomplish different parts of the same task.

Consider how experiences can be consistent and complement the strengths of each device.

More on multidevice development for Android

[IMAGE DO] A contact entry shown on a phone and a watch, with options to call or message.

**DO:** _Consider which actions are appropriate for each device_

##### Always on displays

Watches can have always on displays, which allow ambient content to be shown when the watch isn’t in use.

This are especially helpful for ongoing experiences like a timer or a workout that should remain in view. Because they remain on the screen for long time periods, consider limiting the number of pixels that are illuminated.

More on always-on apps and system ambient mode in Wear OS

[IMAGE] A watch face showing progress through a task and the current time.

_Limit the number of illuminated pixels for a display that's always on_

## Styles

#### Resources

|

Type |

Resource
|

Design |

M3 Expressive on Wear OS
|

Typography for Wear OS
|

Color for Wear OS
|

Motion for Wear OS
|

Implementation |

Android Developers: Wear OS
|

Jetpack Compose for Wear OS

#### Typography

GM3 Expressive adds two type styles specially designed for watches. More on the type scale for Wear OS

##### Numerals

Numeral text styles display numbers, usually only a few digits at a time. This text can take on more expressive properties at larger display sizes without the accommodations usually required by text that must be localized.

[IMAGE] A watch face with an indication for the numeral text style used to show the time.

_- Numeral Large_

##### Arc text

Arc text is specially designed for text following a curved path on a round screen, such as page titles, confirmation overlays, or a call to action. It optimizes character spacing for text displayed along a curve at the top or bottom of a round screen.

[IMAGE] A close-up of a watch face with arc text showing “check your phone” hugging the bottom bevel of a round screen.

_- Arc Large_

#### Color

Material for Wear OS provides a custom color system to create vibrant experiences and clear visual hierarchy.

##### Build from black

Watches are designed with a black background, instead of the tinted background that phones use.

[IMAGE] 2 watch screens showing sleeping app and a number picker.

_Watches use a black background to conserve battery_

##### Color roles

Since watches are used throughout the day, color tokens for Wear OS are specifically tailored for dark themes in low-light environments and light themes for daylight environments.

More on color roles for Wear OS

[IMAGE DO] 3 examples of “Accept” and “Decline” buttons that are legible.

**DO:** _Buttons with (2) **on primary** on (1) **primary** and (4) **on** **primary container** on (3) **primary container** stay legible as the contrast level changes_

[IMAGE DON'T] Buttons with (2) primary dim on (1) primary or (4) primary dim on (3) primary container become illegible as contrast levels shift

**DON'T:** _Buttons with (2) **primary dim** on (1) **primary** or (4) **primary dim** on (3) **primary container** become illegible as contrast levels shift_

##### Recommended color combinations for Wear OS

Below are some common color pairings that can help establish priority, function, and elevation.
- Use **primary dim** to highlight important elements and **tertiary** to provide standout feedback, such as tap responses
- When the main action isn't clear, use **tertiary** and **primary** for main actions and **secondary container** for complementary actions
- Use **secondary** and **primary container** to show two equally important options or containers, while maintaining contrast

[IMAGE] Three buttons, with the center one in Primary and side ones in Primary Dim.

_- Primary
- Primary dim_

[IMAGE] A number pad with most keys in Primary Dim and the pressed key in Tertiary.

_- Primary dim
- Tertiary_

[IMAGE] Call button in tertiary color, open button in primary color and open on phone button in secondary-container.

_- Tertiary
- Primary
- Secondary container_

[IMAGE] accept button in primary and delete button in primary container.

_- Primary
- Tertiary
- Primary container_

[IMAGE] Plus button in primary color, date button is in tertiary color and appointment summary is in primary container color.

_- Primary dim
- Tertiary dim_

[IMAGE] Bicycle button in primary, weight button in tertiary dim.

_- Primary
- Tertiary dim_

#### Motion and transitions

Expressive motion makes interactions feel more alive, fluid, and natural. Use transitions give intuitive feedback to show how an app works, such as standard patterns for changes in state or hierarchy.

More on shape and motion for Wear OS

[VIDEO] A settings screen scrolling through options.

_Use expressive motion to show items selected_

#### Haptics

Haptics are tactile effects used to grab a person’s attention for something important, or add emphasis to an interaction on the screen. You can use haptics to provide responsive feedback to scrolling and selecting items from a list, or controlling volume.

Use system-defined patterns and tokens (when available) to reinforce interaction expectations. Synchronize haptics with UI motion and sound to provide richer feedback.
- Use stronger haptics for key interactions, such as a payment confirmation
- Use subtler haptics for precision interactions, such as scrolling through a list

More on haptics for Wear OS

[IMAGE] 2 watch faces with lines indicating haptics for a payment being accepted and scrolling through notifications.

_Use stronger haptics for key interactions and subtler feedback for precision interactions_

## Layout

#### Resources

|

Type |

Resource
|

Design |

Wear OS common design layouts
|

Adaptive layout for Wear OS
|

Figma Design Kit for Wear OS Apps
|

Figma Design Kit for Wear OS Tiles
|

Implementation |

Android Developers: Wear OS

#### Layout principles

[IMAGE] A social networking scrolling screen showing Followers, sorting and search.

_**Prioritize content**

Place the most important information at the top of the screen._

[IMAGE] An audio interface showing headphones are connected with volume indicator and controls.

_**Limit choices**

Reduce the number of actions to prevent decision fatigue. Focus on critical tasks to help people get things done within seconds._

[IMAGE] A dialog confirming device access to call log and contacts.

_**Simplify navigation**

Use a clear, shallow hierarchy so people don't get lost in complex menus. Aim to display content and navigation inline._

#### Standard layouts

For scrolling and non-scrolling apps:
- The time is shown on most app screens
- Edge-hugging buttons are used for round screens
- Show an indicator if more content is available on a scrolling apps

Wear OS offers Figma Design Kits for standard layouts, with components, styles, and variables.

[IMAGE] A watch screen showing a social media app and a watch screen showing a timer with element indicators.

_- Time text
- Page title
- Scroll indicator
- Action button_

##### Non-scrolling layouts

Non-scrolling layouts are for focused tasks or single-screen interactions where all content fits within the display, such as:
- Media players
- Pickers and switchers
- Fitness tracking screens
- Confirmation dialogs

More on non-scrolling layouts for Wear OS

[IMAGE] A non-scrollable timer with current time 9:30, timer set for 00:25:52, and a start button.

_Use non-scrolling layouts for focused tasks like a timer_

##### Scrolling layouts

Scrolling layouts can show content that exceeds the screen height, such as lists or dialogs. These might include:
- Message threads
- Contact lists
- Menu options

More on scrolling layouts for Wear OS

[IMAGE] A scrollable layout with current time, search button, 1st follower and avatar on screen, and two followers off screen.

_Lists use scrolling layouts to show additional options_

##### Tiles

Tiles (or widgets) are designed for glanceability. Use them to show timely updates or to help people perform frequent tasks quickly, such as checking progress towards a goal or viewing the weather.

Tiles are accessible with a swipe from the watch face. They have a fixed screen height and don't scroll.

More on tiles for Wear OS

[IMAGE] Icon buttons for meditation, running, cycling, and a More button

_Use tiles for quick access to to a few key options_

##### Notifications

Notifications can be expanded to offer more interactions, such as replying to a message, opening a location on a map, or playing a song.

Wear OS provides notification templates for instant messaging and calendar events.

More on notifications for Wear OS

[IMAGE] A message notification shown in a drawer with dimmed notifications above and below.

_Notifications should offer easy access to more interactions_

##### Adaptive layout

Adaptive design allows apps to adapt to different screen sizes and device contexts. On Wear OS, this means apps scale and reorganize to maximize the available space on both small and large round displays. The Material 3 Compose component library has built-in adaptive behavior.
- Design for small screens first: Start by designing for the smallest common screen size
- Use percentages: Define margins and padding using percentages rather than fixed pixel values. This prevents clipping and ensures content remains centered and proportional as the screen size increases.
- Add value on larger screens: Use the extra space on screens larger to show more content, such as additional buttons, text lines, or data visualizations
- Test all font sizes: Font scaling and accessibility settings such as bold text may cause changes in the size of UI elements

More on adaptive layout for Wear OS

[IMAGE] 5, 10, and 15 minute alarm buttons plus an edge-hugging more button.

_Design for small screens first, starting with a 192dp size watch_

[IMAGE] 5, 10, 15, 20 &amp; 30 minute alarm buttons plus an edge-hugging more button.

_Show more content on devices that are larger than 225dp_
