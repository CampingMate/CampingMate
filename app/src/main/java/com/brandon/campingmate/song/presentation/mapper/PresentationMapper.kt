package com.brandon.campingmate.song.presentation.mapper

import com.brandon.campingmate.song.domain.model.PostEntity
import com.brandon.campingmate.song.domain.model.PostsEntity
import com.brandon.campingmate.song.presentation.board.adapter.PostListItem

fun List<PostEntity>.toPostListItem(): List<PostListItem> {
    return this.map { postEntity ->
        PostListItem.PostItem(
            postId = postEntity.id,
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