# Isla implementation roadmap

Date: 2026-10-21
Status: Proposed delivery roadmap; no application features implemented by this document.
Repository: urfavxbf/isla
Baseline inspected: main, app versionName 1.2.1, versionCode 4.

## Product goal

Build a reliable, customizable Android Dynamic Island for everyday music, notifications and ongoing activities. Preserve phone-based development with Code on the Go and deliver installable APKs through GitHub Actions. The product owner defines product priorities; the implementer owns technical decisions, code, tests, branches and PRs.

This is a portfolio of small subsystem plans, not a single implementation task. Write a focused implementation plan with exact files, interfaces and tests before each subsystem starts. Release labels below are proposed milestones, not promised dates.

## Baseline and evidence

Inspected README.md, repository tree, app/build.gradle.kts, .github/workflows/android-build.yml, MusicState.kt and IslandOverlayService.kt.

- Kotlin app; Compose/Material 3 settings; the overlay currently uses Android Views.
- Existing overlay sizing, vertical positioning, preference-backed configuration, explicit tap expansion and collapse.
- Existing media session bridge with title, artist and basic transport commands.
- TrackInfo currently has no artwork, duration or playback position.
- Current CI assembles a debug APK only. No test sources or Gradle wrapper appear in the inspected tree.
- Source inspection is not proof of device reliability or current CI success.
- Baseline minSdk 26, compileSdk 36, targetSdk 35; retain compatibility during incremental work. Recheck current Android and distribution requirements before SDK or release decisions.

## Delivery decision

Use incremental vertical slices: stable overlay -> richer music -> safe notifications -> local activities -> concurrent activities -> optional integrations.

A full rewrite would delay usable updates and increase regression risk. Building every integration first would leave too many permissions and service states untested. Keep the working app and introduce boundaries only where the next feature needs them.

## Engineering boundaries

- WindowManager and service lifecycle own window attachment, bounds, touch regions and cleanup.
- A pure state coordinator owns hidden/compact/expanded state, selected activity, dismissal and priority.
- Feature providers produce activity snapshots and supported actions; UI does not call platform APIs directly.
- Settings use a repository; migrate existing saved values without resetting customization.
- Compose + Material 3 is the target UI. Migrate the View overlay only after verifying service lifecycle owners, disposal and touch behavior in a dedicated PR.
- Use MVVM, Coroutines/Flow and dependency injection incrementally, checking Code on the Go compatibility.
- Use DataStore for preferences when migration is verified. Add Room only if durable structured data needs it. Retrofit is unnecessary for local-only features.
- Do not retain notification content or artwork across restarts by default.
- Accessibility, privacy and error handling are acceptance criteria from the first feature.

## Phase 0 — Reproducible build and safety baseline

Dependencies: none. Proposed release: maintenance patch.

Work:
- [ ] Record actual baseline build and CI status; fix existing failures before feature work.
- [ ] Verify Code on the Go toolchain and commit a compatible Gradle wrapper for reproducible CI/local builds.
- [ ] Add unit-test setup, Android lint, compatible Detekt/ktlint configurations and APK artifacts.
- [ ] Split pure sizing/action-capability rules out of UI code for regression tests.
- [ ] Add a device QA checklist with API 26, an intermediate API and current target/modern Android; include a real OEM device.
- [ ] Document known limitations and install/update steps.

PR slices: build reproducibility; meaningful baseline tests and quality gates.

Exit: assemble, unit tests, lint and configured static checks pass on the exact commit. APK installs and start/stop/settings survive a smoke test. Record unavailable device checks as pending rather than passed.

## Phase 1 — Stable overlay engine

Dependencies: Phase 0. Proposed release: 1.3.

Work:
- [ ] Model hidden, compact and expanded states with deterministic transitions.
- [ ] Separate service/window ownership from rendering without changing existing preference values.
- [ ] Handle repeated start/stop, revoked overlay access, detached windows and process recreation.
- [ ] Make window bounds adapt to cutouts, rotation and available screen size.
- [ ] Keep touch interception within visible interactive regions; validate outside-tap collapse on device.
- [ ] Add configurable auto-collapse, explicit close, idle visibility and reduced-motion behavior.
- [ ] Audit foreground/background service strategy against supported Android versions before choosing manifest/service changes.
- [ ] Verify a Compose overlay host in one isolated PR, including lifecycle disposal; migrate only after parity checks pass.

Likely existing files: IslandOverlayService.kt, IslandConfig.kt, MainActivity.kt, AndroidManifest.xml.
Proposed boundaries: overlay/IslandState.kt, overlay/IslandReducer.kt, overlay/OverlayWindowController.kt.

PR slices: state coordinator; lifecycle/bounds fixes; interaction and Compose parity.

