package com.brandon.campingmate.network.retrofit

import com.google.gson.annotations.SerializedName

data class Response(val response: CampImageResponse)
data class CampImageResponse(
    @SerializedName("header")
    val campHeader: CampHeader,
    @SerializedName("body")
    val campBody: CampBody,
)

data class CampHeader(
    val resultCode: Int?,
    val resultMsg: String?,
)

data class CampBody(
    val totalCount: Int?,
    val pageNo: Int?,
    val numOfRows: Int?,
    @SerializedName("items")
    val campImageItems: CampImageItems?,
)

data class CampImageItems(
    @SerializedName("item")
    val campImageItem: List<CampItem>?
)

data class CampItem(
    val contentId: Int?,
    val serialnum: Int?,
    val imageurl: String?,
    val createdtime: String?,
    val modifiedtime: String?,
)


data class ResponseSearch(val response: SearchResponse)
data class SearchResponse(
    @SerializedName("header")
    val searchHeader: SearchHeader,
    @SerializedName("body")
    val searchBody: SearchBody,
)

data class SearchHeader(
    val resultCode: String?,
    val resultMsg: String?,
)

data class SearchBody(
    val numOfRows: Int?,
    val pageNo: Int?,
    val totalCount: Int?,
    @SerializedName("items")
    val items: MutableList<SearchItem>?,
)

data class SearchItem(
    val firstImageUrl: String?,
    val siteMg3Vrticl: String?,
    val siteMg2Vrticl: String?,
    val siteMg1Co: String?,
    val siteMg2Co: String?,
    val siteMg3Co: String?,
    val siteBottomCl1: String?,
    val siteBottomCl2: String?,
    val siteBottomCl3: String?,
    val siteBottomCl4: String?,
    val fireSensorCo: String?,
    val themaEnvrnCl: String?,
    val eqpmnLendCl: String?,
    val animalCmgCl: String?,
    val tooltip: String?,
    val glampInnerFclty: String?,
    val caravInnerFclty: String?,
    val prmisnDe: String?,
    val operPdCl: String?,
    val operDeCl: String?,
    val trlerAcmpnyAt: String?,
    val caravAcmpnyAt: String?,
    val toiletCo: String?,
    val frprvtWrppCo: String?,
    val frprvtSandCo: String?,
    val induty: String?,
    val siteMg1Vrticl: String?,
    val posblFcltyEtc: String?,
    val clturEventAt: String?,
    val clturEvent: String?,
    val exprnProgrmAt: String?,
    val exprnProgrm: String?,
    val extshrCo: String?,
    val manageSttus: String?,
    val hvofBgnde: String?,
    val hvofEnddle: String?,
    val trsagntNo: String?,
    val bizrno: String?,
    val facltDivNm: String?,
    val mangeDivNm: String?,
    val mgcDiv: String?,
    val tourEraCl: String?,
    val lctCl: String?,
    val doNm: String?,
    val sigunguNm: String?,
    val zipcode: String?,
    val addr1: String?,
    val addr2: String?,
    val mapX: String?,
    val mapY: String?,
    val direction: String?,
    val tel: String?,
    val homepage: String?,
    val contentId: String?,
    val swrmCo: String?,
    val wtrplCo: String?,
    val brazierCl: String?,
    val sbrsCl: String?,
    val sbrsEtc: String?,
    val modifiedtime: String?,
    val facltNm: String?,
    val lineIntro: String?,
    val intro: String?,
    val allar: String?,
    val insrncAt: String?,
    val resveUrl: String?,
    val resveCl: String?,
    val manageNmpr: String?,
    val gnrlSiteCo: String?,
    val autoSiteCo: String?,
    val glampSiteCo: String?,
    val caravSiteCo: String?,
    val indvdlCaravSiteCo: String?,
    val sitedStnc: String?,
    val siteMg1Width: String?,
    val siteMg2Width: String?,
    val siteMg3Width: String?,
    val createdtime: String?,
    val posblFcltyCl: String?,
    val featureNm: String?,
    val siteBottomCl5: String?
)
