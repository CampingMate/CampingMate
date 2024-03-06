package com.brandon.campingmate.presentation.home

data class HomeDistrictThemeModel(
    //공통사항
    val campImg : String,
    val campName : String,

    //후기 캠핑장
    val campCategory : String,   //캠핑 유형
    val campAddr : String,  //캠핑장 주소
    val campReviewNum: String, //댓글수
    val campReview : String,    //후기

    //반려동물 캠핑장
    val campCity : String,  //지역
    val campLineIntro : String, //한줄소개

)
