package com.bignerdranch.android.calendarapp3.database

import androidx.room.*

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