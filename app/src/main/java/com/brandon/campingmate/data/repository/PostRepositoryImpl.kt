package com.brandon.campingmate.data.repository

import android.net.Uri
import com.brandon.campingmate.data.mapper.toPostDTO
import com.brandon.campingmate.data.source.network.PostRemoteDataSource
import com.brandon.campingmate.domain.mapper.toPostEntity
import com.brandon.campingmate.domain.mapper.toPostsEntity
import com.brandon.campingmate.domain.model.Post
import com.brandon.campingmate.domain.model.PostComment
import com.brandon.campingmate.domain.model.Posts
import com.brandon.campingmate.domain.repository.PostRepository
import com.brandon.campingmate.utils.Resource
import com.brandon.campingmate.utils.mappers.toCommentDTO
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
    ): Resource<Posts> {
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

    override suspend fun getPostById(postId: String): Resource<Post> {
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
        post: Post,
        imageUris: List<Uri>,
    ): Result<String> = coroutineScope {
        runCatching {
            val imageUrls = imageUris.map { uri ->
                async { postRemoteDataSource.uploadPostImage(uri).getOrThrow() }
            }.awaitAll()
            val newPost = post.copy(imageUrls = imageUrls).toPostDTO()
            postRemoteDataSource.uploadPost(newPost).getOrThrow()
        }
    }

    override suspend fun uploadComment(postId: String, postComment: PostComment): Result<String> {
        return postRemoteDataSource.uploadPostComment(postId, postComment.toCommentDTO())
    }
}


