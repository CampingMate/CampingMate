package com.brandon.campingmate.song.data.source.remote

import com.brandon.campingmate.song.data.model.response.PostListResponse
import com.brandon.campingmate.song.domain.model.PostEntity
import com.brandon.campingmate.song.utils.Resource
import com.google.firebase.firestore.DocumentSnapshot

interface PostRemoteDataSource {
    suspend fun getPosts(pageSize: Int, lastVisibleDoc: DocumentSnapshot?): Resource<PostListResponse>
    suspend fun uploadPost(postEntity: PostEntity) : Resource<String>
}