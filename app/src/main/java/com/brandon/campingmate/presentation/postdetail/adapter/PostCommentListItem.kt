package com.brandon.campingmate.presentation.postdetail.adapter

sealed class PostCommentListItem {

    data class PostCommentItem(
        val commentId: String?,
        val postId: String?,
        val authorName: String?,
        val authorImageUrl: String?,
        val content: String?,
        val timestamp: String?
    ) : PostCommentListItem()

    object Loading : PostCommentListItem()

}
