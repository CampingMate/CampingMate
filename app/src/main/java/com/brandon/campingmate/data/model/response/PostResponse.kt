package com.brandon.campingmate.data.model.response

import com.google.firebase.Timestamp

data class PostResponse(
    var postId: String? = null,
    val authorName: String? = null,
    val authorId: String? = null,
    val authorProfileImageUrl: String? = null,
    val title: String? = null,
    val content: String? = null,
    val imageUrls: List<String>? = null,
    val timestamp: Timestamp? = null
)
