# Implementation status

First version is local-only and manual-only: the user types values from their own chart, the app checks them against the documented rules, and readings are saved on the device. No live data, no broker, no orders, no cloud, no credentials.

## Built
- Rules engine (`BtmmEngine.kt`) with explicit results per rule: PASS, FAIL, or NOT CHECKED (input missing).
- Overall outcomes: No data, Invalid input, Incomplete, Gate closed, Gate open. Only the last two are verdicts, and "Gate closed" is only given when a rule actually failed.
- Setup Monitor: manual input form, live result card, save / update / clear.
- Dashboard: latest saved reading and its recomputed result.
- Analysis Tools: saved readings list with edit and delete.
- Trade Desk: manual-only statement, plus reference levels (entry, stop, target, 1% risk amount) only when the form is Gate open and an ATR was entered. No position size is calculated.
- Room storage (`readings` table): stores only typed inputs; results are recomputed on display.
- Manual-only statement is visible on every tab.

## Checked
- Engine, form parsing and repository logic: 37 JVM unit tests (`BtmmEngineTest`, `ReadingFormTest`, `ReadingRepositoryTest`), run outside Gradle with a plain Kotlin compile.
- Mutation check: reintroducing the old daily-risk and RSI bugs makes the tests fail.

## Not yet checked (needs Android Studio / a device)
- `.\gradlew.bat testDebugUnitTest lintDebug assembleDebug` has not been run.
- Compose UI, `ReadingsViewModel`, `MainActivity` and Room (`BtmmDatabase`, DAO) have never been compiled.
- `connectedDebugAndroidTest` (`SetupMonitorFlowTest`, `ManualOnlyBoundaryTest`, `ReadingDaoTest`) has never been run.
- No walkthrough on an emulator or phone.

## Known limits (by design for now)
- Rule 8 (D1 50 EMA) mirrors rule 1, as in the docs. There is no separate D1 input.
- Rule 4 fails on RSI < 30 or > 70 in either direction (matches the documented examples).
- With a bias, the score can only be 70, 80 or 90, so the "below 65" branch is unreachable. This is the prototype formula carried over.
- Market state (Anchor, Trap, Static, Cross, TDI, Safety, AR) is not computed: it needs candle history, which this version does not take.
- Stop distance uses 1.5 x ATR only; the documented cap by day high/low is not applied.
- Breakeven and trailing rules are not applicable (no positions are tracked).
- Delete has no confirmation prompt.

## Removed in this version
- The previous engine (did not compile; placeholder spread check, news lock and TDI cross).
- The live-data Settings screen and encrypted-prefs code: live feeds and credentials are out of scope for the first version.
- Unused navigation3 and security-crypto dependencies.
- Tests that asserted constants against themselves.
- Backup is switched off (`allowBackup="false"`) so saved readings stay on the device.

## Next
1. Run the build and tests locally, fix whatever fails.
2. Walk through the app on a device: enter, view, save, reopen, edit, delete.
3. Decide the live-data architecture and privacy requirements before adding any feed.
4. Signing and release only after the above pass.
