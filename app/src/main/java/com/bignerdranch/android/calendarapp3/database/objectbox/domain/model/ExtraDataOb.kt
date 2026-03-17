package com.bignerdranch.android.calendarapp3.database.objectbox.domain.model

import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import io.objectbox.relation.ToOne

//*
@Entity
class ExtraDataOb(
    var reminderTypeOb: String? = null,
    var repeatOb: String? = null,
    var repeatDetailsOb: String? = null,
    var attachmentIdOb: Long? = null
) {
    @Id
    var id: Long = 0

    lateinit var entryOb: ToOne<EntryOb>
}