package com.bignerdranch.android.calendarapp3.ui_composables.entry_view

import android.R
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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

import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.bignerdranch.android.calendarapp3.buisness_logic.AttachmentViewModel
import com.bignerdranch.android.calendarapp3.buisness_logic.EditEntryViewModel
import com.bignerdranch.android.calendarapp3.buisness_logic.NewEntryViewModel
import com.bignerdranch.android.calendarapp3.database.EntryAttachment
import com.bignerdranch.android.calendarapp3.ui_composables.entry_view.entry_functions.ReminderSelector
import com.bignerdranch.android.calendarapp3.ui_composables.entry_view.entry_functions.repeat_function.RepeatSelector

import androidx.compose.ui.res.painterResource
import com.bignerdranch.android.calendarapp3.ui_composables.entry_view.entry_functions.InputTimePicker
import com.bignerdranch.android.calendarapp3.ui_composables.entry_view.entry_functions.repeat_function.rrule_generation.generateRRuleString


@Composable
fun EditEntry(
    navController: NavController,
    editEntryViewModel: EditEntryViewModel,
    attachmentViewModel: AttachmentViewModel = viewModel()
) {
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val selectedEntry = editEntryViewModel.selectedEntry
    var entryContent by remember { mutableStateOf(selectedEntry?.entryDB ?: "") }
    var selectedTimeMinutes by remember { mutableStateOf(selectedEntry?.timeMinutes) }
    val context = LocalContext.current

    // Use the reminder and repeat types from ViewModel
    var selectedReminderType by remember { mutableStateOf(editEntryViewModel.selectedReminderType) }
    var selectedRepeatType by remember { mutableStateOf(editEntryViewModel.selectedRepeatType) }
    var repeatOptions by remember { mutableStateOf(editEntryViewModel.repeatOptions) }

    val entryId = selectedEntry?.id ?: -1


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

    // NEW: Load attachments when the entry is selected
    LaunchedEffect(selectedEntry) {
        if (entryId != -1) {
            attachmentViewModel.loadAttachmentsForEntry(entryId)
        }
    }
    val attachments by attachmentViewModel.attachments

    // NEW: File Picker Launcher
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { selectedUri ->
            // Now we have an entryId, so we can add the attachment
            if (entryId != -1) {
                attachmentViewModel.addAttachment(entryId, selectedUri)
            }
        }
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

        // NEW: Attachment Section
        Text("Attachments", modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.titleSmall)

        // Button to Add Attachment
        Button(
            onClick = { filePickerLauncher.launch("*/*") },
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            Text("Add Attachment")
        }

        // List of Attachments
        LazyColumn {
            items(attachments) { attachment ->
                AttachmentItem(
                    attachment = attachment,
                    onViewClick = {
                        // Use the ViewModel to get a shareable URI and open it
                        val uri = attachmentViewModel.getUriForAttachment(attachment)
                        val intent = Intent(Intent.ACTION_VIEW).apply {
                            setDataAndType(uri, attachment.mimeType)
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        try {
                            context.startActivity(intent)
                        } catch (e: ActivityNotFoundException) { // <- FIXED: Use fully qualified name
                            Toast.makeText(context, "No app found to open this file", Toast.LENGTH_SHORT).show()
                        }
                    },
                    onDeleteClick = {
                        attachmentViewModel.deleteAttachment(attachment.id)
                    }
                )
            }
        }

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

                    // Generate RRule string using the new generator
                    val repeatDetails = if (selectedRepeatType != "Never") {
                        generateRRuleString(repeatOptions, selectedRepeatType) // ← CHANGE THIS LINE
                    } else {
                        null
                    }

                    // Determine if we need extra data (has reminder OR repeat)
                    val needsExtraData = selectedReminderType != "None" || selectedRepeatType != "Never"

                    // Update the entry in database
                    editEntryViewModel.updateBasicEntry(updatedEntry)


                    // Update extra data with repeat details
                    editEntryViewModel.updateEntry(
                        updatedEntry,
                        needsExtraData,
                        if (selectedReminderType != "None") selectedReminderType else null,
                        if (selectedRepeatType != "Never") selectedRepeatType else null,
                        repeatDetails
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

// NEW: Composable to display a single attachment
// NEW: Composable to display a single attachment
// NEW: Composable to display a single attachment
// Add these imports at the top


// NEW: Composable to display a single attachment
@Composable
fun AttachmentItem(
    attachment: EntryAttachment,
    onViewClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(modifier = Modifier.padding(8.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(attachment.fileName, fontWeight = FontWeight.Bold)
                Text("${attachment.fileSize / 1024} KB")
            }
            IconButton(onClick = onViewClick) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_menu_view), // system view icon
                    contentDescription = "View Attachment"
                )
            }
            IconButton(onClick = onDeleteClick) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_menu_delete), // system delete icon
                    contentDescription = "Delete Attachment"
                )
            }

        }
    }
}