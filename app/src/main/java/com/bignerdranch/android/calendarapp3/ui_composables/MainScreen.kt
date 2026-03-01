package com.bignerdranch.android.calendarapp3.ui_composables

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.kizitonwose.calendar.compose.HorizontalCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.core.daysOfWeek
import com.kizitonwose.calendar.core.firstDayOfWeekFromLocale
import java.time.YearMonth
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.bignerdranch.android.calendarapp3.ui_composables.month_view.Day
import com.bignerdranch.android.calendarapp3.ui_composables.month_view.DaysOfWeekTitle
import com.bignerdranch.android.calendarapp3.ui_composables.month_view.DayContentsDialog
import com.bignerdranch.android.calendarapp3.ui_composables.month_view.YearAndMonthDisplay
import com.bignerdranch.android.calendarapp3.buisness_logic.CalendarViewModel
import com.bignerdranch.android.calendarapp3.buisness_logic.CouchbaseCalendarViewModel
import com.bignerdranch.android.calendarapp3.buisness_logic.EditEntryViewModel
import com.bignerdranch.android.calendarapp3.buisness_logic.NewEntryViewModel

import com.bignerdranch.android.calendarapp3.buisness_logic.objectbox.ObjectBoxEditEntryViewModel

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
    newEntryViewModel: NewEntryViewModel,
    editEntryViewModel: EditEntryViewModel,
    couchbaseCalendarViewModel: CouchbaseCalendarViewModel = viewModel(),
    objectBoxEditEntryViewModel: ObjectBoxEditEntryViewModel = viewModel()

) {
    val currentMonth = remember { YearMonth.now() }
    val startMonth = remember { currentMonth.minusMonths(100) }
    val endMonth = remember { currentMonth.plusMonths(100) }
    val firstDayOfWeek = remember { firstDayOfWeekFromLocale() }

    // Get the entries for the currently selected date
    val dateEntries by editEntryViewModel.dateEntries

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
            DayContentsDialog(
                onDismissRequest = {
                    viewModel.toggleDayContentDialog(false)
                    viewModel.onDateSelected(null)
                },
                onNewEntry = {
                    viewModel.toggleDayContentDialog(false)
                    navController.navigate("NewEntry/sqlite")
                },
                onEditEntry = { entry ->  // Now receives the entry directly
                    editEntryViewModel.selectedEntry = entry
                    navController.navigate("EditEntry/sqlite")
                },
                editEntryViewModel = editEntryViewModel,
                objectBoxEditEntryViewModel = objectBoxEditEntryViewModel,
                couchbaseCalendarViewModel = couchbaseCalendarViewModel,  // ADD THIS LINE
                eventList = dateEntries,// Use the date-specific entries

                        onNewEntryCouchbase = {
                    viewModel.toggleDayContentDialog(false)
                    navController.navigate("NewEntry/couchbase")
                },
                onEditEntryCouchbase = { ev ->
                    editEntryViewModel.selectedCouchbaseId = ev.id   // you’ll add this field
                    navController.navigate("EditEntry/couchbase")
                },

                onNewEntryObjectBox = {
                    viewModel.toggleDayContentDialog(false)
                    navController.navigate("NewEntry/objectbox")
                },
                onEditEntryObjectBox = { ev ->
                    // store the selected ObjectBox id somewhere (similar to Couchbase)
                    editEntryViewModel.selectedObjectBoxId = ev.id
                    navController.navigate("EditEntry/objectbox")
                },

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
                            editEntryViewModel.loadEntriesForDate(it.toString())
                        }
                    },
                    day = day,
                    isSelected = viewModel.selectedDate == day.date,
                    onDateSelect = { selectedDate ->
                        editEntryViewModel.saveSelectedDate(selectedDate.toString())
                        // Load entries immediately when date is selected
                        editEntryViewModel.loadEntriesForDate(selectedDate.toString())
                        objectBoxEditEntryViewModel.loadEntriesForDate(selectedDate.toString())
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






