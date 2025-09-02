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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController

@Composable
fun EditEntry(
    navController: NavController,
    entryTableViewModel: EntryTableViewModel,
    editEntryViewModel: EditEntryViewModel
) {
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val selectedEntry = editEntryViewModel.selectedEntry
    var entryContent by remember { mutableStateOf(selectedEntry?.entryDB ?: "") }
    var selectedTimeMinutes by remember { mutableStateOf(selectedEntry?.timeMinutes) }

    // Use the reminder and repeat types from ViewModel
    var selectedReminderType by remember { mutableStateOf(editEntryViewModel.selectedReminderType) }
    var selectedRepeatType by remember { mutableStateOf(editEntryViewModel.selectedRepeatType) }
    var repeatOptions by remember { mutableStateOf(editEntryViewModel.repeatOptions) }

    // Update local state when ViewModel changes
    LaunchedEffect(editEntryViewModel.selectedReminderType) {
        selectedReminderType = editEntryViewModel.selectedReminderType
    }

    LaunchedEffect(editEntryViewModel.selectedRepeatType) {
        selectedRepeatType = editEntryViewModel.selectedRepeatType
    }

    LaunchedEffect(editEntryViewModel.repeatOptions) {
        repeatOptions = editEntryViewModel.repeatOptions
    }

    Column {
        OutlinedTextField(
            value = entryContent,
            onValueChange = {
                entryContent = it
                errorMessage = null
            },
            label = { Text("Edit Event") },
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

        // Reminder Selector
        ReminderSelector(
            selectedReminderType = selectedReminderType,
            onReminderTypeChange = { newType ->
                selectedReminderType = newType
                editEntryViewModel.selectedReminderType = newType
            }
        )

        // Repeat Selector with detailed options
        RepeatSelector(
            selectedRepeatType = selectedRepeatType,
            onRepeatTypeChange = { newType ->
                selectedRepeatType = newType
                editEntryViewModel.selectedRepeatType = newType
            },
            repeatOptions = repeatOptions,
            onRepeatOptionsChange = { newOptions ->
                repeatOptions = newOptions
                editEntryViewModel.repeatOptions = newOptions
            }
        )

        Button(
            onClick = {
                if (entryContent.isBlank()) {
                    errorMessage = "Event cannot be empty"
                    return@Button
                }

                selectedEntry?.let { entry ->
                    // Update the entry with new content and time
                    val updatedEntry = entry.copy(
                        entryDB = entryContent,
                        timeMinutes = selectedTimeMinutes
                    )

                    // Serialize repeat options if needed
                    val repeatDetails = if (selectedRepeatType != "Never") {
                        RepeatOptionsSerializer.serialize(repeatOptions, selectedRepeatType)
                    } else {
                        null
                    }

                    // Update the entry in database
                    entryTableViewModel.updateEntry(updatedEntry)

                    // Update extra data with repeat details
                    editEntryViewModel.updateEntry(
                        updatedEntry,
                        selectedReminderType != "None" || selectedRepeatType != "Never",
                        if (selectedReminderType != "None") selectedReminderType else null,
                        if (selectedRepeatType != "Never") selectedRepeatType else null,
                        repeatDetails // PASS REPEAT DETAILS
                    )

                    navController.popBackStack()
                }
            },
            modifier = Modifier.padding(16.dp)
        ) {
            Text("Save Changes")
        }
    }
}