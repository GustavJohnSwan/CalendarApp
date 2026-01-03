package com.bignerdranch.android.calendarapp3.database.DAO

import android.content.Context
import android.util.Log
import com.couchbase.lite.BasicAuthenticator
import com.couchbase.lite.Collection
import com.couchbase.lite.CollectionConfiguration
import com.couchbase.lite.CouchbaseLite
import com.couchbase.lite.DataSource
import com.couchbase.lite.Database
import com.couchbase.lite.Expression
import com.couchbase.lite.MutableDocument
import com.couchbase.lite.Query
import com.couchbase.lite.QueryBuilder
import com.couchbase.lite.Replicator
import com.couchbase.lite.ReplicatorChange
import com.couchbase.lite.ReplicatorConfigurationFactory
import com.couchbase.lite.ReplicatorType
import com.couchbase.lite.SelectResult
import com.couchbase.lite.URLEndpoint
import com.couchbase.lite.newConfig
import com.couchbase.lite.replicatorChangesFlow
import kotlinx.coroutines.flow.Flow
import java.net.URI
import java.util.concurrent.atomic.AtomicReference
import kotlin.use

// Add these imports at the TOP of the file:
import android.content.ClipboardManager
import android.content.ClipData
import android.os.Handler
import android.widget.Toast
import com.couchbase.lite.Ordering
import com.couchbase.lite.Meta
import com.google.gson.GsonBuilder


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




    /* _________________________________________________________________________________________ */
    /* _________________________________________________________________________________________ */
    /* _________________________________________________________________________________________ */

    /* Data model + mechanics for option 1 - attachment functionality one document = entry data + blob */


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

        // Read bytes on IO (caller should already be on Dispatchers.IO)
        val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
            ?: throw IllegalStateException("Unable to read attachment bytes")

        val id = UUID.randomUUID().toString()
        val meta = CblAttachmentMeta(id = id, name = name, mime = mime, size = bytes.size.toLong())

        // 1) Update metadata array
        val attachments = (mutable.getArray(FIELD_ATTACHMENTS)?.toMutable() ?: MutableArray())
        val md = MutableDictionary()
            .setString("id", meta.id)
            .setString("name", meta.name)
            .setString("mime", meta.mime)
            .setLong("size", meta.size)
        attachments.addDictionary(md)
        mutable.setArray(FIELD_ATTACHMENTS, attachments)

        // 2) Store blob inside the same entry document
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

        // Remove metadata from array
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

        // Remove blob property
        mutable.remove(blobKey(attachmentId))

        entriesCollection.save(mutable)
        Log.i(TAG, "CBL attachment removed from entry=$entryId attachmentId=$attachmentId")
    }

    /**
     * Writes the blob to a temp file and returns that file.
     * UI can share/open it via FileProvider.
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

        // Ensure unique filename to avoid collisions
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

    /* _________________________________________________________________________________________ */
    /* _________________________________________________________________________________________ */
    /* _________________________________________________________________________________________ */

    /* _________________________________________________________________________________________ */
    /* _________________________________________________________________________________________ */
    /* _________________________________________________________________________________________ */


    // Add these methods to CouchBaseLiteDao class:

    // Create a calendar-specific database (call this instead of createDb)
    fun createCalendarDb() {
        if (database == null) {
            database = Database("calendar_db")
            Log.i(TAG, "Calendar Database opened/created: calendar_db")
        } else {
            Log.i(TAG, "Calendar Database already open: calendar_db")
        }
    }

    // Create calendar collections
    fun createCalendarCollections() {
    val db = database ?: throw IllegalStateException("Call createCalendarDb() first")

    if (db.getCollection("entries") == null) db.createCollection("entries")
    if (db.getCollection("extra_data") == null) db.createCollection("extra_data")
    if (db.getCollection("recurring_events") == null) db.createCollection("recurring_events")
    if (db.getCollection("attachments") == null) db.createCollection("attachments")

    Log.i(TAG, "Calendar collections ensured")
}

    // Get specific collections
    fun getEntriesCollection(): Collection? {
        return database?.getCollection("entries", "_default")
    }

    fun getExtraDataCollection(): Collection? {
        return database?.getCollection("extra_data", "_default")
    }

    fun getRecurringEventsCollection(): Collection? {
        return database?.getCollection("recurring_events", "_default")
    }


/**
 * Ensures the calendar DB + required collections exist.
 * This keeps the UI simple (no separate init button required).
 *
 * @return Pair(entriesCollection, extraDataCollection)
 */
