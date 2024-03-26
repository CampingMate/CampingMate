package com.brandon.campingmate.presentation.board

import com.brandon.campingmate.domain.model.Post

sealed class BoardEvent {
    data class OpenContent(
        val post: Post,
    ) : BoardEvent()

    data class MakeToast(
        val message: String
    ) : BoardEvent()

    object NavigateToPostWrite : BoardEvent()

    object RefreshPostsRequested : BoardEvent()

    object LoadMorePostsRequested : BoardEvent()

    object RefreshPostsAndScrollToTopRequested : BoardEvent()

}