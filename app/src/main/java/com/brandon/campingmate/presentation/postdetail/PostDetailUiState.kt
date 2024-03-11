package com.brandon.campingmate.presentation.postdetail

import com.brandon.campingmate.domain.model.PostEntity

data class PostDetailUiState(
    val post: PostEntity?,
    // 다른 UiState 도 관리 가능
) {
    companion object {
        fun init() = PostDetailUiState(
            post = null,
        )
    }
}
