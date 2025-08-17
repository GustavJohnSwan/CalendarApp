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

    suspend fun insertEntryWithExtraData(dateDB: String, entryDB: String, needsExtraData: Boolean) {
        // Insert main entry
        entryDao.insert_IntoEntryTable(EntryTable(dateDB = dateDB, entryDB = entryDB))

        if (needsExtraData) {
            // Get the ID of the entry we just inserted
            val entries = entryDao.getEntriesByDate(dateDB)
            val newEntry = entries.last()

            // Insert extra data with PROPER COLUMN NAME that matches your table
            extraDataDao.insertExtraData(
                ExtraDataTable(
                    entryId = newEntry.id  // This must match @ColumnInfo name exactly
                )
            )
        }
    }

    suspend fun insertEntryInEntryTable(dateDB: String, entryDB: String) {

    }

    // Make this function available to UI
    fun insertEntry(date: String, content: String, exDaBo: Boolean) {
        viewModelScope.launch {
            insertEntryWithExtraData(date, content, exDaBo)
        }
    }

    fun getAllEntries() {
        viewModelScope.launch {
            _entryList.value = entryDao.get_AllEntries()
        }
    }

    fun updateEntry(entry: EntryTable) {
        viewModelScope.launch {
            entryDao.update_Entry(entry)
            getAllEntries()
        }
    }

    fun deleteEntry(entry: EntryTable) {
        viewModelScope.launch {
            entryDao.delete_Entry(entry)
            getAllEntries()
        }
    }
}