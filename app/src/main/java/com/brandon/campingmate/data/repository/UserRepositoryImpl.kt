package com.brandon.campingmate.data.repository

import com.brandon.campingmate.data.local.preferences.PreferencesDataSource
import com.brandon.campingmate.data.remote.firestore.FirestoreDataSource
import com.brandon.campingmate.domain.model.User
import com.brandon.campingmate.domain.repository.UserRepository
import com.brandon.campingmate.utils.mappers.toUser

class UserRepositoryImpl(
    private val sharedPreferencesDataSource: PreferencesDataSource,
    private val firestoreDataSource: FirestoreDataSource,
) : UserRepository {

    override fun getUserId(): String? {
        return sharedPreferencesDataSource.getUserId()
    }

    override suspend fun getUserById(userId: String): Result<User?> {
        return firestoreDataSource.getUserById(userId).mapCatching { it?.toUser() }
    }
}