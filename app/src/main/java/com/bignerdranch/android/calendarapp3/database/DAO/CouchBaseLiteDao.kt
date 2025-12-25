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
import com.google.gson.GsonBuilder

class CouchBaseLiteDao {


    // Add these methods to CouchBaseLiteDao class:

    // Create a calendar-specific database (call this instead of createDb)
    fun createCalendarDb() {
        database = Database("calendar_db")  // Different name from example
        Log.i(TAG, "Calendar Database created: calendar_db")
    }

    // Create calendar collections
    fun createCalendarCollections() {
        // Main entries collection
        collection = database!!.createCollection("entries")

        // Extra data collection
        database!!.createCollection("extra_data")

        // Recurring events collection
        database!!.createCollection("recurring_events")

        // Attachments collection
        database!!.createCollection("attachments")

        Log.i(TAG, "Calendar collections created")
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

    // Create a calendar entry (like SQLite insert)
    fun createCalendarEntry(
        dateDB: String,
        entryDB: String,
        timeMinutes: Int? = null
    ): String {
        val mutableDocument = MutableDocument()
            .setString("type", "entry")
            .setString("dateDB", dateDB)
            .setString("entryDB", entryDB)

        if (timeMinutes != null) {
            mutableDocument.setInt("timeMinutes", timeMinutes)
        }

        val entriesCollection = getEntriesCollection()
        entriesCollection?.save(mutableDocument)
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

        val extraDataCollection = getExtraDataCollection()
        extraDataCollection?.save(mutableDocument)

        // Update the entry with extra data reference
        val entriesCollection = getEntriesCollection()
        entriesCollection?.getDocument(entryId)?.let { entryDoc ->
            entriesCollection.save(
                entryDoc.toMutable().setString("extraDataId", mutableDocument.id)
            )
        }

        Log.i(TAG, "Extra data added for entry: $entryId")
        return mutableDocument.id
    }

    // Query entries by date (like SQLite getEntriesByDate)
    fun getEntriesByDate(date: String): List<Map<String, Any>> {
        val entriesCollection = getEntriesCollection() ?: return emptyList()

        val query = QueryBuilder.select(SelectResult.all())
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
                result.toMap().forEach { (key, value) ->
                    docMap[key] = value
                }
                // Add document ID
                result.getString("id")?.let { docMap["_id"] = it }
                results.add(docMap)
            }
        }

        Log.i(TAG, "Found ${results.size} entries for date: $date")
        return results
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
        val entriesCollection = getEntriesCollection() ?: throw IllegalStateException("Entries collection not initialized")

        val query = QueryBuilder.select(SelectResult.all())
            .from(DataSource.collection(entriesCollection))
            .where(Expression.property("type").equalTo(Expression.string("entry")))

        val results = mutableListOf<Map<String, Any>>()
        query.execute().use { resultSet ->
            resultSet.forEach { result ->
                val docMap = mutableMapOf<String, Any>()
                result.toMap().forEach { (key, value) ->
                    docMap[key] = value
                }
                // Add document ID if available
                result.getString("id")?.let { docMap["_id"] = it }

                // Get related extra data if exists
                val extraDataId = result.getString("extraDataId")
                if (extraDataId != null) {
                    val extraDataCollection = getExtraDataCollection()
                    extraDataCollection?.getDocument(extraDataId)?.let { extraDoc ->
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
        val entriesCollection = getEntriesCollection() ?: return
        val extraDataCollection = getExtraDataCollection()

        val query = QueryBuilder.select(SelectResult.all())
            .from(DataSource.collection(entriesCollection))
            .where(Expression.property("type").equalTo(Expression.string("entry")))

        query.execute().use { resultSet ->
            Log.d(TAG, "=== CALENDAR DATABASE CONTENTS ===")
            Log.d(TAG, "Total calendar entries: ${resultSet.allResults().size}")

            resultSet.forEachIndexed { index, result ->
                Log.d(TAG, "=== Calendar Entry $index ===")
                Log.d(TAG, "ID: ${result.getString("id")}")
                Log.d(TAG, "Date: ${result.getString("dateDB")}")
                Log.d(TAG, "Content: ${result.getString("entryDB")}")
                Log.d(TAG, "Time: ${result.getInt("timeMinutes")}")

                // Show extra data if exists
                val extraDataId = result.getString("extraDataId")
                if (extraDataId != null && extraDataCollection != null) {
                    extraDataCollection.getDocument(extraDataId)?.let { extraDoc ->
                        Log.d(TAG, "--- Extra Data ---")
                        extraDoc.toMap().forEach { (key, value) ->
                            if (key != "id") {
                                Log.d(TAG, "$key: $value")
                            }
                        }
                    }
                }
                Log.d(TAG, "")
            }
        }
    }
}