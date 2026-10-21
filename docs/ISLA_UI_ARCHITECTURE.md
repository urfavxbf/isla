# Isla UI Architecture — original five-destination experience

Status: Phase 1 implemented in `feature/isla-five-tab-ui` (PR #10); detailed settings and card refresh scheduled separately. The original app behavior/services remain unchanged.

## Goal and constraints

Give Isla a recognizable violet/dark, preview-first UI without hiding its GPL-3.0 upstream attribution. All existing Android feature providers, overlay, notification listener, settings persistence and privacy behavior must remain accessible and functional. No feature may be simulated in place of the real system functionality.

## Navigation map

| Tab | Primary purpose | Reused production features |
|---|---|---|
| **Home** | Live status, island preview, start/stop, setup and quick navigation | `MasterPowerCard`, system recovery, service enabled repository state |
| **Overlay** | Cutout/position, portrait/landscape, size, animation and idle shape | `PositionsSection`, `LandscapeSection`, `AnimationSpeedSection`, `MergeStyleSection`, `IdleWidgetsSection` |
| **Features** | Activity toggles and privacy/per-app rules | `EventTogglesSection`, `NotificationsAndPrivacySection`, `GesturesSection`, `IdleWidgetsSettingsSection` |
| **Style** | Material theme, accent, glass, fonts and island appearance | `AppThemeOptions`, `ThemeModeSelector`, `IslandFontPicker`, `IslandFontMixSection`, `CustomizationsSection`, animation settings |
| **System** | Search, permission hub, notification history, backup, diagnostics, support and About/legal | existing `SettingsOverviewSection`, `DetailScreenHost` and unchanged detail screens |

The original Demo functionality remains reachable from Features as the **feature playground**. It is intentionally not a sixth bottom tab; Android back returns to Features.

All original nested detail pages are still routed through `DetailScreenHost`, which preserves notification history, supported replies, Face ID settings, calendar/weather/net-speed, app shortcuts, diagnostics, backup, About, and GPL attribution.

### Source files

- `ui/IslaDesignShell.kt` — new original dashboard visuals, page headers, quick links and five-item Material 3 navigation.
- `ui/IslaHomeScreen.kt` — existing permissions, settings flow, nested navigation; now composes the new shell. No new Android permissions or service entry points.
- `ui/search/SettingsSearchIndex.kt` — event result destinations updated to Features; Home and Style indexed.
- `ui/search/SettingsSearchIndexTest.kt` — tests for search targets and screen reachability.
- `values/strings.xml` — five navigation labels with standard Android locale fallback.
- `.github/workflows/isla-lab-build.yml` — assemble plus JVM tests before publishing the APK artifact.

## Design system

Isla uses Material 3 color roles as the source of truth (user-chosen theme, dynamic wallpaper colors and contrast preserved). Shared design primitives:

- `IslaPageIntro`: concise high-level page identity.
- `IslaHubHero`: truthful island status and static preview (does not invent song/notification content).
- `IslaSectionJump`: full-width accessible route card with real onClick action.
- `IslaBottomNav`: five selected-state destinations, Demo treated as an internal Features destination.

Violet/charcoal are accent visuals in the dashboard. Existing persisted user colors, glass and light/dark system preferences remain authoritative for the rest of the app.

## Follow-on screen redesign work

**Phase 2 — Overlay editor:** replace dense layout cards with a cutout preview-first design; reuse the exact same persisted settings, orientation logic and bounds validations. Capture device behavior on hole, corner, notch and landscape presets.

**Phase 3 — Activity cards:** redesign notifications/media/call/charging/timer/transfer cards with cohesive typography, icons and safe touch regions. Never change transport actions or priority logic just to re-skin a card.

**Phase 4 — Style and settings:** replace the nested legacy sections with Isla components. Verify font scaling, RTL, color contrast, keyboard/search, backups and reset migration; keep all legal acknowledgements accessible.

**Phase 5 — Onboarding and polish:** redesign dialogs, permission flow, empty/loading/error states, demo lab, accessible TalkBack names and motion-reduction. Use actual device screenshots, not imported upstream marketing assets.

## Release gates

- Debug build and `testDebugUnitTest` pass on the exact PR head.
- Search results select the correct tab/detail and focus the right feature row.
- Permissions/settings remain persistent; toggles still drive the same repository and services.
- No unreachable About/legal/license screens or deleted user features.
- Code on the Go build checked with a compatible JDK/Gradle toolchain.
- Real-device smoke test: music detect/control, notification receive/reply, charging, timers, call indicator, overlay touch-through, gesture actions, rotation and permission revocation.
- Keep `main` intact until complete QA and approval.
