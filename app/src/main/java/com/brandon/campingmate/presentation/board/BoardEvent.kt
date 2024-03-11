package com.brandon.campingmate.presentation.board

import com.brandon.campingmate.domain.model.PostEntity

sealed class BoardEvent {
    data class OpenContent(
        val postEntity: PostEntity,
    ) : BoardEvent()

    data class LoadPosts(
        val trigger: BoardViewModel.RefreshTrigger
    ) : BoardEvent()

    data class MakeToast(
        val message: String
    ) : BoardEvent()

    object NavigateToPostWrite : BoardEvent()

}