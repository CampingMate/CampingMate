package com.brandon.campingmate.song.domain.model

import com.google.firebase.Timestamp

data class PostEntity(
    val id: String? = null,
    val author: String? = null,
    val authorId: String? = null,
    val authorProfileImageUrl: String? = null,
    val title: String? = null,
    val content: String? = null,
    val imageUrlList: List<String>? = null,
    val timestamp: Timestamp? = null
)