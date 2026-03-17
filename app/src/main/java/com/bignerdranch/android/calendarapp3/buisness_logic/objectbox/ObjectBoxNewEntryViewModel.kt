package com.bignerdranch.android.calendarapp3.buisness_logic.objectbox

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.bignerdranch.android.calendarapp3.database.DAO.objectbox.ObjectBoxEntryRepository
import com.bignerdranch.android.calendarapp3.database.DAO.objectbox.ObjectBoxExtraDataRepository
import com.bignerdranch.android.calendarapp3.database.objectbox.ObjectBoxProvider
import com.bignerdranch.android.calendarapp3.database.objectbox.domain.model.EntryAttachmentOb_.id
import com.bignerdranch.android.calendarapp3.database.objectbox.domain.model.EntryOb
import com.bignerdranch.android.calendarapp3.database.objectbox.domain.model.ExtraDataOb
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class ObjectBoxNewEntryViewModel(application: Application) : AndroidViewModel(application) {

    private val store = ObjectBoxProvider.get()

    private val entryRepo = ObjectBoxEntryRepository(store)
    private val extraRepo = ObjectBoxExtraDataRepository(store)


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
                attachmentIdOb = null
            )
            extraRepo.insertExtraData(extra, entryIdLong)
        }


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
        Log.d("ObjectBoxTest", "Inserted EntryOb id=$id date=$date content=$content")
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


            Log.d("ObjectBoxTest", "Inserted EntryOb id=$entryId date=$date content=$content")


            entryRepo.logAllEntries()

            onEntryInserted(entryId)
        }
    }

}