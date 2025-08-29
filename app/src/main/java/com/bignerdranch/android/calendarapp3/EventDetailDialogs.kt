package com.bignerdranch.android.calendarapp3

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun EventDetailDialog(
    viewModel: EventDetailsViewModel = viewModel(),
    modifier: Modifier = Modifier,
    onDismissRequest: () -> Unit,
    selectedOption: String, // ADD THIS PARAMETER
    onOptionSelected: (String) -> Unit // ADD THIS CALLBACK

    ){
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
                RadioButtonSingleSelection_V2(
                    selectedOption = selectedOption,
                    onOptionSelected = onOptionSelected
                )

                Button(
                    onClick = {
                        viewModel.toggleEventDetailsDialog(false)
                    },
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text("Select")
                }
            }
        }
    }
}