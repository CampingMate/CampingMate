package com.brandon.campingmate.domain.usecase

import com.brandon.campingmate.domain.repository.EncryptedSharedPreferencesRepository

class GetUserIdUseCase(private val encryptedSharedPreferencesRepository: EncryptedSharedPreferencesRepository) {
    suspend operator fun invoke(): String? {
        return encryptedSharedPreferencesRepository.getUserId()
    }
}