package com.brandon.campingmate.data.remote.dto

import com.google.gson.annotations.SerializedName

data class SearchPostResponse(
    val took: Int?,
    val timedOut: Boolean?,
    @SerializedName("_shards") val shards: Shards?,
    val hits: HitsContainer?
)

data class Shards(
    val total: Int?,
    val successful: Int?,
    val skipped: Int?,
    val failed: Int?
)

data class HitsContainer(
    val total: Total?,
    val maxScore: Double?,
    @SerializedName("hits") val postHits: List<SearchPostHit>?
)

data class Total(
    val value: Int?,
    val relation: String?
)

data class SearchPostHit(
    @SerializedName("_index") val index: String?,
    @SerializedName("_type") val type: String?,
    @SerializedName("_id") val id: String?,
    @SerializedName("_score") val score: Double?,
    @SerializedName("_source") val post: PostHitDTO?
)