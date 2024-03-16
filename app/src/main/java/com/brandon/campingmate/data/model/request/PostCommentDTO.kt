package com.brandon.campingmate.data.model.request

import com.google.firebase.Timestamp

data class PostCommentDTO(
    val id: String?,
    val userName: String?,
    val content: String?,
    val timestamp: Timestamp?,
)