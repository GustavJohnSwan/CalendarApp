package com.bignerdranch.android.calendarapp3

import androidx.lifecycle.ViewModel
import java.time.LocalDate
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.State

// this is the first viewModel - a class that functions as a business logic or screen level state holder.
// It makes sure that state values are remembered when navigating between activities or following configuration changes (screen rotation)
class CalendarViewModel : ViewModel() {

    // these are state variables
    var selectedDate by mutableStateOf<LocalDate?>(null)
        private set

    var showDayContentDialog by mutableStateOf(false)
        private set

    var newEventText by mutableStateOf("")
        private set




    // used to update the SELECTED DATE
    // CAUTION : this function didn't have a name origianlly. Recently this caused an error, so I identified that this was probably the function name
    // I'm not sure if this will cause an error down the line so BE AWARE that this change happenned
    fun onDateSelected(date: LocalDate?) {
        selectedDate = if (selectedDate == date) null else date
    }

    // used to toggle the dialog
    fun toggleDayContentDialog(show: Boolean) {
        showDayContentDialog = show
    }

    // Function to change newEventText
    fun onEventTextBoxSelect(text: String) {
        newEventText = text
    }


}
