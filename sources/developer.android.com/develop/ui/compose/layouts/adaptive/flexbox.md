# FlexBox
Source: https://developer.android.com/develop/ui/compose/layouts/adaptive/flexbox

`FlexBox` is a container that lays out items in a single direction. It can resize, wrap, align, and distribute space among items to optimally fill the available space. It's a useful layout for different sized items and for resizing items when the available space changes.

With `FlexBox`, you can:
- Control how items grow and shrink to fill the available space
- Wrap items onto new rows or columns when there isn't enough space for them
- Distribute extra space between items using convenient presets

## When to use FlexBox

`FlexBox` is usually used to display a small number of items _within_ an overall screen layout. For an overall screen layout, `Grid` is usually a better choice. `FlexBox` does not support lazy-loading of items. To display large numbers of items, use lazy lists and grids. If you need to wrap items, use `FlexBox` instead of `FlowRow` and `FlowColumn`.

## Terminology and concepts **Key Point:** `FlexBox` is heavily influenced by the CSS Flexible Box Layout specification and has almost identical concepts, terminology, and behavior. If you're familiar with `display: flex`, you'll find `FlexBox`'s properties and behavior almost identical.

`FlexBox` lays out its items in either horizontal or vertical _lines_. This direction of these lines establishes the _main axis_. 90 degrees to the main axis is the _cross axis_. The length of the `FlexBox` along the main axis is known as the _main size_. The corresponding cross axis length is known as the _cross size_. These sizes and axes form the basis of `FlexBox`'s behavior.

[IMAGE] FlexBox with horizontal main axis and vertical cross axis.

**Figure 1.** Axes and sizes when the `FlexBox` direction is `Row`.

[IMAGE] FlexBox with vertical main axis and horizontal cross axis.

**Figure 2.** Axes and sizes when the `FlexBox` direction is `Column`.

### Apply properties

You can apply `FlexBox` properties in two ways:
- To the `FlexBox` container using `FlexBox(config)`
- To an item inside the `FlexBox` using `Modifier.flex`

|

**Container properties (`config`**)  |

**Item properties (`Modifier.flex`**)
|
- `direction` - the item layout direction
- `wrap` - whether to wrap items if the **main size** is insufficient
- `justifyContent` - how to **distribute** items along the **main axis**
- `maxItemsInEachLine` - limits the maximum number of items placed on a single line when wrapping is enabled, regardless of remaining available space
- `alignItems` - how to **align** items along the **cross axis**
- `alignContent` - how to distribute extra space from the **cross size** when there are multiple lines
- `rowGap` / `columnGap` - adds space between items and lines

See Set container behavior for more information about these properties.  |
- `basis` - the size of the item before any extra space from the **main size** is distributed
- `grow` - the share of extra space from the **main size** that this item should receive
- `shrink` - the share of space deficit from the **main size** that this item should receive
- `alignSelf` - how to distribute extra space from the **cross size** to this item, overrides `alignItems`
- `order` - controls the layout order

See Set item behavior for more information about these properties.

### Understand the `FlexBox` layout algorithm

One of `FlexBox`'s most powerful features is its ability to resize its children to best fit the space available to it. Understanding how `FlexBox` does this can help you set `FlexBox` properties to optimize your UI for all possible sizes.

`FlexBox`'s layout algorithm works in the following way:
- **Calculate child base size**: Use the child's `basis` value to calculate its initial size along the main axis before any extra space is distributed.
- **Sort the children**: Sort the children by their `order` values, if present.
- **Build lines**: For each child, check if its initial size plus `gap` will fit into the remaining space on the current line. If so, place this child into the line. If not, place it onto a new line if wrapping is enabled, or place the item into the current line where it will overflow (it will be partially obscured by the edge of the container).
- **Align or resize items in the main axis**: For each line, distribute extra space _to_ or between items by resizing or aligning them.
- **Align or resize items in the cross axis**: For each line, distribute extra space to or between items and lines by stretching or aligning them.

Now that you're familiar with `FlexBox` concepts, see Get started to create a basic `FlexBox`.

Previous  arrow_back   Set item properties

Next  Get started   arrow_forward
