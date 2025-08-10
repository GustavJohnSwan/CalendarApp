package com.bignerdranch.android.calendarapp3


import androidx.room.*

// these are Data Access Objects (DAOs), they define methods that interact with the database
@Dao
interface EntryDao {
    @Insert
    suspend fun insert_IntoEntryTable(entryTable: EntryTable)

    @Query("SELECT * FROM EntryTable")
    suspend fun get_AllEntries(): List<EntryTable>

    // Add this new query to get entries by specific date
    @Query("SELECT * FROM EntryTable WHERE date = :date")
    suspend fun getEntriesByDate(date: String): List<EntryTable>

    @Update
    suspend fun update_Entry(entryTable: EntryTable)

    @Delete
    suspend fun delete_Entry(entryTable: EntryTable)

    @Insert
    suspend fun insertExtraData(extraDataTable: ExtraDataTable): Long

    @Update
    suspend fun updateEntryWithExtraId(entryTable: EntryTable)
}


@Dao
interface ExtraDataDao {
    @Insert
    suspend fun insertExtraData(extraDataTable: ExtraDataTable): Long  // Returns the generated ID

    @Query("SELECT * FROM ExtraDataTable")
    suspend fun get_AllExData(): List<ExtraDataTable>

    @Update
    suspend fun update_ExData(extraDataTable: ExtraDataTable)

    @Delete
    suspend fun delete_ExData(extraDataTable: ExtraDataTable)
}