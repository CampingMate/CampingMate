package com.brandon.campingmate.utils.mappers

import com.brandon.campingmate.data.remote.dto.PostCommentDTO
import com.brandon.campingmate.domain.model.PostComment

fun PostComment.toCommentDTO(): PostCommentDTO {
    return PostCommentDTO(
        id = id,
        userName = userName,
        content = content,
        timestamp = timestamp
    )
}

fun PostCommentDTO.toPostComment(): PostComment {
    return PostComment(
        id = id,
        userName = userName,
        content = content,
        timestamp = timestamp
    )
}