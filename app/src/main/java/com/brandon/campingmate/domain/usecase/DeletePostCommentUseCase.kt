package com.brandon.campingmate.domain.usecase

import com.brandon.campingmate.domain.repository.PostRepository

class DeletePostCommentUseCase(
    private val postRepository: PostRepository
) {
    suspend operator fun invoke(
        commentId: String?,
        postId: String?,
    ): Result<String> {
        if (commentId.isNullOrBlank()) throw IllegalArgumentException("commentId can't be null or blank")
        if (postId.isNullOrBlank()) throw IllegalArgumentException("postId can't be null or blank")
        return postRepository.deletePostCommentById(commentId, postId)
    }
}