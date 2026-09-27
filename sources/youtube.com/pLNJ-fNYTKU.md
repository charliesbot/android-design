# The future unfolds! How to optimize Android apps for adaptive layouts
Source: https://www.youtube.com/watch?v=pLNJ-fNYTKU

The future is unfolding. Foldables,
landscape foldables, and desktops
[music]
give users massive, beautiful screens,
but they also bring unique developer
challenges. Is your app ready? Let's get
it optimized. With the wave of new
premium devices hitting the market at
Samsung Galaxy Unpacked, there are even
more form factors for your app to
[music] meet users exactly where they
are. First up, drop your assumptions
about display orientation.
The natural orientation of the device
isn't always portrait, and it might even
vary based on user preference. Often,
the inner and outer displays of a
foldable have different pixel densities
and natural orientation,
&gt;&gt; [music]
&gt;&gt; in addition to different resolutions.
To help you build for the newest form
factors showcased at Galaxy Unpacked,
[music]
we published brand new guidelines
detailing development best practices for
trifolds and landscape foldables.
Pair that with our core adaptive app
quality guidelines to ensure your app is
excellent across every single screen
size.
&gt;&gt; [music]
&gt;&gt; And here are three quick ways to start.
First, always calculate the amount of
display space your app occupies using
libraries like Jetpack Window Manager or
Material 3 Adaptive's window size
[music] classes.
Second, update your camera previews. Use
Camera X and its preview view to let the
library handle sensor orientation and
scaling.
Third, maintain app continuity across
configuration changes by retaining the
state with view model or similar
approaches.
And remember, use adaptive layouts.
Adapting to new devices just got easier
with the latest Jetpack Compose update,
the experimental media query API to
detect device postures, and the grid and
flex box APIs for seamless [music]
dynamic layouts. Start building
adaptively today and deliver an
optimized user experience on [music]
every screen.
Check out our full playlist,
documentation, and skills linked below
to dive deeper. Start building, and we
can't wait to see what you create.
