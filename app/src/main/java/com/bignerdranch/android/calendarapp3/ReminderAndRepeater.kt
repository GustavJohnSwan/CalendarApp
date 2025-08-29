package com.bignerdranch.android.calendarapp3

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp


@Composable
fun CheckboxMinimalExample(
    onCheckedChange: (Boolean) -> Unit,
    isChecked: Boolean,
    selectedReminderType: String, // ADD THIS PARAMETER
    onReminderTypeChange: (String) -> Unit // ADD THIS CALLBACK
) {


    Row(verticalAlignment = Alignment.CenterVertically) {
        Checkbox(
            checked = isChecked, // USE THE PASSED PARAMETER
            onCheckedChange = { onCheckedChange(it) } // CALL THE CALLBACK DIRECTLY
        )
        Text(if (isChecked) "Reminder ON" else "Reminder OFF") // USE THE PARAMETER
    }

    Row(verticalAlignment = Alignment.CenterVertically) {
        if (isChecked) { // USE THE PARAMETER
            RadioButtonSingleSelection(
                selectedOption = selectedReminderType,
                onOptionSelected = onReminderTypeChange
            )
        }
    }
}



@Composable
fun RadioButtonSingleSelection(
    modifier: Modifier = Modifier,
    selectedOption: String, // ADD THIS PARAMETER
    onOptionSelected: (String) -> Unit // ADD THIS CALLBACK
) {
    val radioOptions = listOf("At time of event", "10 mins before", "1 hour before", "1 day before")
    //val (selectedOption, onOptionSelected) = remember { mutableStateOf(radioOptions[0]) }
    // Note that Modifier.selectableGroup() is essential to ensure correct accessibility behavior
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





@Composable
fun CheckboxRepeatEvent(
    onCheckedChange: (Boolean) -> Unit,
    isChecked: Boolean,
    selectedRepeatType: String,
    onRepeatTypeChange: (String) -> Unit
) {

    Row(verticalAlignment = Alignment.CenterVertically) {
        Checkbox(
            checked = isChecked,
            onCheckedChange = { onCheckedChange(it) }
        )
        Text(if (isChecked) "Repeat YES" else "Repeat NO")
    }

    Row(verticalAlignment = Alignment.CenterVertically) {
        if (isChecked) {
            RadioButtonRepeatType(
                selectedOption = selectedRepeatType,
                onOptionSelected = onRepeatTypeChange
            )
        }
    }
}

@Composable
fun RadioButtonRepeatType(
    modifier: Modifier = Modifier,
    selectedOption: String,
    onOptionSelected: (String) -> Unit
) {
    val radioOptions = listOf("Daily", "Weekly", "Monthly", "Yearly")
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
            RepeatTypeDetails(repeatType = selectedOption)
        }
    }
}


@Composable
fun RepeatTypeDetails(
    repeatType: String
) {
    when (repeatType) {
        "Daily" ->
            Row() {

            }
        "Weekly" ->
            Row() {

            }
        "Monthly" ->
            Row() {

            }
        "Yearly" ->
            Row() {

            }
        else ->
            Row() {

            }
    }
}