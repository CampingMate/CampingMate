package com.brandon.campingmate.domain.usecase

import com.brandon.campingmate.domain.model.PostCommentEntity
import com.brandon.campingmate.domain.repository.PostRepository

class UploadPostCommentUseCase(
    private val postRepository: PostRepository
) {
    suspend operator fun invoke(postId: String, postCommentEntity: PostCommentEntity) {

    }

}