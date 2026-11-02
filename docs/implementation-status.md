# Implementation status — initial shell

## Included
- Four Compose tabs: Dashboard, Setup Monitor, Analysis Tools, and Trade Desk.
- Persistent, visible manual-only statement.
- UI tests for the manual-only boundary and Trade Desk copy.

## Not included
- BTMM stages, gates, pattern checks, cycle/Fibonacci/EMA calculations.
- Trade ticket generation, broker connection, order placement, or automated execution.
- Market data, backend, persistence, account features, or cloud sync.

## Next steps
1. Generate compatible Android project scaffolding in Android Studio and integrate these source files.
2. Run lint, unit tests, build, and instrumented UI tests.
3. Add characterization tests from the supplied prototype before porting behavior.
4. Record an approved BTMM rule source and examples before implementing rule-specific evaluation.
