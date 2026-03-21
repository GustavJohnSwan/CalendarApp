package com.bignerdranch.android.calendarapp3.benchmark.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface BenchmarkRoomIndexedDao {

    @Query("DELETE FROM benchmark_entries_indexed")
    suspend fun clearAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entries: List<BenchmarkRoomIndexedEntity>)

    @Query("SELECT * FROM benchmark_entries_indexed")
    suspend fun getAll(): List<BenchmarkRoomIndexedEntity>

    @Query("SELECT * FROM benchmark_entries_indexed WHERE benchmarkId = :id LIMIT 1")
    suspend fun getById(id: String): BenchmarkRoomIndexedEntity?

    @Update
    suspend fun updateAll(entries: List<BenchmarkRoomIndexedEntity>)

    @Query("DELETE FROM benchmark_entries_indexed WHERE benchmarkId IN (:ids)")
    suspend fun deleteByIds(ids: List<String>)

    @Query("SELECT COUNT(*) FROM benchmark_entries_indexed")
    suspend fun countEntries(): Int

    @Query("SELECT * FROM benchmark_entries_indexed ORDER BY startMillis ASC")
    suspend fun getAllOrderedByStartMillis(): List<BenchmarkRoomIndexedEntity>

    @Query("""
        SELECT * FROM benchmark_entries_indexed
        WHERE startMillis BETWEEN :rangeStartMillis AND :rangeEndMillis
        ORDER BY startMillis ASC
    """)
    suspend fun readEntriesInRangeOrderedByStartMillis(
        rangeStartMillis: Long,
        rangeEndMillis: Long
    ): List<BenchmarkRoomIndexedEntity>
}