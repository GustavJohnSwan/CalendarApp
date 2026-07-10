package com.bignerdranch.android.calendarapp3.database_2.objectbox

import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id

@Entity
data class User(
    @Id
    var id: Long = 0,
    var name: String? = null
)

@Entity
data class Household(
    @Id
    var id: Long = 0,
    var address: String? = null,
    var color: String? = null,
    var stories: Int = 1
)