package com.btmm.mobile.btmm.storage

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class BtmmRepositoryTest {
    private lateinit var db: BtmmDatabase
    private lateinit var repository: BtmmRepository

    @Before
    fun setup() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            BtmmDatabase::class.java
        ).build()
        repository = BtmmRepository(db.snapshotDao(), db.tradeDao(), db.eventDao())
    }

    @After
    fun teardown() {
        db.close()
    }

    @Test
    fun saveAndLoadSnapshot() = runBlocking {
        val snapshot = BtmmSnapshotEntity(
            symbol = "EURUSD",
            timeframe = "H1",
            price = 1.0520,
            bias = "LONG",
            biasScore = 82,
            state = "STATIC",
            gateOpen = true,
            rulesPassed = 8,
            tier = "T5",
            riskPct = 1.0,
            ghostEntry = 1.0520,
            ghostSl = 1.0500,
            ghostTp = 1.0560,
            ghostLots = 0.10,
            ghostRiskAmt = 100.0,
            createdAt = System.currentTimeMillis()
        )
        val id = repository.saveSnapshot(snapshot)
        val loaded = repository.getAllSnapshots().first()
        assertEquals(1, loaded.size)
        assertEquals("EURUSD", loaded[0].symbol)
        assertEquals(82, loaded[0].biasScore)
        assertEquals("STATIC", loaded[0].state)
    }

    @Test
    fun saveAndLoadTrade() = runBlocking {
        val trade = BtmmTradeEntity(
            symbol = "EURUSD",
            dir = "LONG",
            entry = 1.0520,
            exit = null,
            lots = 0.10,
            pnl = null,
            state = "OPEN",
            openedAt = System.currentTimeMillis(),
            closedAt = null
        )
        val id = repository.saveTrade(trade)
        val loaded = repository.getAllTrades().first()
        assertEquals(1, loaded.size)
        assertEquals("EURUSD", loaded[0].symbol)
        assertEquals(0.10, loaded[0].lots, 0.001)
        assertEquals("OPEN", loaded[0].state)
    }

    @Test
    fun closeTrade_updatesFields() = runBlocking {
        val trade = BtmmTradeEntity(
            symbol = "EURUSD",
            dir = "LONG",
            entry = 1.0520,
            exit = null,
            lots = 0.10,
            pnl = null,
            state = "OPEN",
            openedAt = System.currentTimeMillis(),
            closedAt = null
        )
        val id = repository.saveTrade(trade)
        repository.closeTrade(id, 1.0560, 50.0, System.currentTimeMillis())
        val loaded = repository.getAllTrades().first()
        assertEquals(1.0560, loaded[0].exit!!, 0.0001)
        assertEquals(50.0, loaded[0].pnl!!, 0.01)
        assertEquals("CLOSED", loaded[0].state)
    }

    @Test
    fun clearTrades_removesAll() = runBlocking {
        repository.saveTrade(
            BtmmTradeEntity(
                symbol = "EURUSD",
                dir = "LONG",
                entry = 1.0520,
                lots = 0.10,
                state = "OPEN",
                openedAt = System.currentTimeMillis()
            )
        )
        repository.saveTrade(
            BtmmTradeEntity(
                symbol = "GBPUSD",
                dir = "SHORT",
                entry = 1.2650,
                lots = 0.20,
                state = "OPEN",
                openedAt = System.currentTimeMillis()
            )
        )
        repository.clearTrades()
        val loaded = repository.getAllTrades().first()
        assertEquals(0, loaded.size)
    }
}
