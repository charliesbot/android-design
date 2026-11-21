# Create a progress-centric notification
Source: https://developer.android.com/develop/ui/compose/notifications/progress-centric

[IMAGE] top shade progress-centric notification

**Figure 1.** A progress-centric notification at the top of the shade.

Android 16 introduces a new notification template to help users seamlessly track user-initiated start-to-end journeys. These notifications have upgraded visibility on system surfaces and top ranking in the notification drawer.

Use `Notification.ProgressStyle` to stylize progress-centric notifications. Key use cases include rideshare, delivery, and navigation. Within that class, you can denote states and milestones in a user journey using Points and Segments.

## Relevant classes

The following classes contain the different APIs that you use to construct a `ProgressStyle` notification:
- `Notification.ProgressStyle`
- `Notification.ProgressStyle.Point`
- `Notification.ProgressStyle.Segment`

## Anatomy and customization

The following images show the different parts that make up `ProgressStyle` notifications:
**Figure 2.**
|

A. Header - Subtext  |

`Notification.Builder#setSubText()`
|

B. Header - Time  |

`Notification.Builder#setWhen()`
|

C. Content Title  |

`Notification.Builder#setContentTitle()`
|

D. Content Text  |

`Notification.Builder#setContentText()`
|

E. Progress bar  |

`Notification.ProgressStyle`
|

F. Action button  |

`Notification.Builder#addAction()`
**Figure 3.** Apps can set a vehicle image for the tracker icon and use segments and points to denote the rideshare experience and milestones.

## Best practices

Use the right APIs and follow best practices to provide the best user experience for progress updates.
- Set the right fields to meet promoted visibility in the notification shade.
- Use the right visual elements to guide users. For example, rideshare apps should set a vehicle image and use the most accurate color of the vehicle in the notification using `Notification#setLargeIcon`.
- Use concise and clear language to define the progress of the user journey. Time of arrival, driver name, and state of the journey are important text that the notification should communicate.
- Provide useful and relevant actions in the notification that streamline the user journey. For example, providing "Tip" and "Add dish" to a newly initiated food delivery order are useful actions before delivery.
- Use segments and points to denote states. For example, segments can colorize the state and duration of traffic in a rideshare journey. Points represent states for milestones such as food preparation, delivery, and passenger pickup.
- Update the progress experience to accurately reflect the actual progression of the journey. For example, changes in traffic conditions can be reflected in changes in segment colors and updates in text.

The following code snippet shows how a `ProgressStyle` notification could be used for a rideshare context:

```
var ps =
Notification.ProgressStyle()
.setStyledByProgress(false)
.setProgress(456)
.setProgressTrackerIcon(Icon.createWithResource(appContext, R.drawable.ic_car_red))
.setProgressSegments(
listOf(
Notification.ProgressStyle.Segment(41).setColor(Color.BLACK),
Notification.ProgressStyle.Segment(552).setColor(Color.YELLOW),
Notification.ProgressStyle.Segment(253).setColor(Color.WHITE),
Notification.ProgressStyle.Segment(94).setColor(Color.BLUE)
)
)
.setProgressPoints(
listOf(
Notification.ProgressStyle.Point(60).setColor(Color.RED),
Notification.ProgressStyle.Point(560).setColor(Color.GREEN)
)
)

```

See the sample app to experiment with these APIs.
