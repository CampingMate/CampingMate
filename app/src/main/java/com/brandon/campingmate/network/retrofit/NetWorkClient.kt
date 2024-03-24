package com.brandon.campingmate.network.retrofit

import com.brandon.campingmate.BuildConfig
import com.brandon.campingmate.data.remote.api.OpenSearchService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object NetWorkClient {
    private const val IMAGE_BASE_URL = "https://apis.data.go.kr/B551011/GoCamping/"
    private const val HOLIDAY_URL = "https://apis.data.go.kr/B090041/openapi/service/SpcdeInfoService/"
    private const val OPEN_SEARCH_BASE_URL = "https://querydocuments-s357wnlf7a-uc.a.run.app"

    private fun createOkHttpClient(): OkHttpClient {
        val interceptor = HttpLoggingInterceptor()

        if (BuildConfig.DEBUG) interceptor.level = HttpLoggingInterceptor.Level.BODY
        else interceptor.level = HttpLoggingInterceptor.Level.NONE

        return OkHttpClient.Builder().connectTimeout(20, TimeUnit.SECONDS).readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS).addNetworkInterceptor(interceptor).build()
    }

    private val imageRetrofit =
        Retrofit.Builder().baseUrl(IMAGE_BASE_URL).addConverterFactory(GsonConverterFactory.create()).client(
            createOkHttpClient()
        ).build()

    val imageNetWork: NetWorkInterface = imageRetrofit.create(NetWorkInterface::class.java)

    private val holidayRetrofit =
        Retrofit.Builder().baseUrl(HOLIDAY_URL).addConverterFactory(GsonConverterFactory.create()).client(
            createOkHttpClient()
        ).build()

    val holidayNetWork: NetWorkInterface = holidayRetrofit.create(NetWorkInterface::class.java)


    // OpenSearch 요청을 위한 OkHttpClient 인스턴스를 생성하는 함수
    private fun createOpenSearchOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level =
                if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
        }

        // 인증을 위한 Interceptor
//        val authInterceptor = Interceptor { chain ->
//            val originalRequest = chain.request()
//            val authenticatedRequest = originalRequest.newBuilder()
//                .header("Authorization", okhttp3.Credentials.basic(OPENSEARCH_ID, OPENSEARCH_PASSWORD))
//                .method(originalRequest.method, originalRequest.body).build()
//            chain.proceed(authenticatedRequest)
//        }

        return OkHttpClient.Builder().connectTimeout(20, TimeUnit.SECONDS).readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS).addInterceptor(loggingInterceptor) // 로깅 인터셉터 추가
            .build()
    }

    // OpenSearch 서비스에 대한 Retrofit 인스턴스
    private val openSearchRetrofit: Retrofit = Retrofit.Builder().baseUrl(OPEN_SEARCH_BASE_URL)
        .client(createOpenSearchOkHttpClient()) // OpenSearch 요청용 OkHttpClient 사용
        .addConverterFactory(GsonConverterFactory.create()).build()

    // OpenSearchDataSource 인터페이스의 구현체
    val openSearchService: OpenSearchService = openSearchRetrofit.create(OpenSearchService::class.java)
}