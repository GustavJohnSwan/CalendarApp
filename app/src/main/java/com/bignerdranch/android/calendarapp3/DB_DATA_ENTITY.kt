package com.bignerdranch.android.calendarapp3

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.Relation

// this defines the tables of the database

// main table
// FK doesn't work yet
@Entity/*(
    foreignKeys = [ForeignKey(
        entity = EntryTable::class,
        parentColumns = ["idExtra"],
        childColumns = ["idEx"],
        onDelete = ForeignKey.CASCADE
    )]
)
*/
data class EntryTable(
    @PrimaryKey(autoGenerate = true) val id: Int,
    @ColumnInfo(name = "date") val dateDB: String?,
    @ColumnInfo(name = "entry") val entryDB: String?,
    @ColumnInfo(name = "id_ex") val idEx: String? // this has to be changed into an Int

)

// this table is not currently used by the app
@Entity
data class ExtraDataTable(
    @PrimaryKey(autoGenerate = true) val idExtra: Int,
    @ColumnInfo(name = "reminder") val reminderDB: String?,
    @ColumnInfo(name = "repeat") val repeatDB: String?,
    @ColumnInfo(name = "attachment") val attachmentDB: String?
)