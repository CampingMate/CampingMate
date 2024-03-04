package com.brandon.campingmate.song.presentation.board

import com.brandon.campingmate.song.domain.model.PostEntity

sealed class BoardEvent {
    object LoadMoreItems : BoardEvent()
    object ScrollEndEvent : BoardEvent()
    object PostListEmpty : BoardEvent()

    data class OpenContent(
        val postEntity: PostEntity,
    ) : BoardEvent()

}