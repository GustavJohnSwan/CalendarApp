package com.bignerdranch.android.calendarapp3.database.couchbase_lite


// Couchbase document structure for calendar entries
data class CouchbaseEntry(
    val id: String,  // Couchbase document ID
    val dateDB: String,
    val entryDB: String,
    val timeMinutes: Int? = null,
    val extraDataId: String? = null  // Reference to extra data document
)

// Couchbase document structure for extra data
data class CouchbaseExtraData(
    val id: String,  // Couchbase document ID
    val entryId: String,  // Reference to main entry
    val reminderType: String? = null,
    val repeat: String? = null,
    val repeatDetails: String? = null
)

// Couchbase document structure for recurring events
data class CouchbaseRecurringEvent(
    val id: String,  // Couchbase document ID
    val entryId: String,
    val occurrenceDate: String
)

// Couchbase document structure for attachments
data class CouchbaseAttachment(
    val id: String,  // Couchbase document ID
    val entryId: String,
    val filePath: String,
    val mimeType: String
)
