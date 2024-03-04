package com.brandon.campingmate.song.data.repository

import com.brandon.campingmate.song.data.model.response.PostListResponse
import com.brandon.campingmate.song.data.source.remote.PostRemoteDataSource
import com.brandon.campingmate.song.domain.model.PostEntity
import com.brandon.campingmate.song.domain.model.PostsEntity
import com.brandon.campingmate.song.domain.mapper.toPostsEntity
import com.brandon.campingmate.song.domain.repository.PostRepository
import com.brandon.campingmate.song.utils.Resource
import com.google.firebase.firestore.DocumentSnapshot


class PostRepositoryImpl(
    private val postRemoteDataSource: PostRemoteDataSource
) : PostRepository {
    override suspend fun getPosts(pageSize: Int, lastVisibleDoc: DocumentSnapshot?): Resource<PostsEntity> {
        return runCatching {
            val result = postRemoteDataSource.getPosts(
                pageSize = pageSize,
                lastVisibleDoc = lastVisibleDoc
            )
            when (result) {
                is Resource.Success -> {
                    // PostsResult 형 변환 후 Resource 에 넣어 반환
                    PostListResponse(listOf(), null).toPostsEntity()
                    val postResult = result.data?.toPostsEntity() ?: PostsEntity(emptyList(), null)
                    Resource.Success(postResult)
                }

                is Resource.Error -> Resource.Error(
                    result.message ?: "Unknown error"
                )
            }
        }.getOrElse { exception ->
            Resource.Error("Unexpected error: ${exception.localizedMessage}")
        }
    }

    override suspend fun uploadPost(post: PostEntity): Resource<String> {
        TODO("Not yet implemented")
    }
}


