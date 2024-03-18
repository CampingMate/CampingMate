package com.brandon.campingmate.domain.model

import com.google.firebase.Timestamp

data class PostComment(
    val commentId: String?,
    val postId: String?,
    val authorName: String?,
    val content: String?,
    val timestamp: Timestamp?
)
