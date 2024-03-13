package com.brandon.campingmate.presentation.campdetail

data class CampCommentEntity(
    val userId: String,
    val campId: String,
    val nickName: String,
    val title: String,
    val content: String,
    val date: String,
    val imageUrl: String,
)
