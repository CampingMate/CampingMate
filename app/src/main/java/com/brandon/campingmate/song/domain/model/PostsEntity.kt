package com.brandon.campingmate.song.domain.model

import com.google.firebase.firestore.DocumentSnapshot

data class PostsEntity(
    val posts: List<PostEntity>,
    val lastVisibleDoc: DocumentSnapshot?
)

