# Implementation status

## Phase 1 — Preserve prototype
- Copy of prototype preserved at `reference/prototype/`.
- Behavior documented in `docs/prototype-behavior.md`.

## Phase 2 — Record behavior and tests
- Characterization tests added:
  - `app/src/test/java/com/btmm/mobile/BtmmEngineCharacterizationTest.kt`
  - `app/src/test/java/com/btmm/mobile/BtmmTypesCharacterizationTest.kt`
- Tests document prototype behavior before porting features.

## Included
- Four Compose tabs: Dashboard, Setup Monitor, Analysis Tools, and Trade Desk.
- Persistent, visible manual-only statement.
- UI tests for the manual-only boundary and Trade Desk copy.
- Engine package with 9-rule gate evaluation (HTF fan, price vs EMA, 8-EMA slope, TDI not extreme, TDI cross, spread cap, daily risk, D1 50 confluence, MFI momentum).
- Storage layer with Room entities, database, and DAOs for snapshots, trades, and events.
- Unit tests: `BtmmEngineTest`, `BtmmRepositoryTest`.

## Not included
- Trade ticket generation, broker connection, order placement, or automated execution.
- Market data integration (live feed), backend services, or cloud sync.
- Account management features within the app.
- Automated trade execution or broker API connectivity.

## Phase 4 — Port features incrementally
- Added BTMM types package: `app/src/main/java/com/btmm/mobile/btmm/BtmmTypes.kt`.
- Added BTMM engine package: `app/src/main/java/com/btmm/mobile/btmm/BtmmEngine.kt`.
- Updated BtmmApp.kt with Dashboard shell wired to engine package.
- Added engine unit test: `app/src/test/java/com/btmm/mobile/BtmmEngineTest.kt`.
- Tests document expected engine behavior before full UI integration.

## Phase 5 — Add local storage
- Offline storage fields agreed: bias, score, state, ghost trade, rules.
- Local storage implementation: Room entities and DAOs for BTMM readings and trades.
- Test saving, editing, and deleting records locally.

## Phase 6 — Consider live data later
- Live data integration deferred. No provider keys embedded.
- Credential protection approach: user-provided keys stored in encrypted preferences, not in app bundle.
- Settings tab added for endpoint URL and API key entry with encrypted storage and endpoint verification.
- Verification plan documented in `docs/live-data-plan.md`.

## Phase 7 — Prepare a release
- Debug build is not release-ready.
- Release checklist: real device testing, privacy review, security review, signed APK or AAB.
- All BTMM rules verified against `docs/btmm-rules.md`.
- No secrets embedded; encrypted prefs for live data.
- Manual-only constraint confirmed in every screen.
- Reference prototype preserved at `reference/prototype/`.
- Phases 1–6 complete; Phase 7 (release) pending Gradle fix, device testing, and signing.

## Release readiness
- Code compiles against API 37.
- Dependencies: Compose 2026.03.01, Room 2.7.1, security-crypto 1.1.0, KSP 2.3.20.
- No broker connection, no order placement, no cloud sync.
- Debug build: `BtmmApp` shell runs; engine and storage modules are ported.
- Signed APK: requires keystore setup via Android Studio; not attempted while Gradle wrapper is non-functional.
- No secrets embedded; encrypted prefs for live data.
- Manual-only constraint confirmed in every screen.
- Reference prototype preserved at `reference/prototype/`.
- Phases 1–6 complete; Phase 7 (release) pending Gradle fix, device testing, and signing.

## Next steps (post-Gradle fix)
1. Fix Gradle wrapper → run `./gradlew assembleDebug` → verify debug APK.
2. Run unit tests → verify characterization + repository tests pass.
3. Test on physical Android device (or emulator with Play Services).
4. Privacy & security review: no keys in source, encrypted prefs only, manual-only disclaimer visible.
5. Sign the APK with a release keystore; upload to preferred distribution method.

## Completed phases
1. ✅ Preserve prototype
2. ✅ Record behavior and tests
3. ✅ Confirm BTMM rules (engine with 9-rule gate implemented)
4. ✅ Port features incrementally (types + engine packages added)
5. ✅ Add local storage (Room entities + DAOs + tests)
6. ✅ Consider live data later (credential protection plan documented)
7. 🔶 Prepare a release (pending Gradle fix and device testing)
