# Lists
Source: https://developer.android.com/design/ui/wear/guides/m2-5/components/lists

Lists are continuous, vertical indexes of elements. Use ScalingLazyColumn to create lists on Wear OS.

`ScalingLazyColumn` is a customization of `LazyColumn` that's built specifically for Wear OS. Lists offer scaling and transparency, which allows content to shrink and fade at the top and bottom of the screen to help users see what to focus on. Lists are anchored on the center of the screen, which gives additional emphasis to the items in the middle of the list.

Lists can contain items of fixed or variable height. List content should have margin enough to accommodate for circular screens.

## Padding

Standardized Paddings are used when creating a list depending on the element. If further vertical padding is needed for more complex lists, spacing is set in multiples of 4 dp for consistency.

**Between sections**

Padding between top, middle and bottom section should be set to 16 dp.

**Between titles and content**

Padding between top, middle and bottom section should be set to 12 dp.

**Between slots**

Padding between slots in the middle section should be set to 16 dp.

**Between groups**

Padding between groups within a slot should be set to 8 dp.

**Between elements**

Padding between elements within a group should be set to 4 dp.

## Snapping behavior

To give emphasis to a list item when scrolling, use snapping. Use snapping when items are tall but not taller than the screen.

Snapping behavior comes in two forms. `ItemCenter` uses the center of the item as a reference point. `ItemStart` uses the edge of the item as a reference point. Use the first to center one item on the screen, use the second to center two items on the screen. Set this behavior using the anchorType parameter.

For further control over the snapping behavior in a list, set `flingBehavior` to `ScalingLazyColumnDefaults.snapFlingBehavior` and `rotaryScrollableBehavior` to `RotaryScrollableDefaults.snapBehavior`.

## Usage

See the following examples of how to use lists on watches.

## Adaptive layouts

The following images show several examples of adaptive layouts. For implementation guidance, visit the develop for different screen sizes page.

**Responsive behavior**

Lists components stretch to fill the available width on larger displays.

### Top margins

Top margins vary depending on which elements are in the top section.

**List starts with Title**

**List starts with 1 or 2+ buttons**

**List starts with other elements**

### Bottom margins

Bottom margins vary depending on which elements are in the bottom section.

**List ends with buttons**

**List ends with other elements**

### Side margins

Side margins uses a standard percentage of 5.2% across all list types to ensure scalability on larger displays.

### Internal margins

**Titles**

There’s an added 7.3% internal margin to ensure titles don’t clip.

**Bottom buttons**

Bottom buttons fill the available width past the 225 breakpoint. To keep visual hierarchy, there’s an added internal padding of 14.56% on larger screens.
