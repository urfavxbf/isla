# Isla Lab: full-featured Dynamic Island foundation

**Status:** import automation prepared; actual import and build require successful GitHub Actions execution.

## Decision

Use [SmartIsland](https://github.com/agupta07505/SmartIsland) as a complete feature foundation rather than transplanting only its music player. The independent Android project is stored under `isla-lab/`. Keep the original Isla app in `app/` and its main branch untouched until the lab APK builds and real-device tests pass.

Upstream source revision: `149efd1fde40416b93116ee7c958c2793a5d4459` (2026-10-19). The importer is pinned to that commit, not mutable upstream HEAD.

## Legal provenance

The upstream project is © Animesh Gupta and distributed under **GNU GPL v3**. Any derivative distribution must preserve copyright/license notices and comply with GPL obligations, including availability of corresponding source code. Only the modifications we write are ours; rebranding does not transfer ownership of the original code. Retain `isla-lab/LICENSE`, notices and credits. Do not import any upstream workflows without security review.

## Isolation and installation

- Existing Isla (main project): `com.pao.isla`.
- Experimental Isla Lab: `com.pao.isla.lab` (a *different* application ID, deliberately installable alongside existing Isla).
- Source namespace/package stays upstream initially to minimize breakages; changing Java/Kotlin package namespace and app branding is a separate tested change.
- Android phone development: import `isla-lab/` as a separate project in Code on the Go if compatible; Github Actions is the authoritative initial build environment due to upstream AGP 9.1.1, Gradle 9.7.1 and new dependencies.

## Features to validate

Notifications (group/reply/privacy), media playback and album art, calls, charging, timers, navigation, download/upload indicators, Bluetooth, quick launcher, idle widgets, gestures, customization and permissions. Availability differs by Android ROM, permissions and compatible source apps; **advertised features are not proof of real-device operation**.

## Acceptance gates

1. Confirm full upstream files imported with GPL license and exact commit provenance.
2. Assemble and archive debug APK on GitHub Actions; review build warnings.
3. Install Lab without replacing Isla and test active music, paused/stopped sessions, notification access, notification replies, charging, overlay touch-through, rotation, permission revocation and screen-off on physical phone.
4. Remove or explicitly gate intrusive/unnecessary permissions, hidden APIs and unsupported integrations before distribution.
5. Adapt Isla branding: UI copy, icons, colors, onboarding and package ID while retaining license/attribution.
6. Only merge small reviewed changes and ensure legacy main remains green; choose final migration/release when build and device gates are met.

## How import works

`.github/workflows/isla-lab-import.yml` runs only on the experimental branch. It clones upstream at the pinned revision, imports files into `isla-lab/` (excluding external workflows and Git history), changes app identity for side-by-side install, checks for GPL license, commits the result and attempts a debug build. If GitHub Actions token permissions forbid a push, the workflow will report this instead of silently claiming completion.
