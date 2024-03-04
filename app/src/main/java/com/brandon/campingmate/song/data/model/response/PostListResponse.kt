package com.brandon.campingmate.song.data.model.response

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot

/**
 * firestore 환경에서는 불필요함
 */

data class PostListResponse(
    val posts: List<PostResponse>,
    var lastVisibleDoc: DocumentSnapshot?
)

data class PostResponse(
    var id: String? = null, // id 업데이트 필요(초기 업로드 시 id 없음)
    val author: String? = null,
    val authorId: String? = null,
    val authorProfileImageUrl: String? = null,
    val title: String? = null,
    val content: String? = null,
    val imageUrlList: List<String>? = null,
    val timestamp: Timestamp? = null //
)


