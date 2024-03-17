package com.brandon.campingmate.domain.model

data class User(
    val userEmail: String?,
    val nickName: String?,
    val profileImage: String?,
    val bookmarked: List<String>?
)
