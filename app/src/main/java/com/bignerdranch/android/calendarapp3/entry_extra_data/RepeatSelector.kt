package com.bignerdranch.android.calendarapp3.entry_extra_data

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
import androidx.compose.ui.window.DialogProperties
import com.bignerdranch.android.calendarapp3.RadioButtonRepeatType

@Composable
fun RepeatSelector(
    selectedRepeatType: String,
    onRepeatTypeChange: (String) -> Unit,
    repeatOptions: RepeatOptions = RepeatOptions(),
    onRepeatOptionsChange: (RepeatOptions) -> Unit = {}
) {
    val dialogState = rememberRepeatDialogState()

    // Generate display text based on selected options (single line)
    val displayText = generateRepeatDisplayText(selectedRepeatType, repeatOptions)

    Text(
        text = displayText,
        modifier = Modifier
            .clickable { dialogState.openDialog() }
            .padding(8.dp)
    )

    if (dialogState.isDialogOpen) {
        RepeatSelectionDialog(
            selectedOption = selectedRepeatType,
            onOptionSelected = { newType ->
                onRepeatTypeChange(newType)
            },
            onDismissRequest = { dialogState.closeDialog() },
            repeatOptions = repeatOptions,
            onRepeatOptionsChange = onRepeatOptionsChange,
            dialogState = dialogState
        )
    }
}

// Function to generate display text for the repeat selector (single line version)
// Function to generate display text for the repeat selector
fun generateRepeatDisplayText(repeatType: String, options: RepeatOptions): String {
    val baseText = when (repeatType) {
        "Never" -> "Repeat: Never"
        "Daily" -> "Repeat: Daily (every ${options.interval} day${if (options.interval > 1) "s" else ""})"
        "Weekly" -> {
            val daysText = if (options.selectedDays.isNotEmpty()) {
                val dayNames = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
                options.selectedDays.sorted().joinToString(", ") { dayIndex ->
                    dayNames.getOrElse(dayIndex - 1) { "Day $dayIndex" }
                }
            } else {
                "No days selected"
            }
            "Repeat: Weekly (every ${options.interval} week${if (options.interval > 1) "s" else ""} on $daysText)"
        }
        "Monthly" -> {
            val monthlyText = if (options.monthlyType == "absolute") {
                "on day ${options.absoluteDay}"
            } else {
                "${options.relativeWeek} ${options.relativeDay}"
            }
            "Repeat: Monthly (every ${options.interval} month${if (options.interval > 1) "s" else ""} $monthlyText)"
        }
        "Yearly" -> "Repeat: Yearly (every year on ${getMonthName(options.month)} ${options.yearlyDay})"
        else -> "Repeat: $repeatType"
    }

    // Add end option information
    val endText = when (options.endType) {
        "never" -> ""
        "on_date" -> " • Ends on ${options.endDateDay}/${options.endDateMonth}/${options.endDateYear}"
        "after_occurrences" -> " • Ends after ${options.occurrences} occurrence${if (options.occurrences > 1) "s" else ""}"
        else -> ""
    }

    return baseText + endText
}

// Helper function to get month name
fun getMonthName(month: Int): String {
    val months = listOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    )
    return months.getOrElse(month - 1) { "Month $month" }
}

@Composable
fun RepeatSelectionDialog(
    selectedOption: String,
    onOptionSelected: (String) -> Unit,
    onDismissRequest: () -> Unit,
    repeatOptions: RepeatOptions,
    onRepeatOptionsChange: (RepeatOptions) -> Unit,
    dialogState: RepeatDialogState
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false) // Allow custom sizing
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f) // Use percentage instead of fixed values
                .fillMaxHeight(0.8f),
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

                val scrollState = rememberScrollState()

                // Save scroll position when it changes
                LaunchedEffect(scrollState.value) {
                    dialogState.updateScrollPosition(scrollState.value.toFloat())
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(scrollState)
                ) {
                    RadioButtonRepeatType(
                        selectedOption = selectedOption,
                        onOptionSelected = onOptionSelected
                    )

                    Spacer(modifier = Modifier.height(16.dp))

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