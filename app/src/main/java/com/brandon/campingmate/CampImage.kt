package com.brandon.campingmate

import com.google.gson.annotations.SerializedName

data class Response(val response: CampImageResponse)
data class CampImageResponse(
    @SerializedName("header")
    val campHeader: CampHeader,
    @SerializedName("body")
    val campBody: CampBody,
)
data class CampHeader(
    val resultCode: Int?,
    val resultMsg: String?,
)
data class CampBody(
    val totalCount: Int?,
    val pageNo: Int?,
    val numOfRows: Int?,
    @SerializedName("items")
    val campImageItems: CampImageItems?,
)
data class CampImageItems(
    @SerializedName("item")
    val campImageItem: List<CampImageItem>?
)
data class CampImageItem(
    val contentId: Int?,
    val serialnum: Int?,
    val imageurl: String?,
    val createdtime: String?,
    val modifiedtime: String?,
)