package com.btmm.mobile

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.btmm.mobile.btmm.storage.BtmmDatabase
import com.btmm.mobile.btmm.storage.ReadingRepository

/** An in-memory database plus ViewModels over it. A second ViewModel on the same env simulates reopening the app. */
class TestEnv {
    val db: BtmmDatabase = Room.inMemoryDatabaseBuilder(
        ApplicationProvider.getApplicationContext(),
        BtmmDatabase::class.java
    ).build()
    val repository = ReadingRepository(db.readingDao())

    fun newViewModel() = ReadingsViewModel(repository)

    fun close() = db.close()
}
