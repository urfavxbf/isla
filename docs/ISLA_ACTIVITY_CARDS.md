# Isla Activity Cards — Phase 3, first batch

Branch: `feature/isla-activity-cards` (stacked on `feature/isla-overlay-studio`, PR #12).

## Visual language

Expanded activity cards receive a common lightweight **Isla eyebrow**: a restrained rounded surface, the activity's existing user-configurable accent, and a readable category/status label. A short accent stroke adds continuity without increasing card height. Incoming-call actions now use compact rounded-square controls, with the same touch bounds and handlers.

The shared composable is `ui/expanded/IslaActivityEyebrow.kt`. Colors are sourced from the existing Isla settings and the existing dark/light `IslandPalette`; this does not overwrite or reset user customization.

| Activity | Status/caption source | Functional path left intact |
|---|---|---|
| Music | Current play/pause state | `MediaController`, action PendingIntents, seek/prev/next, artwork, output routing |
| Notifications | Notifying application label | Notification reader, inline reply, multi-picture steps, app actions |
| Calls / VoIP | Ringing/connecting/ongoing state | Real app PendingIntents, mute, speaker, answer, hang up |
| Charging / battery | Power status | Battery percentage, charging time/visualization and original event priority |
| Download / upload | Transfer kind/state | Open original notification, cancel/pause/retry actions, progress and transfer speed |
| Navigation | Navigation indicator | Source navigation app tap, turn direction, ETA, destination details |

No new Android permissions, persisted settings, event parsing, overlay window layout rules, or device interfaces were introduced.

## Coverage limits

This is the **first batch** of activity-card polish, not a claim that every individual event screen has been redesigned. Timers, stopwatch, Bluetooth, hotspot, recorder, secondary controls and other cards retain their behavior and current rendering. They will be handled in subsequent small, testable branches.

## Validation checklist

- `./gradlew :app:assembleDebug :app:testDebugUnitTest` from `isla-lab/`, on this exact PR head.
- Verify both light and dark expanded appearance, custom per-feature color settings, and font-size/contrast accessibility.
- Real device: title/subtitle overflow with narrow island width, playback next/seek/play/pause, notification reply and picture states, incoming/ongoing VoIP actions, battery/charging, download cancellation, turn-by-turn opening, split landscape and expansion/collapse.
- Confirm search, permissions and legal attribution remain accessible in System/About.
- Do not merge to stable `main` until Code on the Go and actual phone QA pass.

Original GPL-3.0 source copyrights and attribution must remain in the code and legal pages.
