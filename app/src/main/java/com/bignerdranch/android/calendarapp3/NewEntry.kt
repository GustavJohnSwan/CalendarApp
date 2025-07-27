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
import androidx.compose.ui.Alignment

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController

@Composable
fun NewEntry(navController: NavController, viewModel: CalendarViewModel = viewModel(), entryTableViewModel: EntryTableViewModel, editEntryViewModel: EditEntryViewModel,) {
    var errorText by remember { mutableStateOf("") }

    var isExtraDataEnabled by remember { mutableStateOf(false) }
    var extraFieldOne by remember { mutableStateOf("") }
    var extraFieldTwo by remember { mutableStateOf("") }

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
                    //val dateDB = nameParts.getOrNull(0) ?: ""
                    val dateDB = editEntryViewModel.selectedDate ?: ""
                    val entryDB = nameParts.getOrNull(0) ?: ""
                    val idEx = nameParts.getOrNull(1) ?: ""

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
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    "Extra Data ",
                    modifier = Modifier.padding(end = 8.dp),
                    textAlign = TextAlign.Center
                )
            }
            Column {
                SwitchExtraData(
                    isChecked = isExtraDataEnabled,
                    onCheckedChange = { isExtraDataEnabled = it }
                )

            }
        }
        // Conditionally show extra fields
        if (isExtraDataEnabled) {
            Row {
                OutlinedTextField(
                    value = extraFieldOne,
                    onValueChange = { extraFieldOne = it },
                    label = { Text("Reminder (yyyy-mm-dd hh:mm)") },
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Row {
                CheckBoxRepeatEvent()
            }
        }
    }
}