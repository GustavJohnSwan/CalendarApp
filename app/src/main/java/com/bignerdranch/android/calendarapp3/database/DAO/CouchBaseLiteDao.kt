package com.bignerdranch.android.calendarapp3.database.DAO

import android.content.Context
import android.util.Log
import com.couchbase.lite.Collection
import com.couchbase.lite.CouchbaseLite
import com.couchbase.lite.DataSource
import com.couchbase.lite.Database
import com.couchbase.lite.Expression
import com.couchbase.lite.MutableDocument
import com.couchbase.lite.QueryBuilder
import com.couchbase.lite.SelectResult
import java.util.concurrent.atomic.AtomicReference
import kotlin.use


import com.couchbase.lite.Ordering
import com.couchbase.lite.Meta


import android.database.Cursor
import android.net.Uri
import android.provider.OpenableColumns
import com.couchbase.lite.Blob
import com.couchbase.lite.MutableArray
import com.couchbase.lite.MutableDictionary
import java.io.File
import java.io.FileOutputStream
import java.util.UUID


class CouchBaseLiteDao {

// =========================================================
// Internal models and helper constants
// Defines attachment metadata and helper keys used to store
// attachment metadata + blobs inside entry documents.
// =========================================================


    data class CblAttachmentMeta(
        val id: String,
        val name: String,
        val mime: String,
        val size: Long
    )

    private val FIELD_ATTACHMENTS = "attachments"
    private val BLOB_KEY_PREFIX = "att_"
    private val BLOB_KEY_SUFFIX = "_blob"

    private fun blobKey(id: String) = "$BLOB_KEY_PREFIX$id$BLOB_KEY_SUFFIX"


    private fun getFileName(context: Context, uri: Uri): String {
        var name = "attachment"
        val cursor: Cursor? = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            val idx = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (idx >= 0 && it.moveToFirst()) name = it.getString(idx)
        }
        return name
    }


    // =========================================================
// Database structure / schema-by-code
// Couchbase Lite does not use fixed entity schema files like
// Room. Instead, the database name, collections, and document
// structure are defined programmatically in this DAO.
// Active collections:
// - entries: main calendar entry documents
// - extra_data: reminder / repeat metadata documents
// =========================================================

    private var database: Database? = null

    // Creates a calendar-specific database
    fun createCalendarDb() {
        if (database == null) {
            database = Database("calendar_db")
            Log.i(TAG, "Calendar Database opened/created: calendar_db")
        } else {
            Log.i(TAG, "Calendar Database already open: calendar_db")
        }
    }

    // Creates calendar collections
    fun createCalendarCollections() {
        val db = database ?: throw IllegalStateException("Call createCalendarDb() first")

        if (db.getCollection("entries") == null) db.createCollection("entries")
        if (db.getCollection("extra_data") == null) db.createCollection("extra_data")


        Log.i(TAG, "Calendar collections ensured")
    }


    /*
     Ensures the calendar DB + required collections exist
     */
    private fun ensureCalendarReady(): Pair<Collection, Collection> {
        if (database == null) {
            database = Database("calendar_db")
            Log.i(TAG, "Calendar Database opened/created: calendar_db")
        }

        val db = database!!

        val entries = db.getCollection("entries") ?: db.createCollection("entries")
        val extra = db.getCollection("extra_data") ?: db.createCollection("extra_data")


        return entries to extra
    }


// =========================================================
// Couchbase initialization
// Initializes the Couchbase Lite library once when the DAO
// singleton is first created.
// =========================================================


    // One-off initialization
    private fun init(context: Context) {
        CouchbaseLite.init(context)
        Log.i(TAG, "CBL Initialized")

    }

