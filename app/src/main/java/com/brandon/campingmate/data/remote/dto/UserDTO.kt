package com.brandon.campingmate.data.remote.dto

data class UserDTO(
    val userId: String? = null,
    val userEmail: String? = null,
    val nickName: String? = null,
    val profileImage: String? = null,
    val bookmarked: List<String>? = null
)