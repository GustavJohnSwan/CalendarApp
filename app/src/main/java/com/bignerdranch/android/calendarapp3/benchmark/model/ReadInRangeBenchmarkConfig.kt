package com.bignerdranch.android.calendarapp3.benchmark.model

data class ReadInRangeBenchmarkConfig(
    val entryCount: Int,
    val rangeStartIndex: Int,
    val rangeSize: Int,
    val warmupRuns: Int,
    val measuredRuns: Int
)