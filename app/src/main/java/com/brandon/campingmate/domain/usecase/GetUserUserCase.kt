package com.brandon.campingmate.domain.usecase

import com.brandon.campingmate.domain.model.User
import com.brandon.campingmate.domain.repository.UserRepository

class GetUserUserCase(private val userRepository: UserRepository) {
    suspend operator fun invoke(): Result<User?> {
        val userId = userRepository.getUserId()

        return if (userId != null) {
            userRepository.getUserById(userId)
        } else {
            Result.success(null)
        }
    }
}