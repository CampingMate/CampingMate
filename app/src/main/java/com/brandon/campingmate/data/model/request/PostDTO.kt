package com.brandon.campingmate.data.model.request

import com.google.firebase.Timestamp

data class PostDTO(
    val postId: String? = null,
    val authorName: String? = null,
    val authorId: String? = null,
    val authorProfileImageUrl: String? = null,
    val title: String? = null,
    val content: String? = null,
    val imageUrlList: List<String>? = null,
    val timestamp: Timestamp? = null
)


