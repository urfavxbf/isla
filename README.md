# Isla — Dynamic Island for Android

A Kotlin + Jetpack Compose Android app with a customizable floating Dynamic Island overlay.

## Features

- Dark Material 3 settings UI with live preview
- Overlay permission flow
- Start/stop floating island
- Adjustable width, height, corner radius, transparency and accent color
- Preferences stored locally
- Circular punch-hole preset (36dp) and width as small as 28dp
- Screen-top position adjustment for different camera cutouts
- **v1.2:** Opt-in media-session detection, track title/artist, Play/Pause and Skip controls
- Optionally expand the island while a song is playing

## Build on an Android phone with Code on the Go

This project targets **Code on the Go 26.38+** with its bundled toolchain:
**Gradle 9.6.1, Android Gradle Plugin 9.3.1, Kotlin 2.3.21**, and Android SDK 36.

1. Pull the latest `main` branch from GitHub or re-clone this repository inside Code on the Go. A pre-existing local copy does **not** update automatically when GitHub is changed.
2. Open the root `Isla` project (the folder containing `settings.gradle.kts`).
3. Save and choose **Sync** when Code on the Go prompts you.
4. Run `:app:assembleDebug`.

The root Gradle script includes the Kotlin classpath expected by CoGo's injected LogSender plugin, and uses AGP 9's built-in Kotlin support rather than the incompatible `org.jetbrains.kotlin.android` plugin.

### If a dependency says `Could not find ...jar` in `localMvnRepository`

CoGo usually builds offline using its bundled local Maven repository at
`/data/data/com.itsaky.androidide/files/home/maven/localMvnRepository`.
Its harvested offline repository supports only the toolchain/dependency versions it contains.

1. Verify you actually pulled the new Gradle files, especially **root `build.gradle.kts` version 9.3.1** and **`app/build.gradle.kts`**.
2. In **Code on the Go → Preferences → Build & Run → Additional Gradle flags**, remove `--offline` if it is present. Connect to the internet, sync, and build again; Maven Central / Google Maven are already configured in this project.
3. If the dependency still cannot be found and the log *only* searches the local Maven repository, the device's bundled Maven assets or Gradle initialization need attention. This cannot be fixed by changing UI Kotlin code or creating an empty jar.

CoGo's [official toolchain migration guide](https://github.com/appdevforall/CodeOnTheGo/wiki/IMPORTANT:-Fix-project-breaking-changes-after-the-Gradle-AGP-Kotlin-toolchain-upgrade) provides the matching AGP/Kotlin settings and offline-flag location.

## Minimum device

Android 8.0 (API 26) or newer. The floating island requires Android's
**Display over other apps** permission.

## Music controls (v1.2)

1. Open Isla and tap **Enable music access** in the **Now playing** section.
2. On Android's notification access page, turn on **Isla music controls** and confirm.
3. Return to Isla, press **Start**, then play a song in an app that publishes a standard Android media session.
4. The island shows the current track. Tap it to expand the controls, or tap **×** to minimize.
5. You can turn **Show music on island** and **Expand while playing** on/off independently.

**Privacy:** Android's notification-listener permission is broader than media playback. Isla's listener only reads active media sessions. It ignores other notifications and never stores or uploads their content. The feature is disabled until the user explicitly grants access.

**Compatibility:** Apps must publish active media sessions with supported transport actions for their playback controls to work; device-specific battery restrictions and overlay behavior may affect operation. Calls and non-media notifications are not implemented yet.
