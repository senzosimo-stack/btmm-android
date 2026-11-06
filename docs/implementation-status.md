# Implementation status

## Included
- Four Compose tabs: Dashboard, Setup Monitor, Analysis Tools, and Trade Desk.
- Persistent, visible manual-only statement.
- UI tests for the manual-only boundary and Trade Desk copy.
- Basic app scaffold: MainActivity.kt + BtmmApp.kt entry point.

## Not included
- BTMM engine (rule evaluation, state classification, ghost trade generation).
- Storage layer (Room entities, database, DAOs, snapshot/trade/event persistence).
- Unit characterization tests (BtmmEngineTest, BtmmRepositoryTest, etc.).
- BTMM rules documentation (docs/btmm-rules.md not on this branch).
- Market data integration, broker connection, or order placement.
- Automated trade execution or backend services.

## Next steps
1. Add BTMM engine package (BtmmEngine.kt with 9-rule gate evaluation).
2. Add BTMM types package (BtmmTypes.kt with domain types).
3. Add Room storage entities (BtmmEntities.kt, BtmmDatabase.kt, BtmmRepository.kt, BtmmDaos.kt).
4. Add characterization/unit tests.
5. Add docs/btmm-rules.md with rule definitions and examples.
6. Wire engine to Dashboard shell in BtmmApp.kt.
7. Consider live data later (credential protection plan).
8. Prepare a release (Gradle fix, device testing, signing).

## Completed phases
- 🔶 Initial shell scaffold (MainActivity + BtmmApp + four-tab Compose UI).
