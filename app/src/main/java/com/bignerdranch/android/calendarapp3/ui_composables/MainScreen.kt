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
import com.bignerdranch.android.calendarapp3.benchmark.model.ReadByIdBenchmarkConfig

import com.bignerdranch.android.calendarapp3.benchmark.model.ReadOrderedBenchmarkConfig

import com.bignerdranch.android.calendarapp3.benchmark.model.ReadInRangeBenchmarkConfig

// for benchmark button scrollability
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.saveable.rememberSaveable

import com.bignerdranch.android.calendarapp3.benchmark.model.UpdateBenchmarkConfig

import com.bignerdranch.android.calendarapp3.benchmark.model.DeleteBenchmarkConfig

import com.bignerdranch.android.calendarapp3.benchmark.adapter.RoomIndexedCrudBenchmarkAdapter



import androidx.compose.material3.OutlinedButton
import androidx.compose.foundation.layout.Row

import com.bignerdranch.android.calendarapp3.benchmark.adapter.ObjectBoxIndexedCrudBenchmarkAdapter
import com.bignerdranch.android.calendarapp3.benchmark.model.BenchmarkObjectBoxIndexedEntity


/*
I implemented viewModel to seperate the UI design elements (composables) from business logic elements.
I also make sure the app remembers (saves) certain state data when recomposition and/or system changes
(screen rotation) occur
 */

// this is the main screen composable that calls many of the other relevant composables for the month view

private data class BenchmarkButtonItem(
    val label: String,
    val onClick: suspend () -> Unit
)

