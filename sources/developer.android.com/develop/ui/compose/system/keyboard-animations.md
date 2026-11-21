# Use keyboard IME animations
Source: https://developer.android.com/develop/ui/compose/system/keyboard-animations

You can apply `Modifier.imeNestedScroll()` to a scrolling container to open and close the IME automatically when scrolling to the bottom of the container.

```
class WindowInsetsExampleActivity : AppCompatActivity() {
override fun onCreate(savedInstanceState: Bundle?) {
super.onCreate(savedInstanceState)

WindowCompat.setDecorFitsSystemWindows(window, false)

setContent {
MaterialTheme {
MyScreen()
}
}
}
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MyScreen() {
Box {
LazyColumn(
modifier = Modifier
.fillMaxSize() // fill the entire window
.imePadding() // padding for the bottom for the IME
.imeNestedScroll(), // scroll IME at the bottom
content = { }
)
FloatingActionButton(
modifier = Modifier
.align(Alignment.BottomEnd)
.padding(16.dp) // normal 16dp of padding for FABs
.navigationBarsPadding() // padding for navigation bar
.imePadding(), // padding for when IME appears
onClick = { }
) {
Icon(imageVector = Icons.Filled.Add, contentDescription = "Add")
}
}
}
```

[IMAGE] Animation showing a UI element scrolling up and down to make way for a keyboard

**Figure 3.** IME animations.

Previous  arrow_back   Set up window insets

Next  Use Material 3 insets   arrow_forward
