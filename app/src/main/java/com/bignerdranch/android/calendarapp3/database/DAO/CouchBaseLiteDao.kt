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

class CouchBaseLiteDao {
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
}