package com.bignerdranch.android.calendarapp3.ui_models

data class UiEvent(
    val id: String,              // works for both SQLite and Couchbase
    val date: String?,
    val content: String?,
    val timeMinutes: Int?
)
