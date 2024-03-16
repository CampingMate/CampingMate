package com.brandon.campingmate.domain.model

import com.google.firebase.firestore.DocumentSnapshot

data class Posts(
    val posts: List<Post>,
    val lastVisibleDoc: DocumentSnapshot?
)

