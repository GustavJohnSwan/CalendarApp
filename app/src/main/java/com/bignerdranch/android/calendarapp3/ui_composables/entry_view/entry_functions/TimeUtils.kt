package com.bignerdranch.android.calendarapp3.ui_composables.entry_view.entry_functions

object TimeUtils {
    // Convert minutes to formatted time string (HH:mm)
    fun formatTime(minutes: Int): String {
        val hours = minutes / 60
        val mins = minutes % 60
        return "${hours.toString().padStart(2, '0')}:${mins.toString().padStart(2, '0')}"
    }

}