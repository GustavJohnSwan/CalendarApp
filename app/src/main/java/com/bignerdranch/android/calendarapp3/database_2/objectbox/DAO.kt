package com.bignerdranch.android.calendarapp3.database_2.objectbox

import com.bignerdranch.android.calendarapp3.database.objectbox.domain.model.EntryOb
import io.objectbox.BoxStore


class OB_DAO (store: BoxStore) {

    val userBox = store.boxFor(User::class.java)
    val householdBox = store.boxFor(Household::class.java)

    fun insertUser(entry: User): Long {
        userBox.put(entry)
        return entry.id
    }

    fun insertHousehold(entry: Household): Long {
        householdBox.put(entry)
        return entry.id
    }

    fun getAllUsers(): List<User> {
        return userBox.all
    }

    fun getAllHouseholds(): List<Household> {
        return householdBox.all
    }


    // userId is an argument for calling this function from buisness logic
    fun getSpecificUser(userId: Long): User? {
        return userBox.get(userId)
    }



    // User_ is the metadata created object code that is generated when building a project with objectbox database. You use User to edit the actual object data,
    // you use User_ to query / read already existing data
    fun getUsersType1(): List<User> {
        val query = userBox
            .query(User_.name.startsWith ("G"))
            .order(User_.name)
            .build()
        val results = query.find()
        query.close()
        return results
    }


    fun removeUser(userId: Long): String {
        userBox.remove(userId)
        return "User $userId removed"
    }

    fun removeUserAll(): String {
        userBox.removeAll()
        return "All Users removed"
    }

    fun countHouseholds(): Long {
        return householdBox.count()
    }







}