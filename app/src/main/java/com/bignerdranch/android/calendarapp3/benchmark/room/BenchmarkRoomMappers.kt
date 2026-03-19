package com.bignerdranch.android.calendarapp3.benchmark.room

import com.bignerdranch.android.calendarapp3.benchmark.model.BenchmarkEntry

fun BenchmarkEntry.toRoomEntity(): BenchmarkRoomEntity {
    return BenchmarkRoomEntity(
        benchmarkId = benchmarkId,
        title = title,
        description = description,
        startMillis = startMillis,
        endMillis = endMillis,
        hasReminder = hasReminder
    )
}

fun BenchmarkRoomEntity.toBenchmarkEntry(): BenchmarkEntry {
    return BenchmarkEntry(
        benchmarkId = benchmarkId,
        title = title,
        description = description,
        startMillis = startMillis,
        endMillis = endMillis,
        hasReminder = hasReminder
    )
}