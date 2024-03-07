package com.brandon.campingmate.presentation.map

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.brandon.campingmate.domain.model.CampEntity
import com.brandon.campingmate.domain.model.NaverItem
import com.brandon.campingmate.domain.model.LocationBasedListItem
import com.brandon.campingmate.network.retrofit.NetWorkClient
import com.brandon.campingmate.network.retrofit.SearchItem
import kotlinx.coroutines.launch
import ted.gun0912.clustering.naver.TedNaverClustering

class MapViewModel : ViewModel(){
    //private lateinit var tedNaverClustering: TedNaverClustering<LocationBasedListItem>
    private var campDataList = mutableListOf<LocationBasedListItem>()
    private var naverItems = mutableListOf<NaverItem>()
    private val _paramHashmap:MutableLiveData<HashMap<String, String>> = MutableLiveData()
    val paramHashmap: LiveData<HashMap<String, String>> get() = _paramHashmap


    private var _campList : MutableLiveData<MutableList<LocationBasedListItem>> = MutableLiveData()
    val campList : LiveData<MutableList<LocationBasedListItem>> get() = _campList
    var authKey =
        "wPKSnhEmKeTSpI60GYZ8ITHvIIfjSvDK2IqmCS+OG1wXeBAn5t+Kxk/I9pV55PhG86E2NhyZj8+VCnkG3AVCTQ=="

    fun getParamHashmap(mapx:String,mapy:String,radius:String){
        _paramHashmap.value = hashMapOf(
            "numOfRows" to "100",
            "pageNo" to "1",
            "MobileOS" to "AND",
            "MobileApp" to "com.brandon.campingmate",
            "serviceKey" to authKey,
            "_type" to "json",
            "mapX" to mapx,
            "mapY" to mapy,
            "radius" to radius
        )
    }
    fun getBlParamHashmap(): HashMap<String, String> {
        var hashMap = hashMapOf(
            "numOfRows" to "500",
            "pageNo" to "1",
            "MobileOS" to "AND",
            "MobileApp" to "com.brandon.campingmate",
            "serviceKey" to authKey,
            "_type" to "json"
        )
        return hashMap
    }
    fun getCampList(param: HashMap<String, String>?){
        viewModelScope.launch {
            val responseData = param?.let {
                NetWorkClient.imageNetWork.getLocationBasedList(it)
            }
            val items = responseData?.response?.LocationBasedListBody?.items?.item
            val locationBasedList = mutableListOf<LocationBasedListItem>()
            if (items != null) {
                for(item in items){
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
            _campList.value =  locationBasedList



        }
    }

    fun getAllCampList(param: HashMap<String, String>?){
        viewModelScope.launch {
            val responseData = param?.let {
                NetWorkClient.imageNetWork.getBasedList(it)
            }
            val items = responseData?.response?.LocationBasedListBody?.items?.item
            val locationBasedList = mutableListOf<LocationBasedListItem>()
            if (items != null) {
                for(item in items){
                    val mapX = if(item.mapX.isNullOrEmpty()) "130" else item.mapX
                    val mapY = if(item.mapY.isNullOrEmpty()) "38" else item.mapY
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
                        mapX = mapX,
                        mapY = mapY,
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
            _campList.value =  locationBasedList
        }
    }
}