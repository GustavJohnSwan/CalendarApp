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

    suspend fun insertEntryWithExtraData(dateDB: String, entryDB: String) {
        // First insert empty ExtraData entry with all null values
        val extraDataId = extraDataDao.insertExtraData(ExtraDataTable())

        // Then insert main entry with reference
        val entry = EntryTable(
            id = 0,
            dateDB = dateDB,
            entryDB = entryDB,
            idEx = extraDataId.toInt()
        )
        entryDao.insert_IntoEntryTable(entry)
    }

    // Make this function available to UI
    fun insertEntry(date: String, content: String) {
        viewModelScope.launch {
            insertEntryWithExtraData(date, content)
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