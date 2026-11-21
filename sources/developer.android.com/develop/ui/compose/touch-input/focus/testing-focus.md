# Test focus navigation
Source: https://developer.android.com/develop/ui/compose/touch-input/focus/testing-focus

Automating focus navigation tests ensures your app delivers a consistent and predictable user experience across hardware keyboards, D-pads, and accessibility tools. Compose provides built-in testing APIs to simulate key input and verify focus states.

## Set up focus tests

To write focus tests, use `createComposeRule()` from the Compose testing library:

```
@RunWith(AndroidJUnit4::class)
class FocusNavigationTest {
@get:Rule
val composeTestRule = createComposeRule()
```
**Note:** By default, Compose testing environments initialize in touch mode. To configure whether your test environment starts in touch mode or keyboard mode, see the default input mode configuration.

## Verify focus targets

You can verify whether a UI element can receive focus by performing a click or requesting focus, and then asserting its state using `assertIsFocused()` and `assertIsNotFocused()`.

```
@Test
fun interactiveElement_isFocusTarget() {
composeTestRule.setContent {
AppTheme {
CardListScreen()
}
}

val firstCard = composeTestRule.onNodeWithTag("card_1")
val secondCard = composeTestRule.onNodeWithTag("card_2")

// Focus the first element
firstCard.performClick()
firstCard.assertIsFocused()
secondCard.assertIsNotFocused()
}
```

## Test one-dimensional focus traversal

Simulate Tab and Shift+Tab key presses using `performKeyInput` to verify that focus advances through the UI in appearance order. As a best practice, verify both forward navigation (`Key.Tab`) and reverse navigation (`Key.Tab` with `Key.ShiftLeft` held down) to ensure two-way traversal integrity:

```
@Test
fun tabKey_navigatesInAppearanceOrder() {
composeTestRule.setContent {
AppTheme {
CardListScreen()
}
}

val firstCard = composeTestRule.onNodeWithTag("card_1")
val secondCard = composeTestRule.onNodeWithTag("card_2")
val thirdCard = composeTestRule.onNodeWithTag("card_3")

firstCard.performClick()
firstCard.assertIsFocused()

// Press Tab -> Moves to second card
firstCard.performKeyInput {
pressKey(Key.Tab)
}
secondCard.assertIsFocused()
firstCard.assertIsNotFocused()

// Press Tab -> Moves to third card
secondCard.performKeyInput {
pressKey(Key.Tab)
}
thirdCard.assertIsFocused()

// Press Shift + Tab -> Moves backward to second card
thirdCard.performKeyInput {
withKeyDown(Key.ShiftLeft) {
pressKey(Key.Tab)
}
}
secondCard.assertIsFocused()
}
```

## Test two-dimensional directional traversal

Simulate arrow keys or D-pad directional keys using `Key.DirectionDown`, `Key.DirectionUp`, `Key.DirectionRight`, and `Key.DirectionLeft`.

```
@Test
fun arrowKeys_moveFocusTwoDimensionallyWithoutWrap() {
composeTestRule.setContent {
AppTheme {
GridLayoutScreen()
}
}

val topLeftButton = composeTestRule.onNodeWithTag("btn_top_left")
val bottomLeftButton = composeTestRule.onNodeWithTag("btn_bottom_left")

topLeftButton.performClick()
topLeftButton.assertIsFocused()

// Down arrow moves focus downward to bottom-left button
topLeftButton.performKeyInput {
pressKey(Key.DirectionDown)
}
bottomLeftButton.assertIsFocused()

// Directional keys do not wrap around: pressing Down on bottom element stays focused
bottomLeftButton.performKeyInput {
pressKey(Key.DirectionDown)
}
bottomLeftButton.assertIsFocused()
}
```

## Test text field focus behavior

Verify that single-line text fields advance focus on Tab, while multi-line text fields retain focus:

```
@Test
fun textField_handlesTabAccordingToLineLimits() {
composeTestRule.setContent {
AppTheme {
FormScreen()
}
}

val singleLineNode = composeTestRule.onNodeWithTag("single_line_field")
val multiLineNode = composeTestRule.onNodeWithTag("multi_line_field")
val submitButtonNode = composeTestRule.onNodeWithTag("submit_button")

// Single-line field advances focus on Tab
singleLineNode.performClick()
singleLineNode.assertIsFocused()
singleLineNode.performKeyInput { pressKey(Key.Tab) }
multiLineNode.assertIsFocused()

// Multi-line field keeps focus and inserts '\t'
multiLineNode.performKeyInput { pressKey(Key.Tab) }
multiLineNode.assertIsFocused()
submitButtonNode.assertIsNotFocused()

// Shift + Tab escapes multi-line field to previous target
multiLineNode.performKeyInput {
withKeyDown(Key.ShiftLeft) { pressKey(Key.Tab) }
}
singleLineNode.assertIsFocused()
}
```

## Recommended for you
- Note: link text is displayed when JavaScript is off
- Focus targets
- Focus traversal order
- Focus in text fields
- Group focus targets

Previous  arrow_back   Capture and release focus
