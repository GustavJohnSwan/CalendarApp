package com.bignerdranch.android.calendarapp3.ui_composables.entry_view.entry_functions.repeat_function

// RepeatEventListener.kt - with proper RFC 5545 date handling

import android.util.Log
import com.bignerdranch.android.calendarapp3.buisness_logic.EntryTableViewModel
import org.dmfs.rfc5545.DateTime
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
// In RepeatEventListener.kt
import org.dmfs.rfc5545.recur.RecurrenceRule

// In RepeatEventListener.kt - update the function
fun repeatEventListener(
    entryId: Int,
    repeatDetails: String,
    startDate: String,
    entryTableViewModel: EntryTableViewModel // ADD THIS PARAMETER
) {
    if (repeatDetails.isBlank()) return

    CoroutineScope(Dispatchers.IO).launch {
        try {
            // 1. Parse your RRule string into RecurrenceRule object
            val recurrenceRule = RecurrenceRule(repeatDetails)

            // 2. Convert your start date to DateTime
            val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val startDateObj = inputFormat.parse(startDate)
            val calendar = Calendar.getInstance().apply { time = startDateObj }

            val startDateTime = DateTime(
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH) + 1,
                calendar.get(Calendar.DAY_OF_MONTH)
            )

            // 3. Use OfRule with the RecurrenceRule object
            val recurrenceSet = OfRule(recurrenceRule, startDateTime)
            val limitedOccurrences = First(365, recurrenceSet.iterator())

            // 4. Process occurrences and save to database
            while (limitedOccurrences.hasNext()) {
                val occurrence = limitedOccurrences.next()
                val formattedDate = String.format("%04d-%02d-%02d", occurrence.year, occurrence.month, occurrence.dayOfMonth)

                // Use the ViewModel to save to database
                entryTableViewModel.saveRecurringEventToDatabase(entryId, formattedDate)
            }

// In RepeatEventListener.kt - enhance the catch block
        } catch (e: Exception) {
            e.printStackTrace()
            // Add Log.d or Log.e for better debugging
            Log.e("RepeatEvent", "Error generating recurring events: ${e.message}")
        }
    }
}

// REMOVE the standalone saveRecurringEventToDatabase function - it's now in ViewModel