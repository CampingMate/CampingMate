package com.brandon.campingmate.domain.usecase

import com.brandon.campingmate.domain.model.PostEntity
import com.brandon.campingmate.domain.repository.PostRepository

class UploadPostUseCase(
    private val postRepository: PostRepository
) {
    suspend operator fun invoke(
        postEntity: PostEntity,
        onSuccess: (String) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        postRepository.uploadPost(postEntity, onSuccess, onFailure)
    }
}