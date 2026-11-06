# Prototype behavior reference

## Source
- Location: `reference/prototype/`
- Framework: React + TypeScript (Vite + TanStack Router)
- State: Zustand store (`src/store/btmm-store.ts`)
- Engine: Pure TS simulation + live-feed modules under `src/lib/btmm/`

## 1. Screens and navigation
- Single-page terminal UI (`Terminal` component).
- Header bar with:
  - Symbol selector (dropdown, all PAIRS).
  - Timeframe selector: M15 / H1 / H4.
  - Mode toggle: Live / Sim.
  - Density toggle: Full / Compact / Tiny.
  - Pause toggle, force-scan, flatten/manual-entry shortcuts.
- Main area (non-compact):
  - State pipeline strip (ANCHOR → TRAP → STATIC → CROSS → TDI → SAFETY → AR).
  - Bar countdown + confluence dots row.
  - Price chart (canvas-based, interactive: zoom, pan, tick dots).
  - Positions card + Journal card grid.
- Right sidebar:
  - Account card (equity, daily P/L).
  - Bias card (long/short/none, score, conditions 200/50/8).
  - Gate card (9 rules pass/fail, gate open/closed).
  - TDI card (RSI, signal, reload type).
  - P/L card.
  - Scanner (multi-pair ranked list with score, bias, comment).
  - Stats card.
- Help overlay (`?` key) describing shortcuts.
- Toast stack for events (state transitions, entries, exits, scans, news).

## 2. Inputs and user actions
- Symbol picker: switches current symbol and candle series.
- Timeframe picker: switches TF, regenerates candle data.
- Live/Sim toggle: connects/disconnects live feed.
- Pause/Resume: stops bar progression.
- Density cycle: changes layout density.
- Force scan: refreshes multi-pair scanner.
- Flatten: closes all positions.
- Manual entry: opens position based on current ghost trade.
- Keyboard shortcuts:
  - `?` / `Shift+/`: toggle help
  - `P`: pause/resume
  - `S`: force scan
  - `C`: flatten/close all
  - `H`: cycle density

## 3. Outputs and data flow
- `EngineSnap` recomputed each tick/bar:
  - bias (long/short/none), biasScore (0–100).
  - 9 rule results with pass/fail.
  - state machine value (anchor/trap/static/cross/tdi/safety/ar/none).
  - ATR, EMAs, RSI/TDI, MFI.
  - ghost trade (entry/SL/TP/lots/risk) when bias != none and score >= 65.
  - MTF bias (M15/H1/H4/D1).
  - sessions (asia/london/ny flags + asia high/low).
  - news item (title, impact, locked flag).
  - volume profile (VAH/POC/VAL/nPOC).
- World state:
  - candles per pair, positions array, closed trades.
  - equity curve, daily P/L.
  - scanner rows sorted by score.
  - events log, state log, visited states.
- Live tick updates:
  - bid/ask prices, live tick array (last 60 ticks).
  - live pairs list.
  - account info (balance, equity, margin, leverage).
- Trade actions:
  - Sim: paper positions with ticket IDs, SL/TP management, breakeven, trailing.
  - Live: real MT5 orders via HTTP server (`server/routes/api/mt5/index.ts`).
  - Auto-entry: when score >= 65, maybeEnter opens 1/2/3 entries based on score tiers.

## 4. Core behaviors and rules
- 9 rules evaluated each tick:
  1. HTF 200/1200 fan (bias not none).
  2. Price vs 50 EMA.
  3. 8-EMA slope direction.
  4. TDI not extreme vs bias.
  5. TDI cross with bias.
  6. Spread cap (<= 30 pts, news not locked).
  7. Daily risk limit (< 3% of account).
  8. D1 50 EMA confluence.
  9. MFI momentum direction.
- Gate opens when all 9 pass.
- State classification priority:
  1. AR: safety + TDI cross + EMA cross + bias-aligned.
  2. Safety: safety + (TDI or cross).
  3. TDI: TDI cross only.
  4. Cross: EMA cross only.
  5. Static: ATR contraction (ATR0 < ATR1 < ATR2).
  6. Trap: 5-bar range < 1.5*ATR0 and price near anchor zone.
  7. Anchor: price near recent high/low.
- Entry scoring:
  - 86–100: 3 entries.
  - 76–85: 2 entries.
  - 65–75: 1 entry.
  - < 65: no trade.
- Risk:
  - Fixed 1% risk per trade (no DWAT tiers in current prototype).
  - SL = today/yesterday lod/hod minus/plus ATR buffer, capped at 1.5*ATR.
  - TP = entry ± risk * 1.5.
  - Lots sized to risk 1% of account.
- Position management:
  - Breakeven: peakR > 0.5 → SL moved to entry ± 0.1*ATR buffer.
  - Trail: peakR > 1.5 → SL trails at 1.5*ATR.
  - Exit: hit SL/TP, then close position, update daily P/L, equity.
- News:
  - Hard-lock during red-impact events within 15 min.
  - Soft-lock for orange within 20 min (spread rule still applies).
- Scanner:
  - Evaluates all PAIRS, sorts by score descending.
  - Comments: TRADE_LONG/SHORT_3, TRADE_LONG/SHORT_2, TRADE_LONG/SHORT_1, STRONG_LONG/SHORT, LONG/SHORT_BUILD, WEAK_LONG/SHORT, NO_SIGNAL.

## 5. Edge cases
- Insufficient candles: engine returns conservative defaults, no crash.
- Zero/negative ATR: fallback to pip value for SL/TP distances.
- Spread > MAX_SPREAD: rule 6 fails, gate stays closed.
- Daily loss >= 3% account: rule 7 fails, gate stays closed.
- News locked: rule 6 fails, no entries.
- Score below 65: no ghost trade, no auto-entries.
- Position already at max for symbol: skip additional entries.
- Live feed failure: retry loop, UI shows connecting/disconnected.
- MT5 order failure: order removed from positions, error logged.

## 6. Android app intended matches
- All screens, panels, cards, and layout structure from the prototype.
- Same 9-rule evaluation, state machine, scoring, and scoring tiers.
- Same risk model (fixed 1%, breakeven, trailing).
- Same scanner behavior (multi-pair, ranked, score thresholds).
- Same keyboard shortcuts mapped to touch/button equivalents.
- Manual-only trade execution (no automated order placement from engine).
- Local storage for density, mode, trade mode, and journal/statements.
- No cloud sync in v1.

## 7. Secrets and API keys
- The prototype contains no hardcoded API keys or broker credentials.
- Live data path uses a local MT5 HTTP server on localhost:8081; the Android app must not embed remote provider keys.
- If any future live-data provider is added, credentials must be stored in secure Android storage (Keystore/EncryptedSharedPreferences), never in code or plain text.
