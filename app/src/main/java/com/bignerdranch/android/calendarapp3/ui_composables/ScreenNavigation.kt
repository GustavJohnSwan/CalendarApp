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

import com.bignerdranch.android.calendarapp3.buisness_logic.objectbox.ObjectBoxNewEntryViewModel
import com.bignerdranch.android.calendarapp3.buisness_logic.objectbox.ObjectBoxEditEntryViewModel
import com.bignerdranch.android.calendarapp3.buisness_logic.objectbox.attachment.ObjectBoxAttachmentViewModel

// this composable is responsible for navigating between two main composables : MainScreen and NewEntry
@Composable
fun ScreenNavigation() {
    val navController = rememberNavController()
    val newEntryViewModel: NewEntryViewModel = viewModel()
    val editEntryViewModel: EditEntryViewModel = viewModel()
    val couchbaseCalendarViewModel: CouchbaseCalendarViewModel = viewModel()

    val objectBoxNewEntryViewModel: ObjectBoxNewEntryViewModel = viewModel()
    val objectBoxEditEntryViewModel: ObjectBoxEditEntryViewModel = viewModel()
    val objectBoxAttachmentViewModel: ObjectBoxAttachmentViewModel = viewModel()

    NavHost(navController = navController, startDestination = "MainScreen") {
        composable("MainScreen") {
            MainScreen(
                navController = navController,
                newEntryViewModel = newEntryViewModel,
                editEntryViewModel = editEntryViewModel,
                couchbaseCalendarViewModel = couchbaseCalendarViewModel,
                objectBoxEditEntryViewModel = objectBoxEditEntryViewModel
            )
        }
        composable("NewEntry/{source}") { backStackEntry ->
            val source = backStackEntry.arguments?.getString("source") ?: "sqlite"
            NewEntry(
                navController = navController,
                newEntryViewModel = newEntryViewModel,
                editEntryViewModel = editEntryViewModel,
                couchbaseCalendarViewModel = couchbaseCalendarViewModel,
                objectBoxNewEntryViewModel = objectBoxNewEntryViewModel,
                source = source
            )
        }

        composable("EditEntry/{source}") { backStackEntry ->
            val source = backStackEntry.arguments?.getString("source") ?: "sqlite"
            EditEntry(
                navController = navController,
                editEntryViewModel = editEntryViewModel,
                couchbaseCalendarViewModel = couchbaseCalendarViewModel,
                objectBoxEditEntryViewModel = objectBoxEditEntryViewModel,
                objectBoxAttachmentViewModel = objectBoxAttachmentViewModel,
                source = source
            )
        }

    }

}