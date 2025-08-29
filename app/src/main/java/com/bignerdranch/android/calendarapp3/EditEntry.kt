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
    //var hasReminder by remember { mutableStateOf(editEntryViewModel.hasReminder) } // ADD THIS
    var selectedReminderType by remember { mutableStateOf(editEntryViewModel.selectedReminderType) } // ADD THIS

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

        // ADD TIME PICKER
        selectedEntry?.let { entry ->
            InputTimePicker(
                currentTimeMinutes = entry.timeMinutes,
                onTimeSelected = { newTimeMinutes ->
                    // Update the entry with new time
                    entryTableViewModel.updateEntry(
                        entry.copy(timeMinutes = newTimeMinutes)
                    )
                }
            )
        }

        // NEW: Reminder Selector (works like time picker)
        ReminderSelector(
            selectedReminderType = selectedReminderType,
            onReminderTypeChange = { newType -> selectedReminderType = newType }
        )

        errorMessage?.let {
            Text(
                text = it,
                color = Color.Red,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        Button(
            onClick = {
                if (entryContent.isBlank()) {
                    errorMessage = "Event cannot be empty"
                    return@Button
                }

                selectedEntry?.let { entry ->
                    // UPDATE to include reminder and time
                    val updatedEntry = entry.copy(entryDB = entryContent)
                    entryTableViewModel.updateEntry(updatedEntry)
                    //editEntryViewModel.updateEntry(updatedEntry, hasReminder, selectedReminderType) // UPDATE THIS
                    navController.popBackStack()
                }
            },
            modifier = Modifier.padding(16.dp)
        ) {
            Text("Save Changes")
        }
    }
}

// ADD THIS COMPOSABLE FUNCTION (copy from your NewEntry.kt)
@Composable
fun RadioButtonSingleSelection(
    selectedOption: String,
    onOptionSelected: (String) -> Unit
) {
    val radioOptions = listOf("At time of event", "10 mins before", "1 hour before", "1 day before")

    Column(Modifier.selectableGroup()) {
        radioOptions.forEach { text ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .selectable(
                        selected = (text == selectedOption),
                        onClick = { onOptionSelected(text) },
                        role = Role.RadioButton
                    )
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = (text == selectedOption),
                    onClick = null
                )
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(start = 16.dp)
                )
            }
        }
    }
}