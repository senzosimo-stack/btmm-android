package com.btmm.mobile.btmm.storage

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface BtmmSnapshotDao {
    @Insert
    suspend fun insert(entity: BtmmSnapshotEntity): Long

    @Query("SELECT * FROM btmm_snapshots ORDER BY createdAt DESC")
    suspend fun getAll(): List<BtmmSnapshotEntity>

    @Query("DELETE FROM btmm_snapshots")
    suspend fun clear()
}

@Dao
interface BtmmTradeDao {
    @Insert
    suspend fun insert(entity: BtmmTradeEntity): Long

    @Query("SELECT * FROM btmm_trades ORDER BY openedAt DESC")
    suspend fun getAll(): List<BtmmTradeEntity>

    @Query("UPDATE btmm_trades SET exit = :exit, pnl = :pnl, closedAt = :closedAt WHERE id = :id")
    suspend fun closeTrade(id: Long, exit: Double, pnl: Double, closedAt: Long)

    @Query("DELETE FROM btmm_trades WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("DELETE FROM btmm_trades")
    suspend fun clear()
}

@Dao
interface BtmmEventDao {
    @Insert
    suspend fun insert(entity: BtmmEventEntity): Long

    @Query("SELECT * FROM btmm_events ORDER BY createdAt DESC")
    suspend fun getAll(): List<BtmmEventEntity>

    @Query("DELETE FROM btmm_events")
    suspend fun clear()
}
