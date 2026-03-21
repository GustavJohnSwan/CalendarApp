package com.bignerdranch.android.calendarapp3.database.objectbox

import android.content.Context

import com.bignerdranch.android.calendarapp3.benchmark.MyObjectBox
import io.objectbox.BoxStore

//*
object ObjectBoxProvider {
    @Volatile private var store: BoxStore? = null

    fun init(context: Context) {
        if (store == null) {
            synchronized(this) {
                if (store == null) {
                    store = MyObjectBox.builder()
                        .androidContext(context.applicationContext)
                        .build()
                }
            }
        }
    }

    fun get(): BoxStore =
        store ?: error("ObjectBoxProvider not initialized. Call ObjectBoxProvider.init(context) first.")
}