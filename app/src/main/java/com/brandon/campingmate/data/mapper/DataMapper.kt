package com.brandon.campingmate.data.mapper

import com.brandon.campingmate.data.model.request.PostRequest
import com.brandon.campingmate.domain.model.PostEntity
import com.google.firebase.Timestamp

fun PostEntity.toPostRequest(): PostRequest {
    return PostRequest(
        id = this.postId ?: "",
        author = this.author ?: "",
        authorId = this.authorId ?: "",
        authorProfileImageUrl = this.authorProfileImageUrl ?: "",
        title = this.title ?: "",
        content = this.content ?: "",
        imageUrlList = this.imageUrlList ?: listOf(),
        timestamp = this.timestamp ?: Timestamp.now()
    )
}