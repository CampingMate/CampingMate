package com.brandon.campingmate.domain.model

import android.net.Uri

data class CampCommentEntity(
    val userId: String,
    val userName: Any?,
    val content: String,
    val date: String,
    val imageUrl: Uri?,
    val campId: String,
)
