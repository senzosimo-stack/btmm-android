# Live data plan

## Goal
Verify that any external data source can be reached from the Android app, then define how credentials will be protected without embedding secrets in the binary.

## Current state
- No broker connection or order placement in this release.
- No provider API keys are committed to the repository.
- Live data integration is deferred until the manual-only core is stable.

## Candidate sources
1. **Local prototype server**  
   The BTMM web prototype runs a Python HTTP server on `http://localhost:8081`.  
   - Verification: run the prototype server and call `/health` or equivalent from the Android device/emulator.  
   - Constraint: only works on the same network; not suitable for production, but useful for lab validation.

2. **MT5 / broker API**  
   - Verification: confirm MT5 terminal is running, enable the local MetaTrader 5 Python bridge, and test a read-only request (e.g., `symbol_info`).  
   - Constraint: never embed account credentials or API tokens in the app.

3. **Public market-data API**  
   - Verification: pick a provider, register a free or test tier key, and call a public endpoint from the device.  
   - Constraint: key must be supplied by the user at runtime.

## Credential protection
- Store user-supplied keys in `EncryptedSharedPreferences` backed by the Android Keystore.
- Never hardcode keys, tokens, or passwords in source code, resources, or build configs.
- Rotate keys outside the app; the app only references what the user enters.

## Verification steps
1. Add a **Settings** screen where the user can paste an API key or endpoint URL.
2. Save the value in encrypted local storage.
3. From the app, attempt a lightweight network call (e.g., `GET /health`) to the configured endpoint.
4. Show success/failure in the UI; do not retry silently or log secrets.

## Release boundary
- Live data is optional and off by default.
- The app ships without any preconfigured endpoint or key.
- If the user does not configure a source, the app operates in manual-only mode.
