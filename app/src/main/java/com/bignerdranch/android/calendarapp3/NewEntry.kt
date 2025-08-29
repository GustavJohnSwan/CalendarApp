package com.bignerdranch.android.calendarapp3


import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController

@Composable
fun NewEntry(
    navController: NavController,
    viewModel: CalendarViewModel = viewModel(),
    eventDetailViewModel: EventDetailsViewModel = viewModel(),
    entryTableViewModel: EntryTableViewModel,
    editEntryViewModel: EditEntryViewModel
) {
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isExtraDataEnabled by remember { mutableStateOf(false) }
    var extraDataBoolean by remember { mutableStateOf(false) } // Make this mutable
    var selectedTimeMinutes by remember { mutableStateOf<Int?>(null) } // ADD THIS
    var selectedReminderType by remember { mutableStateOf("None") } // ADD THIS for radio button selection

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

        // ADD TIME PICKER SECTION
        // ← REPLACE WITH NEW TIME PICKER
        InputTimePicker(
            currentTimeMinutes = selectedTimeMinutes,
            onTimeSelected = { minutes -> selectedTimeMinutes = minutes }
        )


        // KEEP YOUR EXISTING CHECKBOX BUT ADD REMINDER TYPE CAPABILITY
        /*
        CheckboxMinimalExample_V2(
            onCheckedChange = { checked ->
                extraDataBoolean = checked
            },
            isChecked = extraDataBoolean,
            selectedReminderType = selectedReminderType, // ADD THIS PARAMETER
            onReminderTypeChange = { newType -> selectedReminderType = newType } // ADD THIS CALLBACK
        )

         */


        // NEW: Reminder Selector (works like time picker)
        ReminderSelector(
            selectedReminderType = selectedReminderType,
            onReminderTypeChange = { newType -> selectedReminderType = newType }
        )



        Button(
            onClick = {
                if (viewModel.newEventText.isBlank()) {
                    errorMessage = "Event cannot be empty"
                    return@Button
                }

                entryTableViewModel.insertEntry(
                    date = editEntryViewModel.selectedDate,
                    content = viewModel.newEventText,
                    exDaBo = extraDataBoolean,
                    timeMinutes = selectedTimeMinutes,
                    reminderType = if (extraDataBoolean) selectedReminderType else null // ADD THIS
                )
                navController.popBackStack()
            },
            modifier = Modifier.padding(16.dp)
        ) {
            Text("Save Event")
        }

    }
}

