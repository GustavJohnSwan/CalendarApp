package com.bignerdranch.android.calendarapp3.buisness_logic

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.bignerdranch.android.calendarapp3.database.DAO.CouchBaseLiteDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

import android.util.Log


class CouchBaseLiteViewModel(application: Application) : AndroidViewModel(application) {
    // LiveData to track replication state
    val replicationState: MutableLiveData<String> = MutableLiveData("Not Started")

    fun runIt() {
        viewModelScope.launch(Dispatchers.IO) {
            // Use getApplication() instead of application
            val mgr = CouchBaseLiteDao.getInstance(getApplication())
            mgr.createDb("example")
            mgr.createCollection("example")
            val id = mgr.createDoc()
            mgr.retrieveDoc(id)
            mgr.updateDoc(id)
            mgr.queryDocs()

            // Post to LiveData
            replicationState.postValue("Completed - Check Logcat")

            // Show Toast on the main thread
            withContext(Dispatchers.Main) {
                Toast.makeText(
                    getApplication(),
                    "Couchbase operations completed!\nCheck Logcat for details.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
    // Add this ONE method to your existing ViewModel:
    /*
    fun logDatabaseContents() {
        viewModelScope.launch(Dispatchers.IO) {
            val mgr = CouchBaseLiteDao.getInstance(getApplication())
            mgr.logDatabaseContents()
        }
    }

     */


    // Add these methods INSIDE the class:

    // Export to JSON function
    fun exportToJson() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val mgr = CouchBaseLiteDao.getInstance(getApplication())
                mgr.shareDatabaseExport(getApplication())
            } catch (e: Exception) {
                Log.e("CouchBaseLiteViewModel", "Export failed", e)
            }
        }
    }

    // Log database contents function
    fun logDatabaseContents() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val mgr = CouchBaseLiteDao.getInstance(getApplication())
                mgr.logDatabaseContents()
            } catch (e: Exception) {
                Log.e("CouchBaseLiteViewModel", "Failed to log database contents", e)
            }
        }
    }
}