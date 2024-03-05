package com.brandon.campingmate.presentation.board.adapter

import com.google.firebase.Timestamp

sealed class PostListItem {

    data class PostItem(
        val postId: String? = null,
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