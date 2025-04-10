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

    // DELETE THIS AND EVERYTHING RELATED TO IT
    /*
    private var _counter = mutableStateOf(0)
    val counter: State<Int> = _counter
     */


    // used to update the SELECTED DATE
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
