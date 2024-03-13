package com.brandon.campingmate.domain.usecase

import android.net.Uri
import com.brandon.campingmate.domain.model.PostEntity
import com.brandon.campingmate.domain.repository.PostRepository

class UploadPostUseCase(
    private val postRepository: PostRepository
) {
    suspend operator fun invoke(
        postEntity: PostEntity,
        imageUris: List<Uri>,
        onSuccess: (String) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        postRepository.uploadPost(postEntity, imageUris, onSuccess, onFailure)
    }
}