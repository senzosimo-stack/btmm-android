package com.btmm.mobile.btmm

import kotlin.math.*

object BtmmEngine {

    fun evaluate(world: World, pair: PairInfo): EngineSnap {
        val candles = world.candles
        if (candles.size < 200) return emptySnap(world, pair)

        val closes = candles.map { it.close }
        val highs = candles.map { it.high }
        val lows = candles.map { it.low }

        val ema8 = ema(closes, 8)
        val ema50 = ema(closes, 50)
        val ema200 = ema(closes, 200)
        val ema1200 = ema(closes, 1200)

        val atr = atr(candles, 14)
        val atrPips = atr / pair.pip
        val atrPct = atr / closes.last() * 100

        val rsi = rsi(closes, 14)
        val (tdiSig, tdiUp, tdiLo) = tdi(candles, closes)
        val mfi = mfi(candles, 14)

        val bias = computeBias(closes.last(), ema50, ema200, ema1200, ema8)
        val biasScore = computeBiasScore(bias, closes.last(), ema50, ema200, ema1200, ema8, rsi, mfi)

        val cond200 = if (bias == Bias.LONG) closes.last() > ema200 else closes.last() < ema200
        val cond50 = if (bias == Bias.LONG) closes.last() > ema50 else closes.last() < ema50
        val cond8 = if (bias == Bias.LONG) ema8 > ema8Prev(closes, 8) else ema8 < ema8Prev(closes, 8)

        val rules = evaluateRules(
            bias = bias,
            price = closes.last(),
            ema50 = ema50,
            ema8 = ema8,
            ema8Prev = ema8Prev(closes, 8),
            tdiSig = tdiSig,
            rsi = rsi,
            spread = world.liveStatus,
            account = world.account,
            dailyPL = world.dailyPL,
            mfi = mfi,
            mfiPrev = mfiPrev(candles, 14)
        )

        val gateOpen = rules.all { it.pass }
        val rulesPassed = rules.count { it.pass }

        val state = classifyState(
            atr = atr,
            candles = candles,
            price = closes.last(),
            safety = rules[6].pass, // placeholder; real safety is separate
            tdiCross = rules[4].pass,
            emaCross = cond8
        )

        val tier = scoreToTier(biasScore)
        val dwatScore = biasScore
        val riskPct = TIER_RISK[tier] ?: 1.0

        val tdiReload = when {
            rsi < 30 -> TdiReload.OVERSOLD
            rsi > 70 -> TdiReload.OVERBOUGHT
            atrPct < 0.1 -> TdiReload.SQUEEZE
            else -> TdiReload.NONE
        }

        val mfiConfirm = (bias == Bias.LONG && mfi > mfiPrev(candles, 14)) ||
                (bias == Bias.SHORT && mfi < mfiPrev(candles, 14))

        val today = dailyRange(candles.takeLast(1))
        val yest = dailyRange(candles.takeLast(2))

        val profile = volumeProfile(candles)

        val sessions = Sessions(
            asia = true, // placeholder
            london = true,
            ny = true,
            asiaHigh = highs.maxOrNull() ?: 0.0,
            asiaLow = lows.minOrNull() ?: 0.0
        )

        val ghost = if (bias != Bias.NONE && biasScore >= 65 && gateOpen) {
            val slDist = min(atr * 1.5, today.mid - today.low).coerceAtLeast(atr * 0.5)
            val tpDist = slDist * MIN_RR
            val entry = closes.last()
            val sl = if (bias == Bias.LONG) entry - slDist else entry + slDist
            val tp = if (bias == Bias.LONG) entry + tpDist else entry - tpDist
            val riskAmt = world.account * 1.0 / 100.0
            val lots = riskAmt / (slDist / pair.pip * 10) // rough lot calc
            GhostTrade(
                dir = bias,
                entry = entry,
                sl = sl,
                tp = tp,
                lots = lots,
                riskAmt = riskAmt,
                rr = MIN_RR
            )
        } else null

        val mtf = MtfBias(
            m15 = bias,
            h1 = bias,
            h4 = bias,
            d1 = bias
        )

        val news = world.events.lastOrNull { it.kind == EventKind.NEWS }?.let {
            NewsItem(
                title = it.title,
                impact = NewsImpact.RED,
                time = System.currentTimeMillis(),
                locked = false
            )
        }

        val hod = highs.maxOrNull() ?: 0.0
        val lod = lows.minOrNull() ?: 0.0

        return EngineSnap(
            price = closes.last(),
            bid = closes.last() - world.liveStatus.name.length.toDouble() * 0.0001,
            ask = closes.last() + world.liveStatus.name.length.toDouble() * 0.0001,
            spreadPts = if (world.liveStatus == LiveStatus.CONNECTED) 15 else 0,
            ema8 = ema8,
            ema50 = ema50,
            ema200 = ema200,
            ema1200 = ema1200,
            ema8Prev = ema8Prev(closes, 8),
            atr = atr,
            atrPips = atrPips,
            atrPct = atrPct,
            rsi = rsi,
            tdiSig = tdiSig,
            tdiUp = tdiUp,
            tdiLo = tdiLo,
            mfi = mfi,
            bias = bias,
            biasScore = biasScore,
            cond200 = cond200,
            cond50 = cond50,
            cond8 = cond8,
            state = state,
            rules = rules,
            gateOpen = gateOpen,
            rulesPassed = rulesPassed,
            tier = tier,
            dwatScore = dwatScore,
            riskPct = riskPct,
            tdiReload = tdiReload,
            mfiConfirm = mfiConfirm,
            today = today,
            yest = yest,
            profile = profile,
            sessions = sessions,
            ghost = ghost,
            mtf = mtf,
            news = news,
            hod = hod,
            lod = lod
        )
    }

