# Constraints and modifier order
Source: https://developer.android.com/develop/ui/compose/layouts/constraints-modifiers

In Compose, you can chain multiple modifiers together to change the look and feel of a composable. These modifier chains can affect the _constraints_ passed to composables, which define width and height bounds.

This page describes how chained modifiers affect constraints and, in turn, the measurement and placement of composables.

## Modifiers in the UI tree

To understand how modifiers influence each other, it's helpful to visualize how they appear in the UI tree, which is generated during the composition phase. For more information, see the Composition section.

In the UI tree, you can visualize modifiers as wrapper nodes for the layout nodes:
[IMAGE] Code for composables and modifiers, and their visual representation as a UI tree.

**Figure 1.** Modifiers wrapping layout nodes in the UI tree.

Adding more than one modifier to a composable creates a chain of modifiers. When you chain multiple modifiers, each modifier node _wraps the rest of the chain and the layout node within_. For example, when you chain a `clip` and a `size` modifier, the `clip` modifier node wraps the `size` modifier node, which then wraps the `Image` layout node.

In the layout phase, the algorithm that walks the tree stays the same, but each modifier node is visited as well. This way, a modifier can change the size requirements and placement of the modifier or layout node that it wraps.

As shown in Figure 2, the implementation of the `Image` and `Text` composables themselves consists of a chain of modifiers wrapping a single layout node.

The implementations of `Row` and `Column` are layout nodes that describe how to lay out their children.
[IMAGE] The tree structure from before, but now each node is just a simple layout, with a lot of modifier wrapping nodes around it.

**Figure 2.** The same tree structure as in Figure 1, but with composables in the UI tree visualized as chains of modifiers.

To summarize:
- Modifiers wrap a single modifier or layout node.
- Layout nodes can lay out multiple child nodes.

The following sections describe how to use this mental model to reason about modifier chaining and how it influences the size of composables.

## Constraints in the layout phase

The layout phase follows a three-step algorithm to find each layout node's width, height, and x, y coordinate:
- **Measure children**: A node measures its children, if any.
- **Decide own size**: Based on those measurements, a node decides on its own size.
- **Place children**: Each child node is placed relative to a node's own position.

`Constraints` help find the right sizes for the nodes during the first two steps of the algorithm. Constraints define the minimum and maximum bounds for a node's width and height. When the node decides on its size, its measured size should fall within this size range.

### Types of constraints

A constraint can be one of the following:
- **Bounded**: The node has a maximum and minimum width and height.

[IMAGE] Bounded constraints of different sizes within a container.

**Figure 3.** Bounded constraints.
- **Unbounded**: The node is not constrained to any size. The maximum width and height bounds are set to infinity.

[IMAGE] Unbounded constraints that have the width and height set to infinity. The constraints extend beyond the container.

**Figure 4.** Unbounded constraints.
- **Exact**: The node is asked to follow an exact size requirement. The minimum and maximum bounds are set to the same value.

[IMAGE] Exact constraints that conform to an exact size requirement within the container.

**Figure 5.** Exact constraints.
- **Combination**: The node follows a combination of the preceding constraint types. For example, a constraint could bound the width while allowing for an unbounded maximum height, or set an exact width but provide a bounded height.

[IMAGE] Two containers that show combinations of bounded and unbounded constraints and exact widths and heights.

**Figure 6.** Combinations of bounded and unbounded constraints and exact widths and heights.

The next section describes how these constraints are passed from a parent to a child.

### How constraints are passed from parent to child

During the first step of the algorithm described in Constraints in the layout phase, constraints are passed down from parent to child in the UI tree.

When a parent node measures its children, it provides these constraints to each child to let them know how big or small they're allowed to be. Then, when it decides its own size, it also adheres to the constraints that were passed in by its own parents.

At a high level, the algorithm works in the following way:
- To decide the size it actually wants to occupy, the root node in the UI tree measures its children and forwards the same constraints to its first child.
- If the child is a modifier that does not impact measurement, it forwards the constraints to the next modifier. The constraints are passed down the modifier chain as-is unless a modifier that impacts measurement is reached. The constraints are then re-sized accordingly.
- Once a node is reached that doesn't have any children (referred to as a "leaf node"), it decides its size based on the constraints that were passed in, and returns this resolved size to its parent.
- The parent adapts its constraints based on this child's measurements, and calls its next child with these adjusted constraints.
- Once all children of a parent are measured, the parent node decides on its own size and communicates that to its own parent.
- This way, the whole tree is traversed depth-first. Eventually, all the nodes have decided on their sizes, and the measurement step is completed.

For an in-depth example, see the Constraints and modifier order video.

## Modifiers that affect constraints

You learned in the previous section that some modifiers can affect constraint size. The following sections describe specific modifiers that impact constraints.

### The `size` modifier

The `size` modifier declares the preferred size of the content.

