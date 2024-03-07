package com.brandon.campingmate.data.source.network

import com.brandon.campingmate.data.model.request.PostDTO
import com.brandon.campingmate.data.model.response.PostListResponse
import com.brandon.campingmate.data.model.response.PostResponse
import com.brandon.campingmate.utils.Resource
import com.google.firebase.firestore.DocumentSnapshot

interface PostRemoteDataSource {
    suspend fun getPosts(pageSize: Int, lastVisibleDoc: DocumentSnapshot?): Resource<PostListResponse>
    suspend fun uploadPost(postDto: PostDTO, onSuccess: (String) -> Unit, onFailure: (Exception) -> Unit)
    suspend fun getPostById(postId: String): Resource<PostResponse>
}