package com.bignerdranch.android.calendarapp3.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

// this defines the tables of the database

// main table

// MAIN ENTRY TABLE (parent)
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
    )]
)
data class ExtraDataTable(
    @PrimaryKey(autoGenerate = true) val idExtra: Int = 0,
    @ColumnInfo(name = "entry_id") val entryId: Int,  // This is the FK to EntryTable
    @ColumnInfo(name = "reminder_type") val reminderType: String? = null, // CHANGED from "reminder" to "reminder_type"
    @ColumnInfo(name = "repeat") val repeat: String? = null,
    @ColumnInfo(name = "repeat_details") val repeatDetails: String? = null, // ADD THIS NEW COLUMN
    @ColumnInfo(name = "attachment") val attachment: String? = null
) {
    constructor() : this(0, 0, null, null, null, null)
}