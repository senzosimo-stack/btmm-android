package com.btmm.mobile.btmm.storage

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [ReadingEntity::class], version = 1, exportSchema = false)
abstract class BtmmDatabase : RoomDatabase() {
    abstract fun readingDao(): ReadingDao

    companion object {
        @Volatile
        private var instance: BtmmDatabase? = null

        fun get(context: Context): BtmmDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    BtmmDatabase::class.java,
                    "btmm_readings.db"
                ).build().also { instance = it }
            }
    }
}
