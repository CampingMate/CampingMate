package com.brandon.campingmate.data.source.network

import android.net.Uri
import com.brandon.campingmate.data.model.request.PostCommentDTO
import com.brandon.campingmate.data.model.request.PostDTO
import com.brandon.campingmate.data.model.response.PostResponse
import com.brandon.campingmate.data.model.response.PostsResponse
import com.brandon.campingmate.utils.Resource
import com.google.firebase.firestore.DocumentSnapshot

interface PostRemoteDataSource {
    suspend fun getPosts(pageSize: Int, lastVisibleDoc: DocumentSnapshot?): Resource<PostsResponse>
    suspend fun uploadPost(postEntity: PostDTO): Result<String>
    suspend fun uploadPostComment(postId: String, postCommentDTO: PostCommentDTO): Result<String>
    suspend fun getPostById(postId: String): Resource<PostResponse>
    suspend fun uploadPostImage(imageUris: Uri): Result<String>
}