package com.brandon.campingmate.domain.model

import com.google.firebase.firestore.DocumentSnapshot

data class PostsEntity(
    val posts: List<PostEntity>,
    val lastVisibleDoc: DocumentSnapshot?
)

