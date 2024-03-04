package com.brandon.campingmate.song.presentation.board

import com.brandon.campingmate.song.presentation.board.adapter.PostListItem
import com.brandon.campingmate.song.utils.UiState
import com.google.firebase.firestore.DocumentSnapshot

data class BoardUiState(
    val posts: UiState<List<PostListItem>>,
    val lastVisibleDoc: DocumentSnapshot?,
    // 다른 UiState 도 관리 가능
) {
    companion object {
        fun init() = BoardUiState(
            posts = UiState.Loading,
            lastVisibleDoc = null
        )
    }
}
