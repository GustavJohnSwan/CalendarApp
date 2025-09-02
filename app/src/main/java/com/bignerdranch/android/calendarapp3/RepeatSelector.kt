package com.bignerdranch.android.calendarapp3

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

@Composable
fun RepeatSelector(
    selectedRepeatType: String,
    onRepeatTypeChange: (String) -> Unit,
    repeatOptions: RepeatOptions = RepeatOptions(),
    onRepeatOptionsChange: (RepeatOptions) -> Unit = {}
) {
    var showRepeaterDialog by remember { mutableStateOf(false) }

    // Display the current selection
    val displayText = if (selectedRepeatType == "Never") {
        "Repeat: Never"
    } else {
        "Repeat: $selectedRepeatType"
    }

    Column {
        Text(
            text = displayText,
            modifier = Modifier
                .clickable { showRepeaterDialog = true }
                .padding(8.dp)
        )

        // Show detailed options if a repeat type is selected (other than Never)
        if (selectedRepeatType != "Never") {
            RepeatOptionsDetail(
                repeatType = selectedRepeatType,
                options = repeatOptions,
                onOptionsChange = onRepeatOptionsChange,
                modifier = Modifier.padding(start = 16.dp, top = 8.dp)
            )
        }
    }

    if (showRepeaterDialog) {
        RepeatSelectionDialog(
            selectedOption = selectedRepeatType,
            onOptionSelected = { newType ->
                onRepeatTypeChange(newType)
                // Don't close dialog when a type is selected
            },
            onDismissRequest = { showRepeaterDialog = false },
            repeatOptions = repeatOptions,
            onRepeatOptionsChange = onRepeatOptionsChange
        )
    }
}

@Composable
fun RepeatSelectionDialog(
    selectedOption: String,
    onOptionSelected: (String) -> Unit,
    onDismissRequest: () -> Unit,
    repeatOptions: RepeatOptions,
    onRepeatOptionsChange: (RepeatOptions) -> Unit
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.8f), // Use percentage of screen height instead of fixed height
            shape = RoundedCornerShape(16.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Select Repeat Type",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Make the content scrollable
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    RadioButtonRepeatType(
                        selectedOption = selectedOption,
                        onOptionSelected = onOptionSelected
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Show detailed options when a repeat type is selected
                    if (selectedOption != "Never") {
                        Divider(modifier = Modifier.padding(vertical = 8.dp))
                        Text(
                            text = "Repeat Options:",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        RepeatOptionsDetail(
                            repeatType = selectedOption,
                            options = repeatOptions,
                            onOptionsChange = onRepeatOptionsChange
                        )
                    }
                }

                Button(
                    onClick = onDismissRequest,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Done")
                }
            }
        }
    }
}