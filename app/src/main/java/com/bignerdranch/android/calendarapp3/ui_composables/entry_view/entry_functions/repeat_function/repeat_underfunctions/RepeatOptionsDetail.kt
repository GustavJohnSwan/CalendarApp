package com.bignerdranch.android.calendarapp3.ui_composables.entry_view.entry_functions.repeat_function.repeat_underfunctions

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

import com.bignerdranch.android.calendarapp3.ui_composables.entry_view.entry_functions.repeat_function.repeat_underfunctions.repeat_options_underfunctions.EndOptions
import com.bignerdranch.android.calendarapp3.ui_composables.entry_view.entry_functions.repeat_function.repeat_underfunctions.repeat_types.DailyRepeatOptions
import com.bignerdranch.android.calendarapp3.ui_composables.entry_view.entry_functions.repeat_function.repeat_underfunctions.repeat_types.MonthlyRepeatOptions
import com.bignerdranch.android.calendarapp3.ui_composables.entry_view.entry_functions.repeat_function.repeat_underfunctions.repeat_types.WeeklyRepeatOptions
import com.bignerdranch.android.calendarapp3.ui_composables.entry_view.entry_functions.repeat_function.repeat_underfunctions.repeat_types.YearlyRepeatOptions


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
    var endDateDay: Int = 1,
    var endDateMonth: Int = 1,
    var endDateYear: Int = 2025,
    var occurrences: Int = 10
)
