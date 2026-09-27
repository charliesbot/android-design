# Make Material your own
Source: https://www.youtube.com/watch?v=HbAFGivZ158

[music]
&gt;&gt; Hi, I'm Bradley, a senior visual
designer on the material design team.
And I'm Liam, a senior interaction
designer on material.
Material design has evolved from a fixed
system into a flexible, customizable
foundation that empowers you to build
beautiful and thoughtful products and
truly make material your own. In this
session, we'll explore how product teams
can use our expressive design updates
and new customization capabilities to
build your own unique design systems on
top of material's research-driven,
accessible, and functional UX layer.
You will learn how to apply your bespoke
design language while staying anchored
to the systematic reliability that makes
material an industry standard.
Material is no longer an all-or-nothing
choice.
Join us to discover how to bring your
unique ideas to life without breaking
the material connection.
Since the beginning of material design,
we've provided a beautiful and usable
visual design language and a solid
design system that establishes
interaction patterns and behaviors on
Android and beyond.
At I/O 2014, there was a strong need for
a rigid design system with clear
instructions to establish norms and
consistency on Android and across apps.
Year over year, the material design
system has evolved with new ideas and
emerging product needs, adding
components, variations, and new
features. Most recently, with our
material 3 expressive design launches.
Now in 2026, design systems are mature
and teams are pushing product design in
exciting new directions.
However, material design is often still
seen as a fixed visual language, leading
designers to ask, "How can I truly
customize material to reflect my brand
identity?"
We started to answer this question by
introducing theming, a way to adopt
custom properties through a visual theme
for your app.
And we gave makers more choices through
our expressive design updates with brand
new components like toolbar and more
component variants, so designers had
more options to realize their design
ideas.
We saw a growing trend, makers wanting
to stretch our components as far as they
could go, and exploring more and more
custom UI solutions.
We heard you. This was a clear signal to
change how we deliver the design system
to support your ideas and keep them
connected to the material foundation.
So, we asked a question. What if we
evolve the design system itself, making
the component catalog flexible enough
that you could fundamentally make it
your own without sacrificing its core
reliability?
While designers and developers want to
adhere to Material's foundation, their
unique brand and new product ideas often
require visual directions that we can't
always predict.
Consequently, many developers base their
design systems on Material, but find
themselves forking our components to
achieve these specific design intents.
When you fork, you break your connection
to the Material foundation. This cuts
the component off from automatic updates
to essential features like
accessibility, functional logic, and new
behaviors all for the sake of a
stylistic change. For designers, this is
like detaching a component from a Figma
library.
Our goal is to reduce the need to fork
so you can create beautiful, premium,
and deeply branded apps while allowing
you to reuse as much of the Material
design system as possible.
Soon, we're integrating Material
components with the new experimental
Jetpack Compose styles API
to enable deeper, more powerful visual
customization than ever before.
These updates allow you to move beyond
Material defaults to build premium,
engaging experiences that reflect your
brand.
We're making customizability easier,
letting you bring your design language
into Material while staying anchored to
our functional foundation. Now, let's
walk through an example of how you can
make Material your own.
This is one of our Material studies,
Shrine.
Shrine uses a calming, muted peach color
palette, but the designer wants to
customize some of the app's components
to make them feel subtle and more
dimensional, like this promotional
button in the middle of the screen.
The designer loves the expressive shape
morph that comes with our buttons. He
wants to add custom shadows and a subtle
animated background that makes the
button feel more present in the
interface and help it draw a bit more
attention.
Since the button doesn't support these
effects out of the box, the engineering
team would currently have to fork the
component and build the functionality
from scratch.
When they fork, this component
immediately loses the ability to receive
future material updates.
The team is now burdened with
maintaining this custom component,
forcing them to manually rebuild all the
underlying functional and accessibility
logic the material design would provide
for free.
With styles, the team can now implement
the specific visual change without
forking.
They remain attached to material,
guaranteeing seamless future updates and
new features.
Next, let's look at another material
study, Achieve, an app for setting and
completing goals.
Its core brand focuses on motivating
users through fun and positive visual
feedback, making users feel rewarded
when they complete a task. This custom
aesthetic is fundamental to their user
experience.
Achieve's designers want completing a
task to feel special and highly
rewarding.
To achieve this, they need to replace
the standard material ripple effect with
a specific custom animation that is a
hallmark of their unique design system.
Using styles, they can create this
custom visual effect while maintaining
all of material's functional basics,
like pressed and focused states and
accessibility support.
Now the team can focus on creating a
high-polished custom visual that suits
their brand without having to rebuild
the component's foundations.
Here's another example. This is a sample
app, Clip, an audio recording app.
One hallmark of their design system is
the use of oversized graphic components
to create a bold look that's distinct
and easy to use.
With material design's recent expressive
update, the design team has new
components and more size variants to
experiment with to achieve the look
they're going for.
Previously, Clips designers were limited
to the size variants material provided,
and other components often had no size
variants at all.
Now with Styles, they can more easily
modify the size, shape, and interspacing
directly to create truly bespoke designs
that perfectly fit their bold aesthetic.
Soon you'll have the freedom to push
your brand and design language in bold
new directions without the fear of
breaking the material connection.
You'll be able to expand and evolve your
unique aesthetic while staying anchored
to the rigorous research, accessibility
standards, and functional reliability
that keeps material components best in
class.
You no longer have to choose between
standout brand identity and the built-in
usability gains of material.
With material components integration
with Styles, you'll get the ability to
create what you need without forking,
along with consistent accessibility and
usability.
These updates are coming soon to
Material Components. Follow along on our
Material Design site and learn more
about Styles and Jetpack Compose on our
Android developer site.
Please share your work and feedback with
us using the hashtag M3 Expressive on
socials or by tagging Google Design on
Instagram and X.
And be sure to check out the technical
deep dive by Material Engineering.
With these upcoming capabilities, we're
looking forward to seeing what you come
up with when you make Material your own.
&gt;&gt; [music]
