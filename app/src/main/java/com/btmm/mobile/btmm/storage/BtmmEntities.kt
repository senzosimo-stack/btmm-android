package com.btmm.mobile.btmm.storage

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "btmm_snapshots")
data class BtmmSnapshotEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val symbol: String,
    val timeframe: String,
    val price: Double,
    val bias: String,
    val biasScore: Int,
    val state: String,
    val gateOpen: Boolean,
    val rulesPassed: Int,
    val tier: String,
    val riskPct: Double,
    val ghostEntry: Double?,
    val ghostSl: Double?,
    val ghostTp: Double?,
    val ghostLots: Double?,
    val ghostRiskAmt: Double?,
    val createdAt: Long
)

@Entity(tableName = "btmm_trades")
data class BtmmTradeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val symbol: String,
    val dir: String,
    val entry: Double,
    val exit: Double?,
    val lots: Double,
    val pnl: Double?,
    val state: String,
    val openedAt: Long,
    val closedAt: Long?
)

@Entity(tableName = "btmm_events")
data class BtmmEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val kind: String,
    val title: String,
    val body: String,
    val createdAt: Long
)
