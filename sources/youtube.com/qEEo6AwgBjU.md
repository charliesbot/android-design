# WearOS Material 3 shape morphing | Jetpack Compose Tips
Source: https://www.youtube.com/watch?v=qEEo6AwgBjU

Are you looking to add a little sh
morphing delight to your wos apps? If
so, you are in the right place. In this
episode, you'll learn how to implement
this cool new effects with compost
material 3.
[Music]
If you previously tried implementing
cash morphing effect in compose, you may
notice that it usually requires many
lines of code. This example shows how to
manually implement shape morph between a
triangle and a square. And then you need
to also implement the animation between
these two. In this example, we perform
an infinite morphing animation between
the two. But a lot of the time we'd also
like to tie into the interaction source
for button press morphing. It is quite a
lot of code for this effect.
But today I have not just one but two
pieces of good news for you. The first
one is that you can now bring the cool
shorphing effect into your Wos apps with
the new compost material tree for Wos
adding a delightful touch to your UI.
The second one is that there are a
number of compos material tree
components that greatly simplify
implementation morphing on.
Let's explore some way to use those.
First, round buttons. Components like
icon text icon toggle and text tole
buttons support variations that animate
when pressed or checked. In this
example, notice how the play button
morphs it shape when pressed. Next,
we'll dive into the button group, a new
material tree component that implements
an expressive group of buttons in a row
that shape morph when touched.
And finally, transforming lazy column, a
new lazy vertically scrolling list that
can be used to apply scaling and
morphing animations to elements in the
list. So the height and width of the
items morph as they get closer to the
top and bottom screen edges. Let's look
at some code now.
Fil icon button is one of their own
buttons in compose will use. You can add
shape morphing by simply using the
animated shapes method from the icon
button defaults.
And by simply adding this single line of
code, you can get a fun shame morphing
button like the one in this example.
The second component we want to look at
is button group. In a button group, you
can nest multiple buttons. So in this
case, we will use two field icon
buttons.
To achieve morphing, you need to declare
an interaction source for each button.
In this sample, we have two buttons, one
for play and one for adding to Q. So, we
are adding two interaction sources.
And then we use the animated width
modifier to add the interaction source,
which will adjust the width of the
button based on user interaction.
Note that this modifier is only
available within a button group scope.
The last step is to set the interaction
source to the one we have created above
for observing and the meeting
interaction for this button. With all of
that in place, now the two buttons
inside the button group change width
based on the interaction source.
Button group allows you to quickly add
shape morphing effects with three quick
steps.
creating an interaction source for each
button using the animate with modifier
and setting the interaction source
parameter. The last component for today
is transform column a new component from
1.5 compos
[Music]
with it. You can apply morphing effects
to the elements in the list. Eight and
width of the elements can morph as the
items get closer to the top and bottom
screen edges.
First step is to create a transformation
spec which defines visual transformation
of the item of a transform lazy column.
Here we are using remember
transformation spec which creates a
remembers a responsive transformation
spec for us. Then for each item of the
list, you use modifier transformed
height to calculate the changed height
that will be passed into the
transforming scope based on the
transformation spec. Lastly, you set the
transformation creating a new surface
transformation.
This will cause the container of the
component to dynamically change content
separately from the background.
Finally, this is the end result.
The list items shrink in height and
width when approaching the top and
bottom edges of the screen. This was
quick. We used a computed transformation
spec.
If you feel more creative, you can take
it a step further and create a custom
transformation spec for a transforming
lazy column.
For customizing a transformation spec,
first you create remember a
transformation spec as we did before and
then use the cotlins delegation feature
creating a new object that implements
the transformation spec interface.
You then override the apply container
transformation based on the score
progress of the list. Here we are trying
to rotate the list items as they
approach the bottom of the screen.
But you could also change the shape if
you want to morph into a different shape
while scrolling.
As before, we use modifier transformed
height to calculate and pass the height
of the item to the transforming laces
scope based on the transformation spec.
Then we use the graphic layer to apply
effects to content.
Note that since we want to rotate both
the button and the text inside it, we
apply the effect also the text
component. And that's it. You get a
custom transformation that makes the
bottom list item rotate as it approaches
the end of the screen.
We hope that today's episode inspire you
to add more delightful shape morphy to
your wear subs with compose by using
components such as round buttons. the
button group and the transforming lazy
column.
For more information, check out the link
documentation.
Happy composing.
[Music]
