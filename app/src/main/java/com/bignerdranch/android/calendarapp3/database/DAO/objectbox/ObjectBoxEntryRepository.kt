package com.bignerdranch.android.calendarapp3.database.DAO.objectbox

import com.bignerdranch.android.calendarapp3.database.objectbox.domain.model.EntryOb
import com.bignerdranch.android.calendarapp3.database.objectbox.domain.model.EntryOb_
import io.objectbox.BoxStore

class ObjectBoxEntryRepository (store: BoxStore) {

    private val entryBox = store.boxFor(EntryOb::class.java)

    fun insert_IntoEntryTable(entry: EntryOb): Long {
        entryBox.put(entry)
        return entry.id
    }

    fun get_AllEntries(): List<EntryOb> =
        entryBox.all

    fun getEntriesByDate(date: String): List<EntryOb> =
        entryBox.query(EntryOb_.dateOb.equal(date))
            .build()
            .find()
            .sortedBy { it.timeMinutesOb ?: Int.MAX_VALUE }

    fun update_Entry(entry: EntryOb) {
        entryBox.put(entry)
    }

    fun delete_Entry(entry: EntryOb) {
        entryBox.remove(entry.id)
    }

    fun updateTime(entryId: Long, timeMinutes: Int) {
        val e = entryBox.get(entryId) ?: return
        e.timeMinutesOb = timeMinutes
        entryBox.put(e)
    }

    fun getById(entryId: Long): EntryOb? =
        entryBox.get(entryId)

}