@Composable
private fun BenchmarkSection(
    title: String,
    buttons: List<Pair<String, () -> Unit>>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        buttons.forEach { (label, onClick) ->
            Button(
                onClick = onClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(label)
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
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

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = benchmarkStatus,
            modifier = Modifier.fillMaxWidth()
        )

        var showIndexedBenchmarks by rememberSaveable { mutableStateOf(false) }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (!showIndexedBenchmarks) {
                Button(
                    onClick = { showIndexedBenchmarks = false },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("No Index")
                }
            } else {
                OutlinedButton(
                    onClick = { showIndexedBenchmarks = false },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("No Index")
                }
            }

            if (showIndexedBenchmarks) {
                Button(
                    onClick = { showIndexedBenchmarks = true },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Indexed")
                }
            } else {
                OutlinedButton(
                    onClick = { showIndexedBenchmarks = true },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Indexed")
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                BenchmarkSection(
                    title = "Basic CRUD Benchmark",
                    buttons = listOf(
                        "Run Room Benchmark" to {
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
                                            entryCount = 5000,
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
                        },
                        "Run ObjectBox Benchmark" to {
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
                                            entryCount = 5000,
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
                        },
                        "Run Couchbase Benchmark" to {
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
                                            entryCount = 5000,
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
                    )
                )
            }

            item {
                BenchmarkSection(
                    title = "Read-by-ID Benchmark",
                    buttons = listOf(
                        "Run Room Read-by-ID" to {
                            scope.launch {
                                benchmarkStatus = "Running Room read-by-ID benchmark..."

                                try {
                                    val db = AppDatabase.getDatabase(context)
                                    val dao = db.benchmarkRoomDao()
                                    val adapter = RoomCrudBenchmarkAdapter(dao)
                                    val runner = BenchmarkRunner(
                                        databaseName = "Room",
                                        adapter = adapter
                                    )

                                    val result = runner.runReadByIdBenchmark(
                                        ReadByIdBenchmarkConfig(
                                            entryCount = 1000,
                                            lookupsPerRun = 100,
                                            warmupRuns = 2,
                                            measuredRuns = 5
                                        )
                                    )

                                    benchmarkStatus =
                                        "Room read-by-ID done. Avg: ${result.lookupAverageMs} ms"
                                } catch (e: Exception) {
                                    benchmarkStatus = "Room read-by-ID failed: ${e.message}"
                                    Log.e("BENCHMARK_UI", "Room read-by-ID benchmark failed", e)
                                }
                            }
                        },
                        "Run ObjectBox Read-by-ID" to {
                            scope.launch {
                                benchmarkStatus = "Running ObjectBox read-by-ID benchmark..."

                                try {
                                    ObjectBoxProvider.init(context)
                                    val boxStore = ObjectBoxProvider.get()
                                    val box = boxStore.boxFor(BenchmarkObjectBoxEntity::class.java)

                                    val adapter = ObjectBoxCrudBenchmarkAdapter(box)
                                    val runner = BenchmarkRunner(
                                        databaseName = "ObjectBox",
                                        adapter = adapter
                                    )

                                    val result = runner.runReadByIdBenchmark(
                                        ReadByIdBenchmarkConfig(
                                            entryCount = 1000,
                                            lookupsPerRun = 100,
                                            warmupRuns = 2,
                                            measuredRuns = 5
                                        )
                                    )

                                    benchmarkStatus =
                                        "ObjectBox read-by-ID done. Avg: ${result.lookupAverageMs} ms"
                                } catch (e: Exception) {
                                    benchmarkStatus = "ObjectBox read-by-ID failed: ${e.message}"
                                    Log.e("BENCHMARK_UI", "ObjectBox read-by-ID benchmark failed", e)
                                }
                            }
                        },
                        "Run Couchbase Read-by-ID" to {
                            scope.launch {
                                benchmarkStatus = "Running Couchbase read-by-ID benchmark..."

                                try {
                                    val dao = CouchbaseBenchmarkDao.getInstance(context)
                                    val adapter = CouchbaseCrudBenchmarkAdapter(dao)
                                    val runner = BenchmarkRunner(
                                        databaseName = "Couchbase",
                                        adapter = adapter
                                    )

                                    val result = runner.runReadByIdBenchmark(
                                        ReadByIdBenchmarkConfig(
                                            entryCount = 1000,
                                            lookupsPerRun = 100,
                                            warmupRuns = 2,
                                            measuredRuns = 5
                                        )
                                    )

                                    benchmarkStatus =
                                        "Couchbase read-by-ID done. Avg: ${result.lookupAverageMs} ms"
                                } catch (e: Exception) {
                                    benchmarkStatus = "Couchbase read-by-ID failed: ${e.message}"
                                    Log.e("BENCHMARK_UI", "Couchbase read-by-ID benchmark failed", e)
                                }
                            }
                        }
                    )
                )
            }

            item {
                BenchmarkSection(
                    title = "Ordered Read Benchmark",
                    buttons = listOf(
                        "Run Room Ordered Read" to {
                            scope.launch {
                                benchmarkStatus = "Running Room ordered-read benchmark..."

                                try {
                                    val db = AppDatabase.getDatabase(context)
                                    val dao = db.benchmarkRoomDao()
                                    val adapter = RoomCrudBenchmarkAdapter(dao)
                                    val runner = BenchmarkRunner(
                                        databaseName = "Room",
                                        adapter = adapter
                                    )

                                    val result = runner.runReadOrderedBenchmark(
                                        ReadOrderedBenchmarkConfig(
                                            entryCount = 1000,
                                            warmupRuns = 2,
                                            measuredRuns = 5
                                        )
                                    )

                                    benchmarkStatus =
                                        "Room ordered-read done. Avg: ${result.readAverageMs} ms"
                                } catch (e: Exception) {
                                    benchmarkStatus = "Room ordered-read failed: ${e.message}"
                                    Log.e("BENCHMARK_UI", "Room ordered-read benchmark failed", e)
                                }
                            }
                        },
                        "Run ObjectBox Ordered Read" to {
                            scope.launch {
                                benchmarkStatus = "Running ObjectBox ordered-read benchmark..."

                                try {
                                    ObjectBoxProvider.init(context)
                                    val boxStore = ObjectBoxProvider.get()
                                    val box = boxStore.boxFor(BenchmarkObjectBoxEntity::class.java)

                                    val adapter = ObjectBoxCrudBenchmarkAdapter(box)
                                    val runner = BenchmarkRunner(
                                        databaseName = "ObjectBox",
                                        adapter = adapter
                                    )

                                    val result = runner.runReadOrderedBenchmark(
                                        ReadOrderedBenchmarkConfig(
                                            entryCount = 1000,
                                            warmupRuns = 2,
                                            measuredRuns = 5
                                        )
                                    )

                                    benchmarkStatus =
                                        "ObjectBox ordered-read done. Avg: ${result.readAverageMs} ms"
                                } catch (e: Exception) {
                                    benchmarkStatus = "ObjectBox ordered-read failed: ${e.message}"
                                    Log.e("BENCHMARK_UI", "ObjectBox ordered-read benchmark failed", e)
                                }
                            }
                        },
                        "Run Couchbase Ordered Read" to {
                            scope.launch {
                                benchmarkStatus = "Running Couchbase ordered-read benchmark..."

                                try {
                                    val dao = CouchbaseBenchmarkDao.getInstance(context)
                                    val adapter = CouchbaseCrudBenchmarkAdapter(dao)
                                    val runner = BenchmarkRunner(
                                        databaseName = "Couchbase",
                                        adapter = adapter
                                    )

                                    val result = runner.runReadOrderedBenchmark(
                                        ReadOrderedBenchmarkConfig(
                                            entryCount = 1000,
                                            warmupRuns = 2,
                                            measuredRuns = 5
                                        )
                                    )

                                    benchmarkStatus =
                                        "Couchbase ordered-read done. Avg: ${result.readAverageMs} ms"
                                } catch (e: Exception) {
                                    benchmarkStatus = "Couchbase ordered-read failed: ${e.message}"
                                    Log.e("BENCHMARK_UI", "Couchbase ordered-read benchmark failed", e)
                                }
                            }
                        }
                    )
                )
            }



            item {
                BenchmarkSection(
                    title = if (showIndexedBenchmarks) {
                        "Range Read Benchmark (Indexed)"
                    } else {
                        "Range Read Benchmark (No Index)"
                    },
                    buttons = if (!showIndexedBenchmarks) {
                        listOf(
                            "Run Room Range Read" to {
                                scope.launch {
                                    benchmarkStatus = "Running Room range-read benchmark..."

                                    try {
                                        val db = AppDatabase.getDatabase(context)
                                        val dao = db.benchmarkRoomDao()
                                        val adapter = RoomCrudBenchmarkAdapter(dao)
                                        val runner = BenchmarkRunner(
                                            databaseName = "Room_NoIndex",
                                            adapter = adapter
                                        )

                                        val result = runner.runReadInRangeBenchmark(
                                            ReadInRangeBenchmarkConfig(
                                                entryCount = 5000,
                                                rangeStartIndex = 1500,
                                                rangeSize = 500,
                                                warmupRuns = 2,
                                                measuredRuns = 5
                                            )
                                        )

                                        benchmarkStatus =
                                            "Room (no index) range-read done. Avg: ${result.readAverageMs} ms"
                                    } catch (e: Exception) {
                                        benchmarkStatus = "Room (no index) range-read failed: ${e.message}"
                                        Log.e("BENCHMARK_UI", "Room no-index range-read benchmark failed", e)
                                    }
                                }
                            },

                            "Run ObjectBox Range Read" to {
                                scope.launch {
                                    benchmarkStatus = "Running ObjectBox range-read benchmark (no index)..."

                                    try {
                                        ObjectBoxProvider.init(context)
                                        val boxStore = ObjectBoxProvider.get()
                                        val box = boxStore.boxFor(BenchmarkObjectBoxEntity::class.java)

                                        val adapter = ObjectBoxCrudBenchmarkAdapter(box)
                                        val runner = BenchmarkRunner(
                                            databaseName = "ObjectBox_NoIndex",
                                            adapter = adapter
                                        )

                                        val result = runner.runReadInRangeBenchmark(
                                            ReadInRangeBenchmarkConfig(
                                                entryCount = 5000,
                                                rangeStartIndex = 1500,
                                                rangeSize = 500,
                                                warmupRuns = 2,
                                                measuredRuns = 5
                                            )
                                        )

                                        benchmarkStatus =
                                            "ObjectBox (no index) range-read done. Avg: ${result.readAverageMs} ms"
                                    } catch (e: Exception) {
                                        benchmarkStatus = "ObjectBox (no index) range-read failed: ${e.message}"
                                        Log.e("BENCHMARK_UI", "ObjectBox no-index range-read benchmark failed", e)
                                    }
                                }
                            },

                            "Run Couchbase Range Read" to {
                                scope.launch {
                                    benchmarkStatus = "Running Couchbase range-read benchmark (no index)..."

                                    try {
                                        val dao = CouchbaseBenchmarkDao.getInstance(context)

                                        // ensure NO index
                                        dao.removeStartMillisIndex()

                                        val adapter = CouchbaseCrudBenchmarkAdapter(dao)
                                        val runner = BenchmarkRunner(
                                            databaseName = "Couchbase_NoIndex",
                                            adapter = adapter
                                        )

                                        val result = runner.runReadInRangeBenchmark(
                                            ReadInRangeBenchmarkConfig(
                                                entryCount = 5000,
                                                rangeStartIndex = 1500,
                                                rangeSize = 500,
                                                warmupRuns = 2,
                                                measuredRuns = 5
                                            )
                                        )

                                        benchmarkStatus =
                                            "Couchbase (no index) range-read done. Avg: ${result.readAverageMs} ms"
                                    } catch (e: Exception) {
                                        benchmarkStatus = "Couchbase (no index) range-read failed: ${e.message}"
                                        Log.e("BENCHMARK_UI", "Couchbase no-index range-read benchmark failed", e)
                                    }
                                }
                            },





                        )
                    } else {
                        listOf(
                            "Run Room Range Read (Indexed)" to {
                                scope.launch {
                                    benchmarkStatus = "Running Room range-read benchmark (indexed)..."

                                    try {
                                        val db = AppDatabase.getDatabase(context)
                                        val dao = db.benchmarkRoomIndexedDao()
                                        val adapter = RoomIndexedCrudBenchmarkAdapter(dao)
                                        val runner = BenchmarkRunner(
                                            databaseName = "Room_Indexed",
                                            adapter = adapter
                                        )

                                        val result = runner.runReadInRangeBenchmark(
                                            ReadInRangeBenchmarkConfig(
                                                entryCount = 5000,
                                                rangeStartIndex = 1500,
                                                rangeSize = 500,
                                                warmupRuns = 2,
                                                measuredRuns = 5
                                            )
                                        )

                                        benchmarkStatus =
                                            "Room (indexed) range-read done. Avg: ${result.readAverageMs} ms"
                                    } catch (e: Exception) {
                                        benchmarkStatus = "Room (indexed) range-read failed: ${e.message}"
                                        Log.e("BENCHMARK_UI", "Room indexed range-read benchmark failed", e)
                                    }
                                }
                            },

                            "Run ObjectBox Range Read (Indexed)" to {
                                scope.launch {
                                    benchmarkStatus = "Running ObjectBox range-read benchmark (indexed)..."

                                    try {
                                        ObjectBoxProvider.init(context)
                                        val boxStore = ObjectBoxProvider.get()
                                        val box = boxStore.boxFor(BenchmarkObjectBoxIndexedEntity::class.java)

                                        val adapter = ObjectBoxIndexedCrudBenchmarkAdapter(box)
                                        val runner = BenchmarkRunner(
                                            databaseName = "ObjectBox_Indexed",
                                            adapter = adapter
                                        )

                                        val result = runner.runReadInRangeBenchmark(
                                            ReadInRangeBenchmarkConfig(
                                                entryCount = 5000,
                                                rangeStartIndex = 1500,
                                                rangeSize = 500,
                                                warmupRuns = 2,
                                                measuredRuns = 5
                                            )
                                        )

                                        benchmarkStatus =
                                            "ObjectBox (indexed) range-read done. Avg: ${result.readAverageMs} ms"
                                    } catch (e: Exception) {
                                        benchmarkStatus = "ObjectBox (indexed) range-read failed: ${e.message}"
                                        Log.e("BENCHMARK_UI", "ObjectBox indexed range-read benchmark failed", e)
                                    }
                                }
                            },

                            "Run Couchbase Range Read (Indexed)" to {
                                scope.launch {
                                    benchmarkStatus = "Running Couchbase range-read benchmark (indexed)..."

                                    try {
                                        val dao = CouchbaseBenchmarkDao.getInstance(context)
                                        dao.ensureStartMillisIndex()

                                        val adapter = CouchbaseCrudBenchmarkAdapter(dao)
                                        val runner = BenchmarkRunner(
                                            databaseName = "Couchbase_Indexed",
                                            adapter = adapter
                                        )

                                        val result = runner.runReadInRangeBenchmark(
                                            ReadInRangeBenchmarkConfig(
                                                entryCount = 5000,
                                                rangeStartIndex = 1500,
                                                rangeSize = 500,
                                                warmupRuns = 2,
                                                measuredRuns = 5
                                            )
                                        )

                                        benchmarkStatus =
                                            "Couchbase (indexed) range-read done. Avg: ${result.readAverageMs} ms"
                                    } catch (e: Exception) {
                                        benchmarkStatus = "Couchbase (indexed) range-read failed: ${e.message}"
                                        Log.e("BENCHMARK_UI", "Couchbase indexed range-read benchmark failed", e)
                                    }
                                }
                            }
                        )
                    }
                )
            }

            item {
                BenchmarkSection(
                    title = "Standalone Update Benchmark",
                    buttons = listOf(
                        "Run Room Update" to {
                            scope.launch {
                                benchmarkStatus = "Running Room update benchmark..."

                                try {
                                    val db = AppDatabase.getDatabase(context)
                                    val dao = db.benchmarkRoomDao()
                                    val adapter = RoomCrudBenchmarkAdapter(dao)
                                    val runner = BenchmarkRunner(
                                        databaseName = "Room",
                                        adapter = adapter
                                    )

                                    val result = runner.runUpdateBenchmark(
                                        UpdateBenchmarkConfig(
                                            entryCount = 1000,
                                            warmupRuns = 2,
                                            measuredRuns = 5
                                        )
                                    )

                                    benchmarkStatus =
                                        "Room update done. Avg: ${result.updateAverageMs} ms"
                                } catch (e: Exception) {
                                    benchmarkStatus = "Room update failed: ${e.message}"
                                    Log.e("BENCHMARK_UI", "Room update benchmark failed", e)
                                }
                            }
                        },

                        "Run ObjectBox Update" to {
                            scope.launch {
                                benchmarkStatus = "Running ObjectBox update benchmark..."

                                try {
                                    ObjectBoxProvider.init(context)
                                    val boxStore = ObjectBoxProvider.get()
                                    val box = boxStore.boxFor(BenchmarkObjectBoxEntity::class.java)

                                    val adapter = ObjectBoxCrudBenchmarkAdapter(box)
                                    val runner = BenchmarkRunner(
                                        databaseName = "ObjectBox",
                                        adapter = adapter
                                    )

                                    val result = runner.runUpdateBenchmark(
                                        UpdateBenchmarkConfig(
                                            entryCount = 1000,
                                            warmupRuns = 2,
                                            measuredRuns = 5
                                        )
                                    )

                                    benchmarkStatus =
                                        "ObjectBox update done. Avg: ${result.updateAverageMs} ms"
                                } catch (e: Exception) {
                                    benchmarkStatus = "ObjectBox update failed: ${e.message}"
                                    Log.e("BENCHMARK_UI", "ObjectBox update benchmark failed", e)
                                }
                            }
                        },

                        "Run Couchbase Update" to {
                            scope.launch {
                                benchmarkStatus = "Running Couchbase update benchmark..."

                                try {
                                    val dao = CouchbaseBenchmarkDao.getInstance(context)
                                    val adapter = CouchbaseCrudBenchmarkAdapter(dao)
                                    val runner = BenchmarkRunner(
                                        databaseName = "Couchbase",
                                        adapter = adapter
                                    )

                                    val result = runner.runUpdateBenchmark(
                                        UpdateBenchmarkConfig(
                                            entryCount = 1000,
                                            warmupRuns = 2,
                                            measuredRuns = 5
                                        )
                                    )

                                    benchmarkStatus =
                                        "Couchbase update done. Avg: ${result.updateAverageMs} ms"
                                } catch (e: Exception) {
                                    benchmarkStatus = "Couchbase update failed: ${e.message}"
                                    Log.e("BENCHMARK_UI", "Couchbase update benchmark failed", e)
                                }
                            }
                        }
                    )
                )
            }

            item {
                BenchmarkSection(
                    title = "Standalone Delete Benchmark",
                    buttons = listOf(
                        "Run Room Delete" to {
                            scope.launch {
                                benchmarkStatus = "Running Room delete benchmark..."

                                try {
                                    val db = AppDatabase.getDatabase(context)
                                    val dao = db.benchmarkRoomDao()
                                    val adapter = RoomCrudBenchmarkAdapter(dao)
                                    val runner = BenchmarkRunner(
                                        databaseName = "Room",
                                        adapter = adapter
                                    )

                                    val result = runner.runDeleteBenchmark(
                                        DeleteBenchmarkConfig(
                                            entryCount = 1000,
                                            warmupRuns = 2,
                                            measuredRuns = 5
                                        )
                                    )

                                    benchmarkStatus =
                                        "Room delete done. Avg: ${result.deleteAverageMs} ms"
                                } catch (e: Exception) {
                                    benchmarkStatus = "Room delete failed: ${e.message}"
                                    Log.e("BENCHMARK_UI", "Room delete benchmark failed", e)
                                }
                            }
                        },

                        "Run ObjectBox Delete" to {
                            scope.launch {
                                benchmarkStatus = "Running ObjectBox delete benchmark..."

                                try {
                                    ObjectBoxProvider.init(context)
                                    val boxStore = ObjectBoxProvider.get()
                                    val box = boxStore.boxFor(BenchmarkObjectBoxEntity::class.java)

                                    val adapter = ObjectBoxCrudBenchmarkAdapter(box)
                                    val runner = BenchmarkRunner(
                                        databaseName = "ObjectBox",
                                        adapter = adapter
                                    )

                                    val result = runner.runDeleteBenchmark(
                                        DeleteBenchmarkConfig(
                                            entryCount = 1000,
                                            warmupRuns = 2,
                                            measuredRuns = 5
                                        )
                                    )

                                    benchmarkStatus =
                                        "ObjectBox delete done. Avg: ${result.deleteAverageMs} ms"
                                } catch (e: Exception) {
                                    benchmarkStatus = "ObjectBox delete failed: ${e.message}"
                                    Log.e("BENCHMARK_UI", "ObjectBox delete benchmark failed", e)
                                }
                            }
                        },

                        "Run Couchbase Delete" to {
                            scope.launch {
                                benchmarkStatus = "Running Couchbase delete benchmark..."

                                try {
                                    val dao = CouchbaseBenchmarkDao.getInstance(context)
                                    val adapter = CouchbaseCrudBenchmarkAdapter(dao)
                                    val runner = BenchmarkRunner(
                                        databaseName = "Couchbase",
                                        adapter = adapter
                                    )

                                    val result = runner.runDeleteBenchmark(
                                        DeleteBenchmarkConfig(
                                            entryCount = 1000,
                                            warmupRuns = 2,
                                            measuredRuns = 5
                                        )
                                    )

                                    benchmarkStatus =
                                        "Couchbase delete done. Avg: ${result.deleteAverageMs} ms"
                                } catch (e: Exception) {
                                    benchmarkStatus = "Couchbase delete failed: ${e.message}"
                                    Log.e("BENCHMARK_UI", "Couchbase delete benchmark failed", e)
                                }
                            }
                        }
                    )
                )
            }
        }

        ////////////////////////////////////////////////////////////////////////////////////////////

        /*
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
                                entryCount = 5000,
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
                                entryCount = 5000,
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
                                entryCount = 5000,
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



        //__________________________________________________________________________________________
        Button(
            onClick = {
                scope.launch {
                    benchmarkStatus = "Running Room read-by-ID benchmark..."

                    try {
                        val db = AppDatabase.getDatabase(context)
                        val dao = db.benchmarkRoomDao()
                        val adapter = RoomCrudBenchmarkAdapter(dao)
                        val runner = BenchmarkRunner(
                            databaseName = "Room",
                            adapter = adapter
                        )

                        val result = runner.runReadByIdBenchmark(
                            ReadByIdBenchmarkConfig(
                                entryCount = 1000,
                                lookupsPerRun = 100,
                                warmupRuns = 2,
                                measuredRuns = 5
                            )
                        )

                        benchmarkStatus =
                            "Room read-by-ID done. Avg: ${result.lookupAverageMs} ms"
                    } catch (e: Exception) {
                        benchmarkStatus = "Room read-by-ID failed: ${e.message}"
                        Log.e("BENCHMARK_UI", "Room read-by-ID benchmark failed", e)
                    }
                }
            }
        ) {
            Text("Run Room Read-by-ID")
        }


        Button(
            onClick = {
                scope.launch {
                    benchmarkStatus = "Running ObjectBox read-by-ID benchmark..."

                    try {
                        ObjectBoxProvider.init(context)
                        val boxStore = ObjectBoxProvider.get()
                        val box = boxStore.boxFor(BenchmarkObjectBoxEntity::class.java)

                        val adapter = ObjectBoxCrudBenchmarkAdapter(box)
                        val runner = BenchmarkRunner(
                            databaseName = "ObjectBox",
                            adapter = adapter
                        )

                        val result = runner.runReadByIdBenchmark(
                            ReadByIdBenchmarkConfig(
                                entryCount = 1000,
                                lookupsPerRun = 100,
                                warmupRuns = 2,
                                measuredRuns = 5
                            )
                        )

                        benchmarkStatus =
                            "ObjectBox read-by-ID done. Avg: ${result.lookupAverageMs} ms"
                    } catch (e: Exception) {
                        benchmarkStatus = "ObjectBox read-by-ID failed: ${e.message}"
                        Log.e("BENCHMARK_UI", "ObjectBox read-by-ID benchmark failed", e)
                    }
                }
            }
        ) {
            Text("Run ObjectBox Read-by-ID")
        }


        Button(
            onClick = {
                scope.launch {
                    benchmarkStatus = "Running Couchbase read-by-ID benchmark..."

                    try {
                        val dao = CouchbaseBenchmarkDao.getInstance(context)
                        val adapter = CouchbaseCrudBenchmarkAdapter(dao)
                        val runner = BenchmarkRunner(
                            databaseName = "Couchbase",
                            adapter = adapter
                        )

                        val result = runner.runReadByIdBenchmark(
                            ReadByIdBenchmarkConfig(
                                entryCount = 1000,
                                lookupsPerRun = 100,
                                warmupRuns = 2,
                                measuredRuns = 5
                            )
                        )

                        benchmarkStatus =
                            "Couchbase read-by-ID done. Avg: ${result.lookupAverageMs} ms"
                    } catch (e: Exception) {
                        benchmarkStatus = "Couchbase read-by-ID failed: ${e.message}"
                        Log.e("BENCHMARK_UI", "Couchbase read-by-ID benchmark failed", e)
                    }
                }
            }
        ) {
            Text("Run Couchbase Read-by-ID")
        }


        //__________________________________________________________________________________________

        Button(
            onClick = {
                scope.launch {
                    benchmarkStatus = "Running Room ordered-read benchmark..."

                    try {
                        val db = AppDatabase.getDatabase(context)
                        val dao = db.benchmarkRoomDao()
                        val adapter = RoomCrudBenchmarkAdapter(dao)
                        val runner = BenchmarkRunner(
                            databaseName = "Room",
                            adapter = adapter
                        )

                        val result = runner.runReadOrderedBenchmark(
                            ReadOrderedBenchmarkConfig(
                                entryCount = 1000,
                                warmupRuns = 2,
                                measuredRuns = 5
                            )
                        )

                        benchmarkStatus =
                            "Room ordered-read done. Avg: ${result.readAverageMs} ms"
                    } catch (e: Exception) {
                        benchmarkStatus = "Room ordered-read failed: ${e.message}"
                        Log.e("BENCHMARK_UI", "Room ordered-read benchmark failed", e)
                    }
                }
            }
        ) {
            Text("Run Room Ordered Read")
        }


        Button(
            onClick = {
                scope.launch {
                    benchmarkStatus = "Running ObjectBox ordered-read benchmark..."

                    try {
                        ObjectBoxProvider.init(context)
                        val boxStore = ObjectBoxProvider.get()
                        val box = boxStore.boxFor(BenchmarkObjectBoxEntity::class.java)

                        val adapter = ObjectBoxCrudBenchmarkAdapter(box)
                        val runner = BenchmarkRunner(
                            databaseName = "ObjectBox",
                            adapter = adapter
                        )

                        val result = runner.runReadOrderedBenchmark(
                            ReadOrderedBenchmarkConfig(
                                entryCount = 1000,
                                warmupRuns = 2,
                                measuredRuns = 5
                            )
                        )

                        benchmarkStatus =
                            "ObjectBox ordered-read done. Avg: ${result.readAverageMs} ms"
                    } catch (e: Exception) {
                        benchmarkStatus = "ObjectBox ordered-read failed: ${e.message}"
                        Log.e("BENCHMARK_UI", "ObjectBox ordered-read benchmark failed", e)
                    }
                }
            }
        ) {
            Text("Run ObjectBox Ordered Read")
        }



        Button(
            onClick = {
                scope.launch {
                    benchmarkStatus = "Running Couchbase ordered-read benchmark..."

                    try {
                        val dao = CouchbaseBenchmarkDao.getInstance(context)
                        val adapter = CouchbaseCrudBenchmarkAdapter(dao)
                        val runner = BenchmarkRunner(
                            databaseName = "Couchbase",
                            adapter = adapter
                        )

                        val result = runner.runReadOrderedBenchmark(
                            ReadOrderedBenchmarkConfig(
                                entryCount = 1000,
                                warmupRuns = 2,
                                measuredRuns = 5
                            )
                        )

                        benchmarkStatus =
                            "Couchbase ordered-read done. Avg: ${result.readAverageMs} ms"
                    } catch (e: Exception) {
                        benchmarkStatus = "Couchbase ordered-read failed: ${e.message}"
                        Log.e("BENCHMARK_UI", "Couchbase ordered-read benchmark failed", e)
                    }
                }
            }
        ) {
            Text("Run Couchbase Ordered Read")
        }

        //__________________________________________________________________________________________

        Button(
            onClick = {
                scope.launch {
                    benchmarkStatus = "Running Room range-read benchmark..."

                    try {
                        val db = AppDatabase.getDatabase(context)
                        val dao = db.benchmarkRoomDao()
                        val adapter = RoomCrudBenchmarkAdapter(dao)
                        val runner = BenchmarkRunner(
                            databaseName = "Room",
                            adapter = adapter
                        )

                        val result = runner.runReadInRangeBenchmark(
                            ReadInRangeBenchmarkConfig(
                                entryCount = 1000,
                                rangeStartIndex = 300,
                                rangeSize = 100,
                                warmupRuns = 2,
                                measuredRuns = 5
                            )
                        )

                        benchmarkStatus =
                            "Room range-read done. Avg: ${result.readAverageMs} ms"
                    } catch (e: Exception) {
                        benchmarkStatus = "Room range-read failed: ${e.message}"
                        Log.e("BENCHMARK_UI", "Room range-read benchmark failed", e)
                    }
                }
            }
        ) {
            Text("Run Room Range Read")
        }


        Button(
            onClick = {
                scope.launch {
                    benchmarkStatus = "Running ObjectBox range-read benchmark..."

                    try {
                        ObjectBoxProvider.init(context)
                        val boxStore = ObjectBoxProvider.get()
                        val box = boxStore.boxFor(BenchmarkObjectBoxEntity::class.java)

                        val adapter = ObjectBoxCrudBenchmarkAdapter(box)
                        val runner = BenchmarkRunner(
                            databaseName = "ObjectBox",
                            adapter = adapter
                        )

                        val result = runner.runReadInRangeBenchmark(
                            ReadInRangeBenchmarkConfig(
                                entryCount = 1000,
                                rangeStartIndex = 300,
                                rangeSize = 100,
                                warmupRuns = 2,
                                measuredRuns = 5
                            )
                        )

                        benchmarkStatus =
                            "ObjectBox range-read done. Avg: ${result.readAverageMs} ms"
                    } catch (e: Exception) {
                        benchmarkStatus = "ObjectBox range-read failed: ${e.message}"
                        Log.e("BENCHMARK_UI", "ObjectBox range-read benchmark failed", e)
                    }
                }
            }
        ) {
            Text("Run ObjectBox Range Read")
        }


        Button(
            onClick = {
                scope.launch {
                    benchmarkStatus = "Running Couchbase range-read benchmark..."

                    try {
                        val dao = CouchbaseBenchmarkDao.getInstance(context)
                        val adapter = CouchbaseCrudBenchmarkAdapter(dao)
                        val runner = BenchmarkRunner(
                            databaseName = "Couchbase",
                            adapter = adapter
                        )

                        val result = runner.runReadInRangeBenchmark(
                            ReadInRangeBenchmarkConfig(
                                entryCount = 1000,
                                rangeStartIndex = 300,
                                rangeSize = 100,
                                warmupRuns = 2,
                                measuredRuns = 5
                            )
                        )

                        benchmarkStatus =
                            "Couchbase range-read done. Avg: ${result.readAverageMs} ms"
                    } catch (e: Exception) {
                        benchmarkStatus = "Couchbase range-read failed: ${e.message}"
                        Log.e("BENCHMARK_UI", "Couchbase range-read benchmark failed", e)
                    }
                }
            }
        ) {
            Text("Run Couchbase Range Read")
        }
        */
    }
}






