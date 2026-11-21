# Configure text layout
Source: https://developer.android.com/develop/ui/compose/text/configure-layout

This page describes how to configure your text layout with parameters like `maxLines` and `overflow`.

## Limit visible lines

To limit the number of visible lines in a `Text` composable, set the `maxLines` parameter:

```
@Composable
fun LongText() {
Text("hello ".repeat(50), maxLines = 2)
}
```

[IMAGE] A long text passage truncated after two lines

## Indicate text overflow

When limiting a long text, you may want to indicate a `TextOverflow`, which is only shown if the displayed text is truncated. To do so, set the `textOverflow` parameter:

```
@Composable
fun OverflowedText() {
Text("Hello Compose ".repeat(50), maxLines = 2, overflow = TextOverflow.Ellipsis)
}
```

[IMAGE] A long passage of text truncated after three lines, with an ellipsis at the end

## Recommended for you
- Note: link text is displayed when JavaScript is off
- Graphics in Compose
- Style paragraph
- Work with fonts
