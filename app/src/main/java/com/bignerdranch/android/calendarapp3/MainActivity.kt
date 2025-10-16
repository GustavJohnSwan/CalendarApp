package com.bignerdranch.android.calendarapp3

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.bignerdranch.android.calendarapp3.ui.theme.CalendarApp3Theme
import com.bignerdranch.android.calendarapp3.ui_composables.ScreenNavigation

// this is the main activity or the entry point of the app
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) { // savedInstanceState restores UI state after configurational changes
        super.onCreate(savedInstanceState)
        enableEdgeToEdge() // this enables the app to draw under the system bars (status bar and navigation bar)
        setContent { // this defines the UI of the screen using Jetpack Compose
            CalendarApp3Theme { // this is a custom theme defined in the app
                ScreenNavigation() // this is a compsable function that handles navigation in the app
            }
        }
    }
}