package com.btmm.mobile

import com.btmm.mobile.btmm.*
import org.junit.Assert.assertEquals
import org.junit.Test

class BtmmEngineTest {

    @Test
    fun engine_returnsEmptySnap_whenCandlesTooFew() {
        val world = World(
            symbol = "EURUSD",
            tf = Timeframe.H1,
            candles = emptyList(),
            markets = emptyMap(),
            positions = emptyList(),
            closed = emptyList(),
            dailyPL = 0.0,
            equity = emptyList(),
            seedTrades = emptyList(),
            barStartedAt = 0L,
            simTime = 0L,
            paused = false,
            compact = false,
            helpOpen = false,
            density = Density.FULL,
            lastScanAt = 0L,
            scanner = emptyList(),
            stateLog = emptyList(),
            events = emptyList(),
            eventSeq = 0,
            visited = emptyList(),
            account = 10000.0,
            mode = Mode.SIM,
            liveStatus = LiveStatus.DISCONNECTED,
            nextTicket = 1
        )
        val pair = PairInfo("EURUSD", "Euro/US Dollar", 5, 0.0001, 1.0, 12345L)

        val snap = BtmmEngine.evaluate(world, pair)

        assertEquals(0.0, snap.price, 0.0)
        assertEquals(Bias.NONE, snap.bias)
        assertEquals(0, snap.biasScore)
        assertEquals(BtmmState.NONE, snap.state)
        assertEquals(false, snap.gateOpen)
        assertEquals(0, snap.rulesPassed)
        assertEquals(null, snap.ghost)
    }

    @Test
    fun engine_evaluatesLongBias_whenPriceAboveEMAs() {
        val candles = mutableListOf<Candle>()
        var time = 1000L
        var price = 1.0500
        repeat(200) {
            candles.add(
                Candle(
                    time = time,
                    open = price,
                    high = price + 0.0010,
                    low = price - 0.0010,
                    close = price,
                    volume = 1000L
                )
            )
            time += 3600000
            price += 0.0001
        }

        val world = World(
            symbol = "EURUSD",
            tf = Timeframe.H1,
            candles = candles,
            markets = emptyMap(),
            positions = emptyList(),
            closed = emptyList(),
            dailyPL = -100.0,
            equity = emptyList(),
            seedTrades = emptyList(),
            barStartedAt = 0L,
            simTime = time,
            paused = false,
            compact = false,
            helpOpen = false,
            density = Density.FULL,
            lastScanAt = 0L,
            scanner = emptyList(),
            stateLog = emptyList(),
            events = emptyList(),
            eventSeq = 0,
            visited = emptyList(),
            account = 10000.0,
            mode = Mode.SIM,
            liveStatus = LiveStatus.DISCONNECTED,
            nextTicket = 1
        )
        val pair = PairInfo("EURUSD", "Euro/US Dollar", 5, 0.0001, 1.0, 12345L)

        val snap = BtmmEngine.evaluate(world, pair)

        assertEquals(Bias.LONG, snap.bias)
        assertEquals(true, snap.cond200)
        assertEquals(true, snap.cond50)
    }
}
