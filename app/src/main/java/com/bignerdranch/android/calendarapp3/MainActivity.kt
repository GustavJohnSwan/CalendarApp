package com.bignerdranch.android.calendarapp3

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import com.bignerdranch.android.calendarapp3.buisness_logic.objectbox.ObjectBoxNewEntryViewModel
import com.bignerdranch.android.calendarapp3.database.objectbox.ObjectBoxProvider
import com.bignerdranch.android.calendarapp3.ui.theme.CalendarApp3Theme
import com.bignerdranch.android.calendarapp3.ui_composables.ScreenNavigation
import kotlinx.coroutines.launch

// this is the main activity or the entry point of the app
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) { // savedInstanceState restores UI state after configurational changes
        super.onCreate(savedInstanceState)
        ObjectBoxProvider.init(applicationContext) // Initialize ObjectBox

        lifecycleScope.launch {
            val vm = ObjectBoxNewEntryViewModel(application)

            val entryId = vm.insertEntryWithExtraData(
                dateDB = "2026-02-27",
                entryDB = "ObjectBox smoke test",
                needsExtraData = true,
                timeMinutes = 600,
                reminderType = "Notification",
                repeat = "Daily",
                repeatDetails = "Test details"
            )

            vm.saveRecurringEventToDatabase(entryId, "2026-02-28")

            Log.d("ObjectBoxTest", "Inserted entryId=$entryId")
        }
        enableEdgeToEdge() // this enables the app to draw under the system bars (status bar and navigation bar)
        setContent { // this defines the UI of the screen using Jetpack Compose
            CalendarApp3Theme { // this is a custom theme defined in the app
                ScreenNavigation() // this is a compsable function that handles navigation in the app
            }
        }
    }
}