package com.brandon.campingmate.presentation.postdetail


sealed class PostDetailEvent {
    data class UploadComment(
        val comment: String,
    ) : PostDetailEvent()

    object UploadCommentSuccess : PostDetailEvent()

}