package com.bignerdranch.android.calendarapp3

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.Relation

// this defines the tables of the database

// main table

@Entity(
    foreignKeys = [ForeignKey(
        entity = EntryTable::class,
        parentColumns = ["id"],
        childColumns = ["id_ex"],
        onDelete = ForeignKey.CASCADE
    )]
)

data class EntryTable(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "date") val dateDB: String?,
    @ColumnInfo(name = "entry") val entryDB: String?,
    @ColumnInfo(name = "id_ex") val idEx: Int? // Changed to Int

)

// this table is not currently used by the app
@Entity
data class ExtraDataTable(
    @PrimaryKey(autoGenerate = true) val idExtra: Int = 0,
    @ColumnInfo(name = "reminder") val reminderDB: String? = null,
    @ColumnInfo(name = "repeat") val repeatDB: String? = null,
    @ColumnInfo(name = "attachment") val attachmentDB: String? = null
) {
    // Secondary constructor for empty initialization
    constructor() : this(0, null, null, null)
}