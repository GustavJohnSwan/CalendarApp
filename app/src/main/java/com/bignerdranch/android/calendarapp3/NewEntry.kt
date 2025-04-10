package com.bignerdranch.android.calendarapp3


import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController

@Composable
fun NewEntry(navController: NavController, viewModel: CalendarViewModel = viewModel(), entryTableViewModel: EntryTableViewModel) {
    var errorText by remember { mutableStateOf("") }

    Column {


        Row {

            // takes the input and gives it to viewModel
            OutlinedTextField(
                value = viewModel.newEventText,
                onValueChange = {
                    viewModel.onEventTextBoxSelect(it)
                    errorText = ""
                },
                label = { Text("Enter Event") },
                isError = errorText.isNotEmpty()
            )

            if (errorText.isNotEmpty()) {
                Text(
                    text = errorText,
                    color = Color.Red,
                    modifier = Modifier.padding(start = 16.dp, top = 4.dp),
                    textAlign = TextAlign.Start
                )
            }
        }


        Row {
            // takes the user input and adds it to the database EntryTable
            Button(
                onClick = {
                    val trimmedInput = viewModel.newEventText.trim()
                    if (trimmedInput.isBlank()) {
                        errorText = "Name cannot be empty."
                        return@Button
                    }

                    val nameParts = trimmedInput.split(" ")
                    val dateDB = nameParts.getOrNull(0) ?: ""
                    val entryDB = nameParts.getOrNull(1) ?: ""
                    val idEx = nameParts.getOrNull(2) ?: ""

                    if (dateDB.length < 2) {
                        errorText = "First name must be at least 2 characters."
                        return@Button
                    }

                    entryTableViewModel.insertEntryTable(dateDB, entryDB, idEx)
                    navController.popBackStack()
                    entryTableViewModel.getAllEntryTables()
                },
                modifier = Modifier.padding(16.dp)
            ) {
                Text("Save Changes")
            }
        }
    }
}