// =========================================================
// Entry CRUD
// Handles creation, retrieval, updating, deletion, and
// date-based querying of main calendar entry documents.
// =========================================================

    // Creates a calendar entry
    fun createCalendarEntry(
        dateDB: String,
        entryDB: String,
        timeMinutes: Int? = null
    ): String {
        val (entriesCollection, _) = ensureCalendarReady()

        val mutableDocument = MutableDocument()
            .setString("type", "entry")
            .setString("dateDB", dateDB)
            .setString("entryDB", entryDB)

        if (timeMinutes != null) {
            mutableDocument.setInt("timeMinutes", timeMinutes)
        }

        entriesCollection.save(mutableDocument)
        Log.i(TAG, "Calendar entry created with ID: ${mutableDocument.id}")
        return mutableDocument.id
    }

    // Queries entries by date
    fun getEntriesByDate(date: String): List<Map<String, Any>> {
        val (entriesCollection, _) = ensureCalendarReady()

        val query = QueryBuilder
            .select(
                SelectResult.expression(Meta.id).`as`("id"),
                SelectResult.all()
            )
            .from(DataSource.collection(entriesCollection))
            .where(
                Expression.property("dateDB").equalTo(Expression.string(date))
                    .and(Expression.property("type").equalTo(Expression.string("entry")))
            )
            .orderBy(Ordering.property("timeMinutes").ascending())

        val results = mutableListOf<Map<String, Any>>()
        query.execute().use { resultSet ->
            resultSet.forEach { result ->
                val docMap = mutableMapOf<String, Any>()
                val props = result.getDictionary(entriesCollection.name)?.toMap() ?: emptyMap()
                docMap.putAll(props)
                result.getString("id")?.let { docMap["_id"] = it }
                results.add(docMap)
            }
        }

        Log.i(TAG, "Found ${results.size} entries for date: $date")
        return results
    }

    fun getEntryById(entryId: String): Map<String, Any?>? {
        val (entriesCollection, extraDataCollection) = ensureCalendarReady()

        val doc = entriesCollection.getDocument(entryId) ?: return null

        // Default values
        var reminderType: String? = null
        var repeat: String? = null
        var repeatDetails: String? = null

        // Reads linked extra_data doc
        val extraId = doc.getString("extraDataId")
        if (!extraId.isNullOrBlank()) {
            val extraDoc = extraDataCollection.getDocument(extraId)
            reminderType = extraDoc?.getString("reminderType")
            repeat = extraDoc?.getString("repeat")
            repeatDetails = extraDoc?.getString("repeatDetails")
        }

        return mapOf(
            "id" to doc.id,
            "entryDB" to doc.getString("entryDB"),
            "timeMinutes" to (doc.getValue("timeMinutes") as? Number)?.toInt(),
            "reminderType" to reminderType,
            "repeat" to repeat,
            "repeatDetails" to repeatDetails
        )
    }

    fun updateCalendarEntry(
        entryId: String,
        dateDB: String,
        entryDB: String,
        timeMinutes: Int?
    ) {
        val (entriesCollection, _) = ensureCalendarReady()

        val doc = entriesCollection.getDocument(entryId)
            ?: throw IllegalArgumentException("No Couchbase entry with id=$entryId")

        val mutable = doc.toMutable()
            .setString("type", "entry")
            .setString("dateDB", dateDB)
            .setString("entryDB", entryDB)

        if (timeMinutes != null) {
            mutable.setInt("timeMinutes", timeMinutes)
        } else {
            mutable.remove("timeMinutes")
        }

        entriesCollection.save(mutable)
        Log.i(TAG, "Calendar entry updated: $entryId")
    }


    fun deleteEntry(entryId: String) {
        val (entriesCollection, _) = ensureCalendarReady()

        val doc = entriesCollection.getDocument(entryId)
            ?: throw IllegalArgumentException("No Couchbase entry with id=$entryId")

        entriesCollection.delete(doc)

        Log.i(TAG, "CBL entry deleted id=$entryId")
    }


