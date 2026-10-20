# Isla — Dynamic Island for Android

A native Kotlin + Jetpack Compose Android app for a customizable floating Dynamic Island overlay.

## Features
- Material 3 dark settings UI
- Overlay permission flow
- Start/stop floating island
- Adjustable width, height, corner radius, transparency and accent color
- Preferences saved locally

## Build
Open this repository as a Gradle project in Code on the Go or Android Studio and run `:app:assembleDebug`.

Requires Android 8.0 (API 26) or newer. The overlay requires the user to grant Android's **Display over other apps** permission. Media-session and notification integrations are not included in this starter yet.