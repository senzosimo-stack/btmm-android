# Implementation status

## Included
- Four Compose tabs: Dashboard, Setup Monitor, Analysis Tools, and Trade Desk.
- Persistent, visible manual-only statement.
- UI tests for the manual-only boundary and Trade Desk copy.
- Basic app scaffold: MainActivity.kt + BtmmApp.kt entry point.
- BTMM engine (9-rule gate evaluation: HTF fan, price vs EMA, 8-EMA slope, TDI not extreme, TDI cross, spread cap, daily risk, D1 50 confluence, MFI momentum).
- Storage layer with Room entities, DAOs, and repository for snapshots, trades, and events.
- Unit characterization tests (BtmmEngineTest, BtmmRepositoryTest, BtmmTypesCharacterizationTest).
- BTMM rules documentation (docs/btmm-rules.md).
- Navigation and live-data configuration ready.

## Not included
- Market data integration (live feed), broker connection, or order placement.
- Automated trade execution or backend services.
- Account management features within the app.
- Secrets embedded in source — credentials stored encrypted in preferences only.

## Next steps
1. Wire engine to Dashboard shell in BtmmApp.kt (state/gate badges, ghost trade display).
2. Add Settings screen for live data endpoint and API key (encrypted storage).
3. Test on physical Android device (or emulator with Play Services).
4. Privacy & security review: no keys in source, encrypted prefs only, manual-only disclaimer visible.
5. Prepare release: signed APK/AAB, device testing, Gradle cleanup.

## Completed phases
- 🔶 Initial shell scaffold (MainActivity + BtmmApp + four-tab Compose UI)
- 🔶 BTMM engine and types ported (9-rule gate evaluation)
- 🔶 Room storage entities, DAOs, and repository implemented
- 🔶 Unit characterization tests added
