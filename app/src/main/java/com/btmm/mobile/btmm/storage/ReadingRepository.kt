package com.btmm.mobile.btmm.storage

import com.btmm.mobile.btmm.ManualReading
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ReadingRepository(private val dao: ReadingDao) {

    val readings: Flow<List<SavedReading>> = dao.observeAll().map { list -> list.map { it.toSaved() } }

    /** Inserts a new reading and returns its id. */
    suspend fun add(reading: ManualReading, now: Long = System.currentTimeMillis()): Long =
        dao.insert(reading.toEntity(createdAt = now))

    /** Replaces the inputs of an existing reading, keeping its id and original time. False if it no longer exists. */
    suspend fun edit(id: Long, reading: ManualReading): Boolean {
        val existing = dao.get(id) ?: return false
        return dao.update(reading.toEntity(id = id, createdAt = existing.createdAt)) > 0
    }

    /** False if there was nothing to delete. */
    suspend fun remove(id: Long): Boolean = dao.delete(id) > 0
}
