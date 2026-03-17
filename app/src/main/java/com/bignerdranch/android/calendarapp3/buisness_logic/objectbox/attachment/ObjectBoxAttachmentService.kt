package com.bignerdranch.android.calendarapp3.buisness_logic.objectbox.attachment


import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import com.bignerdranch.android.calendarapp3.database.objectbox.domain.model.EntryAttachmentOb
import com.bignerdranch.android.calendarapp3.database.objectbox.domain.model.EntryAttachmentOb_
import com.bignerdranch.android.calendarapp3.database.objectbox.domain.model.EntryOb
import io.objectbox.BoxStore
import java.io.File
import java.util.UUID

class ObjectBoxAttachmentService(
    private val store: BoxStore,
    private val appContext: Context
) {
    private val attachBox = store.boxFor(EntryAttachmentOb::class.java)
    private val entryBox = store.boxFor(EntryOb::class.java)

    /**
     1) Copy content Uri -> app storage file
     2) Store metadata + local path in ObjectBox
     3) Return created attachment or null if failed
     */
    fun addAttachment(entryId: Long, uri: Uri): EntryAttachmentOb? {
        val entry = entryBox.get(entryId) ?: return null

        val saved = copyToAppStorage(uri) ?: return null

        val ob = EntryAttachmentOb(
            fileNameOb = saved.displayName,
            mimeTypeOb = saved.mimeType,
            fileSizeOb = saved.sizeBytes,
            uriPathOb = saved.file.absolutePath,
            dateAddedOb = System.currentTimeMillis()
        )


        ob.entryOb.target = entry

        // Save attachment to ObjectBox
        attachBox.put(ob)

        logAllAttachments()

        return ob
    }

    fun getAttachmentsForEntry(entryId: Long): List<EntryAttachmentOb> =
        attachBox.query(EntryAttachmentOb_.entryObId.equal(entryId))
            .build()
            .find()



    fun deleteAttachment(attachmentId: Long): Boolean {
        val ob = attachBox.get(attachmentId) ?: return false

        // best-effort file delete
        val path = ob.uriPathOb
        if (!path.isNullOrBlank()) {
            runCatching { File(path).delete() }
        }

        val removed = attachBox.remove(attachmentId)

        logAllAttachments()

        return removed
    }

    // -------------------------
    // File handling
    // -------------------------

    private data class SavedFile(
        val file: File,
        val displayName: String,
        val mimeType: String,
        val sizeBytes: Long
    )

    private fun copyToAppStorage(uri: Uri): SavedFile? {
        val resolver = appContext.contentResolver
        val mimeType = resolver.getType(uri) ?: "application/octet-stream"

        var displayName = "attachment"
        var sizeBytes = 0L

        resolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIdx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            val sizeIdx = cursor.getColumnIndex(OpenableColumns.SIZE)
            if (cursor.moveToFirst()) {
                if (nameIdx >= 0) displayName = cursor.getString(nameIdx) ?: displayName
                if (sizeIdx >= 0) sizeBytes = cursor.getLong(sizeIdx)
            }
        }

        val safeName = displayName.replace(Regex("""[^\w.\- ]"""), "_")
        val targetDir = File(appContext.filesDir, "attachments").apply { mkdirs() }
        val targetFile = File(targetDir, "${UUID.randomUUID()}_$safeName")

        val input = resolver.openInputStream(uri) ?: return null
        input.use { ins ->
            targetFile.outputStream().use { outs ->
                ins.copyTo(outs)
            }
        }

        if (sizeBytes <= 0L) sizeBytes = targetFile.length()

        return SavedFile(
            file = targetFile,
            displayName = displayName,
            mimeType = mimeType,
            sizeBytes = sizeBytes
        )
    }

    private fun logAllAttachments() {
        val attachments = attachBox.all

        Log.d("ObjectBoxTest", "----- OBJECTBOX ATTACHMENT DUMP -----")
        Log.d("ObjectBoxTest", "Total attachments: ${attachments.size}")

        attachments.forEach { att ->
            Log.d(
                "ObjectBoxTest",
                "AttachmentOb(id=${att.id}, entryId=${att.entryOb.targetId}, name=${att.fileNameOb}, mime=${att.mimeTypeOb}, path=${att.uriPathOb})"
            )
        }

        Log.d("ObjectBoxTest", "-------------------------------------")
    }


    fun deleteAttachmentsForEntry(entryId: Long) {
        val attachments = attachBox.query(
            EntryAttachmentOb_.entryObId.equal(entryId)
        ).build().find()

        attachments.forEach { att ->
            val path = att.uriPathOb
            if (!path.isNullOrBlank()) {
                runCatching { File(path).delete() }
            }
            attachBox.remove(att.id)
        }

        logAllAttachments()
    }

}