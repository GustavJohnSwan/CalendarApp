package com.bignerdranch.android.calendarapp3.ui_composables.entry_view.entry_functions.repeat_function.rrule_generation

import com.bignerdranch.android.calendarapp3.ui_composables.entry_view.entry_functions.repeat_function.repeat_underfunctions.RepeatOptions
import com.philjay.Frequency
import com.philjay.RRule
import com.philjay.Weekday
import com.philjay.WeekdayNum
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date

/**
 * Converts RepeatOptions into a standard RRule string.
 */
fun generateRRuleString(
    options: RepeatOptions,
    repeatType: String
): String {

    val rule = RRule()
    val dateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'").withZone(ZoneOffset.UTC)

    // Set frequency
    rule.freq = when (repeatType) {
        "Daily" -> Frequency.Daily
        "Weekly" -> Frequency.Weekly
        "Monthly" -> Frequency.Monthly
        "Yearly" -> Frequency.Yearly
        else -> throw IllegalArgumentException("Invalid repeat type: $repeatType")
    }

    // Set interval
    if (options.interval > 1) {
        rule.interval = options.interval
    }

    // Handle type-specific rules
    when (repeatType) {
        "Weekly" -> {
            // Convert your day numbers (1=Mon, 7=Sun) to Weekday constants
            options.selectedDays.forEach { dayNumber ->
                val weekday = when (dayNumber) {
                    1 -> Weekday.Monday
                    2 -> Weekday.Tuesday
                    3 -> Weekday.Wednesday
                    4 -> Weekday.Thursday
                    5 -> Weekday.Friday
                    6 -> Weekday.Saturday
                    7 -> Weekday.Sunday
                    else -> throw IllegalArgumentException("Invalid day number: $dayNumber")
                }
                rule.byDay.add(WeekdayNum(0, weekday)) // 0 means "every occurrence"
            }
        }
        "Monthly" -> {
            if (options.monthlyType == "absolute") {
                rule.byMonthDay.add(options.absoluteDay)
            }
            // Add relative monthly logic later if needed
        }
        "Yearly" -> {
            rule.byMonth.add(options.month)
            rule.byMonthDay.add(options.yearlyDay)
        }
    }

    // Handle end conditions
    when (options.endType) {
        "never" -> {
            // No end date - rule continues indefinitely (default)
        }
        "on_date" -> {
            // Create UTC date string in format: yyyyMMdd'T'HHmmss'Z'
            val untilString = String.format(
                "%04d%02d%02dT000000Z",
                options.endDateYear,
                options.endDateMonth,
                options.endDateDay
            )
            rule.until = LocalDateTime.parse(untilString, dateFormatter).toInstant(ZoneOffset.UTC)
        }
        "after_occurrences" -> {
            rule.count = options.occurrences
        }
    }

    val fullRuleString = rule.toRFC5545String()

    // Remove "RRULE:" prefix if present
    return if (fullRuleString.startsWith("RRULE:")) {
        fullRuleString.substring(6) // Remove first 6 characters "RRULE:"
    } else {
        fullRuleString
    }
}

/*____________________________________________________________________________________________*/
/*____________________________________________________________________________________________*/
/*____________________________________________________________________________________________*/

/**
 * Parses an RRULE string back into RepeatOptions for UI editing
 */
fun parseRRuleToRepeatOptions(rRuleString: String, repeatType: String): RepeatOptions {
    val options = RepeatOptions()
    val rule = RRule(rRuleString)

    // Extract interval
    options.interval = rule.interval

    when (repeatType) {
        "Weekly" -> {
            // Extract days (MO, TU, WE, etc. -> 1, 2, 3, etc.)
            options.selectedDays = rule.byDay.map { weekdayNum ->
                when (weekdayNum.weekday) {
                    Weekday.Monday -> 1
                    Weekday.Tuesday -> 2
                    Weekday.Wednesday -> 3
                    Weekday.Thursday -> 4
                    Weekday.Friday -> 5
                    Weekday.Saturday -> 6
                    Weekday.Sunday -> 7
                    else -> 1 // fallback
                }
            }.toSet()
        }
        "Monthly" -> {
            if (rule.byMonthDay.isNotEmpty()) {
                options.monthlyType = "absolute"
                options.absoluteDay = rule.byMonthDay.first()
            }
            // Add relative monthly parsing if needed
        }
        "Yearly" -> {
            if (rule.byMonth.isNotEmpty()) {
                options.month = rule.byMonth.first()
            }
            if (rule.byMonthDay.isNotEmpty()) {
                options.yearlyDay = rule.byMonthDay.first()
            }
        }
    }

    // Parse end conditions
    when {
        rule.count > 0 -> {
            options.endType = "after_occurrences"
            options.occurrences = rule.count
        }
        rule.until != null -> {
            options.endType = "on_date"
            val calendar = Calendar.getInstance()
            calendar.time = Date.from(rule.until)
            options.endDateYear = calendar.get(Calendar.YEAR)
            options.endDateMonth = calendar.get(Calendar.MONTH) + 1
            options.endDateDay = calendar.get(Calendar.DAY_OF_MONTH)
        }
        else -> {
            options.endType = "never"
        }
    }

    return options
}