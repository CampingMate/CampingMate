package com.brandon.campingmate.network.retrofit

import com.brandon.campingmate.domain.model.HolidayEntity
import com.brandon.campingmate.domain.model.ResponseLocationBasedList
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.QueryMap

interface NetWorkInterface {
    @GET("imageList")
    suspend fun getImage(@QueryMap param: HashMap<String, String>): Response

    @GET("searchList")
    suspend fun getSearch(@QueryMap param: HashMap<String, String>): ResponseSearch

    @GET("locationBasedList")
    suspend fun getLocationBasedList(@QueryMap param: HashMap<String, String>): ResponseLocationBasedList

    @GET("basedList")
    suspend fun getBasedList(@QueryMap param: HashMap<String, String>): ResponseLocationBasedList

    @GET("getRestDeInfo")
    suspend fun getRestDeInfo(
//        @Query("solMonth") solMonth : String?,
        @Query("ServiceKey") ServiceKey : String?,
        @Query("solYear") solYear : String?,
        @Query("_type") type : String?,
        @Query("numOfRows") numOfRows : Int?
    ): HolidayEntity
}