package com.brandon.campingmate.utils.mappers

import com.brandon.campingmate.data.remote.dto.PostCommentDTO
import com.brandon.campingmate.domain.model.PostComment
import com.brandon.campingmate.presentation.postdetail.adapter.PostCommentListItem
import com.brandon.campingmate.utils.toFormattedString

fun PostComment.toCommentDTO(): PostCommentDTO {
    return PostCommentDTO(
        commentId = commentId,
        postId = postId,
        authorId = authorId,
        authorName = authorName,
        authorImageUrl = authorImageUrl,
        content = content,
        timestamp = timestamp
    )
}

fun PostCommentDTO.toPostComment(): PostComment {
    return PostComment(
        commentId = commentId,
        postId = postId,
        authorId = authorId,
        authorName = authorName,
        authorImageUrl = authorImageUrl,
        content = content,
        timestamp = timestamp
    )
}

fun PostComment.toPostCommentListItem(): PostCommentListItem.PostCommentItem {
    return PostCommentListItem.PostCommentItem(
        commentId = commentId,
        postId = postId,
        authorName = authorName,
        authorId = authorId,
        authorImageUrl = authorImageUrl,
        content = content,
        timestamp = timestamp.toFormattedString()
    )
}