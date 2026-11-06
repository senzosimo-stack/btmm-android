package com.btmm.mobile.btmm

import kotlin.math.max

/**
 * Values the user reads off their own chart and types in. null = not entered.
 * Nothing here is fetched from a feed, and nothing is assumed when a value is missing.
 */
data class ManualReading(
    val symbol: String = "",
    val timeframe: Timeframe = Timeframe.H1,
    val price: Double? = null,
    val ema8: Double? = null,
    val ema8Prev: Double? = null,
    val ema50: Double? = null,
    val ema200: Double? = null,
    val ema1200: Double? = null,
    val rsi: Double? = null,
    val tdiCross: TdiCross? = null,
    val mfi: Double? = null,
    val mfiPrev: Double? = null,
    val spreadPoints: Double? = null,
    val newsLocked: Boolean? = null,
    val dailyPl: Double? = null,
    val account: Double? = null,
    /** Optional. Only used to size the reference levels. */
    val atr: Double? = null
) {
    fun hasAnyInput(): Boolean =
        listOf(price, ema8, ema8Prev, ema50, ema200, ema1200, rsi, mfi, mfiPrev, spreadPoints, dailyPl, account, atr)
            .any { it != null } || tdiCross != null || newsLocked != null
}

data class RuleResult(val id: Int, val name: String, val status: RuleStatus, val detail: String)

/** Reference distances for a gate-open reading. Not an order, not advice. */
data class ReferenceLevels(
    val direction: Bias,
    val entry: Double,
    val stopLoss: Double,
    val takeProfit: Double,
    val riskAmount: Double
)

data class Assessment(
    val verdict: Verdict,
    val rules: List<RuleResult>,
    /** null = could not be determined from the entered values. */
    val bias: Bias?,
    /** null = could not be determined. */
    val score: Int?,
    /** 0..3 entries per docs/btmm-rules.md; null = could not be determined. */
    val entries: Int?,
    val reference: ReferenceLevels?,
    val errors: List<String>,
    val missingInputs: List<String>
) {
    val rulesPassed: Int get() = rules.count { it.status == RuleStatus.PASS }
    val rulesFailed: Int get() = rules.count { it.status == RuleStatus.FAIL }
    val rulesNotChecked: Int get() = rules.count { it.status == RuleStatus.MISSING }
}

object BtmmEngine {

    fun evaluate(r: ManualReading): Assessment {
        val errors = validate(r)
        if (errors.isNotEmpty()) {
            return Assessment(Verdict.INVALID, emptyList(), null, null, null, null, errors, emptyList())
        }
        if (!r.hasAnyInput()) {
            return Assessment(Verdict.NO_DATA, emptyList(), null, null, null, null, emptyList(), requiredMissing(r))
        }

        val bias = computeBias(r)
        val rules = listOf(
            rule1(bias), rule2(r, bias), rule3(r, bias), rule4(r), rule5(r, bias),
            rule6(r), rule7(r), rule8(bias), rule9(r, bias)
        )
        val missing = requiredMissing(r)
        val score = computeScore(r, bias)
        val entries = score?.let { entriesForScore(it) }

        val verdict = when {
            rules.any { it.status == RuleStatus.FAIL } -> Verdict.GATE_CLOSED
            rules.any { it.status == RuleStatus.MISSING } -> Verdict.INCOMPLETE
            else -> Verdict.GATE_OPEN
        }

        val reference =
            if (verdict == Verdict.GATE_OPEN && bias != null && bias != Bias.NONE &&
                score != null && score >= MIN_ENTRY_SCORE
            ) referenceLevels(r, bias) else null

        return Assessment(verdict, rules, bias, score, entries, reference, emptyList(), missing)
    }

    // ---- validation ----

    private fun validate(r: ManualReading): List<String> {
        val e = mutableListOf<String>()
        fun finite(label: String, v: Double?) {
            if (v != null && !v.isFinite()) e += "$label is not a finite number"
        }
        fun positive(label: String, v: Double?) {
            if (v != null && v.isFinite() && v <= 0.0) e += "$label must be greater than 0"
        }
        fun range0to100(label: String, v: Double?) {
            if (v != null && v.isFinite() && (v < 0.0 || v > 100.0)) e += "$label must be between 0 and 100"
        }
        listOf(
            "Price" to r.price, "EMA 8" to r.ema8, "EMA 8 (previous bar)" to r.ema8Prev,
            "EMA 50" to r.ema50, "EMA 200" to r.ema200, "EMA 1200" to r.ema1200,
            "RSI" to r.rsi, "MFI" to r.mfi, "MFI (previous bar)" to r.mfiPrev,
            "Spread" to r.spreadPoints, "Daily P/L" to r.dailyPl, "Account" to r.account, "ATR" to r.atr
        ).forEach { (label, v) -> finite(label, v) }

        listOf(
            "Price" to r.price, "EMA 8" to r.ema8, "EMA 8 (previous bar)" to r.ema8Prev,
            "EMA 50" to r.ema50, "EMA 200" to r.ema200, "EMA 1200" to r.ema1200,
            "Account" to r.account, "ATR" to r.atr
        ).forEach { (label, v) -> positive(label, v) }

        listOf("RSI" to r.rsi, "MFI" to r.mfi, "MFI (previous bar)" to r.mfiPrev)
            .forEach { (label, v) -> range0to100(label, v) }

        val s = r.spreadPoints
        if (s != null && s.isFinite() && s < 0.0) e += "Spread cannot be negative"
        return e
    }

