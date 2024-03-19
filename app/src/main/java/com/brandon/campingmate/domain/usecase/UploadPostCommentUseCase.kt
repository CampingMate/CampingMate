package com.brandon.campingmate.domain.usecase

import com.brandon.campingmate.domain.model.PostComment
import com.brandon.campingmate.domain.model.User
import com.brandon.campingmate.domain.repository.PostRepository
import com.brandon.campingmate.presentation.postdetail.adapter.PostCommentListItem
import com.brandon.campingmate.utils.mappers.toPostCommentListItem
import com.google.firebase.Timestamp

class UploadPostCommentUseCase(
    private val postRepository: PostRepository
) {
    suspend operator fun invoke(postId: String?, user: User?, comment: String): Result<PostCommentListItem> {
        if (postId.isNullOrBlank()) throw IllegalArgumentException("Post ID cannot be null.")
        if (user == null) throw IllegalArgumentException("User cannot be null.")
        val postComment = PostComment(
            commentId = null,
            postId = postId,
            authorName = user.nickName,
            authorImageUrl = user.profileImage,
            content = comment,
            timestamp = Timestamp.now()
        )
        return postRepository.uploadComment(postId, postComment).mapCatching { it.toPostCommentListItem() }
    }
}