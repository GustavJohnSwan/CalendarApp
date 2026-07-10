package com.bignerdranch.android.calendarapp3.database_2.objectbox

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import com.bignerdranch.android.calendarapp3.database.objectbox.ObjectBoxProvider

class OB_buisness_logic (application: Application) : AndroidViewModel(application){

    private val store = ObjectBoxProvider.get()
    private val repo = OB_DAO(store)

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
}