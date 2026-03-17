package com.bignerdranch.android.calendarapp3.database.objectbox.domain.model


import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import io.objectbox.relation.ToOne

@Entity
class EntryAttachmentOb(
    var fileNameOb: String = "",
    var mimeTypeOb: String = "",
    var fileSizeOb: Long = 0L,
    var uriPathOb: String = "",
    var dateAddedOb: Long = System.currentTimeMillis()
) {
    @Id
    var id: Long = 0

    lateinit var entryOb: ToOne<EntryOb>
}