    private fun emptySnap(world: World, pair: PairInfo): EngineSnap {
        return EngineSnap(
            price = 0.0,
            bid = 0.0,
            ask = 0.0,
            spreadPts = 0,
            ema8 = 0.0,
            ema50 = 0.0,
            ema200 = 0.0,
            ema1200 = 0.0,
            ema8Prev = 0.0,
            atr = 0.0,
            atrPips = 0.0,
            atrPct = 0.0,
            rsi = 0.0,
            tdiSig = 0.0,
            tdiUp = 0.0,
            tdiLo = 0.0,
            mfi = 0.0,
            bias = Bias.NONE,
            biasScore = 0,
            cond200 = false,
            cond50 = false,
            cond8 = false,
            state = BtmmState.NONE,
            rules = emptyList(),
            gateOpen = false,
            rulesPassed = 0,
            tier = DwatTier.T1,
            dwatScore = 0,
            riskPct = 1.0,
            tdiReload = TdiReload.NONE,
            mfiConfirm = false,
            today = DailyRange(0.0, 0.0, 0.0, 0.0),
            yest = DailyRange(0.0, 0.0, 0.0, 0.0),
            profile = VolumeArea(0.0, 0.0, 0.0, null, 0),
            sessions = Sessions(false, false, false, 0.0, 0.0),
            ghost = null,
            mtf = MtfBias(Bias.NONE, Bias.NONE, Bias.NONE, Bias.NONE),
            news = null,
            hod = 0.0,
            lod = 0.0
        )
    }

    private fun ema(values: List<Double>, period: Int): Double {
        if (values.size < period) return values.lastOrNull() ?: 0.0
        val k = 2.0 / (period + 1)
        var ema = values.take(period).average()
        for (i in period until values.size) {
            ema = values[i] * k + ema * (1 - k)
        }
        return ema
    }

    private fun ema8Prev(values: List<Double>, period: Int): Double {
        if (values.size < period + 1) return values.lastOrNull() ?: 0.0
        val k = 2.0 / (period + 1)
        var ema = values.take(period).average()
        for (i in period until values.size - 1) {
            ema = values[i] * k + ema * (1 - k)
        }
        return ema
    }

    private fun atr(candles: List<Candle>, period: Int): Double {
        if (candles.size < 2) return 0.0
        val trs = candles.zipWithNext { a, b ->
            maxOf(
                b.high - b.low,
                abs(b.high - a.close),
                abs(b.low - a.close)
            )
        }
        if (trs.isEmpty()) return 0.0
        return trs.takeLast(period).average()
    }

    private fun rsi(values: List<Double>, period: Int): Double {
        if (values.size < period + 1) return 50.0
        val changes = values.zipWithNext { a, b -> b - a }
        val gains = changes.map { if (it > 0) it else 0.0 }
        val losses = changes.map { if (it < 0) -it else 0.0 }
        val avgGain = gains.takeLast(period).average()
        val avgLoss = losses.takeLast(period).average()
        if (avgLoss == 0.0) return 100.0
        val rs = avgGain / avgLoss
        return 100 - 100 / (1 + rs)
    }

    private fun tdi(candles: List<Candle>, closes: List<Double>): Triple<Double, Double, Double> {
        val rsiLine = rsi(closes, 14)
        val signal = ema(closes, 18) // placeholder signal
        val up = rsiLine + 5
        val lo = rsiLine - 5
        return Triple(signal, up, lo)
    }

    private fun mfi(candles: List<Candle>, period: Int): Double {
        if (candles.size < period + 1) return 50.0
        val typical = candles.map { (it.high + it.low + it.close) / 3 }
        val raw = typical.zip(candles.map { it.volume }) { p, v -> p * v }
        val changes = raw.zipWithNext { a, b -> b - a }
        val pos = changes.filter { it > 0 }.sum()
        val neg = changes.filter { it < 0 }.sum().absoluteValue
        if (neg == 0.0) return 100.0
        val mfRatio = pos / neg
        return 100 - 100 / (1 + mfRatio)
    }

    private fun mfiPrev(candles: List<Candle>, period: Int): Double {
        if (candles.size < period + 2) return 50.0
        val sub = candles.dropLast(1)
        return mfi(sub, period)
    }

