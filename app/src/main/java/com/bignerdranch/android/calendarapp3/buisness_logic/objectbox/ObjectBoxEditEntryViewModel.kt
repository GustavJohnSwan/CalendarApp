package com.bignerdranch.android.calendarapp3.buisness_logic.objectbox




import android.app.Application
import android.util.Log
import androidx.compose.runtime.*
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.bignerdranch.android.calendarapp3.database.DAO.objectbox.ObjectBoxEntryRepository
import com.bignerdranch.android.calendarapp3.database.DAO.objectbox.ObjectBoxExtraDataRepository
import com.bignerdranch.android.calendarapp3.database.objectbox.ObjectBoxProvider
import com.bignerdranch.android.calendarapp3.database.objectbox.domain.model.EntryOb
import com.bignerdranch.android.calendarapp3.database.objectbox.domain.model.ExtraDataOb
import com.bignerdranch.android.calendarapp3.ui_composables.entry_view.entry_functions.repeat_function.repeat_underfunctions.RepeatOptions
import com.bignerdranch.android.calendarapp3.ui_composables.entry_view.entry_functions.repeat_function.rrule_generation.parseRRuleToRepeatOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ObjectBoxEditEntryViewModel(application: Application) : AndroidViewModel(application) {

    private val store = ObjectBoxProvider.get()
    private val entryRepo = ObjectBoxEntryRepository(store)
    private val extraRepo = ObjectBoxExtraDataRepository(store)

    var selectedEntry by mutableStateOf<EntryOb?>(null)
    var selectedDate by mutableStateOf("")
    var hasReminder by mutableStateOf(false)
    var selectedReminderType by mutableStateOf("None")
    var selectedRepeatType by mutableStateOf("Never")
    var repeatOptions by mutableStateOf(RepeatOptions())

    private val _dateEntries = mutableStateOf<List<EntryOb>>(emptyList())
    val dateEntries: State<List<EntryOb>> = _dateEntries

    // -----------------------------
    // Load entries by date
    // -----------------------------
    fun loadEntriesForDate(date: String) {
        Log.d("ObjectBoxTest", "Loaded ${_dateEntries.value.size} entries for $date")
        viewModelScope.launch {
            _dateEntries.value = entryRepo.getEntriesByDate(date)
        }
    }

    // -----------------------------
    // Select entry
    // -----------------------------
    fun onEventSelect(entry: EntryOb) {
        selectedEntry = entry

        viewModelScope.launch {
            val extraData = extraRepo.getExtraDataByEntryId(entry.id)

            hasReminder = extraData != null
            selectedReminderType = extraData?.reminderTypeOb ?: "None"
            selectedRepeatType = extraData?.repeatOb ?: "Never"

            if (extraData != null &&
                !extraData.repeatDetailsOb.isNullOrEmpty() &&
                extraData.repeatOb != null
            ) {
                repeatOptions = parseRRuleToRepeatOptions(
                    extraData.repeatDetailsOb!!,
                    extraData.repeatOb!!
                )
            } else {
                repeatOptions = RepeatOptions()
            }
        }
    }

    // -----------------------------
    // Update basic entry only
    // -----------------------------

/*
    fun updateBasicEntry(entry: EntryOb) {
        viewModelScope.launch {
            entryRepo.update_Entry(entry)
            val currentDate = entry.dateOb
            currentDate?.let { loadEntriesForDate(it) }
        }
    }

 */

    // -----------------------------
    // Full update logic (mirror of Room)
    // -----------------------------
    fun updateEntry(
        entry: EntryOb,
        hasReminder: Boolean,
        reminderType: String?,
        repeatType: String?,
        repeatDetails: String? = null
    ) {
        viewModelScope.launch(Dispatchers.IO) {

            entryRepo.update_Entry(entry)

            val existingExtra = extraRepo.getExtraDataByEntryId(entry.id)

            if (hasReminder || repeatType != null) {

                val newReminderType = if (hasReminder) reminderType else null
                val newRepeatType = if (repeatType != "Never") repeatType else null

                if (existingExtra == null &&
                    (newReminderType != null || newRepeatType != null)
                ) {

                    extraRepo.insertExtraData(
                        ExtraDataOb(
                            reminderTypeOb = newReminderType,
                            repeatOb = newRepeatType,
                            repeatDetailsOb = repeatDetails,
                            attachmentIdOb = null
                        ),
                        entry.id
                    )

                } else if (existingExtra != null) {

                    if (newReminderType != null || newRepeatType != null) {

                        existingExtra.reminderTypeOb = newReminderType
                        existingExtra.repeatOb = newRepeatType
                        existingExtra.repeatDetailsOb = repeatDetails

                        extraRepo.update_ExData(existingExtra)

                    } else {
                        extraRepo.delete_ExData(existingExtra)
                    }
                }

            } else {
                existingExtra?.let { extraRepo.delete_ExData(it) }
            }
            val currentDate = entry.dateOb
            currentDate?.let { loadEntriesForDate(it) }
        }

    }

    fun getById(id: Long): EntryOb? {
        return store.boxFor(EntryOb::class.java).get(id)
    }

    suspend fun getEntryById(id: Long): EntryOb? = withContext(Dispatchers.IO) {
        entryRepo.getById(id)
    }
}