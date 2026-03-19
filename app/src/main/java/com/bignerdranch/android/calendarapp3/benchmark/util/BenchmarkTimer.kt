package com.bignerdranch.android.calendarapp3.benchmark.util

suspend fun measureMillis(block: suspend () -> Unit): Long {
    val start = System.nanoTime()
    block()
    val end = System.nanoTime()
    return (end - start) / 1_000_000
}