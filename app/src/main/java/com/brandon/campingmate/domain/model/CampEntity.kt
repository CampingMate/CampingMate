package com.brandon.campingmate.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import ted.gun0912.clustering.clustering.TedClusterItem
import ted.gun0912.clustering.geometry.TedLatLng

@Parcelize
data class CampEntity(
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
    val sbrsCl: String? = "",
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