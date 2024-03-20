package com.brandon.campingmate.presentation.postdetail.adapter

data class PostCommentListItem(
    val commentId: String?,
    val postId: String?,
    val authorName: String?,
    val authorImageUrl: String?,
    val content: String?,
    val timestamp: String?
)
