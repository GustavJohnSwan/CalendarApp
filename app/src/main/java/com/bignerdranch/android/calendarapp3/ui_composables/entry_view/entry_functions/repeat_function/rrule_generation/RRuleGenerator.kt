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

// Converts RepeatOptions into a standard RRule string.

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
        "Monthly" -> {
            if (options.monthlyType == "absolute") {
                rule.byMonthDay.add(options.absoluteDay)
            } else {
                val weekNumber = when (options.relativeWeek) {
                    "first" -> 1
                    "second" -> 2
                    "third" -> 3
                    "fourth" -> 4
                    "last" -> -1
                    else -> throw IllegalArgumentException(
                        "Invalid relative week: ${options.relativeWeek}"
                    )
                }

                val weekday = when (options.relativeDay) {
                    "monday" -> Weekday.Monday
                    "tuesday" -> Weekday.Tuesday
                    "wednesday" -> Weekday.Wednesday
                    "thursday" -> Weekday.Thursday
                    "friday" -> Weekday.Friday
                    "saturday" -> Weekday.Saturday
                    "sunday" -> Weekday.Sunday
                    else -> throw IllegalArgumentException(
                        "Invalid relative day: ${options.relativeDay}"
                    )
                }

                rule.byDay.add(WeekdayNum(weekNumber, weekday))
            }
        }
        "Yearly" -> {
            rule.byMonth.add(options.month)
            rule.byMonthDay.add(options.yearlyDay)
        }
    }

    // Handle end conditions
    when (options.endType) {
        "never" -> {
            // No end date - rule continues indefinitely
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

    // Remove "RRULE:" prefix
    return if (fullRuleString.startsWith("RRULE:")) {
        fullRuleString.substring(6) // Remove first 6 characters "RRULE:"
    } else {
        fullRuleString
    }
}

/*____________________________________________________________________________________________*/
/*____________________________________________________________________________________________*/
/*____________________________________________________________________________________________*/

// Parses an RRULE string back into RepeatOptions for UI editing

fun parseRRuleToRepeatOptions(rRuleString: String, repeatType: String): RepeatOptions {
    val options = RepeatOptions()
    val rule = RRule(rRuleString)

    // Extract interval
    options.interval = rule.interval

    when (repeatType) {
        "Monthly" -> {
            if (rule.byMonthDay.isNotEmpty()) {
                options.monthlyType = "absolute"
                options.absoluteDay = rule.byMonthDay.first()
            } else if (rule.byDay.isNotEmpty()) {
                val weekdayNum = rule.byDay.first()

                options.monthlyType = "relative"

                options.relativeWeek = when (weekdayNum.number) {
                    1 -> "first"
                    2 -> "second"
                    3 -> "third"
                    4 -> "fourth"
                    -1 -> "last"
                    else -> "first"
                }

                options.relativeDay = when (weekdayNum.weekday) {
                    Weekday.Monday -> "monday"
                    Weekday.Tuesday -> "tuesday"
                    Weekday.Wednesday -> "wednesday"
                    Weekday.Thursday -> "thursday"
                    Weekday.Friday -> "friday"
                    Weekday.Saturday -> "saturday"
                    Weekday.Sunday -> "sunday"
                }
            }
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