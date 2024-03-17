package com.brandon.campingmate.domain.repository

import com.brandon.campingmate.domain.model.User

interface UserRepository {
    fun getUserId(): String?
    suspend fun getUserById(userId: String): Result<User?>
}