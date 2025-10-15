package com.bignerdranch.android.calendarapp3.ui_composables.main

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.bignerdranch.android.calendarapp3.ui_composables.entry_view.EditEntry
import com.bignerdranch.android.calendarapp3.ui_composables.MainScreen
import com.bignerdranch.android.calendarapp3.ui_composables.entry_view.NewEntry
import com.bignerdranch.android.calendarapp3.buisness_logic.EditEntryViewModel
import com.bignerdranch.android.calendarapp3.buisness_logic.EntryTableViewModel


// this composable is responsible for navigating between two main composables : MainScreen and NewEntry
@Composable
fun ScreenNavigation() {
    val navController = rememberNavController()
    val entryTableViewModel: EntryTableViewModel = viewModel()
    val editEntryViewModel: EditEntryViewModel = viewModel()

    NavHost(navController = navController, startDestination = "MainScreen") {
        composable("MainScreen") {
            MainScreen(
                navController = navController,
                entryTableViewModel = entryTableViewModel,
                editEntryViewModel = editEntryViewModel
            )
        }
        composable("NewEntry") {
            NewEntry(
                navController = navController,
                entryTableViewModel = entryTableViewModel,
                editEntryViewModel = editEntryViewModel
            )
        }
        composable("EditEntry") {
            EditEntry(
                navController = navController,
                entryTableViewModel = entryTableViewModel,
                editEntryViewModel = editEntryViewModel
            )
        }
    }

}