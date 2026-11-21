# Iterative code development
Source: https://developer.android.com/develop/ui/compose/tooling/iterative-development

As a mobile developer, you're often developing your app's UI step by step rather than developing everything at once. Android Studio embraces this approach with Jetpack Compose by providing tools that don't require a full build to inspect, modify values, and verify the final result.

## AI-assisted UI iteration

You can use the AI agent in Android Studio to iteratively refine and transform your Compose UI directly from the design preview. Right-click on a Compose Preview and select one of the following **AI Actions**:
- **Match UI to target image**: Select to upload a reference design mock. The agent then suggests code changes to make your implementation match the design as closely as possible.

**Figure 1.** Update the UI to match a target image.
- **Transform UI**: Use natural language prompts (for example, "change the button color to blue") to describe specific modifications you want to make to your UI.

**Figure 2.** Ask the agent to transform the UI.

## Live Edit **Important:** This feature is in active development, so you might experience some unstable behavior. In Android Studio Giraffe and higher it requires Jetpack Compose Runtime 1.3.0 or higher and AGP 8.1.0-alpha05 or higher. Google is continuously working to improve this feature and welcomes your feedback. If you find an issue, please report it. Include information from Logcat and a description of the code change you were making. You can also check the list of open issues.

Live Edit is a feature that lets you update composables in emulators and physical devices in real time. This functionality minimizes context switches between writing and building your app, letting you focus on writing code longer without interruption.    **Important:** The manual mode shortcut was updated to Control+' (Command+' on macOS) in Koala Feature Drop. Previous versions of Android Studio used Control+\ (Command+\ on macOS) as the manual mode shortcut.

Live Edit has three modes:
- Manual: Code changes are applied when they're manually pushed using Control+' (Command+' on macOS)
- Manual on Save: Code changes are applied when they're manually saved using Control+S (Command+S on macOS).
- Automatic: Changes are applied in your device or emulator when you update a composable function.

Live Edit is focused on UI- and UX-related code changes. Live Edit doesn't support changes such as method signature updates, adding new methods, or class hierarchy changes. For more information, see the list of Limitations of Live Edit.

This feature is not a replacement for building and running your app or for Apply Changes. Instead, it's designed to optimize your workflow as you build, deploy, and iterate to develop Compose UI.

The best practice workflow is as follows:
- Set up your application so that it can be run.
- Live Edit as much as possible until you need to make a change that Live Edit doesn't support, such as adding new methods while the app is running.
- After you make an unsupported change, click **Run**
[IMAGE] Run
icon
to restart your app and resume Live Edit.

### Get started with Live Edit

To get started, follow these steps to create an empty Compose Activity, enable Live Edit for your project, and make changes with Live Edit.

#### Set up your new project
- Before you begin, make sure that you have Android Studio Giraffe or higher installed and that the API level of your physical device or emulator is at least 30.
- Open Android Studio and select **New Project** in the **Welcome to Android Studio** dialog. If you already have a project open, you can create a new one by navigating to **File** > **New** > **New Project**.
- Choose the **Empty Compose Activity** template for **Phone and Tablet**, and then click **Next**.
[IMAGE] Template selection in Android Studio

**Figure 3. **Templates you can choose from. For Live Edit, choose **Empty Compose Activity**.
- Complete the **New Project** dialog with the required information: name, package name, save location, minimum SDK, and build configuration language.
[IMAGE] Example project settings from Step 4 entered in Android Studio

**Figure 4. **Example project settings.
- Click **Finish**.

#### Enable Live Edit
- Navigate to the settings to enable Live Edit.
  - On Windows or Linux, navigate to **File** > **Settings** > **Editor** > **Live Edit**.
  - On macOS, navigate to **Android Studio** > **Settings** > **Editor** > **Live Edit**.

- Select the **Live Edit** option and the mode you want to run from the settings.
In manual mode, your code changes are pushed every time you press Control+' (Command+' on macOS). In manual mode on save, your code changes are applied every time you manually save, using Control+S (Command+S on macOS). In automatic mode, your code changes are applied in your device or emulator as you make your changes.
[IMAGE] Live Edit checkbox UI in Android Studio settings

**Figure 5. **Live Edit settings.
- In the editor, open the `MainActivity` file, which is the entry point for your app.
- Click **Run**
[IMAGE] UI button
to deploy your app.
- After you turn on Live Edit, the **Up-to-date** green checkmark appears in the top right of the **Running Devices** tool window:

[IMAGE] Live Edit green checkmark UI

#### Make and review changes

As you make supported changes in the editor, the virtual or physical test device updates automatically.

For example, edit the existing `Greeting` method in `MainActivity` to the following:

```
@Composable
fun Greeting(name: String) {
Text(
text = "Hello $name!",
Modifier
.padding(80.dp) // Outer padding; outside background
.background(color = Color.Cyan) // Solid element background color
.padding(16.dp) // Inner padding; inside background, around text)
)
}
```

Your changes appear instantaneously on the test device, as shown in Figure 6.
[IMAGE] Changes to Greeting method applied on a device

**Figure 6. **Test device displaying Live Edit changes to the `Greeting` method.

### Troubleshoot Live Edit

If you don't see your edits on the test device, Android Studio might have failed to update your edits. Check whether the Live Edit indicator says **Out Of Date** as shown in Figure 7, which indicates a compilation error. For information about the error and suggestions for how to resolve it, click the indicator.
[IMAGE] Live Edit out of date icon

