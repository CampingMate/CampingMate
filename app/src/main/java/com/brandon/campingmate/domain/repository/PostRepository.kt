package com.brandon.campingmate.domain.repository

import android.net.Uri
import com.brandon.campingmate.domain.model.Post
import com.brandon.campingmate.domain.model.PostComment
import com.brandon.campingmate.domain.model.Posts
import com.brandon.campingmate.utils.Resource
import com.google.firebase.firestore.DocumentSnapshot

interface PostRepository {

    suspend fun getPosts(
        pageSize: Int,
        lastVisibleDoc: DocumentSnapshot?
    ): Resource<Posts>

    suspend fun getPostById(
        postId: String
    ): Resource<Post>


    suspend fun uploadPostWithImages(
        post: Post,
        imageUris: List<Uri>,
    ): Result<String>

    suspend fun uploadComment(
        postId: String,
        postComment: PostComment
    ): Result<String>
}