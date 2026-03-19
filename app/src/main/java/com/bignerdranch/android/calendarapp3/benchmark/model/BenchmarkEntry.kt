package com.bignerdranch.android.calendarapp3.benchmark.model

data class BenchmarkEntry(
    val benchmarkId: String,
    val title: String,
    val description: String,
    val startMillis: Long,
    val endMillis: Long,
    val hasReminder: Boolean
)