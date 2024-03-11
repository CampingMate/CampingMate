package com.brandon.campingmate.domain.mapper

import com.brandon.campingmate.data.model.response.PostResponse
import com.brandon.campingmate.data.model.response.PostsResponse
import com.brandon.campingmate.domain.model.PostEntity
import com.brandon.campingmate.domain.model.PostsEntity


fun PostsResponse.toPostsEntity(): PostsEntity {
    return PostsEntity(
        posts = this.posts.map { it.toPostEntity() },
        lastVisibleDoc = this.lastVisibleDoc
    )
}

fun PostResponse.toPostEntity(): PostEntity {
    return PostEntity(
        postId = this.postId ?: "",
        authorName = this.authorName ?: "",
        authorId = this.authorId ?: "",
        authorProfileImageUrl = this.authorProfileImageUrl ?: "",
        title = this.title ?: "",
        content = this.content ?: "",
        imageUrlList = this.imageUrlList ?: listOf(),
        timestamp = this.timestamp
    )
}




