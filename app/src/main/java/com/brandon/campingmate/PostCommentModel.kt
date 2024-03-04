package com.brandon.campingmate

import com.google.firebase.Timestamp

data class PostCommentModel(
    val author: String? = null,
    val comment: String? = null,
    val timestamp: Timestamp? = Timestamp.now() // Firestore의 Timestamp 사용
)