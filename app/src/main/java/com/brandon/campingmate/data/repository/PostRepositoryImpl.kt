package com.brandon.campingmate.data.repository

import android.net.Uri
import com.brandon.campingmate.data.source.network.PostRemoteDataSource
import com.brandon.campingmate.domain.mapper.toPostEntity
import com.brandon.campingmate.domain.mapper.toPostsEntity
import com.brandon.campingmate.domain.model.PostEntity
import com.brandon.campingmate.domain.model.PostsEntity
import com.brandon.campingmate.domain.repository.PostRepository
import com.brandon.campingmate.utils.Resource
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope


class PostRepositoryImpl(
    private val postRemoteDataSource: PostRemoteDataSource
) : PostRepository {

    override suspend fun getPosts(
        pageSize: Int,
        lastVisibleDoc: DocumentSnapshot?
    ): Resource<PostsEntity> {
        return try {
            when (val result = postRemoteDataSource.getPosts(pageSize, lastVisibleDoc)) {
                Resource.Empty -> Resource.Empty
                is Resource.Error -> Resource.Error(result.message)
                is Resource.Success -> Resource.Success(result.data.toPostsEntity())
            }
        } catch (e: Exception) {
            Resource.Error("Unknown Error")
        }
    }

    override suspend fun getPostById(postId: String): Resource<PostEntity> {
        return try {
            when (val result = postRemoteDataSource.getPostById(postId)) {
                Resource.Empty -> Resource.Empty
                is Resource.Error -> Resource.Error(result.message)
                is Resource.Success -> Resource.Success(result.data.toPostEntity())
            }
        } catch (e: Exception) {
            Resource.Error("Unknown Error")
        }
    }

    override suspend fun uploadPostWithImages(
        postEntity: PostEntity,
        imageUris: List<Uri>,
    ): Result<String> = coroutineScope {
        runCatching {
            val imageUrls = imageUris.map { uri ->
                async { postRemoteDataSource.uploadPostImage(uri).getOrElse { throw it } }
            }.awaitAll()
            val newPost = postEntity.copy(imageUrls = imageUrls)
            postRemoteDataSource.uploadPost(newPost).getOrElse { throw it }
        }
    }

}


