package com.brandon.campingmate.domain.model

import android.os.Parcelable
import com.google.firebase.Timestamp
import kotlinx.parcelize.Parcelize

@Parcelize
data class Post(
    val postId: String? = null,
    val authorName: String? = null,
    val authorId: String? = null,
    val authorProfileImageUrl: String? = null,
    val title: String? = null,
    val content: String? = null,
    val imageUrls: List<String>? = null,
    val timestamp: Timestamp? = null
) : Parcelable