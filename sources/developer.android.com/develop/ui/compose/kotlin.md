# Kotlin for Jetpack Compose
Source: https://developer.android.com/develop/ui/compose/kotlin

Jetpack Compose is built around Kotlin. In some cases, Kotlin provides special idioms that make it easier to write good Compose code. If you think in another programming language and mentally translate that language to Kotlin, you're likely to miss out on some of the strength of Compose, and you might find it difficult to understand idiomatically-written Kotlin code. Gaining more familiarity with Kotlin's style can help you avoid those pitfalls.

## Default arguments

When you write a Kotlin function, you can specify _default values for function arguments_, used if the caller doesn't explicitly pass those values. This feature reduces the need for overloaded functions.

For example, suppose you want to write a function that draws a square. That function might have a single required parameter, **sideLength**, specifying the length of each side. It might have several optional parameters, like **thickness**, **edgeColor** and so on; if the caller doesn't specify those, the function uses default values. In other languages, you might expect to write several functions:

```
// We don't need to do this in Kotlin!
void drawSquare(int sideLength) { }

void drawSquare(int sideLength, int thickness) { }

void drawSquare(int sideLength, int thickness, Color edgeColor) { }
```

In Kotlin, you can write a single function and specify the default values for the arguments:

```
fun drawSquare(
sideLength: Int,
thickness: Int = 2,
edgeColor: Color = Color.Black
) {
}
```

Besides saving you from having to write multiple redundant functions, this feature makes your code much clearer to read. If the caller doesn't specify a value for an argument, that indicates that they're willing to use the default value. In addition, the named parameters make it much easier to see what's going on. If you look at the code and see a function call like this, you might not know what the parameters mean without checking the `drawSquare()` code:

```
drawSquare(30, 5, Color.Red);
```

By contrast, this code is self-documenting:

```
drawSquare(sideLength = 30, thickness = 5, edgeColor = Color.Red)
```

Most Compose libraries use default arguments, and it's a good practice to do the same for the composable functions that you write. This practice makes your composables customizable, but still makes the default behavior simple to invoke. So, for example, you might create a simple text element like this:

```
Text(text = "Hello, Android!")
```

That code has the same effect as the following, much more verbose code, in which more of the `Text` parameters are set explicitly:

```
Text(
text = "Hello, Android!",
color = Color.Unspecified,
fontSize = TextUnit.Unspecified,
letterSpacing = TextUnit.Unspecified,
overflow = TextOverflow.Clip
)
```

Not only is the first code snippet much simpler and easier to read, it's also self-documenting. By specifying only the `text` parameter, you document that for all the other parameters, you want to use the default values. By contrast, the second snippet implies that you want to explicitly set the values for those other parameters, though the values you set happen to be the default values for the function.

## Higher-order functions and lambda expressions

Kotlin supports _higher-order functions_, functions that receive other functions as parameters. Compose builds upon this approach. For example, the `Button` composable function provides an `onClick` lambda parameter. The value of that parameter is a function, which the button calls when the user clicks it:

```
Button(
// ...
onClick = myClickFunction
)
// ...
```

Higher-order functions pair naturally with _lambda expressions_, expressions which evaluate to a function. If you only need the function once, you don't have to define it elsewhere to pass it to the higher-order function. Instead, you can just define the function right there with a lambda expression. The previous example assumes that `myClickFunction()` is defined elsewhere. But if you only use that function here, it's simpler to just define the function inline with a lambda expression:

```
Button(
// ...
onClick = {
// do something
// do something else
}
) { /* ... */ }
```

### Trailing lambdas

Kotlin offers a special syntax for calling higher-order functions whose _last_ parameter is a lambda. If you want to pass a lambda expression as that parameter, you can use _trailing lambda syntax_. Instead of putting the lambda expression within the parentheses, you put it afterwards. This is a common situation in Compose, so you need to be familiar with how the code looks.

For example, the last parameter to all layouts, such as the `Column()` composable function, is `content`, a function which emits the child UI elements. Suppose you wanted to create a column containing three text elements, and you need to apply some formatting. This code would work, but it's very cumbersome:

```
Column(
modifier = Modifier.padding(16.dp),
content = {
Text("Some text")
Text("Some more text")
Text("Last text")
}
)
```

