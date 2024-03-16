package com.brandon.campingmate.data.remote.impl

import androidx.security.crypto.EncryptedSharedPreferences
import com.brandon.campingmate.data.remote.EncryptedSharedPreferencesDataSource

class EncryptedSharedPreferencesDataSourceImpl(private val sharedPreferences: EncryptedSharedPreferences) :
    EncryptedSharedPreferencesDataSource {
    override fun getUserId(): String? {
        return sharedPreferences.getString("myID", null)
    }
}