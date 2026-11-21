# Use a baseline profile
Source: https://developer.android.com/develop/ui/compose/performance/baseline-profiles

Baseline Profiles improve code execution speed by about 30% from the first launch by avoiding interpretation and just-in-time (JIT) compilation steps for included code paths. By shipping a Baseline Profile in an app or library, you enable Android Runtime (ART) to optimize included code paths through ahead-of-time (AOT) compilation, providing performance enhancements for every new app install and every app update. This profile-guided optimization (PGO) lets apps optimize startup, reduce interaction jank, and improve overall runtime performance from the first launch for end users.

## Compose performance considerations

Compose is distributed as a library instead of as part of the Android platform. This approach lets the Compose team update Compose frequently and support a wide range of Android versions. However, distributing Compose as a library imposes a cost.

Android platform code is already compiled and installed on the device. Libraries, however, need to be loaded when the app launches and interpreted JIT when needed. This can slow the app on startup and when it uses a library feature for the first time.

## Benefits of baseline profiles

You can improve performance by defining Baseline Profiles. These profiles define classes and methods needed on critical user journeys and are distributed with your app's APK or AAB. During app installation, ART compiles this critical code AOT so that it's ready for use when the app launches.

A good Baseline Profile definition is not always straightforward, and because of this, Compose ships with one by default. You might not have to do any work to see this benefit. However, the Baseline Profile that ships with Compose only contains optimizations for the code within the Compose library.

### Macrobenchmark

To get the best optimization, create a Baseline Profile for your app that uses Macrobenchmark to cover critical user journeys. When you define your own profile, you must test the profile to verify that it's helping. A good way to do that is to write Macrobenchmark tests for your app and check the test results as you write and revise your Baseline Profile.

For an example of how to write Macrobenchmark tests for your Compose UI, see the Macrobenchmark Compose sample.

## Additional resources
- **App performance guide:** Discover best practices, libraries, and tools to improve performance on Android.
- **Inspect Performance:** Inspect app performance.
- **Benchmarking:** Benchmark app performance.
- **App startup:** Optimize app startup.
- **Baseline profiles:** Understand baseline profiles.
