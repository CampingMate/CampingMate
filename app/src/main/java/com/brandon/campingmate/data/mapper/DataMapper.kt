package com.brandon.campingmate.data.mapper

import com.brandon.campingmate.data.model.request.PostDTO
import com.brandon.campingmate.domain.model.PostEntity
import com.google.firebase.Timestamp

fun PostEntity.toPostDTO(): PostDTO {
    return PostDTO(
        postId = this.postId ?: "",
        authorName = this.authorName ?: "",
        authorId = this.authorId ?: "",
        authorProfileImageUrl = this.authorProfileImageUrl ?: "",
        title = this.title ?: "",
        content = this.content ?: "",
        imageUrlList = this.imageUrls ?: listOf(),
        timestamp = this.timestamp ?: Timestamp.now()
    )
}

