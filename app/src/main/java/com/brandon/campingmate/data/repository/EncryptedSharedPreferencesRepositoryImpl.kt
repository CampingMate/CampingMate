package com.brandon.campingmate.data.repository

import com.brandon.campingmate.data.remote.EncryptedSharedPreferencesDataSource
import com.brandon.campingmate.domain.repository.EncryptedSharedPreferencesRepository

class EncryptedSharedPreferencesRepositoryImpl(private val encryptedSharedPreferencesDataSource: EncryptedSharedPreferencesDataSource) :
    EncryptedSharedPreferencesRepository {
    override fun getUserId(): String? {
        return encryptedSharedPreferencesDataSource.getUserId()
    }
}