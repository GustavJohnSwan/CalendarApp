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

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable

import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import com.bignerdranch.android.calendarapp3.buisness_logic.objectbox.ObjectBoxEditEntryViewModel

import com.bignerdranch.android.calendarapp3.ui_models.UiEvent





private enum class EventSource { SQLITE, COUCHBASE, OBJECTBOX }


@Composable
fun DayContentsDialog(
    modifier: Modifier = Modifier,
    onDismissRequest: () -> Unit,
    onNewEntry: () -> Unit,
    onEditEntry: (EntryTable) -> Unit,
    editEntryViewModel: EditEntryViewModel,
    eventList: List<EntryTable>,
    couchbaseCalendarViewModel: CouchbaseCalendarViewModel,
    onNewEntryCouchbase: () -> Unit,
    onEditEntryCouchbase: (UiEvent) -> Unit,

    objectBoxEditEntryViewModel: ObjectBoxEditEntryViewModel,
    onNewEntryObjectBox: () -> Unit,
    onEditEntryObjectBox: (UiEvent) -> Unit
) {
    val context = LocalContext.current

    var source by rememberSaveable { mutableStateOf(EventSource.SQLITE) }
    val couchEvents by couchbaseCalendarViewModel.eventsForSelectedDate.collectAsState()

    val obEntries = objectBoxEditEntryViewModel.dateEntries.value




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

    LaunchedEffect(editEntryViewModel.selectedDate) {
        // Preload Couchbase list so toggle is instant
        couchbaseCalendarViewModel.loadEntriesForDate(editEntryViewModel.selectedDate)
        objectBoxEditEntryViewModel.loadEntriesForDate(editEntryViewModel.selectedDate)
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

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ElevatedButton(
                        onClick = { source = EventSource.SQLITE },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "SQLite",
                            maxLines = 1
                        )
                    }

                    ElevatedButton(
                        onClick = {
                        source = EventSource.COUCHBASE
                        // refresh on demand too
                        couchbaseCalendarViewModel.loadEntriesForDate(editEntryViewModel.selectedDate) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Couchbase",
                            maxLines = 1
                        )
                    }

                    ElevatedButton(
                        onClick = {
                        source = EventSource.OBJECTBOX
                        objectBoxEditEntryViewModel.loadEntriesForDate(editEntryViewModel.selectedDate) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "ObjectBox",
                            maxLines = 1
                        )
                    }
                }




                val uiList: List<UiEvent> = when (source) {
                    EventSource.SQLITE -> {
                        filteredEntries.map { e ->
                            UiEvent(
                                id = e.id.toString(),
                                date = e.dateDB,
                                content = e.entryDB,
                                timeMinutes = e.timeMinutes
                            )
                        }
                    }

                    EventSource.COUCHBASE -> {
                        couchEvents
                    }

                    EventSource.OBJECTBOX -> {
                        obEntries.map { e ->
                            UiEvent(
                                id = e.id.toString(),
                                date = e.dateOb,
                                content = e.entryOb,
                                timeMinutes = e.timeMinutesOb
                            )
                        }
                    }
                }

                if (uiList.isEmpty()) {
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
                        items(uiList, key = { it.id }) { ev ->
                            UiEventItem(
                                ev = ev,
                                onClick = {


                                    when (source) {
                                        EventSource.SQLITE -> {
                                            val sqliteEntry = filteredEntries.firstOrNull { it.id.toString() == ev.id }
                                            if (sqliteEntry != null) {
                                                editEntryViewModel.onEventSelect(sqliteEntry)
                                                onEditEntry(sqliteEntry)
                                            }
                                        }

                                        EventSource.COUCHBASE -> {
                                            onEditEntryCouchbase(ev)
                                        }

                                        EventSource.OBJECTBOX -> {
                                            onEditEntryObjectBox(ev)
                                        }
                                    }
                                }

                            )
                        }
                    }
                }



                Spacer(modifier = Modifier.height(16.dp))

                when (source) {
                    EventSource.SQLITE -> {
                        ElevatedButton(
                            onClick = onNewEntry,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("New Event (SQLite)")
                        }
                    }
                    EventSource.COUCHBASE -> {
                        ElevatedButton(
                            onClick = onNewEntryCouchbase,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("New Event (Couchbase Lite)")
                        }
                    }
                    EventSource.OBJECTBOX -> {
                        ElevatedButton(
                            onClick = onNewEntryObjectBox,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("New Event (ObjectBox)")
                        }
                    }
                }
                }


            }
        }
    }


@Composable
private fun UiEventItem(
    ev: UiEvent,
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
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = ev.content ?: "",
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
