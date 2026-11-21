# Display text
Source: https://developer.android.com/develop/ui/compose/text/display-text

The most basic way to display text is to use the `Text` composable with a `String` as an argument:

```
@Composable
fun SimpleText() {
Text("Hello World")
}
```

[IMAGE] The words

## Display text from resource

We recommend you use string resources instead of hardcoding `Text` values, as you can share the same strings with your Android Views as well as preparing your app for internationalization:

```
@Composable
fun StringResourceText() {
Text(stringResource(R.string.hello_world))
}
```

## Recommended for you
- Note: link text is displayed when JavaScript is off
- Enable user interactions
- Thinking in Compose
- Display emoji
