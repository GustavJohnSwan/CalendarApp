package com.bignerdranch.android.calendarapp3.entry_extra_data

import com.bignerdranch.android.calendarapp3.database.EntryTable

// EventInstance.kt
data class EventInstance(
    val id: String, // Unique ID for this instance (masterId + date)
    val masterEventId: Int, // Link back to the original event
    val title: String,
    val date: String,
    val timeMinutes: Int?,
    val isRecurringInstance: Boolean
) {
    companion object {
        fun fromEntry(entry: EntryTable, isRecurringInstance: Boolean): EventInstance {
            return EventInstance(
                id = entry.id.toString(),
                masterEventId = entry.id,
                title = entry.entryDB ?: "",
                date = entry.dateDB ?: "",
                timeMinutes = entry.timeMinutes,
                isRecurringInstance = isRecurringInstance
            )
        }
    }
}