package com.brandon.campingmate.data.model.response

import com.google.firebase.Timestamp

/**
 * firestore 환경에서는 불필요함
 */
data class PostCommentResponse(
    val id: String? = null,
    val author: String? = null,
    val comment: String? = null,
    val timestamp: Timestamp? = Timestamp.now() // Firestore의 Timestamp 사용
)