    private fun requiredMissing(r: ManualReading): List<String> = buildList {
        if (r.price == null) add("Price")
        if (r.ema8 == null) add("EMA 8")
        if (r.ema8Prev == null) add("EMA 8 (previous bar)")
        if (r.ema50 == null) add("EMA 50")
        if (r.ema200 == null) add("EMA 200")
        if (r.ema1200 == null) add("EMA 1200")
        if (r.rsi == null) add("RSI")
        if (r.tdiCross == null) add("TDI cross")
        if (r.mfi == null) add("MFI")
        if (r.mfiPrev == null) add("MFI (previous bar)")
        if (r.spreadPoints == null) add("Spread")
        if (r.newsLocked == null) add("News lock")
        if (r.dailyPl == null) add("Daily P/L")
        if (r.account == null) add("Account")
    }

    // ---- bias ----

    /** null when any of price, EMA 8/50/200/1200 is missing: bias is unknown, not "none". */
    private fun computeBias(r: ManualReading): Bias? {
        val p = r.price ?: return null
        val e8 = r.ema8 ?: return null
        val e50 = r.ema50 ?: return null
        val e200 = r.ema200 ?: return null
        val e1200 = r.ema1200 ?: return null
        return when {
            p > e50 && p > e200 && p > e1200 && e8 > e50 -> Bias.LONG
            p < e50 && p < e200 && p < e1200 && e8 < e50 -> Bias.SHORT
            else -> Bias.NONE
        }
    }

    // ---- rules (docs/btmm-rules.md, "9-rule gate") ----

    private fun pass(id: Int, name: String, detail: String) = RuleResult(id, name, RuleStatus.PASS, detail)
    private fun fail(id: Int, name: String, detail: String) = RuleResult(id, name, RuleStatus.FAIL, detail)
    private fun missing(id: Int, name: String, needs: String) =
        RuleResult(id, name, RuleStatus.MISSING, "Not checked: needs $needs")

    private const val BIAS_NEEDS = "price and EMA 8, 50, 200, 1200"

    private fun rule1(bias: Bias?): RuleResult = when (bias) {
        null -> missing(1, "HTF 200/1200 fan", BIAS_NEEDS)
        Bias.NONE -> fail(1, "HTF 200/1200 fan", "Price and EMAs do not line up long or short")
        else -> pass(1, "HTF 200/1200 fan", "Bias is $bias")
    }

    private fun rule2(r: ManualReading, bias: Bias?): RuleResult {
        val name = "Price vs 50 EMA"
        val price = r.price
        val e50 = r.ema50
        if (bias == null || price == null || e50 == null) return missing(2, name, BIAS_NEEDS)
        return when {
            bias == Bias.NONE -> fail(2, name, "No bias to align with")
            bias == Bias.LONG && price > e50 -> pass(2, name, "Long and price is above EMA 50")
            bias == Bias.SHORT && price < e50 -> pass(2, name, "Short and price is below EMA 50")
            else -> fail(2, name, "Price is on the wrong side of EMA 50 for $bias")
        }
    }

    private fun rule3(r: ManualReading, bias: Bias?): RuleResult {
        val name = "8-EMA slope"
        val e8 = r.ema8
        val prev = r.ema8Prev
        if (bias == null || e8 == null || prev == null) return missing(3, name, "$BIAS_NEEDS, and EMA 8 (previous bar)")
        return when {
            bias == Bias.NONE -> fail(3, name, "No bias to align with")
            bias == Bias.LONG && e8 > prev -> pass(3, name, "EMA 8 is rising")
            bias == Bias.SHORT && e8 < prev -> pass(3, name, "EMA 8 is falling")
            else -> fail(3, name, "EMA 8 is not moving with $bias (flat counts as fail)")
        }
    }

    /** Fails on RSI < 30 or > 70, in either direction (matches the documented examples). */
    private fun rule4(r: ManualReading): RuleResult {
        val name = "TDI not extreme"
        val rsi = r.rsi ?: return missing(4, name, "RSI")
        return if (rsi < 30.0 || rsi > 70.0) fail(4, name, "RSI ${plain(rsi)} is outside 30 to 70")
        else pass(4, name, "RSI ${plain(rsi)} is inside 30 to 70")
    }

