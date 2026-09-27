# Canonical widget layouts
Source: https://developer.android.com/design/ui/mobile/guides/widgets/layouts

Craft effective widget layouts by first identifying your core content. Your layout dictates how information and interactive elements are organized within your widget. Android offers several prebuilt layouts for toolbars, text, list and grid-type widgets to streamline this process. **Note:** View detailed layout specs in our Figma Widget Canonical Builder, and find the code samples using Jetpack Glance in the Android Platform Samples GitHub repository.

## Text

Text layouts are ideal for displaying concise information. Enhance the visual appeal of your widget by optionally including an image alongside the text.

**Text only**

Ideal for titles, status updates, short descriptions, or any scenario where a single line of text effectively conveys the message.
**Text and image**

Include an image for added visual impact. For more information, see Breakpoints to learn how to adapt this layout for different screen sizes.

## Toolbars

Use toolbar layouts to provide users with quick access to frequently used tasks in your app, in a flexible layout that adapts across widget sizes.

**Search Toolbar**

A search toolbar layout is intentionally designed to draw focus to search as a primary action in the toolbar. Additional handy buttons can provide quick access to frequently used functions.

**Toolbar**

Toolbars present app branding followed by buttons for the most used tasks that are ideal for toggleable settings or task links. When resizing, less commonly used options can be hidden in favor of more common actions. Use Breakpoints to add a new minimum 48dp tappable button when there's room.

## Lists

Use list layouts to organize multiple items in a clear, scannable format. This is ideal for news headlines, to-do lists or messages. Organize content into a structured, scannable list. Choose between containerized or containerless presentation based on your content needs. **Note:** Lists on Android Auto widgets don't scroll to prevent driver distraction.

**Text and image list**

Scannable text and image lists are perfect for showcasing multiple content types, such as news headlines, playlists with album art, or messages.
**Checklist**

The checklist layout is perfect for displaying tasks, providing clear tap targets for users to quickly mark items as done.
**Action list**

Provide intuitive control grouping with action lists, where visual on/off states offer immediate feedback on item statuses.
**Full bleed image**

Ideal for showcasing immersive, full-bleed images on Android 17 and higher, this layout uses snap scrolling to ensure each child element perfectly aligns with the height of the widget container.

## Grid

Present images in a compact, flexible, visually rich grid with optional labels. Use columns and rows that adapt to different screen sizes.

**Image only**

Create visually impactful, scrollable image galleries using image-only grids. Rows and columns automatically adapt to various screen sizes for optimal presentation.
**Image and text**

You can also incorporate text labels and descriptions, enriching your image grid content with additional context and information.

## Code samples

The following table maps each canonical layout to its corresponding Jetpack Glance implementation in the Android Platform Samples GitHub repository.
| Canonical Layout  | Layout Category  | Sample Implementation File
| **Text only**  | Text  | LongTextAppWidget.kt
| **Text and image**  | Text  | TextWithImageAppWidget.kt
| **Search toolbar**  | Toolbars  | SearchToolBarAppWidget.kt
| **Toolbar** (Standard)  | Toolbars  | ToolBarAppWidget.kt
| **Text and image list**  | Lists  | ImageTextListAppWidget.kt
| **Checklist**  | Lists  | CheckListAppWidget.kt
| **Action list**  | Lists  | ActionListAppWidget.kt
| **Full bleed snap scroll**  | Lists  | FullBleedImageAppWidget.kt
| **Image only**  | Grid  | ImageGridAppWidget.kt
| **Image and text**  | Grid  | ImageGridAppWidget.kt
