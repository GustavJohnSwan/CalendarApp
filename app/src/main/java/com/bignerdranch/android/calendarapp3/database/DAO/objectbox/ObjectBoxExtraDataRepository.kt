package com.bignerdranch.android.calendarapp3.database.DAO.objectbox

import com.bignerdranch.android.calendarapp3.database.objectbox.domain.model.EntryOb
import com.bignerdranch.android.calendarapp3.database.objectbox.domain.model.ExtraDataOb
import com.bignerdranch.android.calendarapp3.database.objectbox.domain.model.ExtraDataOb_
import io.objectbox.BoxStore

class ObjectBoxExtraDataRepository(store: BoxStore) {

    private val extraBox = store.boxFor(ExtraDataOb::class.java)
    private val entryBox = store.boxFor(EntryOb::class.java)

    fun insertExtraData(extraData: ExtraDataOb, entryId: Long): Long {
        val entry = entryBox.get(entryId) ?: error("No EntryOb with id=$entryId")
        extraData.entryOb.target = entry
        extraBox.put(extraData)
        return extraData.id
    }

    fun get_AllExData(): List<ExtraDataOb> =
        extraBox.all

    fun getExtraDataByEntryId(entryId: Long): ExtraDataOb? =
        extraBox.query(ExtraDataOb_.entryObId.equal(entryId))
            .build()
            .findFirst()

    fun update_ExData(extraData: ExtraDataOb) {
        extraBox.put(extraData)
    }

    fun delete_ExData(extraData: ExtraDataOb) {
        extraBox.remove(extraData.id)
    }
}