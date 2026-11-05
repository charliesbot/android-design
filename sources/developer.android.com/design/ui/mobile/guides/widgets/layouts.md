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
| Canonical layout  | Reference files  | Guidance
| **Action List Layout**  |
- ActionListAppWidget.kt
- ActionListLayout.kt
- ListItem.kt
- RoundedScrollingLazyColumn.kt
- RoundedScrollingLazyVerticalGrid.kt
- EmptyListContent.kt
- NoDataContent.kt
- FakeActionListDataRepository.kt
- sample_action_list_widget_info.xml
- dimens.xml
- strings.xml
- sample_home_icon.xml
- sample_power_settings_icon.xml
- sample_bulb_icon.xml
- sample_thermostat_icon.xml
- sample_ac_icon.xml
- sample_door_icon.xml
- sample_arrow_right_icon.xml
- sample_no_data_icon.xml
- sample_add_icon.xml
|
- Handle list item actions with Glance action callbacks and ensure proper state updates.

| **Check List Layout**  |
- CheckListAppWidget.kt
- CheckListLayout.kt
- ListItem.kt
- RoundedScrollingLazyColumn.kt
- EmptyListContent.kt
- NoDataContent.kt
- FakeCheckListDataRepository.kt
- sample_check_list_widget_info.xml
- dimens.xml
- strings.xml
- sample_pin_icon.xml
- sample_checked_circle_icon.xml
- sample_circle_icon.xml
- sample_delete_icon.xml
- sample_edit_icon.xml
- sample_snooze_icon.xml
- sample_no_data_icon.xml
- sample_add_icon.xml
|
- Implement checkbox state toggling using action parameters or callbacks to update collection items.
- Use Glance action callbacks, such as `actionRunCallback`, `ActionCallback(actionParametersOf(...))`, or `actionStartActivity(...)`, for widget `onClick` parameters to handle click events like tapping a checkbox.

| **Expressive Toolbar Layout**  |
- ExpressiveToolbarAppWidget.kt
- ExpressiveToolbarLayout.kt
- sample_expressive_toolbar_widget_info.xml
- Buttons.kt
- LayoutComponents.kt
- dimens.xml
- strings.xml
- four_side_cookie_background.xml
- sample_edit_icon.xml
- sample_share_icon.xml
- sample_delete_icon.xml
- sample_info_icon.xml
- sample_add_icon.xml
- sample_file_upload_icon.xml
- sample_mic_icon.xml
- sample_camera_icon.xml
|
- Avoid `ColorProvider` import errors: import `androidx.glance.unit.ColorProvider` and use `androidx.glance.color.ColorProvider(dayColor, nightColor)`.
- Use `GlanceModifier.background(Color)` directly for solid background colors rather than `ColorProvider`.

| **Image Grid Layout**  |
- ImageGridAppWidget.kt
- ImageGridLayout.kt
- RoundedScrollingLazyVerticalGrid.kt
- EmptyListContent.kt
- NoDataContent.kt
- FakeImageGridDataRepository.kt
- sample_image_grid_widget_info.xml
- ImageUtils.kt
- dimens.xml
- strings.xml
- sample_placeholder_image.png
- sample_grid_icon.xml
- sample_refresh_icon.xml
- sample_no_data_icon.xml
- sample_add_icon.xml
|
- Use solid colors (`GlanceModifier.background`) or local drawables for image items unless an image loading library is configured or requested.

| **Image Text List Layout**  |
- ImageTextListAppWidget.kt
- ImageTextListLayout.kt
- ListItem.kt
- RoundedScrollingLazyColumn.kt
- EmptyListContent.kt
- NoDataContent.kt
- FakeImageTextListDataRepository.kt
- sample_image_text_list_widget_info.xml
- ImageUtils.kt
- dimens.xml
- strings.xml
- sample_placeholder_image.png
- sample_image_icon.xml
- sample_heart_icon.xml
- sample_refresh_icon.xml
- sample_no_data_icon.xml
- sample_add_icon.xml
|
- Common use cases: news feeds, article lists, workout histories, message feeds, and vertical thumbnail lists.
- Balance list row height and text clipping when combining thumbnail images with primary/secondary text.

| **Long Text Layout**  |
- LongTextAppWidget.kt
- LongTextLayout.kt
- FakeLongTextRepository.kt
- sample_long_text_widget_info.xml
- FontUtils.kt
- dimens.xml
- strings.xml
- sample_text_icon.xml
- sample_refresh_icon.xml
|
- If you multiply `Dp` values, such as `2 * padding`, import `androidx.compose.ui.unit.times`.
- Follow sizing rules: ensure dimensions from `dimens.xml` are used for padding and typography scaling.

| **Search Toolbar Layout**  |
- SearchToolBarAppWidget.kt
- SearchToolBarLayout.kt
- sample_search_toolbar_widget_info.xml
- Buttons.kt
- LayoutComponents.kt
- dimens.xml
- strings.xml
- sample_search_icon.xml
- sample_mic_icon.xml
- sample_camera_icon.xml
- sample_videocam_icon.xml
- sample_share_icon.xml
|
- Avoid `ColorProvider` import errors: import `androidx.glance.unit.ColorProvider` and use `androidx.glance.color.ColorProvider(dayColor, nightColor)`.
- Use `GlanceModifier.background(Color)` directly for solid background colors rather than ColorProvider.

| **Full-Bleed Image Layout / Snap Scrolling**  |
- FullBleedImageAppWidget.kt
- FullBleedImageLayout.kt
- sample_full_bleed_image_widget_info.xml
- FontUtils.kt
- dimens.xml
- strings.xml
- sample_scrim_gradient.xml
- sample_placeholder_image.png
- sample_app_logo.xml
- sample_info_icon.xml
- sample_no_data_icon.xml
|
- Set `verticalScrollMode = VerticalScrollMode.SnapScrollMatchHeight(size.height)` on LazyColumn for Snap Scrolling.
- If you implement snap scrolling, Glance 1.3.0-alpha02 or higher, compile SDK 37 (Baklava) or higher, and Android Gradle Plugin (AGP) 9.1.0 or higher are required.
- Set `override val sizeMode: SizeMode = SizeMode.Exact` in the widget provider class.

| **Standard Toolbar Layout**  |
- ToolBarAppWidget.kt
- ToolBarLayout.kt
- sample_toolbar_widget_info.xml
- Buttons.kt
- LayoutComponents.kt
- dimens.xml
- strings.xml
- sample_app_logo.xml
- sample_search_icon.xml
- sample_add_icon.xml
- sample_edit_icon.xml
- sample_share_icon.xml
- sample_mic_icon.xml
- sample_camera_icon.xml
- sample_videocam_icon.xml
|
- See Troubleshooting to avoid `ColorProvider` import errors: use `androidx.glance.color.ColorProvider(dayColor, nightColor)`.
- Use `GlanceModifier.background(Color)` directly for solid background colors rather than `ColorProvider`.

| **Text with Image Layout**  |
- TextWithImageAppWidget.kt
- TextWithImageLayout.kt
- FakeTextWithImageRepository.kt
- sample_text_with_image_widget_info.xml
- FontUtils.kt
- dimens.xml
- strings.xml
- sample_text_icon.xml
- sample_refresh_icon.xml
- sample_placeholder_image.png
- sample_info_icon.xml
- sample_no_data_icon.xml
|
- Follow responsive layout sizing rules to accommodate text and image side-by-side or stacked based on available widget dimensions.
