package com.brandon.campingmate.data.remote.dto

import com.google.firebase.Timestamp

data class PostCommentDTO(
    val id: String?,
    val userName: String?,
    val content: String?,
    val timestamp: Timestamp?,
)