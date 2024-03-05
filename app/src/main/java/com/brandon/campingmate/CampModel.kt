package com.brandon.campingmate

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.UUID

@Parcelize
data class CampModel(
    val addr1: String? = "",
    val addr2: String? = "",
    val allar: String? = "",
    val animalCmgCl: String? = "",
    val autoSiteCo: String? = "",
    val bizrno: String? = "",
    val brazierCl: String? = "",
    val caravAcmpnyAt: String? = "",
    val caravInnerFclty: List<String>? = emptyList(),
    val caravSiteCo: String? = "",
    val clturEvent: String? = "",
    val clturEventAt: String? = "",
    val contentId: String? = "",
    val createdtime: String? = "",
    val direction: String? = "",
    val doNm: String? = "",
    val eqpmnLendCl: List<String>? = emptyList(),
    val exprnProgrm: String? = "",
    val exprnProgrmAt: String? = "",
    val extshrCo: String? = "",
    val facltDivNm: String? = "",
    val facltNm: String? = "",
    val featureNm: String? = "",
    val fireSensorCo: String? = "",
    val firstImageUrl: String? = "",
    val frprvtSandCo: String? = "",
    val frprvtWrppCo: String? = "",
    val glampInnerFclty: List<String>? = emptyList(),
    val glampSiteCo: String? = "",
    val gnrlSiteCo: String? = "",
    val homepage: String? = "",
    val hvofBgnde: String? = "",
    val hvofEnddle: String? = "",
    val induty: List<String>? = emptyList(),
    val induty1: String? = "",
    val induty2: String? = "",
    val induty3: String? = "",
    val induty4: String? = "",
    val indvdlCaravSiteCo: String? = "",
    val insrncAt: String? = "",
    val intro: String? = "",
    val lctCl: List<String>? = emptyList(),
    val lineIntro: String? = "",
    val manageNmpr: String? = "",
    val manageSttus: String? = "",
    val mangeDivNm: String? = "",
    val mapX: String? = "",
    val mapY: String? = "",
    val mgcDiv: String? = "",
    val modifiedtime: String? = "",
    val operDeCl: String? = "",
    val operPdCl: String? = "",
    val posblFcltyCl: List<String>? = emptyList(),
    val posblFcltyEtc: String? = "",
    val prmisnDe: String? = "",
    val resveCl: String? = "",
    val resveUrl: String? = "",
    val sbrsCl: List<String>? = emptyList(),
    val sbrsEtc: String? = "",
    val sigunguNm: String? = "",
    val siteBottomCl1: String? = "",
    val siteBottomCl2: String? = "",
    val siteBottomCl3: String? = "",
    val siteBottomCl4: String? = "",
    val siteBottomCl5: String? = "",
    val siteMg1Co: String? = "",
    val siteMg1Vrticl: String? = "",
    val siteMg1Width: String? = "",
    val siteMg2Co: String? = "",
    val siteMg2Vrticl: String? = "",
    val siteMg2Width: String? = "",
    val siteMg3Co: String? = "",
    val siteMg3Vrticl: String? = "",
    val siteMg3Width: String? = "",
    val sitedStnc: String? = "",
    val swrmCo: String? = "",
    val tel: String? = "",
    val themaEnvrnCl: List<String>? = emptyList(),
    val toiletCo: String? = "",
    val tooltip: String? = "",
    val tourEraCl: String? = "",
    val trlerAcmpnyAt: String? = "",
    val trsagntNo: String? = "",
    val wtrplCo: String? = "",
    val zipcode: String? = "",
) : Parcelable

@Parcelize
data class CampDetailModel(
    val contentId: String?, //id
    val facltNm: String?, //야영장이름
    val wtrplCo: String?, //개수대
    val brazierCl: String?,//화로대
    val sbrsCl: String?,//부대시설
    val posblFcltyCl: String?, //주변이용가능시설
    val hvofBgnde: String?, //휴장기간, 휴무기간 시작일
    val hvofEnddle: String?, //종료일
    val toiletCo: String?, //화장실개수
    val swrmCo: String?, //샤워실개수
    val featureNm: String?, //특징
    val induty: String?, //업종
    val addr1: String?, //주소
    val tel: String?, //전화
    val homepage: String?, //홈페이지
    val resveUrl: String?, //예약페이지
    val siteBottomCl1: String?, //잔디
    val siteBottomCl2: String?, //파쇄석
    val siteBottomCl3: String?, //테크
    val siteBottomCl4: String?, //자갈
    val siteBottomCl5: String?, //맨흙
    val glampInnerFclty: String?, //글램핑내부시설
    val caravInnerFclty: String?, //카라반내부시설
    val intro: String?, //소개
    val themaEnvrnCl: String?, //테마환경
) : Parcelable