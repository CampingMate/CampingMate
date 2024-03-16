package com.brandon.campingmate.data.source.network.impl

import android.net.Uri
import com.brandon.campingmate.data.model.response.PostResponse
import com.brandon.campingmate.data.model.response.PostsResponse
import com.brandon.campingmate.data.source.network.PostRemoteDataSource
import com.brandon.campingmate.domain.model.PostEntity
import com.brandon.campingmate.utils.Resource
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

/**
 * 데이터소스 레이어에서는 raw 한 데이터를 반환한다
 */
class PostRemoteDataSourceImpl(
    private val firestore: FirebaseFirestore, private val storage: FirebaseStorage
) : PostRemoteDataSource {
    override suspend fun getPosts(pageSize: Int, lastVisibleDoc: DocumentSnapshot?): Resource<PostsResponse> {
        return try {
            withContext(IO) {
                val query = firestore.collection("posts").orderBy("timestamp", Query.Direction.DESCENDING)
                val paginatedQuery = lastVisibleDoc?.let { query.startAfter(it) } ?: query
                val snapshot = paginatedQuery.limit(pageSize.toLong()).get().await()
                val posts = snapshot.documents.mapNotNull { it.toObject(PostResponse::class.java) }
                if (posts.isNotEmpty()) {
                    Resource.Success(PostsResponse(posts, snapshot.documents.lastOrNull()))
                } else {
                    Resource.Empty
                }
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "UnknownError")
        }
    }

    override suspend fun uploadPost(postEntity: PostEntity): Result<String> = withContext(IO) {
        runCatching {
            val postsCollection = firestore.collection("posts")
            val newPostRef = postsCollection.document() // 새 문서 생성
            val newPostId = newPostRef.id
            postsCollection.document(newPostId).set(postEntity.copy(postId = newPostId)).await()
            newPostId
        }
    }


    override suspend fun getPostById(postId: String): Resource<PostResponse> {
        return withContext(IO) {
            try {
                val document = firestore.collection("posts").document(postId).get().await()
                val post = document.toObject(PostResponse::class.java)
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

    override suspend fun uploadPostImage(imageUri: Uri): Result<String> = withContext(IO) {
        runCatching {
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            val fileName = "IMG_${timeStamp}_${UUID.randomUUID()}.jpg"
            val imageRef = storage.reference.child("postImages/$fileName")
            val uploadTask = imageRef.putFile(imageUri).await() // 코루틴을 사용해 업로드를 기다림
            val imageUrl = uploadTask.metadata?.reference?.downloadUrl?.await()?.toString()
            imageUrl ?: throw Exception("Failed to get download URL")
        }
    }
}
