package com.brandon.campingmate.presentation.postdetail


sealed class PostDetailEvent {
    data class UploadComment(
        val comment: String,
    ) : PostDetailEvent()

    data class MakeToast(
        val message: String
    ) : PostDetailEvent()

    object UploadCommentSuccess : PostDetailEvent()

    object SwipeRefresh : PostDetailEvent()

    object InfiniteScroll : PostDetailEvent()

    object Delete

}