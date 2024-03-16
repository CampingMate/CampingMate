package com.brandon.campingmate.domain.mapper

import com.brandon.campingmate.data.model.response.PostResponse
import com.brandon.campingmate.data.model.response.PostsResponse
import com.brandon.campingmate.domain.model.Post
import com.brandon.campingmate.domain.model.Posts


fun PostsResponse.toPostsEntity(): Posts {
    return Posts(
        posts = this.posts.map { it.toPostEntity() },
        lastVisibleDoc = this.lastVisibleDoc
    )
}

fun PostResponse.toPostEntity(): Post {
    return Post(
        postId = this.postId ?: "",
        authorName = this.authorName ?: "",
        authorId = this.authorId ?: "",
        authorProfileImageUrl = this.authorProfileImageUrl ?: "",
        title = this.title ?: "",
        content = this.content ?: "",
        imageUrls = this.imageUrls ?: listOf(),
        timestamp = this.timestamp
    )
}




