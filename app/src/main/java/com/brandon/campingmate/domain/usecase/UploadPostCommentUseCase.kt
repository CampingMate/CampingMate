package com.brandon.campingmate.domain.usecase

import com.brandon.campingmate.domain.model.PostComment
import com.brandon.campingmate.domain.repository.PostRepository
import com.google.firebase.Timestamp

class UploadPostCommentUseCase(
    private val postRepository: PostRepository
) {
    suspend operator fun invoke(postId: String, comment: String): Result<String> {
        val postComment = PostComment(
            commentId = "OhjH7RyaFCL5NEAVdIa7",
            postId = "fda",
            authorName = "김철수",
            content = comment,
            timestamp = Timestamp.now()
        )
        return postRepository.uploadComment(postId, postComment)
    }
}