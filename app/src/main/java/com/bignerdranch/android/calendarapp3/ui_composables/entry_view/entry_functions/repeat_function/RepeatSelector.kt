package com.bignerdranch.android.calendarapp3.ui_composables.entry_view.entry_functions.repeat_function

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.bignerdranch.android.calendarapp3.ui_composables.entry_view.entry_functions.repeat_function.repeat_underfunctions.RepeatDialogState
import com.bignerdranch.android.calendarapp3.ui_composables.entry_view.entry_functions.repeat_function.repeat_underfunctions.RepeatOptions
import com.bignerdranch.android.calendarapp3.ui_composables.entry_view.entry_functions.repeat_function.repeat_underfunctions.RepeatOptionsDetail
import com.bignerdranch.android.calendarapp3.ui_composables.entry_view.entry_functions.repeat_function.repeat_underfunctions.rememberRepeatDialogState
import com.bignerdranch.android.calendarapp3.ui_composables.entry_view.entry_functions.repeat_function.repeat_underfunctions.generateRepeatDisplayText

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


@Composable
fun RadioButtonRepeatType(
    modifier: Modifier = Modifier,
    selectedOption: String,
    onOptionSelected: (String) -> Unit
) {
    val radioOptions = listOf("Never", "Daily", "Weekly", "Monthly", "Yearly")
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
                    onClick = null // null recommended for accessibility with screen readers
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