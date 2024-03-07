package com.brandon.campingmate.domain.model


import android.os.Parcelable
import com.brandon.campingmate.network.retrofit.SearchHeader
import com.google.gson.annotations.SerializedName
import com.naver.maps.geometry.LatLng
import kotlinx.parcelize.Parcelize
import ted.gun0912.clustering.clustering.TedClusterItem
import ted.gun0912.clustering.geometry.TedLatLng


//@Parcelize
//data class MapEntity(
//    var addr1: String?="주소없음",
//    var doNm:  String?="미분류",
//    var facltNm:  String?="이름없음",
//    var firstImageUrl:  String?="https://pbs.twimg.com/media/EgkUVPaUwAAr6K6.jpg",
//    var induty: List<String>,
//    var lctCl: List<String>? =null,
//    var mapX:  String?="129.08832",
//    var mapY:  String?="35.67312"
//) : Parcelable

data class NaverItem(var position: LatLng) : TedClusterItem {
    override fun getTedLatLng(): TedLatLng {
        return TedLatLng(position.latitude, position.longitude)
    }

    var donm : String? = null
    constructor(lat: Double, lng: Double) : this(LatLng(lat, lng)) {
        donm = null
    }

    constructor(lat: Double, lng: Double, donm : String?) : this(
        LatLng(lat, lng)
    ) {
        this.donm = donm
    }
}

data class ResponseLocationBasedList(val response: LocationBasedListResponse)
data class LocationBasedListResponse(
    @SerializedName("header")
    val searchHeader: SearchHeader,
    @SerializedName("body")
    val  LocationBasedListBody:  LocationBasedListBody,
)

data class LocationBasedListBody(
    val numOfRows: Int?,
    val pageNo: Int?,
    val totalCount: Int?,
    @SerializedName("items")
    val items: LocationBasedListitems?,
)
data class LocationBasedListitems(
    @SerializedName("item")
    val item: MutableList<LocationBasedListItem>?,
)

@Parcelize
data class LocationBasedListItem(
    val wtrplCo	: String?,
    val brazierCl	: String?,
    val featureNm	: String?,
    val induty	: String?,
    val caravAcmpnyAt	: String?,
    val toiletCo	: String?,
    val swrmCo	: String?,
    val intro	: String?,
    val allar	: String?,
    val insrncAt	: String?,
    val trsagntNo	: String?,
    val bizrno	: String?,
    val facltDivNm	: String?,
    val mangeDivNm	: String?,
    val exprnProgrmAt	: String?,
    val exprnProgrm	: String?,
    val extshrCo	: String?,
    val frprvtWrppCo	: String?,
    val frprvtSandCo	: String?,
    val caravInnerFclty	: String?,
    val prmisnDe	: String?,
    val operPdCl	: String?,
    val operDeCl	: String?,
    val trlerAcmpnyAt	: String?,
    val mgcDiv	: String?,
    val manageSttus	: String?,
    val hvofBgnde	: String?,
    val hvofEnddle	: String?,
    val siteMg1Width	: String?,
    val siteMg2Width	: String?,
    val siteMg3Width	: String?,
    val siteMg1Vrticl	: String?,
    val siteMg2Vrticl	: String?,
    val siteMg3Vrticl	: String?,
    val siteMg1Co	: String?,
    val siteMg2Co	: String?,
    val siteMg3Co	: String?,
    val siteBottomCl1	: String?,
    val siteBottomCl2	: String?,
    val fireSensorCo	: String?,
    val themaEnvrnCl	: String?,
    val eqpmnLendCl	: String?,
    val animalCmgCl	: String?,
    val tourEraCl	: String?,
    val firstImageUrl	: String?="https://pbs.twimg.com/media/EgkUVPaUwAAr6K6.jpg",
    val createdtime	: String?,
    val modifiedtime	: String?,
    val doNm	: String?="미분류",
    val sigunguNm	: String?,
    val zipcode	: String?,
    val addr1	: String?="주소없음",
    val addr2	: String?,
    val mapX	: String?="129.08832",
    val mapY	: String?="35.67312",
    val direction	: String?,
    val tel	: String?,
    val homepage	: String?,
    val resveUrl	: String?,
    val resveCl	: String?,
    val manageNmpr	: String?,
    val gnrlSiteCo	: String?,
    val autoSiteCo	: String?,
    val glampSiteCo	: String?,
    val caravSiteCo	: String?,
    val indvdlCaravSiteCo	: String?,
    val sitedStnc	: String?,
    val sbrsEtc	: String?,
    val posblFcltyCl	: String?,
    val posblFcltyEtc	: String?,
    val clturEventAt	: String?,
    val clturEvent	: String?,
    val siteBottomCl3	: String?,
    val siteBottomCl4	: String?,
    val siteBottomCl5	: String?,
    val tooltip	: String?,
    val glampInnerFclty	: String?,
    val contentId	: String?,
    val facltNm	: String?="이름없음",
    val lineIntro	: String?,
    val sbrsCl	: String?,
    val lctCl	: String?
) : TedClusterItem, Parcelable {
    override fun getTedLatLng(): TedLatLng {
        return TedLatLng(mapY!!.toDouble(), mapX!!.toDouble())
    }

}
