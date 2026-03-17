package com.bignerdranch.android.calendarapp3.ui_composables.entry_view.entry_functions.repeat_function.repeat_underfunctions.repeat_types

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bignerdranch.android.calendarapp3.ui_composables.entry_view.entry_functions.repeat_function.repeat_underfunctions.repeat_types.number_input_field.NumberInputField
import com.bignerdranch.android.calendarapp3.ui_composables.entry_view.entry_functions.repeat_function.repeat_underfunctions.RepeatOptions

//*
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
