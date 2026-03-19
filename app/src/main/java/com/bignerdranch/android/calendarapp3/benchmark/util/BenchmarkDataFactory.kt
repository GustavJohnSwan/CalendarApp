package com.bignerdranch.android.calendarapp3.benchmark.util

import com.bignerdranch.android.calendarapp3.benchmark.model.BenchmarkEntry
import java.util.UUID

object BenchmarkDataFactory {

    fun createEntries(count: Int): List<BenchmarkEntry> {
        val now = System.currentTimeMillis()

        return List(count) { index ->
            val start = now + index * 3_600_000L
            val end = start + 3_600_000L

            BenchmarkEntry(
                benchmarkId = UUID.randomUUID().toString(),
                title = "Benchmark Title $index",
                description = "Benchmark Description $index",
                startMillis = start,
                endMillis = end,
                hasReminder = index % 2 == 0
            )
        }
    }

    fun createUpdatedEntries(entries: List<BenchmarkEntry>): List<BenchmarkEntry> {
        return entries.map {
            it.copy(
                title = "${it.title}_updated",
                description = "${it.description}_updated",
                hasReminder = !it.hasReminder
            )
        }
    }
}