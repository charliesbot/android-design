# Glance interoperability
Source: https://developer.android.com/develop/ui/compose/glance/interoperability

In some cases, you may want to use XML and `RemoteViews` to provide a view. Perhaps you have already implemented a feature without Glance, or the feature is not yet available or possible with the current Glance API. For these situations, Glance provides `AndroidRemoteViews`, an interoperability API.

The `AndroidRemoteViews` composable allows `RemoteViews` to be placed together with your other composables:

```
val packageName = LocalContext.current.packageName
Column(modifier = GlanceModifier.fillMaxSize()) {
Text("Isn't that cool?")
AndroidRemoteViews(RemoteViews(packageName, R.layout.example_layout))
}
```

Create and define the `RemoteViews` as you would without Glance, and pass it as a parameter.

In addition, you can create `RemoteViews` containers for your composables:

```
AndroidRemoteViews(
remoteViews = RemoteViews(packageName, R.layout.my_container_view),
containerViewId = R.id.example_view
) {
Column(modifier = GlanceModifier.fillMaxSize()) {
Text("My title")
Text("Maybe a long content...")
}
}
```

In this case, a layout that contains the "container" is passed with the defined ID. This container must be a `ViewGroup`, since it is used to place the defined content. **Note:** Any children of the defined container are removed and replaced with the content. Also, the provided `ViewGroup` must be supported by `RemoteViews.` See `RemoteViewsWidget.kt` for an example of using `AndroidRemoteViews`.

Previous  arrow_back   Implement a Glance theme

Next  Additional resources   arrow_forward
