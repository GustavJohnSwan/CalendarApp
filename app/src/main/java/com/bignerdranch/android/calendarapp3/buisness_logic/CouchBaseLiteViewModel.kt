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
}