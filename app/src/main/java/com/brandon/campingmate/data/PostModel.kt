package com.brandon.campingmate.data

import com.google.firebase.Timestamp

data class PostModel(
    val author: String? = null,
    val authorId: String? = null,
    val authorProfileImageUrl: String? = null,
    val title: String? = null,
    val content: String? = null,
    val imageUrlList: List<String>? = null,
    val timestamp: Timestamp? = Timestamp.now() // Firestore의 Timestamp 사용
)

