package com.brandon.campingmate.presentation.mapper

import com.brandon.campingmate.domain.model.PostEntity
import com.brandon.campingmate.presentation.board.adapter.PostListItem

fun List<PostEntity>.toPostListItem(): List<PostListItem> {
    return this.map { postEntity ->
        PostListItem.PostItem(
            postId = postEntity.postId,
            author = postEntity.author,
            authorId = postEntity.authorId,
            authorProfileImageUrl = postEntity.authorProfileImageUrl,
            title = postEntity.title,
            content = postEntity.content,
            imageUrlList = postEntity.imageUrlList,
            timestamp = postEntity.timestamp
        )
    }
}

fun PostListItem.PostItem.toPostEntity(): PostEntity {
    return PostEntity(
        postId = this.postId,
        author = this.author,
        authorId = this.authorId,
        authorProfileImageUrl = this.authorProfileImageUrl,
        title = this.title,
        content = this.content,
        imageUrlList = this.imageUrlList,
        timestamp = this.timestamp
    )
}