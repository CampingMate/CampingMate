package com.brandon.campingmate.data.repository

import com.brandon.campingmate.data.mapper.toPostDTO
import com.brandon.campingmate.data.source.network.PostRemoteDataSource
import com.brandon.campingmate.domain.mapper.toPostEntity
import com.brandon.campingmate.domain.mapper.toPostsEntity
import com.brandon.campingmate.domain.model.PostEntity
import com.brandon.campingmate.domain.model.PostsEntity
import com.brandon.campingmate.domain.repository.PostRepository
import com.brandon.campingmate.utils.Resource
import com.google.firebase.firestore.DocumentSnapshot


class PostRepositoryImpl(
    private val postRemoteDataSource: PostRemoteDataSource
) : PostRepository {
    override suspend fun getPosts(pageSize: Int, lastVisibleDoc: DocumentSnapshot?): Resource<PostsEntity> {
        return runCatching {
            val result = postRemoteDataSource.getPosts(
                pageSize = pageSize, lastVisibleDoc = lastVisibleDoc
            )
            when (result) {
                is Resource.Success -> {
                    // PostsResult 형 변환 후 Resource 에 넣어 반환
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

    override suspend fun uploadPost(
        postEntity: PostEntity, onSuccess: (String) -> Unit, onFailure: (Exception) -> Unit
    ) {
        postRemoteDataSource.uploadPost(
            postDto = postEntity.toPostDTO(),
            onSuccess = onSuccess,
            onFailure = onFailure,
        )
    }

    override suspend fun getPostById(postId: String): Resource<PostEntity> {
        return runCatching {
            val result = postRemoteDataSource.getPostById(postId)
            when (result) {
                is Resource.Success -> {
                    val postEntity = result.data?.toPostEntity() ?: PostEntity()
                    Resource.Success(postEntity)
                }

                is Resource.Error -> Resource.Error(
                    result.message ?: "Unknown error"
                )
            }
        }.getOrElse { exception ->
            Resource.Error("Unexpected error: ${exception.localizedMessage}")
        }
    }
}


