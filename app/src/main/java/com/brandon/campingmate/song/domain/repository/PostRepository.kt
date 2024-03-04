package com.brandon.campingmate.song.domain.repository

import com.brandon.campingmate.song.domain.model.PostEntity
import com.brandon.campingmate.song.domain.model.PostsEntity
import com.brandon.campingmate.song.utils.Resource
import com.google.firebase.firestore.DocumentSnapshot

interface PostRepository {
    suspend fun getPosts(pageSize: Int, lastVisibleDoc: DocumentSnapshot?) : Resource<PostsEntity>
    suspend fun uploadPost(post: PostEntity) : Resource<String>
}