Exit:
- 30 start/stop and expand/collapse cycles produce no duplicate or orphan window.
- Revoking permission exits safely and settings show how to restore access.
- Rotation, large font and camera presets keep controls reachable.
- Underlying apps remain tappable outside the island.
- Stop removes the window and cancels its collectors/animations.

## Phase 2 — Complete music experience

Dependencies: Phase 1. Proposed release: 1.4.

Work:
- [ ] Add bounded-size artwork, title, artist and source app icon with fallbacks.
- [ ] Replace text-symbol controls with consistent accessible icons.
- [ ] Respect supported play, pause, previous, next and seek actions; unknown capabilities do not imply support.
- [ ] Add duration/progress and seeking only when the session supports them.
- [ ] Update progress only while necessary; stop updates when hidden, stopped or screen-off.
- [ ] Handle playing, paused, buffering, stopped and missing session states.
- [ ] Add open-player action and deterministic active-session selection.
- [ ] Offer player selection when multiple usable sessions exist.

Likely existing files: MusicState.kt, IslaMusicListener.kt, IslandOverlayService.kt.
Proposed boundary: media/MediaRepository.kt and media/MediaSnapshot.kt.

PR slices: capability-correct state; artwork/card UI; progress/seek; session selection.

Exit: tested missing metadata/artwork, long titles, unsupported actions, session destruction and switching players. Controls operate the selected session; no stale track remains after removal. Physical media-app tests are recorded separately from unit tests.

## Phase 3 — Safe notifications

Dependencies: Phases 1 and 2. Proposed release: 1.5.

Work:
- [ ] Extend notification access with explicit opt-in and explain the broader permission.
- [ ] Add app allowlist/blocklist, icon/title/body preview, open and dismiss.
- [ ] Process updates/removals by stable notification key, deduplicate, group and bound the queue.
- [ ] Add display duration, sensitive-content masking and conservative lock-screen behavior.
- [ ] Prevent duplicate music notifications and ignore Isla's own service notification.
- [ ] Invoke provided notification actions safely; implement quick reply only when supported.
- [ ] Fail gracefully for cancelled actions, expired replies, revoked access and missing content.
- [ ] Keep notification content out of logs and persistent storage.

PR slices: ingestion/filtering; privacy and display queue; actions and quick reply.

Exit: repeated updates to the same notification do not create duplicates; removed notifications disappear; blocked apps never render; hidden previews remain hidden after restart. Reply/open failures have a clear recovery path. Notification bursts cannot grow memory without a bound.

## Phase 4 — Charging, timer and stopwatch

Dependencies: Phase 3. Proposed release: 1.6.

Work:
- [ ] Charging/unplugged/full indicators, battery percentage and configurable low-battery alert.
- [ ] Optional battery temperature with unavailable-value handling.
- [ ] Built-in timer with pause/resume/cancel and stopwatch.
- [ ] Persist timer intent/deadline appropriately; use monotonic elapsed time during a boot.
- [ ] Define restart/reboot behavior explicitly and verify alert delivery requirements before requesting exact-alarm access.
- [ ] Add timer completion alert and an upcoming-alarm shortcut where exposed.
- [ ] Add independent toggles and sample previews for these activities.

PR slices: charging; timer/stopwatch; restart and completion handling.

Exit: charging events do not spam; timer countdown is accurate across background/foreground and clock changes; process-death and reboot behavior is tested and documented. Offline operation works.

## Phase 5 — Concurrent activities and gestures

Dependencies: Phases 2–4. Proposed release: 1.7.

Work:
- [ ] Add bounded activity registry, primary island and secondary indicator.
- [ ] Prioritize user-selected activity and urgent events without destroying ongoing music/timer state.
- [ ] Add swipe switching, optional dismiss, long-press action and optional haptics.
- [ ] Distinguish dismissing a preview from stopping its underlying activity.
- [ ] Add pending count and deterministic tie-breaking.
- [ ] Delay automatic collapse during an active interaction such as seeking.

Proposed boundary: activities/ActivityCoordinator.kt, with pure ordering and transition rules.

Default priority proposal: active/incoming supported call > timer completion > allowed notification > user-selected ongoing activity > music/timer > passive system event. DND/privacy and explicit user choices can suppress presentation.

Exit: music + timer + notification work together; removing one activity reveals the correct next activity; gestures do not trigger playback by accident; no indefinite or starvation-prone queue.

## Phase 6 — Customization and daily-use controls

Dependencies: Phase 5. Proposed release: 1.8.

