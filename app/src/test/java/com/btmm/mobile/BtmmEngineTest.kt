package com.btmm.mobile

import com.btmm.mobile.btmm.Assessment
import com.btmm.mobile.btmm.Bias
import com.btmm.mobile.btmm.BtmmEngine
import com.btmm.mobile.btmm.ManualReading
import com.btmm.mobile.btmm.RuleStatus
import com.btmm.mobile.btmm.TdiCross
import com.btmm.mobile.btmm.Verdict
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BtmmEngineTest {

    /** A reading where all 9 rules pass, long. Tests change one thing at a time from this. */
    private fun openLong() = ManualReading(
        symbol = "EURUSD",
        price = 1.0850, ema8 = 1.0848, ema8Prev = 1.0846, ema50 = 1.0830, ema200 = 1.0800, ema1200 = 1.0700,
        rsi = 55.0, tdiCross = TdiCross.UP, mfi = 55.0, mfiPrev = 50.0,
        spreadPoints = 20.0, newsLocked = false, dailyPl = -100.0, account = 10000.0, atr = 0.0010
    )

    private fun openShort() = ManualReading(
        symbol = "EURUSD",
        price = 1.0650, ema8 = 1.0652, ema8Prev = 1.0654, ema50 = 1.0670, ema200 = 1.0700, ema1200 = 1.0800,
        rsi = 45.0, tdiCross = TdiCross.DOWN, mfi = 45.0, mfiPrev = 50.0,
        spreadPoints = 20.0, newsLocked = false, dailyPl = 0.0, account = 10000.0, atr = 0.0010
    )

    private fun Assessment.rule(id: Int) = rules.first { it.id == id }.status

    // ---- no data / invalid / incomplete ----

    @Test
    fun emptyReading_isNoData_andNeverAVerdict() {
        val a = BtmmEngine.evaluate(ManualReading())
        assertEquals(Verdict.NO_DATA, a.verdict)
        assertTrue(a.rules.isEmpty())
        assertNull(a.bias); assertNull(a.score); assertNull(a.reference)
    }

    @Test
    fun symbolAndTimeframeAloneAreNotData() {
        assertEquals(Verdict.NO_DATA, BtmmEngine.evaluate(ManualReading(symbol = "EURUSD")).verdict)
    }

    @Test
    fun partialReading_isIncomplete_withMissingInputsListed() {
        val a = BtmmEngine.evaluate(ManualReading(price = 1.0850, ema50 = 1.0830))
        assertEquals(Verdict.INCOMPLETE, a.verdict)
        assertTrue(a.missingInputs.contains("EMA 200"))
        assertTrue(a.missingInputs.contains("Account"))
        assertNull(a.bias)
        assertNull(a.reference)
    }

    @Test
    fun missingBias_marksBiasRulesNotChecked_notFailed() {
        val a = BtmmEngine.evaluate(ManualReading(rsi = 50.0))
        assertEquals(RuleStatus.MISSING, a.rule(1))
        assertEquals(RuleStatus.MISSING, a.rule(2))
        assertEquals(RuleStatus.PASS, a.rule(4))
        assertEquals(Verdict.INCOMPLETE, a.verdict)
    }

    @Test
    fun aKnownFailure_closesTheGate_evenWithOtherInputsMissing() {
        val a = BtmmEngine.evaluate(ManualReading(rsi = 85.0))
        assertEquals(RuleStatus.FAIL, a.rule(4))
        assertEquals(Verdict.GATE_CLOSED, a.verdict)
    }

    @Test
    fun invalidValues_giveInvalid_andRunNoRules() {
        listOf(
            openLong().copy(rsi = 120.0), openLong().copy(rsi = -1.0), openLong().copy(mfi = 101.0),
            openLong().copy(account = 0.0), openLong().copy(account = -5.0), openLong().copy(price = 0.0),
            openLong().copy(spreadPoints = -1.0), openLong().copy(atr = 0.0),
            openLong().copy(price = Double.NaN), openLong().copy(ema50 = Double.POSITIVE_INFINITY)
        ).forEach { r ->
            val a = BtmmEngine.evaluate(r)
            assertEquals(Verdict.INVALID, a.verdict)
            assertTrue(a.errors.isNotEmpty())
            assertTrue(a.rules.isEmpty())
            assertNull(a.reference)
        }
    }

    // ---- gate ----

    @Test
    fun allNineRulesPass_long_opensGate() {
        val a = BtmmEngine.evaluate(openLong())
        assertEquals(Verdict.GATE_OPEN, a.verdict)
        assertEquals(Bias.LONG, a.bias)
        assertEquals(9, a.rulesPassed)
        assertEquals(0, a.rulesFailed)
    }

    @Test
    fun allNineRulesPass_short_opensGate() {
        val a = BtmmEngine.evaluate(openShort())
        assertEquals(Verdict.GATE_OPEN, a.verdict)
        assertEquals(Bias.SHORT, a.bias)
    }

    @Test
    fun mixedEmas_giveBiasNone_andCloseGate() {
        val a = BtmmEngine.evaluate(openLong().copy(price = 1.0750)) // below ema200 but above ema1200
        assertEquals(Bias.NONE, a.bias)
        assertEquals(Verdict.GATE_CLOSED, a.verdict)
        assertEquals(RuleStatus.FAIL, a.rule(1))
        assertEquals(RuleStatus.FAIL, a.rule(8))
        assertEquals(0, a.score)
        assertNull(a.reference)
    }

    // ---- rule by rule, one change from the open reading ----

    @Test
    fun rule3_flatOrWrongSlope_fails() {
        assertEquals(RuleStatus.FAIL, BtmmEngine.evaluate(openLong().copy(ema8Prev = 1.0848)).rule(3))
        assertEquals(RuleStatus.FAIL, BtmmEngine.evaluate(openLong().copy(ema8Prev = 1.0850)).rule(3))
    }

    @Test
    fun rule4_boundaries() {
        assertEquals(RuleStatus.PASS, BtmmEngine.evaluate(openLong().copy(rsi = 30.0)).rule(4))
        assertEquals(RuleStatus.PASS, BtmmEngine.evaluate(openLong().copy(rsi = 70.0)).rule(4))
        assertEquals(RuleStatus.FAIL, BtmmEngine.evaluate(openLong().copy(rsi = 29.9)).rule(4))
        assertEquals(RuleStatus.FAIL, BtmmEngine.evaluate(openLong().copy(rsi = 70.1)).rule(4))
        assertEquals(RuleStatus.FAIL, BtmmEngine.evaluate(openLong().copy(rsi = 25.0)).rule(4)) // docs example
    }

    @Test
    fun rule5_crossMustMatchBias() {
        assertEquals(RuleStatus.FAIL, BtmmEngine.evaluate(openLong().copy(tdiCross = TdiCross.DOWN)).rule(5))
        assertEquals(RuleStatus.FAIL, BtmmEngine.evaluate(openLong().copy(tdiCross = TdiCross.NONE)).rule(5))
        assertEquals(RuleStatus.FAIL, BtmmEngine.evaluate(openShort().copy(tdiCross = TdiCross.UP)).rule(5))
    }

    @Test
    fun rule6_spreadAndNews() {
        assertEquals(RuleStatus.PASS, BtmmEngine.evaluate(openLong().copy(spreadPoints = 30.0)).rule(6))
        assertEquals(RuleStatus.FAIL, BtmmEngine.evaluate(openLong().copy(spreadPoints = 30.1)).rule(6))
        assertEquals(RuleStatus.FAIL, BtmmEngine.evaluate(openLong().copy(newsLocked = true)).rule(6))
        assertEquals(RuleStatus.MISSING, BtmmEngine.evaluate(openLong().copy(newsLocked = null)).rule(6))
        assertEquals(RuleStatus.MISSING, BtmmEngine.evaluate(openLong().copy(spreadPoints = null)).rule(6))
    }

    @Test
    fun rule6_unknownNewsLock_isNeverTreatedAsNotLocked() {
        val a = BtmmEngine.evaluate(openLong().copy(newsLocked = null))
        assertEquals(Verdict.INCOMPLETE, a.verdict)
        assertNull(a.reference)
    }

    @Test
    fun rule7_onlyLossesCount() {
        assertEquals(RuleStatus.PASS, BtmmEngine.evaluate(openLong().copy(dailyPl = -299.0)).rule(7))
        assertEquals(RuleStatus.FAIL, BtmmEngine.evaluate(openLong().copy(dailyPl = -300.0)).rule(7)) // limit is strictly below 3%
        assertEquals(RuleStatus.FAIL, BtmmEngine.evaluate(openLong().copy(dailyPl = -400.0)).rule(7))
        assertEquals(RuleStatus.PASS, BtmmEngine.evaluate(openLong().copy(dailyPl = 900.0)).rule(7)) // a 9% PROFIT must pass
    }

    @Test
    fun rule9_flatOrWrongMomentum_fails() {
        assertEquals(RuleStatus.FAIL, BtmmEngine.evaluate(openLong().copy(mfiPrev = 55.0)).rule(9))
        assertEquals(RuleStatus.FAIL, BtmmEngine.evaluate(openLong().copy(mfiPrev = 60.0)).rule(9))
        assertEquals(RuleStatus.FAIL, BtmmEngine.evaluate(openShort().copy(mfiPrev = 40.0)).rule(9))
    }

    // ---- score and entries ----

    @Test
    fun score_isSeventyEightyOrNinety_whenBiasExists() {
        assertEquals(90, BtmmEngine.evaluate(openLong()).score)
        assertEquals(80, BtmmEngine.evaluate(openLong().copy(rsi = 65.0)).score)
        assertEquals(70, BtmmEngine.evaluate(openLong().copy(rsi = 65.0, mfi = 65.0)).score)
    }

    @Test
    fun entriesFollowDocumentedTiers() {
        assertEquals(3, BtmmEngine.entriesForScore(100)); assertEquals(3, BtmmEngine.entriesForScore(86))
        assertEquals(2, BtmmEngine.entriesForScore(85)); assertEquals(2, BtmmEngine.entriesForScore(76))
        assertEquals(1, BtmmEngine.entriesForScore(75)); assertEquals(1, BtmmEngine.entriesForScore(65))
        assertEquals(0, BtmmEngine.entriesForScore(64)); assertEquals(0, BtmmEngine.entriesForScore(0))
    }

    @Test
    fun scoreIsUnknown_whenRsiOrMfiMissing() {
        assertNull(BtmmEngine.evaluate(openLong().copy(rsi = null)).score)
        assertNull(BtmmEngine.evaluate(openLong().copy(mfi = null)).score)
    }

    // ---- reference levels ----

    @Test
    fun referenceLevels_long() {
        val ref = BtmmEngine.evaluate(openLong()).reference!!
        assertEquals(Bias.LONG, ref.direction)
        assertEquals(1.0850, ref.entry, 1e-9)
        assertEquals(1.0850 - 0.0015, ref.stopLoss, 1e-9)
        assertEquals(1.0850 + 0.00225, ref.takeProfit, 1e-9)
        assertEquals(100.0, ref.riskAmount, 1e-9)
    }

    @Test
    fun referenceLevels_short() {
        val ref = BtmmEngine.evaluate(openShort()).reference!!
        assertEquals(Bias.SHORT, ref.direction)
        assertEquals(1.0650 + 0.0015, ref.stopLoss, 1e-9)
        assertEquals(1.0650 - 0.00225, ref.takeProfit, 1e-9)
    }

    @Test
    fun noReferenceLevels_withoutAtr_orWhenGateNotOpen() {
        assertEquals(Verdict.GATE_OPEN, BtmmEngine.evaluate(openLong().copy(atr = null)).verdict)
        assertNull(BtmmEngine.evaluate(openLong().copy(atr = null)).reference)
        assertNull(BtmmEngine.evaluate(openLong().copy(rsi = 85.0)).reference)
    }
}
