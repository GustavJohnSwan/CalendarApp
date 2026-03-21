package com.bignerdranch.android.calendarapp3.benchmark.couchbase

import android.content.Context
import com.bignerdranch.android.calendarapp3.benchmark.model.BenchmarkEntry
import com.couchbase.lite.Collection
import com.couchbase.lite.CouchbaseLite
import com.couchbase.lite.DataSource
import com.couchbase.lite.Database
import com.couchbase.lite.Expression
import com.couchbase.lite.Meta
import com.couchbase.lite.MutableDocument
import com.couchbase.lite.Ordering
import com.couchbase.lite.QueryBuilder
import com.couchbase.lite.SelectResult
import java.util.concurrent.atomic.AtomicReference
import kotlin.use

class CouchbaseBenchmarkDao private constructor() {

    private var database: Database? = null

    private fun init(context: Context) {
        CouchbaseLite.init(context)
    }

    private fun ensureBenchmarkCollection(): Collection {
        if (database == null) {
            database = Database("calendar_db")
        }

        val db = database!!
        return db.getCollection("benchmark_entries")
            ?: db.createCollection("benchmark_entries")
    }

    fun clearBenchmarkEntries() {
        val collection = ensureBenchmarkCollection()

        val query = QueryBuilder
            .select(SelectResult.expression(Meta.id).`as`("id"))
            .from(DataSource.collection(collection))

        query.execute().use { resultSet ->
            resultSet.forEach { result ->
                val id = result.getString("id") ?: return@forEach
                collection.getDocument(id)?.let { doc ->
                    collection.delete(doc)
                }
            }
        }
    }

    fun insertBenchmarkEntries(entries: List<BenchmarkEntry>) {
        val collection = ensureBenchmarkCollection()
        val db = database ?: error("Benchmark database is not initialized")

        db.inBatch<java.lang.Exception>({
            entries.forEach { entry ->
                val doc = MutableDocument(entry.benchmarkId)
                    .setString("type", "benchmark")
                    .setString("benchmarkId", entry.benchmarkId)
                    .setString("title", entry.title)
                    .setString("description", entry.description)
                    .setLong("startMillis", entry.startMillis)
                    .setLong("endMillis", entry.endMillis)
                    .setBoolean("hasReminder", entry.hasReminder)

                collection.save(doc)
            }
        })
    }

    fun readAllBenchmarkEntries(): List<BenchmarkEntry> {
        val collection = ensureBenchmarkCollection()

        val query = QueryBuilder
            .select(SelectResult.all())
            .from(DataSource.collection(collection))
            .where(
                Expression.property("type").equalTo(Expression.string("benchmark"))
            )

        val results = mutableListOf<BenchmarkEntry>()

        query.execute().use { resultSet ->
            resultSet.forEach { result ->
                val dict = result.getDictionary(collection.name) ?: return@forEach

                results.add(
                    BenchmarkEntry(
                        benchmarkId = dict.getString("benchmarkId") ?: "",
                        title = dict.getString("title") ?: "",
                        description = dict.getString("description") ?: "",
                        startMillis = dict.getLong("startMillis"),
                        endMillis = dict.getLong("endMillis"),
                        hasReminder = dict.getBoolean("hasReminder")
                    )
                )
            }
        }

        return results
    }

    fun readBenchmarkEntryById(id: String): BenchmarkEntry? {
        val collection = ensureBenchmarkCollection()
        val doc = collection.getDocument(id) ?: return null

        return BenchmarkEntry(
            benchmarkId = doc.getString("benchmarkId") ?: "",
            title = doc.getString("title") ?: "",
            description = doc.getString("description") ?: "",
            startMillis = doc.getLong("startMillis"),
            endMillis = doc.getLong("endMillis"),
            hasReminder = doc.getBoolean("hasReminder")
        )
    }

