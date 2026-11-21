# Display emoji
Source: https://developer.android.com/develop/ui/compose/text/emoji

The standard set of emoji is refreshed annually by Unicode, as emoji usage is increasing rapidly for all types of apps.

If your app displays internet content or provides text input, _we strongly recommend supporting the latest emoji fonts._ Otherwise, later emoji might be displayed as a small square box called _tofu_ (☐) or other incorrectly rendered emoji sequences.

Android versions 11 (API level 30) and lower can't update the emoji font, so apps that display them on those versions must be updated manually.

The following are examples of modern emoji.
| Examples   | Version
| 🫩 🪉 🇨🇶   | 16.0 (September 2024)
| 🐦‍🔥 🧑‍🧑‍🧒‍🧒 👩🏽‍🦽‍➡️ 🇲🇶   | 15.1 (September 2023)
| 🩷 🫸🏼 🐦‍⬛   | 15.0 (September 2022)
| 🫠 🫱🏼‍🫲🏿 🫰🏽   | 14.0 (September 2021)
| 😶‍🌫️ 🧔🏻‍♀️ 🧑🏿‍❤️‍🧑🏾   | 13.1 (September 2020)
| 🥲 🥷🏿 🐻‍❄️   | 13.0 (March 2020)
| 🧑🏻‍🦰 🧑🏿‍🦯 👩🏻‍🤝‍👩🏼   | 12.1 (October 2019)
| 🦩 🦻🏿 👩🏼‍🤝‍👩🏻   | 12.0 (February 2019)

BOM March 2023 (Compose UI 1.4) brings support for the latest emoji version, including backwards compatibility with older Android versions down to API 21.

_This support requires no changes to your app_— if you use `Text` and `TextField` (Material 2 or Material 3) or `BasicText` and `BasicTextField`, you get modern emoji support out of the box.

The best way to test the latest emojis in your app is by using a real device on API 30 or below.

If you're using a custom emoji solution, or need to disable the default emoji resolution in Compose for any other reason, you can use `PlatformTextStyle(emojiSupportMatch)`:

```
Text(
text = "Hello $EMOJI_TEXT",
style = TextStyle(
platformStyle = PlatformTextStyle(
emojiSupportMatch = EmojiSupportMatch.None
)/* ... */
)
)
```

## Interoperability

If your app uses both Views and Compose in the same `Activity`, make sure you are using the appropriate APIs to configure emojis correctly. The following sections describe when to use each API.

### Extending from `ComponentActivity`

If your `Activity` extends from Compose `ComponentActivity` instead of `AppCompatActivity`, follow the Support emoji without AppCompat instructions.

Because you are not extending `AppCompatActivity`, add the Emoji2 library to your dependencies and use `EmojiTextView` in your views instead of the `TextView` widget, as shown in the following snippet:

```
class MainActivity : ComponentActivity() {

override fun onCreate(savedInstanceState: Bundle?) {
super.onCreate(savedInstanceState)
setContentView(R.layout.activity_main)

val emojiTextView: EmojiTextView = findViewById(R.id.emoji_text_view)
emojiTextView.text = getString(R.string.emoji_text_view, EMOJI_TEXT)

val composeView: ComposeView = findViewById(R.id.compose_view)

composeView.apply {
setContent {
// compose code
}
}
}
}
```

Then, in your XML file:

```
<androidx.emoji2.widget.EmojiTextView
android:id="@+id/emoji_text_view"
android:layout_width="match_parent"
android:layout_height="wrap_content"
/>
```

### Extending from `AppCompatActivity`

If your `Activity` extends from `AppCompatActivity`, you can use `ComposeView` to call composable functions. Emojis render correctly across Android versions when you use Text composables.

If you are extending from `AppCompatActivity`, inflate `TextView` from XML to have emojis rendered correctly.

This applies if you are inflating the XML:
- outside `ComposeView`, in the `Activity`. Notice the usage of `AppCompatActivity` and `TextView` in the following snippet:

```
class MyActivity : AppCompatActivity() {

override fun onCreate(savedInstanceState: Bundle?) {
super.onCreate(savedInstanceState)
setContentView(R.layout.activity_main)

val emojiTextView: TextView = findViewById(R.id.emoji_text_view)
emojiTextView.text = getString(R.string.emoji_text_view, EMOJI_TEXT)

val composeView: ComposeView = findViewById(R.id.compose_view)

composeView.apply {
setContent {
// compose code
}
}
}
}
```

- inside `ComposeView`, via view binding using `AndroidViewBinding`, like in the following snippet:

```
class MyActivity : AppCompatActivity() {

override fun onCreate(savedInstanceState: Bundle?) {
super.onCreate(savedInstanceState)

setContentView(
ComposeView(this).apply {
setContent {
Column {
Text(EMOJI_TEXT)

AndroidViewBinding(ExampleViewBinding::inflate) {
emojiTextView.text = EMOJI_TEXT
}
}
}
}
)
}
}
```

To inflate a text with `AndroidView` inside `ComposeView`, use `AppCompatTextView` to render emojis properly:

```
class MyActivity : AppCompatActivity() {

override fun onCreate(savedInstanceState: Bundle?) {
super.onCreate(savedInstanceState)

setContentView(
ComposeView(this).apply {
setContent {
Column {
Text(EMOJI_TEXT)

AndroidView(
factory = { context -> AppCompatTextView(context) },
update = { it.text = EMOJI_TEXT }
)
}
}
}
)
}
}
```

See the Interoperability APIs documentation for details.

## Troubleshooting

If you're seeing _tofu_ (☐) instead of the emoji, first check if the problem is your specific test device. There are a few main things you can check:
- You might be using a recently flashed device or a new emulator. If possible, try another real test device you use often that is signed into your Google Account. Remember that API should be 30 or below to ensure emojis work in the correct versions.
- The test phone doesn't support downloadable fonts.
- Check the correct Google Play services version.

## Recommended for you
- Note: link text is displayed when JavaScript is off
- Other considerations
- Text in Compose
- Scroll
