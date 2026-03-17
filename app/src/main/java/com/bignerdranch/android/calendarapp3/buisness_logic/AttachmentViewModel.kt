package com.bignerdranch.android.calendarapp3.buisness_logic

import android.app.Application
import android.net.Uri
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.bignerdranch.android.calendarapp3.database.AppDatabase
import com.bignerdranch.android.calendarapp3.database.EntryAttachment
import com.bignerdranch.android.calendarapp3.ui_composables.entry_view.entry_functions.AttachmentRepository
import kotlinx.coroutines.launch

class AttachmentViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val attachmentDao = db.attachmentDao()
    private val repository = AttachmentRepository(attachmentDao, application)

    // State for the current entry's attachments
    private val _attachments = mutableStateOf<List<EntryAttachment>>(emptyList())
    val attachments get() = _attachments

    fun loadAttachmentsForEntry(entryId: Int) {
        viewModelScope.launch {
            _attachments.value = repository.getAttachmentsForEntry(entryId)
        }
    }

    fun addAttachment(entryId: Int, uri: Uri) {
        viewModelScope.launch {
            repository.addAttachment(entryId, uri)?.let { newAttachment ->
                // Successfully added, reload the list
                loadAttachmentsForEntry(entryId)
            }
        }
    }

    fun deleteAttachment(attachmentId: Long) {
        viewModelScope.launch {
            repository.deleteAttachment(attachmentId)
        }
    }

    fun getUriForAttachment(attachment: EntryAttachment): Uri {
        return repository.getShareableUri(attachment)
    }
}