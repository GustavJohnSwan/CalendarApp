package com.bignerdranch.android.calendarapp3.benchmark.util

suspend fun measureNanos(block: suspend () -> Unit): Long {
    val start = System.nanoTime()
    block()
    val end = System.nanoTime()
    return end - start
}