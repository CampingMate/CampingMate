package com.brandon.campingmate.domain.mapper

import com.brandon.campingmate.data.model.response.PostListResponse
import com.brandon.campingmate.data.model.response.PostResponse
import com.brandon.campingmate.domain.model.PostEntity
import com.brandon.campingmate.domain.model.PostsEntity


fun PostListResponse.toPostsEntity(): PostsEntity = PostsEntity(
    posts = this.posts.map { it.toPostEntity() },
    lastVisibleDoc = this.lastVisibleDoc
)

fun PostResponse.toPostEntity(): PostEntity {
    return PostEntity(
        postId = this.id ?: "",
        authorName = this.author ?: "",
        authorId = this.authorId ?: "",
        authorProfileImageUrl = this.authorProfileImageUrl ?: "",
        title = this.title ?: "",
        content = this.content ?: "",
        imageUrlList = this.imageUrlList ?: listOf(),
        timestamp = this.timestamp
    )
}