    private fun computeBias(price: Double, ema50: Double, ema200: Double, ema1200: Double, ema8: Double): Bias {
        return when {
            price > ema50 && price > ema200 && price > ema1200 && ema8 > ema50 -> Bias.LONG
            price < ema50 && price < ema200 && price < ema1200 && ema8 < ema50 -> Bias.SHORT
            else -> Bias.NONE
        }
    }

    private fun computeBiasScore(
        bias: Bias,
        price: Double,
        ema50: Double,
        ema200: Double,
        ema1200: Double,
        ema8: Double,
        rsi: Double,
        mfi: Double
    ): Int {
        if (bias == Bias.NONE) return 0
        var score = 40
        if (bias == Bias.LONG && price > ema50) score += 10
        if (bias == Bias.SHORT && price < ema50) score += 10
        if (bias == Bias.LONG && price > ema200) score += 10
        if (bias == Bias.SHORT && price < ema200) score += 10
        if (bias == Bias.LONG && ema8 > ema50) score += 10
        if (bias == Bias.SHORT && ema8 < ema50) score += 10
        if (rsi in 40.0..60.0) score += 10
        if (mfi in 40.0..60.0) score += 10
        return score.coerceIn(0, 100)
    }

    private fun evaluateRules(
        bias: Bias,
        price: Double,
        ema50: Double,
        ema8: Double,
        ema8Prev: Double,
        tdiSig: Double,
        rsi: Double,
        spread: LiveStatus,
        account: Double,
        dailyPL: Double,
        mfi: Double,
        mfiPrev: Double
    ): List<RuleResult> {
        val newsLocked = false // placeholder
        val spreadOk = spread != LiveStatus.CONNECTED || true // placeholder spread check
        val riskOk = abs(dailyPL) / account * 100 < MAX_DAILY_RISK

        return listOf(
            RuleResult(1, "HTF fan", "R1", bias != Bias.NONE),
            RuleResult(2, "Price vs 50", "R2", (bias == Bias.LONG && price > ema50) || (bias == Bias.SHORT && price < ema50)),
            RuleResult(3, "8-EMA slope", "R3", (bias == Bias.LONG && ema8 > ema8Prev) || (bias == Bias.SHORT && ema8 < ema8Prev)),
            RuleResult(4, "TDI not extreme", "R4", !((bias == Bias.LONG && rsi < 30) || (bias == Bias.LONG && rsi > 70) || (bias == Bias.SHORT && rsi > 70) || (bias == Bias.SHORT && rsi < 30))),
            RuleResult(5, "TDI cross", "R5", (bias == Bias.LONG && tdiUp > tdiSig) || (bias == Bias.SHORT && tdiUp < tdiSig)),
            RuleResult(6, "Spread cap", "R6", spreadOk && !newsLocked),
            RuleResult(7, "Daily risk", "R7", riskOk),
            RuleResult(8, "D1 50 confluence", "R8", bias != Bias.NONE),
            RuleResult(9, "MFI momentum", "R9", (bias == Bias.LONG && mfi > mfiPrev) || (bias == Bias.SHORT && mfi < mfiPrev))
        )
    }

    private fun classifyState(
        atr: Double,
        candles: List<Candle>,
        price: Double,
        safety: Boolean,
        tdiCross: Boolean,
        emaCross: Boolean
    ): BtmmState {
        val atr0 = atr
        val atr1 = atr * 1.1
        val atr2 = atr * 1.2
        val range5 = candles.takeLast(5).maxOf { it.high } - candles.takeLast(5).minOf { it.low }

        return when {
            safety && tdiCross && emaCross -> BtmmState.AR
            safety && (tdiCross || emaCross) -> BtmmState.SAFETY
            tdiCross -> BtmmState.TDI
            emaCross -> BtmmState.CROSS
            atr0 < atr1 && atr1 < atr2 -> BtmmState.STATIC
            range5 < 1.5 * atr0 -> BtmmState.TRAP
            else -> BtmmState.ANCHOR
        }
    }

    private fun scoreToTier(score: Int): DwatTier {
        return when {
            score >= 86 -> DwatTier.T7
            score >= 76 -> DwatTier.T6
            score >= 65 -> DwatTier.T5
            score >= 50 -> DwatTier.T3
            score >= 35 -> DwatTier.T2
            else -> DwatTier.T1
        }
    }

    private fun dailyRange(candles: List<Candle>): DailyRange {
        if (candles.isEmpty()) return DailyRange(0.0, 0.0, 0.0, 0.0)
        val high = candles.maxOf { it.high }
        val low = candles.minOf { it.low }
        val range = high - low
        return DailyRange(high, low, range, (high + low) / 2)
    }

    private fun volumeProfile(candles: List<Candle>): VolumeArea {
        if (candles.isEmpty()) return VolumeArea(0.0, 0.0, 0.0, null, 0)
        val vah = candles.maxOf { it.high }
        val val = candles.minOf { it.low }
        val poc = (vah + val) / 2
        return VolumeArea(vah, poc, val, null, 0)
    }
}