// =========================================================
// Extra data CRUD
// Stores optional reminder / repeat information in a separate
// extra_data document linked to the main entry document.
// =========================================================

    // Adds extra data to an entry
    fun addExtraDataToEntry(
        entryId: String,
        reminderType: String? = null,
        repeat: String? = null,
        repeatDetails: String? = null
    ): String {
        val (entriesCollection, extraDataCollection) = ensureCalendarReady()

        val mutableDocument = MutableDocument()
            .setString("type", "extra_data")
            .setString("entryId", entryId)

        if (reminderType != null) {
            mutableDocument.setString("reminderType", reminderType)
        }

        if (repeat != null) {
            mutableDocument.setString("repeat", repeat)
        }

        if (repeatDetails != null) {
            mutableDocument.setString("repeatDetails", repeatDetails)
        }

        extraDataCollection.save(mutableDocument)

        // Link from entry -> extra_data doc id
        entriesCollection.getDocument(entryId)?.let { entryDoc ->
            entriesCollection.save(
                entryDoc.toMutable().setString("extraDataId", mutableDocument.id)
            )
        }

        Log.i(TAG, "Extra data added for entry: $entryId (extraDataId=${mutableDocument.id})")
        return mutableDocument.id
    }

    fun upsertExtraDataForEntry(
        entryId: String,
        reminderType: String?,
        repeat: String?,
        repeatDetails: String?
    ) {
        val (entriesCollection, extraDataCollection) = ensureCalendarReady()

        val entryDoc = entriesCollection.getDocument(entryId)
            ?: throw IllegalArgumentException("No Couchbase entry with id=$entryId")

        val existingExtraId = entryDoc.getString("extraDataId")

        val extraMutable = if (!existingExtraId.isNullOrBlank()) {
            extraDataCollection.getDocument(existingExtraId)?.toMutable() ?: MutableDocument(existingExtraId)
        } else {
            MutableDocument()
        }

        extraMutable.setString("type", "extra_data")
        extraMutable.setString("entryId", entryId)

        // Stores nulls by removing fields
        if (reminderType.isNullOrBlank()) extraMutable.remove("reminderType")
        else extraMutable.setString("reminderType", reminderType)

        if (repeat.isNullOrBlank() || repeat == "Never") extraMutable.remove("repeat")
        else extraMutable.setString("repeat", repeat)

        if (repeatDetails.isNullOrBlank()) extraMutable.remove("repeatDetails")
        else extraMutable.setString("repeatDetails", repeatDetails)

        extraDataCollection.save(extraMutable)

        // entry links to extra_data doc
        if (existingExtraId.isNullOrBlank()) {
            entriesCollection.save(entryDoc.toMutable().setString("extraDataId", extraMutable.id))
        }
    }


// =========================================================
// Attachment handling
// Stores attachment metadata and blobs inside the main entry
// document, and materializes blobs into temporary files when
// the UI needs to open them.
// =========================================================

    fun listAttachmentsForEntry(entryId: String): List<CblAttachmentMeta> {
        val (entriesCollection, _) = ensureCalendarReady()
        val doc = entriesCollection.getDocument(entryId) ?: return emptyList()

        val arr = doc.getArray(FIELD_ATTACHMENTS) ?: return emptyList()
        val out = mutableListOf<CblAttachmentMeta>()

        for (i in 0 until arr.count()) {
            val d = arr.getDictionary(i) ?: continue
            val id = d.getString("id") ?: continue
            out.add(
                CblAttachmentMeta(
                    id = id,
                    name = d.getString("name") ?: "attachment",
                    mime = d.getString("mime") ?: "*/*",
                    size = d.getLong("size")
                )
            )
        }
        return out
    }

    fun addAttachmentToEntry(entryId: String, context: Context, uri: Uri): CblAttachmentMeta {
        val (entriesCollection, _) = ensureCalendarReady()

        val doc = entriesCollection.getDocument(entryId)
            ?: throw IllegalArgumentException("No Couchbase entry with id=$entryId")

        val mutable = doc.toMutable()

        val name = getFileName(context, uri)
        val mime = context.contentResolver.getType(uri) ?: "*/*"

        // Reads bytes on IO (caller should already be on Dispatchers.IO)
        val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
            ?: throw IllegalStateException("Unable to read attachment bytes")

        val id = UUID.randomUUID().toString()
        val meta = CblAttachmentMeta(id = id, name = name, mime = mime, size = bytes.size.toLong())

        // updates metadata array
        val attachments = (mutable.getArray(FIELD_ATTACHMENTS)?.toMutable() ?: MutableArray())
        val md = MutableDictionary()
            .setString("id", meta.id)
            .setString("name", meta.name)
            .setString("mime", meta.mime)
            .setLong("size", meta.size)
        attachments.addDictionary(md)
        mutable.setArray(FIELD_ATTACHMENTS, attachments)

        // stores blob inside the same entry document
        mutable.setBlob(blobKey(meta.id), Blob(meta.mime, bytes))

        entriesCollection.save(mutable)
        Log.i(TAG, "CBL attachment added to entry=$entryId attachmentId=${meta.id}")
        return meta
    }

    fun removeAttachmentFromEntry(entryId: String, attachmentId: String) {
        val (entriesCollection, _) = ensureCalendarReady()

        val doc = entriesCollection.getDocument(entryId)
            ?: throw IllegalArgumentException("No Couchbase entry with id=$entryId")

        val mutable = doc.toMutable()

        // Removes metadata from array
        val arr = mutable.getArray(FIELD_ATTACHMENTS)
        if (arr != null) {
            val newArr = MutableArray()
            for (i in 0 until arr.count()) {
                val d = arr.getDictionary(i)
                val id = d?.getString("id")
                if (id != attachmentId && d != null) newArr.addDictionary(d)
            }
            mutable.setArray(FIELD_ATTACHMENTS, newArr)
        }

        // Removes blob property
        mutable.remove(blobKey(attachmentId))

        entriesCollection.save(mutable)
        Log.i(TAG, "CBL attachment removed from entry=$entryId attachmentId=$attachmentId")
    }




    /*
     Writes the blob to a temp file and returns that file.
     UI can share/open it via FileProvider.
     */
    fun materializeAttachmentToTempFile(
        entryId: String,
        attachmentId: String,
        context: Context
    ): File {
        val (entriesCollection, _) = ensureCalendarReady()
        val doc = entriesCollection.getDocument(entryId)
            ?: throw IllegalArgumentException("No Couchbase entry with id=$entryId")

        val blob = doc.getBlob(blobKey(attachmentId))
            ?: throw IllegalArgumentException("No blob for attachmentId=$attachmentId")

        val metas = listAttachmentsForEntry(entryId)
        val meta = metas.firstOrNull { it.id == attachmentId }

        val safeName = meta?.name ?: "attachment"
        val outDir = File(context.cacheDir, "cbl_attachments")
        if (!outDir.exists()) outDir.mkdirs()

        // unique filename to avoid collisions
        val outFile = File(outDir, "${UUID.randomUUID()}_$safeName")

        val input = blob.contentStream
            ?: throw IllegalStateException("Blob has no contentStream for attachmentId=$attachmentId")

        input.use { stream ->
            FileOutputStream(outFile).use { output ->
                stream.copyTo(output)
            }
        }


        return outFile
    }

    // =========================================================
