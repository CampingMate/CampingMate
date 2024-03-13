package com.brandon.campingmate.domain.usecase

import android.net.Uri
import com.brandon.campingmate.domain.repository.PostRepository

class UploadPostImagesUseCase(private val postRepository: PostRepository) {
    suspend operator fun invoke(
        imageUris: List<Uri>, onSuccess: (List<String>) -> Unit, onFailure: (Exception) -> Unit
    ) {
        postRepository.uploadPostImage(
            imageUris, onSuccess, onFailure
        )
    }
}