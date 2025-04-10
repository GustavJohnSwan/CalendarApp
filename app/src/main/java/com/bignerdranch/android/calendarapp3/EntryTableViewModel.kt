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
    private val EntryDao = db.entryDao()

    // this list holds the values of EntryTable
    private val _entryList = mutableStateOf<List<EntryTable>>(emptyList())

    // this exposes the list as READ-ONLY
    val entryList: State<List<EntryTable>> = _entryList

    // this inserts a new entry in the EntryTable
    fun insertEntryTable(dateDB: String, entryDB: String, idEx: String) {
        viewModelScope.launch {
            EntryDao.insert_IntoEntryTable(EntryTable(id = 0, dateDB = dateDB, entryDB = entryDB, idEx = idEx))
        }
    }

    // this gets all the entries from the database EntryTable and inserts them into the List
    fun getAllEntryTables() {
        viewModelScope.launch {
            val entryTables = EntryDao.get_AllEntries()
            _entryList.value = entryTables // Update the state
        }
    }

    // this changes the value of the selected entry in EntryTable
    fun updateEntryTable(entryTable: EntryTable) {
        viewModelScope.launch {
            EntryDao.update_Entry(entryTable)
        }
    }

    // this deletes the selected entry from the database table EntryTable
    fun deleteEntryTable(entryTable: EntryTable) {
        viewModelScope.launch {
            EntryDao.delete_Entry(entryTable)
            getAllEntryTables() // Refresh list after delete IMPLEMENT THIS FROM THE BEGINNING AUTOMATICALLY
        }
    }


}
