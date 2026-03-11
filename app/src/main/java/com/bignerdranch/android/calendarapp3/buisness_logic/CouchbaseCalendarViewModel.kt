package com.bignerdranch.android.calendarapp3.buisness_logic

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
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

    private val _attachmentsForSelectedEntry =
        MutableStateFlow<List<CouchBaseLiteDao.CblAttachmentMeta>>(emptyList())
    val attachmentsForSelectedEntry: StateFlow<List<CouchBaseLiteDao.CblAttachmentMeta>> =
        _attachmentsForSelectedEntry

    private val _editLoadStatus = MutableStateFlow<String?>(null)
    val editLoadStatus: StateFlow<String?> = _editLoadStatus





    init {
        // Open/create DB + collections as early as possible so the UI doesn't need an "Initialize" button.
        initializeCalendarDatabase()
    }

    data class CblEditUi(
        val content: String,
        val timeMinutes: Int?,
        val repeat: String?,
        val reminderType: String?,
        val repeatDetails: String?
    )


    private val _editingEntry = MutableStateFlow<CblEditUi?>(null)
    val editingEntry: StateFlow<CblEditUi?> = _editingEntry


    fun loadEntryForEdit(entryId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _editLoadStatus.value = "Loading id=$entryId"
                val mgr = CouchBaseLiteDao.getInstance(getApplication())
                val map = mgr.getEntryById(entryId)

                if (map == null) {
                    _editingEntry.value = null
                    _editLoadStatus.value = "NOT FOUND id=$entryId"
                    return@launch
                }

                _editingEntry.value = CblEditUi(
                    content = map["entryDB"] as? String ?: "",
                    timeMinutes = map["timeMinutes"] as? Int,
                    repeat = map["repeat"] as? String,
                    reminderType = map["reminderType"] as? String,
                    repeatDetails = map["repeatDetails"] as? String
                )


                _editLoadStatus.value = "Loaded OK"
            } catch (e: Exception) {
                _editingEntry.value = null
                _editLoadStatus.value = "Error: ${e.message}"
                Log.e("CouchbaseCalendar", "loadEntryForEdit failed", e)
            }
        }
    }




    fun loadAttachmentsForEntry(entryId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val mgr = CouchBaseLiteDao.getInstance(getApplication())
                val list = mgr.listAttachmentsForEntry(entryId)
                _attachmentsForSelectedEntry.value = list
            } catch (e: Exception) {
                Log.e("CouchbaseCalendar", "Failed to load attachments", e)
            }
        }
    }

    fun addAttachmentToEntry(entryId: String, uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val mgr = CouchBaseLiteDao.getInstance(getApplication())
                mgr.addAttachmentToEntry(entryId, getApplication(), uri)
                // refresh
                _attachmentsForSelectedEntry.value = mgr.listAttachmentsForEntry(entryId)
            } catch (e: Exception) {
                Log.e("CouchbaseCalendar", "Failed to add attachment", e)
            }
        }
    }

    fun removeAttachmentFromEntry(entryId: String, attachmentId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val mgr = CouchBaseLiteDao.getInstance(getApplication())
                mgr.removeAttachmentFromEntry(entryId, attachmentId)
                _attachmentsForSelectedEntry.value = mgr.listAttachmentsForEntry(entryId)
            } catch (e: Exception) {
                Log.e("CouchbaseCalendar", "Failed to remove attachment", e)
            }
        }
    }

    /**
     * Returns a FileProvider URI for opening/sharing.
     * Caller should launch intent on Main thread.
     */
    suspend fun getOpenableUriForAttachment(entryId: String, attachmentId: String): Uri {
        return withContext(Dispatchers.IO) {
            val mgr = CouchBaseLiteDao.getInstance(getApplication())
            val file = mgr.materializeAttachmentToTempFile(entryId, attachmentId, getApplication())
            FileProvider.getUriForFile(
                getApplication(),
                //"${getApplication<Application>().packageName}.provider",
                "${getApplication<Application>().packageName}.fileprovider",
                file
            )
        }
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
        hasExtraData: Boolean,
        reminderType: String?,
        repeat: String?,
        repeatDetails: String?,
        onUpdated: () -> Unit = {}
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val mgr = CouchBaseLiteDao.getInstance(getApplication())

                if (!_isReady.value) {
                    mgr.createCalendarDb()
                    mgr.createCalendarCollections()
                    _isReady.value = true
                }

                // 1) Update the entry doc
                mgr.updateCalendarEntry(
                    entryId = entryId,
                    dateDB = date,
                    entryDB = content,
                    timeMinutes = timeMinutes
                )

                // 2) Update/insert extra_data doc
                if (hasExtraData) {
                    mgr.upsertExtraDataForEntry(
                        entryId = entryId,
                        reminderType = reminderType,
                        repeat = repeat,
                        repeatDetails = repeatDetails
                    )
                } else {
                    // Optional: if you support clearing extra data when user sets None/Never
                    // mgr.deleteExtraDataForEntry(entryId)
                }

                withContext(Dispatchers.Main) { onUpdated() }
                Log.i("CouchbaseCalendar", "Calendar entry updated (with extra): $entryId")
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


    fun deleteEntry(entryId: String, date: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val mgr = CouchBaseLiteDao.getInstance(getApplication())
                mgr.deleteEntry(entryId)
                loadEntriesForDate(date)
            } catch (e: Exception) {
                Log.e("CouchbaseCalendar", "Failed to delete entry", e)
            }
        }
    }

}