Work:
- [ ] Separate compact/expanded dimensions; horizontal/vertical position; cutout presets.
- [ ] Theme, background, accent, border, shadow, opacity, font and artwork options.
- [ ] Animation speed, gesture settings, idle visibility and reset-to-defaults.
- [ ] Live preview and versioned settings backup/restore with validation and safe ranges.
- [ ] Setup checklist, permission status, troubleshooting and sample activity tests.
- [ ] Quick Settings tile and explicit pause/resume.
- [ ] Evaluate per-app hiding/full-screen/keyboard behavior using legitimate available signals; expose only verified modes.
- [ ] Evaluate opt-in reboot restoration and document platform/device limits.

Exit: invalid imports cannot break rendering; settings survive upgrades; reset is predictable; presets remain usable with large text. Settings and previews clearly distinguish supported versus unavailable options.

## Phase 7 — Optional integrations, one provider at a time

Dependencies: Phase 5; each integration starts with a feasibility gate. Proposed release: 1.9, in smaller releases as needed.

| Integration | Minimum useful outcome | Ship gate |
| --- | --- | --- |
| Calls/VoIP | Supported call indicator and return-to-call action | Verify permissions, roles and available actions on each supported path before promising answer/mute/speaker controls |
| Navigation | Available direction/distance and open-navigation action | Test an explicitly documented compatible app/source; do not parse arbitrary text as a universal interface |
| Downloads/uploads | Progress from a compatible notification | Handle indeterminate progress, cancellation and completion |
| Delivery/ride | Preview of source-provided status | Label compatibility; no invented live tracking |
| Calendar/reminders | Reminder preview | Prefer existing notifications; justify any broader access |
| Bluetooth/headset | Connection state, accessory battery when available | Permissions, reconnect tests and unknown battery fallback |
| Network/ringer/volume | Optional supported event indicators | Verify event availability and background limits per Android version |

Unsupported integrations stay disabled or deferred with a reason. No accessibility-service workaround is assumed in this roadmap. Hiding inside arbitrary apps, guaranteed reboot restart and universal call controls are feasibility questions, not unconditional promises.

## Phase 8 — Release candidate and distribution

Dependencies: all features selected for release, not every optional provider. Proposed release: 2.0.

Work:
- [ ] End-to-end regression suite for setup, permissions, overlay, media, notifications and timer.
- [ ] TalkBack, 200% font scale, contrast, reduced motion and reachable touch targets.
- [ ] Profile idle/media/burst scenarios on real hardware; record CPU, memory, frame timing and battery evidence before setting device-specific budgets.
- [ ] Run an 8-hour mixed-use soak with screen-off, rotation, media switching, permission changes and app restarts; investigate crashes, ANRs and retained windows.
- [ ] Verify offline use, upgrade compatibility and settings migration.
- [ ] Prepare release signing, recovery of signing credentials, versioning and a signed release artifact.
- [ ] Recheck current official Android/Google Play SDK, permission and foreground-service requirements before submission.
- [ ] Prepare privacy policy, data disclosures, screenshots, store copy and honest compatibility notes.
- [ ] Keep last known good source/artifact available; fixes ship with increasing versionCode. Never promise APK downgrade installation over newer versions.
- [ ] Make publication/monetization a separate product decision.

Exit: exact release commit passes required checks; supported device matrix is recorded; no unresolved release-blocking crash/privacy/touch-interception defect; distribution requirements verified. Store acceptance is not guaranteed by building successfully.

## GitHub execution workflow

1. Open a scoped issue with outcome, dependency and acceptance criteria when its phase is ready.
2. Create a small feature/fix branch and a focused implementation plan.
3. For changed behavior, write meaningful tests, implement, and run the relevant checks.
4. Open a PR documenting user impact, screenshots where useful, tests and remaining device checks.
5. Review the diff and CI results for the exact head commit.
6. Merge only when required checks pass; keep main runnable.
7. Produce an APK with version, changelog and known limitations.
8. Record phase acceptance before advancing. A successful build is not evidence of device behavior.

Default CI after Phase 0: debug assembly, unit tests, lint, Detekt and ktlint with compatible configurations. Run targeted instrumented/device tests where UI/lifecycle behavior needs them.

## Cross-cutting regression cases

- Permission revoked mid-animation: detach safely and show permission recovery.
- Session disappears during seek: cancel command and remove stale data.
- Notification removed while reply is open: explain unavailable action, never send to a different conversation.
- Rotation/large fonts/cutout changes: keep controls in bounds without a full-screen invisible touch blocker.
- Process death while timer or activity is active: restore only durable state with defined semantics; never replay old private notifications.

## Progress tracking

All phases begin unchecked. A phase becomes complete only with links to merged PRs, CI runs, APK artifacts and required device-test evidence. Dates are set after Phase 0 establishes build and test capacity.

Immediate next implementation scope: Phase 0, followed by Phase 1 overlay stability. Richer media visuals follow once that base is reliable.
