package com.brandon.campingmate.data.source.network.impl

import com.brandon.campingmate.data.model.response.PostListResponse
import com.brandon.campingmate.data.model.response.PostResponse
import com.brandon.campingmate.data.source.network.PostRemoteDataSource
import com.brandon.campingmate.domain.model.PostEntity
import com.brandon.campingmate.utils.Resource
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class PostRemoteDataSourceImpl(private val db: FirebaseFirestore) : PostRemoteDataSource {
    override suspend fun getPosts(
        pageSize: Int,
        lastVisibleDoc: DocumentSnapshot?
    ): Resource<PostListResponse> = withContext(IO) {
        runCatching {
            val query = if (lastVisibleDoc == null) {
                db.collection("posts").orderBy("timestamp").limit(pageSize.toLong())
            } else {
                // 이전 페이지 로딩이 있었던 경우
                db.collection("posts").orderBy("timestamp").startAfter(lastVisibleDoc)
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
            val newLastVisibleDoc =
                if (snapshot.documents.isNotEmpty()) snapshot.documents.lastOrNull() else lastVisibleDoc

            Resource.Success(
                PostListResponse(
                    posts = posts,
                    lastVisibleDoc = newLastVisibleDoc
                )
            )
        }.getOrElse { exception ->
            Resource.Error(exception.message ?: "Unknown error")
        }
    }

    override suspend fun uploadPost(postEntity: PostEntity): Resource<String> {
        TODO("Not yet implemented")
    }
    //    override suspend fun getPosts(startAfter: DocumentSnapshot? = null): PostListResponse {
//        val query = db.collection("posts")
//            .limit(10)
//            .let { if(startAfter != null) it.startAfter(startAfter) else it }
//
//        val snapshot = query.get().asDeferred().await()
//        val PostResponse
//        PostListResponse(
//            posts =
//        )
//    }
//
//    override suspend fun uploadPost(postEntity: PostEntity): String {
//        TODO("Not yet implemented")
//    }
    //    override suspend fun getPosts(): Resource<List<PostResponse>> = withContext(Dispatchers.IO) {
//        runCatching {
//            val task = db.collection("posts").get()
//            val snapshot = task.asDeferred().await()
////            val posts = snapshot.toObjects(PostResponse::class.java)
//            val posts = snapshot.documents.mapNotNull { it.toObject(PostResponse::class.java) }
//
//        }.getOrElse { e ->
//        }
//    }
//
//    override suspend fun uploadPost(post: PostEntity): Resource<String> = withContext(Dispatchers.IO) {
//        runCatching {
//            val documentRef = db.collection("posts").add(post).asDeferred().await()
//            Result.success(documentRef.id)
//        }.getOrElse { e ->
//            Result.failure(e)
//        }
//    }
}