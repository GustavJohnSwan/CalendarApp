package com.bignerdranch.android.calendarapp3.buisness_logic

import android.app.Application
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.bignerdranch.android.calendarapp3.database.AppDatabase
import com.bignerdranch.android.calendarapp3.database.EntryTable
import com.bignerdranch.android.calendarapp3.database.ExtraDataTable
import com.bignerdranch.android.calendarapp3.ui_composables.entry_view.entry_functions.repeat_function.repeat_underfunctions.RepeatOptions
import com.bignerdranch.android.calendarapp3.ui_composables.entry_view.entry_functions.repeat_function.rrule_generation.parseRRuleToRepeatOptions
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

    var selectedCouchbaseId: String? = null
    var selectedObjectBoxId: String? = null


    fun onEventSelect(entry: EntryTable) {
        selectedEntry = entry
        // Load reminder status and repeat details when entry is selected
        viewModelScope.launch {
            val extraData = extraDataDao.getExtraDataByEntryId(entry.id)
            hasReminder = extraData != null
            selectedReminderType = extraData?.reminderType ?: "None"
            selectedRepeatType = extraData?.repeat ?: "Never"

            // Load repeat options from extra data
            if (extraData != null && !extraData.repeatDetails.isNullOrEmpty() && extraData.repeat != null) {
                // parse the stored RRULE back into RepeatOptions
                repeatOptions = parseRRuleToRepeatOptions(extraData.repeatDetails!!, extraData.repeat!!)
            } else {
                repeatOptions = RepeatOptions() // Reset to default
            }
        }
    }

    fun saveSelectedDate(date: String) {
        selectedDate = date
    }

    /* ____________________________________________________________________________________________ */
    /* ____________________________________________________________________________________________ */
    /* ____________________________________________________________________________________________ */


    private val _dateEntries = mutableStateOf<List<EntryTable>>(emptyList())
    val dateEntries: State<List<EntryTable>> = _dateEntries

    fun loadEntriesForDate(date: String) {
        viewModelScope.launch {
            _dateEntries.value = entryDao.getEntriesByDate(date)
        }
    }

    /* ____________________________________________________________________________________________ */
    /* ____________________________________________________________________________________________ */
    /* ____________________________________________________________________________________________ */

    fun updateEntry(
        entryTable: EntryTable,
        hasReminder: Boolean,
        reminderType: String?,
        repeatType: String?,
        repeatDetails: String? = null
    ) {
        viewModelScope.launch {
            entryDao.update_Entry(entryTable)

            // Handles reminder and repeat data
            val existingExtraData = extraDataDao.getExtraDataByEntryId(entryTable.id)

            if (hasReminder || repeatType != null) {
                val newReminderType = if (hasReminder) reminderType else null
                val newRepeatType = if (repeatType != "Never") repeatType else null

                if (existingExtraData == null && (newReminderType != null || newRepeatType != null)) {
                    // Creates new extra data
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
                        // Updates existing extra data
                        extraDataDao.update_ExData(
                            existingExtraData.copy(
                                reminderType = newReminderType,
                                repeat = newRepeatType,
                                repeatDetails = repeatDetails
                            )
                        )
                    } else {
                        // Removes extra data if no longer needed
                        extraDataDao.delete_ExData(existingExtraData)
                    }
                }
            } else {
                // Removes extra data if it exists and no reminder/repeat is needed
                existingExtraData?.let { extraDataDao.delete_ExData(it) }
            }
            val currentDate = _dateEntries.value.firstOrNull()?.dateDB
            currentDate?.let { loadEntriesForDate(it) }

        }

    }

    fun deleteEntry(entryTable: EntryTable) {
        viewModelScope.launch {
            val existingExtraData = extraDataDao.getExtraDataByEntryId(entryTable.id)
            existingExtraData?.let { extraDataDao.delete_ExData(it) }

            entryDao.delete_Entry(entryTable)

            entryTable.dateDB?.let { loadEntriesForDate(it) }
        }
    }
}