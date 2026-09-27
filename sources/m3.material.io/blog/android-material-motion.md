# Building Beautiful Transitions with Material Motion for Android
Source: https://m3.material.io/blog/android-material-motion

> Introducing four transition patterns and how to add them to your app

Published: 2020-09-01

The Material motion system, recently released as part of the MDC-Android library (v 1.2.0), distills common transitions into a group of simple patterns for a smoother, more understandable user experience. Material motion currently includes four transitions:
- Container transform
- Shared axis
- Fade through
- Fade

We’ve implemented these transitions on top of the Android platform & AndroidX Transition system so they can easily be used when moving between Activities, Fragments, and Views.

This post introduces each pattern and explains how to add them to your app. I’ll illustrate each step by implementing it for our example app Reply, a simple and easy-to-use email client. Three of the app’s flows will get the motion transitions: **opening an email, opening the search page, **and** switching mailboxes**.

If you’re more of a hands-on learner and want to get right into the code, then consider doing the Material motion codelab, which lets you practice these techniques by executing each step as you go along.

## Container transform: Opening an email

The “hero” of transitions, container transform is used when one thing turns into another thing. Examples include a list item that expands into a details page, a FAB that morphs into a toolbar, or a chip that expands into a floating card. In each case there is one component transforming into another, maintaining a shared “outer” container while animating a swap of “inner” content. Using a container transform to animate between views can help reinforce their relationship and maintain a user’s navigational context.

In Reply, we’re adding a container transform between a Fragment holding a list of emails (`HomeFragment`) and an email details fragment (`EmailFragment`). If you’re familiar with Android shared element transitions, the setup is pretty similar!

Start by identifying our two shared element views and give them each a transition name. The first is a single email list item card where we will use Data Binding to make sure each item has a unique transition name.

The second is the full-screen card inside of our `EmailFragment`, which can be given a static transition name since it’s the only one in the view hierarchy. Note that our first and second shared elements don’t need to use the same transition name.

These two views will be picked up by our container transform. Under the hood, both will be placed inside a drawable whose bounds are clipped inside a “container” that animates its shape from a list item to a details page. During the transition, the container’s contents (the list item and the details page) are swapped by fading the incoming screen in on top of the outgoing screen.

Now that we’ve marked our shared element views, let’s create and set our destination Fragment’s `sharedElementEnterTransition` to a new instance of `MaterialContainerTransform`. By default, this `sharedElementEnterTransition` will also be automatically reversed and played when going back from the details page.

_For details on `MaterialContainerTransform` parameters, see the motion documentation._

Now when an email is clicked, all we need to do is supply our Fragment transaction with mappings between our start and end view transition names. With this information, the detail fragment’s shared element transition is able to find and animate the two views using our supplied `MaterialContainerTransform`.

_In the above snippet, we’re also setting an exit and reenter transition for the outgoing list fragment. Material Components provides two helper transitions to smoothly animate a Fragment that is being replaced: `Hold` and `MaterialElevationScale`. In addition to a fade, `MaterialElevationScale` will scale out our list of emails when exiting and scale them back in when reentering. `Hold` would simply keep our list of emails in place. Without setting an exit transition, our list of emails would immediately be removed and disappear from view._

If we were to run the code at this point and navigate back to our list of emails from the details page, the return transition wouldn’t run. This is because the transition system isn’t able to find the two views which are mapped to our transition names, since the email list adapter hasn’t yet been populated when our transition starts. Luckily, we have two handy methods at our disposal: `postponeEnterTransition` and `startPostponedEnterTransition`. These allow us to delay the transition until we know our shared elements are laid out and can be found by the transition system. In Reply, we can postpone until we are sure our `RecyclerView` adapter has been populated and our list items have been bound with transition names using the following snippet:

In your own app, you may need to experiment with these two methods to find the right time to start postponed transitions depending on how and when you’re populating your UI. If you find your return animation isn’t running, starting transitions before a shared element is ready is likely the cause.

