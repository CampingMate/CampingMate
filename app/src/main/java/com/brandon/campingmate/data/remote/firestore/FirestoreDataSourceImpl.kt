package com.brandon.campingmate.data.remote.firestore

import com.brandon.campingmate.data.remote.dto.PostCommentDTO
import com.brandon.campingmate.data.remote.dto.PostDTO
import com.brandon.campingmate.data.remote.dto.UserDTO
import com.brandon.campingmate.domain.model.PostComment
import com.brandon.campingmate.utils.Resource
import com.brandon.campingmate.utils.UserCryptoUtils.toDecryptedUser
import com.brandon.campingmate.utils.mappers.toPostComment
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import timber.log.Timber

class FirestoreDataSourceImpl(
    private val firestore: FirebaseFirestore
) : FirestoreDataSource {

    private var lastVisiblePostDoc: DocumentSnapshot? = null
    private var lastVisibleCommentDoc: DocumentSnapshot? = null

    override suspend fun getPosts(pageSize: Int, shouldFetchFromFirst: Boolean): Result<List<PostDTO>> {
        return withContext(IO) {
            runCatching {
                val query = firestore.collection("posts").orderBy("timestamp", Query.Direction.DESCENDING)
                if (shouldFetchFromFirst) lastVisiblePostDoc = null
                val paginatedQuery = lastVisiblePostDoc?.let { query.startAfter(it) } ?: query
                val snapshot = paginatedQuery.limit(pageSize.toLong()).get().await()
                lastVisiblePostDoc = snapshot.documents.lastOrNull() ?: lastVisiblePostDoc
                val posts = snapshot.documents.mapNotNull { it.toObject(PostDTO::class.java) }
                posts
            }
        }
    }

    override suspend fun getComments(
        postId: String,
        pageSize: Int,
        shouldFetchFromFirst: Boolean,
    ): Result<List<PostCommentDTO>> {
        return withContext(IO) {
            runCatching {
                val query = firestore.collection("posts").document(postId).collection("comments")
                    .orderBy("timestamp", Query.Direction.ASCENDING)
                if (shouldFetchFromFirst) lastVisibleCommentDoc = null
                val paginatedQuery = lastVisibleCommentDoc?.let { query.startAfter(it) } ?: query
                val snapshot = paginatedQuery.limit(pageSize.toLong()).get().await()
                lastVisibleCommentDoc = snapshot.documents.lastOrNull() ?: lastVisibleCommentDoc
                val comments = snapshot.documents.mapNotNull { it.toObject(PostCommentDTO::class.java) }
                comments
            }
        }
    }

    override suspend fun uploadPostComment(
        postId: String, postCommentDto: PostCommentDTO
    ): Result<PostComment> = withContext(IO) {
        runCatching {
            val commentsCollection = firestore.collection("posts").document(postId).collection("comments")
            val newCommentRef = commentsCollection.document()
            val newCommentId = newCommentRef.id
            val newPost = postCommentDto.copy(commentId = newCommentId)
            Timber.tag("USER").d("datasource url: ${newPost.authorImageUrl}")
            commentsCollection.document(newCommentId).set(newPost).await()
            newPost.toPostComment()
        }
    }

    override suspend fun uploadPost(postDto: PostDTO): Result<String> = withContext(IO) {
        runCatching {
            val postsCollection = firestore.collection("posts")
            val newPostRef = postsCollection.document() // 새 문서 생성
            val newPostId = newPostRef.id
            Timber.tag("USER").d("postDto: $postDto")
            postsCollection.document(newPostId).set(postDto.copy(postId = newPostId)).await()
            newPostId
        }
    }

    override suspend fun getPostById(postId: String): Resource<PostDTO> {
        return withContext(IO) {
            try {
                val document = firestore.collection("posts").document(postId).get().await()
                val post = document.toObject(PostDTO::class.java)
                if (post != null) {
                    Resource.Success(post)
                } else {
                    Resource.Empty
                }
            } catch (e: Exception) {
                Resource.Error(e.message ?: "UnkownError")
            }
        }
    }

    override suspend fun getUserById(userId: String): Result<UserDTO?> {
        return withContext(IO) {
            runCatching {
                val document = firestore.collection("users").document(userId).get().await()
                val encryptedUser = document.toObject(UserDTO::class.java)
                    ?: throw NoSuchElementException("Can't find the User using local myID: $userId")
                encryptedUser.toDecryptedUser()
            }
        }
    }

    override suspend fun deletePostCommentById(commentId: String, postId: String): Result<String> {
        return withContext(IO) {
            runCatching {
                firestore.collection("posts").document(postId)
                    .collection("comments").document(commentId).delete()
                commentId
            }
        }
    }

    override suspend fun deletePostById(postId: String): Result<String> {
        return withContext(IO) {
            runCatching {
                firestore.collection("posts").document(postId).delete()
                postId
            }
        }
    }
}
