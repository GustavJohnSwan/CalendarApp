package com.bignerdranch.android.calendarapp3.benchmark.room

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "benchmark_entries")
data class BenchmarkRoomEntity(
    @PrimaryKey
    val benchmarkId: String,
    val title: String,
    val description: String,
    val startMillis: Long,
    val endMillis: Long,
    val hasReminder: Boolean
)