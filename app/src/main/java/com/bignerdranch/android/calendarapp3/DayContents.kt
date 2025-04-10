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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow

// this composable is responsible for the style and properties of the SELECTED DATE EVENT CONTENT DIALOG
@Composable
fun MinimalDialog(modifier: Modifier = Modifier, onDismissRequest: () -> Unit, onNewEntry: () -> Unit, eventList: List<EntryTable>) { // salaboju šo un citas problēmas
    Dialog(onDismissRequest = { onDismissRequest() }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(500.dp),

            shape = RoundedCornerShape(16.dp),
        ) {


            Box(modifier = Modifier.fillMaxSize()) {
                Row () {
                    Text("List Of Events")
                }



                // used to display ALL EVENTS FROM SELECTED DATE. Currently events are not bound by dates
                // so this displays all events from the EntryTable
                LazyColumn(
                    modifier = Modifier
                        .padding(16.dp)
                        .align(Alignment.CenterStart)
                        .fillMaxSize(),
                    // adds padding BETWEEN ROWS
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // takes the content from a eventList from EntryTableViewModel and displays it
                    items(eventList) { EntryTable ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(60.dp) // each row has a static height
                                .background(Color(0xFFD8CAB8), shape = RoundedCornerShape(8.dp))
                                .padding(horizontal = 16.dp) // adds padding INSIDE OF ROWS
                                // makes the rows clickable. Currently this doesn't do anything
                                .clickable {
                                    // here will be a function that opens a new window to SHOW, EDIT or DELETE the FULL EVENT CONTENT
                                },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${EntryTable.dateDB} ${EntryTable.entryDB} ${EntryTable.idEx}",
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(end = 8.dp),
                                maxLines = 1, // this limits the text to one line
                                overflow = TextOverflow.Ellipsis, // this makes sure that when the EVENT text is too long
                                // it "cuts it off" and adds "..." (an ellipsis)
                                color = Color.Black
                            )
                        }
                    }
                }


                // adds a new row with a BUTTON that takes the user to a new screen where he/she can create a NEW EVENT (currently limited)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                ) {
                    Spacer(modifier = Modifier.weight(1f))

                    // this BUTTON hovers above the other content
                    ElevatedButton(
                        onClick =  onNewEntry,
                        modifier = Modifier
                            .padding(16.dp)

                    ) {
                        Text("New Event")
                    }
                }
            }
        }
    }
}