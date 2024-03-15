package com.brandon.campingmate.presentation.campdetail

import android.net.Uri

data class CampCommentEntity(
    val userId: String,
    val userName: Any?,
    val content: String,
    val date: String,
    val imageUrl: Uri?,
)
