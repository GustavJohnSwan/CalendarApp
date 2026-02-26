package com.bignerdranch.android.calendarapp3.database.objectbox.domain.model


import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import io.objectbox.relation.ToMany
import io.objectbox.relation.ToOne

@Entity
class EntryOb(
    var dateOb: String? = null,
    var entryOb: String? = null,
    var timeMinutesOb: Int? = null
) {
    @Id
    var id: Long = 0

    // relations

    lateinit var extraDataOb: ToOne<ExtraDataOb>
    lateinit var attachmentsOb: ToMany<EntryAttachmentOb>
    lateinit var recurringEventsOb: ToMany<RecurringEventOb>


}