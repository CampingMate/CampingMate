package com.brandon.campingmate.presentation.postdetail

import com.brandon.campingmate.domain.model.Post
import com.brandon.campingmate.presentation.postdetail.adapter.PostCommentListItem

data class PostDetailUiState(
    val post: Post?,
    val comments: List<PostCommentListItem>,
    val isSwipeLoadingComments: Boolean,
    val isInfiniteLoadingComments: Boolean
) {
    companion object {
        fun init() = PostDetailUiState(
            post = null,
            comments = emptyList(),
            isSwipeLoadingComments = false,
            isInfiniteLoadingComments = false
        )
    }
}
