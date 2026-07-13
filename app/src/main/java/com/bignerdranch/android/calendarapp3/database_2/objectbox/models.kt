package com.bignerdranch.android.calendarapp3.database_2.objectbox

import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id

// defining a fundamental Objectbox database for benchmarking
@Entity
data class EntryOb_B(
    @Id
    var id: Long = 0,
    val dateOb: String?,
    val entryOb: String?,
    val timeMinutesOb: Int?
)

@Entity
data class ExtraDataOb_B(
    @Id
    var id: Long = 0,
    val reminderTypeOb: String?,
    val repeatOb: String?,
    val repeatDetailsOb: String?
)

@Entity
data class EntryAttachmentOb_B(
    @Id
    var id: Long = 0,
    val fileNameOb: String,
    val mimeTypeOb: String,
    val fileSizeOb: Long,
    val uriPathOb: String,
    val dataAddedOb: Long
)