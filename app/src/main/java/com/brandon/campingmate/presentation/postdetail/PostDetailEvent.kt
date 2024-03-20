package com.brandon.campingmate.presentation.postdetail

import com.brandon.campingmate.presentation.postdetail.adapter.PostCommentListItem


sealed class PostDetailEvent {
    data class UploadComment(
        val comment: String,
    ) : PostDetailEvent()

    data class MakeToast(
        val message: String
    ) : PostDetailEvent()

    data class ShowBottomSheetMenuIfUserExists(
        val item: PostCommentListItem.PostCommentItem
    ) : PostDetailEvent()

    data class ShowBottomSheetMenu(
        val isOwner: Boolean,
        val postCommentId: String?,
    ) : PostDetailEvent()

    data class DeletePostComment(
        val commentId: String?
    ) : PostDetailEvent()

    object UploadCommentSuccess : PostDetailEvent()

    object SwipeRefresh : PostDetailEvent()

    object InfiniteScroll : PostDetailEvent()


}