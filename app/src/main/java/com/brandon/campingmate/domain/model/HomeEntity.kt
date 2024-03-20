package com.brandon.campingmate.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue

@Parcelize
data class HomeEntity(
    val contentId: String? = "",
    val firstImageUrl: String? = "",    //이미지 URL
    val facltNm: String? = "",      //캠핑장명
    val lineIntro: String? = "",    //한줄소개
    val addr1: String? = "",    //주소

    val induty1:String? = "",    //글램핑
    val induty2:String? = "",    //일반야영장
    val induty3:String? = "",    //자동차야영장
    val induty4:String? = "",    //카라반
    val commentList: MutableList<@RawValue Map<String, Any?>> = mutableListOf()   //댓글
) : Parcelable
