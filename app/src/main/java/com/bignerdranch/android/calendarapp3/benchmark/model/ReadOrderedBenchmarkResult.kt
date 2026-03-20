package com.bignerdranch.android.calendarapp3.benchmark.model

data class ReadOrderedBenchmarkResult(
    val databaseName: String,
    val entryCount: Int,
    val readRunsNs: List<Long>
) {
    val readAverageNs: Double
        get() = readRunsNs.average()

    val readAverageMs: Double
        get() = readAverageNs / 1_000_000.0
}