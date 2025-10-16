package com.bignerdranch.android.calendarapp3.ui_composables.entry_view.entry_functions.repeat_function.repeat_underfunctions.repeat_types

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bignerdranch.android.calendarapp3.ui_composables.entry_view.entry_functions.repeat_function.repeat_underfunctions.repeat_types.number_input_field.NumberInputField
import com.bignerdranch.android.calendarapp3.ui_composables.entry_view.entry_functions.repeat_function.repeat_underfunctions.RepeatOptions


@Composable
fun WeeklyRepeatOptions(
    options: RepeatOptions,
    onOptionsChange: (RepeatOptions) -> Unit
) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Repeat every")
            NumberInputField(
                value = options.interval.toString(),
                onValueChange = { newValue ->
                    val newInterval = newValue.toIntOrNull() ?: 1
                    onOptionsChange(options.copy(interval = newInterval))
                },
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .width(60.dp),
                label = "",
                maxLength = 2,
                onFocusLost = {
                    val validatedInterval = options.interval.coerceAtLeast(1)
                    if (options.interval != validatedInterval) {
                        onOptionsChange(options.copy(interval = validatedInterval))
                    }
                }
            )
            Text("week(s) on:")
        }

        val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
        val scrollState = rememberScrollState()

        Row(
            modifier = Modifier
                .padding(top = 8.dp)
                .horizontalScroll(scrollState)
        ) {
            days.forEachIndexed { index, day ->
                val isSelected = options.selectedDays.contains(index + 1)
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        val newDays = if (isSelected) {
                            options.selectedDays - (index + 1)
                        } else {
                            options.selectedDays + (index + 1)
                        }
                        onOptionsChange(options.copy(selectedDays = newDays))
                    },
                    label = { Text(day) },
                    modifier = Modifier.padding(end = 4.dp)
                )
            }
        }
    }
}
