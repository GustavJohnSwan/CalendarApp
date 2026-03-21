package com.bignerdranch.android.calendarapp3.benchmark.room

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.bignerdranch.android.calendarapp3.benchmark.model.BenchmarkEntry

@Entity(
    tableName = "benchmark_entries_indexed",
    indices = [Index(value = ["startMillis"])]
)
data class BenchmarkRoomIndexedEntity(
    @PrimaryKey
    val benchmarkId: String,
    val title: String,
    val description: String,
    val startMillis: Long,
    val endMillis: Long,
    val hasReminder: Boolean
) {
    fun toBenchmarkEntry(): BenchmarkEntry {
        return BenchmarkEntry(
            benchmarkId = benchmarkId,
            title = title,
            description = description,
            startMillis = startMillis,
            endMillis = endMillis,
            hasReminder = hasReminder
        )
    }

    companion object {
        fun fromBenchmarkEntry(entry: BenchmarkEntry): BenchmarkRoomIndexedEntity {
            return BenchmarkRoomIndexedEntity(
                benchmarkId = entry.benchmarkId,
                title = entry.title,
                description = entry.description,
                startMillis = entry.startMillis,
                endMillis = entry.endMillis,
                hasReminder = entry.hasReminder
            )
        }
    }
}