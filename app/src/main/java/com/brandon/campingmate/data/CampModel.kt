package com.brandon.campingmate.data

import java.util.UUID

data class CampModel(
    val campId: String? = UUID.randomUUID().toString()
)
