package com.brandon.campingmate.song.domain.mapper

import com.brandon.campingmate.song.data.model.response.PostListResponse
import com.brandon.campingmate.song.data.model.response.PostResponse
import com.brandon.campingmate.song.domain.model.PostEntity
import com.brandon.campingmate.song.domain.model.PostsEntity


fun PostListResponse.toPostsEntity(): PostsEntity = PostsEntity(
    posts = this.posts.map { it.toPostEntity() },
    lastVisibleDoc = this.lastVisibleDoc
)

fun PostResponse.toPostEntity(): PostEntity {
    return PostEntity(
        postId = this.id ?: "",
        author = this.author ?: "",
        authorId = this.authorId ?: "",
        authorProfileImageUrl = this.authorProfileImageUrl ?: "",
        title = this.title ?: "",
        content = this.content ?: "",
        imageUrlList = this.imageUrlList ?: listOf(),
        timestamp = this.timestamp
    )
}




