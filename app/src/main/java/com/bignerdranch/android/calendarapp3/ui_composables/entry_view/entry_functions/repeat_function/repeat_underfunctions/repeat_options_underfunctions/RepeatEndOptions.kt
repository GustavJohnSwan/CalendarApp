package com.bignerdranch.android.calendarapp3.ui_composables.entry_view.entry_functions.repeat_function.repeat_underfunctions.repeat_options_underfunctions

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bignerdranch.android.calendarapp3.ui_composables.entry_view.entry_functions.repeat_function.repeat_underfunctions.RepeatOptions
import com.bignerdranch.android.calendarapp3.ui_composables.entry_view.entry_functions.repeat_function.repeat_underfunctions.repeat_types.number_input_field.NumberInputField


@Composable
fun EndOptions(
    options: RepeatOptions,
    onOptionsChange: (RepeatOptions) -> Unit
) {
    Column {
        Text("Ends:", modifier = Modifier.padding(bottom = 8.dp))

        // Never option
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(
                selected = options.endType == "never",
                onClick = { onOptionsChange(options.copy(endType = "never")) }
            )
            Text("Never", modifier = Modifier.padding(start = 4.dp))
        }

        // On date option
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(
                selected = options.endType == "on_date",
                onClick = { onOptionsChange(options.copy(endType = "on_date")) }
            )
            Text("On", modifier = Modifier.padding(start = 4.dp, end = 8.dp))

            if (options.endType == "on_date") {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Day input
                    NumberInputField(
                        value = options.endDateDay.toString(),
                        onValueChange = { newValue ->
                            val newDay = newValue.toIntOrNull() ?: 1
                            onOptionsChange(options.copy(endDateDay = newDay))
                        },
                        modifier = Modifier.width(50.dp),
                        label = "Day",
                        maxLength = 2,
                        onFocusLost = {
                            val validatedDay = options.endDateDay.coerceIn(1, 31)
                            if (options.endDateDay != validatedDay) {
                                onOptionsChange(options.copy(endDateDay = validatedDay))
                            }
                        }
                    )

                    Text("/", modifier = Modifier.padding(horizontal = 4.dp))

                    // Month input
                    NumberInputField(
                        value = options.endDateMonth.toString(),
                        onValueChange = { newValue ->
                            val newMonth = newValue.toIntOrNull() ?: 1
                            onOptionsChange(options.copy(endDateMonth = newMonth))
                        },
                        modifier = Modifier.width(50.dp),
                        label = "Month",
                        maxLength = 2,
                        onFocusLost = {
                            val validatedMonth = options.endDateMonth.coerceIn(1, 12)
                            if (options.endDateMonth != validatedMonth) {
                                onOptionsChange(options.copy(endDateMonth = validatedMonth))
                            }
                        }
                    )

                    Text("/", modifier = Modifier.padding(horizontal = 4.dp))

                    // Year input
                    NumberInputField(
                        value = options.endDateYear.toString(),
                        onValueChange = { newValue ->
                            val newYear = newValue.toIntOrNull() ?: 2024
                            onOptionsChange(options.copy(endDateYear = newYear))
                        },
                        modifier = Modifier.width(70.dp),
                        label = "Year",
                        maxLength = 4,
                        onFocusLost = {
                            val validatedYear = options.endDateYear.coerceIn(2024, 2100)
                            if (options.endDateYear != validatedYear) {
                                onOptionsChange(options.copy(endDateYear = validatedYear))
                            }
                        }
                    )
                }
            }
        }

        // After occurrences option
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(
                selected = options.endType == "after_occurrences",
                onClick = { onOptionsChange(options.copy(endType = "after_occurrences")) }
            )
            Text("After", modifier = Modifier.padding(start = 4.dp, end = 8.dp))

            NumberInputField(
                value = options.occurrences.toString(),
                onValueChange = { newValue ->
                    val newOccurrences = newValue.toIntOrNull() ?: 1
                    onOptionsChange(options.copy(occurrences = newOccurrences))
                },
                modifier = Modifier.width(60.dp),
                label = "",
                maxLength = 3,
                onFocusLost = {
                    val validatedOccurrences = options.occurrences.coerceAtLeast(1)
                    if (options.occurrences != validatedOccurrences) {
                        onOptionsChange(options.copy(occurrences = validatedOccurrences))
                    }
                }
            )

            Text("occurrences", modifier = Modifier.padding(start = 8.dp))
        }
    }
}