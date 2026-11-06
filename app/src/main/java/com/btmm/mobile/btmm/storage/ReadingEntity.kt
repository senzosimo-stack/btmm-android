package com.btmm.mobile.btmm.storage

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.btmm.mobile.btmm.ManualReading
import com.btmm.mobile.btmm.TdiCross
import com.btmm.mobile.btmm.Timeframe

/**
 * Stores only what the user typed. Verdicts are recomputed from these inputs when shown,
 * so a saved result can never disagree with the current rules.
 */
@Entity(tableName = "readings")
data class ReadingEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val createdAt: Long,
    val symbol: String,
    val timeframe: String,
    val price: Double?,
    val ema8: Double?,
    val ema8Prev: Double?,
    val ema50: Double?,
    val ema200: Double?,
    val ema1200: Double?,
    val rsi: Double?,
    val tdiCross: String?,
    val mfi: Double?,
    val mfiPrev: Double?,
    val spreadPoints: Double?,
    val newsLocked: Boolean?,
    val dailyPl: Double?,
    val account: Double?,
    val atr: Double?
)

data class SavedReading(val id: Long, val createdAt: Long, val reading: ManualReading)

fun ReadingEntity.toSaved(): SavedReading = SavedReading(
    id = id,
    createdAt = createdAt,
    reading = ManualReading(
        symbol = symbol,
        timeframe = Timeframe.entries.firstOrNull { it.name == timeframe } ?: Timeframe.H1,
        price = price, ema8 = ema8, ema8Prev = ema8Prev, ema50 = ema50, ema200 = ema200, ema1200 = ema1200,
        rsi = rsi,
        tdiCross = tdiCross?.let { name -> TdiCross.entries.firstOrNull { it.name == name } },
        mfi = mfi, mfiPrev = mfiPrev, spreadPoints = spreadPoints, newsLocked = newsLocked,
        dailyPl = dailyPl, account = account, atr = atr
    )
)

fun ManualReading.toEntity(id: Long = 0, createdAt: Long): ReadingEntity = ReadingEntity(
    id = id,
    createdAt = createdAt,
    symbol = symbol,
    timeframe = timeframe.name,
    price = price, ema8 = ema8, ema8Prev = ema8Prev, ema50 = ema50, ema200 = ema200, ema1200 = ema1200,
    rsi = rsi, tdiCross = tdiCross?.name, mfi = mfi, mfiPrev = mfiPrev, spreadPoints = spreadPoints,
    newsLocked = newsLocked, dailyPl = dailyPl, account = account, atr = atr
)
