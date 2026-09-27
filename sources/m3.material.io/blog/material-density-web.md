# Using Material Density on the Web
Source: https://m3.material.io/blog/material-density-web

> A hands-on guide to applying default, comfortable, and compact density to your application

Published: 2020-01-16

A common issue for any front-end developer is layout and spacing. How dense should your user interface be? Can a design system really account for all the different layouts in a product? Material is here to help.

The latest update to our density guidelines includes a new systematic approach to applying density and spacing in digital products. Now, you can change the spacing of your product to suit layouts for different use cases, such as data-heavy layouts vs. a layout with a single call-to-action. **Just as you use Material Theming to systematically apply color, typography, and shape to your UI, now you can systematically apply density.**

Global density changes affect the spacing between all elements at a macro level. However, you can also apply bespoke density values to any individual component.

Material Design’s density scale includes three levels of density: **default**, **comfortable**, and **compact**.

## The Rules of Applying Density

Compact density can be great in data-rich applications that need to display a lot of information in a small amount of space. For example, data tables or long forms may benefit from an increase in density. This would allow more information to be seen on the page, giving the user important relational context at any given time.

However, an increased density display may be less useful in layouts that involve focused tasks or small user inputs. For example, date pickers need ample space for the user to be able to accurately make a selection. High density spacing can also make alerts and messages difficult to notice and read.

An increase in density can also decrease the accessibility of your UI in some cases, so make sure you’re applying density in places where user feedback is not affected. This dense calendar UI is an example of how increasing density could make it harder to use an application.

It’s important to keep accessibility in mind when you’re applying density to your UI. **No matter the density, all touch targets should be at least `48px` in size.**

For the buttons in dense layouts, we’ve applied external padding so that even when they appear smaller and condensed, they still have an accessible touch target size.

It’s important to maintain a good design equilibrium in dense layouts, so when increasing component density you should also _decrease_ your layout or grid density. This means that the more dense your components become, the larger your margins and gutter widths should be. This helps keep your UI legible.

## Using Density on the Web

**The Density Scale**

Density is defined by a scale starting at 0 for default density. The scale moves to negative numbers (-1, -2, -3) as the space between components decreases, resulting in a higher density layout. Each increment also decreases the height of a component by 4px. But it does not affect the horizontal spacing within the component. For example, in an icon button, increasing the density will not make the icon appear closer to the text.

The height or size of a component is calculated with the following formula:

**Let’s See Some Code**

When you update to the latest version of MDC (4.0.0 or greater), density is included as mixins within the components. You can apply a density of `-3` (the most dense option) to a button by applying the following code:

In your terminal, install an MDC button via :

In your HTML, Add the following button markup:

_Note: this markup is different than the previous version of MDC (3.0.0)_

In your Sass file, Import the button mixins and apply the density as a mixin to your custom button:

The following components currently provide density Sass mixins:
- Button
- Checkbox
- Chip
- Data Table
- Icon Button
- List
- Radio
- Switch
- Tab Bar
- Text Field

We recommend customizing density using the provided density mixins rather than arbitrarily applying component height.

That’s pretty much it! You can check out the updated design guidance for more details, but applying the density scale is a breeze with the new MDC 4.0. If you added density to your application, reach out to us on Twitter @MaterialDesign. We’d love to see it.

[IMAGE] : Various Material themes created by adjusting color, shape, and typography.

[IMAGE] : A demo of Material’s density scale in action, created by engineer Abhinay Omkar.

[IMAGE] List items and text fields shown in default, comfortable, and compact densities: https://material.io/design/layout/applying-density.html#usage

[IMAGE] Date picker displaying September and October, with highlighted dates: An example of where not to use a higher density display. The increased density makes it more difficult for users to interact with the dates in this calendar.

[IMAGE] : Even though density can visually decrease, it’s important for touch targets to remain at least 48px in size.

[IMAGE] Dense app overlaid with columns illustrating its wider horizontal spacing: Increase horizontal spacing as you decrease vertical spacing.

[IMAGE] : Various density values change the vertical spacing in the button component.
