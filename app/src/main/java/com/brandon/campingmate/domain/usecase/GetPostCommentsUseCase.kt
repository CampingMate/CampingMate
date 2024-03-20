package com.brandon.campingmate.domain.usecase

import com.brandon.campingmate.domain.repository.PostRepository
import com.brandon.campingmate.presentation.postdetail.adapter.PostCommentListItem
import com.brandon.campingmate.utils.mappers.toPostCommentListItem

class GetPostCommentsUseCase(
    private val postRepository: PostRepository
) {
    suspend operator fun invoke(
        postId: String?,
        pageSize: Int,
    ): Result<List<PostCommentListItem.PostCommentItem>> {
        if (postId.isNullOrBlank()) throw IllegalArgumentException("Post ID cannot be blank or null.")
        return postRepository.getComments(postId, pageSize).mapCatching { modelList ->
            modelList.map { model -> model.toPostCommentListItem() }
        }
    }
}