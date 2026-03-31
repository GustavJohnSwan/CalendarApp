package com.bignerdranch.android.calendarapp3.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

import androidx.room.Index

// this defines the tables of the database
@Entity
data class EntryTable(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "date") val dateDB: String?,
    @ColumnInfo(name = "entry") val entryDB: String?,
    @ColumnInfo(name = "time_minutes") val timeMinutes: Int? = null // Store as minutes
)

// EXTRA DATA TABLE (child - contains the foreign key)
@Entity(
    foreignKeys = [ForeignKey(
        entity = EntryTable::class,          // References the PARENT table
        parentColumns = ["id"],             // PK in EntryTable
        childColumns = ["entry_id"],        // FK in THIS table
        onDelete = ForeignKey.CASCADE       // Delete extra data if entry is deleted
    )],
    indices = [Index(value = ["entry_id"], unique = true)]
)

data class ExtraDataTable(
    @PrimaryKey(autoGenerate = true) val idExtra: Int = 0,
    @ColumnInfo(name = "entry_id") val entryId: Int,  // This is the FK to EntryTable
    @ColumnInfo(name = "reminder_type") val reminderType: String? = null,
    @ColumnInfo(name = "repeat") val repeat: String? = null,
    @ColumnInfo(name = "repeat_details") val repeatDetails: String? = null
)



@Entity(
    tableName = "attachments",
    foreignKeys = [ForeignKey(
        entity = EntryTable::class,
        parentColumns = ["id"],
        childColumns = ["entry_id"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class EntryAttachment(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "entry_id") val entryId: Int,
    @ColumnInfo(name = "file_name") val fileName: String,
    @ColumnInfo(name = "mime_type") val mimeType: String,
    @ColumnInfo(name = "file_size") val fileSize: Long,
    @ColumnInfo(name = "uri_path") val uriPath: String,
    @ColumnInfo(name = "date_added") val dateAdded: Long = System.currentTimeMillis()
)
