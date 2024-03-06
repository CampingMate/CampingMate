package com.brandon.campingmate.domain.model


import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import ted.gun0912.clustering.clustering.TedClusterItem
import ted.gun0912.clustering.geometry.TedLatLng


@Parcelize
data class MapModel(
    var addr1: String?="주소없음",
    var doNm:  String?="미분류",
    var facltNm:  String?="이름없음",
    var firstImageUrl:  String?="https://pbs.twimg.com/media/EgkUVPaUwAAr6K6.jpg",
    var induty: List<String>,
    var lctCl: List<String>? =null,
    var mapX:  String?="129.08832",
    var mapY:  String?="35.67312"
) : Parcelable, TedClusterItem {
    override fun getTedLatLng(): TedLatLng {
        return TedLatLng(mapY!!.toDouble(), mapX!!.toDouble())
    }

}