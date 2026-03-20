package com.bignerdranch.android.calendarapp3.benchmark.runner

import android.util.Log
import com.bignerdranch.android.calendarapp3.benchmark.adapter.CrudBenchmarkAdapter
import com.bignerdranch.android.calendarapp3.benchmark.model.BenchmarkConfig
import com.bignerdranch.android.calendarapp3.benchmark.model.BenchmarkResult
import com.bignerdranch.android.calendarapp3.benchmark.util.BenchmarkDataFactory
import com.bignerdranch.android.calendarapp3.benchmark.util.measureNanos
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class BenchmarkRunner(
    private val databaseName: String,
    private val adapter: CrudBenchmarkAdapter
) {

    suspend fun runBasicCrudBenchmark(config: BenchmarkConfig): BenchmarkResult =
        withContext(Dispatchers.IO) {
            val insertRunsNs = mutableListOf<Long>()
            val readAllRunsNs = mutableListOf<Long>()
            val updateRunsNs = mutableListOf<Long>()
            val deleteRunsNs = mutableListOf<Long>()

            repeat(config.warmupRuns) {
                runSingleCrudCycle(config.entryCount)
            }

            repeat(config.measuredRuns) {
                val cycleResult = runSingleCrudCycle(config.entryCount)
                insertRunsNs += cycleResult.insertNs
                readAllRunsNs += cycleResult.readAllNs
                updateRunsNs += cycleResult.updateNs
                deleteRunsNs += cycleResult.deleteNs
            }

            val result = BenchmarkResult(
                databaseName = databaseName,
                entryCount = config.entryCount,
                insertRunsNs = insertRunsNs,
                readAllRunsNs = readAllRunsNs,
                updateRunsNs = updateRunsNs,
                deleteRunsNs = deleteRunsNs
            )

            logResult(result)

            result
        }

    private suspend fun runSingleCrudCycle(entryCount: Int): CrudCycleTiming {
        adapter.clearAll()

        val entries = BenchmarkDataFactory.createEntries(entryCount)

        val insertNs = measureNanos {
            adapter.insertEntries(entries)
        }

        val readAllNs = measureNanos {
            adapter.readAllEntries()
        }

        val updatedEntries = entries.map {
            it.copy(title = it.title + "_updated")
        }

        val updateNs = measureNanos {
            adapter.updateEntries(updatedEntries)
        }

        val deleteNs = measureNanos {
            adapter.deleteEntriesByIds(entries.map { it.benchmarkId })
        }

        return CrudCycleTiming(
            insertNs = insertNs,
            readAllNs = readAllNs,
            updateNs = updateNs,
            deleteNs = deleteNs
        )
    }

    private fun logResult(result: BenchmarkResult) {
        Log.d(
            "BENCHMARK",
            """
        DB=${result.databaseName}
        N=${result.entryCount}
        INSERT_RUNS_NS=${result.insertRunsNs}
        READ_ALL_RUNS_NS=${result.readAllRunsNs}
        UPDATE_RUNS_NS=${result.updateRunsNs}
        DELETE_RUNS_NS=${result.deleteRunsNs}
        INSERT_AVG_MS=${result.insertAverageMs}
        READ_ALL_AVG_MS=${result.readAllAverageMs}
        UPDATE_AVG_MS=${result.updateAverageMs}
        DELETE_AVG_MS=${result.deleteAverageMs}
        """.trimIndent()
        )
    }

    private data class CrudCycleTiming(
        val insertNs: Long,
        val readAllNs: Long,
        val updateNs: Long,
        val deleteNs: Long
    )
}