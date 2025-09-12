package com.bignerdranch.android.calendarapp3.entry_extra_data

import com.bignerdranch.android.calendarapp3.database.ExtraDataTable

object RepeatOptionsSerializer {
    fun serialize(options: RepeatOptions, repeatType: String): String {
        var baseString = when (repeatType) {
            "Daily" -> "interval=${options.interval}"
            "Weekly" -> "interval=${options.interval}&days=${options.selectedDays.joinToString(",")}"
            "Monthly" -> "type=${options.monthlyType}&interval=${options.interval}" +
                    if (options.monthlyType == "absolute") {
                        "&day=${options.absoluteDay}"
                    } else {
                        "&week=${options.relativeWeek}&day=${options.relativeDay}"
                    }
            "Yearly" -> "month=${options.month}&day=${options.yearlyDay}"
            else -> ""
        }

        // Add end options to ALL repeat types
        baseString += "&endType=${options.endType}"

        when (options.endType) {
            "on_date" -> baseString += "&endDateDay=${options.endDateDay}&endDateMonth=${options.endDateMonth}&endDateYear=${options.endDateYear}"
            "after_occurrences" -> baseString += "&occurrences=${options.occurrences}"
        }

        return baseString
    }

    fun deserialize(serialized: String): RepeatOptions {
        val options = RepeatOptions()
        if (serialized.isEmpty()) return options

        val params = serialized.split("&").associate {
            val parts = it.split("=")
            parts[0] to if (parts.size > 1) parts[1] else ""
        }

        // Parse basic fields
        params["interval"]?.toIntOrNull()?.let { options.interval = it }
        params["endType"]?.let { options.endType = it }
        params["occurrences"]?.toIntOrNull()?.let { options.occurrences = it }

        // Parse weekly days
        params["days"]?.let { daysStr ->
            options.selectedDays = daysStr.split(",").mapNotNull { it.toIntOrNull() }.toSet()
        }

        // Parse monthly options
        params["type"]?.let { options.monthlyType = it }
        params["day"]?.toIntOrNull()?.let { options.absoluteDay = it }
        params["week"]?.let { options.relativeWeek = it }
        params["relativeDay"]?.let { options.relativeDay = it }

        // Parse yearly options
        params["month"]?.toIntOrNull()?.let { options.month = it }
        params["yearlyDay"]?.toIntOrNull()?.let { options.yearlyDay = it }

        // Parse end date fields
        params["endDateDay"]?.toIntOrNull()?.let { options.endDateDay = it }
        params["endDateMonth"]?.toIntOrNull()?.let { options.endDateMonth = it }
        params["endDateYear"]?.toIntOrNull()?.let { options.endDateYear = it }

        return options
    }

    // Add this to RepeatOptionsSerializer.kt
// Add this to RepeatOptionsSerializer.kt
// Add this to RepeatOptionsSerializer.kt
    fun getRepeatOptionsForEntry(extraData: ExtraDataTable?, repeatType: String): RepeatOptions {
        return if (extraData != null && extraData.repeatDetails != null && extraData.repeatDetails.isNotEmpty()) {
            deserialize(extraData.repeatDetails)
        } else {
            // Return default options based on repeat type with proper end date defaults
            val options = RepeatOptions()
            when (repeatType) {
                "Weekly" -> options.copy(selectedDays = setOf(1))
                else -> options
            }
        }
    }
}