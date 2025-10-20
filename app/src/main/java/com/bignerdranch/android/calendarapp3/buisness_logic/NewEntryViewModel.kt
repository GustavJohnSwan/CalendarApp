package com.bignerdranch.android.calendarapp3.buisness_logic

import androidx.compose.runtime.State
import android.app.Application
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.bignerdranch.android.calendarapp3.database.AppDatabase
import com.bignerdranch.android.calendarapp3.database.EntryTable
import com.bignerdranch.android.calendarapp3.database.ExtraDataTable
import com.bignerdranch.android.calendarapp3.database.RecurringEvent
import kotlinx.coroutines.launch

// this is the second viewModel - a class that functions as a business logic or screen level state holder.
// this one is responsible for the business logic relating to interactions with the database
class NewEntryViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val entryDao = db.entryDao()
    private val extraDataDao = db.extraDataDao()  // Add this line

    private val recurringEventDao = db.recurringEventDao() // ADD THIS

    // For all entries
    private val _entryList = mutableStateOf<List<EntryTable>>(emptyList())
    val entryList: State<List<EntryTable>> = _entryList

    // For date-specific entries

    /*
    private val _dateEntries = mutableStateOf<List<EntryTable>>(emptyList())
    val dateEntries: State<List<EntryTable>> = _dateEntries

     */

    // Add this method to handle recurring events
    suspend fun saveRecurringEventToDatabase(entryId: Int, date: String) {
        Log.d("RepeatEvent", "Saving recurring event to DB - Original ID: $entryId, Date: $date")
        val recurringEvent = RecurringEvent(
            entryId = entryId,
            occurrenceDate = date
        )
        recurringEventDao.insert(recurringEvent)
    }
/*
    fun loadEntriesForDate(date: String) {
        viewModelScope.launch {
            _dateEntries.value = entryDao.getEntriesByDate(date)
        }
    }

 */

    // UPDATE this method to accept time parameter
// In EntryTableViewModel.kt
    // UPDATE this method to accept repeatDetails parameter
    suspend fun insertEntryWithExtraData(
        dateDB: String,
        entryDB: String,
        needsExtraData: Boolean,
        timeMinutes: Int? = null,
        reminderType: String? = null,
        repeat: String? = null,
        repeatDetails: String? = null // ADD THIS PARAMETER
    ): Int {
        // Insert main entry WITH TIME
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
                    repeatDetails = repeatDetails // ADD THIS FIELD
                )
            )
        }
        return entryId // RETURN the primary key
    }

    // Make this function available to UI
    // UPDATE this function to accept time parameter
    // UPDATE this function to accept reminderType
    // UPDATE this function to accept repeatDetails
    fun insertEntry(
        date: String,
        content: String,
        exDaBo: Boolean,
        timeMinutes: Int? = null,
        reminderType: String? = null,
        repeat: String?,
        repeatDetails: String? = null, // ADD THIS PARAMETER
        onEntryInserted: (Int) -> Unit = {} // ADD callback for the primary key
    ) {
        viewModelScope.launch {
            val entryId = insertEntryWithExtraData(
                date,
                content,
                exDaBo,
                timeMinutes,
                reminderType,
                repeat,
                repeatDetails // PASS THE PARAMETER
            )
            onEntryInserted(entryId) // CALL callback with the primary key
        }
    }

    // ADD THIS METHOD to update entry with time
// In EntryTableViewModel.kt - update the updateEntry method
    /*
    fun updateEntry(entry: EntryTable) {
        viewModelScope.launch {
            entryDao.update_Entry(entry)
            // Refresh the current view
            val currentDate = _dateEntries.value.firstOrNull()?.dateDB
            currentDate?.let { loadEntriesForDate(it) }
        }
    }
     */


}