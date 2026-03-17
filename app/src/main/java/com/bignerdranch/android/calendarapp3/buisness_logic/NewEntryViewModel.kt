package com.bignerdranch.android.calendarapp3.buisness_logic

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.bignerdranch.android.calendarapp3.database.AppDatabase
import com.bignerdranch.android.calendarapp3.database.EntryTable
import com.bignerdranch.android.calendarapp3.database.ExtraDataTable
import kotlinx.coroutines.launch


class NewEntryViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val entryDao = db.entryDao()
    private val extraDataDao = db.extraDataDao()


    suspend fun insertEntryWithExtraData(
        dateDB: String,
        entryDB: String,
        needsExtraData: Boolean,
        timeMinutes: Int? = null,
        reminderType: String? = null,
        repeat: String? = null,
        repeatDetails: String? = null
    ): Int {
        // Insert main entry with time
        val newEntry = EntryTable(
            dateDB = dateDB,
            entryDB = entryDB,
            timeMinutes = timeMinutes
        )
        val entryId = entryDao.insert_IntoEntryTable(newEntry).toInt()

        if (needsExtraData) {
            extraDataDao.insertExtraData(
                ExtraDataTable(
                    entryId = entryId,
                    reminderType = reminderType,
                    repeat = repeat,
                    repeatDetails = repeatDetails
                )
            )
        }
        return entryId // return the primary key
    }

    fun insertEntry(
        date: String,
        content: String,
        exDaBo: Boolean,
        timeMinutes: Int? = null,
        reminderType: String? = null,
        repeat: String?,
        repeatDetails: String? = null,
        onEntryInserted: (Int) -> Unit = {} // callback for the primary key
    ) {
        viewModelScope.launch {
            val entryId = insertEntryWithExtraData(
                date,
                content,
                exDaBo,
                timeMinutes,
                reminderType,
                repeat,
                repeatDetails
            )
            onEntryInserted(entryId)
        }
    }


}