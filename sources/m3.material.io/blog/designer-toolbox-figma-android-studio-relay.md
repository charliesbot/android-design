# The designer’s toolbox
Source: https://m3.material.io/blog/designer-toolbox-figma-android-studio-relay

> How Material resources work together, from customizing the design kit to Android Studio and Relay

Published: 2023-05-26

Material Design is an open source design system for creating beautiful projects at Google scale, and we’ve been busy making resources tools for you to use Material Design all from your Figma canvas. Read on about how to get the most out of the Figma design resources.

## Make the Design Kit your own

The M3 Figma design kit is a comprehensive tool for getting started with Material Design. Evolving from a Sketch stickersheet into a robust resource with guidance and prototyping.

The Material Design system is adaptable in use, from a foundation to your own system to only the needed components. So the design kit can be utilized in the same manner: duplicate or remix it for what your product needs. Here are some more ways to utilize the design kit effectively:

- Copy components out of the kit as needed for your app to use as local components.
- Start ideating within the kit, then curate as needed. This will enable you to spin up and test product ideas quickly and more realistically.

- Duplicate the M3 kit file and publish to your team as a component and style library.
- Theming or updating the styles can dramatically shift the look and feel!
- Material includes three theming systems (color, type, shape), but you may have other style attributes. Add them as styles and connect to components.
- Tricks for using components (maybe talk about the building blocks or structure)
- The building blocks are hidden on most components, but can be revealed to show how they are built out at a system level.

## Start building

The design kit is a great place to dive in to learn about M3 components or start building your app.

Whether starting within the M3 design kit or starting fresh, you’ll need to first set up your frames to start designing.

From here, you can use the kit to start blocking in features along with content. Building up from rough sketches to higher fidelity mockups swapping for real components as you go.

The design kit and Material Theme Builder (MTB) work together to help you efficiently design and build products, so you can focus on your users and features. By duplicating the design kit to your own Figma account, you get ready-to-use design components set up with Material tokens, and quickly customizable with the Theme Builder.

**Tips and tricks for working with Material Theme Builder and the design kit**
- Manage your Figma styles to add or remove themes – for example, you may want to generate a custom theme and then remove the pre-existing baseline purple theme of the file. The more Figma styles, the larger your Figma files, so add new themes as needed.
- You can select all the components on the components page to swap them to your custom palette as it generates – two birds, one plugin operation
- Certain generation features are off by default to speed up performance. Turn on features like state layer and surface updating within settings to fully customize everything in the design kit.

- Take advantage of the strengths of both: utilize the already existing elements (like the color schematic) and styles of the design kit, and use the Theme Builder to create and swap new styles and swap
- If you find the Theme Builder isn’t operating as expected, here are some common ways to troubleshoot:
  - If swapping doesn’t seem to be working, try selecting smaller groups or layers. The design kit and plugin have a lot of pieces to them! Sometimes Figma takes a little bit longer to load.
  - Close the plugin if it times out, styles are generated first and needed to swap.

For a more in-depth tutorial on using the Material Theme Builder to create a custom theme and apply it to Material components, dive into the Customizing Material color lab on Figma. With this Figma project, you’ll learn how to create a custom theme with the Material Theme Builder and apply that theme to design mockups.

## Need inspiration?

If you are looking for some more inspiration and samples, check out some sample projects. Like Now in Android, which is the Figma file for the published Now in Android app and gives a thorough view of the app in multiple form factors and themes. Or Pesto, a study for advanced theming in Flutter.

Many of the samples are built with common layout patterns in mind, which can be used as a starting point for your own app. Some even have matching code samples!

For a more guided approach, try out one of the design labs, they come with sample screens and a walk through of various topics, including dynamic color and variable fonts.

## Outside the design system

Of course any product will extend past the app with app icons, notifications, and other home screen experiences. The newly released Android UI Kit helps you craft your app’s full user journey.
- The Android UI kit helps create full Android user flows to extend your app past the core app experience.
- The kit is divided into components for the system UI elements and templates for full system UI screens.
- Build a notification from one of the template types and copy into the Lockscreen and Notification shade templates.
- Use the Adaptive app icon template to design and preview your app icon for adaptive shape and color, plus legacy.

## Into code

With your app built in Figma, we can take a step into implementation by using Relay! Relay packages are like supercharged Figma components that can be used directly in Jetpack Compose projects.

Some tips to use Relay effectively:
- Each UI package represents a Figma component that can be translated to one composable.
- Packages not only reflect what is on your canvas but also capture additional design intent beyond Figma component properties, like interaction and parametrization.
- Create a named Figma version to keep package definitions stable. You can do this easily in the “Share with Developer” screen.
- Take a second to give important layers and properties meaningful names.
- Fix any blocking errors to ensure packages build correctly for the developer.
- Talk to your developer about implementing Relay style and component mapping. These features transform your Figma styles and design system components into custom Compose code.
- Work with your developer to make sure your packages have the parameters they need. This is a great way to get on the same page about UI components!

We’re evolving the Material tools and resources to help you create beautiful products with your input. If you have any questions, feedback, or bugs please let us know @MaterialDesign on Twitter, or link for DK, or goo.gle/MTB-submit-issue for MTB.

Go get started with the Material 3 design kit and try out the Material Theme Builder in Figma.

[IMAGE] Collection of components in figma: 

[IMAGE] Themed components in figma: 

[IMAGE] example figma boards: 

[IMAGE] low-fi wireframes: 

[IMAGE] mockups with the theme editor overlaid: 

[IMAGE] animated prototype: 

[IMAGE] themebuilder workflow: 

[IMAGE] theme builder settings: 

[IMAGE] cover for the customizing material color design lab: 

[IMAGE] : 

[IMAGE] pesto sample screenshots: 

[IMAGE] Android UI kit: 

[IMAGE] Relay settings:
