package com.bignerdranch.android.calendarapp3.ui_composables.entry_view.entry_functions

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import androidx.core.content.FileProvider
import com.bignerdranch.android.calendarapp3.database.DAO.AttachmentDao
import com.bignerdranch.android.calendarapp3.database.EntryAttachment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID



fun Context.getFileName(uri: Uri): String {
    var name = ""
    val returnCursor: Cursor? = this.contentResolver.query(uri, null, null, null, null)
    returnCursor?.use { cursor ->
        val nameIndex: Int = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        cursor.moveToFirst()
        name = cursor.getString(nameIndex)
    }
    return name
}

class AttachmentRepository(
    private val attachmentDao: AttachmentDao,
    private val context: Context
) {

    companion object {
        private const val TAG = "AttachmentRepository"
    }

    suspend fun getAttachmentsForEntry(entryId: Int): List<EntryAttachment> {
        return withContext(Dispatchers.IO) {
            attachmentDao.getAttachmentsForEntry(entryId)
        }
    }

    suspend fun addAttachment(entryId: Int, originalUri: Uri): EntryAttachment? {
        return withContext(Dispatchers.IO) {
            try {
                // Get file metadata from the original Uri
                val fileName = context.getFileName(originalUri)
                val mimeType = context.contentResolver.getType(originalUri) ?: "*/*"

                // Create a destination directory for this entry's attachments
                val entryAttachmentsDir = File(context.filesDir, "attachments/entry_$entryId")
                if (!entryAttachmentsDir.exists()) {
                    entryAttachmentsDir.mkdirs()
                }

                // Create a unique destination file, keep the original extension
                val fileExtension = fileName.substringAfterLast('.', "")
                val uniqueFileName = "${UUID.randomUUID()}.$fileExtension"
                val destFile = File(entryAttachmentsDir, uniqueFileName)

                // Copy the file content
                context.contentResolver.openInputStream(originalUri)?.use { inputStream ->
                    FileOutputStream(destFile).use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }

                // Get the size of the new file
                val fileSize = destFile.length()

                // Create and insert the Attachment entity into the database
                val newAttachment = EntryAttachment(
                    entryId = entryId,
                    fileName = fileName,
                    mimeType = mimeType,
                    fileSize = fileSize,
                    uriPath = destFile.absolutePath
                )
                val newId = attachmentDao.insert(newAttachment)
                newAttachment.copy(id = newId)

            } catch (e: Exception) {
                Log.e(TAG, "Failed to add attachment", e)
                null
            }
        }
    }

    suspend fun deleteAttachment(attachmentId: Long): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val attachment = attachmentDao.getAttachmentById(attachmentId)
                if (attachment != null) {
                    // Delete the physical file
                    val file = File(attachment.uriPath)
                    if (file.exists()) {
                        file.delete()
                    }
                    // Delete the database record
                    attachmentDao.delete(attachment)
                    true
                } else {
                    false
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to delete attachment", e)
                false
            }
        }
    }

    fun getShareableUri(attachment: EntryAttachment): Uri {
        val file = File(attachment.uriPath)
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    }
}