package com.brandon.campingmate.presentation.board

import com.brandon.campingmate.presentation.board.adapter.PostListItem
import com.google.firebase.firestore.DocumentSnapshot

data class BoardUiState(
    val posts: List<PostListItem>,
    val lastVisibleDoc: DocumentSnapshot?,
    val isLoading: Boolean,
    val isScrolling: Boolean,
    val shouldScrollToTop: Boolean,
    val isInitLoading: Boolean,
) {
    companion object {
        fun init() = BoardUiState(
            posts = emptyList(),
            lastVisibleDoc = null,
            isLoading = false,
            isScrolling = false,
            shouldScrollToTop = true,
            isInitLoading = false,
        )
    }
}
