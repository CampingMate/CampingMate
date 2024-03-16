package com.brandon.campingmate.data.remote

import android.net.Uri
import com.brandon.campingmate.data.remote.dto.PostCommentDTO
import com.brandon.campingmate.data.remote.dto.PostDTO
import com.brandon.campingmate.data.remote.dto.PostsDTO
import com.brandon.campingmate.utils.Resource
import com.google.firebase.firestore.DocumentSnapshot

interface PostRemoteDataSource {
    suspend fun getPosts(pageSize: Int, lastVisibleDoc: DocumentSnapshot?): Resource<PostsDTO>
    suspend fun uploadPost(postEntity: PostDTO): Result<String>
    suspend fun uploadPostComment(postId: String, postCommentDTO: PostCommentDTO): Result<String>
    suspend fun getPostById(postId: String): Resource<PostDTO>
    suspend fun uploadPostImage(imageUris: Uri): Result<String>
}