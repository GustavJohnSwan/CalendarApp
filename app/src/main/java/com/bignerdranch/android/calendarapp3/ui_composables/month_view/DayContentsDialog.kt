package com.bignerdranch.android.calendarapp3.ui_composables.month_view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.bignerdranch.android.calendarapp3.buisness_logic.CouchbaseCalendarViewModel
import com.bignerdranch.android.calendarapp3.buisness_logic.EditEntryViewModel
import com.bignerdranch.android.calendarapp3.database.EntryTable

@Composable
fun DayContentsDialog(
    modifier: Modifier = Modifier,
    onDismissRequest: () -> Unit,
    onNewEntry: () -> Unit,
    onEditEntry: (EntryTable) -> Unit,
    editEntryViewModel: EditEntryViewModel,
    eventList: List<EntryTable>,
    couchbaseCalendarViewModel: CouchbaseCalendarViewModel,
) {
    val context = LocalContext.current

    // Optional: show toast messages emitted by couchbaseCalendarViewModel
    val toastMessage by couchbaseCalendarViewModel.toastMessage.collectAsState()
    LaunchedEffect(toastMessage) {
        toastMessage?.let { message ->
            android.widget.Toast.makeText(context, message, android.widget.Toast.LENGTH_SHORT).show()
            couchbaseCalendarViewModel.clearToastMessage()
        }
    }

    val filteredEntries = remember(eventList, editEntryViewModel.selectedDate) {
        eventList.filter { it.dateDB == editEntryViewModel.selectedDate }
    }

    Dialog(onDismissRequest = onDismissRequest) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .height(500.dp),
            shape = RoundedCornerShape(16.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                Text(
                    text = "Events for ${editEntryViewModel.selectedDate}",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                if (filteredEntries.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No events for this date")
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredEntries) { entry ->
                            EventItem(
                                entry = entry,
                                onClick = {
                                    editEntryViewModel.onEventSelect(entry)
                                    onEditEntry(entry)
                                }
                            )
                        }
                    }
                }

                // --- Minimal Couchbase controls (backbone for 2-day goal) ---

                ElevatedButton(
                    onClick = {
                        couchbaseCalendarViewModel.createCalendarEntry(
                            date = editEntryViewModel.selectedDate,
                            content = "CBL test event",
                            timeMinutes = 900,
                            hasExtraData = true,
                            reminderType = "Notification",
                            repeat = "Weekly"
                        )
                        android.widget.Toast.makeText(
                            context,
                            "Created Couchbase event (check Logcat / export)",
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                        onDismissRequest()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                ) {
                    Text("New Event (Couchbase Lite)")
                }

                ElevatedButton(
                    onClick = {
                        couchbaseCalendarViewModel.logCalendarDatabaseContents()
                        android.widget.Toast.makeText(
                            context,
                            "Couchbase contents logged to Logcat",
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    Text("Log Couchbase DB (Logcat)")
                }

                ElevatedButton(
                    onClick = onNewEntry,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    Text("New Event (SQLite)")
                }
            }
        }
    }
}

@Composable
private fun EventItem(
    entry: EntryTable,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = entry.entryDB ?: "",
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
