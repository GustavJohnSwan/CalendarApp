package com.bignerdranch.android.calendarapp3.ui_composables.entry_view



import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.bignerdranch.android.calendarapp3.ui_composables.entry_view.entry_functions.InputTimePicker
import com.bignerdranch.android.calendarapp3.buisness_logic.CalendarViewModel
import com.bignerdranch.android.calendarapp3.buisness_logic.CouchbaseCalendarViewModel
import com.bignerdranch.android.calendarapp3.buisness_logic.EditEntryViewModel
import com.bignerdranch.android.calendarapp3.buisness_logic.NewEntryViewModel
import com.bignerdranch.android.calendarapp3.ui_composables.entry_view.entry_functions.ReminderSelector
import com.bignerdranch.android.calendarapp3.ui_composables.entry_view.entry_functions.repeat_function.repeat_underfunctions.RepeatOptions
import com.bignerdranch.android.calendarapp3.ui_composables.entry_view.entry_functions.repeat_function.RepeatSelector
import com.bignerdranch.android.calendarapp3.ui_composables.entry_view.entry_functions.repeat_function.repeatEventListener
import com.bignerdranch.android.calendarapp3.ui_composables.entry_view.entry_functions.repeat_function.rrule_generation.generateRRuleString
// import com.bignerdranch.android.calendarapp3.ui_composables.entry_view.entry_functions.repeat_function.repeat_underfunctions.repeatEventListener
import com.bignerdranch.android.calendarapp3.buisness_logic.objectbox.ObjectBoxNewEntryViewModel

