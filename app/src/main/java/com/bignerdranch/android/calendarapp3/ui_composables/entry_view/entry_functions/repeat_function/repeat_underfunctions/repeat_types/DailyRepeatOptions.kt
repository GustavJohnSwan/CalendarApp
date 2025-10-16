package com.bignerdranch.android.calendarapp3.ui_composables.entry_view.entry_functions.repeat_function.repeat_underfunctions.repeat_types

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bignerdranch.android.calendarapp3.ui_composables.entry_view.entry_functions.repeat_function.repeat_underfunctions.repeat_types.number_input_field.NumberInputField
import com.bignerdranch.android.calendarapp3.ui_composables.entry_view.entry_functions.repeat_function.repeat_underfunctions.RepeatOptions


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
