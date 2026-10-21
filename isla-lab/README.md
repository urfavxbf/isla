# Isla Lab

**Isla** is an Android Dynamic Island–style overlay for music, notifications, timers, battery events, calls, and other ongoing activities. This experimental app is an independently branded **GPL-3.0 derivative** under active testing.

**Status:** GitHub debug APK builds successfully. Compatibility and permissions must be tested on your Android phone before replacing the original Isla app.

## Identity

| | Isla Lab |
|---|---|
| App display name | Isla Lab |
| Android application ID | `com.pao.isla.lab` |
| Version | `0.1.0-lab` |
| Minimum Android | Android 8 (API 26) |
| UI | Kotlin, Jetpack Compose, Material 3 |
| Brand | Isla violet / dark, original Isla launcher artwork |

It can be installed alongside the earlier `com.pao.isla` prototype without overwriting it.

## Features in the imported codebase

- Floating camera-cutout island with expanded cards, portrait/landscape layout, gestures and customization.
- Music sessions with metadata, album art and playback controls.
- Notification previews, supported actions and inline replies.
- Charging and battery status; timers, stopwatch and idle widgets.
- Compatible system and app activity indicators such as calls, navigation, downloads and Bluetooth.
- Themes, onboarding, access diagnostics, settings backup and restoration.

Some integrations depend on notification access, other special Android permissions, source-app support, and manufacturer restrictions. **The feature list describes present code paths, not confirmed performance on every Android device.** Use only required permissions and disable unsupported integrations.

## Get a test APK from GitHub

1. Open [Isla Lab build runs](https://github.com/urfavxbf/isla/actions/runs/39533671264).
2. Open the latest **successful** `Rebrand Isla Lab and validate build` run.
3. In **Artifacts**, download `Isla-fully-branded-debug` and extract the APK.
4. Install Isla Lab, then grant only the required Android permissions in Settings.

You can also build locally using the complete `isla-lab/` folder.

## Code on the Go

The Android project is inside `isla-lab/`, **not** the outer `urfavxbf/isla` repository root. Import that folder as the project in Code on the Go.

The upstream foundation uses Android Gradle Plugin **9.1.1** and **Gradle 9.7.1**. Code on the Go must support those versions and dependencies; a successful GitHub build does not prove its bundled Gradle environment is compatible.

### Build using a Gradle-compatible environment

```bash
cd isla-lab
./gradlew :app:assembleDebug
```

Output: `app/build/outputs/apk/debug/app-debug.apk` (relative to `isla-lab/`).

## Project status and source

- [Isla 2.0 experimental pull request](https://github.com/urfavxbf/isla/pull/9)
- [Migration notes](../docs/ISLA_2_FOUNDATION.md)
- [Original Isla roadmap](../docs/ROADMAP.md)

Old screenshots and marketing/release badges have been removed because they belonged to the upstream project. Capture actual Isla screenshots from a tested device before publishing store material.

## Privacy and safety

Isla may request notification-listener, overlay, accessibility and additional feature-specific permissions. Review what each permission enables, and do not treat imported security or privacy claims as audited facts. Do not turn on more access than needed. Review and validate the permission model before public distribution.

## License and attribution

Isla Lab is derived from [Smart Island by Animesh Gupta](https://github.com/agupta07505/SmartIsland), licensed under **GNU GPL v3**. The upstream author retains the original copyright; Isla branding and our modifications do not transfer ownership of that code. Redistribution must comply with the GPL, preserve required notices, and make corresponding source code available.

See [LICENSE](LICENSE), [ISLA_ATTRIBUTION.md](ISLA_ATTRIBUTION.md), and [UPSTREAM_PROVENANCE.md](UPSTREAM_PROVENANCE.md).
