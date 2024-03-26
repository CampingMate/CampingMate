package com.brandon.campingmate.presentation.board

import com.brandon.campingmate.presentation.board.adapter.PostListItem

data class BoardUiState(
    val posts: List<PostListItem>,
    val isRefreshing: Boolean,
    val isLoadingMore: Boolean,
    val isInitialLoading: Boolean,
    val isSearchLoading: Boolean,
    val isNothingToShow: Boolean,
    val shouldScrollToTop: Boolean,
) {
    companion object {
        fun init() = BoardUiState(
            posts = emptyList(),
            isRefreshing = false,
            isLoadingMore = false,
            isInitialLoading = false,
            isSearchLoading = false,
            isNothingToShow = false,
            shouldScrollToTop = false,
        )
    }
}
