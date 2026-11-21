# Build adaptive apps for ChromeOS
Source: https://developer.android.com/develop/adaptive-apps/guides/chromeos/build-adaptive-apps-for-chromeos

ChromeOS devices, such as Chromebooks, provide a unique desktop-like environment for Android apps. Users expect apps to behave like desktop applications, featuring resizable windows, robust keyboard and mouse support, and high-productivity layouts.

## Key considerations for ChromeOS
- **Desktop windowing**: Apps on ChromeOS typically run in free-form windows that can be resized, maximized, or tiled. Use window size classes to ensure your app adjusts its layout fluidly as the window dimensions change.
- **Keyboard and mouse input**: Unlike touch-primary devices, ChromeOS centers on physical input. Ensure your app supports:
  - **Keyboard shortcuts**: Common actions (like Ctrl+C/V) and app-specific shortcuts.
  - **Mouse interactions**: Right-click context menus, scroll wheel support, and hover states for interactive elements.

- **Built-in display**: Chromebook displays are often larger than tablets. Take advantage of this space by using multi-pane layouts and expanded navigation components.
- **External displays**: Many ChromeOS users connect their devices to external monitors. Support connected displays to provide a seamless multi-screen experience.

## Adaptation strategies
- **Optimize for productivity**: Implement canonical layouts like list-detail to display more information on a large ChromeOS display, reducing the need for frequent screen transitions.
- **Handle configuration changes**: Ensure your app maintains state during window resizing to prevent data loss or UI resets.
- **Refine navigation**: Use `NavigationSuiteScaffold` to automatically switch to a navigation rail or drawer when the app window is expanded on a desktop screen.

## Learn more

For ChromeOS development guidance, see ChromeOS devices.
