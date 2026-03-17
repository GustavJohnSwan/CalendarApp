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


        enableEdgeToEdge() // this enables the app to draw under the system bars (status bar and navigation bar)
        setContent { // this defines the UI of the screen using Jetpack Compose
            CalendarApp3Theme { // this is a custom theme defined in the app
                ScreenNavigation() // this is a composable function that handles navigation in the app
            }
        }
    }
}