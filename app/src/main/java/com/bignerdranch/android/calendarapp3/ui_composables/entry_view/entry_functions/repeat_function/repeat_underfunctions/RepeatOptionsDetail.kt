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
    var endDateDay: Int = 1,        // Make sure these exist
    var endDateMonth: Int = 1,      // Make sure these exist
    var endDateYear: Int = 2025,    // Make sure these exist
    var occurrences: Int = 10
)
