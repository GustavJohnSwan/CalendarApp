package com.bignerdranch.android.calendarapp3.buisness_logic

import androidx.lifecycle.ViewModel
import java.time.LocalDate
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

// this is the first viewModel - a class that functions as a business logic or screen level state holder.
// It makes sure that state values are remembered when navigating between activities or following configuration changes (screen rotation)
class CalendarViewModel : ViewModel() {
    var selectedDate by mutableStateOf<LocalDate?>(null)
        private set

    var showDayContentDialog by mutableStateOf(false)
        private set

    var newEventText by mutableStateOf("")
        private set

    fun onDateSelected(date: LocalDate?) {
        selectedDate = if (selectedDate == date) null else date
    }

    fun toggleDayContentDialog(show: Boolean) {
        showDayContentDialog = show
    }

    fun onEventTextBoxSelect(text: String) {
        newEventText = text
    }
}
