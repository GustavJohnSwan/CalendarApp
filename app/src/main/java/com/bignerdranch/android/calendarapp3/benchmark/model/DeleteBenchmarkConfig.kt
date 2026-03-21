package com.bignerdranch.android.calendarapp3.benchmark.model

data class DeleteBenchmarkConfig(
    val entryCount: Int,
    val warmupRuns: Int,
    val measuredRuns: Int
)