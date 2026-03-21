package com.bignerdranch.android.calendarapp3.benchmark.adapter

import com.bignerdranch.android.calendarapp3.benchmark.model.BenchmarkEntry
import com.bignerdranch.android.calendarapp3.benchmark.room.BenchmarkRoomIndexedDao
import com.bignerdranch.android.calendarapp3.benchmark.room.BenchmarkRoomIndexedEntity

class RoomIndexedCrudBenchmarkAdapter(
    private val dao: BenchmarkRoomIndexedDao
) : CrudBenchmarkAdapter {

    override suspend fun clearAll() {
        dao.clearAll()
    }

    override suspend fun insertEntries(entries: List<BenchmarkEntry>) {
        dao.insertAll(entries.map { BenchmarkRoomIndexedEntity.fromBenchmarkEntry(it) })
    }

    override suspend fun readAllEntries(): List<BenchmarkEntry> {
        return dao.getAll().map { it.toBenchmarkEntry() }
    }

    override suspend fun readEntryById(id: String): BenchmarkEntry? {
        return dao.getById(id)?.toBenchmarkEntry()
    }

    override suspend fun updateEntries(entries: List<BenchmarkEntry>) {
        dao.updateAll(entries.map { BenchmarkRoomIndexedEntity.fromBenchmarkEntry(it) })
    }

    override suspend fun deleteEntriesByIds(ids: List<String>) {
        dao.deleteByIds(ids)
    }

    override suspend fun countEntries(): Int {
        return dao.countEntries()
    }

    override suspend fun readAllEntriesOrderedByStartMillis(): List<BenchmarkEntry> {
        return dao.getAllOrderedByStartMillis().map { it.toBenchmarkEntry() }
    }

    override suspend fun readEntriesInRangeOrderedByStartMillis(
        rangeStartMillis: Long,
        rangeEndMillis: Long
    ): List<BenchmarkEntry> {
        return dao.readEntriesInRangeOrderedByStartMillis(
            rangeStartMillis,
            rangeEndMillis
        ).map { it.toBenchmarkEntry() }
    }
}