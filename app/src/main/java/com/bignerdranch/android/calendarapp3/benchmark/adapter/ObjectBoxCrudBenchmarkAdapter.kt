package com.bignerdranch.android.calendarapp3.benchmark.adapter

import com.bignerdranch.android.calendarapp3.benchmark.model.BenchmarkEntry
import com.bignerdranch.android.calendarapp3.benchmark.objectbox.BenchmarkObjectBoxEntity
import com.bignerdranch.android.calendarapp3.benchmark.objectbox.BenchmarkObjectBoxEntity_
import com.bignerdranch.android.calendarapp3.benchmark.objectbox.toBenchmarkEntry
import com.bignerdranch.android.calendarapp3.benchmark.objectbox.toObjectBoxEntity
import io.objectbox.Box
import io.objectbox.query.QueryBuilder

class ObjectBoxCrudBenchmarkAdapter(
    private val box: Box<BenchmarkObjectBoxEntity>
) : CrudBenchmarkAdapter {

    override suspend fun clearAll() {
        box.removeAll()
    }

    override suspend fun insertEntries(entries: List<BenchmarkEntry>) {
        val entities = entries.map { it.toObjectBoxEntity() }
        box.put(entities)
    }

    override suspend fun readAllEntries(): List<BenchmarkEntry> {
        return box.all.map { it.toBenchmarkEntry() }
    }

    override suspend fun readEntryById(id: String): BenchmarkEntry? {
        val entity = box.query(
            BenchmarkObjectBoxEntity_.benchmarkId.equal(id, QueryBuilder.StringOrder.CASE_SENSITIVE)
        ).build().use { query ->
            query.findFirst()
        }

        return entity?.toBenchmarkEntry()
    }

    override suspend fun updateEntries(entries: List<BenchmarkEntry>) {
        val existingByBenchmarkId = box.all.associateBy { it.benchmarkId }

        val updatedEntities = entries.mapNotNull { entry ->
            val existing = existingByBenchmarkId[entry.benchmarkId] ?: return@mapNotNull null
            BenchmarkObjectBoxEntity(
                id = existing.id,
                benchmarkId = entry.benchmarkId,
                title = entry.title,
                description = entry.description,
                startMillis = entry.startMillis,
                endMillis = entry.endMillis,
                hasReminder = entry.hasReminder
            )
        }

        box.put(updatedEntities)
    }

    override suspend fun deleteEntriesByIds(ids: List<String>) {
        val idSet = ids.toSet()
        val entitiesToDelete = box.all.filter { it.benchmarkId in idSet }
        box.remove(entitiesToDelete)
    }

    override suspend fun countEntries(): Int {
        return box.count().toInt()
    }

    override suspend fun readAllEntriesOrderedByStartMillis(): List<BenchmarkEntry> {
        return box.query()
            .order(BenchmarkObjectBoxEntity_.startMillis)
            .build()
            .use { query ->
                query.find().map { it.toBenchmarkEntry() }
            }
    }
}