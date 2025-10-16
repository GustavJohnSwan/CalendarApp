package com.bignerdranch.android.calendarapp3.ui_composables.entry_view.entry_functions.repeat_function.repeat_underfunctions.repeat_types

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.bignerdranch.android.calendarapp3.ui_composables.entry_view.entry_functions.repeat_function.repeat_underfunctions.repeat_types.number_input_field.NumberInputField
import com.bignerdranch.android.calendarapp3.ui_composables.entry_view.entry_functions.repeat_function.repeat_underfunctions.RepeatOptions


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonthlyRepeatOptions(
    options: RepeatOptions,
    onOptionsChange: (RepeatOptions) -> Unit
) {
    Column {
        // Option A: Absolute day
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = options.monthlyType == "absolute",
                    onClick = { onOptionsChange(options.copy(monthlyType = "absolute")) }
                )
                Text("On day", modifier = Modifier.padding(start = 4.dp))
            }

            if (options.monthlyType == "absolute") {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(start = 48.dp)) {
                    NumberInputField(
                        value = options.absoluteDay.toString(),
                        onValueChange = { newValue ->
                            val newDay = newValue.toIntOrNull() ?: 1
                            onOptionsChange(options.copy(absoluteDay = newDay))
                        },
                        modifier = Modifier.width(60.dp),
                        label = "",
                        maxLength = 2,
                        onFocusLost = {
                            val validatedDay = options.absoluteDay.coerceIn(1, 31)
                            if (options.absoluteDay != validatedDay) {
                                onOptionsChange(options.copy(absoluteDay = validatedDay))
                            }
                        }
                    )
                    Text("of every", modifier = Modifier.padding(horizontal = 8.dp))
                    NumberInputField(
                        value = options.interval.toString(),
                        onValueChange = { newValue ->
                            val newInterval = newValue.toIntOrNull() ?: 1
                            onOptionsChange(options.copy(interval = newInterval))
                        },
                        modifier = Modifier.width(60.dp),
                        label = "",
                        maxLength = 2,
                        onFocusLost = {
                            val validatedInterval = options.interval.coerceAtLeast(1)
                            if (options.interval != validatedInterval) {
                                onOptionsChange(options.copy(interval = validatedInterval))
                            }
                        }
                    )
                    Text("month(s)")
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Spacer(modifier = Modifier.height(8.dp))

        // Option B: Relative day
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = options.monthlyType == "relative",
                    onClick = { onOptionsChange(options.copy(monthlyType = "relative")) }
                )
                Text("On the", modifier = Modifier.padding(start = 4.dp))
            }

            if (options.monthlyType == "relative") {
                Column(modifier = Modifier.padding(start = 48.dp)) {
                    // Week selection
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Week:", modifier = Modifier.width(60.dp))

                        val weeks = listOf("First", "Second", "Third", "Fourth", "Last")
                        var showWeekDropdown by remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(
                            expanded = showWeekDropdown,
                            onExpandedChange = { showWeekDropdown = !showWeekDropdown }
                        ) {
                            OutlinedTextField(
                                value = options.relativeWeek.replaceFirstChar { it.titlecase() },
                                onValueChange = {},
                                readOnly = true,
                                modifier = Modifier
                                    .menuAnchor()
                                    .width(120.dp)
                            )
                            ExposedDropdownMenu(
                                expanded = showWeekDropdown,
                                onDismissRequest = { showWeekDropdown = false }
                            ) {
                                weeks.forEach { week ->
                                    DropdownMenuItem(
                                        text = { Text(week) },
                                        onClick = {
                                            onOptionsChange(options.copy(relativeWeek = week.lowercase()))
                                            showWeekDropdown = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Day selection
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Day:", modifier = Modifier.width(60.dp))

                        val daysOfWeek = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
                        var showDayDropdown by remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(
                            expanded = showDayDropdown,
                            onExpandedChange = { showDayDropdown = !showDayDropdown }
                        ) {
                            OutlinedTextField(
                                value = options.relativeDay.replaceFirstChar { it.titlecase() },
                                onValueChange = {},
                                readOnly = true,
                                modifier = Modifier
                                    .menuAnchor()
                                    .width(120.dp)
                            )
                            ExposedDropdownMenu(
                                expanded = showDayDropdown,
                                onDismissRequest = { showDayDropdown = false }
                            ) {
                                daysOfWeek.forEach { day ->
                                    DropdownMenuItem(
                                        text = { Text(day) },
                                        onClick = {
                                            onOptionsChange(options.copy(relativeDay = day.lowercase()))
                                            showDayDropdown = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Interval
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Every", modifier = Modifier.width(60.dp))
                        OutlinedTextField(
                            value = options.interval.toString(),
                            onValueChange = {
                                val newInterval = it.toIntOrNull() ?: 1
                                onOptionsChange(options.copy(interval = newInterval))
                            },
                            modifier = Modifier.width(60.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                        Text("month(s)", modifier = Modifier.padding(start = 8.dp))
                    }
                }
            }
        }
    }
}
