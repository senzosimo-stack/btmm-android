# BTMM Android

Offline-first Android companion for BTMM. Kotlin, Jetpack Compose, Material 3, Room. Package `com.btmm.mobile`.

The user types chart values by hand. The app checks them and can store a local journal. The user makes every trading decision and places any trade manually outside the app.

## What this branch contains

Branch `feature/android-compose-shell` is a Gradle project (wrapper included). It is not a source-only starter that still needs an Empty Activity scaffold.

- Four tabs: Dashboard, Setup Monitor, Analysis Tools, Trade Desk.
- Manual-only notice on every tab. No broker, orders, bots, accounts, cloud sync, or live feed.
- Manual-input rules check in `BtmmEngine.kt` (pass / fail / not checked). Gate open only when every rule passes.
- Room `readings` table. Saved values are the typed inputs; results are recomputed on display.
- Unit tests for the engine, form parsing, and repository. Instrumented tests exist and have not been run on a device from this environment.

`docs/implementation-status.md` is the status of what is built versus what is still unchecked. `docs/btmm-rules.md` describes the prototype rules. The prototype is not in this repository; keep it unchanged under `reference/` when it is added. Do not treat the engine as a verified canonical BTMM implementation.

## Build

Open the project in Android Studio so it can write `local.properties` with the SDK path. Do not commit that file. JDK 17.

```bash
./gradlew testDebugUnitTest lintDebug assembleDebug
./gradlew connectedDebugAndroidTest
```

The second command needs an emulator or a device. A Gradle run in an environment without the Android SDK fails at "SDK location not found" before compile. No successful `assembleDebug` is claimed until that command passes on a machine with the SDK.

## Safety

Do not add order placement, broker APIs, automated trading, or background execution. Keep the first release local-only. Never put market-data provider secrets in the Android client.
