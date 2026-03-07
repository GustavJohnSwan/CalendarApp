package com.bignerdranch.android.calendarapp3.buisness_logic.objectbox.attachment


import android.app.Application
import android.net.Uri
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.bignerdranch.android.calendarapp3.database.objectbox.ObjectBoxProvider
import com.bignerdranch.android.calendarapp3.database.objectbox.domain.model.EntryAttachmentOb
import com.bignerdranch.android.calendarapp3.buisness_logic.objectbox.attachment.ObjectBoxAttachmentRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

class ObjectBoxAttachmentViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = ObjectBoxAttachmentRepository(
        store = ObjectBoxProvider.get(),
        appContext = application.applicationContext
    )

    private val _attachments = mutableStateOf<List<EntryAttachmentOb>>(emptyList())
    val attachments get() = _attachments

    fun loadAttachmentsForEntry(entryId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            _attachments.value = repo.getAttachmentsForEntry(entryId)
        }
    }

    fun addAttachment(entryId: Long, uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            repo.addAttachment(entryId, uri)
            _attachments.value = repo.getAttachmentsForEntry(entryId)
        }
    }

    fun deleteAttachment(attachmentId: Long, currentEntryId: Long? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            repo.deleteAttachment(attachmentId)
            if (currentEntryId != null) {
                _attachments.value = repo.getAttachmentsForEntry(currentEntryId)
            }
        }
    }

    fun getUriForAttachment(attachment: EntryAttachmentOb): Uri {
        val file = File(attachment.uriPathOb)
        return FileProvider.getUriForFile(
            getApplication(),
            "${getApplication<Application>().packageName}.fileprovider",
            file
        )
    }
}