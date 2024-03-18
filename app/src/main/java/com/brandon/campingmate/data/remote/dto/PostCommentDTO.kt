package com.brandon.campingmate.data.remote.dto

import com.google.firebase.Timestamp

data class PostCommentDTO(
    val id: String? = null,
    val userName: String? = null,
    val content: String? = null,
    val timestamp: Timestamp? = null,
)