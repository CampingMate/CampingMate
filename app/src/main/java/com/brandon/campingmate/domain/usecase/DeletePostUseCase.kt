package com.brandon.campingmate.domain.usecase

import com.brandon.campingmate.domain.repository.PostRepository

class DeletePostUseCase(
    private val postRepository: PostRepository
) {
    suspend operator fun invoke(
        postId: String?
    ): Result<String> {
        if (postId.isNullOrBlank()) throw IllegalArgumentException("postId can't be null or blank")
        return postRepository.deletePostById(postId)
    }
}