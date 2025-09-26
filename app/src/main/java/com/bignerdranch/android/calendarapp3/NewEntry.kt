package com.bignerdranch.android.calendarapp3


import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.bignerdranch.android.calendarapp3.buisness_logic.AttachmentViewModel
import com.bignerdranch.android.calendarapp3.buisness_logic.CalendarViewModel
import com.bignerdranch.android.calendarapp3.buisness_logic.EditEntryViewModel
import com.bignerdranch.android.calendarapp3.buisness_logic.EntryTableViewModel
import com.bignerdranch.android.calendarapp3.buisness_logic.EventDetailsViewModel
import com.bignerdranch.android.calendarapp3.entry_extra_data.ReminderSelector
import com.bignerdranch.android.calendarapp3.entry_extra_data.RepeatOptions
import com.bignerdranch.android.calendarapp3.entry_extra_data.RepeatOptionsSerializer
import com.bignerdranch.android.calendarapp3.entry_extra_data.RepeatSelector
import com.bignerdranch.android.calendarapp3.entry_extra_data.generateRRuleString
import com.bignerdranch.android.calendarapp3.entry_extra_data.repeatEventListener

@Composable
fun NewEntry(
    navController: NavController,
    viewModel: CalendarViewModel = viewModel(),
    eventDetailViewModel: EventDetailsViewModel = viewModel(),
    entryTableViewModel: EntryTableViewModel,
    editEntryViewModel: EditEntryViewModel,
    attachmentViewModel: AttachmentViewModel = viewModel() // <- ADD THIS
) {
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isExtraDataEnabled by remember { mutableStateOf(false) }
    var extraDataBoolean by remember { mutableStateOf(false) }
    var selectedTimeMinutes by remember { mutableStateOf<Int?>(null) }
    var selectedReminderType by remember { mutableStateOf("None") } // Default to "None"

    var selectedRepeatType by remember { mutableStateOf("Never") } // Default to "None"
    var repeatOptions by remember { mutableStateOf(RepeatOptions()) }

    // NEW: LaunchedEffect to load attachments (for this new entry, it will be empty initially)
    LaunchedEffect(Unit) {
        // We don't have an entryId yet for a new entry, so we can't load attachments here.
        // Attachments will be linked after the entry is created and we have an ID.
    }

    // NEW: File Picker Launcher
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { selectedUri ->
            // We will handle this after the entry is saved, as we need an entryId
            // For now, we can store the URI(s) in a temporary list
            // This is a more advanced flow. For simplicity, we might add attachments in the Edit screen after creation.
            // Let's focus on implementing this first in EditEntry.
        }
    }

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

        // Time Picker
        InputTimePicker(
            currentTimeMinutes = selectedTimeMinutes,
            onTimeSelected = { minutes -> selectedTimeMinutes = minutes }
        )

        // NEW: Reminder Selector (works like time picker)
        ReminderSelector(
            selectedReminderType = selectedReminderType,
            onReminderTypeChange = { newType -> selectedReminderType = newType }
        )


        RepeatSelector(
            selectedRepeatType = selectedRepeatType,
            onRepeatTypeChange = { newType -> selectedRepeatType = newType },
            repeatOptions = repeatOptions,
            onRepeatOptionsChange = { newOptions -> repeatOptions = newOptions }
        )

        // NEW: Attachment Section
        Text("Attachments", modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.titleSmall)
        // For a new entry, we'll keep it simple and suggest adding attachments after saving.
        Text("Add attachments after saving the event.", modifier = Modifier.padding(horizontal = 16.dp))

        Button(
            onClick = {
                if (viewModel.newEventText.isBlank()) {
                    errorMessage = "Event cannot be empty"
                    return@Button
                }

                // Generate RRule string using the new generator
                val repeatDetails = if (selectedRepeatType != "Never") {
                    generateRRuleString(repeatOptions, selectedRepeatType) // ← CHANGE THIS LINE
                } else {
                    null
                }

                entryTableViewModel.insertEntry(
                    date = editEntryViewModel.selectedDate,
                    content = viewModel.newEventText,
                    exDaBo = selectedReminderType != "None", // Enable extra data only if reminder is not "None"
                    timeMinutes = selectedTimeMinutes,
                    reminderType = if (selectedReminderType != "None") selectedReminderType else null,
                    repeat = if (selectedRepeatType != "Never") selectedRepeatType else null,
                    repeatDetails = repeatDetails, // Store the RRule string
                    onEntryInserted = { entryId ->  // ADD THIS CALLBACK
                        // This gets called AFTER the entry is inserted and we have the ID
                        if (repeatDetails != null) {
                            repeatEventListener(entryId, repeatDetails)
                        }
                        navController.popBackStack()
                    }
                )

                //repeatEventListener(entryId, repeatDetails)


            },
            modifier = Modifier.padding(16.dp)
        ) {
            Text("Save Event")
        }
    }
}


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