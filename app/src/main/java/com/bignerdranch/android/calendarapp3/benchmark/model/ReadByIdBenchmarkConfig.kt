package com.bignerdranch.android.calendarapp3.benchmark.model

data class ReadByIdBenchmarkConfig(
    val entryCount: Int,
    val lookupsPerRun: Int,
    val warmupRuns: Int,
    val measuredRuns: Int
)