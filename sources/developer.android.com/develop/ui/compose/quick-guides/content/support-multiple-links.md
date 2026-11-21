# Support multiple links in a single string of text
Source: https://developer.android.com/develop/ui/compose/quick-guides/content/support-multiple-links

You can support multiple links in a single string of text to perform different actions when clicking a subsection of text.

## Results
[IMAGE] One text string containing two different links

**Figure 1.** A screenshot of one text string containing two different links.

## Version compatibility

This implementation requires that your project minSDK be set to API level 21 or higher.

### Dependencies

## Display multiple links in a single string

This snippet embeds multiple clickable links into a single string of text:

```
@Composable
fun AnnotatedStringWithLinkSample() {
// Display multiple links in the text
Text(
buildAnnotatedString {
append("Go to the ")
withLink(
LinkAnnotation.Url(
"https://developer.android.com/",
TextLinkStyles(style = SpanStyle(color = Color.Blue))
)
) {
append("Android Developers ")
}
append("website, and check out the")
withLink(
LinkAnnotation.Url(
"https://developer.android.com/jetpack/compose",
TextLinkStyles(style = SpanStyle(color = Color.Green))
)
) {
append("Compose guidance")
}
append(".")
}
)
}
```

### Key points about the code
- Uses the `buildAnnotatedString` function to create an annotated string of text.
- Specifies the link and text styling by passing them as arguments of the `LinkAnnotation.Url()` function (itself passed as an argument of the `withLink()` function). A click listener is built into `LinkAnnotation.Url()`.
- Adds text using `append()` in the body of the `withLink` function.
- Repeats this process to add another linked text segment.

## Collections that contain this guide

This guide is part of these curated Quick Guide collections that cover broader Android development goals:

###### Display text
Text is a central piece of any UI. Find out different ways you can present text in your app to provide a delightful user experience.
Quick guide collection
