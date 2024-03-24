package com.brandon.campingmate.data.remote.api

import com.brandon.campingmate.data.remote.dto.SearchPostResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface OpenSearchService {
    @GET("/")
    suspend fun searchPosts(@Query("keyword") keyword: String): SearchPostResponse
}