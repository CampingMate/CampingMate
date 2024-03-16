package com.brandon.campingmate.domain.usecase

import android.net.Uri
import com.brandon.campingmate.domain.model.Post
import com.brandon.campingmate.domain.repository.PostRepository

class UploadPostUseCase(
    private val postRepository: PostRepository
) {
    suspend operator fun invoke(post: Post, imageUris: List<Uri>): Result<String> {
        return postRepository.uploadPostWithImages(post, imageUris)
    }
}