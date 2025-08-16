package com.bignerdranch.android.calendarapp3


import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
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
fun NewEntry(
    navController: NavController,
    viewModel: CalendarViewModel = viewModel(),
    entryTableViewModel: EntryTableViewModel,
    editEntryViewModel: EditEntryViewModel
) {
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isExtraDataEnabled by remember { mutableStateOf(false) }

    Column {
        OutlinedTextField(
            value = viewModel.newEventText,
            onValueChange = {
                viewModel.onEventTextBoxSelect(it)
                errorMessage = null
            },
            label = { Text("Enter Event") },
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

        CheckboxMinimalExample()

        Button(
            onClick = {
                if (viewModel.newEventText.isBlank()) {
                    errorMessage = "Event cannot be empty"
                    return@Button
                }

                entryTableViewModel.insertEntry(
                    date = editEntryViewModel.selectedDate,
                    content = viewModel.newEventText
                )
                navController.popBackStack()
            },
            modifier = Modifier.padding(16.dp)
        ) {
            Text("Save Event")
        }
    }
}


@Composable
fun CheckboxMinimalExample() {
    var checked by remember { mutableStateOf(false) }

    Row(
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = { checked = it }
        )
        Text(
            if (checked) "Reminder ON" else "Reminder OFF"
        )
    }
}