package com.bignerdranch.android.calendarapp3.database_2.objectbox

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.bignerdranch.android.calendarapp3.database.objectbox.ObjectBoxProvider
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class BBL_OB_CBL_Room (application: Application) : AndroidViewModel(application){


    var benchmarkStatus by mutableStateOf("Ready")
    private set


    //-------------------------------------------------------------------------
    // Objectbox
    private val store = ObjectBoxProvider.get()
    private val repo = OB_DAO(store)


    // generate random entries (right now two fillins)
    fun generateRandomEntries(): List<EntryOb_B> {
        val entries = listOf(
            EntryOb_B(
                dateOb = "2026-07-14",
                entryOb = "Example text 1",
                timeMinutesOb = 630
        ),

            EntryOb_B(
                dateOb = "2026-07-14",
                entryOb = "Example text 2",
                timeMinutesOb = 90
            )
        )
        return entries
    }

    // INSERT BULK
    // date format YYYY-MM-DD
    // time format formula : H*60+M --> 10:30 --> 10*60+30 = 630

    fun insertEntryBulk() {
        val entries = generateRandomEntries()

        Log.d(
            "OB_BENCHMARK",
            "About to insert ${entries.size} entries"
        )

        entries.forEach { entry ->
            Log.d(
                "OB_ADDED",
                "Before insert: $entry"
            )
        }

        repo.insertEntriesBulk(entries)

        entries.forEach { entry ->
            Log.d(
                "OB_ADDED",
                "Inserted: $entry"
            )
        }

        // BULK GET entries
        val allEntries = repo.getAllEntriesBulk()

        Log.d(
            "OB_DATABASE",
            "Current database contains ${allEntries.size} entries"
        )

        allEntries.forEach { entry ->
            Log.d(
                "OB_DATABASE",
                "Stored: $entry"
            )
        }



    }










/*
    fun universalPlaceholder() {


        val user = User(name = "Gatis")
        val household = Household(
            address = "Test address",
            color = "Blue",
            stories = 2
        )

        val userId = repo.insertUser(user)
        val houseHoldId = repo.insertHousehold(household)

        val allUsers = repo.getAllUsers()
        val allHouseholds = repo.getAllHouseholds()

        val specificUser = repo.getSpecificUser(userId)

        val usersStartingWithG = repo.getUsersType1()

        val householdCount = repo.countHouseholds()


        Log.d("OB_TEST", "Inserted user id: $userId")
        //Log.d("OB_TEST", "Inserted household id: $householdId")
        Log.d("OB_TEST", "All users: $allUsers")
        Log.d("OB_TEST", "All households: $allHouseholds")
        Log.d("OB_TEST", "Specific user: $specificUser")
        Log.d("OB_TEST", "Users starting with G: $usersStartingWithG")
        Log.d("OB_TEST", "Household count: $householdCount")
    }

 */
}