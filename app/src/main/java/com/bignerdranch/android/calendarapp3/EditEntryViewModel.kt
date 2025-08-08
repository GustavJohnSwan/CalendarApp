package com.bignerdranch.android.calendarapp3

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import java.time.LocalDate

class EditEntryViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val EntryDao = db.entryDao()


    var selectedEntry by mutableStateOf<EntryTable?>(null)
        private set

    var selectedDate by mutableStateOf("")

    fun onEventSelect(entry: EntryTable) {
        selectedEntry = entry
    }

    fun saveSelectedDate(date: String) {
        selectedDate = date
    }

    /*
    var selectedEvent1 by mutableStateOf<String?>("")
        private set

    var selectedEvent2 by mutableStateOf<String?>("")
        private set

    var selectedEvent3 by mutableStateOf<String?>("")
        private set





    fun onEventSelect(text1: String?, text2: String?, text3: String?) {
        selectedEvent1 = text1
        selectedEvent2 = text2
        selectedEvent3 = text3
    }

     */

    // this changes the value of the selected entry in EntryTable
    fun updateEntry(entryTable: EntryTable) {
        viewModelScope.launch {
            EntryDao.update_Entry(entryTable)
        }
    }

}