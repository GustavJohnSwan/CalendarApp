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

/*
I implemented viewModel to seperate the UI design elements (composablse) from business logic elements.
I also make sure the app remembers (saves) certain state data when recomposition and/or system changes
(screen rotation) occur
 */

// this is the main screen composable that calls many of the other relevant composables for the month view
@Composable
fun MainScreen(navController: NavController, viewModel: CalendarViewModel = viewModel(), entryTableViewModel: EntryTableViewModel) {

    // only calls this function (retrieves database EntryTable data) once when MainScreen composable is first composed
    LaunchedEffect(Unit) {
        entryTableViewModel.getAllEntryTables()
    }

    val currentMonth = remember { YearMonth.now() }
    val startMonth = remember { currentMonth.minusMonths(100) }
    val endMonth = remember { currentMonth.plusMonths(100) }
    val firstDayOfWeek = remember { firstDayOfWeekFromLocale() }

    // for DATABASE
    // this is the list that contains the database EntryTable entries
    val entryList by entryTableViewModel.entryList

    // these are the EntryTable entry values
    var dateDB by remember { mutableStateOf("") }
    var entryDB by remember { mutableStateOf("") }
    var idEx by remember { mutableStateOf("") }

    // some example values for testing
    dateDB = "Viens"
    entryDB = "VēlViens"
    idEx = "1"


    // saves the parameters across recomposition
    val state = rememberCalendarState(
        startMonth = startMonth,
        endMonth = endMonth,
        firstVisibleMonth = currentMonth,
        firstDayOfWeek = firstDayOfWeek
    )


    // will produce => Sun | Mon | Tue | Wed | Thu | Fri | Sat
    val daysOfWeek = daysOfWeek()

    Column {
        // defines variables to hold year and month values
        val displayedYear = state.firstVisibleMonth.yearMonth.year
        val displayedMonth = state.firstVisibleMonth.yearMonth.month.name.uppercase()

        // calls the composable for displaying these variables correctly
        YearAndMonthDisplay(displayedMonth = displayedMonth, displayedYear = displayedYear)


        // calls the MinimalDialog composable (responsible for the contents of a specific date, doesn't yet work as intended) if showDayContentDialog is set to true
        if (viewModel.showDayContentDialog) {
            MinimalDialog(
                onDismissRequest = {
                    viewModel.toggleDayContentDialog(false) // changes showDayContentDialog value on dismiss
                    viewModel.onDateSelected(null) // clears selection on dismiss
                },
                onNewEntry = {
                    viewModel.toggleDayContentDialog(false) // changes showDayContentDialog value on dismiss
                    navController.navigate("NewEntry") // navigates to different composable
                },
                eventList = entryList // passes the entryTable list as event list to the dialog
            )
        }

        /*
I give the composable Day() a function onDateClick() as a parameter so that I could change the boolean value
of showDayContentDialog. This boolean defines whether the DayContents DIALOG pops up.
 */
        // calls the HorizontalCalendar composable
        // this comes from the kizitonwose library and it is responsible for generating the interactive month view UI
        HorizontalCalendar(
            state = state,
            dayContent = { day ->
                Day(
                    onDateClick = { viewModel.toggleDayContentDialog(true) },
                    day,
                    isSelected = viewModel.selectedDate == day.date
                ) { day ->
                    viewModel.onDateSelected(day.date)
                }
            },

            monthHeader = {
                DaysOfWeekTitle(daysOfWeek = daysOfWeek) // calls the composable responsible for week day names
            }
        )



        // for the DATABASE
        Column(modifier = Modifier.padding(16.dp)) {

            // button that inserts predefined entry into database EntryTable
            Button(onClick = {
                if (dateDB.isNotEmpty() && entryDB.isNotEmpty()) {
                    entryTableViewModel.insertEntryTable(dateDB, entryDB, idEx)
                }
            }) {
                Text("Insert Event")
            }
            Spacer(modifier = Modifier.height(8.dp))

            // button that calls the function to refresh EntryList content from EntryTable
            //(obsolete?)
            Button(onClick = {
                entryTableViewModel.getAllEntryTables()
            }) {
                Text("Get Events")
            }
            Spacer(modifier = Modifier.height(8.dp))

            // displays the EntryList entries (for testing)
            /*
            Column {
                entryList.forEach { entryTable ->
                    Text(text = "Rez:${entryTable.id}: ${entryTable.dateDB} ${entryTable.entryDB} ${entryTable.idEx}")
                }
            }

             */



            //------------------------------------------------------------------------------------
            // delete and update (for testing, not currently integrated in the project)
            entryList.forEach { entryTable ->
                Column(modifier = Modifier.padding(4.dp)) {
                    Text(text = "Rez: ${entryTable.id}: ${entryTable.dateDB} ${entryTable.entryDB} ${entryTable.idEx}")

                    Row {
                        Button(
                            onClick = {
                                val updatedEntry = entryTable.copy(dateDB = "Updated", entryDB = "Name")
                                entryTableViewModel.updateEntryTable(updatedEntry)
                                entryTableViewModel.getAllEntryTables() // refresh the list after update
                            },
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Text("Update")
                        }

                        Button(
                            onClick = {
                                entryTableViewModel.deleteEntryTable(entryTable)
                            }
                        ) {
                            Text("Delete")
                        }
                    }
                }
            }
        }
    }
}






