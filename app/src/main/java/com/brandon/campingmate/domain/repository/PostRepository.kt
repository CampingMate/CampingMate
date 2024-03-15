package com.brandon.campingmate.domain.repository

import android.net.Uri
import com.brandon.campingmate.domain.model.PostEntity
import com.brandon.campingmate.domain.model.PostsEntity
import com.brandon.campingmate.utils.Resource
import com.google.firebase.firestore.DocumentSnapshot

interface PostRepository {

    suspend fun getPosts(
        pageSize: Int,
        lastVisibleDoc: DocumentSnapshot?
    ): Resource<PostsEntity>

    suspend fun getPostById(
        postId: String
    ): Resource<PostEntity>


    suspend fun uploadPostWithImages(
        postEntity: PostEntity,
        imageUris: List<Uri>,
    ): Result<String>
}