For example, the following UI tree should be rendered in a container of `300dp` by `200dp`. The constraints are bounded, allowing widths between `100dp` and `300dp`, and heights between `100dp` and `200dp`:
[IMAGE] A portion of a UI tree with the size modifier wrapping a layout node, and the
representation of the bounded constraints set by the size modifier in a container.

**Figure 7.** Bounded constraints in the UI tree and its representation in a container.

The `size` modifier adapts incoming constraints to match the value passed to it. In this example, the value is `150dp`:
[IMAGE] The same as Figure 7, except with the size modifier adapting incoming constraints to match the value passed to it.

**Figure 8.** The `size` modifier adjusting constraints to `150dp`.

If the width and height are smaller than the smallest constraint bound, or larger than the largest constraint bound, the modifier matches the passed constraints as closely as it can while still adhering to the constraints passed in:
[IMAGE] Two UI trees and their corresponding representations in containers. In the first, the
size modifier accepts the incmoing constraints; in the second, the size modifier adapts to the
too-large constraints as closely as possible, resulting in constraints that fill the container.

**Figure 9.** The `size` modifier adhering to the passed constraint as closely as possible.

Note that chaining multiple `size` modifiers does not work. The first `size` modifier sets both the minimum and maximum constraints to a fixed value. Even if the second size modifier requests a smaller or larger size, it still needs to adhere to the exact bounds passed in, so it won't override those values:
[IMAGE] A chain of two size modifiers in the UI tree and its representation in a container,
which is the result of the first value passed in and not the second value.

**Figure 10.** A chain of two `size` modifiers, in which the second value passed in (`50dp`) does not override the first value (`100dp`).

### The `requiredSize` modifier

Use the `requiredSize` modifier instead of `size` if you need your node to override the incoming constraints. The `requiredSize` modifier replaces the incoming constraints and passes the size you specify as exact bounds.

If the size you pass is outside the incoming constraints, the node reports a size that fits them when the size is passed back up the tree, and its content is centered in that space on each axis where the two sizes differ. Content larger than the constraints extends past them. In Figure 11, `requiredSize(50)` is smaller than the `100dp` that `size` passes in, so the `50dp` content is centered in a `100dp` space:
[IMAGE] The size and requiredSize modifier chained in a UI tree, and the corresponding
representation in a container. The requiredSize modifier constraints override the size modifier
constraints.

**Figure 11.** The `requiredSize` modifier overriding incoming constraints from the `size` modifier.

### The `width` and `height` modifiers

The `size` modifier adapts both the width and height of the constraints. With the `width` modifier, you can set a fixed width but leave the height undecided. Similarly, with the `height` modifier, you can set a fixed height, but leave the width undecided:
[IMAGE] Two UI trees, one with the width modifier and its container representation and the other
with the height modifier and its representation.

**Figure 12.** The `width` modifier and `height` modifier setting a fixed width and height, respectively.

To set a width or height that overrides the incoming constraints, use `requiredWidth` or `requiredHeight`. Like `requiredSize`, each one replaces the incoming constraints, but only on its own axis: `requiredWidth` keeps the incoming height constraints, and `requiredHeight` keeps the incoming width constraints.

### The `sizeIn` modifier

The `sizeIn` modifier lets you set minimum and maximum constraints for width and height, within the incoming constraints: where the incoming constraints are tighter, they win. Use the `sizeIn` modifier if you need fine-grained control over the constraints. The `widthIn` and `heightIn` modifiers do the same on one axis and leave the constraints of the other axis unchanged. Each one takes a minimum, a maximum, or both.
[IMAGE] A UI tree with the sizeIn modifier with minimum and maximum widths and heights set,
and its representation within a container.

**Figure 13.** The `sizeIn` modifier with `minWidth`, `maxWidth`, `minHeight`, and `maxHeight` set.

To set a range that overrides the incoming constraints, use `requiredSizeIn`, or `requiredWidthIn` and `requiredHeightIn` for one axis. They relate to `sizeIn`, `widthIn`, and `heightIn` the way `requiredSize` relates to `size`.

## Examples

This section shows and explains the output from several code snippets with chained modifiers.

```
Image(
painterResource(R.drawable.hero),
contentDescription = null,
Modifier
.fillMaxSize()
.size(50.dp)
)
```

This snippet produces the following output:
[IMAGE] A blue square that fills its parent container.

**Figure 14.** The `Image` fills the maximum size as a result of the modifier chain.
- The `fillMaxSize` modifier changes the constraints to set both the minimum width and height to the maximum value — `300dp` in width and `200dp` in height.
- Even though the `size` modifier wants to use a size of `50dp`, it still needs to adhere to the incoming minimum constraints. So the `size` modifier will also output the exact constraint bounds of `300` by `200`, effectively ignoring the value provided in the `size` modifier.
- The `Image` follows these bounds and reports a size of `300` by `200`, which is passed all the way up the tree.

```
Image(
painterResource(R.drawable.hero),
contentDescription = null,
Modifier
.fillMaxSize()
.wrapContentSize()
.size(50.dp)
)
```

This snippet produces the following output:
[IMAGE] A small blue square centered within its parent container.

