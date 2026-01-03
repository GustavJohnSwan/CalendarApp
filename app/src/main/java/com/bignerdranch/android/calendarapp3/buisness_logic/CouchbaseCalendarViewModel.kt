package com.bignerdranch.android.calendarapp3.buisness_logic

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.bignerdranch.android.calendarapp3.database.DAO.CouchBaseLiteDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

import com.bignerdranch.android.calendarapp3.ui_models.UiEvent

import kotlinx.coroutines.withContext




class CouchbaseCalendarViewModel(application: Application) : AndroidViewModel(application) {

    private val _eventsForSelectedDate = MutableStateFlow<List<UiEvent>>(emptyList())
    val eventsForSelectedDate: StateFlow<List<UiEvent>> = _eventsForSelectedDate

    private val _isReady = MutableStateFlow(false)
    val isReady: StateFlow<Boolean> = _isReady



    init {
        // Open/create DB + collections as early as possible so the UI doesn't need an "Initialize" button.
        initializeCalendarDatabase()
    }

    fun loadEntriesForDate(date: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val mgr = CouchBaseLiteDao.getInstance(getApplication())

                // Ensure DB + collections exist before querying
                if (!_isReady.value) {
                    mgr.createCalendarDb()
                    mgr.createCalendarCollections()
                    _isReady.value = true
                }

                val entries = mgr.getEntriesByDate(date)

                val mapped = entries.map { m ->
                    UiEvent(
                        id = (m["_id"] as? String) ?: "",
                        date = m["dateDB"] as? String,
                        content = m["entryDB"] as? String,
                        timeMinutes = when (val t = m["timeMinutes"]) {
                            is Number -> t.toInt()
                            else -> null
                        }
                    )
                }

                _eventsForSelectedDate.value = mapped
            } catch (e: Exception) {
                Log.e("CouchbaseCalendar", "Failed to load entries for date", e)
            }
        }
    }



    // Add these methods to the class:

    // Export calendar data to JSON
    fun exportCalendarToJson() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val mgr = CouchBaseLiteDao.getInstance(getApplication())
                mgr.shareCalendarDatabaseExport(getApplication())
            } catch (e: Exception) {
                Log.e("CouchbaseCalendar", "Calendar export failed", e)
            }
        }
    }

    // Log calendar database contents
    fun logCalendarDatabaseContents() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val mgr = CouchBaseLiteDao.getInstance(getApplication())
                mgr.logCalendarDatabaseContents()
            } catch (e: Exception) {
                Log.e("CouchbaseCalendar", "Failed to log calendar contents", e)
            }
        }
    }

    // Optional: Add toast message tracking
    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage

    fun clearToastMessage() {
        _toastMessage.value = null
    }


    fun updateCalendarEntry(
        entryId: String,
        date: String,
        content: String,
        timeMinutes: Int?,
        onUpdated: () -> Unit = {}
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val mgr = CouchBaseLiteDao.getInstance(getApplication())

                // Ensure ready (same logic you already used in loadEntriesForDate)
                if (!_isReady.value) {
                    mgr.createCalendarDb()
                    mgr.createCalendarCollections()
                    _isReady.value = true
                }

                mgr.updateCalendarEntry(
                    entryId = entryId,
                    dateDB = date,
                    entryDB = content,
                    timeMinutes = timeMinutes
                )


                withContext(Dispatchers.Main) {
                    onUpdated()
                }

                Log.i("CouchbaseCalendar", "Calendar entry updated: $entryId")
            } catch (e: Exception) {
                Log.e("CouchbaseCalendar", "Failed to update calendar entry", e)
            }
        }
    }



    //______________________________________________________________________________________________
    //______________________________________________________________________________________________
    //______________________________________________________________________________________________

    fun initializeCalendarDatabase() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val mgr = CouchBaseLiteDao.getInstance(getApplication())


                mgr.createCalendarDb()
                mgr.createCalendarCollections()
                _isReady.value = true
                Log.i("CouchbaseCalendar", "Calendar database initialized")

            } catch (e: Exception) {
                Log.e("CouchbaseCalendar", "Failed to initialize database", e)
            }
        }
    }

    fun createCalendarEntry(
        date: String,
        content: String,
        timeMinutes: Int? = null,
        hasExtraData: Boolean = false,
        reminderType: String? = null,
        repeat: String? = null,
        repeatDetails: String? = null,
        onEntryCreated: (String) -> Unit = {}  // Callback with document ID
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val mgr = CouchBaseLiteDao.getInstance(getApplication())
                val entryId = mgr.createCalendarEntry(date, content, timeMinutes)

                if (hasExtraData) {
                    mgr.addExtraDataToEntry(entryId, reminderType, repeat, repeatDetails)
                }

                withContext(Dispatchers.Main) {
                    onEntryCreated(entryId)
                }

                Log.i("CouchbaseCalendar", "Calendar entry created: $entryId")
            } catch (e: Exception) {
                Log.e("CouchbaseCalendar", "Failed to create calendar entry", e)
            }
        }
    }

    fun getEntriesForDate(date: String, onEntriesLoaded: (List<Map<String, Any>>) -> Unit = {}) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val mgr = CouchBaseLiteDao.getInstance(getApplication())
                val entries = mgr.getEntriesByDate(date)
                onEntriesLoaded(entries)
            } catch (e: Exception) {
                Log.e("CouchbaseCalendar", "Failed to get entries for date", e)
            }
        }
    }
}