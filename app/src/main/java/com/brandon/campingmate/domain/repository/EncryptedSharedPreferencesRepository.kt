package com.brandon.campingmate.domain.repository

interface EncryptedSharedPreferencesRepository {
    fun getUserId(): String?
}