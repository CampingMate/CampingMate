package com.brandon.campingmate.data.remote.dto

import com.google.firebase.Timestamp

data class PostDTO(
    val postId: String? = null,
    val authorName: String? = null,
    val authorId: String? = null,
    val authorProfileImageUrl: String? = null,
    val title: String? = null,
    val content: String? = null,
    val imageUrls: List<String>? = null,
    val timestamp: Timestamp? = null
)


