package com.brandon.campingmate.network.retrofit

import retrofit2.http.GET
import retrofit2.http.QueryMap

interface NetWorkInterface {
    @GET("imageList")
    suspend fun getImage(@QueryMap param: HashMap<String, String>): Response

    @GET("searchList")
    suspend fun getSearch(@QueryMap param: HashMap<String, String>): ResponseSearch
}