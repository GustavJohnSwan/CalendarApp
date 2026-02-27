package com.bignerdranch.android.calendarapp3.database.objectbox.domain.model

import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import io.objectbox.relation.ToOne

@Entity
class RecurringEventOb(
    var occurrenceDateOb: String = "",
    var isExceptionOb: Boolean = false
) {
    @Id
    var id: Long = 0

    lateinit var entryOb: ToOne<EntryOb>
}