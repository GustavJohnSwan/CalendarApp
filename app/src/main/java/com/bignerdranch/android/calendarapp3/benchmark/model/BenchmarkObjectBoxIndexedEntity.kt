package com.bignerdranch.android.calendarapp3.benchmark.model

import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import io.objectbox.annotation.Index

@Entity
data class BenchmarkObjectBoxIndexedEntity(
    @Id var id: Long = 0,

    var benchmarkId: String = "",
    var title: String = "",
    var description: String = "",

    @Index
    var startMillis: Long = 0L,

    var endMillis: Long = 0L,
    var hasReminder: Boolean = false
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
        fun fromBenchmarkEntry(entry: BenchmarkEntry): BenchmarkObjectBoxIndexedEntity {
            return BenchmarkObjectBoxIndexedEntity(
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