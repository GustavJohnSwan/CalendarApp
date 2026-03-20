package com.bignerdranch.android.calendarapp3.benchmark.model

data class ReadByIdBenchmarkResult(
    val databaseName: String,
    val entryCount: Int,
    val lookupsPerRun: Int,
    val lookupRunsNs: List<Long>
) {
    val lookupAverageNs: Double
        get() = lookupRunsNs.average()

    val lookupAverageMs: Double
        get() = lookupAverageNs / 1_000_000.0
}