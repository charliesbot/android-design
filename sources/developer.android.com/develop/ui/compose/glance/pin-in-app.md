# Pin Glance widgets in-app
Source: https://developer.android.com/develop/ui/compose/glance/pin-in-app

With Android 8.0 (API level 26) and higher, you can let users pin your widgets to their home screen within your app. Promoting widgets directly within your app is a great way to increase user engagement, especially after a user completes a related task, or when a user repeatedly accesses a feature in your app.

## Create a Pin Request

To initiate widget pinning, use the `requestPinGlanceAppWidget` method from the `GlanceAppWidgetManager` class. For apps running on lower versions of Android, this returns false. However, if the request is successfully sent to the system, this returns true.

Here is an example of how you can create a pin request:

```
@Composable
fun AnInAppComposable() {
val context = LocalContext.current
val coroutineScope = rememberCoroutineScope()
Button(
onClick = {
coroutineScope.launch {
GlanceAppWidgetManager(context).requestPinGlanceAppWidget(
receiver = MyWidgetReceiver::class.java,
preview = MyWidget(),
previewState = DpSize(245.dp, 115.dp)
)
}
}
) {}
}
```

In this example, `MyWidgetReceiver` is the class that receives the widget's callbacks, and `MyWidget` is the Glance widget you want to pin. The `successCallback` is a `PendingIntent` that is triggered when the widget is successfully pinned.

## Handle the Pin Request Response

When a user responds to the pin request dialog, your app receives a response. If the user accepts the request, your widget is pinned to their home screen, and the `successCallback` `PendingIntent` is triggered. If the user denies the request, nothing happens.

It is important to note that the `successCallback` is only triggered if the widget is successfully added to the home screen. It is not triggered if the user denies the request or if the launcher does not support pinning.

Previous  arrow_back   Add generated previews to your widget picker

Next  Handle errors with Glance   arrow_forward
