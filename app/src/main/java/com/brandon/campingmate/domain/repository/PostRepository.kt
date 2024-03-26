package com.brandon.campingmate.domain.repository

import android.net.Uri
import com.brandon.campingmate.domain.model.Post
import com.brandon.campingmate.domain.model.PostComment
import com.brandon.campingmate.utils.Resource

interface PostRepository {

    suspend fun getPosts(
        pageSize: Int,
        shouldFetchFromFirst: Boolean,
    ): Result<List<Post>>

    suspend fun getComments(
        postId: String,
        pageSize: Int,
        shouldFetchFromFirst: Boolean,
    ): Result<List<PostComment>>

    suspend fun getPostById(
        postId: String
    ): Resource<Post>


    suspend fun uploadPostWithImages(
        post: Post,
        imageUris: List<Uri>,
    ): Result<String>

    suspend fun uploadComment(
        postId: String, postComment: PostComment
    ): Result<PostComment>

    suspend fun deletePostCommentById(commentId: String, postId: String): Result<String>

    suspend fun deletePostById(postId: String): Result<String>
}