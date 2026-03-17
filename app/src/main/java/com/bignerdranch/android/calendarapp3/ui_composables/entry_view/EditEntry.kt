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
import androidx.compose.material3.ButtonDefaults
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

import com.bignerdranch.android.calendarapp3.buisness_logic.objectbox.ObjectBoxEditEntryViewModel
import com.bignerdranch.android.calendarapp3.buisness_logic.objectbox.attachment.ObjectBoxAttachmentViewModel


@Composable
fun EditEntry(
    navController: NavController,
    editEntryViewModel: EditEntryViewModel,
    attachmentViewModel: AttachmentViewModel = viewModel(),
    couchbaseCalendarViewModel: CouchbaseCalendarViewModel,
    objectBoxEditEntryViewModel: ObjectBoxEditEntryViewModel,
    objectBoxAttachmentViewModel: ObjectBoxAttachmentViewModel,
    source: String

) {



    val cblId = editEntryViewModel.selectedCouchbaseId
    val cblUi by couchbaseCalendarViewModel.editingEntry.collectAsState()

    val obIdStr = editEntryViewModel.selectedObjectBoxId
    objectBoxEditEntryViewModel.selectedEntry
    val obEntryId: Long? = obIdStr?.toLongOrNull()




    var errorMessage by remember { mutableStateOf<String?>(null) }
    val selectedEntry = editEntryViewModel.selectedEntry


    val roomEntry = editEntryViewModel.selectedEntry
    val obEntry = objectBoxEditEntryViewModel.selectedEntry

    val initialContent = when (source) {
        "sqlite" -> roomEntry?.entryDB ?: ""
        "objectbox" -> obEntry?.entryOb ?: ""
        "couchbase" -> "" // will be filled from cblUi in LaunchedEffect
        else -> ""
    }

    val initialTime = when (source) {
        "sqlite" -> roomEntry?.timeMinutes
        "objectbox" -> obEntry?.timeMinutesOb
        else -> null
    }

    var entryContent by remember { mutableStateOf(initialContent) }
    var selectedTimeMinutes by remember { mutableStateOf(initialTime) }



    val context = LocalContext.current

    val scope = rememberCoroutineScope()


    // reminder and repeat types from ViewModel
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


    val sqliteEntryId = selectedEntry?.id ?: -1
    val couchbaseEntryId = editEntryViewModel.selectedCouchbaseId

    LaunchedEffect(source, cblId) {
        if (source == "couchbase" && !cblId.isNullOrBlank()) {
            couchbaseCalendarViewModel.loadEntryForEdit(cblId)
        }
    }

    LaunchedEffect(source, obIdStr) {

        if (source == "objectbox" && !obIdStr.isNullOrBlank()) {
            val id = obIdStr.toLongOrNull()
            if (id != null) {
                val entry = objectBoxEditEntryViewModel.getEntryById(id)
                if (entry != null) {
                    objectBoxEditEntryViewModel.onEventSelect(entry)
                }
            }
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



    LaunchedEffect(source, sqliteEntryId, couchbaseEntryId, obEntryId) {
        when (source) {
            "sqlite" -> if (sqliteEntryId != -1) {
                attachmentViewModel.loadAttachmentsForEntry(sqliteEntryId)
            }
            "couchbase" -> if (!couchbaseEntryId.isNullOrBlank()) {
                couchbaseCalendarViewModel.loadAttachmentsForEntry(couchbaseEntryId)
            }
            "objectbox" -> if (obEntryId != null) {
                objectBoxAttachmentViewModel.loadAttachmentsForEntry(obEntryId)
            }
        }
    }




    LaunchedEffect(cblUi) {
        if (source == "couchbase" && cblUi != null) {
            entryContent = cblUi!!.content
            selectedTimeMinutes = cblUi!!.timeMinutes
            selectedRepeatType = cblUi!!.repeat ?: "Never"
            selectedReminderType = cblUi!!.reminderType ?: "None"

            // parses repeatDetails into RepeatOptions
            repeatOptions =
                if (!cblUi!!.repeatDetails.isNullOrBlank() && selectedRepeatType != "Never") {
                    parseRRuleToRepeatOptions(cblUi!!.repeatDetails!!, selectedRepeatType)
                } else {
                    RepeatOptions()
                }

            // keeps VM in sync
            editEntryViewModel.repeatOptions = repeatOptions
        }
    }

    LaunchedEffect(obEntry) {
        if (source == "objectbox" && obEntry != null) {
            entryContent = obEntry.entryOb ?: ""
            selectedTimeMinutes = obEntry.timeMinutesOb

            // pulls extra-data-driven UI state from objectBoxEditEntryViewModel
            selectedRepeatType = objectBoxEditEntryViewModel.selectedRepeatType
            selectedReminderType = objectBoxEditEntryViewModel.selectedReminderType
            repeatOptions = objectBoxEditEntryViewModel.repeatOptions
        }
    }








    val sqliteAttachments by attachmentViewModel.attachments
    val cblAttachments by couchbaseCalendarViewModel.attachmentsForSelectedEntry.collectAsState()
    val obAttachments by objectBoxAttachmentViewModel.attachments

    // File Picker Launcher
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { selectedUri ->
            // entryId exists now, so the attachment can be added
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

    val obFilePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        val id = obEntryId
        if (uri != null && id != null) {
            objectBoxAttachmentViewModel.addAttachment(id, uri)
            objectBoxAttachmentViewModel.loadAttachmentsForEntry(id)
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
                when (source) {
                    "sqlite" -> filePickerLauncher.launch("*/*")
                    "couchbase" -> cblFilePickerLauncher.launch("*/*")
                    "objectbox" -> obFilePickerLauncher.launch("*/*")
                }
            },
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            Text("Add Attachment")
        }




        LazyColumn {
            when (source) {
                "sqlite" -> {
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
                }

                "couchbase" -> {
                    items(cblAttachments) { att ->
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
                            },
                            onDelete = {
                                val id = couchbaseEntryId ?: return@CouchbaseAttachmentItem
                                couchbaseCalendarViewModel.removeAttachmentFromEntry(id, att.id)
                            }
                        )
                    }
                }

                "objectbox" -> {
                    items(obAttachments, key = { it.id }) { att ->
                        CouchbaseAttachmentItem(
                            name = att.fileNameOb,
                            mime = att.mimeTypeOb,
                            size = att.fileSizeOb,
                            onOpen = {
                                try {
                                    val uri = objectBoxAttachmentViewModel.getUriForAttachment(att)

                                    Log.d("ObjectBoxOpen", "uriPathOb = ${att.uriPathOb}")
                                    Log.d("ObjectBoxOpen", "mimeTypeOb = ${att.mimeTypeOb}")
                                    Log.d("ObjectBoxOpen", "generatedUri = $uri")
                                    Log.d("ObjectBoxOpen", "fileExists = ${java.io.File(att.uriPathOb).exists()}")

                                    val intent = Intent(Intent.ACTION_VIEW).apply {
                                        setDataAndType(uri, att.mimeTypeOb)
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }

                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    Log.e("ObjectBoxOpen", "Failed to open attachment", e)
                                    Toast.makeText(context, "Failed to open attachment: ${e.message}", Toast.LENGTH_LONG).show()
                                }
                            },
                            onDelete = {
                                val id = obEntryId ?: return@CouchbaseAttachmentItem
                                objectBoxAttachmentViewModel.deleteAttachment(att.id, id)
                            }
                        )
                    }
                }
            }
        }









        Button(
            onClick = {
                Log.d("ObjectBoxTest", "Save clicked, source=$source")
                if (entryContent.isBlank()) {
                    errorMessage = "Event cannot be empty"
                    return@Button
                }


                val repeatDetails = if (selectedRepeatType != "Never") {
                    generateRRuleString(repeatOptions, selectedRepeatType)
                } else {
                    null
                }

                val needsExtraData = selectedReminderType != "None" || selectedRepeatType != "Never"

                // COUCHBASE SAVE (does NOT depend on selectedEntry)
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
                        hasExtraData = needsExtraData,
                        reminderType = if (selectedReminderType != "None") selectedReminderType else null,
                        repeat = if (selectedRepeatType != "Never") selectedRepeatType else null,
                        repeatDetails = repeatDetails,

                        onUpdated = { navController.popBackStack() }
                    )
                    return@Button
                }

                // SQLITE SAVE (still uses selectedEntry)
                selectedEntry?.let { entry ->
                    val updatedEntry = entry.copy(
                        entryDB = entryContent,
                        timeMinutes = selectedTimeMinutes
                    )



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

                // OBJECTBOX SAVE (does NOT depend on Room selectedEntry)
                if (source == "objectbox") {
                    Log.d("ObjectBoxTest", "Saving ObjectBox entry...")

                    val obEntry = objectBoxEditEntryViewModel.selectedEntry
                    if (obEntry == null) {
                        Toast.makeText(context, "Missing ObjectBox selected entry", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    // Update fields
                    obEntry.entryOb = entryContent
                    obEntry.timeMinutesOb = selectedTimeMinutes



                    // Save extra data (reminder/repeat)
                    objectBoxEditEntryViewModel.updateEntry(
                        entry = obEntry,
                        hasReminder = needsExtraData,
                        reminderType = if (selectedReminderType != "None") selectedReminderType else null,
                        repeatType = if (selectedRepeatType != "Never") selectedRepeatType else null,
                        repeatDetails = repeatDetails
                    )

                    navController.popBackStack()
                    return@Button
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF476810)),
            modifier = Modifier.padding(16.dp)
        ) {
            Text("Save Changes")
        }


        if (source == "sqlite") {
            Button(
                onClick = {
                    selectedEntry?.let { entry ->
                        editEntryViewModel.deleteEntry(entry)
                        navController.popBackStack()
                    }
                },
                modifier = Modifier.padding(horizontal = 16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Red
                )
            ) {
                Text("Delete")
            }
        }
        if (source == "objectbox") {
            Button(
                onClick = {
                    val obEntry = objectBoxEditEntryViewModel.selectedEntry
                    if (obEntry != null) {
                        objectBoxEditEntryViewModel.deleteEntry(obEntry)
                        navController.popBackStack()
                    } else {
                        Toast.makeText(context, "Missing ObjectBox selected entry", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.padding(horizontal = 16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Red
                )
            ) {
                Text("Delete")
            }
        }

        if (source == "couchbase") {
            Button(
                onClick = {
                    val id = editEntryViewModel.selectedCouchbaseId
                    if (!id.isNullOrBlank()) {
                        couchbaseCalendarViewModel.deleteEntry(
                            entryId = id,
                            date = editEntryViewModel.selectedDate
                        )
                        navController.popBackStack()
                    } else {
                        Toast.makeText(context, "Missing Couchbase entry ID", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.padding(horizontal = 16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Red
                )
            ) {
                Text("Delete")
            }
        }

    }
}


// Composable to display a single attachment
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