package com.bignerdranch.android.calendarapp3.ui_composables.entry_view.entry_functions.repeat_function.repeat_underfunctions

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp



// Add these imports if not already present
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue

// Add this composable at the top of the file, before DailyRepeatOptions
@Composable
fun NumberInputField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "",
    maxLength: Int = 4,
    onFocusLost: (() -> Unit)? = null
) {
    var textFieldValue by remember(value) {
        mutableStateOf(TextFieldValue(value, selection = TextRange(value.length)))
    }
    var hasFocus by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = textFieldValue,
        onValueChange = { newValue ->
            // Allow only digits and limit length
            val newText = newValue.text.filter { char -> char.isDigit() }.take(maxLength)
            textFieldValue = newValue.copy(text = newText)
            onValueChange(newText)
        },
        modifier = modifier
            .onFocusChanged { focusState ->
                if (focusState.isFocused && !hasFocus) {
                    // Select all text when gaining focus
                    textFieldValue = textFieldValue.copy(
                        selection = TextRange(0, textFieldValue.text.length)
                    )
                } else if (hasFocus && !focusState.isFocused) {
                    // Validate when losing focus
                    onFocusLost?.invoke()
                }
                hasFocus = focusState.isFocused
            },
        label = { if (label.isNotEmpty()) Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true
    )
}

data class RepeatOptions(
    var interval: Int = 1,
    var selectedDays: Set<Int> = emptySet(),
    var monthlyType: String = "absolute",
    var absoluteDay: Int = 1,
    var relativeWeek: String = "first",
    var relativeDay: String = "monday",
    var month: Int = 1,
    var yearlyDay: Int = 1,
    var endType: String = "never",
    var endDateDay: Int = 1,        // Make sure these exist
    var endDateMonth: Int = 1,      // Make sure these exist
    var endDateYear: Int = 2024,    // Make sure these exist
    var occurrences: Int = 10
)

@Composable
fun RepeatOptionsDetail(
    repeatType: String,
    options: RepeatOptions,
    onOptionsChange: (RepeatOptions) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        when (repeatType) {
            "Daily" -> DailyRepeatOptions(options, onOptionsChange)
            "Weekly" -> WeeklyRepeatOptions(options, onOptionsChange)
            "Monthly" -> MonthlyRepeatOptions(options, onOptionsChange)
            "Yearly" -> YearlyRepeatOptions(options, onOptionsChange)
        }

        Spacer(modifier = Modifier.height(16.dp))
        EndOptions(options, onOptionsChange)
    }
}

@Composable
fun DailyRepeatOptions(
    options: RepeatOptions,
    onOptionsChange: (RepeatOptions) -> Unit
) {
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
        Text("day(s)")
    }
}

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun YearlyRepeatOptions(
    options: RepeatOptions,
    onOptionsChange: (RepeatOptions) -> Unit
) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Every", modifier = Modifier.width(60.dp))

            val months = listOf(
                "January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"
            )
            var showMonthDropdown by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = showMonthDropdown,
                onExpandedChange = { showMonthDropdown = !showMonthDropdown }
            ) {
                OutlinedTextField(
                    value = months[options.month - 1],
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier
                        .menuAnchor()
                        .width(120.dp)
                )
                ExposedDropdownMenu(
                    expanded = showMonthDropdown,
                    onDismissRequest = { showMonthDropdown = false }
                ) {
                    months.forEachIndexed { index, month ->
                        DropdownMenuItem(
                            text = { Text(month) },
                            onClick = {
                                onOptionsChange(options.copy(month = index + 1))
                                showMonthDropdown = false
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Day:", modifier = Modifier.width(60.dp))
            NumberInputField(
                value = options.yearlyDay.toString(),
                onValueChange = { newValue ->
                    val newDay = newValue.toIntOrNull() ?: 1
                    onOptionsChange(options.copy(yearlyDay = newDay))
                },
                modifier = Modifier.width(60.dp),
                label = "",
                maxLength = 2,
                onFocusLost = {
                    val validatedDay = options.yearlyDay.coerceIn(1, 31)
                    if (options.yearlyDay != validatedDay) {
                        onOptionsChange(options.copy(yearlyDay = validatedDay))
                    }
                }
            )
        }
    }
}

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