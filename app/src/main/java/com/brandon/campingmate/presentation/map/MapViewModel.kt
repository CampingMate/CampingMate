package com.brandon.campingmate.presentation.map

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.brandon.campingmate.BuildConfig
import com.brandon.campingmate.R
import com.brandon.campingmate.domain.model.LocationBasedListItem
import com.brandon.campingmate.network.retrofit.NetWorkClient
import com.google.firebase.firestore.FirebaseFirestore
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.overlay.Align
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.util.MarkerIcons
import kotlinx.coroutines.launch
import timber.log.Timber

class MapViewModel : ViewModel(){

    private val _paramHashmap:MutableLiveData<HashMap<String, String>>? = MutableLiveData()
    val paramHashmap: LiveData<HashMap<String, String>>? get() = _paramHashmap
    private val _imageRes: MutableLiveData<MutableList<String>> = MutableLiveData()
    val imageRes: LiveData<MutableList<String>> get() = _imageRes

    private var _campList : MutableLiveData<MutableList<LocationBasedListItem>> = MutableLiveData()
    val campList : LiveData<MutableList<LocationBasedListItem>> get() = _campList
    val campMarker: LiveData<MutableList<Marker>> get() = _campMarker
    private val _campMarker: MutableLiveData<MutableList<Marker>> = MutableLiveData()
    val bookmarkedList : LiveData<MutableList<LocationBasedListItem>> get() = _bookmarkedList
    private var _bookmarkedList : MutableLiveData<MutableList<LocationBasedListItem>> = MutableLiveData()
    val bookmarkCampMarker: LiveData<MutableList<Marker>> get() = _bookmarkCampMarker
    private val _bookmarkCampMarker: MutableLiveData<MutableList<Marker>> = MutableLiveData()
    private val db = FirebaseFirestore.getInstance()
    var authKey =
        "wPKSnhEmKeTSpI60GYZ8ITHvIIfjSvDK2IqmCS+OG1wXeBAn5t+Kxk/I9pV55PhG86E2NhyZj8+VCnkG3AVCTQ=="

    fun getBlParamHashmap(): HashMap<String, String> {
        var hashMap = hashMapOf(
            "numOfRows" to "4000",
            "pageNo" to "1",
            "MobileOS" to "AND",
            "MobileApp" to "com.brandon.campingmate",
            "serviceKey" to authKey,
            "_type" to "json"
        )
        return hashMap
    }

    fun getImgParamHashmap(contentId:String): HashMap<String, String> {
        var hashMap = hashMapOf(
            "numOfRows" to "8",
            "pageNo" to "1",
            "MobileOS" to "AND",
            "MobileApp" to "com.brandon.campingmate",
            "serviceKey" to authKey,
            "_type" to "json",
            "contentId" to contentId
        )
        return hashMap
    }

    fun getAllCampList(param: HashMap<String, String>?){
        viewModelScope.launch {
            try {
                val responseData = param?.let {
                    NetWorkClient.imageNetWork.getBasedList(it)
                }
                val items = responseData?.response?.LocationBasedListBody?.item?.item
                val locationBasedList = mutableListOf<LocationBasedListItem>()
                if (items != null) {
                    for(item in items){
                        if(item.mapX.isNullOrEmpty() || item.mapY.isNullOrEmpty()) {
                            continue
                        }
                        var value = LocationBasedListItem(
                            firstImageUrl = item.firstImageUrl,
                            siteMg3Vrticl = item.siteMg3Vrticl,
                            siteMg2Vrticl = item.siteMg2Vrticl,
                            siteMg1Co = item.siteMg1Co,
                            siteMg2Co = item.siteMg2Co,
                            siteMg3Co = item.siteMg3Co,
                            siteBottomCl1 = item.siteBottomCl1,
                            siteBottomCl2 = item.siteBottomCl2,
                            siteBottomCl3 = item.siteBottomCl3,
                            siteBottomCl4 = item.siteBottomCl4,
                            fireSensorCo = item.fireSensorCo,
                            themaEnvrnCl = item.themaEnvrnCl,
                            eqpmnLendCl = item.eqpmnLendCl,
                            animalCmgCl = item.animalCmgCl,
                            tooltip = item.tooltip,
                            glampInnerFclty = item.glampInnerFclty,
                            caravInnerFclty = item.caravInnerFclty,
                            prmisnDe = item.prmisnDe,
                            operPdCl = item.operPdCl,
                            operDeCl = item.operDeCl,
                            trlerAcmpnyAt = item.trlerAcmpnyAt,
                            caravAcmpnyAt = item.caravAcmpnyAt,
                            toiletCo = item.toiletCo,
                            frprvtWrppCo = item.frprvtWrppCo,
                            frprvtSandCo = item.frprvtSandCo,
                            induty = item.induty,
                            siteMg1Vrticl = item.siteMg1Vrticl,
                            posblFcltyEtc = item.posblFcltyEtc,
                            clturEventAt = item.clturEventAt,
                            clturEvent = item.clturEvent,
                            exprnProgrmAt = item.exprnProgrmAt,
                            exprnProgrm = item.exprnProgrm,
                            extshrCo = item.extshrCo,
                            manageSttus = item.manageSttus,
                            hvofBgnde = item.hvofBgnde,
                            hvofEnddle = item.hvofEnddle,
                            trsagntNo = item.trsagntNo,
                            bizrno = item.bizrno,
                            facltDivNm = item.facltDivNm,
                            mangeDivNm = item.mangeDivNm,
                            mgcDiv = item.mgcDiv,
                            tourEraCl = item.tourEraCl,
                            lctCl = item.lctCl,
                            doNm = item.doNm,
                            sigunguNm = item.sigunguNm,
                            zipcode = item.zipcode,
                            addr1 = item.addr1,
                            addr2 = item.addr2,
                            mapX = item.mapX,
                            mapY = item.mapY,
                            direction = item.direction,
                            tel = item.tel,
                            homepage = item.homepage,
                            contentId = item.contentId,
                            swrmCo = item.swrmCo,
                            wtrplCo = item.wtrplCo,
                            brazierCl = item.brazierCl,
                            sbrsCl = item.sbrsCl,
                            sbrsEtc = item.sbrsEtc,
                            modifiedtime = item.modifiedtime,
                            facltNm = item.facltNm,
                            lineIntro = item.lineIntro,
                            intro = item.intro,
                            allar = item.allar,
                            insrncAt = item.insrncAt,
                            resveUrl = item.resveUrl,
                            resveCl = item.resveCl,
                            manageNmpr = item.manageNmpr,
                            gnrlSiteCo = item.gnrlSiteCo,
                            autoSiteCo = item.autoSiteCo,
                            glampSiteCo = item.glampSiteCo,
                            caravSiteCo = item.caravSiteCo,
                            indvdlCaravSiteCo = item.indvdlCaravSiteCo,
                            sitedStnc = item.sitedStnc,
                            siteMg1Width = item.siteMg1Width,
                            siteMg2Width = item.siteMg2Width,
                            siteMg3Width = item.siteMg3Width,
                            createdtime = item.createdtime,
                            posblFcltyCl = item.posblFcltyCl,
                            featureNm = item.featureNm,
                            siteBottomCl5 = item.siteBottomCl5

                        )
                        locationBasedList.add(value)
                    }
                }

                locationBasedList.sortWith(compareBy<LocationBasedListItem> { it.mapX }.thenBy { it.mapY })
                val tempList = mutableListOf<Marker>()
                for (camp in locationBasedList) {
                    val marker = Marker()
                    if (camp.mapX.isNullOrEmpty() || camp.mapY.isNullOrEmpty()) {
                        continue
                    }
                    marker.captionText = camp.facltNm.toString()
                    marker.captionRequestedWidth = 400
                    marker.setCaptionAligns(Align.Top)
                    marker.tag = camp
                    marker.captionOffset = 5
                    marker.captionTextSize = 16f
                    marker.position = LatLng(camp.mapY.toDouble(), camp.mapX.toDouble())
                    tempList.add(marker)
                }
                _campList.value =  locationBasedList
                _campMarker.value = tempList
                //북마크 로드

            } catch (e:Exception ){
                Timber.tag("error").e("error : MapviewModel.getAllCampList()")
            }
        }
    }

