package com.brandon.campingmate.domain.model

import com.google.firebase.Timestamp

data class PostComment(
    val id: String?,
    val userName: String?,
    val content: String?,
    val timestamp: Timestamp?
)