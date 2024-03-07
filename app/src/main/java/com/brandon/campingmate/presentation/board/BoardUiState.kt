package com.brandon.campingmate.presentation.board

import com.brandon.campingmate.presentation.board.adapter.PostListItem
import com.brandon.campingmate.utils.UiState
import com.google.firebase.firestore.DocumentSnapshot

data class BoardUiState(
    val posts: UiState<List<PostListItem>>,
    val lastVisibleDoc: DocumentSnapshot?,
    val isLoadingNext: Boolean,
    val isRefreshing: Boolean,
    val isNeedScroll: Boolean,
    // 다른 UiState 도 관리 가능
) {
    companion object {
        fun init() = BoardUiState(
            posts = UiState.Empty,
            lastVisibleDoc = null,
            isLoadingNext = false,
            isRefreshing = false,
            isNeedScroll = false
        )
    }
}
