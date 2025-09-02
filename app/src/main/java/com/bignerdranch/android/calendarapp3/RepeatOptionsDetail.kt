package com.bignerdranch.android.calendarapp3

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

data class RepeatOptions(
    var interval: Int = 1,
    var selectedDays: Set<Int> = emptySet(), // For weekly: 1=Mon, 2=Tue, etc.
    var monthlyType: String = "absolute", // "absolute" or "relative"
    var absoluteDay: Int = 1,
    var relativeWeek: String = "first", // first, second, third, fourth, last
    var relativeDay: String = "monday",
    var month: Int = 1, // For yearly: 1=Jan, 2=Feb, etc.
    var yearlyDay: Int = 1,
    var endType: String = "never", // never, on_date, after_occurrences
    val endDate: Long? = null,
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
        OutlinedTextField(
            value = options.interval.toString(),
            onValueChange = {
                val newInterval = it.toIntOrNull() ?: 1
                onOptionsChange(options.copy(interval = newInterval))
            },
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .width(60.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
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
            OutlinedTextField(
                value = options.interval.toString(),
                onValueChange = {
                    val newInterval = it.toIntOrNull() ?: 1
                    onOptionsChange(options.copy(interval = newInterval))
                },
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .width(60.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            Text("week(s) on:")
        }

        val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
        Row(modifier = Modifier.padding(top = 8.dp)) {
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
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(
                selected = options.monthlyType == "absolute",
                onClick = { onOptionsChange(options.copy(monthlyType = "absolute")) }
            )
            Text("On day")
            OutlinedTextField(
                value = options.absoluteDay.toString(),
                onValueChange = {
                    val newDay = it.toIntOrNull() ?: 1
                    onOptionsChange(options.copy(absoluteDay = newDay.coerceIn(1, 31)))
                },
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .width(60.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            Text("of every")
            OutlinedTextField(
                value = options.interval.toString(),
                onValueChange = {
                    val newInterval = it.toIntOrNull() ?: 1
                    onOptionsChange(options.copy(interval = newInterval))
                },
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .width(60.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            Text("month(s)")
        }

        // Option B: Relative day
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(
                selected = options.monthlyType == "relative",
                onClick = { onOptionsChange(options.copy(monthlyType = "relative")) }
            )
            Text("On the")

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
                        .width(100.dp)
                        .padding(horizontal = 8.dp)
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
                        .padding(horizontal = 8.dp)
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

            Text("of every")
            OutlinedTextField(
                value = options.interval.toString(),
                onValueChange = {
                    val newInterval = it.toIntOrNull() ?: 1
                    onOptionsChange(options.copy(interval = newInterval))
                },
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .width(60.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            Text("month(s)")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun YearlyRepeatOptions(
    options: RepeatOptions,
    onOptionsChange: (RepeatOptions) -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text("Every")

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
                    .padding(horizontal = 8.dp)
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

        OutlinedTextField(
            value = options.yearlyDay.toString(),
            onValueChange = {
                val newDay = it.toIntOrNull() ?: 1
                onOptionsChange(options.copy(yearlyDay = newDay.coerceIn(1, 31)))
            },
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .width(60.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
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
            Text("Never")
        }

        // On date option
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(
                selected = options.endType == "on_date",
                onClick = { onOptionsChange(options.copy(endType = "on_date")) }
            )
            Text("On")
            // TODO: Add date picker here
            Text("(date picker to be implemented)")
        }

        // After occurrences option
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(
                selected = options.endType == "after_occurrences",
                onClick = { onOptionsChange(options.copy(endType = "after_occurrences")) }
            )
            Text("After")
            OutlinedTextField(
                value = options.occurrences.toString(),
                onValueChange = {
                    val newOccurrences = it.toIntOrNull() ?: 1
                    onOptionsChange(options.copy(occurrences = newOccurrences))
                },
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .width(60.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            Text("occurrences")
        }
    }
}