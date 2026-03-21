package com.bignerdranch.android.calendarapp3.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.bignerdranch.android.calendarapp3.benchmark.room.BenchmarkRoomDao
import com.bignerdranch.android.calendarapp3.database.DAO.AttachmentDao
import com.bignerdranch.android.calendarapp3.database.DAO.EntryDao
import com.bignerdranch.android.calendarapp3.database.DAO.ExtraDataDao
import com.bignerdranch.android.calendarapp3.benchmark.room.BenchmarkRoomEntity
import com.bignerdranch.android.calendarapp3.benchmark.room.BenchmarkRoomIndexedDao
import com.bignerdranch.android.calendarapp3.benchmark.room.BenchmarkRoomIndexedEntity

// this class sets up the database - defines its configuration and acts as the main access point to the data
// version needs to be incremented every time the schema is updated/changed
@Database(entities = [EntryTable::class, ExtraDataTable::class, EntryAttachment::class, BenchmarkRoomEntity::class, BenchmarkRoomIndexedEntity::class], version = 14)
abstract class AppDatabase : RoomDatabase() { // AppDatabase extends RoomDatabase
    // returns DAO interfaces
    abstract fun entryDao(): EntryDao
    abstract fun extraDataDao(): ExtraDataDao
    abstract fun attachmentDao(): AttachmentDao

    // for testing basic CRUD for Room
    abstract fun benchmarkRoomDao(): BenchmarkRoomDao

    abstract fun benchmarkRoomIndexedDao(): BenchmarkRoomIndexedDao


    // this creates a shared, static variable called INSTANCE. It holds the only copy of the database. It is a SINGLETON that allows only one INSTANCE to exist in the whole app
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // this returns an existing database instance or it creates a new one
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) { // this makes sure that only one thread initializes the database
                val instance = Room.databaseBuilder(
                    context.applicationContext, // this prevents memory leaks by not tying the database to an Activity context
                    AppDatabase::class.java,
                    "app_database"
                )
                    .fallbackToDestructiveMigration() // this makes ROOM detect if there has been a version change. It wipes and rebuild the DB (deletes data)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
