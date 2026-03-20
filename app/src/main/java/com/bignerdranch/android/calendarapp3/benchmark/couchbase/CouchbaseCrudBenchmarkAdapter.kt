package com.bignerdranch.android.calendarapp3.benchmark.adapter

import com.bignerdranch.android.calendarapp3.benchmark.couchbase.CouchbaseBenchmarkDao
import com.bignerdranch.android.calendarapp3.benchmark.model.BenchmarkEntry

class CouchbaseCrudBenchmarkAdapter(
    private val dao: CouchbaseBenchmarkDao
) : CrudBenchmarkAdapter {

    override suspend fun clearAll() {
        dao.clearBenchmarkEntries()
    }

    override suspend fun insertEntries(entries: List<BenchmarkEntry>) {
        dao.insertBenchmarkEntries(entries)
    }

    override suspend fun readAllEntries(): List<BenchmarkEntry> {
        return dao.readAllBenchmarkEntries()
    }

    override suspend fun readEntryById(id: String): BenchmarkEntry? {
        return dao.readBenchmarkEntryById(id)
    }

    override suspend fun updateEntries(entries: List<BenchmarkEntry>) {
        dao.updateBenchmarkEntries(entries)
    }

    override suspend fun deleteEntriesByIds(ids: List<String>) {
        dao.deleteBenchmarkEntriesByIds(ids)
    }

    override suspend fun countEntries(): Int {
        return dao.countBenchmarkEntries()
    }
}