package com.bignerdranch.android.calendarapp3

import androidx.compose.runtime.State
import android.app.Application
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

// this is the second viewModel - a class that functions as a business logic or screen level state holder.
// this one is responsible for the business logic relating to interactions with the database
class EntryTableViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val entryDao = db.entryDao()
    private val extraDataDao = db.extraDataDao()  // Add this line

    // For all entries
    private val _entryList = mutableStateOf<List<EntryTable>>(emptyList())
    val entryList: State<List<EntryTable>> = _entryList

    // For date-specific entries
    private val _dateEntries = mutableStateOf<List<EntryTable>>(emptyList())
    val dateEntries: State<List<EntryTable>> = _dateEntries

    fun loadEntriesForDate(date: String) {
        viewModelScope.launch {
            _dateEntries.value = entryDao.getEntriesByDate(date)
        }
    }

    // ADD THIS METHOD to update entry time
    fun updateEntryTime(entryId: Int, timeMinutes: Int) {
        viewModelScope.launch {
            entryDao.updateTime(entryId, timeMinutes)
            // Refresh the current view if needed
            val currentDate = _dateEntries.value.firstOrNull()?.dateDB
            currentDate?.let { loadEntriesForDate(it) }
        }
    }

    // UPDATE this method to accept time parameter
    suspend fun insertEntryWithExtraData(
        dateDB: String,
        entryDB: String,
        needsExtraData: Boolean,
        timeMinutes: Int? = null,
        reminderType: String? = null // ADD THIS
        ) {
        // Insert main entry WITH TIME
        val newEntry = EntryTable(
            dateDB = dateDB,
            entryDB = entryDB,
            timeMinutes = timeMinutes
        )
        entryDao.insert_IntoEntryTable(newEntry)

        if (needsExtraData) {
            // Get the ID of the entry we just inserted
            val entries = entryDao.getEntriesByDate(dateDB)
            val insertedEntry = entries.last()

            extraDataDao.insertExtraData(
                ExtraDataTable(
                    entryId = insertedEntry.id,
                    reminderType = reminderType // STORE THE SELECTED OPTION
                )
            )
        }
    }

    suspend fun insertEntryInEntryTable(dateDB: String, entryDB: String) {

    }

    // Make this function available to UI
    // UPDATE this function to accept time parameter
    // UPDATE this function to accept reminderType
    fun insertEntry(date: String, content: String, exDaBo: Boolean, timeMinutes: Int? = null, reminderType: String? = null) {
        viewModelScope.launch {
            insertEntryWithExtraData(date, content, exDaBo, timeMinutes, reminderType)
        }
    }

    fun getAllEntries() {
        viewModelScope.launch {
            _entryList.value = entryDao.get_AllEntries()
        }
    }

    // ADD THIS METHOD to update entry with time
    fun updateEntry(entry: EntryTable) {
        viewModelScope.launch {
            entryDao.update_Entry(entry)
            // Refresh the current view
            val currentDate = _dateEntries.value.firstOrNull()?.dateDB
            currentDate?.let { loadEntriesForDate(it) }
        }
    }

    fun deleteEntry(entry: EntryTable) {
        viewModelScope.launch {
            entryDao.delete_Entry(entry)
            getAllEntries()
        }
    }


}