package com.bignerdranch.android.calendarapp3.ui_composables

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.bignerdranch.android.calendarapp3.buisness_logic.CouchbaseCalendarViewModel
import com.bignerdranch.android.calendarapp3.ui_composables.entry_view.EditEntry
import com.bignerdranch.android.calendarapp3.ui_composables.entry_view.NewEntry
import com.bignerdranch.android.calendarapp3.buisness_logic.EditEntryViewModel
import com.bignerdranch.android.calendarapp3.buisness_logic.NewEntryViewModel


// this composable is responsible for navigating between two main composables : MainScreen and NewEntry
@Composable
fun ScreenNavigation() {
    val navController = rememberNavController()
    val newEntryViewModel: NewEntryViewModel = viewModel()
    val editEntryViewModel: EditEntryViewModel = viewModel()
    val couchbaseCalendarViewModel: CouchbaseCalendarViewModel = viewModel()  // ADD THIS

    NavHost(navController = navController, startDestination = "MainScreen") {
        composable("MainScreen") {
            MainScreen(
                navController = navController,
                newEntryViewModel = newEntryViewModel,
                editEntryViewModel = editEntryViewModel,                couchbaseCalendarViewModel = couchbaseCalendarViewModel  // ADD THIS
            )
        }
        composable("NewEntry") {
            NewEntry(
                navController = navController,
                newEntryViewModel = newEntryViewModel,
                editEntryViewModel = editEntryViewModel
            )
        }
        composable("EditEntry") {
            EditEntry(
                navController = navController,
                editEntryViewModel = editEntryViewModel
            )
        }
    }

}