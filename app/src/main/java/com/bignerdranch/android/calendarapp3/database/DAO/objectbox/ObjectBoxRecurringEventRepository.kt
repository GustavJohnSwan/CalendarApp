package com.bignerdranch.android.calendarapp3.database.DAO.objectbox

import com.bignerdranch.android.calendarapp3.database.objectbox.domain.model.EntryOb
import com.bignerdranch.android.calendarapp3.database.objectbox.domain.model.RecurringEventOb
import com.bignerdranch.android.calendarapp3.database.objectbox.domain.model.RecurringEventOb_
import io.objectbox.BoxStore

class ObjectBoxRecurringEventRepository(store: BoxStore) {

    private val recurBox = store.boxFor(RecurringEventOb::class.java)
    private val entryBox = store.boxFor(EntryOb::class.java)

    fun insert(recurringEvent: RecurringEventOb, entryId: Long) {
        val entry = entryBox.get(entryId) ?: error("No EntryOb with id=$entryId")
        recurringEvent.entryOb.target = entry
        recurBox.put(recurringEvent)
    }

    fun getRecurringEventsByEntryId(entryId: Long): List<RecurringEventOb> =
        recurBox.query(RecurringEventOb_.entryObId.equal(entryId))
            .build()
            .find()

    fun deleteByEntryId(entryId: Long) {
        val list = getRecurringEventsByEntryId(entryId)
        recurBox.remove(list)
    }

    fun getEventsByDate(date: String): List<RecurringEventOb> =
        recurBox.query(RecurringEventOb_.occurrenceDateOb.equal(date))
            .build()
            .find()
}