package com.bignerdranch.android.calendarapp3.benchmark.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface BenchmarkRoomDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entries: List<BenchmarkRoomEntity>)

    @Query("SELECT * FROM benchmark_entries")
    suspend fun getAll(): List<BenchmarkRoomEntity>

    @Query("SELECT * FROM benchmark_entries WHERE benchmarkId = :id LIMIT 1")
    suspend fun getById(id: String): BenchmarkRoomEntity?

    @Update
    suspend fun updateAll(entries: List<BenchmarkRoomEntity>)

    @Query("DELETE FROM benchmark_entries WHERE benchmarkId IN (:ids)")
    suspend fun deleteByIds(ids: List<String>)

    @Query("DELETE FROM benchmark_entries")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM benchmark_entries")
    suspend fun countAll(): Int

    @Query("SELECT * FROM benchmark_entries ORDER BY startMillis ASC")
    suspend fun getAllOrderedByStartMillis(): List<BenchmarkRoomEntity>

    @Query("""
    SELECT * FROM benchmark_entries
    WHERE startMillis BETWEEN :rangeStartMillis AND :rangeEndMillis
    ORDER BY startMillis ASC
""")
    suspend fun readBenchmarkEntriesInRangeOrderedByStartMillis(
        rangeStartMillis: Long,
        rangeEndMillis: Long
    ): List<BenchmarkRoomEntity>
}