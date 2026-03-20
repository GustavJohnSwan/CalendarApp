package com.bignerdranch.android.calendarapp3.benchmark.objectbox

import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id

@Entity
data class BenchmarkObjectBoxEntity(
    @Id
    var id: Long = 0,

    var benchmarkId: String = "",
    var title: String = "",
    var description: String = "",
    var startMillis: Long = 0L,
    var endMillis: Long = 0L,
    var hasReminder: Boolean = false
)