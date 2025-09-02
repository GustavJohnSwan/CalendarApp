package com.bignerdranch.android.calendarapp3

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.bignerdranch.android.calendarapp3.RepeatOptionsSerializer.getRepeatOptionsForEntry
import kotlinx.coroutines.launch

class EditEntryViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val entryDao = db.entryDao()
    private val extraDataDao = db.extraDataDao()

    var selectedEntry by mutableStateOf<EntryTable?>(null)
    var selectedDate by mutableStateOf("")
    var hasReminder by mutableStateOf(false)
    var selectedReminderType by mutableStateOf("None")
    var selectedRepeatType by mutableStateOf("Never")
    var repeatOptions by mutableStateOf(RepeatOptions()) // ADD THIS

    fun onEventSelect(entry: EntryTable) {
        selectedEntry = entry
        // Load reminder status and repeat details when entry is selected
        viewModelScope.launch {
            val extraData = extraDataDao.get_AllExData().find { it.entryId == entry.id }
            hasReminder = extraData != null
            selectedReminderType = extraData?.reminderType ?: "None"
            selectedRepeatType = extraData?.repeat ?: "Never"

            // USE THE UTILITY FUNCTION INSTEAD OF MANUAL DESERIALIZATION
            repeatOptions = getRepeatOptionsForEntry(extraData, selectedRepeatType)
        }
    }

    fun saveSelectedDate(date: String) {
        selectedDate = date
    }

    // UPDATE THIS FUNCTION to handle repeat details
    fun updateEntry(
        entryTable: EntryTable,
        hasReminder: Boolean,
        reminderType: String?,
        repeatType: String?,
        repeatDetails: String? = null // ADD THIS PARAMETER
    ) {
        viewModelScope.launch {
            entryDao.update_Entry(entryTable)

            // Handle reminder and repeat data
            val existingExtraData = extraDataDao.get_AllExData().find { it.entryId == entryTable.id }

            if (hasReminder && (reminderType != null || repeatType != null)) {
                val newReminderType = if (hasReminder) reminderType else null
                val newRepeatType = if (hasReminder) repeatType else null

                if (existingExtraData == null) {
                    // Create new extra data with reminder, repeat, and details
                    extraDataDao.insertExtraData(
                        ExtraDataTable(
                            entryId = entryTable.id,
                            reminderType = newReminderType,
                            repeat = newRepeatType,
                            repeatDetails = repeatDetails // ADD THIS
                        )
                    )
                } else {
                    // Update existing extra data with all fields
                    extraDataDao.update_ExData(
                        existingExtraData.copy(
                            reminderType = newReminderType,
                            repeat = newRepeatType,
                            repeatDetails = repeatDetails // ADD THIS
                        )
                    )
                }
            } else {
                // Remove extra data if it exists and no reminder/repeat is needed
                existingExtraData?.let { extraDataDao.delete_ExData(it) }
            }
        }
    }
}