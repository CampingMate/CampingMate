package com.brandon.campingmate.data.mapper

import com.brandon.campingmate.data.model.request.PostDTO
import com.brandon.campingmate.domain.model.Post
import com.google.firebase.Timestamp

fun Post.toPostDTO(): PostDTO {
    return PostDTO(
        postId = this.postId ?: "",
        authorName = this.authorName ?: "",
        authorId = this.authorId ?: "",
        authorProfileImageUrl = this.authorProfileImageUrl ?: "",
        title = this.title ?: "",
        content = this.content ?: "",
        imageUrls = this.imageUrls ?: listOf(),
        timestamp = this.timestamp ?: Timestamp.now()
    )
}

