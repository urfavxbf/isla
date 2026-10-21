# Isla — Dynamic Island for Android

**Isla** is a Kotlin + Jetpack Compose Android floating-island app with media playback, notifications, calls, charging, timers, navigation, downloads, system activities, gestures and visual customization.

The **`main` branch now opens the complete redesigned Isla Lab Android project** from the repository root. The former v1.2.1 app is preserved at [`archive/isla-v1.2.1`](https://github.com/urfavxbf/isla/tree/archive/isla-v1.2.1); it is no longer the root project's Android app.

## On-device build: Code on the Go

There is no requirement to download a GitHub Actions APK if **Code on the Go can sync, build and install this project**.

1. In Code on the Go, **pull the current `main` branch** (or re-clone if the existing working folder is stuck on an older project). GitHub merges do not sync your phone automatically.
2. Open the **repository root** (the folder with `settings.gradle.kts`, `gradlew` and `README.md`), not the legacy `app` sources.
3. Sync Gradle while online; use the Gradle **wrapper** when possible.
4. Run **`:app:assembleDebug`** and use Code on the Go's **Run / Install** action to install the local build on your phone.

The root `settings.gradle.kts` maps `:app` to `isla-lab/app` and `:baselineprofile` to `isla-lab/baselineprofile`. This ensures the newest redesigned app is built, even when opened from the root.

**Code on the Go compatibility warning:** this full-featured foundation uses **Gradle 9.7.1**, **Android Gradle Plugin 9.1.1**, **Kotlin 2.3.21**, Hilt and a newer Jetpack Compose stack. Some Code on the Go versions ship **Gradle 9.6.1**, older offline dependencies, or an incompatible Gradle init plugin. A successful GitHub build does **not** prove Code on the Go can compile it offline. Prefer Gradle wrapper and online dependency sync; if it fails, inspect the first actionable error from Gradle Output and adjust the toolchain rather than removing features.

## App installation identity

- Current app: `Isla Lab` (Android package `com.pao.isla.lab`, version `0.1.0-lab`).
- Former prototype: `Isla` (package `com.pao.isla`, version `1.2.1`).

They are *separate* Android app installations; building this main branch does not silently replace or update an installed `com.pao.isla`. You may install both while validating the redesign. The displayed app name can be changed to `Isla` after migration testing, independent of the application ID.

## Screens and capabilities

- **Home:** setup, status, island preview, start and stop.
- **Overlay:** position presets, notch/camera cutout, landscape anchors and fine-tuning.
- **Features:** toggles for media sessions, notifications, calls, power, timers, connections, navigation and other supported events, plus an in-app demo.
- **Style:** colors, glass effects, typography, animations and individualized activity-card accents.
- **System:** permission onboarding, security/privacy settings, search, backup/restore, diagnostics and About.

Some activity integrations depend on source apps, Android permissions and device firmware; code availability is not a guarantee of phone compatibility. Grant only the permissions needed for features you choose.

## Build from CLI

```bash
./gradlew :app:assembleDebug :app:testDebugUnitTest
```

Debug APK output: `isla-lab/app/build/outputs/apk/debug/app-debug.apk`.

GitHub CI also builds and runs JVM unit tests at the repository root.

## Project notes and provenance

- [Isla UI architecture](docs/ISLA_UI_ARCHITECTURE.md)
- [Overlay editor PR](https://github.com/urfavxbf/isla/pull/12)
- [Activity card PR](https://github.com/urfavxbf/isla/pull/13)
- [Complete derived source and attribution](isla-lab/README.md)

This product is a **GNU GPL v3 derivative** of [Smart Island by Animesh Gupta and contributors](https://github.com/agupta07505/SmartIsland). Isla's branding and modifications do not remove the original authors' copyrights or required attribution. See [LICENSE](isla-lab/LICENSE), [ISLA_ATTRIBUTION.md](isla-lab/ISLA_ATTRIBUTION.md), and [UPSTREAM_PROVENANCE.md](isla-lab/UPSTREAM_PROVENANCE.md). Redistributors must follow GPL-3.0 corresponding-source and notice requirements.
