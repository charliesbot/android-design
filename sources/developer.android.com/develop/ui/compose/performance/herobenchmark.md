# Hero benchmarks
Source: https://developer.android.com/develop/ui/compose/performance/herobenchmark

Hero benchmarks are a set of benchmarks that cover high-level app user journeys, such as app startup or scrolling in the open-source Pokedex app.
- **Startup hero benchmarks**: Tracking the time from opening the app to content being displayed.
- **Scroll hero benchmarks**: Measuring scrolling performance of a lazy grid with images.

## Test setup
- **App selection**: We conducted these tests using the open-source Pokedex app, comparing its View-based and Compose-based versions. The app was selected to represent real-world scenarios, and was not developed by the Compose team.
- **Hardware setup**: Benchmarks were run on a Pixel 6 running Android 17 (API level 37) with locked CPUs and GPUs. Prior to Compose 1.12, benchmarks were conducted on a Pixel 3a. As of Compose 1.12, Pixel 6 is used as the standard performance baseline for hero benchmarks, and Compose 1.11 was also measured on Pixel 6 for comparison.
- **Build setup**: The benchmarks use the app built in release mode with R8 enabled. The app is fully pre-compiled to reduce instability from just-in-time (JIT) compilation.
- **Result selection**: To ensure worst-case analysis, extreme outliers and non-representative noise were excluded from the benchmark results. A baseline was then established by selecting the median values of the upper-bound performance data.
**Note:** Since the Compose 1.12 release, incremental improvements in the Views benchmark have reduced Views time to full display from 193 ms to 180.32 ms.

## Startup hero benchmarks

To measure startup, we used `PokedexStartupBenchmark`. The `PokedexStartupBenchmark` measures the time it takes for the Pokedex app to be visible to the user.

App launch can take place in one of three states: cold start, warm start, or hot start. Each state affects how long it takes for an app to become visible to the user. In a cold start, the app starts from scratch. In this benchmark, we measure the app launch performance in cold start state. We recommend that you always optimize the app based on an assumption of a cold start. Doing so can improve the performance of warm and hot starts as well.

The metrics we use to measure the app startup time are time to initial display and the time to full display.

### Time to initial display

Time to initial display (TTID) measures the time it takes for an app to produce its first frame, including process initialization during a cold start, activity creation during a cold or warm start, and displaying the first frame.

In the `PokedexStartupBenchmark`, Compose 1.13 reaches initial display in 147.65 ms on a cold start. This result is within normal run-to-run noise of Compose 1.12.1 (145.51 ms) and Compose 1.11.4 (147.78 ms), and 3.4% faster than Views (152.87 ms).
Time to initial display in milliseconds, tested on Pixel 6
| Version | Time to initial display (ms)
| Compose 1.11 | 147.78
| Compose 1.12 | 145.51
| Compose 1.13 | 147.65
| Views | 152.87

Tested on Pixel 6
Time to initial display

Time to initial display (ms)
0 50 100 150 200

147.78
145.51
147.65
152.87
Compose 1.11 Compose 1.12 Compose 1.13 Views
**Figure 1.** This chart compares TTID for Compose 1.11, 1.12, and 1.13 with Views.

### Time to full display

Time to full display (TTFD) measures the time until the app becomes interactive. This means that all data has been loaded and has been drawn. For more information about time to full display, see App Startup Time.

In the `PokedexStartupBenchmark`, Compose 1.13 reaches full display in 202.79 ms on a cold start. This result is within normal run-to-run noise of Compose 1.12.1 (201.96 ms) and Compose 1.11.4 (203.33 ms). Views reaches full display in 180.32 ms, so Compose 1.13 is 12.5% slower than Views for TTFD.
Time to full display in milliseconds, tested on Pixel 6
| Version | Time to full display (ms)
| Compose 1.11 | 203.33
| Compose 1.12 | 201.96
| Compose 1.13 | 202.79
| Views | 180.32

Tested on Pixel 6
Time to full display

Time to full display (ms)
0 50 100 150 200 250

203.33
201.96
202.79
180.32
Compose 1.11 Compose 1.12 Compose 1.13 Views
**Figure 2.** This chart compares TTFD for Compose 1.11, 1.12, and 1.13 with Views.  **Note:** The Compose team is actively working to improve the startup latency. For more details about upcoming improvements in Compose performance, see the Compose roadmap.

## Scroll hero benchmarks

To measure the scrolling performance we used the PokedexScrollBenchmark. It measures the scroll performance of the Pokedex app screen for a lazy grid with images. In this benchmark, the entire screen is scrolled and flung multiple times and new items are loaded. Scroll performance is measured by jank rate.

**Jank rate**

Android devices render at up to 60 or 120 frames per second (fps). This means the system has a strict deadline (for example, 16.6ms for 60fps) to produce a frame. If the app takes too long to do its work, this can cause a visible stutter that we call jank.

In the `PokedexScrollBenchmark`, Compose matches Views performance with 0 percentage points of excess jank since Compose 1.9.0. On Pixel 6, Views and Compose both hold 0.00% jank across Compose 1.11.4, 1.12.1, and 1.13.
Compose excess jank over Views in percentage points, by Compose version (lower is better)
| Compose version | Device | Excess jank over Views (pp)
| 1.3.3 | Pixel 3a | +0.35
| 1.4.3 | Pixel 3a | +0.41
| 1.5.4 | Pixel 3a | +0.42
| 1.6.8 | Pixel 3a | +0.21
| 1.7.8 | Pixel 3a | +0.36
| 1.8.0 | Pixel 3a | +0.38
| 1.9.0 | Pixel 3a | 0
| 1.10.0 | Pixel 3a | 0
| 1.11 | Pixel 6 | 0
| 1.12 | Pixel 6 | 0
| 1.13 | Pixel 6 | 0

Excess jank over Views
Compose   Views

Excess jank over Views (pp)  Percentage points, lower is better.
0 +0.1 +0.2 +0.3 +0.4 +0.5
0  +0.35  +0.41  +0.42  +0.21  +0.36  +0.38  0  0  0  0  0
1.3.3 1.4.3 1.5.4 1.6.8 1.7.8 1.8.0 1.9.0 1.10.0 1.11 1.12 1.13

Pixel 3a
Pixel 6
Compose version
**Figure 3.** Since Compose 1.9.0, Compose matches Views scroll jank.

## Run hero benchmarks

To validate the performance results and run the benchmarks locally:
- Follow the AndroidX guide to check out the source code.
- Follow the instructions in the Hero Benchmarks source code README to build the benchmark and target APKs (`pokedex-macrobenchmark` and `pokedex-macrobenchmark-target`), install them on a test device, and run the `PokedexStartupBenchmark` and `PokedexScrollBenchmark` suites using `adb` or Android Studio.

You can run these benchmarks on any supported device, such as a specific device that you are optimizing your app for. However, to validate the official results, use the same hardware setup.