@Composable
fun NewEntry(
    navController: NavController,
    viewModel: CalendarViewModel = viewModel(),
    newEntryViewModel: NewEntryViewModel,
    editEntryViewModel: EditEntryViewModel,
    couchbaseCalendarViewModel: CouchbaseCalendarViewModel,
    objectBoxNewEntryViewModel: ObjectBoxNewEntryViewModel,
    //source = source,
    source: String
)
 {
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var selectedTimeMinutes by remember { mutableStateOf<Int?>(null) }
    var selectedReminderType by remember { mutableStateOf("None") } // Default to "None"

    var selectedRepeatType by remember { mutableStateOf("Never") } // Default to "None"
    var repeatOptions by remember { mutableStateOf(RepeatOptions()) }

    val context = LocalContext.current

    // NEW: LaunchedEffect to load attachments (for this new entry, it will be empty initially)
    LaunchedEffect(Unit) {
        // We don't have an entryId yet for a new entry, so we can't load attachments here.
        // Attachments will be linked after the entry is created and we have an ID.
    }


    Column {
        OutlinedTextField(
            value = viewModel.newEventText,
            onValueChange = {
                viewModel.onEventTextBoxSelect(it)
                errorMessage = null
            },
            label = { Text("Enter Event") },
            isError = errorMessage != null,
            modifier = Modifier.padding(16.dp)
        )

        errorMessage?.let {
            Text(
                text = it,
                color = Color.Red,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        // Time Picker
        InputTimePicker(
            currentTimeMinutes = selectedTimeMinutes,
            onTimeSelected = { minutes -> selectedTimeMinutes = minutes }
        )

        // NEW: Reminder Selector (works like time picker)
        ReminderSelector(
            selectedReminderType = selectedReminderType,
            onReminderTypeChange = { newType -> selectedReminderType = newType }
        )


        RepeatSelector(
            selectedRepeatType = selectedRepeatType,
            onRepeatTypeChange = { newType -> selectedRepeatType = newType },
            repeatOptions = repeatOptions,
            onRepeatOptionsChange = { newOptions -> repeatOptions = newOptions }
        )

        if (source == "sqlite") {
        // NEW: Attachment Section
        Text("Attachments", modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.titleSmall)
        // For a new entry, we'll keep it simple and suggest adding attachments after saving.
        Text("Add attachments after saving the event.", modifier = Modifier.padding(horizontal = 16.dp))

        } else {
            Text(
                "Attachments are not supported for Couchbase entries yet.",
                modifier = Modifier.padding(16.dp)
            )
        }

        Button(
            onClick = {
                if (viewModel.newEventText.isBlank()) {
                    errorMessage = "Event cannot be empty"
                    return@Button
                }

                // Generate RRule string using the new generator
                val repeatDetails = if (selectedRepeatType != "Never") {
                    /*
                    val testRule = "FREQ=DAILY;COUNT=5"
                    Log.d("RepeatEvent", "Using TEST rule: $testRule")
                    testRule
                     */
                    generateRRuleString(repeatOptions, selectedRepeatType) // ← CHANGE THIS LINE
                } else {
                    null
                }

                // NEW: Show Toast with repeatDetails
                Toast.makeText(
                    context,
                    "Repeat Details: ${repeatDetails ?: "No repeat"}",
                    Toast.LENGTH_LONG
                ).show()


                // val nonNullableString: String = repeatDetails ?: "default value"



                /*
                if (source == "couchbase") {
                    couchbaseCalendarViewModel.createCalendarEntry(
                        date = editEntryViewModel.selectedDate,
                        content = viewModel.newEventText,
                        timeMinutes = selectedTimeMinutes,
                        hasExtraData = selectedReminderType != "None" || selectedRepeatType != "Never",
                        reminderType = if (selectedReminderType != "None") selectedReminderType else null,
                        repeat = if (selectedRepeatType != "Never") selectedRepeatType else null,
                        repeatDetails = repeatDetails,
                        onEntryCreated = {
                            navController.popBackStack()
                        }
                    )
                } else {
                    newEntryViewModel.insertEntry(
                        date = editEntryViewModel.selectedDate,
                        content = viewModel.newEventText,
                        exDaBo = selectedReminderType != "None" || selectedRepeatType != "Never",
                        timeMinutes = selectedTimeMinutes,
                        reminderType = if (selectedReminderType != "None") selectedReminderType else null,
                        repeat = if (selectedRepeatType != "Never") selectedRepeatType else null,
                        repeatDetails = repeatDetails,
                        onEntryInserted = { entryId ->
                            if (repeatDetails != null) {
                                repeatEventListener(
                                    entryId = entryId,
                                    repeatDetails = repeatDetails,
                                    startDate = editEntryViewModel.selectedDate,
                                    newEntryViewModel = newEntryViewModel
                                )
                            }
                            navController.popBackStack()
                        }
                    )
                }

                 */

                when (source) {
                    "couchbase" -> {
                        couchbaseCalendarViewModel.createCalendarEntry(
                            date = editEntryViewModel.selectedDate,
                            content = viewModel.newEventText,
                            timeMinutes = selectedTimeMinutes,
                            hasExtraData = selectedReminderType != "None" || selectedRepeatType != "Never",
                            reminderType = if (selectedReminderType != "None") selectedReminderType else null,
                            repeat = if (selectedRepeatType != "Never") selectedRepeatType else null,
                            repeatDetails = repeatDetails,
                            onEntryCreated = {
                                navController.popBackStack()
                            }
                        )
                    }

                    "objectbox" -> {
                        objectBoxNewEntryViewModel.insertEntry(
                            date = editEntryViewModel.selectedDate,
                            content = viewModel.newEventText,
                            exDaBo = selectedReminderType != "None" || selectedRepeatType != "Never",
                            timeMinutes = selectedTimeMinutes,
                            reminderType = if (selectedReminderType != "None") selectedReminderType else null,
                            repeat = if (selectedRepeatType != "Never") selectedRepeatType else null,
                            repeatDetails = repeatDetails,
                            onEntryInserted = { _ ->
                                // for now we skip repeatEventListener for ObjectBox until recurring is wired
                                navController.popBackStack()
                            }
                        )
                    }

                    else -> { // "sqlite"
                        newEntryViewModel.insertEntry(
                            date = editEntryViewModel.selectedDate,
                            content = viewModel.newEventText,
                            exDaBo = selectedReminderType != "None" || selectedRepeatType != "Never",
                            timeMinutes = selectedTimeMinutes,
                            reminderType = if (selectedReminderType != "None") selectedReminderType else null,
                            repeat = if (selectedRepeatType != "Never") selectedRepeatType else null,
                            repeatDetails = repeatDetails,
                            onEntryInserted = { entryId ->
                                // we will not perform recurring event edits, so this is removed
                                /*
                                if (repeatDetails != null) {
                                    repeatEventListener(
                                        entryId = entryId,
                                        repeatDetails = repeatDetails,
                                        startDate = editEntryViewModel.selectedDate,
                                        newEntryViewModel = newEntryViewModel
                                    )
                                }

                                 */
                                navController.popBackStack()
                            }
                        )
                    }
                }

            },
            modifier = Modifier.padding(16.dp)
        ) {
            Text("Save Event")
        }
    }
}





