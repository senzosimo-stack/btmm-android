package com.btmm.mobile.btmm.storage

class BtmmRepository(private val snapshotDao: BtmmSnapshotDao, private val tradeDao: BtmmTradeDao, private val eventDao: BtmmEventDao) {
    suspend fun saveSnapshot(snapshot: BtmmSnapshotEntity): Long = snapshotDao.insert(snapshot)
    suspend fun getAllSnapshots() = snapshotDao.getAll()
    suspend fun clearSnapshots() = snapshotDao.clear()

    suspend fun saveTrade(trade: BtmmTradeEntity): Long = tradeDao.insert(trade)
    suspend fun getAllTrades() = tradeDao.getAll()
    suspend fun closeTrade(id: Long, exit: Double, pnl: Double, closedAt: Long) = tradeDao.closeTrade(id, exit, pnl, closedAt)
    suspend fun deleteTrade(id: Long) = tradeDao.delete(id)
    suspend fun clearTrades() = tradeDao.clear()

    suspend fun saveEvent(event: BtmmEventEntity): Long = eventDao.insert(event)
    suspend fun getAllEvents() = eventDao.getAll()
    suspend fun clearEvents() = eventDao.clear()
}
