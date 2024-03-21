package com.brandon.campingmate.data.remote.firestore

import com.brandon.campingmate.data.remote.dto.PostCommentDTO
import com.brandon.campingmate.data.remote.dto.PostDTO
import com.brandon.campingmate.data.remote.dto.UserDTO
import com.brandon.campingmate.domain.model.PostComment
import com.brandon.campingmate.utils.Resource

interface FirestoreDataSource {
    suspend fun getPosts(pageSize: Int, shouldFetchFromFirst: Boolean): Result<List<PostDTO>>
    suspend fun getPostById(postId: String): Resource<PostDTO>
    suspend fun getUserById(userId: String): Result<UserDTO?>
    suspend fun getComments(
        postId: String,
        pageSize: Int,
        shouldFetchFromFirst: Boolean
    ): Result<List<PostCommentDTO>>

    suspend fun uploadPost(postEntity: PostDTO): Result<String>
    suspend fun uploadPostComment(postId: String, postCommentDTO: PostCommentDTO): Result<PostComment>
    suspend fun deletePostCommentById(commentId: String, postId: String): Result<String>
}