Because the `content` parameter is the last one in the function signature, and we're passing its value as a lambda expression, we can pull it out of the parentheses:

```
Column(modifier = Modifier.padding(16.dp)) {
Text("Some text")
Text("Some more text")
Text("Last text")
}
```

The two examples have exactly the same meaning. The braces define the lambda expression that is passed to the `content` parameter.

In fact, if the _only_ parameter you're passing is that trailing lambda—that is, if the final parameter is a lambda, and you aren't passing any other parameters—you can omit the parentheses altogether. So, for example, suppose you didn't need to pass a modifier to the `Column`. You could write the code like this:

```
Column {
Text("Some text")
Text("Some more text")
Text("Last text")
}
```

This syntax is quite common in Compose, especially for layout elements like `Column`. The last parameter is a lambda expression defining the element's children, and those children are specified in braces after the function call.

## Scopes and receivers

Some methods and properties are only available in a certain scope. The limited scope lets you offer functionality where it's needed and avoid accidentally using that functionality where it isn't appropriate.

Consider an example used in Compose. When you call the `Row` layout composable, your content lambda is automatically invoked within a `RowScope`. This enables `Row` to expose functionality which is only valid within a `Row`. The following example demonstrates how `Row` has exposed a row-specific value for the `align` modifier:

```
Row {
Text(
text = "Hello world",
// This Text is inside a RowScope so it has access to
// Alignment.CenterVertically but not to
// Alignment.CenterHorizontally, which would be available
// in a ColumnScope.
modifier = Modifier.align(Alignment.CenterVertically)
)
}
```

Some APIs accept lambdas which are called in _receiver scope_. Those lambdas have access to properties and functions that are defined elsewhere, based on the parameter declaration:

```
Box(
modifier = Modifier.drawBehind {
// This method accepts a lambda of type DrawScope.() -> Unit
// therefore in this lambda we can access properties and functions
// available from DrawScope, such as the `drawRectangle` function.
drawRect(
/*...*/
/* ...
)
}
)
```

For more information, see function literals with receiver in the Kotlin documentation.

## Delegated properties

Kotlin supports delegated properties. These properties are called as if they were fields, but their value is determined dynamically by evaluating an expression. You can recognize these properties by their use of the `by` syntax:

```
class DelegatingClass {
var name: String by nameGetterFunction()

// ...
}
```

Other code can access the property with code like this:

```
val myDC = DelegatingClass()
println("The name property is: " + myDC.name)
```

When `println()` executes, `nameGetterFunction()` is called to return the value of the string.

These delegated properties are particularly useful when you're working with state-backed properties:

```
var showDialog by remember { mutableStateOf(false) }

// Updating the var automatically triggers a state change
showDialog = true
```
**Note:** Learn more about `remember` and `mutableStateOf` in the State in Jetpack Compose documentation.

## Destructuring data classes

If you define a data class, you can access the data with a destructuring declaration. For example, suppose you define a `Person` class:

```
data class Person(val name: String, val age: Int)
```

If you have an object of that type, you can access its values with code like this:

```
val mary = Person(name = "Mary", age = 35)

// ...

val (name, age) = mary
```

You'll often see that kind of code in Compose functions:

```
Row {

val (image, title, subtitle) = createRefs()

// The `createRefs` function returns a data object;
// the first three components are extracted into the
// image, title, and subtitle variables.

// ...
}
```

Data classes provide a lot of other useful functionality. For example, when you define a data class, the compiler automatically defines useful functions like `equals()` and `copy()`. You can find more information in the data classes documentation.

## Singleton objects

Kotlin lets you declare _singletons_, classes which always have one and only one instance. These singletons are declared with the `object` keyword. Compose often makes use of such objects. For example, `MaterialTheme` is defined as a singleton object; the `MaterialTheme.colors`, `shapes`, and `typography` properties all contain the values for the current theme.

## Type-safe builders and DSLs

Kotlin allows creating domain-specific languages (DSLs) with type-safe builders. DSLs allow building complex hierarchical data structures in a more maintainable and readable way.

Jetpack Compose uses DSLs for some APIs such as `LazyRow` and `LazyColumn`.

```
@Composable
fun MessageList(messages: List<Message>) {
LazyColumn {
// Add a single item as a header
item {
Text("Message List")
}

// Add list of messages
items(messages) { message ->
Message(message)
}
}
}
```

