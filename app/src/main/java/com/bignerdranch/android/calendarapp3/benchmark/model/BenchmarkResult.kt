package com.bignerdranch.android.calendarapp3.benchmark.model

data class BenchmarkResult(
    val databaseName: String,
    val entryCount: Int,
    val insertRunsNs: List<Long>,
    val readAllRunsNs: List<Long>,
    val updateRunsNs: List<Long>,
    val deleteRunsNs: List<Long>
) {
    val insertAverageNs: Double
        get() = insertRunsNs.average()

    val readAllAverageNs: Double
        get() = readAllRunsNs.average()

    val updateAverageNs: Double
        get() = updateRunsNs.average()

    val deleteAverageNs: Double
        get() = deleteRunsNs.average()

    val insertAverageMs: Double
        get() = insertAverageNs / 1_000_000.0

    val readAllAverageMs: Double
        get() = readAllAverageNs / 1_000_000.0

    val updateAverageMs: Double
        get() = updateAverageNs / 1_000_000.0

    val deleteAverageMs: Double
        get() = deleteAverageNs / 1_000_000.0
}