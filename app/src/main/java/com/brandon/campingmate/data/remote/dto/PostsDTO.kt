package com.brandon.campingmate.data.remote.dto

import com.google.firebase.firestore.DocumentSnapshot

/**
 * firestore 환경에서는 불필요함
 */

data class PostsDTO(
    val posts: List<PostDTO> = emptyList(),
    var lastVisibleDoc: DocumentSnapshot? = null
)


