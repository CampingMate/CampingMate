package com.brandon.campingmate.data.remote.dto

data class PostHitDTO(
    val postId: String?,
    val authorName: String?,
    val authorId: String?,
    val authorProfileImageUrl: String?,
    val title: String?,
    val content: String?,
    val imageUrls: List<String>?,
    val timestamp: Long?
)

