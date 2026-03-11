package com.bignerdranch.android.calendarapp3.database.DAO.objectbox

import android.util.Log
import com.bignerdranch.android.calendarapp3.database.objectbox.domain.model.EntryAttachmentOb
import com.bignerdranch.android.calendarapp3.database.objectbox.domain.model.EntryAttachmentOb_
import com.bignerdranch.android.calendarapp3.database.objectbox.domain.model.EntryOb
import io.objectbox.BoxStore

class ObjectBoxAttachmentRepository(store: BoxStore) {

    private val attachBox = store.boxFor(EntryAttachmentOb::class.java)
    private val entryBox = store.boxFor(EntryOb::class.java)

    fun insert(attachment: EntryAttachmentOb, entryId: Long): Long {
        val entry = entryBox.get(entryId) ?: error("No EntryOb with id=$entryId")
        attachment.entryOb.target = entry
        attachBox.put(attachment)
        return attachment.id
    }

    fun getAttachmentsForEntry(entryId: Long): List<EntryAttachmentOb> =
        attachBox.query(EntryAttachmentOb_.entryObId.equal(entryId))
            .build()
            .find()

    fun getAttachmentById(attachmentId: Long): EntryAttachmentOb? =
        attachBox.get(attachmentId)

    fun delete(attachment: EntryAttachmentOb) {
        attachBox.remove(attachment.id)
    }
}