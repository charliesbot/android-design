# Accessibility in Compose
Source: https://developer.android.com/develop/ui/compose/quick-guides/content/video/accessibility-in-compose

Add accessibility features to your app, transforming what's shown on screen to a more fitting format for users with specific needs. See how to increase your app's reach and versatility with a small amount of work.

## Key points
- Include descriptions of the visual elements so the accessibility services know what to do. Use a localized string resource for content descriptions so users can hear them in their own language.
- Add a semantics modifier to the parent container to improve the selection behavior of the accessibility service.
- To add a custom action to a list item: define the name of the action based on the current status, add a semantics modifier and set the `customActions` property, and clear any additional semantics.

## Collections that contain this guide

This guide is part of these curated Quick Guide collections that cover broader Android development goals:

###### Display text
Text is a central piece of any UI. Find out different ways you can present text in your app to provide a delightful user experience.
Quick guide collection

###### Display images
Discover techniques for using bright, engaging visuals to give your Android app a beautiful look and feel.
Quick guide collection

###### Compose basics
This series of videos introduces various Compose APIs, quickly showing you what’s available and how to use them.
Quick guide collection
