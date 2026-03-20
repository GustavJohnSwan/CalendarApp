package com.bignerdranch.android.calendarapp3.benchmark.objectbox

import com.bignerdranch.android.calendarapp3.benchmark.model.BenchmarkEntry

fun BenchmarkEntry.toObjectBoxEntity(): BenchmarkObjectBoxEntity {
    return BenchmarkObjectBoxEntity(
        benchmarkId = benchmarkId,
        title = title,
        description = description,
        startMillis = startMillis,
        endMillis = endMillis,
        hasReminder = hasReminder
    )
}

fun BenchmarkObjectBoxEntity.toBenchmarkEntry(): BenchmarkEntry {
    return BenchmarkEntry(
        benchmarkId = benchmarkId,
        title = title,
        description = description,
        startMillis = startMillis,
        endMillis = endMillis,
        hasReminder = hasReminder
    )
}