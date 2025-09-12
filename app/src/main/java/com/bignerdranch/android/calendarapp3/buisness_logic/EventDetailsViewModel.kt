package com.bignerdranch.android.calendarapp3.buisness_logic

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class EventDetailsViewModel : ViewModel() {

    var showEventDetailsDialog by mutableStateOf(false)
        private set

    fun toggleEventDetailsDialog(show: Boolean) {
        showEventDetailsDialog = show
    }

}