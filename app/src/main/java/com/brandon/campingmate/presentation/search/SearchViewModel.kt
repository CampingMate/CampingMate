package com.brandon.campingmate.presentation.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.brandon.campingmate.network.retrofit.NetWorkClient
import com.brandon.campingmate.network.retrofit.SearchItem
import kotlinx.coroutines.launch

class SearchViewModel: ViewModel() {
    private val _keywordParam: MutableLiveData<HashMap<String, String>> = MutableLiveData()
    val keywordParam: LiveData<HashMap<String, String>> get() = _keywordParam
    private val _keyword: MutableLiveData<MutableList<SearchItem>> = MutableLiveData()
    val keyword: LiveData<MutableList<SearchItem>> get() = _keyword

    fun setUpParkParameter(text: String) {
        val authKey =
            "wDP6fsVX3kKuaOD7OKrRHaAgPUNtxYUy387PNJRBAW/F6GUdZgv5LyyIAkVXED3leDg3aUD+TFIgBHWCgMBdzQ=="
        _keywordParam.value = hashMapOf(
            "numOfRows" to "10",
            "pageNo" to "1",
            "MobileOS" to "AND",
            "MobileApp" to "CampingMate",
            "serviceKey" to authKey,
            "_type" to "json",
            "keyword" to text
        )
    }
    fun communicateNetWork(param: HashMap<String, String>?) {
        viewModelScope.launch {
            val responseData = param?.let { NetWorkClient.imageNetWork.getSearch(it) }
            val items = responseData?.response?.searchBody?.items
            val myList = mutableListOf<SearchItem>()
            if (items != null) {
                for (item in items) {
                    var value = SearchItem(
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
                    myList.add(value)
                }
            }
            _keyword.value = myList
        }
    }
}