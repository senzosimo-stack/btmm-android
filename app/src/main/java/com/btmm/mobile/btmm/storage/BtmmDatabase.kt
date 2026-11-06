package com.btmm.mobile.btmm.storage

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        BtmmSnapshotEntity::class,
        BtmmTradeEntity::class,
        BtmmEventEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class BtmmDatabase : RoomDatabase() {
    abstract fun snapshotDao(): BtmmSnapshotDao
    abstract fun tradeDao(): BtmmTradeDao
    abstract fun eventDao(): BtmmEventDao

    companion object {
        @Volatile
        private var INSTANCE: BtmmDatabase? = null

        fun getDatabase(context: Context): BtmmDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BtmmDatabase::class.java,
                    "btmm_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
