# Animate character-by-character the appearance of text
Source: https://developer.android.com/develop/ui/compose/quick-guides/content/animate-text

You can animate, character-by-character, the appearance of text, so it looks like a streaming typing effect, similar to what a typewriter would produce.

## Results

**Figure 1.** Text and emoji animated character-by-character.

## Version compatibility

This implementation requires that your project minSDK be set to API level 21 or higher.

### Dependencies

## Animate text character-by-character

This code animates text character-by-character. It tracks an index to control how much of the text is revealed. The displayed text updates dynamically to show only the characters up to the current index. Finally, the variable runs the animation when it changes.

```
@Composable
private fun AnimatedText() {
val text = "This text animates as though it is being typed \uD83E\uDDDE\u200D♀\uFE0F \uD83D\uDD10  \uD83D\uDC69\u200D❤\uFE0F\u200D\uD83D\uDC68 \uD83D\uDC74\uD83C\uDFFD"

// Use BreakIterator as it correctly iterates over characters regardless of how they are
// stored, for example, some emojis are made up of multiple characters.
// You don't want to break up an emoji as it animates, so using BreakIterator will ensure
// this is correctly handled!
val breakIterator = remember(text) { BreakIterator.getCharacterInstance() }

// Define how many milliseconds between each character should pause for. This will create the
// illusion of an animation, as we delay the job after each character is iterated on.
val typingDelayInMs = 50L

var substringText by remember {
mutableStateOf("")
}
LaunchedEffect(text) {
// Initial start delay of the typing animation
delay(1000)
breakIterator.text = StringCharacterIterator(text)

var nextIndex = breakIterator.next()
// Iterate over the string, by index boundary
while (nextIndex != BreakIterator.DONE) {
substringText = text.subSequence(0, nextIndex).toString()
// Go to the next logical character boundary
nextIndex = breakIterator.next()
delay(typingDelayInMs)
}
}
Text(substringText)
```

### Key points about the code
- `BreakIterator` correctly iterates over characters regardless of how they are stored. For example, animated emojis are made up of multiple characters; `BreakIterator` ensures that they're handled as a single character, so that the animation isn't broken.
- `LaunchedEffect` starts a coroutine to introduce the delay between the characters. You can replace the code block with a click listener–or any other event–to trigger animation.
- The `Text` composable re-renders every time the value of `substringText` is updated.

## Collections that contain this guide

This guide is part of these curated Quick Guide collections that cover broader Android development goals:

###### Display text
Text is a central piece of any UI. Find out different ways you can present text in your app to provide a delightful user experience.
Quick guide collection

###### Compose basics (video collection)
This series of videos introduces various Compose APIs, quickly showing you what’s available and how to use them.
Quick guide collection
