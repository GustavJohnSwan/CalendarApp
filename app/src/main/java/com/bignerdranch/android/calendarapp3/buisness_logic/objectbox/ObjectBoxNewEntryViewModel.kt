package com.bignerdranch.android.calendarapp3.buisness_logic.objectbox

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.bignerdranch.android.calendarapp3.database.DAO.objectbox.ObjectBoxEntryRepository
import com.bignerdranch.android.calendarapp3.database.DAO.objectbox.ObjectBoxExtraDataRepository
import com.bignerdranch.android.calendarapp3.database.DAO.objectbox.ObjectBoxRecurringEventRepository
import com.bignerdranch.android.calendarapp3.database.objectbox.ObjectBoxProvider
import com.bignerdranch.android.calendarapp3.database.objectbox.domain.model.EntryOb
import com.bignerdranch.android.calendarapp3.database.objectbox.domain.model.ExtraDataOb
import com.bignerdranch.android.calendarapp3.database.objectbox.domain.model.RecurringEventOb
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class ObjectBoxNewEntryViewModel(application: Application) : AndroidViewModel(application) {

    private val store = ObjectBoxProvider.get()

    private val entryRepo = ObjectBoxEntryRepository(store)
    private val extraRepo = ObjectBoxExtraDataRepository(store)
    private val recurringRepo = ObjectBoxRecurringEventRepository(store)

    /**
     * Mirror of Room version:
     * - Insert Entry
     * - Optionally insert ExtraData linked to Entry
     * - Return entryId (as Int for UI compatibility)
     */
    suspend fun insertEntryWithExtraData(
        dateDB: String,
        entryDB: String,
        needsExtraData: Boolean,
        timeMinutes: Int? = null,
        reminderType: String? = null,
        repeat: String? = null,
        repeatDetails: String? = null
    ): Int = withContext(Dispatchers.IO) {

        val newEntry = EntryOb(
            dateOb = dateDB,
            entryOb = entryDB,
            timeMinutesOb = timeMinutes
        )

        val entryIdLong = entryRepo.insert_IntoEntryTable(newEntry)

        if (needsExtraData) {
            val extra = ExtraDataOb(
                reminderTypeOb = reminderType,
                repeatOb = repeat,
                repeatDetailsOb = repeatDetails,
                attachmentIdOb = null // same as your Room insert (you’re not passing it here)
            )
            extraRepo.insertExtraData(extra, entryIdLong)
        }

        // your UI expects Int ids like Room; convert safely
        entryIdLong.toInt()
    }

    fun insertEntry(
        date: String,
        content: String,
        exDaBo: Boolean,
        timeMinutes: Int? = null,
        reminderType: String? = null,
        repeat: String?,
        repeatDetails: String? = null,
        onEntryInserted: (Int) -> Unit = {}
    ) {
        viewModelScope.launch {
            val entryId = insertEntryWithExtraData(
                dateDB = date,
                entryDB = content,
                needsExtraData = exDaBo,
                timeMinutes = timeMinutes,
                reminderType = reminderType,
                repeat = repeat,
                repeatDetails = repeatDetails
            )
            onEntryInserted(entryId)
        }
    }

    suspend fun saveRecurringEventToDatabase(entryId: Int, date: String) = withContext(Dispatchers.IO) {
        Log.d("RepeatEvent", "Saving recurring event to ObjectBox - Original ID: $entryId, Date: $date")

        val rec = RecurringEventOb(
            occurrenceDateOb = date,
            isExceptionOb = false
        )

        // ObjectBox ids are Long internally
        recurringRepo.insert(rec, entryId.toLong())
    }
}