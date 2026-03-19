package com.bignerdranch.android.calendarapp3.benchmark.runner

import android.util.Log
import com.bignerdranch.android.calendarapp3.benchmark.adapter.CrudBenchmarkAdapter
import com.bignerdranch.android.calendarapp3.benchmark.model.BenchmarkConfig
import com.bignerdranch.android.calendarapp3.benchmark.model.BenchmarkResult
import com.bignerdranch.android.calendarapp3.benchmark.util.BenchmarkDataFactory
import com.bignerdranch.android.calendarapp3.benchmark.util.measureMillis
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class BenchmarkRunner(
    private val databaseName: String,
    private val adapter: CrudBenchmarkAdapter
) {

    suspend fun runBasicCrudBenchmark(config: BenchmarkConfig): BenchmarkResult =
        withContext(Dispatchers.IO) {
            val insertRuns = mutableListOf<Long>()
            val readAllRuns = mutableListOf<Long>()
            val updateRuns = mutableListOf<Long>()
            val deleteRuns = mutableListOf<Long>()

            repeat(config.warmupRuns) {
                runSingleCrudCycle(config.entryCount)
            }

            repeat(config.measuredRuns) {
                val cycleResult = runSingleCrudCycle(config.entryCount)
                insertRuns += cycleResult.insertMs
                readAllRuns += cycleResult.readAllMs
                updateRuns += cycleResult.updateMs
                deleteRuns += cycleResult.deleteMs
            }

            val result = BenchmarkResult(
                databaseName = databaseName,
                entryCount = config.entryCount,
                insertRunsMs = insertRuns,
                readAllRunsMs = readAllRuns,
                updateRunsMs = updateRuns,
                deleteRunsMs = deleteRuns
            )

            logResult(result)

            result
        }

    private suspend fun runSingleCrudCycle(entryCount: Int): CrudCycleTiming {
        adapter.clearAll()

        val entries = BenchmarkDataFactory.createEntries(entryCount)

        val insertMs = measureMillis {
            adapter.insertEntries(entries)
        }

        val readAllMs = measureMillis {
            adapter.readAllEntries()
        }

        val updatedEntries = BenchmarkDataFactory.createUpdatedEntries(entries)

        val updateMs = measureMillis {
            adapter.updateEntries(updatedEntries)
        }

        val deleteMs = measureMillis {
            adapter.deleteEntriesByIds(entries.map { it.benchmarkId })
        }

        return CrudCycleTiming(
            insertMs = insertMs,
            readAllMs = readAllMs,
            updateMs = updateMs,
            deleteMs = deleteMs
        )
    }

    private fun logResult(result: BenchmarkResult) {
        Log.d(
            "BENCHMARK",
            """
            DB=${result.databaseName}
            N=${result.entryCount}
            INSERT_RUNS_MS=${result.insertRunsMs}
            READ_ALL_RUNS_MS=${result.readAllRunsMs}
            UPDATE_RUNS_MS=${result.updateRunsMs}
            DELETE_RUNS_MS=${result.deleteRunsMs}
            INSERT_AVG_MS=${result.insertAverageMs}
            READ_ALL_AVG_MS=${result.readAllAverageMs}
            UPDATE_AVG_MS=${result.updateAverageMs}
            DELETE_AVG_MS=${result.deleteAverageMs}
            """.trimIndent()
        )
    }

    private data class CrudCycleTiming(
        val insertMs: Long,
        val readAllMs: Long,
        val updateMs: Long,
        val deleteMs: Long
    )
}