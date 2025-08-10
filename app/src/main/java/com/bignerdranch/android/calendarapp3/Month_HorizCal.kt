package com.bignerdranch.android.calendarapp3

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kizitonwose.calendar.compose.HorizontalCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.core.daysOfWeek
import com.kizitonwose.calendar.core.firstDayOfWeekFromLocale
import java.time.YearMonth
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import java.time.LocalDate

/*
I implemented viewModel to seperate the UI design elements (composablse) from business logic elements.
I also make sure the app remembers (saves) certain state data when recomposition and/or system changes
(screen rotation) occur
 */

// this is the main screen composable that calls many of the other relevant composables for the month view
@Composable
fun MainScreen(
    navController: NavController,
    viewModel: CalendarViewModel = viewModel(),
    entryTableViewModel: EntryTableViewModel,
    editEntryViewModel: EditEntryViewModel
) {
    val currentMonth = remember { YearMonth.now() }
    val startMonth = remember { currentMonth.minusMonths(100) }
    val endMonth = remember { currentMonth.plusMonths(100) }
    val firstDayOfWeek = remember { firstDayOfWeekFromLocale() }

    // Get the entries for the currently selected date
    val dateEntries by entryTableViewModel.dateEntries

    val state = rememberCalendarState(
        startMonth = startMonth,
        endMonth = endMonth,
        firstVisibleMonth = currentMonth,
        firstDayOfWeek = firstDayOfWeek
    )

    val daysOfWeek = daysOfWeek()

    Column {
        val displayedYear = state.firstVisibleMonth.yearMonth.year
        val displayedMonth = state.firstVisibleMonth.yearMonth.month.name.uppercase()

        YearAndMonthDisplay(displayedMonth = displayedMonth, displayedYear = displayedYear)

        if (viewModel.showDayContentDialog) {
            MinimalDialog(
                onDismissRequest = {
                    viewModel.toggleDayContentDialog(false)
                    viewModel.onDateSelected(null)
                },
                onNewEntry = {
                    viewModel.toggleDayContentDialog(false)
                    navController.navigate("NewEntry")
                },
                onEditEntry = { entry ->  // Now receives the entry directly
                    editEntryViewModel.selectedEntry = entry
                    navController.navigate("EditEntry")
                },
                editEntryViewModel = editEntryViewModel,
                eventList = dateEntries // Use the date-specific entries
            )
        }

        HorizontalCalendar(
            state = state,
            dayContent = { day ->
                Day(
                    onDateClick = {
                        viewModel.toggleDayContentDialog(true)
                        // Load entries when date is clicked
                        viewModel.selectedDate?.let {
                            entryTableViewModel.loadEntriesForDate(it.toString())
                        }
                    },
                    day = day,
                    isSelected = viewModel.selectedDate == day.date,
                    onDateSelect = { selectedDate ->
                        editEntryViewModel.saveSelectedDate(selectedDate.toString())
                        // Load entries immediately when date is selected
                        entryTableViewModel.loadEntriesForDate(selectedDate.toString())
                    }
                ) { day ->
                    viewModel.onDateSelected(day.date)
                }
            },
            monthHeader = {
                DaysOfWeekTitle(daysOfWeek = daysOfWeek)
            }
        )
    }
}






