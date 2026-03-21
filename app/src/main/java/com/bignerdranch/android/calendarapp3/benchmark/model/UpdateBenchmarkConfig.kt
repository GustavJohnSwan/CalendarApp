package com.bignerdranch.android.calendarapp3.benchmark.model

data class UpdateBenchmarkConfig(
    val entryCount: Int,
    val warmupRuns: Int,
    val measuredRuns: Int
)