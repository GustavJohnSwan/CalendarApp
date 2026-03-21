package com.bignerdranch.android.calendarapp3.benchmark.runner

import android.util.Log
import com.bignerdranch.android.calendarapp3.benchmark.adapter.CrudBenchmarkAdapter
import com.bignerdranch.android.calendarapp3.benchmark.model.BenchmarkConfig
import com.bignerdranch.android.calendarapp3.benchmark.model.BenchmarkResult
import com.bignerdranch.android.calendarapp3.benchmark.util.BenchmarkDataFactory
import com.bignerdranch.android.calendarapp3.benchmark.util.measureNanos
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

import com.bignerdranch.android.calendarapp3.benchmark.model.ReadByIdBenchmarkConfig
import com.bignerdranch.android.calendarapp3.benchmark.model.ReadByIdBenchmarkResult

import com.bignerdranch.android.calendarapp3.benchmark.model.ReadOrderedBenchmarkConfig
import com.bignerdranch.android.calendarapp3.benchmark.model.ReadOrderedBenchmarkResult

import com.bignerdranch.android.calendarapp3.benchmark.model.ReadInRangeBenchmarkConfig
import com.bignerdranch.android.calendarapp3.benchmark.model.ReadInRangeBenchmarkResult

import com.bignerdranch.android.calendarapp3.benchmark.model.UpdateBenchmarkConfig
import com.bignerdranch.android.calendarapp3.benchmark.model.UpdateBenchmarkResult

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



    //______________________________________________________________________________________________

    suspend fun runReadByIdBenchmark(config: ReadByIdBenchmarkConfig): ReadByIdBenchmarkResult =
        withContext(Dispatchers.IO) {
            val lookupRunsNs = mutableListOf<Long>()

            repeat(config.warmupRuns) {
                runSingleReadByIdCycle(
                    entryCount = config.entryCount,
                    lookupsPerRun = config.lookupsPerRun
                )
            }

            repeat(config.measuredRuns) {
                val lookupNs = runSingleReadByIdCycle(
                    entryCount = config.entryCount,
                    lookupsPerRun = config.lookupsPerRun
                )
                lookupRunsNs += lookupNs
            }

            val result = ReadByIdBenchmarkResult(
                databaseName = databaseName,
                entryCount = config.entryCount,
                lookupsPerRun = config.lookupsPerRun,
                lookupRunsNs = lookupRunsNs
            )

            logReadByIdResult(result)

            result
        }

    private suspend fun runSingleReadByIdCycle(
        entryCount: Int,
        lookupsPerRun: Int
    ): Long {
        adapter.clearAll()

        val entries = BenchmarkDataFactory.createEntries(entryCount)
        adapter.insertEntries(entries)

        val idsToLookup = entries
            .take(lookupsPerRun)
            .map { it.benchmarkId }

        return measureNanos {
            idsToLookup.forEach { id ->
                adapter.readEntryById(id)
            }
        }
    }

    private fun logReadByIdResult(result: ReadByIdBenchmarkResult) {
        Log.d(
            "BENCHMARK_READ_BY_ID",
            """
        DB=${result.databaseName}
        N=${result.entryCount}
        LOOKUPS_PER_RUN=${result.lookupsPerRun}
        LOOKUP_RUNS_NS=${result.lookupRunsNs}
        LOOKUP_AVG_MS=${result.lookupAverageMs}
        """.trimIndent()
        )
    }

    //______________________________________________________________________________________________

    suspend fun runReadOrderedBenchmark(config: ReadOrderedBenchmarkConfig): ReadOrderedBenchmarkResult =
        withContext(Dispatchers.IO) {
            val readRunsNs = mutableListOf<Long>()

            repeat(config.warmupRuns) {
                runSingleReadOrderedCycle(entryCount = config.entryCount)
            }

            repeat(config.measuredRuns) {
                val readNs = runSingleReadOrderedCycle(entryCount = config.entryCount)
                readRunsNs += readNs
            }

            val result = ReadOrderedBenchmarkResult(
                databaseName = databaseName,
                entryCount = config.entryCount,
                readRunsNs = readRunsNs
            )

            logReadOrderedResult(result)

            result
        }

    private suspend fun runSingleReadOrderedCycle(entryCount: Int): Long {
        adapter.clearAll()

        val entries = BenchmarkDataFactory.createEntries(entryCount)
        adapter.insertEntries(entries)

        return measureNanos {
            adapter.readAllEntriesOrderedByStartMillis()
        }
    }

    private fun logReadOrderedResult(result: ReadOrderedBenchmarkResult) {
        Log.d(
            "BENCHMARK_READ_ORDERED",
            """
        DB=${result.databaseName}
        N=${result.entryCount}
        READ_RUNS_NS=${result.readRunsNs}
        READ_AVG_MS=${result.readAverageMs}
        """.trimIndent()
        )
    }

    //______________________________________________________________________________________________

    suspend fun runReadInRangeBenchmark(config: ReadInRangeBenchmarkConfig): ReadInRangeBenchmarkResult =
        withContext(Dispatchers.IO) {
            val readRunsNs = mutableListOf<Long>()

            repeat(config.warmupRuns) {
                runSingleReadInRangeCycle(config)
            }

            repeat(config.measuredRuns) {
                val readNs = runSingleReadInRangeCycle(config)
                readRunsNs += readNs
            }

            val result = ReadInRangeBenchmarkResult(
                databaseName = databaseName,
                entryCount = config.entryCount,
                rangeStartIndex = config.rangeStartIndex,
                rangeSize = config.rangeSize,
                readRunsNs = readRunsNs
            )

            logReadInRangeResult(result)

            result
        }

    private suspend fun runSingleReadInRangeCycle(
        config: ReadInRangeBenchmarkConfig
    ): Long {
        adapter.clearAll()

        val entries = BenchmarkDataFactory.createEntries(config.entryCount)
        adapter.insertEntries(entries)

        require(config.entryCount > 0) { "entryCount must be > 0" }
        require(config.rangeStartIndex in 0 until config.entryCount) {
            "rangeStartIndex must be within inserted entries"
        }
        require(config.rangeSize > 0) { "rangeSize must be > 0" }

        val rangeEndIndexExclusive = minOf(
            config.rangeStartIndex + config.rangeSize,
            entries.size
        )

        val rangeEntries = entries.subList(
            config.rangeStartIndex,
            rangeEndIndexExclusive
        )

        val rangeStartMillis = rangeEntries.first().startMillis
        val rangeEndMillis = rangeEntries.last().startMillis

        return measureNanos {
            adapter.readEntriesInRangeOrderedByStartMillis(
                rangeStartMillis = rangeStartMillis,
                rangeEndMillis = rangeEndMillis
            )
        }
    }

    private fun logReadInRangeResult(result: ReadInRangeBenchmarkResult) {
        Log.d(
            "BENCHMARK_READ_IN_RANGE",
            """
        DB=${result.databaseName}
        N=${result.entryCount}
        RANGE_START_INDEX=${result.rangeStartIndex}
        RANGE_SIZE=${result.rangeSize}
        READ_RUNS_NS=${result.readRunsNs}
        READ_AVG_MS=${result.readAverageMs}
        """.trimIndent()
        )
    }

    //______________________________________________________________________________________________

    suspend fun runUpdateBenchmark(config: UpdateBenchmarkConfig): UpdateBenchmarkResult =
        withContext(Dispatchers.IO) {
            val updateRunsNs = mutableListOf<Long>()

            repeat(config.warmupRuns) {
                runSingleUpdateCycle(config)
            }

            repeat(config.measuredRuns) {
                val updateNs = runSingleUpdateCycle(config)
                updateRunsNs += updateNs
            }

            val result = UpdateBenchmarkResult(
                databaseName = databaseName,
                entryCount = config.entryCount,
                updateRunsNs = updateRunsNs
            )

            logUpdateResult(result)
            result
        }

    private suspend fun runSingleUpdateCycle(
        config: UpdateBenchmarkConfig
    ): Long {
        adapter.clearAll()

        val originalEntries = BenchmarkDataFactory.createEntries(config.entryCount)
        adapter.insertEntries(originalEntries)

        val updatedEntries = originalEntries.map { entry ->
            entry.copy(
                title = entry.title + " UPDATED",
                description = entry.description + " UPDATED",
                hasReminder = !entry.hasReminder
            )
        }

        return measureNanos {
            adapter.updateEntries(updatedEntries)
        }
    }

    private fun logUpdateResult(result: UpdateBenchmarkResult) {
        Log.d(
            "BENCHMARK_UPDATE",
            """
            DB=${result.databaseName}
            N=${result.entryCount}
            UPDATE_RUNS_NS=${result.updateRunsNs}
            UPDATE_AVG_MS=${result.updateAverageMs}
            """.trimIndent()
        )
    }
}