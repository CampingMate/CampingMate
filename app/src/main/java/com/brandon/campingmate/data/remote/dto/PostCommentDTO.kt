package com.brandon.campingmate.data.remote.dto

import com.google.firebase.Timestamp

data class PostCommentDTO(
    val commentId: String? = null,
    val postId: String? = null,
    val authorId: String? = null,
    val authorName: String? = null,
    val authorImageUrl: String? = null,
    val content: String? = null,
    val timestamp: Timestamp? = null,
)