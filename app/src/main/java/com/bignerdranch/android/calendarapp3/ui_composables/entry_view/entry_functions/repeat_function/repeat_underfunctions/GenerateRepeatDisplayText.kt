package com.bignerdranch.android.calendarapp3.ui_composables.entry_view.entry_functions.repeat_function.repeat_underfunctions

import com.bignerdranch.android.calendarapp3.ui_composables.entry_view.entry_functions.repeat_function.getMonthName


// Function to generate display text for the repeat selector
fun generateRepeatDisplayText(repeatType: String, options: RepeatOptions): String {
    val baseText = when (repeatType) {
        "Never" -> "Repeat: Never"
        "Daily" -> "Repeat: Daily (every ${options.interval} day${if (options.interval > 1) "s" else ""})"
        "Weekly" -> {
            val daysText = if (options.selectedDays.isNotEmpty()) {
                val dayNames = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
                options.selectedDays.sorted().joinToString(", ") { dayIndex ->
                    dayNames.getOrElse(dayIndex - 1) { "Day $dayIndex" }
                }
            } else {
                "No days selected"
            }
            "Repeat: Weekly (every ${options.interval} week${if (options.interval > 1) "s" else ""} on $daysText)"
        }
        "Monthly" -> {
            val monthlyText = if (options.monthlyType == "absolute") {
                "on day ${options.absoluteDay}"
            } else {
                "${options.relativeWeek} ${options.relativeDay}"
            }
            "Repeat: Monthly (every ${options.interval} month${if (options.interval > 1) "s" else ""} $monthlyText)"
        }
        "Yearly" -> "Repeat: Yearly (every year on ${getMonthName(options.month)} ${options.yearlyDay})"
        else -> "Repeat: $repeatType"
    }


    val endText = when (options.endType) {
        "never" -> ""
        "on_date" -> " • Ends on ${options.endDateDay}/${options.endDateMonth}/${options.endDateYear}"
        "after_occurrences" -> " • Ends after ${options.occurrences} occurrence${if (options.occurrences > 1) "s" else ""}"
        else -> ""
    }

    return baseText + endText
}