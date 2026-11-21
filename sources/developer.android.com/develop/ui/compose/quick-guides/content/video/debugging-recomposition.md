# Debugging recomposition in Compose
Source: https://developer.android.com/develop/ui/compose/quick-guides/content/video/debugging-recomposition

A look into debugging a performance issue in Jetsnack and how to fix it in Compose. Learn why deferring state reads by using a lambda means composition can be skipped.

## Key points
- The three phases of Compose are composition, layout, and draw.
- Compose can skip a phase if nothing has changed. Sometimes Compose will entirely skip composition to optimize performance.
- You can use a lambda modifier can skip composition.
- Skipping recomposition can help reduce jank during scrolling.
- The Layout Inspector in Android Studio is a good tool to help you debug recomposition issues.

## Resources
- Blog post
- Jetsnack on GitHub

## Collections that contain this guide

This guide is part of these curated Quick Guide collections that cover broader Android development goals:

###### Compose basics
This series of videos introduces various Compose APIs, quickly showing you what's available and how to use them.
Quick guide collection
