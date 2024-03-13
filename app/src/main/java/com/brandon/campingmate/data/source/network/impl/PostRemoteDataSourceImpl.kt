package com.brandon.campingmate.data.source.network.impl

import android.net.Uri
import com.brandon.campingmate.data.model.request.PostDTO
import com.brandon.campingmate.data.model.response.PostResponse
import com.brandon.campingmate.data.model.response.PostsResponse
import com.brandon.campingmate.data.source.network.PostRemoteDataSource
import com.brandon.campingmate.utils.Resource
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
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
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage
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

    override suspend fun uploadPost(
        postDto: PostDTO, onSuccess: (String) -> Unit, onFailure: (Exception) -> Unit
    ) {
        try {
            val postsCollection = firestore.collection("posts")
            val newPostRef = postsCollection.document()
            val postId = newPostRef.id
            val newPost = postDto.copy(postId = postId)
            postsCollection.document(postId).set(newPost).await()
            onSuccess(postId)
        } catch (e: Exception) {
            onFailure(e)
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

    override suspend fun uploadPostImages(
        imageUris: List<Uri>,
        onSuccess: (List<String>) -> Unit,
        onFailure: (Exception) -> Unit
    ) = withContext(IO) {
        try {
            // 이미지 URI 리스트를 돌며 각각에 대한 업로드 작업을 비동기적으로 수행하고, 결과를 Deferred 객체로 받음
            val uploadTasks = imageUris.map { uri ->
                async {
                    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
                    val fileName = "IMG_${timeStamp}_${UUID.randomUUID()}.jpg"
                    val imagesRef = storage.reference.child("postImages/$fileName")
                    val uploadTaskSnapshot = imagesRef.putFile(uri).await() // 코루틴 사용하여 업로드 대기
                    uploadTaskSnapshot.metadata?.reference?.downloadUrl?.await()
                        .toString() // 다운로드 URL을 문자열로 반환
                }
            }

            // 모든 업로드 작업이 완료될 때까지 대기
            val uploadedImageUrls = uploadTasks.awaitAll()
            // 모든 업로드가 성공적으로 완료되면 성공 콜백 호출
            onSuccess(uploadedImageUrls)
        } catch (e: Exception) {
            // 하나라도 실패한 경우 실패 콜백 호출
            onFailure(e)
        }
    }
}