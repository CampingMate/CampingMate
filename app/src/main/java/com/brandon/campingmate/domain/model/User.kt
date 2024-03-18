package com.brandon.campingmate.domain.model

data class User(
    val userId: String?,
    val userEmail: String?,
    val nickName: String?,
    val profileImage: String?,
    val bookmarked: List<String>?
)
