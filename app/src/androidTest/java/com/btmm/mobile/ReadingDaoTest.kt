package com.btmm.mobile

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.btmm.mobile.btmm.storage.ReadingEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/** Runs on a device or emulator against real Room. */
@RunWith(AndroidJUnit4::class)
class ReadingDaoTest {
    private lateinit var env: TestEnv

    @Before fun setUp() { env = TestEnv() }
    @After fun tearDown() { env.close() }

    private fun entity(symbol: String, at: Long) = ReadingEntity(
        createdAt = at, symbol = symbol, timeframe = "H1",
        price = 1.085, ema8 = null, ema8Prev = null, ema50 = null, ema200 = null, ema1200 = null,
        rsi = 55.0, tdiCross = "UP", mfi = null, mfiPrev = null, spreadPoints = null,
        newsLocked = false, dailyPl = null, account = 10000.0, atr = null
    )

    @Test
    fun insert_thenRead_roundTripsValuesAndNulls() = runBlocking {
        val dao = env.db.readingDao()
        val id = dao.insert(entity("EURUSD", 1L))
        val loaded = dao.get(id)!!
        assertEquals("EURUSD", loaded.symbol)
        assertEquals(1.085, loaded.price!!, 0.0)
        assertEquals("UP", loaded.tdiCross)
        assertEquals(false, loaded.newsLocked)
        assertNull(loaded.ema50)
        assertNull(loaded.dailyPl)
    }

    @Test
    fun update_changesRow_keepsId() = runBlocking {
        val dao = env.db.readingDao()
        val id = dao.insert(entity("EURUSD", 1L))
        assertEquals(1, dao.update(dao.get(id)!!.copy(rsi = 85.0)))
        assertEquals(85.0, dao.get(id)!!.rsi!!, 0.0)
        assertEquals(1, dao.observeAll().first().size)
    }

    @Test
    fun delete_removesRow_andReportsCount() = runBlocking {
        val dao = env.db.readingDao()
        val id = dao.insert(entity("EURUSD", 1L))
        assertEquals(1, dao.delete(id))
        assertNull(dao.get(id))
        assertEquals(0, dao.delete(id))
    }

    @Test
    fun observeAll_isNewestFirst() = runBlocking {
        val dao = env.db.readingDao()
        dao.insert(entity("OLD", 1L))
        dao.insert(entity("NEW", 2L))
        assertEquals(listOf("NEW", "OLD"), dao.observeAll().first().map { it.symbol })
    }
}
