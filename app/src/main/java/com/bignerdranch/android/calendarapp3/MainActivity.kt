package com.bignerdranch.android.calendarapp3

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.room.Room
import com.bignerdranch.android.calendarapp3.ui.theme.CalendarApp3Theme
import com.kizitonwose.calendar.compose.HorizontalCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.core.firstDayOfWeekFromLocale
import java.time.YearMonth

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