private fun ensureCalendarReady(): Pair<Collection, Collection> {
    if (database == null) {
        database = Database("calendar_db")
        Log.i(TAG, "Calendar Database opened/created: calendar_db")
    }

    val db = database!!

    val entries = db.getCollection("entries") ?: db.createCollection("entries")
    val extra = db.getCollection("extra_data") ?: db.createCollection("extra_data")

    // Optional future collections
    if (db.getCollection("recurring_events") == null) db.createCollection("recurring_events")
    if (db.getCollection("attachments") == null) db.createCollection("attachments")

    return entries to extra
}

    // Create a calendar entry (like SQLite insert)
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


    // Add extra data to an entry
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


    // Query entries by date (like SQLite getEntriesByDate)
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
            .setString("type", "entry")      // keep stable
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



    //______________________________________________________________________________________________
    //______________________________________________________________________________________________
    //______________________________________________________________________________________________
    private var database: Database? = null
    private var collection: Collection? = null
    private var replicator: Replicator? = null

    // tag::getting-started[]

    // <.>
    // One-off initialization
    private fun init(context: Context) {
        CouchbaseLite.init(context)
        Log.i(TAG, "CBL Initialized")

    }

    // <.>
    // Create a database
    fun createDb(dbName: String) {
        database = Database(dbName)
        Log.i(TAG, "Database created: $dbName")
    }

    // <.>
    // Create a new named collection (like a SQL table)
    // in the database's default scope.
    fun createCollection(collName: String) {
        collection = database!!.createCollection(collName)
        Log.i(TAG, "Collection created: $collection")
    }

    // <.>
    // Create a new document (i.e. a record)
    // and save it in a collection in the database.
    fun createDoc(): String {
        val mutableDocument = MutableDocument()
            .setFloat("version", 2.0f)
            .setString("language", "Java")
        collection?.save(mutableDocument)
        return mutableDocument.id
    }

    // <.>
    // Retrieve immutable document and log the database generated
    // document ID and some document properties
    fun retrieveDoc(docId: String) {
        collection?.getDocument(docId)
            ?.let {
                Log.i(TAG, "Document ID :: ${docId}")
                Log.i(TAG, "Learning :: ${it.getString("language")}")
            }
            ?: Log.i(TAG, "No such document :: $docId")
    }

    // <.>
    // Retrieve immutable document and update `language` property
    // document ID and some document properties
    fun updateDoc(docId: String) {
        collection?.getDocument(docId)?.let {
            collection?.save(
                it.toMutable().setString("language", "Kotlin")
            )
        }
    }

    // <.>
    // Create a query to fetch documents with language == Kotlin.
    fun queryDocs() {
        val coll = collection ?: return
        val query: Query = QueryBuilder.select(SelectResult.all())
            .from(DataSource.collection(coll))
            .where(Expression.property("language").equalTo(Expression.string("Kotlin")))
        query.execute().use { rs ->
            Log.i(TAG, "Number of rows :: ${rs.allResults().size}")
        }
    }

    // <.>
    // OPTIONAL -- if you have Sync Gateway Installed you can try replication too.
    // Create a replicator to push and pull changes to and from the cloud.
    // Be sure to hold a reference to the Replicator to prevent it from being GCed
    fun replicate(): Flow<ReplicatorChange>? {
        val coll = collection ?: return null

        val collConfig = CollectionConfiguration()
            .setPullFilter { doc, _ -> "Java" == doc.getString("language") }

        // this value is not needed, because we will not synch to server
        val repl = Replicator(
            ReplicatorConfigurationFactory.newConfig(
                target = URLEndpoint(URI("ws://localhost:4984/getting-started-db")),
                collections = mapOf(setOf(coll) to collConfig),
                type = ReplicatorType.PUSH_AND_PULL,
                authenticator = BasicAuthenticator("sync-gateway", "password".toCharArray())
            )
        )

        // Listen to replicator change events.
        val changes = repl.replicatorChangesFlow()

        // Start replication.
        repl.start()
        replicator = repl

        return changes
    }
    // end::getting-started[]

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

    // Add this ONE method to your existing CouchBaseLiteDao class:
    /*
    fun logDatabaseContents() {
        val collection = collection ?: return

        val query = QueryBuilder.select(SelectResult.all())
            .from(DataSource.collection(collection))

        query.execute().use { resultSet ->
            Log.d(TAG, "=== DATABASE CONTENTS ===")
            Log.d(TAG, "Total documents: ${resultSet.allResults().size}")

            resultSet.forEachIndexed { index, result ->
                Log.d(TAG, "=== Document $index ===")
                result.toMap().forEach { (key, value) ->
                    Log.d(TAG, "$key: $value")
                }
                Log.d(TAG, "")
            }
        }
    }
     */

    // 1. Get collection (helper function)
    fun getCollection(): Collection? {
        return collection
    }

    // 2. Export database to JSON string
    fun exportDatabaseToJson(context: Context): String {
        val collection = collection ?: throw IllegalStateException("Collection not initialized")

        val query = QueryBuilder.select(SelectResult.all())
            .from(DataSource.collection(collection))

        val results = mutableListOf<Map<String, Any>>()
        query.execute().use { resultSet ->
            resultSet.forEach { result ->
                val docMap = mutableMapOf<String, Any>()
                result.toMap().forEach { (key, value) ->
                    docMap[key] = value
                }
                // Add document ID if available
                result.getString("id")?.let { docMap["_documentId"] = it }
                results.add(docMap)
            }
        }

        val gson = GsonBuilder().setPrettyPrinting().create()
        return gson.toJson(results)
    }

    // 3. Share export (copy to clipboard)
    fun shareDatabaseExport(context: Context) {
        try {
            val jsonContent = exportDatabaseToJson(context)

            // Copy to clipboard
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Couchbase Export", jsonContent)
            clipboard.setPrimaryClip(clip)

            // Show toast
            Handler(context.mainLooper).post {
                Toast.makeText(
                    context,
                    "Database JSON copied to clipboard",
                    Toast.LENGTH_LONG
                ).show()
            }

            // Log to console
            Log.i(TAG, "=== DATABASE JSON EXPORT ===")
            Log.i(TAG, jsonContent)

        } catch (e: Exception) {
            Log.e(TAG, "Failed to export database", e)
            Handler(context.mainLooper).post {
                Toast.makeText(
                    context,
                    "Export failed: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    // 4. Log database contents (simple version)
    fun logDatabaseContents() {
        val collection = collection ?: return

        val query = QueryBuilder.select(SelectResult.all())
            .from(DataSource.collection(collection))

        query.execute().use { resultSet ->
            Log.d(TAG, "=== DATABASE CONTENTS ===")
            Log.d(TAG, "Total documents: ${resultSet.allResults().size}")

            resultSet.forEachIndexed { index, result ->
                Log.d(TAG, "=== Document $index ===")
                result.toMap().forEach { (key, value) ->
                    Log.d(TAG, "$key: $value")
                }
                Log.d(TAG, "")
            }
        }
    }
    // Add this method to export calendar-specific data
    fun exportCalendarDatabaseToJson(context: Context): String {
    val (entriesCollection, extraDataCollection) = ensureCalendarReady()

    val query = QueryBuilder
        .select(
            SelectResult.expression(Meta.id).`as`("id"),
            SelectResult.all()
        )
        .from(DataSource.collection(entriesCollection))
        .where(Expression.property("type").equalTo(Expression.string("entry")))

    val results = mutableListOf<Map<String, Any>>()
    query.execute().use { resultSet ->
        resultSet.forEach { result ->
            val docMap = mutableMapOf<String, Any>()
            val props = result.getDictionary(entriesCollection.name)?.toMap() ?: emptyMap()
            docMap.putAll(props)
            val id = result.getString("id")
            if (id != null) docMap["_id"] = id

            val extraDataId = props["extraDataId"] as? String
            if (extraDataId != null) {
                extraDataCollection.getDocument(extraDataId)?.let { extraDoc ->
                    docMap["extraData"] = extraDoc.toMap()
                }
            }

            results.add(docMap)
        }
    }

    val gson = GsonBuilder().setPrettyPrinting().create()
    return gson.toJson(results)
}


    // Add this method to share calendar export
    fun shareCalendarDatabaseExport(context: Context) {
        try {
            val jsonContent = exportCalendarDatabaseToJson(context)

            // Copy to clipboard
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Couchbase Calendar Export", jsonContent)
            clipboard.setPrimaryClip(clip)

            // Show toast
            Handler(context.mainLooper).post {
                Toast.makeText(
                    context,
                    "Calendar data JSON copied to clipboard",
                    Toast.LENGTH_LONG
                ).show()
            }

            // Log to console
            Log.i(TAG, "=== CALENDAR DATABASE JSON EXPORT ===")
            Log.i(TAG, jsonContent)

        } catch (e: Exception) {
            Log.e(TAG, "Failed to export calendar database", e)
            Handler(context.mainLooper).post {
                Toast.makeText(
                    context,
                    "Calendar export failed: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    // Add this method to log calendar contents
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





}