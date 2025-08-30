package com.bignerdranch.android.calendarapp3

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TimeInput
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import java.util.Calendar

// Add this to your ReminderAndRepeater_V2.kt file
@Composable
fun ReminderSelector(
    selectedReminderType: String,
    onReminderTypeChange: (String) -> Unit
) {
    var showReminderDialog by remember { mutableStateOf(false) }

    // Display the current selection (show "None" if no reminder is selected)
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