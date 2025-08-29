package com.bignerdranch.android.calendarapp3

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TimeInput
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InputTimePicker(
    currentTimeMinutes: Int?,
    onTimeSelected: (Int) -> Unit
) {
    var showPicker by remember { mutableStateOf(false) }

    // Default to 12:00 (720 minutes) if no time is selected
    val displayTime = currentTimeMinutes ?: 720 // 12:00 = 12*60 = 720 minutes

    // Display "Set Time" + formatted time (always shows something)
    Text(
        text = "Set Time: ${TimeUtils.formatTime(displayTime)}", // ← Always shows "Set Time: HH:mm"
        modifier = Modifier
            .clickable { showPicker = true }
            .padding(8.dp)
    )

    if (showPicker) {
        val initialState = remember {
            Calendar.getInstance().apply {
                // Use current time if available, otherwise default to 12:00
                if (currentTimeMinutes != null) {
                    set(Calendar.HOUR_OF_DAY, currentTimeMinutes / 60)
                    set(Calendar.MINUTE, currentTimeMinutes % 60)
                } else {
                    set(Calendar.HOUR_OF_DAY, 12) // Default to 12:00
                    set(Calendar.MINUTE, 0)
                }
            }
        }

        val timePickerState = rememberTimePickerState(
            initialHour = initialState.get(Calendar.HOUR_OF_DAY),
            initialMinute = initialState.get(Calendar.MINUTE),
            is24Hour = true
        )

        Column(
            modifier = Modifier
                .padding(16.dp)
                .scale(0.7f)
        ) {
            TimeInput(state = timePickerState)

            Row(modifier = Modifier.padding(top = 16.dp)) {
                Button(
                    onClick = { showPicker = false },
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Text("Cancel")
                }
                Button(
                    onClick = {
                        val totalMinutes = timePickerState.hour * 60 + timePickerState.minute
                        onTimeSelected(totalMinutes)
                        showPicker = false
                    }
                ) {
                    Text("Confirm")
                }
            }
        }
    }
}