# BTMM rules — approved source and examples

## Source
- Prototype implementation: `reference/prototype/src/lib/btmm/engine.ts`
- Behavior reference: `docs/prototype-behavior.md`

## Rule evaluation order
The engine evaluates rules in a fixed order each tick. If any rule fails, the gate stays closed.

## 9-rule gate

| # | Rule name | Condition to pass | Example pass | Example fail |
|---|-----------|-------------------|--------------|--------------|
| 1 | HTF 200/1200 fan | Bias is not `none` | Bias = `long` | Bias = `none` |
| 2 | Price vs 50 EMA | Price aligns with bias vs EMA50 | Long bias, price > EMA50 | Short bias, price > EMA50 |
| 3 | 8-EMA slope | EMA8 direction matches bias | Long bias, EMA8 > EMA8Prev | Short bias, EMA8 > EMA8Prev |
| 4 | TDI not extreme | RSI not <30 or >70 against bias | Long bias, RSI = 45 | Long bias, RSI = 25 (oversold) |
| 5 | TDI cross with bias | TDI cross direction matches bias | Long bias, TDI up cross | Short bias, TDI up cross |
| 6 | Spread cap | Spread <= 30 pts and news not locked | Spread = 20, news unlocked | Spread = 40, or red news locked |
| 7 | Daily risk limit | Daily loss < 3% of account | Daily P/L = -1% | Daily P/L = -4% |
| 8 | D1 50 EMA confluence | Bias is not `none` (D1 filter) | Bias = `long` | Bias = `none` |
| 9 | MFI momentum | MFI direction matches bias | Long bias, MFI > MFIprev | Short bias, MFI > MFIprev |

## State classification priority
States are evaluated in this order; the first matching state wins.

| Priority | State | Conditions |
|----------|-------|------------|
| 1 | AR | Safety + TDI cross + EMA cross + bias aligned |
| 2 | Safety | Safety + (TDI or Cross) |
| 3 | TDI | TDI cross only |
| 4 | Cross | EMA cross only |
| 5 | Static | ATR contraction: ATR0 < ATR1 < ATR2 |
| 6 | Trap | 5-bar range < 1.5 * ATR0 and price near anchor zone |
| 7 | Anchor | Price near recent high or low |
| 8 | None | No state matches |

## Entry scoring tiers
Only evaluated when bias != `none` and score >= 65.

| Score range | Entries | Comment |
|-------------|---------|---------|
| 86–100 | 3 | TRADE_LONG_3 / TRADE_SHORT_3 |
| 76–85 | 2 | TRADE_LONG_2 / TRADE_SHORT_2 |
| 65–75 | 1 | TRADE_LONG_1 / TRADE_SHORT_1 |
| <65 | 0 | NO_SIGNAL |

## Risk model
- Fixed 1% risk per trade (no DWAT tiers in current prototype).
- SL distance = min(1.5 * ATR0, distance to today/yesterday lod/hod).
- TP distance = SL distance * MIN_RR (1.5).
- Lots = riskAmt / (SL distance in account currency).
- Breakeven: when peakR > 0.5, move SL to entry ± 0.1*ATR buffer.
- Trailing: when peakR > 1.5, trail SL at 1.5*ATR.

## News locks
- Red impact within 15 min: hard lock (rule 6 fails, no entries).
- Orange impact within 20 min: soft lock (spread rule still applies).

## Ghost trade
Present only when:
- Bias != `none`
- Score >= 65
- Gate is open

Ghost trade fields:
- `entry`: current price aligned to bias
- `sl`: entry ± SL distance
- `tp`: entry ± TP distance
- `lots`: sized for 1% risk
- `riskAmt`: 1% of account

## Scanner
- Evaluates all `PAIRS` symbols.
- Sorts by score descending.
- Comments derived from score range + bias.

## Manual-only constraint
- The Android app must NOT auto-place orders.
- Engine may suggest entries (ghost trade), but user must manually confirm.
- Trade Desk shows “No orders are sent. All decisions and execution remain manual, outside this app.”
