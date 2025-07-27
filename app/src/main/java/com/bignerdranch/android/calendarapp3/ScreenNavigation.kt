package com.bignerdranch.android.calendarapp3

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController


// this composable is responsible for navigating between two main composables : MainScreen and NewEntry
@Composable
fun ScreenNavigation() {
    val navController = rememberNavController()
    val entryTableViewModel: EntryTableViewModel = viewModel()
    val editEntryViewModel: EditEntryViewModel = viewModel()

    NavHost(navController = navController, startDestination = "MainScreen") {
        composable("MainScreen") { MainScreen(navController = navController, entryTableViewModel = entryTableViewModel, editEntryViewModel = editEntryViewModel) }
        composable("NewEntry") { NewEntry(navController = navController, entryTableViewModel = entryTableViewModel, editEntryViewModel = editEntryViewModel) }
        composable("EditEntry") { EditEntry(navController = navController, entryTableViewModel = entryTableViewModel, editEntryViewModel = editEntryViewModel) }
    }

}