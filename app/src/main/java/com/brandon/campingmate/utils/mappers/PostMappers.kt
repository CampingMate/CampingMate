package com.brandon.campingmate.utils.mappers

import com.brandon.campingmate.data.remote.dto.PostDTO
import com.brandon.campingmate.data.remote.dto.PostHitDTO
import com.brandon.campingmate.domain.model.Post
import com.brandon.campingmate.presentation.board.adapter.PostListItem
import com.google.firebase.Timestamp
import java.util.Date

fun Post.toPostDTO(): PostDTO {
    return PostDTO(
        postId = this.postId,
        authorName = this.authorName,
        authorId = this.authorId,
        authorProfileImageUrl = this.authorProfileImageUrl,
        title = this.title,
        content = this.content,
        imageUrls = this.imageUrls,
        timestamp = this.timestamp,
    )
}


fun PostDTO.toPostEntity(): Post {
    return Post(
        postId = this.postId,
        authorName = this.authorName,
        authorId = this.authorId,
        authorProfileImageUrl = this.authorProfileImageUrl,
        title = this.title,
        content = this.content,
        imageUrls = this.imageUrls,
        timestamp = this.timestamp
    )
}

fun List<Post>.toPostListItem(): List<PostListItem.PostItem> {
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

fun PostHitDTO.toPost(): Post {
    return Post(
        postId = this.postId,
        authorName = this.authorName,
        authorId = this.authorId,
        authorProfileImageUrl = this.authorProfileImageUrl,
        title = this.title,
        content = this.content,
        imageUrls = this.imageUrls,
        timestamp = this.timestamp.toTimeStamp()
    )
}

fun Long?.toTimeStamp(): Timestamp? {
    if (this == null) return null
    val date = Date(this)
    return Timestamp(date)
}
