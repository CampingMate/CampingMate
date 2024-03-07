package com.brandon.campingmate.presentation.search

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.brandon.campingmate.domain.model.CampEntity
import com.brandon.campingmate.network.retrofit.NetWorkClient
import com.brandon.campingmate.network.retrofit.SearchItem
import com.brandon.campingmate.presentation.search.SearchFragment.Companion.activatedChips
import com.brandon.campingmate.presentation.search.SearchFragment.Companion.campList
import com.brandon.campingmate.presentation.search.SearchFragment.Companion.doNmList
import com.google.firebase.Firebase
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.launch

class SearchViewModel: ViewModel() {
    private val _keywordParam: MutableLiveData<HashMap<String, String>> = MutableLiveData()
    val keywordParam: LiveData<HashMap<String, String>> get() = _keywordParam
    private val _keyword: MutableLiveData<MutableList<CampEntity>> = MutableLiveData()
    val keyword: LiveData<MutableList<CampEntity>> get() = _keyword
    private val _myList: MutableLiveData<MutableList<CampEntity>> = MutableLiveData()
    val myList: LiveData<MutableList<CampEntity>> get() = _myList

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
            val items = responseData?.response?.searchBody?.items?.item
            val contentIds = mutableListOf<String>()
            if (items != null) {
                for(item in items){
                    val myContentId = item.contentId.toString()
                    if (myContentId != null) {
                        contentIds.add(myContentId)
                    }
                }
                Log.d("checkList", "${contentIds}")
            }
            callKeywordData(contentIds)
        }
    }

    private fun callKeywordData(myList: MutableList<String>) {
        val db = Firebase.firestore
        var baseQuery: Query = db.collection("camps")
        val result = baseQuery.whereIn("contentId", myList)
        result.limit(5)
            .get()
            .addOnSuccessListener { documents ->
                for(document in documents){
                    val camp = document.toObject(CampEntity::class.java)
                    campList.add(camp)
                }
                _keyword.value = campList
            }
    }

    fun callData() {
        val db = Firebase.firestore
        var baseQuery: Query = db.collection("camps")
        var result = if (doNmList.isNotEmpty()) {
            baseQuery.whereIn("doNm", doNmList)
        } else {
            baseQuery
        }

        for (chip in activatedChips) {
            when (chip) {
                "글램핑" -> result = result.whereIn("induty1", listOf("글램핑"))
                "일반야영" -> result = result.whereIn("induty2", listOf("일반야영장"))
                "차박" -> result = result.whereIn("induty3", listOf("자동차야영장"))
                "카라반" -> result = result.whereIn("induty4", listOf("카라반"))
                "화장실" -> result = result.whereIn("bathroom", listOf("화장실"))
                "샤워실" -> result = result.whereIn("shower", listOf("샤워실"))
                "화로대" -> result = result.whereIn("fire", listOf("화로대"))
                "전기" -> result = result.whereIn("electronic", listOf("전기"))
                "냉장고" -> result = result.whereIn("refrigerator", listOf("냉장고"))
                "불멍" -> result = result.whereIn("firesee", listOf("불멍"))
                "에어컨" -> result = result.whereIn("aircon", listOf("에어컨"))
                "침대" -> result = result.whereIn("bed", listOf("침대"))
                "TV" -> result = result.whereIn("tv", listOf("TV"))
                "난방기구" -> result = result.whereIn("warmer", listOf("난방기구"))
                "내부화장실" -> result = result.whereIn("innerBathroom", listOf("내부화장실"))
                "내부샤워실" -> result = result.whereIn("innerShower", listOf("내부샤워실"))
                "유무선인터넷" -> result = result.whereIn("internet", listOf("유무선인터넷"))
                "애견동반" -> result = result.whereIn("animalCmgCl", listOf("가능", "가능(소형견)"))
                "여름물놀이" -> result = result.whereIn("summerPlay", listOf("여름물놀이"))
                "낚시" -> result = result.whereIn("fishing", listOf("낚시"))
                "걷기길" -> result = result.whereIn("walking", listOf("걷기길"))
                "액티비티" -> result = result.whereIn("activity", listOf("액티비티"))
                "봄꽃여행" -> result = result.whereIn("springFlower", listOf("봄꽃여행"))
                "가을단풍명소" -> result = result.whereIn("fallLeaves", listOf("가을단풍명소"))
                "겨울눈꽃명소" -> result = result.whereIn("winterSnow", listOf("겨울눈꽃명소"))
                "일몰명소" -> result = result.whereIn("sunset", listOf("일몰명소"))
                "수상레저" -> result = result.whereIn("waterLeisure", listOf("수상레저"))
                "잔디" -> result = result.whereIn("siteBottomCl1", listOf("잔디"))
                "파쇄석" -> result = result.whereIn("siteBottomCl2", listOf("파쇄석"))
                "테크" -> result = result.whereIn("siteBottomCl3", listOf("테크"))
                "자갈" -> result = result.whereIn("siteBottomCl4", listOf("자갈"))
                "맨흙" -> result = result.whereIn("siteBottomCl5", listOf("맨흙"))
                else -> Unit
            }
        }

        result.limit(5)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val camp = document.toObject(CampEntity::class.java)
                    campList.add(camp)
                }
                _myList.value = campList
            }
            .addOnFailureListener { exception ->
                // 오류 처리
                // 예: Log.w("TAG", "Error getting documents.", exception)
            }
    }
}