Kotlin enables type-safe builders using function literals with receiver. If we take the `Canvas` composable as an example, it takes as a parameter a function with `DrawScope` as the receiver, `onDraw: DrawScope.() -> Unit`, allowing the block of code to call member functions defined in `DrawScope`.

```
Canvas(Modifier.size(120.dp)) {
// Draw grey background, drawRect function is provided by the receiver
drawRect(color = Color.Gray)

// Inset content by 10 pixels on the left/right sides
// and 12 by the top/bottom
inset(10.0f, 12.0f) {
val quadrantSize = size / 2.0f

// Draw a rectangle within the inset bounds
drawRect(
size = quadrantSize,
color = Color.Red
)

rotate(45.0f) {
drawRect(size = quadrantSize, color = Color.Blue)
}
}
}
```

Learn more about type-safe builders and DSLs in Kotlin's documentation.

## Kotlin coroutines

Coroutines offer asynchronous programming support at the language level in Kotlin. Coroutines can _suspend_ execution without blocking threads. A responsive UI is inherently asynchronous, and Jetpack Compose solves this by embracing coroutines at the API level instead of using callbacks.

Jetpack Compose offers APIs that make using coroutines safe within the UI layer. The `rememberCoroutineScope` function returns a `CoroutineScope` with which you can create coroutines in event handlers and call Compose suspend APIs. See the following example using the `ScrollState`'s `animateScrollTo` API.

```
// Create a CoroutineScope that follows this composable's lifecycle
val composableScope = rememberCoroutineScope()
Button(
// ...
onClick = {
// Create a new coroutine that scrolls to the top of the list
// and call the ViewModel to load data
composableScope.launch {
scrollState.animateScrollTo(0) // This is a suspend function
viewModel.loadData()
}
}
) { /* ... */ }
```

Coroutines execute the block of code _sequentially_ by default. A running coroutine that calls a suspend function _suspends_ its execution until the suspend function returns. This is true even if the suspend function moves the execution to a different `CoroutineDispatcher`. In the previous example, `loadData` won't be executed until the suspend function `animateScrollTo` returns.

To execute code concurrently, new coroutines need to be created. In the example above, to parallelize scrolling to the top of the screen and loading data from `viewModel`, two coroutines are needed.

```
// Create a CoroutineScope that follows this composable's lifecycle
val composableScope = rememberCoroutineScope()
Button( // ...
onClick = {
// Scroll to the top and load data in parallel by creating a new
// coroutine per independent work to do
composableScope.launch {
scrollState.animateScrollTo(0)
}
composableScope.launch {
viewModel.loadData()
}
}
) { /* ... */ }
```

Coroutines make it easier to combine asynchronous APIs. In the following example, we combine the `pointerInput` modifier with the animation APIs to animate the position of an element when the user taps on the screen.

```
@Composable
fun MoveBoxWhereTapped() {
// Creates an `Animatable` to animate Offset and `remember` it.
val animatedOffset = remember {
Animatable(Offset(0f, 0f), Offset.VectorConverter)
}

Box(
// The pointerInput modifier takes a suspend block of code
Modifier
.fillMaxSize()
.pointerInput(Unit) {
// Create a new CoroutineScope to be able to create new
// coroutines inside a suspend function
coroutineScope {
while (true) {
// Wait for the user to tap on the screen and animate
// in the same block
awaitPointerEventScope {
val offset = awaitFirstDown().position

// Launch a new coroutine to asynchronously animate to
// where the user tapped on the screen
launch {
// Animate to the pressed position
animatedOffset.animateTo(offset)
}
}
}
}
}
) {
Text("Tap anywhere", Modifier.align(Alignment.Center))
Box(
Modifier
.offset {
// Use the animated offset as the offset of this Box
IntOffset(
animatedOffset.value.x.roundToInt(),
animatedOffset.value.y.roundToInt()
)
}
.size(40.dp)
.background(Color(0xff3c1361), CircleShape)
)
}
```

To learn more about Coroutines, check out the Kotlin coroutines on Android guide.

## Recommended for you
- Note: link text is displayed when JavaScript is off
- Material Components and layouts
- Side-effects in Compose
- Compose layout basics
