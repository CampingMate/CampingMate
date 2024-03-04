package com.brandon.campingmate.song.presentation.board.adapter

import com.google.firebase.Timestamp

sealed class PostListItem {

    data class PostItem(
        val postId: String? = null, // PostModel 에서 변환 시 document 의 id(key) 를 넣어줘야 함
        val author: String? = null,
        val authorId: String? = null,
        val authorProfileImageUrl: String? = null,
        val title: String? = null,
        val content: String? = null,
        val imageUrlList: List<String>? = null,
        val timestamp: Timestamp? = null //
    ) : PostListItem()

    object Loading : PostListItem()

}