**Figure 7. **Live Edit status indicator.

### Limitations of Live Edit

The following is a list of current limitations.
- [Only applies to Android Studio Giraffe and higher] Live Edit requires Compose Runtime 1.3.0 or higher. If your project uses a lower version of Compose, Live Edit is disabled.
- [Only applies to Android Studio Giraffe and higher] Live Edit requires AGP 8.1.0-alpha05 or higher. If your project uses a lower version of AGP, Live Edit is disabled.
- Live Edit requires a physical device or emulator that is running API level 30 or higher.
- Live Edit only supports editing a function body, which means that you can't change the function name or the signature, add or remove a function, or change non-function fields.
- Live Edit resets the app's state the first time you change a Compose function in a file. This only happens after the first code change—the app state isn't reset by subsequent code changes you make to Compose functions in that file.
- Live Edit-modified classes might incur some performance penalty. Run your app and use a clean release build if you are evaluating its performance.
- You must perform a full run for the debugger to operate on classes that you have modified with Live Edit.
- A running app might crash when you edit it with Live Edit. If this happens, you can redeploy the app with the **Run**
[IMAGE] UI
button
button.
- Live Edit doesn't perform any bytecode manipulation that's defined in your project's build file—for example, bytecode manipulation that would be applied when the project is built using the options in the **Build** menu or by clicking the **Build** or **Run** buttons.
- Non-Composable functions are updated live on the device or emulator, and a full recomposition is triggered. The full recomposition might not invoke the updated function. For non-composable functions, you must trigger the newly updated functions or run the app again.
- Live Edit doesn't resume on app restarts. You must run the app again.
- Live Edit only supports debuggable processes.
- Live Edit does not support projects that use custom values for `moduleName` under `kotlinOptions` in build configuration.
- Live Edit doesn't work with multi-deploy deployments. This means that you can't deploy to one device, and then to another. Live Edit is only active on the last set of devices the app was deployed to.
- Live Edit works with multidevice deployments (deployments to multiple devices that were created through **Select multiple devices** in the target device dropdown). However, it's not officially supported and there might be issues. If you experience issues, please report them.
- Apply Changes/Apply Code Changes are not compatible with Live Edit and require the running app to be restarted.
- Live Edit currently does not support Android Automotive projects.

### Frequently asked questions about Live Edit
- **What is the current status of Live Edit?**
Live Edit is available in Android Studio Giraffe. To turn it on, navigate to **File** > **Settings** > **Editor** > **Live Edit** (**Android Studio** > **Settings** > **Editor** > **Live Edit** on macOS).
- **When should I use Live Edit?**
Use Live Edit when you want to quickly see the effect of updates to UX elements (such as modifier updates and animations) on the overall app experience.
- **When should I avoid using Live Edit?**
Live Edit is focused on UI- and UX-related code changes. It doesn't support changes such as method signature updates, adding new methods, or class hierarchy changes. For more information, see Limitations of Live Edit.
- **When should I use Compose Preview?**
Use Compose Preview when you're developing individual composables. Preview visualizes Compose elements and automatically refreshes to display the effect of code changes. Preview also supports viewing UI elements under different configurations and states, such as dark theme, locales, and font scale.

### Iterative code development with Compose

Live Edit and Hot Reload for Compose Multiplatform are features that can save you time and increase your productivity as you develop with Compose. However, they serve the needs of different types of development:
- Live Edit supports iterative development with Jetpack Compose for **Android applications**. It lets you update composables in emulators and physical devices in real time. This functionality minimizes context switches between writing and building your app, letting you focus on writing code for longer without interruption.
- Compose Hot Reload serves the same need, but supports **desktop applications built with Compose Multiplatform**. It enables you to make changes to your UI code in a Compose Multiplatform application and see the results in real time by intelligently reloading your code whenever it is changed.

While these two features share many technologies within the Compose engine and support many similar use cases, they do not have the same capabilities because they apply to different types of Compose development.

If you are developing an Android app, you should use Live Edit to accelerate your development process. If you are developing a desktop application using Compose Multiplatform, you should use Compose Hot Reload.

## Live Edit of literals (deprecated) **Deprecated:** Live Edit of literals, an earlier version of Live Edit that is limited to just a few supported types, is deprecated. We recommend using Live Edit instead.

Android Studio can update in real time some constant literals used in composables within previews, emulator, and physical device. Here are some supported types:
- `Int`
- `String`
- `Color`
- `Dp`
- `Boolean`

[IMAGE] Video of the user changing literals in the source code, and the preview
updating
dynamically

You can view constant literals that trigger real time updates without the compilation step by enabling literal decorations through the Live Edit of literals UI indicator:

[IMAGE] Enabling Live Editing of
Literals
**Note:** The Live Edit of literals UI indicator is be displayed next to **Code/Split/Design** as long as there is an active connection to the running app on an emulator or physical device.

## Apply Changes

Apply Changes lets you update code and resources without having to redeploy your app to an emulator or physical device (with some limitations).

Whenever you add, modify, or delete composables, you can update your app without having to redeploy it by clicking on the **Apply Code Changes** button:

[IMAGE] User clicking the "apply changes"
button

## Recommended for you
- Note: link text is displayed when JavaScript is off
- Customize animations {:#customize-animations}
- Value-based animations

Previous  arrow_back   Preview and debug animations

Next  Editor actions   arrow_forward