Moving on to our search screen!

## Shared Axis: Opening the search page

The shared axis pattern is used for transitions between UI elements that have a spatial or navigational relationship. In Reply, opening search takes the user to a new page that sits on top of the list of emails. To illustrate this 3-dimensional model, we can use a shared z-axis transition between the list of emails (`HomeFragment`) and the search page (`SearchFragment`).

Shared axis transitions operate on two targets simultaneously to create the final, choreographed transition. This means that “pairs” of transitions run together to create a continuous, “directional” animation. For fragments, these pairs are
- FragmentA’s `exitTransition` and Fragment B’s `enterTransition`
- FragmentA’s `reenterTransition` and FragmentB’s `returnTransition`

`MaterialSharedAxis`, the class implementing the shared axis pattern, accepts a property called `forward` to control this concept of directionality. For each transition pair, `forward` must be set to the same value to have the pair’s animations coordinate correctly.

_See the motion documentation for more details on shared axis directionality._

In Reply, here’s how we can set up the exit and reenter transitions for our current fragment (`HomeFragment`).

In our destination fragment (`SearchFragment`), we set up the enter and return transitions.

Notice the current fragment’s exit transition and the search fragment’s enter transition use the same value for `forward` – true. The same goes for the current fragment’s reenter transition and the search fragment’s return transition.

Next, by default, transitions run on all child views within their scene root hierarchy. This means that a shared axis transition will be applied to each email in the list page and each child of the search page. This can be a neat feature if you want to “propagate” or “stagger” animations, but since we want to animate the root of each fragment as a whole, we need to set `android:transitionGroup="true"` on both our email list `RecyclerView` and our search page root view group.

With that, we should have a nice shared z-axis transition to and from our search page! Shared axis is a really flexible transition that can work in a lot of different scenarios – from page transitions, to smart reply selections, to onboarding or vertical stepper flows. Now that you have the set up configured, you can also experiment with `MaterialSharedAxis`’ s `axis` parameter to check out what the other axis animations look like.

## Fade Through: Switching mailboxes

The last pattern we’ll cover is the fade through pattern. A fade through can be used to transition between UI elements that do not have a strong relationship. When transitioning between mailboxes, we don’t want the user to think their sent emails are navigationally related to their inbox. Since each mailbox is a top-level destination, fade through is an appropriate choice. In Reply, we’ll be replacing the list of emails (`HomeFragment`) with a different list of emails (`HomeFragment` with new arguments).

Since `MaterialFadeThrough` doesn’t have the idea of directionality, the set up is even easier. We can just set an exit transition on our outgoing fragment and an enter transition on our incoming fragment.

The need to set **`android:transitionGroup="true"`** on our list of email’s `RecyclerView` also applies here, but this has already been taken care of during our shared axis configuration step.

That’s it for the fade through transition! Find fun places to use the fade through pattern in your own apps – like switching between bottom navigation destinations, swapping a list of items, or replacing a toolbar menu.

## Keep on moving!

That’s a brief look into the Material motion system on Android. There are lots of things you can do to customize motion using the patterns provided, making motion a part of your brand’s experience. Also keep in mind that while we looked at Fragment transitions, the motion system can be used to transition between Activities all the way down to Views. Check out the full motion spec for some inspiring examples to get you thinking about where you might be able topolish your app’s core experience or add some extra delight in small places.

To keep learning, check out the following additional resources:

Material Motion Developer Documentation

Lots of customization options and tips on animating between Activities and Views can be found in Material Android’s motion documentation!

Material Motion Codelab

A full, step-by-step developer tutorial covering how to add Material motion to Reply.

Google Drive for Android

Take a look at Google Drive on Android to see the motion system in action. Clicking on folders, opening search, and navigating between bottom navigation destinations all use transitions from MDC-Android.

[IMAGE] : 

[IMAGE] : 

[IMAGE] :
