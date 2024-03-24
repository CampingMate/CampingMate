package com.brandon.campingmate.domain.model

data class Mart(
    val no: String? = null,
    val callNum: String? = null,
    val size: String? = null,
    val address: String? = null,
    val postNum: String? = null,
    val name: String? = null,
    val type: String? = null,
    val longitude: Double? = null,
    val latitude: Double? = null,
    var geohash: String? = null
)