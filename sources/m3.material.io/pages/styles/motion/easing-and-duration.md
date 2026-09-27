# Easing and duration
Source: https://m3.material.io/styles/motion/easing-and-duration

> Easing and duration create responsive and expressive motion

## Applying easing and duration

star

Note:
In the expressive update, components and motion now use the motion physics system, which uses springs. Products should migrate to the new system. The easing and duration system is still used for transitions and can be used by teams that haven't yet updated to GM3 Expressive, but is no longer maintained.

#### Suggested easing and duration pairs

Choosing the right combination of easing and duration can be complicated. As a simple starting point, these are sensible defaults that will work for most transitions.

| **Easing**  | **Duration** | **Transition type**
| Emphasized | 500ms | Begin and end on screen
| Emphasized decelerate | 400ms | Enter the screen
| Emphasized accelerate | 200ms | Exit the screen
| Standard | 300ms | Begin and end on screen
| Standard decelerate | 250ms | Enter the screen
| Standard accelerate
| 200ms | Exit the screen

#### Easing

In the physical world, objects don’t start or stop instantaneously. Instead, they take time to speed up and slow down. Transitions without easing look stiff and mechanical, while a transition with easing appears more natural.

[VIDEO] Motion curve with and without easing.

_- A transition with easing
- A transition without easing_

Compared to the utilitarian style of M2, M3 easing is more expressive. Transitions have snappy take offs and very soft landings.

Durations are slightly longer compared to M2. This gives transitions time to come to a gentle rest without feeling abrupt.

[VIDEO] Comparison of M2 and M3 easing curves.

_- M2 easing and duration
- M3 easing and duration_

##### Choosing an easing set

The **Emphasized easing set** is recommended for most transitions to capture the style of M3.

The **Standard easing set** can be used for small utility focused transitions that need to be quick. The Standard set is also a fallback for platforms that don't support Emphasized easing, like iOS and Web.

[VIDEO] Expanding card in a note taking app.

_Emphasized easing is used for this full screen transition_

[VIDEO] Text field selection in an email app.

_Standard easing is used for this Text field transition on Web. The simple style fits the utility of this component._

##### Choosing an easing type

Easing types are chosen based on how a transition moves in relation to the screen.

###### Begin and end on screen

These transitions use **Emphasized** easing. It speeds up quickly and then comes to a gentle rest in order to emphasize the end of the transition.

[VIDEO] Card expanding in a podcast app.

_This transition begins and ends on screen so it uses Emphasized easing_

###### Enter the screen

These transitions use **Emphasized decelerate** easing. It begins at peak velocity then comes to a gentle rest.

###### Exit the screen permanently

These transitions use **Emphasized accelerate** easing. It begins at rest and ends at peak velocity. By ending at peak velocity, it gives the impression the exiting component cannot be retrieved.

[VIDEO] Card rising from bottom of screen, then retreating quickly back to bottom of screen after being exited.

_This Bottom sheet enters with Emphasized decelerate and exits permanently with Emphasized accelerate_

###### Exit the screen temporarily

These transitions use **Emphasized** easing. By ending at rest just off screen, it gives the impression the exiting component can be retrieved.

[VIDEO] Calendar menu is temporarily collapsed to the left.

_This drawer enters and exits temporarily with Emphasized easing_

#### Duration

Transitions shouldn’t be jarringly fast or so slow that users feel as though they’re waiting. The right combination of duration and easing produces smooth and responsive transitions.

[VIDEO DO] Clock icon is expanded to smoothly take over the screen.

**DO:** _A transition with a well tuned duration is quick and easy to follow_

