# Material Design Components for Android 1.9.0
Source: https://m3.material.io/blog/android-stable-release-1-9-0

> New ways to navigate in and interact with your applications

Published: 2023-05-17

Fast on the heels of Google I/O, we’re happy to announce the release of Material Design Components for Android (MDC-Android) 1.9.0.

The new Carousel component allows you to create experiences like photo galleries and interact with lists of items in more flexible ways than a `RecyclerView`.

It uses `RecyclerView` as a core so you can rest assured that it will be performant and integrate easily into your designs.

## Making your RecyclerView a Carousel

To use a `Carousel`, start with a working `RecyclerView` and wrap each of your item layout files with a `MaskableFrameLayout`. They will instruct the Carousel how to adjust items in relation to each other.

Next, instead of using a `LinearLayoutManager`, use a `CarouselLayoutManager`.

Masking in `MaskableFrameLayout` ranges from 0% to 100% (0.0 to 1.0 in the code). When the layout manager needs to mask an image, it calculates a masking rectangle. The image below shows different masks applied to the same image and how each will reduce the image’s visible area.

If you are using images in your Carousel with a custom image matrix or other components such as text, verify these objects work well with Carousel’s masking or optionally let them react to changes in mask size using an `OnMaskChangedListener` on your `MaskableFrameLayout`.

## How Items in A Carousel Are Masked

`MultiBrowseCarouselStrategy` allows a carousel to optimally display large, medium, and small items in the allotted space. It attempts to find the optimal amount of large items that will fit into the display with the least amount of size adjustment. After that it will add medium and small items.

In the images below, at the start of the carousel, we can see two large items, a medium item and a small item. At the end of the carousel, the arrangement shifts to become one small item, followed by a medium item, and finally two large items.
|   | 

For more information on integrating the new Carousel component into your app, check out the full developer documentation.

## What’s next for MDC ?

We’re hard at work on the 1.10.0 release with updates targeting Android U. The components or features we highlight in these posts are only a fraction of the work that lands in each release. Check out the release notes for a full listing.

You can follow the progress of new versions, file bug reports and submit feature requests on GitHub. Check out the catalog app to see the components in action. Also feel free to reach out to us on Twitter @materialdesign.

[IMAGE] diagram of how images are masked in a carousel: 

[IMAGE] screenshot of carousel in catalog app with scroll position at 0: 

[IMAGE] screenshot of carousel in catalog app with scroll position at 100: 

[VIDEO]
