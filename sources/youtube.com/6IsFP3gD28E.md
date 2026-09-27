# Build next-level UX with Material 3 Expressive
Source: https://www.youtube.com/watch?v=6IsFP3gD28E

Hi, I'm Anisha Kminini, product manager
for Material Design. Today, I'm excited
to share the latest updates to Material
so you can design and build even more
premium, engaging, and usable products.
I'll introduce the update and the
research behind it. Then you'll get all
the juicy details from material designer
Andrew Louu and material engineer Connie
Shi. For those who don't already know,
material is Google's open-source design
system for building beautiful and usable
products. It's what we use to build
Google's apps from Gmail to Gemini, and
it's the official design language for
Android. Material 3 is our latest
version. It launched in 2021 with the
aim of adding even more flexibility,
color, and personality into products.
Now, we're doubling down on that
promise.
Today, we're introducing an expansion
pack of new components and capabilities
designed to add even more emotional
oomph to your UIs and ensuring your end
users have a deeper connection to your
product, find it easier to use, and get
a bit more joy from key
interactions. Okay, let's meet Material
3 Expressive.
[Applause]
[Music]
How do
you run?
[Music]
This update is named M3 Expressive
because expressive UIs have an emotional
impact. They express a mood through
unexpected shapes, eye-catching colors,
and new motion physics. They make you
feel something. At last year's Google
IO, we introduced a set of attributes
for understanding user perceptions of
interfaces, and many of them were
emotional words, playful, energetic,
creative, friendly, and positive.
Expressive designs consistently score
higher on these attributes on top of
already being useful, helpful, and
trusted. Here's an example of a
fictional media player. The
non-expressive version is on the left,
and the M3 expressive version is on the
right. The expressive version is more
visually engaging and fun, using our new
shape and color capabilities to draw
attention to the playback controls. And
the motion of the wavy indicator makes
the progress of the media play more
exciting. Here's another example, this
time from a Google product, Fitbit. You
can see how the original design is very
functional and readable. It's great, but
the M3 Expressive design uses the new
shape library, toolbar, and other
elements to strengthen the hierarchy and
guide your attention to important parts
of the screen. Even seemingly small
expressive changes can cause impressive
gains in clarity, usability, and user
delight. And we have the data to back it
up.
Over the past 3 years, we've been diving
deep into what makes a user experience
truly engaging. M3 Expressive is backed
by more user research than any previous
update to Google's design system ever.
We're talking 46 separate studies with
hundreds of designs tested and more than
18,000 participants from around the
world. So, what did all this research
tell us? People prefer M3 expressive
designs and are more likely to want to
switch to a product using these
components and techniques. And get this,
that preference is particularly strong
up to 87% among 18 to 24 year olds.
Across all age groups, expressive design
is favored. We also found that
expressive products are more usable.
participants spotted key UI elements up
to four times faster than in
non-expressive screens. What's really
cool is that it can also level the
playing field for users. Usability tests
typically find that older adults take
longer to visually locate key UI
elements, but with M3 Expressive, we saw
a dramatic erasure of the age gap. And
the larger buttons and clearer hierarchy
can make expressive UIs more usable for
people across a spectrum of movement and
visual abilities. You can learn more
about the research over on
design.google. And now I'm going to hand
it over to Andrew and Connie to dive
into the details.
Hi, I'm Andrew from materials UX design
team. and I'm Connie from Material's
engineering team and we're going to run
you through what exactly is in this
update. Starting with the fact that this
is not a new version of material. All
these updates complement existing M3
features. M3 expressive is birectionally
compatible. This means all existing and
new features can be used with both
material and material expressive theme.
So you can start experimenting with them
today. The new expressive APIs are
available as experimental in the
compose.mmaterial3 1.4 alpha libraries.
Support is also available in the view
system using
com.google.android material library
starting in 1.13 alpha 11. This talk
will focus on how to get started in
compose which is the recommended way to
build native Android UI. Let's dig in.
App makers told us they wanted more
component flexibility, and we heard you.
M3 Expressive includes 15 new and
improved components that are more
flexible and configurable than ever.
Starting with app bars and toolbars,
which together support navigation and
actions, previously known as top app
bar, app bar holds a screen's key
content and actions, such as its title
and navigation menu. It's now more
flexible, supporting multiple lines and
subtitles, images, and center alignment,
as well as medium and large sizes for
emphasis. App bar works together with
toolbar, a brand new component that
replaces our old bottom app bar. Where
app bar supports navigation, toolbar
provides critical actions for the
current page. Its versatile behaviors
support a wide range of use cases. For
example, the toolbar can dock to a
screen edge or float above
content. It can hold a variety of
controls like buttons and text fields
and also pair with a fab to emphasize a
hero
action. It can transform contextually
and use color to distinguish itself and
convey its
state. And it even offers a vertical
orientation to adapt to large windows.
You might have noticed toolbars already
in use in Google
Chat. Next, the classic button. This
component expands with an array of new
configurations. We now offer five
different sizes that expand the range of
emphasis, new squared and rounded
variants that provide distinction and
indicate state, and a range of icon
button widths to create visual interest
and hierarchy. Here's an example of some
of our new buttons coming soon to Google
Meet. Combined with existing color
options, these buttons give you more
room to mix and match styles for helpful
and engaging layouts. Let's try using
these expanded button capabilities in
code. Starting with a filled icon
button, let's change it to have a square
shape using the new shape parameter.
Now you can see the icon buttons
container is squarish instead of
round buttons now support shape morphing
animation on
interaction. For example, to animate the
shape to a square on press, we can use
the new shapes parameter that performs a
morph animation between different
interactive states.
For the commonly used small rounded icon
button shown here, we provide a
convenient default. On press, its shape
morphs to a square, then morphs back to
a circle on release. Next, let's change
the original filled icon button to be
extra large in size like you see on the
left by using the component default to
set its size, shape, and the size of the
content icon.
The three different recommended widths
can be similarly configured by passing
the desired width option to the
corresponding container size function.
Here we see how to create a wide rounded
filled icon button. You can see more
combinations of these configuration
options in the samples and demos linked
here.
The button family is growing even more
with split button. Long seen in Google
Workspace apps, this much requested
component is now officially part of the
material catalog. It overflows into a
menu of related actions. And just like
regular buttons, it offers a range of
sizes and styles. It can also live with
other actions inside a
toolbar. Next, we have button groups.
These join buttons together for toggles
or selections and automatically invoke a
shape morph animation for an expressive
touch. Standard groups relay independent
actions together while connected groups
create a selectable set. That means they
can replace the old segmented
button. Groups support the same wide
range of button sizes and shapes and
again are a great way to add expression
with shape morph builtin.
Look out for this component coming to
Google Meet.
Let's see how we can add our buttons
from earlier into the new button group
component. Like Andrew mentioned, the
standard button group can contain
buttons, icon buttons, or any
interactive component that accepts an
interaction source and applies the
animate with modifier. When one item is
pressed, its shape expands and squishes
its neighbors.
Button groups can also support children
with different width or
weights. When there's not enough room to
display all of its children, button
groups will provide an overflow
option. Now, for a classic material
component, the floating action button or
FAB, which also gets some new tricks.
Both FAB and extended FAB gain a new
medium size option, a perfect fit for
medium windows like foldables.
Both also gain three new color mapping
options, giving you more flexibility to
choose their emphasis on
screen. And finally, we've brought back
the speed dial from M2 as a new
component called FAB menu. It provides
easy access to related actions on tap
with an expressive unfurling motion
builtin.
Let me show you how the FAT menu
provides an intricate transition
animation encapsulated in a
straightforward API. A floating action
button menu animates between a toggle
floating action button and a column of
FAB menu items via the animate floating
action button modifier. In addition, the
animate icon modifier animates the icon
of the main FAB to reflect its toggle
state. A floating action button with
expressive vibrant colors can also be
used to expand to reveal the new
floating toolbar. To achieve this,
provide a horizontal floating toolbar
with a fab into a scaffold and apply a
floating toolbar vertical nested scroll
modifier to toggle its expanded
state. The new floating toolbar is also
vertically expandable.
Its body content is configured using
separate leading, trailing, and center
content slots in order to animate them
with respect to the center. Both
orientations can also be configured to
scroll off the screen. A quick update on
carousel, an earlier M3 component that
continues to embody expressive design.
We've invested further in carousel with
new support for text and a more
accessible overflow configuration.
Now for a brand new component,
introducing loading indicator. It's
specially designed to add an expressive
moment when content is loading in. As
you can see, it features exciting new
shapes we're introducing with this
release, which we'll cover more later
with styles.
The loading indicator can be used
standalone or inside of an existing pole
refresh component.
We've also updated the regular progress
indicator. Both linear and circular
formats gain options for track height
and an expressive new wavy style that
makes progress feel more alive and
active. You've probably already seen our
wavy progress indicators in places like
the media player on
Pixel. The new wavy progress indicators
provide advanced stroke and wave
customization capabilities. But to
create a wavy indeterminate circular
progress indicator, you can simply call
circular wavy progress
indicator. Finally, the navigation bar
in rail serving your app's core
navigation needs. The bar, which
supports up to five destinations, gets
revised colors that make the active item
more visible, and a flexible new layout
that adapts icons vertically or
horizontally to optimize for medium
windows.
We've also updated the navigation rail
for large windows like tablets. It now
features both collapsed and expanded
states, replacing the previously
separate navigation drawer with one
seamless, flexible
component. Again, of course, both the
bar and rail adapt to various screen
sizes
automatically. Let's look at this
updated navigation rail. When expanded,
the icon in a nav rail item will move
from above to in front of the label to
take advantage of the extra available
size. Nav rail also supports vertical
layout arrangements for the items via
the new arrangement parameter. The
navigation rail on bar components are
adaptive by default when used with the
navigation suite scaffold and its
related APIs.
These can be found in the compose that
material adaptive navigation suite
library. So we've improved our
components making them more flexible and
capable. But material 3 expressive is
also about the styles within those
components and throughout the
UI. And here we also have some big
updates across material's many style
systems. Let's start with shape. It's
evolved into a comprehensive library,
including 35 new iconic
shapes. Use these to add playful
expression and diversify and distinguish
elements like avatars or bespoke button.
The shapes can be accessed via the
material shapes API. But what about
basic shapes? We've updated those, too.
The shape scale expands to 10 radius
values and a new shape morph ability as
you've seen built into components like
buttons give shapes expressive motion.
Use these options to really craft your
UI, add visual interest, and enliven
states and interactions. You can see in
these examples that Gmail and Fitbit
will be taking advantage of the new
options. Now, shape morph is just one
part of an exciting update to motion.
Motion is now an official tokenized
system in material with out- of-the-box
motion physics that you can customize
for your product. Instead of hard-
coding animations, you can pick from
pre-made motion spring tokens, making
your motion higher quality and much
easier to implement. Animations used to
require complex easing and duration
specs. Now, physics-based springs
produce fluid and natural motion with
simple and time-saving tokens for both
spatial transitions and visual effects.
You can easily change their speed with
faster or slower springs and adjust
their feeling with a standard or
expressive motion scheme. Use springs
and motion schemes across your UI to
make interactions come alive, including
with motion already built in to over 21
of our components.
A motion scheme contains two overall
specification for motion spatial and
effects. For each of those specs, there
are default fast and slow animation
specs that are available to use. You can
configure the motion scheme for all
components in the material theme by
overriding it in your theme. By default,
there are two predefined motion schemes,
standard and expressive. These schemes
use spring animation specs under the
hood. They can be used as is or
customized. Here, my snappy motion
scheme changes all the specs to snap
immediately. Here we see the floating
action menu items appear immediately in
their final width.
You can also apply the motion scheme to
your own customuilt components by
accessing the material theme that motion
scheme
specs. On the color front, M3 Expressive
brings a host of updates that enrich
colors throughout your app. To start,
text and icons are brighter to better
express colors like brand or dynamic
themes. And those themes get richer and
more diverse because we've upgraded
dynamic color to unlock more variation
and higher chroma across all hues. That
means a greater range of style and
personalization for users, whether they
prefer soft neutrals or juicy vibrance.
Apps adopting dynamic color get these
upgrades automatically in the next
version of Android.
Whether you use dynamic theming or
customize your own static theme, take
advantage of the full material scheme to
achieve rich and functional colors in
your UI. That means using a variety of
primary, secondary, and tertiary accent
colors for hierarchy and distinction and
using tonal surface colors within
thoughtfully grouped and contained
content to create nuance hierarchy that
guides users through the screen.
Light expressive color scheme has the
updated uncontainer colors which affect
content colors such as text and icon.
The surface container color roles which
for recently added could also be
accessed via the color scheme API. If
you already use our dynamic color
functions, no additional action is
needed to benefit from the
aforementioned updates.
Just switch your theme from material
theme to material expressive theme and
use expressive light color
scheme. When it comes to typography,
we've extended the type scale with new
emphasized styles that offer a stronger
weight. Use them to finesse hierarchy
and for example give extra impact to a
headline or subtly strengthen text of
the same size. They also give you room
to customize your fonts for expression
or brand. Google Fonts offers a huge
library of type faces to do so,
including variable fonts that enable
truly unique designs. They're especially
useful for bespoke editorial feeling
type that draws attention. Now, remember
that all these features play well
together. Combine type, shape, color, or
flexible components, and more to not
only create an expressive visual style,
but build powerful hierarchy that
directs users focus and makes your app
more functional,
too. In particular, try layering on
these features in hero moments. These
are places that highlight special
functionality or frame content in a
delightful way. In these places, the
full breath of materials, styles, and
components can help you create an
especially expressive moment that
celebrates the unique value of your
product. And now back to
Nisha. Phew, that's a lot. You can of
course explore everything we've talked
about and more in our guidelines, design
kit, alpha code, and API documentation.
We are really excited to see everything
you make. Share your work with us and
your feedback by using the hashtag
M3Expressive on socials or by tagging
Google on Instagram and X. And keep an
eye out for expressive updates making
their way into Google's apps in the
coming months. Thank you.
[Applause]
