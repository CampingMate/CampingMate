package com.brandon.campingmate.data.repository

import android.net.Uri
import com.brandon.campingmate.data.remote.firebasestorage.FireBaseStorageDataSource
import com.brandon.campingmate.data.remote.firestore.FirestoreDataSource
import com.brandon.campingmate.domain.model.Post
import com.brandon.campingmate.domain.model.PostComment
import com.brandon.campingmate.domain.model.Posts
import com.brandon.campingmate.domain.repository.PostRepository
import com.brandon.campingmate.utils.Resource
import com.brandon.campingmate.utils.mappers.toCommentDTO
import com.brandon.campingmate.utils.mappers.toPostComment
import com.brandon.campingmate.utils.mappers.toPostDTO
import com.brandon.campingmate.utils.mappers.toPostEntity
import com.brandon.campingmate.utils.mappers.toPostsEntity
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope


class PostRepositoryImpl(
    private val firestoreDataSource: FirestoreDataSource,
    private val fireBaseStorageDataSource: FireBaseStorageDataSource
) : PostRepository {

    override suspend fun getPosts(
        pageSize: Int,
        lastVisibleDoc: DocumentSnapshot?
    ): Resource<Posts> {
        return try {
            when (val result = firestoreDataSource.getPosts(pageSize, lastVisibleDoc)) {
                Resource.Empty -> Resource.Empty
                is Resource.Error -> Resource.Error(result.message)
                is Resource.Success -> Resource.Success(result.data.toPostsEntity())
            }
        } catch (e: Exception) {
            Resource.Error("Unknown Error")
        }
    }

    override suspend fun getComments(
        postId: String,
        pageSize: Int,
        shouldFetchFromFirst: Boolean,
    ): Result<List<PostComment>> {
        return firestoreDataSource.getComments(postId, pageSize, shouldFetchFromFirst)
            .mapCatching { dtoList ->
                dtoList.map { dto -> dto.toPostComment() }
            }
    }

    override suspend fun getPostById(postId: String): Resource<Post> {
        return try {
            when (val result = firestoreDataSource.getPostById(postId)) {
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
                async { fireBaseStorageDataSource.uploadPostImage(uri).getOrThrow() }
            }.awaitAll()
            val newPost = post.copy(imageUrls = imageUrls).toPostDTO()
            firestoreDataSource.uploadPost(newPost).getOrThrow()
        }
    }

    override suspend fun uploadComment(postId: String, postComment: PostComment): Result<PostComment> {
        return firestoreDataSource.uploadPostComment(postId, postComment.toCommentDTO())
    }

    override suspend fun deletePostCommentById(commentId: String, postId: String): Result<String> {
        return firestoreDataSource.deletePostCommentById(commentId, postId)
    }
}


