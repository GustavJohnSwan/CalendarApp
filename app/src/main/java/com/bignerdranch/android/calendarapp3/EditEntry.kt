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
fun EditEntry(
    navController: NavController,
    entryTableViewModel: EntryTableViewModel,
    editEntryViewModel: EditEntryViewModel
) {
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val selectedEntry = editEntryViewModel.selectedEntry
    var entryContent by remember { mutableStateOf(selectedEntry?.entryDB ?: "") }
    var selectedTimeMinutes by remember { mutableStateOf(selectedEntry?.timeMinutes) }

    // Use the reminder type from ViewModel (loaded when entry was selected)
    var selectedReminderType by remember { mutableStateOf(editEntryViewModel.selectedReminderType) }

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

        // NEW: Use the same ReminderSelector as NewEntry
        ReminderSelector(
            selectedReminderType = selectedReminderType,
            onReminderTypeChange = { newType -> selectedReminderType = newType }
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

                    // Update the entry in database
                    entryTableViewModel.updateEntry(updatedEntry)

                    // Update reminder data (pass whether reminder is enabled and the type)
                    editEntryViewModel.updateEntry(
                        updatedEntry,
                        selectedReminderType != "None", // hasReminder
                        if (selectedReminderType != "None") selectedReminderType else null // reminderType
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