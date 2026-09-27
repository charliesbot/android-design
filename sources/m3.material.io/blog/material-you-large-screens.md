# Better design for bigger screens
Source: https://m3.material.io/blog/material-you-large-screens

> How Material You supports beautiful, efficient tablet and foldable experiences

Published: 2023-04-29

In recent years, we’ve seen a growing trend towards larger screens. This is due in part to the increasing popularity of tablets, larger smartphones, and desktops.

At Google I/O this year, Material Design is diving into the latest updates to large screen guidelines for designers and developers. Look out for our talk and read on to learn how Material helps you meet the challenges and opportunities of designing for multiple devices to meet the rising demand from app users.

## Design foundation: Window size classes

One of the top challenges of designing for large screens is creating layouts that are both visually appealing and user-friendly – without completely redesigning your app for each form factor. So, rather than design for an ever-increasing number of formats, we created Window Size Classes to ensure layouts adapt seamlessly across a range of devices.

For more complex layout scenarios, you can also consider incorporating width and height classes. However, for most apps, especially those with a vertically scrolling UI, the three window width size classes can meet your needs. Learn more in Android’s Window Class Size documentation.

## Layout anatomy

A typical app’s layout is made of two parts: the navigation and the body.

When it comes to balancing your design with user needs like ergonomics, selecting the best navigation component for your screen is an important step.

Panes are best-suited for the need to organize the body of an app. Panes help organize content and create a clear containment structure for components. They can be fixed or flexible; they can hold persistent components, such as a top app bar or search bar, and context-specific elements, such as images, text, lists, cards, and buttons.

Content in a pane can be aligned and displayed in multiple columns. Columns are exclusive to a pane and are not used at the window level, allowing more granular control of layout within a pane. Learn more about how Material Design defines parts of a layout.

## Hardware

New hardware is always exciting–but also brings challenges for handling new, unexpected use cases. Material’s updated guidance looks at how your design can adapt to a range of hardware specs with less work to customize and adapt your layout.

One example of a seemingly tricky hardware factor shows up on folding devices with a center hinge that divides your screen into two halves. With some folding devices, there’s no display hardware in this region, so the screen will operate as two distinct panes. When opening a foldable device, an app should transition from the front screen to the unfolded screen automatically and within seconds.

Display cutouts, such front-facing cameras, are also an example of a constraint to consider since this can obstruct some of your screen design. In these scenarios, it helps to offset the body region to make sure no part of the UI is obscured.

## A look at the future of Material for large screens

The future of how Material Design is planning for large screen design and development is a continuation of the visual language of Material You. Our goal is to continue to release spirited, adaptive elements for large screen experiences that are grounded in the ethos of M3.

Stay tuned for I/O 2023 to see some of Material Design’s work in progress ideas!

[IMAGE] : There are three window size classes: compact, medium, and expanded

[IMAGE] : Compact devices use a navigation bar, medium devices can use a navigation rail, and expanded devices can take advantage of a navigation drawer

[IMAGE] : New hardware comes with new challenges: hinges, on-screen cameras, and rapidly changing window size classes

[IMAGE] :
