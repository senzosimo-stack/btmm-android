# BTMM Android

Independent Android application starter for BTMM, built with Kotlin and Jetpack Compose.

## Phase 1: app shell

This branch contains a four-tab placeholder shell: Dashboard, Setup Monitor, Analysis Tools, and Trade Desk. It displays the manual-only boundary. It does not implement BTMM rules, market data, persistence, broker connectivity, or trade execution.

The uploaded PWA/Python project is a behavioral reference, not a verified canonical source for BTMM rule correctness. Preserve it unchanged when added under `reference/`. Do not implement rule-derived evaluations until the approved source and examples are documented and tests are prepared.

## Create and run the Android project

These committed files are source starter files, not a complete Android Studio/Gradle project. To generate the platform scaffolding:

1. In Android Studio, create an **Empty Activity** project using Kotlin and Jetpack Compose.
2. Use `com.btmm.mobile` as the provisional application ID/package, or update all package declarations consistently.
3. Use the compatible stable Gradle, Android Gradle Plugin, Kotlin, and JDK versions selected by Android Studio.
4. Ensure Compose Material 3, Compose UI testing, and the AndroidX test runner dependencies are enabled.
5. Merge the `app/src/` files from this repository into the generated app module; keep Android Studio's generated Gradle files and wrapper.
6. Run on an emulator, then run:

```bash
./gradlew testDebugUnitTest lintDebug assembleDebug
./gradlew connectedDebugAndroidTest
```

The second command requires a running emulator or connected Android device. Build and test results must be checked locally; no build is claimed by this source-only starter.

## Safety and scope

The app may eventually record, calculate, and present information, but the user makes every trading decision and places any order manually outside the app. Do not add order placement, broker APIs, automated trading, bots, or background trade execution. Keep the first release local-only. Never place provider API keys or credentials in the Android client.
