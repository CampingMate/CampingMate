package com.brandon.campingmate.utils.mappers

import com.brandon.campingmate.data.remote.dto.PostDTO
import com.brandon.campingmate.data.remote.dto.PostsDTO
import com.brandon.campingmate.domain.model.Post
import com.brandon.campingmate.domain.model.Posts
import com.brandon.campingmate.presentation.board.adapter.PostListItem
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

fun PostsDTO.toPostsEntity(): Posts {
    return Posts(
        posts = this.posts.map { it.toPostEntity() },
        lastVisibleDoc = this.lastVisibleDoc
    )
}

fun PostDTO.toPostEntity(): Post {
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

fun List<Post>.toPostListItem(): List<PostListItem> {
    return this.map { postEntity ->
        PostListItem.PostItem(
            postId = postEntity.postId,
            author = postEntity.authorName,
            authorId = postEntity.authorId,
            authorProfileImageUrl = postEntity.authorProfileImageUrl,
            title = postEntity.title,
            content = postEntity.content,
            imageUrlList = postEntity.imageUrls,
            timestamp = postEntity.timestamp
        )
    }
}

fun PostListItem.PostItem.toPostEntity(): Post {
    return Post(
        postId = this.postId,
        authorName = this.author,
        authorId = this.authorId,
        authorProfileImageUrl = this.authorProfileImageUrl,
        title = this.title,
        content = this.content,
        imageUrls = this.imageUrlList,
        timestamp = this.timestamp
    )
}