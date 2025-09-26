package com.bignerdranch.android.calendarapp3.buisness_logic

import androidx.compose.runtime.State
import android.app.Application
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.bignerdranch.android.calendarapp3.database.AppDatabase
import com.bignerdranch.android.calendarapp3.database.EntryTable
import com.bignerdranch.android.calendarapp3.database.ExtraDataTable
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
// In EntryTableViewModel.kt
    // UPDATE this method to accept repeatDetails parameter
    suspend fun insertEntryWithExtraData(
        dateDB: String,
        entryDB: String,
        needsExtraData: Boolean,
        timeMinutes: Int? = null,
        reminderType: String? = null,
        repeat: String? = null,
        repeatDetails: String? = null // ADD THIS PARAMETER
    ) {
        // Insert main entry WITH TIME
        val newEntry = EntryTable(
            dateDB = dateDB,
            entryDB = entryDB,
            timeMinutes = timeMinutes
        )
        val entryId = entryDao.insert_IntoEntryTable(newEntry).toInt()

        if (needsExtraData) {
            extraDataDao.insertExtraData(
                ExtraDataTable(
                    entryId = entryId,
                    reminderType = reminderType,
                    repeat = repeat,
                    repeatDetails = repeatDetails // ADD THIS FIELD
                )
            )
        }
    }

    suspend fun insertEntryInEntryTable(dateDB: String, entryDB: String) {

    }

    // Make this function available to UI
    // UPDATE this function to accept time parameter
    // UPDATE this function to accept reminderType
    // UPDATE this function to accept repeatDetails
    fun insertEntry(
        date: String,
        content: String,
        exDaBo: Boolean,
        timeMinutes: Int? = null,
        reminderType: String? = null,
        repeat: String?,
        repeatDetails: String? = null // ADD THIS PARAMETER
    ) {
        viewModelScope.launch {
            insertEntryWithExtraData(
                date,
                content,
                exDaBo,
                timeMinutes,
                reminderType,
                repeat,
                repeatDetails // PASS THE PARAMETER
            )
        }
    }

    fun getAllEntries() {
        viewModelScope.launch {
            _entryList.value = entryDao.get_AllEntries()
        }
    }

    // ADD THIS METHOD to update entry with time
// In EntryTableViewModel.kt - update the updateEntry method
    fun updateEntry(entry: EntryTable) {
        viewModelScope.launch {
            entryDao.update_Entry(entry)
            // Refresh the current view
            val currentDate = _dateEntries.value.firstOrNull()?.dateDB
            currentDate?.let { loadEntriesForDate(it) }
        }
    }

    // Add this method to handle extra data updates
    fun updateEntryWithExtraData(
        entry: EntryTable,
        extraData: ExtraDataTable?
    ) {
        viewModelScope.launch {
            entryDao.update_Entry(entry)

            if (extraData != null) {
                val existingExtraData = extraDataDao.getExtraDataByEntryId(entry.id)
                if (existingExtraData != null) {
                    extraDataDao.update_ExData(extraData)
                } else {
                    extraDataDao.insertExtraData(extraData)
                }
            } else {
                // Remove extra data if it exists
                val existingExtraData = extraDataDao.getExtraDataByEntryId(entry.id)
                existingExtraData?.let { extraDataDao.delete_ExData(it) }
            }

            // Refresh the current view
            val currentDate = _dateEntries.value.firstOrNull()?.dateDB
            currentDate?.let { loadEntriesForDate(it) }
        }
    }

    // ADD THIS METHOD to update extra data (including repeat details)
    fun updateExtraData(extraData: ExtraDataTable) {
        viewModelScope.launch {
            extraDataDao.update_ExData(extraData)
        }
    }

    fun deleteEntry(entry: EntryTable) {
        viewModelScope.launch {
            entryDao.delete_Entry(entry)
            getAllEntries()
        }
    }
}