// RepeatEventListener.kt - with proper RFC 5545 date handling
package com.bignerdranch.android.calendarapp3.entry_extra_data

import org.dmfs.rfc5545.DateTime
import org.dmfs.rfc5545.RecurrenceSet
import org.dmfs.rfc5545.recurrenceset.OfRule
import org.dmfs.jems2.iterator.First
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Listens for repeat events and generates recurring event dates
 */
fun repeatEventListener(
    entryId: Int,
    repeatDetails: String,
    startDate: String // Your date in "yyyy-MM-dd" format
) {
    if (repeatDetails.isBlank()) return

    CoroutineScope(Dispatchers.IO).launch {
        try {
            // 1. Convert your "yyyy-MM-dd" to RFC 5545 format for the library
            val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val startDateObj = inputFormat.parse(startDate)

            // Extract year, month, day for RFC 5545 format
            val calendar = Calendar.getInstance().apply { time = startDateObj }
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH) + 1 // January = 1 in RFC 5545
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            // 2. Create DateTime in RFC 5545 format (20150213T000000)
            val startDateTime = DateTime(year, month, day)

            // 3. Use OfRule with the properly formatted DateTime
            val recurrenceSet: RecurrenceSet = OfRule(repeatDetails, startDateTime)

            // 4. Generate occurrences (limit to 365 for safety)
            val limitedOccurrences = First(365, recurrenceSet.iterator())

            // 5. Process each occurrence - convert back to your preferred format
            val outputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

            while (limitedOccurrences.hasNext()) {
                val occurrenceDateTime = limitedOccurrences.next()

                // The occurrenceDateTime is already in RFC 5545 format, but we need to extract date parts
                val occurrenceYear = occurrenceDateTime.year
                val occurrenceMonth = occurrenceDateTime.month // January = 1
                val occurrenceDay = occurrenceDateTime.dayOfMonth

                // Create a Calendar instance to format it as "yyyy-MM-dd"
                val occurrenceCalendar = Calendar.getInstance().apply {
                    set(occurrenceYear, occurrenceMonth - 1, occurrenceDay) // Convert back to Java Calendar (January = 0)
                }

                val formattedDate = outputFormat.format(occurrenceCalendar.time)

                // 6. Save to your database
                saveRecurringEventToDatabase(entryId, formattedDate)
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

// Even simpler version if you just want to work with the library's native format:
fun repeatEventListenerSimple(
    entryId: Int,
    repeatDetails: String,
    startDate: String // Your date in "yyyy-MM-dd" format
) {
    if (repeatDetails.isBlank()) return

    CoroutineScope(Dispatchers.IO).launch {
        try {
            // Convert your date to RFC 5545 components
            val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val startDateObj = inputFormat.parse(startDate)
            val calendar = Calendar.getInstance().apply { time = startDateObj }

            val startDateTime = DateTime(
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH) + 1, // RFC 5545: January = 1
                calendar.get(Calendar.DAY_OF_MONTH)
            )

            // Generate occurrences
            val recurrenceSet: RecurrenceSet = OfRule(repeatDetails, startDateTime)
            val limitedOccurrences = First(365, recurrenceSet.iterator())

            while (limitedOccurrences.hasNext()) {
                val occurrence = limitedOccurrences.next()

                // occurrence is already a DateTime object with year, month, day properties
                val formattedDate = String.format(
                    "%04d-%02d-%02d",
                    occurrence.year,
                    occurrence.month, // Already 1-12
                    occurrence.dayOfMonth
                )

                saveRecurringEventToDatabase(entryId, formattedDate)
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

// Helper function to see what the library is doing
fun debugRRuleGeneration(rruleString: String, startDate: String) {
    val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    val startDateObj = inputFormat.parse(startDate)
    val calendar = Calendar.getInstance().apply { time = startDateObj }

    val startDateTime = DateTime(
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH) + 1,
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    println("Start date in RFC 5545 format: $startDateTime")
    println("RRule: $rruleString")

    val recurrenceSet: RecurrenceSet = OfRule(rruleString, startDateTime)
    val occurrences = First(5, recurrenceSet.iterator())

    println("First 5 occurrences:")
    while (occurrences.hasNext()) {
        val occurrence = occurrences.next()
        println(" - $occurrence") // This will show dates like "20150213T000000"
    }
}


// Add this to RepeatEventListener.kt or create a new database utility file
suspend fun saveRecurringEventToDatabase(entryId: Int, date: String) {
    // You'll need access to your database here
    // This should be in your ViewModel or a Repository class

    // For now, just log to see if it's working
    println("Would save: entryId=$entryId, date=$date")

    // Later, implement the actual database insertion:
    // val recurringEvent = RecurringEvent(entryId = entryId, occurrenceDate = date)
    // recurringEventDao.insert(recurringEvent)
}