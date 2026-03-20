package com.bignerdranch.android.calendarapp3.ui_composables

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.kizitonwose.calendar.compose.HorizontalCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.core.daysOfWeek
import com.kizitonwose.calendar.core.firstDayOfWeekFromLocale
import java.time.YearMonth
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.bignerdranch.android.calendarapp3.benchmark.adapter.ObjectBoxCrudBenchmarkAdapter
import com.bignerdranch.android.calendarapp3.benchmark.adapter.RoomCrudBenchmarkAdapter
import com.bignerdranch.android.calendarapp3.benchmark.model.BenchmarkConfig
import com.bignerdranch.android.calendarapp3.benchmark.runner.BenchmarkRunner
import com.bignerdranch.android.calendarapp3.ui_composables.month_view.Day
import com.bignerdranch.android.calendarapp3.ui_composables.month_view.DaysOfWeekTitle
import com.bignerdranch.android.calendarapp3.ui_composables.month_view.DayContentsDialog
import com.bignerdranch.android.calendarapp3.ui_composables.month_view.YearAndMonthDisplay
import com.bignerdranch.android.calendarapp3.buisness_logic.CalendarViewModel
import com.bignerdranch.android.calendarapp3.buisness_logic.CouchbaseCalendarViewModel
import com.bignerdranch.android.calendarapp3.buisness_logic.EditEntryViewModel
import com.bignerdranch.android.calendarapp3.buisness_logic.NewEntryViewModel


import com.bignerdranch.android.calendarapp3.buisness_logic.objectbox.ObjectBoxEditEntryViewModel
import com.bignerdranch.android.calendarapp3.database.AppDatabase
import kotlinx.coroutines.launch

import com.bignerdranch.android.calendarapp3.database.objectbox.ObjectBoxProvider
import com.bignerdranch.android.calendarapp3.benchmark.objectbox.BenchmarkObjectBoxEntity

import com.bignerdranch.android.calendarapp3.benchmark.adapter.CouchbaseCrudBenchmarkAdapter
import com.bignerdranch.android.calendarapp3.benchmark.couchbase.CouchbaseBenchmarkDao

/*
I implemented viewModel to seperate the UI design elements (composables) from business logic elements.
I also make sure the app remembers (saves) certain state data when recomposition and/or system changes
(screen rotation) occur
 */

// this is the main screen composable that calls many of the other relevant composables for the month view
@Composable
fun MainScreen(
    navController: NavController,
    viewModel: CalendarViewModel = viewModel(),
    editEntryViewModel: EditEntryViewModel,
    // it shows newEntryViewModel as unused, this is false, its used in ScreenNavigation
    newEntryViewModel: NewEntryViewModel,
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


    // for benchmarking
    var benchmarkStatus by remember { mutableStateOf("Idle") }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

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
                onEditEntry = { entry ->  // Receives the entry directly
                    editEntryViewModel.selectedEntry = entry
                    navController.navigate("EditEntry/sqlite")
                },
                editEntryViewModel = editEntryViewModel,
                objectBoxEditEntryViewModel = objectBoxEditEntryViewModel,
                couchbaseCalendarViewModel = couchbaseCalendarViewModel,
                eventList = dateEntries, // Use the date-specific entries

                        onNewEntryCouchbase = {
                    viewModel.toggleDayContentDialog(false)
                    navController.navigate("NewEntry/couchbase")
                },
                onEditEntryCouchbase = { ev ->
                    editEntryViewModel.selectedCouchbaseId = ev.id
                    navController.navigate("EditEntry/couchbase")
                },

                onNewEntryObjectBox = {
                    viewModel.toggleDayContentDialog(false)
                    navController.navigate("NewEntry/objectbox")
                },
                onEditEntryObjectBox = { ev ->
                    // Store the selected ObjectBox id (Similar to couchbase lite)
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
                        // saveSelectedDate and loadEntriesForDate both requre a String, so don't delete the converters toString()
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

        Button(
            onClick = {
                scope.launch {
                    benchmarkStatus = "Running benchmark..."

                    try {
                        val db = AppDatabase.getDatabase(context)
                        val dao = db.benchmarkRoomDao()
                        val adapter = RoomCrudBenchmarkAdapter(dao)
                        val runner = BenchmarkRunner(
                            databaseName = "Room",
                            adapter = adapter
                        )

                        val result = runner.runBasicCrudBenchmark(
                            BenchmarkConfig(
                                entryCount = 1000,
                                warmupRuns = 2,
                                measuredRuns = 5
                            )
                        )

                        benchmarkStatus =
                            "Done. Insert avg: ${result.insertAverageMs} ms, Read avg: ${result.readAllAverageMs} ms"
                    } catch (e: Exception) {
                        benchmarkStatus = "Failed: ${e.message}"
                        Log.e("BENCHMARK_UI", "Benchmark failed", e)
                    }
                }
            }
        ) {
            Text("Run Room Benchmark")
        }

        Button(
            onClick = {
                scope.launch {
                    benchmarkStatus = "Running ObjectBox benchmark..."

                    try {
                        val boxStore = ObjectBoxProvider.get()
                        val box = boxStore.boxFor(BenchmarkObjectBoxEntity::class.java)

                        val adapter = ObjectBoxCrudBenchmarkAdapter(box)
                        val runner = BenchmarkRunner(
                            databaseName = "ObjectBox",
                            adapter = adapter
                        )

                        val result = runner.runBasicCrudBenchmark(
                            BenchmarkConfig(
                                entryCount = 1000,
                                warmupRuns = 2,
                                measuredRuns = 5
                            )
                        )

                        benchmarkStatus =
                            "ObjectBox done. Insert avg: ${result.insertAverageMs} ms"
                    } catch (e: Exception) {
                        benchmarkStatus = "ObjectBox failed: ${e.message}"
                        Log.e("BENCHMARK_UI", "ObjectBox benchmark failed", e)
                    }
                }
            }
        ) {
            Text("Run ObjectBox Benchmark")
        }

        Button(
            onClick = {
                scope.launch {
                    benchmarkStatus = "Running Couchbase benchmark..."

                    try {
                        val dao = CouchbaseBenchmarkDao.getInstance(context)
                        val adapter = CouchbaseCrudBenchmarkAdapter(dao)
                        val runner = BenchmarkRunner(
                            databaseName = "Couchbase",
                            adapter = adapter
                        )

                        val result = runner.runBasicCrudBenchmark(
                            BenchmarkConfig(
                                entryCount = 1000,
                                warmupRuns = 2,
                                measuredRuns = 5
                            )
                        )

                        benchmarkStatus =
                            "Couchbase done. Insert avg: ${result.insertAverageMs} ms"
                    } catch (e: Exception) {
                        benchmarkStatus = "Couchbase failed: ${e.message}"
                        Log.e("BENCHMARK_UI", "Couchbase benchmark failed", e)
                    }
                }
            }
        ) {
            Text("Run Couchbase Benchmark")
        }

        Text(text = benchmarkStatus)
    }
}






