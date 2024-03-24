package com.brandon.campingmate.domain.model

import android.os.Parcelable
import com.google.firebase.Timestamp
import kotlinx.parcelize.Parcelize

@Parcelize
data class Post(
    val postId: String?,
    val authorName: String?,
    val authorId: String?,
    val authorProfileImageUrl: String?,
    val title: String?,
    val content: String?,
    val imageUrls: List<String>?,
    val timestamp: Timestamp?
) : Parcelable