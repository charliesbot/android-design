# Material Design for XR (Developer Preview)
Source: https://m3.material.io/blog/material-design-xr-dev-preview

> Building UI that adapts for XR with familiar frameworks and tools

Published: 2024-12-12

Android is expanding into extended reality, so your app can be viewed in 3D space, whether in virtual reality or blended with the user's surroundings through passthrough.

Today, we’re releasing the Android XR SDK developer preview, a software development kit to let you build XR apps using familiar Android frameworks and tools. This release also includes Android XR design guidelines and developer documentation to help you create immersive experiences.

The Jetpack XR SDK allows you to enhance your existing Android app for XR and see it come to life in the Android XR emulator in Android Studio. Jetpack Compose for XR provides an easy onboarding to declaratively build spatial UI layouts, with established concepts such as rows and columns. Material Design for XR provides design guidance and code for spatial adaptations of components and layouts, to help you prepare your apps for this new environment.

In addition, we are launching advanced perception capabilities with ARCore for Jetpack XR, as well as SceneCore, our new 3D scene graph library for custom 3D manipulation.

## Android apps in XR

Android XR is a flexible platform that lets you bring your app into extended reality. Compatible Android mobile and large-screen apps will work with no development lift and are automatically available in the Play Store.

If you already have a mobile or large screen app on Android, the Jetpack XR SDK can extend your app into a new dimension by spatializing existing layouts and enhancing your experiences with 3D models and immersive environments. Learn how to bring your Android app into 3D with XR.

The Android XR design guidelines provide a roadmap for creating exceptional XR experiences. Explore key considerations, system foundations, and best practices for designing interactions, spatial UI, 3D content, and immersive environments. Learn how to develop UI with Jetpack Compose for XR.

To help you build integrated and boundless experiences, Android XR includes multimodal inputs, and spatial capabilities. Users can experience your app in two modes: Home Space and Full Space. In Home Space, a user is able to multitask with your app running side by side with other apps. In Full Space, your app takes center stage as the focus of the user's experience with full access to the spatial and 3D capabilities of Android XR.

       

## Material Design for XR

The Material 3 (or M3) library now includes adaptations to help your app stand out in spatial environments. We've also added design guidance for navigation components and layouts, making it easier to create immersive and user-friendly XR experiences.

When designing for XR, you may want to use spatial UI to place content in a user’s physical or virtual environment. You can break out your app into spatial panels, orbiters, and add spatial elevation.

Spatial panels allow you to take advantage of the infinite canvas that Android XR provides, breaking out your existing UI content into panels that are resizable and moveable by the user. Orbiters are floating UI elements that control the content within spatial panels. They give the app’s main content more space, and users can quickly access features while the content remains visible. Adding spatial elevation to a component raises it above the spatial panel on the Z-axis. This can help get a user's attention, create better visual hierarchy, and improve legibility.

Spatial UI behaviors in Material Design components and adaptive layouts will help your app adapt naturally to 3D space.

## XR adaptations

Android XR gives you the flexibility to design a spacious UI that adapts seamlessly to an unlimited display. If you’re building with the latest alpha of Material Design components and adaptive layouts, you may add the `EnableXrComponentOverrides` wrapper to opt into XR changes in your app. This includes placing navigation controls in orbiters—such as the navigation rail and navigation bar—as well as placing panes into spatial panels.

### Navigation rail

The navigation rail in any Compose layout including `NavigationSuiteScaffold` will automatically adapt to an XR orbiter. For more information, read Material Design guidelines.

       

### Navigation bar

Likewise, the navigation bar in any Compose layout including `NavigationSuiteScaffold` will automatically adapt to an XR orbiter. For more information, read Material Design guidelines.

       

### List-detail layout for XR

Compose Material 3 Adaptive Layouts in XR have a 1:1 mapping where each pane becomes an XR spatial panel. Learn more about `ListDetailPaneScaffold` and adaptive design guidelines.

       

### Supporting pane layout for XR

Compose Material 3 Adaptive Layouts in XR also have a 1:1 mapping where each pane becomes an XR spatial panel. Learn more about `SupportingPaneScaffold` and adaptive design guidelines.

       

## Start building with Material Design for XR

### Design with the Material 3 Design Kit for Figma

In the Figma design kit, you’ll be able to start designing with navigation components as orbiters.

Download the Material 3 Design Kit to get started

### EnableXrComponentOverrides to adapt your existing app

All M3 Compose UI inside of the `EnableXrComponentOverrides` wrapper will adapt on XR devices. Add the `EnableXrComponentOverrides` wrapper to adapt your app to Material Design for XR.

### Develop spatial UI with Jetpack Compose for XR

With Jetpack Compose for XR, you can declaratively build your spatial UI layouts using familiar Compose concepts such as rows and columns, including new components, spatial panels and orbiters. Start building with our new subspace and spatial components.

## Designing the future together

This is our initial developer preview of the Android XR SDK. We’re continuing to extend the design and developer tools you know and love to Android XR. We welcome your feedback, and want to make our developer platform work better for you.

Share your feedback

[IMAGE] Woman standing at a table with 3 apps open around her.: In Home Space, a user is able to multitask with your app running side by side with other apps.

[IMAGE] Woman standing in a virtual moon crater with an app open in front of her. The app has four spatial panels, an orbiter, and a 3D pyramid.: In Full Space, your app takes center stage as the focus of the user's experience with full access to the spatial and 3D capabilities of Android XR.

[IMAGE] Material adaptive layout adjusting between home space and full space modes: Material Design adaptive components and layouts for XR

[IMAGE] Home space app with navigation rail: Non-spatialized navigation rail

[IMAGE] Full space app with navigation rail: Spatialized (XR-adapted) navigation rail

[IMAGE] Compact layout in home space mode with nav bar: Non-spatialized navigation bar

[IMAGE] Expanded layout in full space with nav bar: Spatialized (XR-adapted) navigation bar

[IMAGE] List-detail layout in home space: Non-spatialized ListDetailPaneScaffold

[IMAGE] List-detail layout in full space: Spatialized (XR-adapted) ListDetailPaneScaffold

[IMAGE] supporting pane layout in home space: Non-spatialized SupportingPaneScaffold

[IMAGE] supporting pane layout in full space: Spatialized (XR-adapted) SupportingPaneScaffold
