package com.brandon.campingmate.presentation.postdetail

import com.brandon.campingmate.domain.model.PostEntity
import com.brandon.campingmate.utils.UiState

data class PostDetailUiState(
    val posts: UiState<PostEntity>,
    // 다른 UiState 도 관리 가능
) {
    companion object {
        fun init() = PostDetailUiState(
            posts = UiState.Empty,
        )
    }
}
