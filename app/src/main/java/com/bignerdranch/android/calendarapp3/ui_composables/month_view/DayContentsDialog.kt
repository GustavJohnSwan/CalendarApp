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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.remember
import androidx.compose.ui.text.style.TextOverflow
import com.bignerdranch.android.calendarapp3.buisness_logic.CouchBaseLiteViewModel
import com.bignerdranch.android.calendarapp3.buisness_logic.EditEntryViewModel
import com.bignerdranch.android.calendarapp3.database.EntryTable

// Add these imports at the TOP:
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalContext


@Composable
fun DayContentsDialog(
    modifier: Modifier = Modifier,
    onDismissRequest: () -> Unit,
    onNewEntry: () -> Unit,
    onEditEntry: (EntryTable) -> Unit, // Changed to accept EntryTable directly
    editEntryViewModel: EditEntryViewModel,
    couchBaseLiteViewModel: CouchBaseLiteViewModel, // ADD THIS
    eventList: List<EntryTable>,
) {
    val context = LocalContext.current

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
                    .padding(16.dp)
            ) {
                // Header with date
                Text(
                    text = "Events for ${editEntryViewModel.selectedDate}",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Entries list
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
                                    onEditEntry(entry) // Pass the entry directly
                                }
                            )
                        }
                    }
                }

                ElevatedButton(
                    onClick = { couchBaseLiteViewModel.runIt() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                ) {
                    Text("CouchBase Lite")
                }

                // Add this button in your dialog (after the CouchBase Lite button):
                ElevatedButton(
                    onClick = { couchBaseLiteViewModel.logDatabaseContents() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    Text("Log DB Contents")
                }


// Inside your Column, add these buttons:

// Log Database Contents button
                // Log Database Contents button
                ElevatedButton(
                    onClick = {
                        couchBaseLiteViewModel.logDatabaseContents()
                        // Show toast using Android's Toast
                        android.widget.Toast.makeText(
                            context,
                            "Database contents logged to Logcat",
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    Text("Log DB Contents")
                }

                // Export to JSON button
                ElevatedButton(
                    onClick = { couchBaseLiteViewModel.exportToJson() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    Text("Export to JSON (Clipboard)")
                }




                // New Event button
                ElevatedButton(
                    onClick = onNewEntry,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                ) {
                    Text("New Event")
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