[VIDEO DON'T] Clock icon is expanded rapidly, in abrupt fashion, to take over the screen.

**DON'T:** _Avoid transitions with such a short duration they become jarring_

##### Choosing a duration

Durations are chosen based on these criteria:

###### Transition size

Transitions that cover small areas of the screen have short durations. Those that traverse large areas have long durations. Scaling duration with the size of a transition area gives a consistent sense of speed.

[VIDEO] A series of radio buttons are selected on the Settings screen.

_This transition covers a small area with a short 200ms duration_

[VIDEO] An album is selected that takes over the screen.

_This transition covers a large area with a long 500ms duration_

###### Enter vs. exit transitions

Transitions that exit, dismiss, or collapse an element use shorter durations. Exit transitions are faster because they require less attention than the user’s next task.

Transitions that enter or remain persistent on the screen use longer durations. This helps users focus attention on what's new on screen.

[VIDEO] Pop up screen on an email draft has option to delete or cancel.

_- An Enter transition has a long duration of 500ms
- An Exit transition has a short duration of 200ms_

[VIDEO] Bottom sheet uses a longer animation duration to enter and a shorter duration to exit the screen.

_- An Enter transition has a long duration of 500ms
- An Exit transition has a short duration of 200ms_

## Tokens & specs

star

Note:
In the expressive update, components and motion now use the motion physics system, which uses springs. Products should migrate to the new system. The easing and duration system is still used for transitions and can be used by teams that haven't yet updated to GM3 Expressive, but is no longer maintained.

#### Tokens

Motion easing and duration can be implemented using easing and duration tokens. Learn more about design tokens

#### Easing

##### Emphasized easing set

This set is the most common because it captures the expressive style of M3.

[VIDEO] A line graph illustrating an emphasized easing pattern.

_Emphasized_

[VIDEO] A line graph illustrating an emphasized decelerate easing pattern.

_Emphasized decelerate_

[VIDEO] A line graph illustrating an emphasized accelerate easing pattern.

_Emphasized accelerate_

| Info/Platform | Emphasized | Emphasized decelerate
| Emphasized accelerate

| Token | md.sys.motion.easing.emphasized | md.sys.motion.easing.emphasized.decelerate | md.sys.motion.easing.emphasized.accelerate
| Android | pathInterpolator(M 0,0 C 0.05, 0, 0.133333, 0.06, 0.166666, 0.4 C 0.208333, 0.82, 0.25, 1, 1, 1) | PathInterpolator(0.05f, 0.7f, 0.1f, 1f) | PathInterpolator(0.3f, 0f, 0.8f, 0.15f)
| CSS | N/A (Use Standard as a fallback) | cubic-bezier(0.05, 0.7, 0.1, 1.0) | cubic-bezier(0.3, 0.0, 0.8, 0.15)
| Flutter | easeInOutCubicEmphasized | Cubic(0.05, 0.7, 0.1, 1.0); | Cubic(0.3, 0.0, 0.8, 0.15);
| iOS | N/A (Use Standard as a fallback) | ControlPoints:0.05f:0.7f:0.1f:1.0f]; | ControlPoints:0.3f:0.0f:0.8f:0.15f];
| After Effects | Use After Effects Easing Panel (download)

##### Standard easing set

This set is used for simple, small, or utility-focused transitions.

[VIDEO] A line graph illustrating a standard easing pattern.

_Standard_

[VIDEO] A line graph illustrating a standard decelerate easing pattern.

_Standard decelerate_

[VIDEO] A line graph illustrating a standard accelerate easing pattern.

_Standard accelerate_

|
| Standard | Standard decelerate | Standard accelerate
| Token | md.sys.motion.easing.standard | md.sys.motion.easing.standard.decelerate | md.sys.motion.easing.standard.accelerate
| Android | PathInterpolator(0.2f, 0f, 0f, 1f) | PathInterpolator(0f, 0f, 0f, 1f) | PathInterpolator(0.3f, 0f, 1f, 1f)
| CSS | cubic-bezier(0.2, 0.0, 0, 1.0); | cubic-bezier(0, 0, 0, 1); | cubic-bezier(0.3, 0, 1, 1);
| Flutter | Cubic(0.2, 0.0, 0, 1.0); | Cubic(0, 0, 0, 1); | Cubic(0.3, 0, 1, 1);
| iOS | ControlPoints:0.2f:0.0f:0.0f:1.0f | ControlPoints:0.0f:0.0f:0.0f:1.0f | ControlPoints:0.3f:0.0f:1.0f:1.0f];
| After Effects | Use After Effects Easing Panel (download)

#### Duration

##### Short durations

These are used for small utility-focused transitions.

| **Token** | **Value**
| md.sys.motion.duration.short1 | 50ms
| md.sys.motion.duration.short2 | 100ms
| md.sys.motion.duration.short3 | 150ms
| md.sys.motion.duration.short4 | 200ms

[VIDEO] Animation showing a 200ms duration and standard easing curve applied to selection control interactions.

_Selection controls have a short duration of 200ms with Standard easing_

##### Medium durations

These are used for transitions that traverse a medium area of the screen.

| **Token** | **Value**
| md.sys.motion.duration.medium1 | 250ms
| md.sys.motion.duration.medium2 | 300ms
| md.sys.motion.duration.medium3 | 350ms
| md.sys.motion.duration.medium4 | 400ms

[VIDEO] Animation showing a FAB expanding into a sheet with a 400ms duration and Emphasized easing.

_A FAB expanding into a Sheet uses a 400ms duration with Emphasized easing_

##### Long durations

These durations are often paired with Emphasized easing. They're used for large expressive transitions.

| **Token** | **Value**
| md.sys.motion.duration.long1 | 450ms
| md.sys.motion.duration.long2 | 500ms
| md.sys.motion.duration.long3 | 550ms
| md.sys.motion.duration.long4 | 600ms

[VIDEO] Animation showing a card expanding into a full screen with a 500ms duration and emphasized easing.

_A Card expanding to full screen uses a long 500ms duration with Emphasized easing_

##### Extra long durations

Though rare, some transitions use durations above 600ms. These are usually used for ambient transitions that don't involve user input.

| **Token** | **Value**
| md.sys.motion.duration.extra-long1 | 700ms
| md.sys.motion.duration.extra-long2 | 800ms
| md.sys.motion.duration.extra-long3 | 900ms
| md.sys.motion.duration.extra-long4 | 1000ms

[VIDEO] Animation showing the transition of an ambient carousel auto-advancing with a 1000ms duration and emphasized easing.

_An ambient carousel auto-advance transition uses an extra long 1000ms duration with emphasized easing_