    fun updateBenchmarkEntries(entries: List<BenchmarkEntry>) {
        val collection = ensureBenchmarkCollection()
        val db = database ?: error("Benchmark database is not initialized")

        db.inBatch<java.lang.Exception>({
            entries.forEach { entry ->
                val existing = collection.getDocument(entry.benchmarkId) ?: return@forEach

                val mutable = existing.toMutable()
                    .setString("type", "benchmark")
                    .setString("benchmarkId", entry.benchmarkId)
                    .setString("title", entry.title)
                    .setString("description", entry.description)
                    .setLong("startMillis", entry.startMillis)
                    .setLong("endMillis", entry.endMillis)
                    .setBoolean("hasReminder", entry.hasReminder)

                collection.save(mutable)
            }
        })
    }

    fun deleteBenchmarkEntriesByIds(ids: List<String>) {
        val collection = ensureBenchmarkCollection()
        val db = database ?: error("Benchmark database is not initialized")

        db.inBatch<Exception>({
            ids.forEach { id ->
                collection.getDocument(id)?.let { doc ->
                    collection.delete(doc)
                }
            }
        })
    }

    fun countBenchmarkEntries(): Int {
        return readAllBenchmarkEntries().size
    }

    companion object {
        private const val TAG = "CBL_BENCHMARK"

        private val INSTANCE = AtomicReference<CouchbaseBenchmarkDao?>()

        @Synchronized
        fun getInstance(context: Context): CouchbaseBenchmarkDao {
            var dao = INSTANCE.get()
            if (dao == null) {
                dao = CouchbaseBenchmarkDao()
                if (INSTANCE.compareAndSet(null, dao)) {
                    dao.init(context)
                }
            }
            return INSTANCE.get()!!
        }
    }


    //______________________________________________________________________________________________
    fun readAllBenchmarkEntriesOrderedByStartMillis(): List<BenchmarkEntry> {
        val collection = ensureBenchmarkCollection()

        val query = QueryBuilder
            .select(SelectResult.all())
            .from(DataSource.collection(collection))
            .where(
                Expression.property("type").equalTo(Expression.string("benchmark"))
            )
            .orderBy(Ordering.property("startMillis").ascending())

        val results = mutableListOf<BenchmarkEntry>()

        query.execute().use { resultSet ->
            resultSet.forEach { result ->
                val dict = result.getDictionary(collection.name) ?: return@forEach

                results.add(
                    BenchmarkEntry(
                        benchmarkId = dict.getString("benchmarkId") ?: "",
                        title = dict.getString("title") ?: "",
                        description = dict.getString("description") ?: "",
                        startMillis = dict.getLong("startMillis"),
                        endMillis = dict.getLong("endMillis"),
                        hasReminder = dict.getBoolean("hasReminder")
                    )
                )
            }
        }

        return results
    }

    fun readBenchmarkEntriesInRangeOrderedByStartMillis(
        rangeStartMillis: Long,
        rangeEndMillis: Long
    ): List<BenchmarkEntry> {
        val collection = ensureBenchmarkCollection()

        val query = QueryBuilder
            .select(SelectResult.all())
            .from(DataSource.collection(collection))
            .where(
                Expression.property("type").equalTo(Expression.string("benchmark"))
                    .and(
                        Expression.property("startMillis").between(
                            Expression.longValue(rangeStartMillis),
                            Expression.longValue(rangeEndMillis)
                        )
                    )
            )
            .orderBy(Ordering.property("startMillis").ascending())

        val results = mutableListOf<BenchmarkEntry>()

        query.execute().use { resultSet ->
            resultSet.forEach { result ->
                val dict = result.getDictionary(collection.name) ?: return@forEach

                results.add(
                    BenchmarkEntry(
                        benchmarkId = dict.getString("benchmarkId") ?: "",
                        title = dict.getString("title") ?: "",
                        description = dict.getString("description") ?: "",
                        startMillis = dict.getLong("startMillis"),
                        endMillis = dict.getLong("endMillis"),
                        hasReminder = dict.getBoolean("hasReminder")
                    )
                )
            }
        }

        return results
    }
}