package com.bignerdranch.android.calendarapp3.database.objectbox.domain.model


import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import io.objectbox.relation.ToMany
import io.objectbox.relation.ToOne

@Entity
class EntryAttachmentOb(
    var fileName: String = "",
    var mimeType: String = "",
    var fileSize: Long = 0L,
    var uriPath: String = "",
    var dateAdded: Long = System.currentTimeMillis()
) {
    @Id
    var id: Long = 0

    lateinit var entryOb: ToOne<EntryOb>
}