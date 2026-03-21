package com.bignerdranch.android.calendarapp3.benchmark.model

data class DeleteBenchmarkResult(
    val databaseName: String,
    val entryCount: Int,
    val deleteRunsNs: List<Long>
) {
    val deleteAverageNs: Double
        get() = deleteRunsNs.average()

    val deleteAverageMs: Double
        get() = deleteAverageNs / 1_000_000.0
}