package com.btmm.mobile.btmm.storage

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ReadingDao {
    @Insert
    suspend fun insert(entity: ReadingEntity): Long

    @Update
    suspend fun update(entity: ReadingEntity): Int

    @Query("DELETE FROM readings WHERE id = :id")
    suspend fun delete(id: Long): Int

    @Query("SELECT * FROM readings WHERE id = :id")
    suspend fun get(id: Long): ReadingEntity?

    @Query("SELECT * FROM readings ORDER BY createdAt DESC, id DESC")
    fun observeAll(): Flow<List<ReadingEntity>>
}
