# Quick start
Source: https://developer.android.com/develop/ui/compose/setup

For the best experience developing with Compose, download and install Android Studio. It includes many smart editor features, such as new project templates and the ability to immediately preview your Compose UI and animations.

Get Android Studio

Follow these instructions to create a new Compose app project, set up Compose for an existing app project, or import a sample app written in Compose.

## Create a new app with support for Compose

If you want to start a new project that includes support for Compose by default, Android Studio includes various project templates to help you get started. To create a new project that has Compose set up correctly, proceed as follows:
- If you're in the **Welcome to Android Studio** window, click **Start a new Android Studio project**. If you already have an Android Studio project open, select **File > New > New Project** from the menu bar.
- In the **Select a Project Template** window, select **Empty Activity** and click **Next**.
- In the **Configure your project** window, do the following:
  - Set the **Name, Package name**, and **Save location** as you normally would. Note that, in the **Language** drop-down menu, **Kotlin** is the only available option because Jetpack Compose works only with classes written in Kotlin.
  - In the **Minimum API level** drop-down menu, select API level 21 or higher.

- Click **Finish**.

Now you're ready to start developing an app using Jetpack Compose. To help you get started and learn about what you can do with the toolkit, try the Jetpack Compose tutorial.

## Setup Compose dependencies and Compiler

See Setup Compose dependencies and Compiler.

## Try Jetpack Compose sample apps

The fastest way to experiment with the capabilities of Jetpack Compose is by trying Jetpack Compose sample apps hosted on GitHub. To import a sample app project from Android Studio, proceed as follows:
- If you're in the **Welcome to Android Studio** window, select **Import an Android code sample**. If you already have an Android Studio project open, select **File > New > Import Sample** from the menu bar.
- In the search bar near the top of the **Browse Samples** wizard, type "compose".
- Select one of the Jetpack Compose sample apps from the search results and click **Next**.
- Either change the **Application name** and **Project location** or keep the default values.
- Click **Finish**.

Android Studio downloads the sample app to the path you specified and opens the project. You can then inspect `MainActivity.kt` in each of the examples to see Jetpack Compose APIs such as crossfade animation, custom components, using typography, and displaying light and dark colors in the in-IDE preview.

To use Jetpack Compose for Wear OS, see Set up Jetpack Compose on Wear OS.

## Recommended for you
- Note: link text is displayed when JavaScript is off
- Navigation overview
- Testing your Compose layout
- React to focus
