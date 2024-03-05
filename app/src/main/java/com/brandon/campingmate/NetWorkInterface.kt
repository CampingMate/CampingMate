package com.brandon.campingmate

import retrofit2.http.GET
import retrofit2.http.QueryMap

interface NetWorkInterface {
    @GET("/imageList")
    suspend fun getImage(@QueryMap param: HashMap<String, String>): Response
}