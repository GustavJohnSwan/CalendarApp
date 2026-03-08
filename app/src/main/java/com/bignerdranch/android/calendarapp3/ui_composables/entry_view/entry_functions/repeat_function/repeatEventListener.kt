package com.bignerdranch.android.calendarapp3.ui_composables.entry_view.entry_functions.repeat_function

// RepeatEventListener.kt - with proper RFC 5545 date handling

import android.util.Log
import com.bignerdranch.android.calendarapp3.buisness_logic.NewEntryViewModel
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
    newEntryViewModel: NewEntryViewModel // ADD THIS PARAMETER
) {
    if (repeatDetails.isBlank()) return

    CoroutineScope(Dispatchers.IO).launch {
        var occurrenceCount = 0
        val occurrenceDates = mutableListOf<String>()

        try {
            Log.d("RepeatEvent", "=== STARTING RECURRENCE GENERATION ===")
            Log.d("RepeatEvent", "Entry ID: $entryId")
            Log.d("RepeatEvent", "Start Date: $startDate")
            Log.d("RepeatEvent", "RRule String: $repeatDetails")

            // 1. Parse your RRule string into RecurrenceRule object
            Log.d("RepeatEvent", "Attempting to parse RRule...")
            val recurrenceRule = RecurrenceRule(repeatDetails)
            Log.d("RepeatEvent", "RRule parsed successfully: $recurrenceRule")

            // 2. Convert your start date to DateTime
            val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val startDateObj = inputFormat.parse(startDate)
            val calendar = Calendar.getInstance().apply { time = startDateObj }

            val startDateTime = DateTime(
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH) + 1,
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            Log.d("RepeatEvent", "Start DateTime: $startDateTime")

            // 3. Use OfRule with the RecurrenceRule object
            Log.d("RepeatEvent", "Creating recurrence set...")
            val recurrenceSet = OfRule(recurrenceRule, startDateTime)
            val limitedOccurrences = First(365, recurrenceSet.iterator())
            Log.d("RepeatEvent", "Recurrence set created, starting iteration...")

            // 4. Process occurrences and save to database
            while (limitedOccurrences.hasNext()) {
                val occurrence = limitedOccurrences.next()
                val formattedDate = String.format("%04d-%02d-%02d", occurrence.year, occurrence.month, occurrence.dayOfMonth)

                occurrenceCount++
                occurrenceDates.add(formattedDate)
                Log.d("RepeatEvent", "Occurrence #$occurrenceCount: $formattedDate")

                // Use the ViewModel to save to database
                //newEntryViewModel.saveRecurringEventToDatabase(entryId, formattedDate)
            }

        } catch (e: Exception) {
            Log.e("RepeatEvent", "=== ERROR GENERATING RECURRENCES ===")
            Log.e("RepeatEvent", "Error type: ${e.javaClass.simpleName}")
            Log.e("RepeatEvent", "Error message: ${e.message}")
            Log.e("RepeatEvent", "Stack trace:", e)
        }

// MOVE THE FINAL LOGS HERE - OUTSIDE THE TRY-CATCH BLOCK
        if (occurrenceCount == 0) {
            Log.w("RepeatEvent", "No occurrences generated!")
        } else {
            Log.d("RepeatEvent", "=== COMPLETED: Generated $occurrenceCount occurrences ===")
            Log.d("RepeatEvent", "All dates: ${occurrenceDates.joinToString(", ")}")
        }
    }
}

// REMOVE the standalone saveRecurringEventToDatabase function - it's now in ViewModel