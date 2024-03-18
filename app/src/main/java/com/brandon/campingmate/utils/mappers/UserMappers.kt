package com.brandon.campingmate.utils.mappers

import com.brandon.campingmate.data.remote.dto.UserDTO
import com.brandon.campingmate.domain.model.User

fun UserDTO.toUser(): User {
    return User(
        userId = userId,
        userEmail = userEmail,
        nickName = nickName,
        profileImage = profileImage,
        bookmarked = bookmarked
    )
}