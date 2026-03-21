package com.bignerdranch.android.calendarapp3.benchmark.adapter


import com.bignerdranch.android.calendarapp3.benchmark.model.BenchmarkEntry

interface CrudBenchmarkAdapter {
    suspend fun clearAll()
    suspend fun insertEntries(entries: List<BenchmarkEntry>)
    suspend fun readAllEntries(): List<BenchmarkEntry>
    suspend fun readEntryById(id: String): BenchmarkEntry?
    suspend fun updateEntries(entries: List<BenchmarkEntry>)
    suspend fun deleteEntriesByIds(ids: List<String>)
    suspend fun countEntries(): Int

    suspend fun readAllEntriesOrderedByStartMillis(): List<BenchmarkEntry>

    suspend fun readEntriesInRangeOrderedByStartMillis(
        rangeStartMillis: Long,
        rangeEndMillis: Long
    ): List<BenchmarkEntry>
}