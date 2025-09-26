package com.bignerdranch.android.calendarapp3.entry_extra_data

import com.bignerdranch.android.calendarapp3.database.EntryTable

data class EventInstance(
    val id: String, // Unique ID for this instance (masterId + date)
    val masterEventId: Int, // Link back to the original event
    val title: String,
    val date: String,
    val timeMinutes: Int?,
    val isRecurringInstance: Boolean
) {
    companion object {
        fun fromEntry(entry: EntryTable, isRecurringInstance: Boolean = false): EventInstance {
            return EventInstance(
                id = if (isRecurringInstance) "${entry.id}_${entry.dateDB}" else entry.id.toString(),
                masterEventId = entry.id,
                title = entry.entryDB ?: "",
                date = entry.dateDB ?: "",
                timeMinutes = entry.timeMinutes,
                isRecurringInstance = isRecurringInstance
            )
        }
    }

    // Helper function to check if this instance occurs on a specific date
    fun occursOnDate(targetDate: String): Boolean {
        return date == targetDate
    }
}