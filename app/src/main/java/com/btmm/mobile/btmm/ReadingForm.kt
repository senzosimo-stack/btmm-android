package com.btmm.mobile.btmm

/** Text the user typed. Kept as strings so half-typed values are never silently changed. */
data class FormState(
    val symbol: String = "",
    val timeframe: Timeframe = Timeframe.H1,
    val price: String = "",
    val ema8: String = "",
    val ema8Prev: String = "",
    val ema50: String = "",
    val ema200: String = "",
    val ema1200: String = "",
    val rsi: String = "",
    val tdiCross: TdiCross? = null,
    val mfi: String = "",
    val mfiPrev: String = "",
    val spreadPoints: String = "",
    val newsLocked: Boolean? = null,
    val dailyPl: String = "",
    val account: String = "",
    val atr: String = ""
)

data class ParsedForm(val reading: ManualReading, val errors: List<String>)

/** Accepts "1.0850" and "1,0850". Blank = not entered. Anything else that is not a finite number is an error. */
private fun parseNumber(label: String, text: String, errors: MutableList<String>): Double? {
    val t = text.trim()
    if (t.isEmpty()) return null
    val normalised = if (t.contains(',') && !t.contains('.')) t.replace(',', '.') else t
    val v = normalised.toDoubleOrNull()
    if (v == null || !v.isFinite()) {
        errors += "$label is not a number: \"$t\""
        return null
    }
    return v
}

fun FormState.parse(): ParsedForm {
    val errors = mutableListOf<String>()
    val reading = ManualReading(
        symbol = symbol.trim(),
        timeframe = timeframe,
        price = parseNumber("Price", price, errors),
        ema8 = parseNumber("EMA 8", ema8, errors),
        ema8Prev = parseNumber("EMA 8 (previous bar)", ema8Prev, errors),
        ema50 = parseNumber("EMA 50", ema50, errors),
        ema200 = parseNumber("EMA 200", ema200, errors),
        ema1200 = parseNumber("EMA 1200", ema1200, errors),
        rsi = parseNumber("RSI", rsi, errors),
        tdiCross = tdiCross,
        mfi = parseNumber("MFI", mfi, errors),
        mfiPrev = parseNumber("MFI (previous bar)", mfiPrev, errors),
        spreadPoints = parseNumber("Spread", spreadPoints, errors),
        newsLocked = newsLocked,
        dailyPl = parseNumber("Daily P/L", dailyPl, errors),
        account = parseNumber("Account", account, errors),
        atr = parseNumber("ATR", atr, errors)
    )
    return ParsedForm(reading, errors)
}

/** Unparseable text means INVALID: no rules are run on guessed values. */
fun FormState.assess(): Assessment {
    val parsed = parse()
    if (parsed.errors.isNotEmpty()) {
        return Assessment(Verdict.INVALID, emptyList(), null, null, null, null, parsed.errors, emptyList())
    }
    return BtmmEngine.evaluate(parsed.reading)
}

private fun fmt(v: Double?): String =
    if (v == null) "" else java.math.BigDecimal.valueOf(v).stripTrailingZeros().toPlainString()

fun ManualReading.toForm(): FormState = FormState(
    symbol = symbol, timeframe = timeframe,
    price = fmt(price), ema8 = fmt(ema8), ema8Prev = fmt(ema8Prev), ema50 = fmt(ema50),
    ema200 = fmt(ema200), ema1200 = fmt(ema1200), rsi = fmt(rsi), tdiCross = tdiCross,
    mfi = fmt(mfi), mfiPrev = fmt(mfiPrev), spreadPoints = fmt(spreadPoints),
    newsLocked = newsLocked, dailyPl = fmt(dailyPl), account = fmt(account), atr = fmt(atr)
)
