# Adopt Compose for teams
Source: https://developer.android.com/develop/ui/compose/adopt/for-large-teams

###### Technical decision makers: Adopt Compose for your teams

Jetpack Compose is a declarative UI toolkit that accelerates Android app development, helps developers be more productive, eliminates common bugs, and enables intuitive app design.

Android is invested in the continued development of Jetpack Compose as many developers love it, including Googlers. Several Google teams are using Jetpack Compose, including the  Play Store.

Guide

###### Start with Kotlin
Compose makes heavy use of many of the great language benefits of Kotlin, and we suggest starting to migrate to Kotlin before jumping in with Compose.

Guide

###### Migrate to a declarative approach
The industry has been shifting toward declarative UI frameworks, such as Jetpack Compose, which eliminate common bugs and simplify adding animations.

Learning pathway

###### Start learning as a team
Start learning together using our in-depth course that covers intermediate and advanced topics, and encourage knowledge sharing along the learning journey.

Guide

###### Design to high standards
Compose components adhere to Material Design (or to your own custom design system), and Compose supports themes.

###### Three approaches to integrating Compose
Compose is fully interoperable with Android's view system so you don’t have to completely rewrite your app to benefit from Compose. This allows you to take advantage of your existing resources, and gives you flexibility on how to add Compose into an existing app. There are three common approaches to consider:

###### Write new features in Compose.
The most common approach is to start by writing new features in Compose. This approach has a lot of flexibility because you can use Compose for complete new screens, all the way down to an individual piece of UI such as a button. Twitter took this approach for their Communities feature.
See Twitter’s Compose story

###### Replace simple screens.
Identifying a few of the more simple screens in your app as a starting point for migration is an easy way to start taking advantage of the benefits of Compose. This is an approach Monzo took when they were starting to dive into Compose.
See Monzo’s Compose story

###### Redesign your UI.
If you're already planning a major UI redesign for your app, it might make more sense to do the full UI update in Compose. Typically teams build out UI components in Compose and then create screens from those components. The Mercari team took this approach.
See Mercari’s Compose story

###### Animate with a few lines of code
Compose supports linear and tweened animations, animations with custom keyframes, and even dynamic spring animations.
Discover Compose animations

###### Preview in Android Studio
With composables, you can set and review properties like size, locale, or light and dark mode as you work.
Learn about Preview

###### See changes in real time
With Live Edit, you can see changes you make to a composable immediately on an emulator or device – there’s no need to rebuild your app.
Learn about Live Edit

###### Test with semantics
Because Compose defines your UI with functions, you can test screens to buttons with the same APIs. Confidently significant updates to your app and test to confirm your features still work.
Test in Compose
