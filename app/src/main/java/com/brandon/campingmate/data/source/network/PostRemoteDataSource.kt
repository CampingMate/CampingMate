package com.brandon.campingmate.data.source.network

import com.brandon.campingmate.data.model.response.PostListResponse
import com.brandon.campingmate.domain.model.PostEntity
import com.brandon.campingmate.utils.Resource
import com.google.firebase.firestore.DocumentSnapshot

interface PostRemoteDataSource {
    suspend fun getPosts(pageSize: Int, lastVisibleDoc: DocumentSnapshot?): Resource<PostListResponse>
    suspend fun uploadPost(postEntity: PostEntity) : Resource<String>
}