# Style parts of text
Source: https://developer.android.com/develop/ui/compose/quick-guides/content/style-parts-text

You can style parts of text to improve readability, increase positive user experience, and encourage greater creativity through use of colors and fonts.

## Results
[IMAGE] Hello World text with multiple styles

**Figure 1.** A line of text with multiple styles.

## Version compatibility

This implementation requires that your project minSDK be set to API level 21 or higher.

### Dependencies

## Style parts of text

The following code displays the string "Hello World" using blue for the "H", red for the "W", and black for the rest of the text. To set different styles within a single `Text` composable, use the following code:

```
@Composable
fun MultipleStylesInText() {
Text(
buildAnnotatedString {
withStyle(style = SpanStyle(color = Color.Blue)) {
append("H")
}
append("ello ")

withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = Color.Red)) {
append("W")
}
append("orld")
}
)
}
```

### Key points about the code
- Uses `buildAnnotatedString` that returns an `AnnotatedString` string to set different styles within text.
- Styles part of text with `SpanStyle`, a configuration that allows character-level styling.

## Collections that contain this guide

This guide is part of these curated Quick Guide collections that cover broader Android development goals:

###### Display text
Text is a central piece of any UI. Find out different ways you can present text in your app to provide a delightful user experience.
Quick guide collection
