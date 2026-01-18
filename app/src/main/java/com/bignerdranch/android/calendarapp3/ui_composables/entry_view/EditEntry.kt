package com.bignerdranch.android.calendarapp3.ui_composables.entry_view

import android.R
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.bignerdranch.android.calendarapp3.buisness_logic.CouchbaseCalendarViewModel
import com.bignerdranch.android.calendarapp3.ui_composables.entry_view.entry_functions.InputTimePicker
import com.bignerdranch.android.calendarapp3.ui_composables.entry_view.entry_functions.repeat_function.repeat_underfunctions.RepeatOptions
import com.bignerdranch.android.calendarapp3.ui_composables.entry_view.entry_functions.repeat_function.rrule_generation.generateRRuleString
import com.bignerdranch.android.calendarapp3.ui_composables.entry_view.entry_functions.repeat_function.rrule_generation.parseRRuleToRepeatOptions
import kotlinx.coroutines.launch


@Composable
fun EditEntry(
    navController: NavController,
    editEntryViewModel: EditEntryViewModel,
    attachmentViewModel: AttachmentViewModel = viewModel(),
    couchbaseCalendarViewModel: CouchbaseCalendarViewModel,
    source: String

) {



    val cblId = editEntryViewModel.selectedCouchbaseId
    val cblUi by couchbaseCalendarViewModel.editingEntry.collectAsState()



    var errorMessage by remember { mutableStateOf<String?>(null) }
    val selectedEntry = editEntryViewModel.selectedEntry
    var entryContent by remember { mutableStateOf(selectedEntry?.entryDB ?: "") }
    var selectedTimeMinutes by remember { mutableStateOf(selectedEntry?.timeMinutes) }
    val context = LocalContext.current

    val scope = rememberCoroutineScope()


    // Use the reminder and repeat types from ViewModel
    var selectedReminderType by remember { mutableStateOf(editEntryViewModel.selectedReminderType) }
    var selectedRepeatType by remember { mutableStateOf(editEntryViewModel.selectedRepeatType) }
    var repeatOptions by remember { mutableStateOf(editEntryViewModel.repeatOptions) }

    val entryId = selectedEntry?.id ?: -1

    val status by couchbaseCalendarViewModel.editLoadStatus.collectAsState()



    // Update local state when ViewModel changes
    LaunchedEffect(editEntryViewModel.selectedReminderType) {
        if (source == "sqlite" && selectedEntry != null) {
            selectedReminderType = editEntryViewModel.selectedReminderType
        }
    }

    LaunchedEffect(editEntryViewModel.selectedRepeatType) {
        if (source == "sqlite" && selectedEntry != null) {
            selectedRepeatType = editEntryViewModel.selectedRepeatType
        }
    }

    LaunchedEffect(editEntryViewModel.repeatOptions) {
        if (source == "sqlite" && selectedEntry != null) {
            repeatOptions = editEntryViewModel.repeatOptions
        }
    }

    // NEW: Load attachments when the entry is selected
    /*
    LaunchedEffect(selectedEntry) {
        if (entryId != -1) {
            attachmentViewModel.loadAttachmentsForEntry(entryId)
        }
    }
    val attachments by attachmentViewModel.attachments
     */

    val sqliteEntryId = selectedEntry?.id ?: -1
    val couchbaseEntryId = editEntryViewModel.selectedCouchbaseId

    LaunchedEffect(source, cblId) {
        if (source == "couchbase" && !cblId.isNullOrBlank()) {
            couchbaseCalendarViewModel.loadEntryForEdit(cblId)
        }
    }

    if (source == "couchbase" && cblUi == null) {
        Column {
            Row {
                Text("Loading...", modifier = Modifier.padding(16.dp))
            }
            Row {
                Text("cblId = ${editEntryViewModel.selectedCouchbaseId}", modifier = Modifier.padding(16.dp))
            }
            Row {
                Text("status = ${status ?: "(none)"}", modifier = Modifier.padding(16.dp))
            }
        }
        Text("Loading...", modifier = Modifier.padding(16.dp))
        Text("cblId = ${editEntryViewModel.selectedCouchbaseId}", modifier = Modifier.padding(16.dp))
        Text("status = ${status ?: "(none)"}", modifier = Modifier.padding(16.dp))
        return
    }


    LaunchedEffect(source, sqliteEntryId, couchbaseEntryId) {
        if (source == "sqlite") {
            if (sqliteEntryId != -1) attachmentViewModel.loadAttachmentsForEntry(sqliteEntryId)
        } else {
            if (!couchbaseEntryId.isNullOrBlank()) couchbaseCalendarViewModel.loadAttachmentsForEntry(couchbaseEntryId)
        }
    }




    LaunchedEffect(cblUi) {
        if (source == "couchbase" && cblUi != null) {
            entryContent = cblUi!!.content
            selectedTimeMinutes = cblUi!!.timeMinutes // don’t force 0 unless you really want that
            selectedRepeatType = cblUi!!.repeat ?: "Never"
            selectedReminderType = cblUi!!.reminderType ?: "None"

            // ✅ NEW: parse repeatDetails into RepeatOptions
            repeatOptions =
                if (!cblUi!!.repeatDetails.isNullOrBlank() && selectedRepeatType != "Never") {
                    parseRRuleToRepeatOptions(cblUi!!.repeatDetails!!, selectedRepeatType)
                } else {
                    RepeatOptions()
                }

            // keep VM in sync
            editEntryViewModel.repeatOptions = repeatOptions
        }
    }








    val sqliteAttachments by attachmentViewModel.attachments
    val cblAttachments by couchbaseCalendarViewModel.attachmentsForSelectedEntry.collectAsState()


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

    val cblFilePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        val id = couchbaseEntryId
        if (uri != null && !id.isNullOrBlank()) {
            couchbaseCalendarViewModel.addAttachmentToEntry(id, uri)
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


        Text("Attachments", modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.titleSmall)

        Button(
            onClick = {
                if (source == "sqlite") {
                    filePickerLauncher.launch("*/*")
                } else {
                    cblFilePickerLauncher.launch("*/*")
                }
            },
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            Text("Add Attachment")
        }


        // List of Attachments
        LazyColumn {
            if (source == "sqlite") {
                items(sqliteAttachments) { attachment ->
                    AttachmentItem(
                        attachment = attachment,
                        onViewClick = {
                            val uri = attachmentViewModel.getUriForAttachment(attachment)
                            val intent = Intent(Intent.ACTION_VIEW).apply {
                                setDataAndType(uri, attachment.mimeType)
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            try {
                                context.startActivity(intent)
                            } catch (e: ActivityNotFoundException) {
                                Toast.makeText(context, "No app found to open this file type", Toast.LENGTH_SHORT).show()
                            }
                        },
                        onDeleteClick = {
                            attachmentViewModel.deleteAttachment(attachment.id)
                            attachmentViewModel.loadAttachmentsForEntry(sqliteEntryId)
                        }
                    )

                }
            } else {
                items(cblAttachments) { att ->
                    // You can reuse the same visual row, but it expects EntryAttachment.
                    // Fastest is to make a small Couchbase row below (copy of AttachmentItem).
                    CouchbaseAttachmentItem(
                        name = att.name,
                        mime = att.mime,
                        size = att.size,
                        onOpen = {
                            val id = couchbaseEntryId ?: return@CouchbaseAttachmentItem
                            scope.launch {
                                val uri = couchbaseCalendarViewModel.getOpenableUriForAttachment(id, att.id)
                                openUri(context, uri, att.mime)
                            }
                        }
,
                        onDelete = {
                            val id = couchbaseEntryId ?: return@CouchbaseAttachmentItem
                            couchbaseCalendarViewModel.removeAttachmentFromEntry(id, att.id)
                        }
                    )
                }
            }
        }










        Button(
            onClick = {
                if (entryContent.isBlank()) {
                    errorMessage = "Event cannot be empty"
                    return@Button
                }

                // ✅ Compute these for BOTH SQLite and Couchbase
                val repeatDetails = if (selectedRepeatType != "Never") {
                    generateRRuleString(repeatOptions, selectedRepeatType)
                } else {
                    null
                }

                val needsExtraData = selectedReminderType != "None" || selectedRepeatType != "Never"

                // ✅ COUCHBASE SAVE (does NOT depend on selectedEntry)
                if (source == "couchbase") {
                    val id = editEntryViewModel.selectedCouchbaseId
                    if (id.isNullOrBlank()) {
                        Toast.makeText(context, "Missing Couchbase ID", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    couchbaseCalendarViewModel.updateCalendarEntry(
                        entryId = id,
                        date = editEntryViewModel.selectedDate,
                        content = entryContent,
                        timeMinutes = selectedTimeMinutes,

                        // 👇 these must exist in the updated ViewModel function signature
                        hasExtraData = needsExtraData,
                        reminderType = if (selectedReminderType != "None") selectedReminderType else null,
                        repeat = if (selectedRepeatType != "Never") selectedRepeatType else null,
                        repeatDetails = repeatDetails,

                        onUpdated = { navController.popBackStack() }
                    )
                    return@Button
                }

                // ✅ SQLITE SAVE (still uses selectedEntry)
                selectedEntry?.let { entry ->
                    val updatedEntry = entry.copy(
                        entryDB = entryContent,
                        timeMinutes = selectedTimeMinutes
                    )

                    editEntryViewModel.updateBasicEntry(updatedEntry)

                    editEntryViewModel.updateEntry(
                        updatedEntry,
                        needsExtraData,
                        if (selectedReminderType != "None") selectedReminderType else null,
                        if (selectedRepeatType != "Never") selectedRepeatType else null,
                        repeatDetails
                    )

                    navController.popBackStack()
                    Log.d("EditEntry", "source=$source cblId=$cblId")
                }
            }
,
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

@Composable
fun CouchbaseAttachmentItem(
    name: String,
    mime: String,
    size: Long,
    onOpen: () -> Unit,
    onDelete: () -> Unit
) {
    Card(modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = name, fontWeight = FontWeight.Bold)
                Text(text = "$mime • ${size / 1024} KB", style = MaterialTheme.typography.bodySmall)
            }
            IconButton(onClick = onOpen) { Text("Open") }
            IconButton(onClick = onDelete) { Text("Del") }
        }
    }
}

private fun openUri(context: Context, uri: Uri, mime: String) {
    try {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, mime)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(context, "No app found to open this file type", Toast.LENGTH_SHORT).show()
    }
}