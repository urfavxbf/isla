# Open-source references for Isla

Reviewed: 2026-10-21. Reference projects inform design and testing decisions; they are **not** drop-in dependencies. Check each project's current license and source history before reusing any code or assets.

| Project | URL | Isla area to study | Cautions |
| --- | --- | --- | --- |
| DynamicIslandMusic (bguerraDev) | https://github.com/bguerraDev/DynamicIslandMusic | Notification listener -> media state bus -> overlay service; play/pause state changes | Experimental; custom license requires attribution if code is reused |
| SmartIsland (agupta07505) | https://github.com/agupta07505/SmartIsland | Media metadata/artwork, permission onboarding, foreground-service resilience | GPL-3.0; do not copy into Isla without deciding license compatibility |
| Islandify (bughunter11) | https://github.com/bughunter11/Islandify | Unified island state, media cards, notifications, customization | Verify license and platform-specific behavior before adaptation |
| Island (Arnav-Dugad) | https://github.com/Arnav-Dugad/dynamic-island-android | Cutout anchoring, interaction physics, media capabilities | Higher Android/Gradle requirements may not run in Code on the Go |
| Android-Island (ProNoob2450) | https://github.com/ProNoob2450/Android-Island | Session/notification ingestion, state reduction, event queue | Verify license and device support |

## Immediate Isla music-detection investigation

Isla v1.2.1 currently reads MediaSessionManager.getActiveSessions() from IslaMusicListener (a NotificationListenerService) and publishes TrackInfo to MusicState; IslandOverlayService observes that state.

1. Verify notification-listener permission is **granted and actually connected**. Distinguish granted, connected and active media-session states in diagnostics. A granted setting alone does not imply a connected service.
2. On listener reconnection, refresh the current sessions and make callback registration idempotent. React to session-list changes and to metadata/playback changes.
3. Support no-session/no-metadata cases without falsely reporting a track; select actively playing sessions deterministically.
4. Test Spotify/YouTube Music playback, pause, source switching, revoke/regrant, APK update, process restart and battery/background restrictions on real devices.
5. Keep sensitive notification data transient and avoid introducing unnecessary permissions.
6. Verify builds against Isla's SDK/Code on the Go dependencies; avoid unrelated migrations until media recovery is tested.

## Attribution and adoption rule

Use behavior and architecture as inspiration and write original Isla implementations. No third-party code, screenshots, assets or dependencies are imported by this document. If any future code is copied, review and comply with the exact repository license and record attribution and obligations first.

## Execution

Track the music listener fix in PR #8, verify CI and device behavior before merge. The main implementation roadmap remains in docs/ROADMAP.md.
