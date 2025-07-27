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
fun EditEntry(navController: NavController, viewModel: CalendarViewModel = viewModel(), entryTableViewModel: EntryTableViewModel, editEntryViewModel: EditEntryViewModel) {
    var errorText by remember { mutableStateOf("") }

    val selectedEntry = editEntryViewModel.selectedEntry
    var editableText by remember {
        mutableStateOf(
            selectedEntry?.let {
                "${it.entryDB} ${it.idEx}"
            } ?: ""
        )
    }



    Column {
        Row {
            OutlinedTextField(
                value = editableText,
                onValueChange = {
                    editableText = it
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
            Button(
                onClick = {
                    val trimmedInput = editableText.trim() // Now using editableText here
                    if (trimmedInput.isBlank()) {
                        errorText = "Name cannot be empty."
                        return@Button
                    }

                    val nameParts = trimmedInput.split(" ")
                    //val dateDB = nameParts.getOrNull(0) ?: ""
                    val entryDB = nameParts.getOrNull(0) ?: ""
                    val idEx = nameParts.getOrNull(1) ?: ""

                    if (entryDB.length < 2) {
                        errorText = "First name must be at least 2 characters."
                        return@Button
                    }

                    // Check if selectedEntry is not null, then update it
                    selectedEntry?.let {
                        val updatedEntry = it.copy(entryDB = entryDB, idEx = idEx)
                        editEntryViewModel.updateEntry(updatedEntry) // Update the database
                        entryTableViewModel.getAllEntryTables() // Refresh the list
                    }

                    navController.popBackStack()
                },
                modifier = Modifier.padding(16.dp)
            ) {
                Text("Save Changes")
            }
            /*
            Button(
                onClick = {
                    entryTableViewModel.deleteEntryTable(entryTable)
                }
            ) {
                Text("Delete")
            }

             */
        }
    }
}