// Debug / export helpers
// Utility functions used for inspecting current Couchbase
// calendar contents during development and testing.
// =========================================================

    fun logCalendarDatabaseContents() {
        val (entriesCollection, extraDataCollection) = ensureCalendarReady()

        val query = QueryBuilder
            .select(
                SelectResult.expression(Meta.id).`as`("id"),
                SelectResult.all()
            )
            .from(DataSource.collection(entriesCollection))
            .where(Expression.property("type").equalTo(Expression.string("entry")))

        query.execute().use { resultSet ->
            val all = resultSet.allResults()
            Log.d(TAG, "=== CALENDAR DATABASE CONTENTS ===")
            Log.d(TAG, "Total calendar entries: ${all.size}")

            all.forEachIndexed { index, result ->
                val props = result.getDictionary(entriesCollection.name)?.toMap() ?: emptyMap()
                val id = result.getString("id")
                Log.d(TAG, "=== Calendar Entry $index ===")
                Log.d(TAG, "ID: $id")
                Log.d(TAG, "Date: ${props["dateDB"]}")
                Log.d(TAG, "Content: ${props["entryDB"]}")
                Log.d(TAG, "Time: ${props["timeMinutes"]}")

                val extraDataId = props["extraDataId"] as? String
                if (extraDataId != null) {
                    extraDataCollection.getDocument(extraDataId)?.let { extraDoc ->
                        Log.d(TAG, "--- Extra Data ---")
                        extraDoc.toMap().forEach { (key, value) ->
                            if (key != "id") Log.d(TAG, "$key: $value")
                        }
                    }
                }
                Log.d(TAG, "")
            }
        }
    }









    // =========================================================
// Singleton access
// Provides one shared DAO instance and performs one-time
// Couchbase Lite initialization.
// =========================================================

    companion object {
        private const val TAG = "START_KOTLIN"

        private val INSTANCE = AtomicReference<CouchBaseLiteDao?>()

        @Synchronized
        fun getInstance(context: Context): CouchBaseLiteDao {
            var mgr = INSTANCE.get()
            if (mgr == null) {
                mgr = CouchBaseLiteDao()
                if (INSTANCE.compareAndSet(null, mgr)) {
                    mgr.init(context)
                }
            }
            return INSTANCE.get()!!
        }
    }
}