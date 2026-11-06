package com.btmm.mobile

import com.btmm.mobile.btmm.ManualReading
import com.btmm.mobile.btmm.TdiCross
import com.btmm.mobile.btmm.Timeframe
import com.btmm.mobile.btmm.storage.ReadingDao
import com.btmm.mobile.btmm.storage.ReadingEntity
import com.btmm.mobile.btmm.storage.ReadingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** In-memory stand-in for the Room DAO, so repository logic is tested on a plain JVM. */
private class FakeReadingDao : ReadingDao {
    private val rows = MutableStateFlow<List<ReadingEntity>>(emptyList())
    private var nextId = 1L

    override suspend fun insert(entity: ReadingEntity): Long {
        val saved = entity.copy(id = nextId++)
        rows.value = rows.value + saved
        return saved.id
    }

    override suspend fun update(entity: ReadingEntity): Int {
        if (rows.value.none { it.id == entity.id }) return 0
        rows.value = rows.value.map { if (it.id == entity.id) entity else it }
        return 1
    }

    override suspend fun delete(id: Long): Int {
        val before = rows.value.size
        rows.value = rows.value.filterNot { it.id == id }
        return before - rows.value.size
    }

    override suspend fun get(id: Long): ReadingEntity? = rows.value.firstOrNull { it.id == id }

    override fun observeAll(): Flow<List<ReadingEntity>> = rows.map { list ->
        list.sortedWith(compareByDescending<ReadingEntity> { it.createdAt }.thenByDescending { it.id })
    }
}

class ReadingRepositoryTest {

    private val full = ManualReading(
        symbol = "EURUSD", timeframe = Timeframe.M15,
        price = 1.0850, ema8 = 1.0848, ema8Prev = 1.0846, ema50 = 1.0830, ema200 = 1.0800, ema1200 = 1.0700,
        rsi = 55.0, tdiCross = TdiCross.UP, mfi = 55.0, mfiPrev = 50.0,
        spreadPoints = 20.0, newsLocked = false, dailyPl = -100.0, account = 10000.0, atr = 0.0010
    )

    @Test
    fun add_thenRead_returnsTheSameInputs() = runBlocking {
        val repo = ReadingRepository(FakeReadingDao())
        val id = repo.add(full, now = 1000L)
        val saved = repo.readings.first().single()
        assertEquals(id, saved.id)
        assertEquals(1000L, saved.createdAt)
        assertEquals(full, saved.reading)
    }

    @Test
    fun partialReading_keepsNullsAsNull_notZero() = runBlocking {
        val repo = ReadingRepository(FakeReadingDao())
        repo.add(ManualReading(symbol = "GBPUSD", price = 1.25), now = 1L)
        val r = repo.readings.first().single().reading
        assertEquals(1.25, r.price!!, 0.0)
        assertNull(r.ema50); assertNull(r.tdiCross); assertNull(r.newsLocked); assertNull(r.account)
    }

    @Test
    fun edit_changesInputs_butKeepsIdAndOriginalTime() = runBlocking {
        val repo = ReadingRepository(FakeReadingDao())
        val id = repo.add(full, now = 1000L)
        assertTrue(repo.edit(id, full.copy(rsi = 85.0, newsLocked = true)))
        val saved = repo.readings.first().single()
        assertEquals(id, saved.id)
        assertEquals(1000L, saved.createdAt)
        assertEquals(85.0, saved.reading.rsi!!, 0.0)
        assertEquals(true, saved.reading.newsLocked)
    }

    @Test
    fun edit_ofMissingReading_returnsFalse_andAddsNothing() = runBlocking {
        val repo = ReadingRepository(FakeReadingDao())
        assertFalse(repo.edit(99L, full))
        assertTrue(repo.readings.first().isEmpty())
    }

    @Test
    fun remove_deletesOnlyThatReading() = runBlocking {
        val repo = ReadingRepository(FakeReadingDao())
        val a = repo.add(full.copy(symbol = "A"), now = 1L)
        val b = repo.add(full.copy(symbol = "B"), now = 2L)
        assertTrue(repo.remove(a))
        assertEquals(listOf(b), repo.readings.first().map { it.id })
    }

    @Test
    fun remove_twice_secondReturnsFalse() = runBlocking {
        val repo = ReadingRepository(FakeReadingDao())
        val id = repo.add(full, now = 1L)
        assertTrue(repo.remove(id))
        assertFalse(repo.remove(id))
    }

    @Test
    fun readings_areNewestFirst() = runBlocking {
        val repo = ReadingRepository(FakeReadingDao())
        repo.add(full.copy(symbol = "OLD"), now = 1L)
        repo.add(full.copy(symbol = "NEW"), now = 2L)
        assertEquals(listOf("NEW", "OLD"), repo.readings.first().map { it.reading.symbol })
    }
}
