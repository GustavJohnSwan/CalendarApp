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
    private val entryDao = db.entryDao()
    private val extraDataDao = db.extraDataDao() // ADD THIS

    var selectedEntry by mutableStateOf<EntryTable?>(null)
    var selectedDate by mutableStateOf("")
    //var hasReminder by mutableStateOf(false) // ADD THIS for checkbox state
    var selectedReminderType by mutableStateOf("None") // ADD THIS

    fun onEventSelect(entry: EntryTable) {
        selectedEntry = entry
        // Load reminder status when entry is selected
        viewModelScope.launch {
            val extraData = extraDataDao.get_AllExData().find { it.entryId == entry.id }
            //hasReminder = extraData != null
            selectedReminderType = extraData?.reminderType ?: "None" // SET THE SELECTED TYPE
        }
    }

    fun saveSelectedDate(date: String) {
        selectedDate = date
    }

    fun updateEntry(entryTable: EntryTable, hasReminder: Boolean, reminderType: String?) { // UPDATE THIS
        viewModelScope.launch {
            entryDao.update_Entry(entryTable)

            // Handle reminder data
            val existingExtraData = extraDataDao.get_AllExData().find { it.entryId == entryTable.id }

            if (hasReminder && reminderType != null) {
                if (existingExtraData == null) {
                    // Create new reminder
                    extraDataDao.insertExtraData(ExtraDataTable(entryId = entryTable.id, reminderType = reminderType))
                }else {
                    // Update existing reminder type
                    extraDataDao.update_ExData(
                        existingExtraData.copy(reminderType = reminderType)
                    )
                }
            } else {
                // Remove reminder if it exists
                existingExtraData?.let { extraDataDao.delete_ExData(it) }
            }
        }
    }
}