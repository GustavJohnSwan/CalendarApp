package com.bignerdranch.android.calendarapp3


import androidx.room.*

// these are Data Access Objects (DAOs), they define methods that interact with the database
@Dao
interface EntryDao {
    @Insert
    suspend fun insert_IntoEntryTable(entryTable: EntryTable)

    @Query("SELECT * FROM EntryTable")
    suspend fun get_AllEntries(): List<EntryTable>

    @Update
    suspend fun update_Entry(entryTable: EntryTable)

    @Delete
    suspend fun delete_Entry(entryTable: EntryTable)
}


@Dao
interface ExtraDataDao {
    @Insert
    suspend fun insert_IntoExDaTable(extraDataTable: ExtraDataTable)

    @Query("SELECT * FROM ExtraDataTable")
    suspend fun get_AllExData(): List<ExtraDataTable>

    @Update
    suspend fun update_ExData(extraDataTable: ExtraDataTable)

    @Delete
    suspend fun delete_ExData(extraDataTable: ExtraDataTable)
}