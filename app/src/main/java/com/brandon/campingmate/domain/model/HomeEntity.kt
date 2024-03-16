package com.brandon.campingmate.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class HomeEntity(
    val contentId: String? = "",
    val firstImageUrl: String? = "",    //이미지 URL
    val facltNm: String? = "",      //캠핑장명
    val lineIntro: String? = "",    //한줄소개
    val addr1: String? = "",    //주소
) : Parcelable
