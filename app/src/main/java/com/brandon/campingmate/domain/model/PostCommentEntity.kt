package com.brandon.campingmate.domain.model

import com.google.firebase.Timestamp

data class PostCommentEntity(
    val id: String? = null,
    val author: String? = null,
    val comment: String? = null,
    val timestamp: Timestamp? = Timestamp.now() // Firestore의 Timestamp 사용
)