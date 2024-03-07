package com.brandon.campingmate.data.source.network.impl

import com.brandon.campingmate.data.model.request.PostDTO
import com.brandon.campingmate.data.model.response.PostListResponse
import com.brandon.campingmate.data.model.response.PostResponse
import com.brandon.campingmate.data.source.network.PostRemoteDataSource
import com.brandon.campingmate.utils.Resource
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import timber.log.Timber

class PostRemoteDataSourceImpl(private val firestore: FirebaseFirestore) : PostRemoteDataSource {
    override suspend fun getPosts(
        pageSize: Int, lastVisibleDoc: DocumentSnapshot?
    ): Resource<PostListResponse> = withContext(IO) {
        runCatching {
            val query = if (lastVisibleDoc == null) {
                Timber.d("시작 요청")
                firestore.collection("posts").orderBy("timestamp", Query.Direction.DESCENDING)
                    .limit(pageSize.toLong())
            } else {
                Timber.d("더보기 요청")
                // 이전 페이지 로딩이 있었던 경우
                firestore.collection("posts").orderBy("timestamp", Query.Direction.DESCENDING)
                    .startAfter(lastVisibleDoc)
                    .limit(pageSize.toLong())
            }

            // 데이터 호출
            val snapshot = query.get().await()

            // 데이터 response 객체로 변환
            val posts = snapshot.documents.mapNotNull {
                it.toObject(PostResponse::class.java).apply { this?.id = it.id }
            }
            /**
             * 문서 끝에 도달한 경우 lastVisibleDoc 를 업데이트 하지 않는다
             */
            Timber.d("현재 요청된 마지막 문서: ${lastVisibleDoc?.id}")
            Timber.d("받아온 posts: ${posts.map { it.title }}")
            val newLastVisibleDoc =
                if (snapshot.documents.isNotEmpty()) snapshot.documents.lastOrNull() else lastVisibleDoc
            Resource.Success(
                PostListResponse(
                    posts = posts, lastVisibleDoc = newLastVisibleDoc
                )
            )
        }.getOrElse { exception ->
            Resource.Error(exception.message ?: "Unknown error")
        }
    }

    override suspend fun uploadPost(
        postDto: PostDTO, onSuccess: (String) -> Unit, onFailure: (Exception) -> Unit
    ) {
        withContext(IO) {
            val postsCollection = firestore.collection("posts")
            val newPostRef = postsCollection.document() // Firestore 에서 자동으로 ID 할당

            val postId = newPostRef.id

            // 생성된 ID 를 포함하여 PostDTO 복사
            val newPost = postDto.copy(postId = postId)

            postsCollection.document(postId).set(newPost)
                .addOnSuccessListener { onSuccess(postId) }
                .addOnFailureListener { exception -> onFailure(exception) }
        }
    }
}