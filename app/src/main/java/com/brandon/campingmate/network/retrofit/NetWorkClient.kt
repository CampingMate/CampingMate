package com.brandon.campingmate.network.retrofit

import com.getkeepsafe.relinker.BuildConfig
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import java.util.concurrent.TimeUnit

object NetWorkClient {
    private const val IMAGE_BASE_URL = "https://apis.data.go.kr/B551011/GoCamping/"
    private const val HOLIDAY_URL = "https://apis.data.go.kr/B090041/openapi/service/SpcdeInfoService/"
    private fun createOkHttpClient(): OkHttpClient {
        val interceptor = HttpLoggingInterceptor()

        if (BuildConfig.DEBUG)
            interceptor.level = HttpLoggingInterceptor.Level.BODY
        else
            interceptor.level = HttpLoggingInterceptor.Level.NONE

        return OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .addNetworkInterceptor(interceptor)
            .build()
    }

    private val imageRetrofit = Retrofit.Builder()
        .baseUrl(IMAGE_BASE_URL).addConverterFactory(GsonConverterFactory.create()).client(
            createOkHttpClient()
        ).build()

    val imageNetWork: NetWorkInterface = imageRetrofit.create(NetWorkInterface::class.java)

    private val holidayRetrofit = Retrofit.Builder()
        .baseUrl(HOLIDAY_URL).addConverterFactory(GsonConverterFactory.create(GsonBuilder().setLenient().create())).client(
            createOkHttpClient()
        ).build()

    val holidayNetWork : NetWorkInterface = holidayRetrofit.create(NetWorkInterface::class.java)

}