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
    var repeatOptions by mutableStateOf(RepeatOptions())

    fun onEventSelect(entry: EntryTable) {
        selectedEntry = entry
        // Load reminder status and repeat details when entry is selected
        viewModelScope.launch {
            val extraData = extraDataDao.getExtraDataByEntryId(entry.id)
            hasReminder = extraData != null
            selectedReminderType = extraData?.reminderType ?: "None"
            selectedRepeatType = extraData?.repeat ?: "Never"

            // Load repeat options from extra data
            if (extraData != null && !extraData.repeatDetails.isNullOrEmpty()) {
                repeatOptions = RepeatOptionsSerializer.deserialize(extraData.repeatDetails!!)
            } else {
                repeatOptions = RepeatOptions() // Reset to default
            }
        }
    }

    fun saveSelectedDate(date: String) {
        selectedDate = date
    }

    fun updateEntry(
        entryTable: EntryTable,
        hasReminder: Boolean,
        reminderType: String?,
        repeatType: String?,
        repeatDetails: String? = null
    ) {
        viewModelScope.launch {
            entryDao.update_Entry(entryTable)

            // Handle reminder and repeat data
            val existingExtraData = extraDataDao.getExtraDataByEntryId(entryTable.id)

            if (hasReminder || repeatType != null) {
                val newReminderType = if (hasReminder) reminderType else null
                val newRepeatType = if (repeatType != "Never") repeatType else null

                if (existingExtraData == null && (newReminderType != null || newRepeatType != null)) {
                    // Create new extra data
                    extraDataDao.insertExtraData(
                        ExtraDataTable(
                            entryId = entryTable.id,
                            reminderType = newReminderType,
                            repeat = newRepeatType,
                            repeatDetails = repeatDetails
                        )
                    )
                } else if (existingExtraData != null) {
                    if (newReminderType != null || newRepeatType != null) {
                        // Update existing extra data
                        extraDataDao.update_ExData(
                            existingExtraData.copy(
                                reminderType = newReminderType,
                                repeat = newRepeatType,
                                repeatDetails = repeatDetails
                            )
                        )
                    } else {
                        // Remove extra data if no longer needed
                        extraDataDao.delete_ExData(existingExtraData)
                    }
                }
            } else {
                // Remove extra data if it exists and no reminder/repeat is needed
                existingExtraData?.let { extraDataDao.delete_ExData(it) }
            }
        }
    }
}