package com.btmm.mobile

import com.btmm.mobile.btmm.FormState
import com.btmm.mobile.btmm.ManualReading
import com.btmm.mobile.btmm.TdiCross
import com.btmm.mobile.btmm.Verdict
import com.btmm.mobile.btmm.assess
import com.btmm.mobile.btmm.parse
import com.btmm.mobile.btmm.toForm
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ReadingFormTest {

    private fun fullForm() = FormState(
        symbol = "EURUSD", price = "1.0850", ema8 = "1.0848", ema8Prev = "1.0846", ema50 = "1.0830",
        ema200 = "1.0800", ema1200 = "1.0700", rsi = "55", tdiCross = TdiCross.UP, mfi = "55", mfiPrev = "50",
        spreadPoints = "20", newsLocked = false, dailyPl = "-100", account = "10000", atr = "0.0010"
    )

    @Test
    fun blankForm_isNoData() {
        assertEquals(Verdict.NO_DATA, FormState().assess().verdict)
    }

    @Test
    fun blankFieldsParseToNull_notZero() {
        assertNull(FormState().parse().reading.price)
    }

    @Test
    fun fullForm_opensGate() {
        assertEquals(Verdict.GATE_OPEN, fullForm().assess().verdict)
    }

    @Test
    fun commaDecimals_areAccepted() {
        assertEquals(1.085, FormState(price = "1,0850").parse().reading.price!!, 1e-9)
    }

    @Test
    fun nonNumericText_isInvalid_andNamesTheField() {
        val a = fullForm().copy(rsi = "abc").assess()
        assertEquals(Verdict.INVALID, a.verdict)
        assertTrue(a.errors.any { it.startsWith("RSI") })
        assertTrue(a.rules.isEmpty())
    }

    @Test
    fun nanAndInfinityText_areInvalid() {
        assertEquals(Verdict.INVALID, fullForm().copy(price = "NaN").assess().verdict)
        assertEquals(Verdict.INVALID, fullForm().copy(ema50 = "Infinity").assess().verdict)
    }

    @Test
    fun roundTrip_readingToFormAndBack() {
        val reading: ManualReading = fullForm().parse().reading
        assertEquals(reading, reading.toForm().parse().reading)
    }

    @Test
    fun roundTrip_smallAndLargeNumbersKeepPlainText() {
        val form = FormState(atr = "0.0001", ema1200 = "1200").parse().reading.toForm()
        assertEquals("0.0001", form.atr)
        assertEquals("1200", form.ema1200)
    }
}
