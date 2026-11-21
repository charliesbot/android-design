# Create shortcuts
Source: https://developer.android.com/develop/ui/compose/system/shortcuts/best-practices

When designing and creating your app's shortcuts, follow these guidelines:  **Follow the design guidelines** To make your app's shortcuts visually consistent with the shortcuts used for system apps, follow the App Shortcuts Icon Design Guidelines. **Publish only four distinct shortcuts**

Although the API supports a combination of up to 15 static and dynamic shortcuts for your app, we recommend that you publish only four distinct shortcuts, to improve their visual appearance in the launcher.

In addition to displaying shortcuts on the launcher, use the Google Shortcuts Integration Library to display shortcuts on Google surfaces such as Google Assistant. This library supports pushing an unlimited number of dynamic shortcuts. If you are using this library to push a large number of shortcuts, we recommend setting the `rank` of the shortcuts that must appear in supported launchers by calling the `setRank()` method. **Limit shortcut description length**

The space in the menu that shows your app's shortcuts in the launcher is limited. When possible, limit the length of the "short description" of a shortcut to 10 characters and limit the length of the "long description" to 25 characters.

For more information about labels for static shortcuts, read Customize attribute values. For dynamic and pinned shortcuts, read the reference documentation on `setLongLabel()` and `setShortLabel()`. **Maintain shortcut and action usage history**

For each shortcut you create, consider the different ways a user can accomplish the same task directly within your app. Call `reportShortcutUsed()` in each of these situations so that the launcher maintains an accurate history of how frequently a user performs the actions representing your shortcuts. **Update shortcuts only when their meaning is retained**

When changing dynamic and pinned shortcuts, only call `updateShortcuts()` when changing the information of a shortcut that retains its meaning. Otherwise, use one of the following methods, depending on the type of shortcut you're recreating:
- Dynamic shortcuts: `pushDynamicShortcut()`.
- Pinned shortcuts: `requestPinShortcut()`.
For example, if you create a shortcut for navigating to a supermarket, it is appropriate to update the shortcut if the name of the supermarket changes but its location stays the same. If the user begins shopping at a different supermarket location, however, it's better to create a new shortcut.
**Check dynamic shortcuts whenever you launch your app** Dynamic shortcuts aren't preserved when the user restores their data onto a new device. For this reason, we recommend that you check the number of objects returned by `getDynamicShortcuts()` each time you launch your app and re-publish dynamic shortcuts as needed, as shown in the code snippet in Backup and restore.

Previous  arrow_back   Manage shortcuts

Next  Adaptive icons   arrow_forward
