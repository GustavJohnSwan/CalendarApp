package com.bignerdranch.android.calendarapp3.ui_composables.entry_view.entry_functions.repeat_function.repeat_underfunctions

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

class RepeatDialogState {
    var scrollState by mutableStateOf(0f)
    var isDialogOpen by mutableStateOf(false)

    fun updateScrollPosition(position: Float) {
        scrollState = position
    }

    fun openDialog() {
        isDialogOpen = true
    }

    fun closeDialog() {
        isDialogOpen = false
    }
}

@Composable
fun rememberRepeatDialogState(): RepeatDialogState {
    return remember { RepeatDialogState() }
}