**Figure 15.** The `Image` is centered and sized to `50dp`.
- The `fillMaxSize` modifier adapts the constraints to set both the minimum width and height to the maximum value — `300dp` in width, and `200dp` in height.
- The `wrapContentSize` modifier resets the minimum constraints. So, while `fillMaxSize` resulted in fixed constraints, `wrapContentSize` _resets it back to bounded constraints_. The following node can now take up the whole space again, or be smaller than the entire space.
- The `size` modifier sets the constraints to minimum and maximum bounds of `50`.
- The `Image` resolves to a size of `50` by `50`, and the `size` modifier forwards that.
- The `wrapContentSize` modifier has a special property. It takes its child and _puts it in the center of the available minimum bounds_ that were passed to it. The size it communicates to its parents is thus equal to the minimum bounds that were passed into it.

By combining just three modifiers, you can define a size for the composable and center it in its parent.

```
Image(
painterResource(R.drawable.hero),
contentDescription = null,
Modifier
.clip(CircleShape)
.padding(10.dp)
.size(100.dp)
)
```

This snippet produces the following output:
[IMAGE] A circular shape that is incorrectly clipped because of
modifier order.

**Figure 16.** An incorrectly clipped shape due to modifier order.
- The `clip` modifier does not change the constraints.
- The `padding` modifier lowers the maximum constraints.
- The `size` modifier sets all constraints to `100dp`.
- The `Image` adheres to those constraints and reports a size of `100dp` by `100dp`.
- The `padding` modifier adds `10dp` on all sides to the size reported by the `Image`, so the layout with padding reports a width and height of `120dp`.
- Now, in the drawing phase, the `clip` modifier acts on a canvas of `120dp` by `120dp`. It creates a circle mask of that size.
- The `padding` modifier then insets its content by `10dp` on all sides, which lowers the canvas size for the `Image` to `100dp` by `100dp`.
- The `Image` is drawn in that smaller canvas. The image is clipped based on the original circle of `120dp`, so the output is a non-round result.

The next three snippets put a `Box` with a background color in the same `300dp` by `200dp` container.

```
Box(
modifier = Modifier
.requiredWidth(400.dp)
.height(100.dp)
.background(Color(0xFF3DDC84))
)
```

This snippet produces the following output:
[IMAGE] A wide green rectangle centered in a light-blue container, extending
horizontally past both sides of the container.

**Figure 17.** With `requiredWidth(400.dp)`, the `Box` extends `50dp` past each side of the `300dp` container.
- The `requiredWidth` modifier replaces the incoming width constraints with exact bounds of `400dp`. It keeps the incoming height constraints, `0` to `200dp`.
- The `height` modifier sets the height to `100dp`, which fits the height constraints.
- The `Box` measures `400dp` by `100dp`. That width is larger than the `300dp` that the container allows, so `requiredWidth` reports a width of `300dp` to its parent and centers its content in that space: the `Box` extends `50dp` past each side of the container.

```
Box(
modifier = Modifier
.widthIn(min = 100.dp, max = 400.dp)
.heightIn(max = 100.dp)
.fillMaxSize()
.background(Color(0xFF3DDC84))
)
```

This snippet produces the following output:
[IMAGE] A green rectangle centered vertically inside a light-blue container,
matching the container's full width without extending past its sides.

**Figure 18.** With `widthIn`, the `Box` measures `300dp` by `100dp` and stays inside the container.
- The `widthIn` modifier asks for a width between `100dp` and `400dp`. The incoming maximum width is `300dp`, so the width constraints become `100dp` to `300dp`.
- The `heightIn` modifier sets a maximum height of `100dp`, so the height constraints become `0` to `100dp`.
- The `fillMaxSize` modifier sets the minimum width and height to those maximums, so the `Box` is `300dp` by `100dp` and stays inside the container.

```
Box(
modifier = Modifier
.requiredSizeIn(minWidth = 100.dp, maxWidth = 400.dp, maxHeight = 100.dp)
.fillMaxSize()
.background(Color(0xFF3DDC84))
)
```

This snippet produces the following output:
[IMAGE] A wide green rectangle centered in a light-blue container, extending
horizontally past both sides of the container.

**Figure 19.** With `requiredSizeIn`, the `Box` measures `400dp` by `100dp` and extends `50dp` past each side of the container, the same result as Figure 17.
- The `requiredSizeIn` modifier replaces the incoming constraints with a width of `100dp` to `400dp` and a height of `0` to `100dp`. The minimum height isn't set, so it keeps the incoming minimum height of `0`.
- The `fillMaxSize` modifier sets the minimum width and height to those maximums, so the `Box` measures `400dp` by `100dp`.
- As in the `requiredWidth` example, the width is larger than the container allows, so the `Box` is centered and extends `50dp` past each side.

With the same range, `widthIn` and `heightIn` keep the `Box` inside the container, and `requiredSizeIn` doesn't.

Previous  arrow_back   Modifiers

Next  Custom modifiers   arrow_forward
