package com.bignerdranch.android.calendarapp3

object RepeatOptionsSerializer {
    fun serialize(options: RepeatOptions, repeatType: String): String {
        return when (repeatType) {
            "Daily" -> "interval=${options.interval}&endType=${options.endType}" +
                    "&occurrences=${options.occurrences}"

            "Weekly" -> "interval=${options.interval}&days=${options.selectedDays.joinToString(",")}" +
                    "&endType=${options.endType}&occurrences=${options.occurrences}"

            "Monthly" -> "type=${options.monthlyType}&interval=${options.interval}" +
                    if (options.monthlyType == "absolute") {
                        "&day=${options.absoluteDay}"
                    } else {
                        "&week=${options.relativeWeek}&day=${options.relativeDay}"
                    } + "&endType=${options.endType}&occurrences=${options.occurrences}"

            "Yearly" -> "month=${options.month}&day=${options.yearlyDay}" +
                    "&endType=${options.endType}&occurrences=${options.occurrences}"

            else -> ""
        }
    }

    fun deserialize(serialized: String): RepeatOptions {
        val options = RepeatOptions()
        if (serialized.isEmpty()) return options

        val params = serialized.split("&").associate {
            val parts = it.split("=")
            parts[0] to if (parts.size > 1) parts[1] else ""
        }

        params["interval"]?.toIntOrNull()?.let { options.interval = it }
        params["endType"]?.let { options.endType = it }
        params["occurrences"]?.toIntOrNull()?.let { options.occurrences = it }

        params["days"]?.let { daysStr ->
            options.selectedDays = daysStr.split(",").mapNotNull { it.toIntOrNull() }.toSet()
        }

        params["type"]?.let { options.monthlyType = it }
        params["day"]?.toIntOrNull()?.let { options.absoluteDay = it }
        params["week"]?.let { options.relativeWeek = it }
        params["relativeDay"]?.let { options.relativeDay = it }

        params["month"]?.toIntOrNull()?.let { options.month = it }
        params["yearlyDay"]?.toIntOrNull()?.let { options.yearlyDay = it }

        return options
    }

    // Add this to RepeatOptionsSerializer.kt
    fun getRepeatOptionsForEntry(extraData: ExtraDataTable?, repeatType: String): RepeatOptions {
        return if (extraData?.repeatDetails != null && extraData.repeatDetails.isNotEmpty()) {
            deserialize(extraData.repeatDetails)
        } else {
            // Return default options based on repeat type
            val options = RepeatOptions()
            when (repeatType) {
                "Weekly" -> options.copy(selectedDays = setOf(1)) // Default to Monday
                else -> options
            }
        }
    }
}