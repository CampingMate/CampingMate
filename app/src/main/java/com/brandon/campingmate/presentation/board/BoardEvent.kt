package com.brandon.campingmate.presentation.board

import com.brandon.campingmate.domain.model.PostEntity

sealed class BoardEvent {
    object LoadMoreItems : BoardEvent()
    object ScrollEndReached : BoardEvent()
    object PostListEmpty : BoardEvent()
    object MoveToPostWrite : BoardEvent()
    data class OpenContent(
        val postEntity: PostEntity,
    ) : BoardEvent()

}