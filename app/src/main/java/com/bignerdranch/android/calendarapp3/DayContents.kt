package com.bignerdranch.android.calendarapp3

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow

@Composable
fun MinimalDialog(
    modifier: Modifier = Modifier,
    onDismissRequest: () -> Unit,
    onNewEntry: () -> Unit,
    onEditEntry: () -> Unit,
    editEntryViewModel: EditEntryViewModel,
    eventList: List<EntryTable>,
) {
    Dialog(onDismissRequest = { onDismissRequest() }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(500.dp),
            shape = RoundedCornerShape(16.dp),
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Row {
                    Text("List Of Events ${editEntryViewModel.selectedDate}")
                }

                // Display all events from the selected date
                LazyColumn(
                    modifier = Modifier
                        .padding(16.dp)
                        .align(Alignment.CenterStart)
                        .fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    //items(eventList) { entryTable -> // entryTable is the event object
                    items(eventList.filter { it.dateDB == editEntryViewModel.selectedDate }) { entryTable ->

                    Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(60.dp)
                                .background(Color(0xFFD8CAB8), shape = RoundedCornerShape(8.dp))
                                .padding(horizontal = 16.dp)
                                .clickable {
                                    // Pass the full EntryTable object to EditEntryViewModel
                                    editEntryViewModel.onEventSelect(entryTable) // Set selected entry
                                    onEditEntry() // Open the Edit screen
                                },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${entryTable.dateDB} ${entryTable.entryDB} ${entryTable.idEx}",
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(end = 8.dp),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = Color.Black
                            )
                        }
                    }
                }

                // Button to create a new event
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                ) {
                    Spacer(modifier = Modifier.weight(1f))

                    ElevatedButton(
                        onClick = onNewEntry,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text("New Event")
                    }
                }
            }
        }
    }
}
