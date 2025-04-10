package com.bignerdranch.android.calendarapp3

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

// this defines the tables of the database

// main table
@Entity
data class EntryTable(
    @PrimaryKey(autoGenerate = true) val id: Int,
    @ColumnInfo(name = "date") val dateDB: String?,
    @ColumnInfo(name = "entry") val entryDB: String?,
    @ColumnInfo(name = "id_ex") val idEx: String?
)

// this table is not currently used by the app
@Entity
data class ExtraDataTable(
    @PrimaryKey(autoGenerate = true) val idExtra: Int,
    @ColumnInfo(name = "reminder") val reminderDB: String?,
    @ColumnInfo(name = "repeat") val repeatDB: String?,
    @ColumnInfo(name = "attachment") val attachmentDB: String?
)