    fun getImgList(map: HashMap<String,String>?) {
        viewModelScope.launch {
            try {
                val response = map?.let { NetWorkClient.imageNetWork.getImage(it) }
                val items = response?.response?.campBody?.campImageItems?.campImageItem
                val imgList = mutableListOf<String>()
                if (items != null) {
                    for(item in items){
                        val imgUrl = item.imageUrl
                        if (imgUrl != null) {
                            imgList.add(imgUrl)
                        }
                    }
                }
                if(imgList.isEmpty()){
                    imgList.add("android.resource://${BuildConfig.APPLICATION_ID}/${R.drawable.ic_login_img}")
                }
                _imageRes.value = imgList
            } catch (e:Exception){
                Timber.tag("error").e("error :  MapviewModel.getImgList()")
            }
        }
    }

    fun getBookmarkedCamp(userId:String,campData : MutableList<LocationBasedListItem> ){
        viewModelScope.launch {
            val db = FirebaseFirestore.getInstance()
            val docRef = db.collection("users").document(userId)
            val bookmarkedContentIds = mutableListOf<String>()
            docRef.get().addOnSuccessListener {
                if (it.exists()) {
                    val bookmarkData = it.get("bookmarked") as? List<*>
                    if (bookmarkData != null) {
                        for (item in bookmarkData) {
                            bookmarkedContentIds.add(item.toString())
                        }
                        //Timber.tag("test").d("콘텐츠 아이디는: $bookmarkedContentIds")
                    }
                }
                val bookmarkedCampIds = campData.filter {
                    bookmarkedContentIds.contains(it.contentId)
                }.toMutableList()

                _bookmarkedList.value = bookmarkedCampIds

                val bookmarkMarker = mutableListOf<Marker>()

                for (camp in bookmarkedCampIds) {
                    val marker = Marker()
                    if (camp.mapX.isNullOrEmpty() || camp.mapY.isNullOrEmpty()) {
                        continue
                    }
                    marker.captionText = camp.facltNm.toString()
                    marker.captionRequestedWidth = 400
                    marker.setCaptionAligns(Align.Top)
                    marker.icon = MarkerIcons.RED
                    marker.tag = camp
                    marker.captionOffset = 5
                    marker.captionTextSize = 16f
                    marker.position = LatLng(camp.mapY.toDouble(), camp.mapX.toDouble())
                    bookmarkMarker.add(marker)
                }
                _bookmarkCampMarker.value = bookmarkMarker
            }.addOnFailureListener {
                Timber.tag("error").e("error : MapviewModel.getBookmarkedCamp()")
            }
        }

    }

    fun getBookmarkedList(campData: MutableList<LocationBasedListItem>,userId: String) {
        if (userId != null) {
            //Timber.tag("maptest").d("userid= ${userId}" )
            val docRef = db.collection("users").document(userId)
            docRef.get().addOnSuccessListener {
                getBookmarkedCamp(it.id, campData)
                //Timber.tag("로그인 정보").d("북마크 리스트 = " + it.id)
            }
        }
    }
}