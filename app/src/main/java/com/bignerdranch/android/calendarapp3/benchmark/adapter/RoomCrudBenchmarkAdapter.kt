package com.bignerdranch.android.calendarapp3.benchmark.adapter

import com.bignerdranch.android.calendarapp3.benchmark.model.BenchmarkEntry
import com.bignerdranch.android.calendarapp3.benchmark.room.BenchmarkRoomDao
import com.bignerdranch.android.calendarapp3.benchmark.room.toBenchmarkEntry
import com.bignerdranch.android.calendarapp3.benchmark.room.toRoomEntity

class RoomCrudBenchmarkAdapter(
    private val dao: BenchmarkRoomDao
) : CrudBenchmarkAdapter {

    override suspend fun clearAll() {
        dao.deleteAll()
    }

    override suspend fun insertEntries(entries: List<BenchmarkEntry>) {
        dao.insertAll(entries.map { it.toRoomEntity() })
    }

    override suspend fun readAllEntries(): List<BenchmarkEntry> {
        return dao.getAll().map { it.toBenchmarkEntry() }
    }

    override suspend fun readEntryById(id: String): BenchmarkEntry? {
        return dao.getById(id)?.toBenchmarkEntry()
    }

    override suspend fun updateEntries(entries: List<BenchmarkEntry>) {
        dao.updateAll(entries.map { it.toRoomEntity() })
    }

    override suspend fun deleteEntriesByIds(ids: List<String>) {
        ids.chunked(500).forEach { chunk ->
            dao.deleteByIds(chunk)
        }
    }

    override suspend fun countEntries(): Int {
        return dao.countAll()
    }
}