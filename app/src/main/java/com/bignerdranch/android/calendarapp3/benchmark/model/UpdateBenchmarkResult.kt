package com.bignerdranch.android.calendarapp3.benchmark.model

data class UpdateBenchmarkResult(
    val databaseName: String,
    val entryCount: Int,
    val updateRunsNs: List<Long>
) {
    val updateAverageNs: Double
        get() = updateRunsNs.average()

    val updateAverageMs: Double
        get() = updateAverageNs / 1_000_000.0
}