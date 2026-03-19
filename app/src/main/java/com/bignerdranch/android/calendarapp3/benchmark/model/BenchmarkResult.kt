package com.bignerdranch.android.calendarapp3.benchmark.model

data class BenchmarkResult(
    val databaseName: String,
    val entryCount: Int,
    val insertRunsMs: List<Long>,
    val readAllRunsMs: List<Long>,
    val updateRunsMs: List<Long>,
    val deleteRunsMs: List<Long>
) {
    val insertAverageMs: Double
        get() = insertRunsMs.average()

    val readAllAverageMs: Double
        get() = readAllRunsMs.average()

    val updateAverageMs: Double
        get() = updateRunsMs.average()

    val deleteAverageMs: Double
        get() = deleteRunsMs.average()
}