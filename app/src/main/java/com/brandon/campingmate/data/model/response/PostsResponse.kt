package com.brandon.campingmate.data.model.response

import com.google.firebase.firestore.DocumentSnapshot

/**
 * firestore 환경에서는 불필요함
 */

data class PostsResponse(
    val posts: List<PostResponse> = emptyList(),
    var lastVisibleDoc: DocumentSnapshot? = null
)