    private fun rule5(r: ManualReading, bias: Bias?): RuleResult {
        val name = "TDI cross with bias"
        val cross = r.tdiCross
        if (bias == null || cross == null) return missing(5, name, "$BIAS_NEEDS, and TDI cross")
        return when {
            bias == Bias.NONE -> fail(5, name, "No bias to align with")
            bias == Bias.LONG && cross == TdiCross.UP -> pass(5, name, "TDI crossed up")
            bias == Bias.SHORT && cross == TdiCross.DOWN -> pass(5, name, "TDI crossed down")
            else -> fail(5, name, "TDI cross is ${cross.name.lowercase()}, which does not match $bias")
        }
    }

    private fun rule6(r: ManualReading): RuleResult {
        val name = "Spread cap / news"
        val spread = r.spreadPoints
        val locked = r.newsLocked
        if (locked == true) return fail(6, name, "News is locked")
        if (spread != null && spread > MAX_SPREAD_POINTS) {
            return fail(6, name, "Spread ${plain(spread)} is above ${plain(MAX_SPREAD_POINTS)} points")
        }
        if (spread == null || locked == null) return missing(6, name, "spread and news lock")
        return pass(6, name, "Spread ${plain(spread)} is within ${plain(MAX_SPREAD_POINTS)} points and news is not locked")
    }

    /** Only a loss counts. A profitable day never fails this rule. */
    private fun rule7(r: ManualReading): RuleResult {
        val name = "Daily risk limit"
        val pl = r.dailyPl
        val account = r.account
        if (pl == null || account == null) return missing(7, name, "daily P/L and account")
        val lossPct = max(0.0, -pl) / account * 100.0
        return if (lossPct < MAX_DAILY_LOSS_PCT) pass(7, name, "Daily loss is ${plain(lossPct)}% (limit ${plain(MAX_DAILY_LOSS_PCT)}%)")
        else fail(7, name, "Daily loss is ${plain(lossPct)}% (limit ${plain(MAX_DAILY_LOSS_PCT)}%)")
    }

    /** Per docs this mirrors rule 1: there is no separate D1 50 EMA input yet. */
    private fun rule8(bias: Bias?): RuleResult = when (bias) {
        null -> missing(8, "D1 50 EMA confluence", BIAS_NEEDS)
        Bias.NONE -> fail(8, "D1 50 EMA confluence", "No bias (this rule currently mirrors rule 1)")
        else -> pass(8, "D1 50 EMA confluence", "Bias present (this rule currently mirrors rule 1)")
    }

    private fun rule9(r: ManualReading, bias: Bias?): RuleResult {
        val name = "MFI momentum"
        val mfi = r.mfi
        val prev = r.mfiPrev
        if (bias == null || mfi == null || prev == null) return missing(9, name, "$BIAS_NEEDS, MFI and MFI (previous bar)")
        return when {
            bias == Bias.NONE -> fail(9, name, "No bias to align with")
            bias == Bias.LONG && mfi > prev -> pass(9, name, "MFI is rising")
            bias == Bias.SHORT && mfi < prev -> pass(9, name, "MFI is falling")
            else -> fail(9, name, "MFI is not moving with $bias (flat counts as fail)")
        }
    }

    // ---- score and entries ----

    /**
     * Same formula as the prototype: 40 base, +10 each for price vs 50, price vs 200, EMA 8 vs 50
     * (always true once a bias exists), +10 RSI in 40..60, +10 MFI in 40..60.
     * So with a bias the result is 70, 80 or 90; below the 65 threshold is unreachable.
     * Returns null when it cannot be computed from what was entered.
     */
    private fun computeScore(r: ManualReading, bias: Bias?): Int? {
        if (bias == null) return null
        if (bias == Bias.NONE) return 0
        val rsi = r.rsi ?: return null
        val mfi = r.mfi ?: return null
        var score = 40 + 30
        if (rsi in 40.0..60.0) score += 10
        if (mfi in 40.0..60.0) score += 10
        return score
    }

    fun entriesForScore(score: Int): Int = when {
        score >= 86 -> 3
        score >= 76 -> 2
        score >= MIN_ENTRY_SCORE -> 1
        else -> 0
    }

    // ---- reference levels ----

    /** Needs ATR and account; SL = 1.5 x ATR, TP = SL x 1.5, risk = 1% of account. */
    private fun referenceLevels(r: ManualReading, bias: Bias): ReferenceLevels? {
        val price = r.price ?: return null
        val atr = r.atr ?: return null
        val account = r.account ?: return null
        val slDist = atr * SL_ATR_MULTIPLE
        val tpDist = slDist * MIN_RR
        val long = bias == Bias.LONG
        return ReferenceLevels(
            direction = bias,
            entry = price,
            stopLoss = if (long) price - slDist else price + slDist,
            takeProfit = if (long) price + tpDist else price - tpDist,
            riskAmount = account * RISK_PER_TRADE_PCT / 100.0
        )
    }

    private fun plain(v: Double): String = java.math.BigDecimal.valueOf(v).stripTrailingZeros().toPlainString()
}
