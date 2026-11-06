package com.btmm.mobile.btmm

/** Label only: which chart timeframe the reading was taken from. It does not change any rule. */
enum class Timeframe { M15, H1, H4 }

enum class Bias { NONE, LONG, SHORT }

/** The TDI cross the user sees on their chart. */
enum class TdiCross { UP, DOWN, NONE }

/**
 * Result of checking one rule against the values the user entered.
 * MISSING means the inputs the rule needs were not entered, so the rule has NOT been checked.
 */
enum class RuleStatus { PASS, FAIL, MISSING }

/**
 * Overall outcome. Only GATE_OPEN and GATE_CLOSED are verdicts about the entered values.
 * NO_DATA, INVALID and INCOMPLETE mean no verdict could be given.
 */
enum class Verdict { NO_DATA, INVALID, INCOMPLETE, GATE_CLOSED, GATE_OPEN }

// Constants from docs/btmm-rules.md
const val MAX_SPREAD_POINTS = 30.0
const val MAX_DAILY_LOSS_PCT = 3.0
const val RISK_PER_TRADE_PCT = 1.0
const val MIN_RR = 1.5
const val SL_ATR_MULTIPLE = 1.5
const val MIN_ENTRY_SCORE = 65
