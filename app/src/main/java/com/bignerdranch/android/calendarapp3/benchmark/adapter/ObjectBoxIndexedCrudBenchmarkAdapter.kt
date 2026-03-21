package com.bignerdranch.android.calendarapp3.benchmark.adapter

import com.bignerdranch.android.calendarapp3.benchmark.model.BenchmarkEntry
import com.bignerdranch.android.calendarapp3.benchmark.model.BenchmarkObjectBoxIndexedEntity
import com.bignerdranch.android.calendarapp3.benchmark.model.BenchmarkObjectBoxIndexedEntity_
import io.objectbox.Box

class ObjectBoxIndexedCrudBenchmarkAdapter(
    private val box: Box<BenchmarkObjectBoxIndexedEntity>
) : CrudBenchmarkAdapter {

    override suspend fun clearAll() {
        box.removeAll()
    }

    override suspend fun insertEntries(entries: List<BenchmarkEntry>) {
        box.put(entries.map { BenchmarkObjectBoxIndexedEntity.fromBenchmarkEntry(it) })
    }

    override suspend fun readAllEntries(): List<BenchmarkEntry> {
        return box.all.map { it.toBenchmarkEntry() }
    }

    override suspend fun readEntryById(id: String): BenchmarkEntry? {
        return box.query(
            BenchmarkObjectBoxIndexedEntity_.benchmarkId.equal(id)
        ).build().use { query ->
            query.findFirst()?.toBenchmarkEntry()
        }
    }

    override suspend fun updateEntries(entries: List<BenchmarkEntry>) {
        val existingByBenchmarkId = box.all.associateBy { it.benchmarkId }

        val updatedEntities = entries.mapNotNull { entry ->
            val existing = existingByBenchmarkId[entry.benchmarkId] ?: return@mapNotNull null
            existing.apply {
                title = entry.title
                description = entry.description
                startMillis = entry.startMillis
                endMillis = entry.endMillis
                hasReminder = entry.hasReminder
            }
        }

        box.put(updatedEntities)
    }

    override suspend fun deleteEntriesByIds(ids: List<String>) {
        box.query(
            BenchmarkObjectBoxIndexedEntity_.benchmarkId.oneOf(ids.toTypedArray())
        ).build().use { query ->
            val entities = query.find()
            box.remove(entities)
        }
    }

    override suspend fun countEntries(): Int {
        return box.count().toInt()
    }

    override suspend fun readAllEntriesOrderedByStartMillis(): List<BenchmarkEntry> {
        return box.query()
            .order(BenchmarkObjectBoxIndexedEntity_.startMillis)
            .build()
            .use { query ->
                query.find().map { it.toBenchmarkEntry() }
            }
    }

    override suspend fun readEntriesInRangeOrderedByStartMillis(
        rangeStartMillis: Long,
        rangeEndMillis: Long
    ): List<BenchmarkEntry> {
        return box.query(
            BenchmarkObjectBoxIndexedEntity_.startMillis.between(rangeStartMillis, rangeEndMillis)
        )
            .order(BenchmarkObjectBoxIndexedEntity_.startMillis)
            .build()
            .use { query ->
                query.find().map { it.toBenchmarkEntry() }
            }
    }
}