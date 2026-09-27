# Build beautiful, premium, adaptive apps with Material
Source: https://www.youtube.com/watch?v=zRBi6oBtpoo

[music]
&gt;&gt; Hello everyone. It is so good to be here
with you.
Today we're going to talk about all the
exciting updates the material Android
team has coming down the pipeline.
And how these updates can help you build
beautiful, premium, and adaptive apps
with material.
For years, we've been on a journey to
redefine how we build UI on Android.
We've seen Jetpack Compose grow from an
ambitious idea into the engine behind
the world's most beautiful apps.
As Android's modern declarative toolkit,
it's designed to simplify and accelerate
UI development with less code and
powerful native tools.
I'm Kendrick, a software engineer on the
material Android team. And I've been
lucky enough to see this evolution
firsthand.
Today we're reaching a definitive
milestone in that journey.
One that marks a new chapter for Android
developers.
We've reached a major milestone with the
stable release of material views 1.14.
This release brings new expressive
components to the view system. But you
may have noticed a change in momentum.
While Compose continues to accelerate,
our updates for views have become more
focused.
That's because for the last few years,
we've been preparing for a fundamental
shift.
To tell you more about where we're going
next, I'll hand it over to Naomi.
Thanks, Kendrick. Hi everyone. I'm
Naomi, a software engineer on material
Android.
To build effectively for the future, we
have to focus.
As of today, we are officially all in on
Compose.
Moving forward, our team is
transitioning to exclusive support for
Jetpack Compose, and we'll be moving all
of our efforts to the material Compose
library.
This means that Material Views 1.14 will
be our final stable release for the
Views library.
We know how much you've built on Views,
but there has never been a better time
to migrate to Jetpack Compose.
If you've been waiting for the right
moment to go Compose first, this is it.
With the upcoming 1.5 release in the
second half of 2026, promoting M3
experimental APIs, Material 3 in Compose
is officially going stable.
In addition to Material 3 going stable,
we're also introducing our Material 3
expressive components.
M3 expressive APIs form an expansion
pack to M3.
You can opt in to using them to deliver
more premium experiences.
We've taken your feedback to heart, and
we're going to show you how we're making
Material 3 on Compose more expressive,
performant, and adaptable.
We'll be diving into new Material 3
expressive component features that bring
more personality and motion to your UI.
The Styles API, our new approach for
performance simplified state-based
styling.
Enhanced adaptive integration to help
your apps feel at home on any screen
size.
Last year, we showed tons of cool new
expressive components such as the
floating toolbar, new progress
indicators, button groups, and more.
Building on the success of last year's
expressive components, we're expanding
the kit to be more versatile than ever.
Our lists have evolved into two
variants, including a sophisticated
segmented visual style, and introduce
more interactivity by supporting clicks,
toggles, and different selection states
that automatically transform the
container.
The best part?
If you're already using the standard
list item, moving to a segmented look is
a seamless swap.
Let's look at these two list variants,
standard and segmented, in code.
First, we have our standard list.
Individual items are separated with
horizontal dividers, and each list item
has parameters for a selected state,
content, and icons.
Now, the segmented list.
By simply switching that list item out
for a segmented list item, and adding a
touch of gap spacing with the vertical
arrangement specification on the list
column,
the framework handles the rest.
It automatically manages the adaptive
corner shapes and color updates, giving
you a completely different visual DNA.
Search received a major visual refresh,
combining with the app bar to introduce
dedicated slots for navigation and
actions that live outside the search
container,
with exciting animations that make the
experience feel more interactive.
Let's walk through some code for the
search component.
We've introduced a new app bar with
search composable that introduces
dedicated slots for content before or
after the search bar.
Navigation icon provides a slot for
things like a back button, and the
action content slot provides a slot for
icons like filter or voice search.
We've also introduced new expanded
search bars like expanded docked search
bar with gap
that you can use in conjunction with app
bar with search to smoothly animate
between the two.
Building on the menu component, we're
introducing refined submenu motion and a
broader palette of shapes and color
styles. We've also added gaps, a new
layout feature that provides visual
breathing room and flexibility.
Now, let's take a moment to walk through
an example of using the updated menu
component.
We start with a flat list of drop-down
menu items.
While functional, a flat list quickly
gets crowded and lacks hierarchy as an
app grows.
To add structure, use drop-down menu
group.
This visually separates items like
modifications from navigation, with menu
defaults.group shape automatically
handling the rounded containers.
To make the menu expressive and
stateful, use the checked parameter and
checked leading icon. This swaps the
icon from outlined to filled when an
option is toggled, providing instant,
clear visual feedback.
You asked for more tactile inputs, so
we're delivering.
Meet the new, highly interactive time
picker, soon to be updated for our
expressive design system.
We're debuting a dedicated, scroll-based
input component that powers a new
tactile variant for time selection.
Designed as a standalone primitive, it
also gives you the power to bring that
same precision scrolling to any
number-based interface in your app.
All of these expressive components are
already available as experimental APIs
in the 1.5 alpha release.
What else is in the pipeline?
Well, we know you want your apps to feel
unique.
That's why we're integrating material
components with the new experimental
Jetpack Compose Styles API to enable
deeper customization than ever before.
This update will help you break out of
the grid, move past the defaults, and
build premium, engaging experiences that
reflect your brand-specific identity.
Style is a new paradigm for customizing
elements and components that evolves
beyond traditional modifiers.
It is designed to unlock deeper and
easier customization.
The Styles
offers significant benefits, improving
overall app performance by skipping the
composition phase during style updates
and simplifying the creation of cohesive
branded experiences.
You can check out the Building Custom
Design Systems with the new Styles API
talk to learn more.
We're integrating the Styles API with
Material Components to offer an easier
and wider degree of customizability than
before.
Here's a sneak peek.
Let's say you want a button and you want
it to have a navy blue background that
animates to a lighter blue when pressed.
Right now, to achieve this, there's a
lot of boilerplate you need, like
manually keeping track of the pressed
interaction state and creating a new
button colors class.
Now, let's say you want the background
to be gradient when pressed.
Currently, there's no out-of-the-box way
to do this with Material buttons.
With Styles, it's easy.
With the upcoming integration with the
Styles API, this will all be possible
simply by passing in a style that sets
these properties as input to the button
components.
Styles makes customization easier and
unlocks new customization that was
previously not supported.
These updates are coming soon to
Material Components.
Now, being premium isn't just about
aesthetics. It's about how your app
behaves across the entire ecosystem.
We know that building for foldables,
tablets, and desktops has historically
been a challenge.
The Material Adaptive library is now
integrated with Navigation 3,
simplifying the development of layouts
that scale across large screen devices.
For a long time, building adaptive
layouts meant switching navigation
frameworks, making it complex to manage
navigation into and out of adaptive
sections of your app.
To solve this, we've deeply integrated
the material adaptive library with
navigation three. This is a
game-changer.
It means your navigation logic and your
adaptive layouts are finally speaking
the same language. Scaling your UI from
a handheld phone to a large-screen
foldable is now a seamless, simplified
process within your compose code.
One core piece of this integration lies
in navigation three's scenes concept.
Instead of navigating between isolated
destinations, adaptive can benefit by
thinking in terms of visual scenes. With
the adaptive navigation three artifact,
we're making it easier to use the list
detail scene strategy. This is
essentially the brain that sits between
your navigation back stack and your UI.
It looks at the entries in your back
stack, and based on the available screen
real estate, automatically decides
whether to show a single pane or a
side-by-side list detail view.
To make this work, simply tag your
entries using list detail scene.list
pane or list detail scene.detail
pane.
Because the navigation library now
understands the intent of view screens
through that metadata, it handles the
heavy lifting for you.
And because this is built directly into
the navigation state, you get predictive
back support for free.
When a user swipes to go back from a
detail view on a foldable, the library
knows to transition the detail pane away
while keeping the list active. It's
fluid, it's intuitive, and most
importantly, it's one single source of
truth for your entire application.
This session isn't just about a library
update. We've listened to your feedback
and are committed to making material 3
more robust, more flexible, and
significantly easier to implement in
your daily workflow.
As you can see, we've been busy making
Compose expressive, performant,
customizable, and adaptive. You can
check out some of what we discussed
today in our guidelines, design kit,
alpha code, and API documentation.
If you're migrating from Views to
Compose, check out our guide to help you
out.
Or, if you want to learn more about
Styles API, read our overview. Both are
available on the Android developer site.
We're so excited to see what you build.
Read about Material going Compose first
on our work and feedback with us using
the hashtag M3 expressive
on social or by tagging @google design
on Instagram and X. Stay tuned. This is
just the beginning. We have more updates
coming to Compose in the months ahead.
Last but not least, if you enjoyed our
session, be sure to check out Bradley
and Liam's design talk, Make Material
Your Own.
Thank you.
&gt;&gt; [music]
