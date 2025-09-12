package com.bignerdranch.android.calendarapp3.entry_extra_data

object TimeUtils {
    // Convert minutes to formatted time string (HH:mm)
    fun formatTime(minutes: Int): String {
        val hours = minutes / 60
        val mins = minutes % 60
        return "${hours.toString().padStart(2, '0')}:${mins.toString().padStart(2, '0')}"
    }

    // Optional: Convert time string to minutes (if needed for migration)
    fun parseTime(timeString: String): Int {
        val parts = timeString.split(":")
        if (parts.size != 2) return 0
        val hours = parts[0].toIntOrNull() ?: 0
        val minutes = parts[1].toIntOrNull() ?: 0
        return hours * 60 + minutes
    }
}