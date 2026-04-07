package com.bignerdranch.android.calendarapp3.database.DAO


import androidx.room.*
import com.bignerdranch.android.calendarapp3.database.EntryAttachment
import com.bignerdranch.android.calendarapp3.database.EntryTable
import com.bignerdranch.android.calendarapp3.database.ExtraDataTable


// these are Data Access Objects (DAOs), they define methods that interact with the database
@Dao
interface EntryDao {
    @Insert
    suspend fun insert_IntoEntryTable(entryTable: EntryTable): Long

    @Query("SELECT * FROM EntryTable")
    suspend fun get_AllEntries(): List<EntryTable>


    // get entries by specific date
    @Query("SELECT * FROM EntryTable WHERE date = :date ORDER BY time_minutes ASC")
    suspend fun getEntriesByDate(date: String): List<EntryTable>

    @Update
    suspend fun update_Entry(entryTable: EntryTable)

    @Delete
    suspend fun delete_Entry(entryTable: EntryTable)


    @Update
    suspend fun updateEntryWithExtraId(entryTable: EntryTable)


    // updates time only
    @Query("UPDATE EntryTable SET time_minutes = :timeMinutes WHERE id = :entryId")
    suspend fun updateTime(entryId: Int, timeMinutes: Int)
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

    @Query("SELECT * FROM ExtraDataTable WHERE entry_id = :entryId")
    suspend fun getExtraDataByEntryId(entryId: Int): ExtraDataTable?
}



@Dao
interface AttachmentDao {
    @Insert
    suspend fun insert(attachment: EntryAttachment): Long

    @Query("SELECT * FROM attachments WHERE entry_id = :entryId")
    suspend fun getAttachmentsForEntry(entryId: Int): List<EntryAttachment>

    @Query("SELECT * FROM attachments WHERE id = :attachmentId")
    suspend fun getAttachmentById(attachmentId: Long): EntryAttachment?

    @Delete
    suspend fun delete(attachment: EntryAttachment)
}

