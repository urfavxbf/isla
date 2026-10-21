# Isla — Dynamic Island for Android

This is the **full SmartIsland-based Isla app** with original Isla branding and our redesigns. The Android project is at the **repository root**: open this folder in Code on the Go and build `:app` from `app/`. There is no nested Android project to select.

## Build in Code on the Go

1. Pull the latest `main` branch (or re-clone if the existing local copy is outdated).
2. Open the repository root containing `settings.gradle.kts`, `gradlew` and `app/`.
3. Sync and run/install using Code on the Go. It can install a locally built APK; you do not need a GitHub Actions APK download when on-phone builds work.

The source uses Gradle **9.7.1**, Android Gradle Plugin **9.1.1**, and Kotlin **2.3.21**, matching the last successfully built SmartIsland-based foundation. **Important:** Code on the Go may force Gradle 9.6.1 and use a local Maven cache missing required dependencies (the previously reported `guava-33.3.1-jre.jar` issue). Moving the source into `main` does **not** automatically fix that separate IDE dependency problem. If a build fails, check whether `--offline` is enabled in Code on the Go, then retry online and share the first real Gradle error.

## Features

Music controls, notifications and replies, call activities, charging/battery, timers, navigation, download activities, gestures, overlay positioning, five-tab settings and Isla-styled cards. Individual features depend on Android and third-party app support.

The current application ID is `com.pao.isla.lab`, so this app installs **alongside**, not over, the old `com.pao.isla` prototype.

## Build with Gradle

```sh
./gradlew :app:assembleDebug :app:testDebugUnitTest
```

Output: `app/build/outputs/apk/debug/app-debug.apk`.

## Archived versions

- [Main before this root replacement](https://github.com/urfavxbf/isla/tree/backup-main-smartisland-20261021)
- [Original Isla v1.2.1](https://github.com/urfavxbf/isla/tree/archive/isla-v1.2.1)

## License

Isla is a **GPL-3.0 derivative** of [SmartIsland by Animesh Gupta](https://github.com/agupta07505/SmartIsland). Original author rights and license obligations remain. See [LICENSE](LICENSE), [ISLA_ATTRIBUTION.md](ISLA_ATTRIBUTION.md) and [UPSTREAM_PROVENANCE.md](UPSTREAM_PROVENANCE.md).
