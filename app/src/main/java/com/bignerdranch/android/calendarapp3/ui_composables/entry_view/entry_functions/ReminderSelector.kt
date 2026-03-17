package com.bignerdranch.android.calendarapp3.ui_composables.entry_view.entry_functions

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog



@Composable
fun ReminderSelector(
    selectedReminderType: String,
    onReminderTypeChange: (String) -> Unit
) {
    var showReminderDialog by remember { mutableStateOf(false) }

    // Display the current selection
    val displayText = if (selectedReminderType == "None") {
        "Reminder: None"
    } else {
        "Reminder: $selectedReminderType"
    }

    Text(
        text = displayText,
        modifier = Modifier
            .clickable { showReminderDialog = true }
            .padding(8.dp)
    )

    if (showReminderDialog) {
        ReminderSelectionDialog(
            selectedOption = selectedReminderType,
            onOptionSelected = { newType ->
                onReminderTypeChange(newType)
                showReminderDialog = false
            },
            onDismissRequest = { showReminderDialog = false }
        )
    }
}

@Composable
fun ReminderSelectionDialog(
    selectedOption: String,
    onOptionSelected: (String) -> Unit,
    onDismissRequest: () -> Unit
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp),
            shape = RoundedCornerShape(16.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Select Reminder",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                RadioButtonSingleSelection(
                    selectedOption = selectedOption,
                    onOptionSelected = onOptionSelected
                )
            }
        }
    }
}


@Composable
fun RadioButtonSingleSelection(
    modifier: Modifier = Modifier,
    selectedOption: String,
    onOptionSelected: (String) -> Unit
) {
    val radioOptions = listOf("None", "At time of event", "10 mins before", "1 hour before", "1 day before")

    Column(modifier.selectableGroup()) {
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