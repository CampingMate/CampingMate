package com.brandon.campingmate.presentation.search

import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.brandon.campingmate.domain.model.CampEntity
import com.brandon.campingmate.network.retrofit.NetWorkClient
import com.brandon.campingmate.presentation.search.SearchFragment.Companion.activatedChips
import com.brandon.campingmate.presentation.search.SearchFragment.Companion.doNmList
import com.google.firebase.Firebase
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.launch

class SearchViewModel : ViewModel() {
    private val _keyword: MutableLiveData<MutableList<CampEntity>> = MutableLiveData()
    val keyword: LiveData<MutableList<CampEntity>> get() = _keyword
    private val _myList: MutableLiveData<MutableList<CampEntity>> = MutableLiveData()
    val myList: LiveData<MutableList<CampEntity>> get() = _myList
    var lastVisible: DocumentSnapshot? = null
    var lastVisibleKeyword: DocumentSnapshot? = null
    var isLoadingData: Boolean = false
    var isLoadingDataKeyword: Boolean = false
    var myKeywordList = mutableListOf<String>()
    private val db = Firebase.firestore
    private var baseQuery: Query = db.collection("camps")
    var isKeyword: Boolean = false
    var isFilter: Boolean = false

    fun setUpParkParameter(text: String) {
        val authKey =
            "wDP6fsVX3kKuaOD7OKrRHaAgPUNtxYUy387PNJRBAW/F6GUdZgv5LyyIAkVXED3leDg3aUD+TFIgBHWCgMBdzQ=="
        communicateNetWork(
            hashMapOf(
                "numOfRows" to "25",
                "pageNo" to "1",
                "MobileOS" to "AND",
                "MobileApp" to "CampingMate",
                "serviceKey" to authKey,
                "_type" to "json",
                "keyword" to text
            )
        )
    }

    fun communicateNetWork(param: HashMap<String, String>?) {
        viewModelScope.launch {
            val responseData = param?.let { NetWorkClient.imageNetWork.getSearch(it) }
            val items = responseData?.response?.searchBody?.searchItems?.item
            val contentIds = mutableListOf<String>()
            if (items != null) {
                for (item in items) {
                    val myContentId = item.contentId.toString()
                    if (myContentId != null) {
                        contentIds.add(myContentId)
                    }
                }
                Log.d("checkList", "${contentIds}")
            }
            if (contentIds.isNotEmpty()) {
                callKeywordData(contentIds)
            }
        }
    }

    private fun callKeywordData(myList: MutableList<String>) {
        isKeyword = true
        isFilter = false
        val result = baseQuery.whereIn("contentId", myList)
        myKeywordList.addAll(myList)
        Log.d("checkLog", "$myKeywordList")
        result.limit(5)
            .get()
            .addOnSuccessListener { documents ->
                val newCampListKeyword = mutableListOf<CampEntity>()
                for (document in documents) {
                    val camp = document.toObject(CampEntity::class.java)
                    newCampListKeyword.add(camp)
                }
                if (documents.size() > 0) {
                    _keyword.value = newCampListKeyword
                    lastVisibleKeyword = when (documents.size()) {
                        5 -> documents.documents[documents.size() - 1]
                        else -> null
                    }
                }
                Log.d("checkLog", "${lastVisibleKeyword?.get("facltNm")}")
            }
    }
    fun loadMoreDataKeyword() {
        if (isLoadingDataKeyword) {
            return
        }
        Log.d("checkLog", "loadMoreDataKeyword 호출")
        isLoadingDataKeyword = true
        val result = baseQuery.whereIn("contentId", myKeywordList)

        if (lastVisibleKeyword != null) {
            val next = result
                .startAfter(lastVisibleKeyword!!)
                .limit(5)

            next.get()
                .addOnSuccessListener { documents ->
                    val newCampListKeyword = mutableListOf<CampEntity>()
                    for (document in documents) {
                        val camp = document.toObject(CampEntity::class.java)
                        newCampListKeyword.add(camp)
                    }
                    val currentList = _keyword.value ?: mutableListOf()
                    currentList.addAll(newCampListKeyword)
                    Log.d("checkLog", "리스트 확인 : ${currentList.size}")
                    _keyword.value = currentList
                    if (documents.size() > 0) {
                        lastVisibleKeyword = documents.documents[documents.size() - 1]
                    } else {
                        lastVisibleKeyword = null  // 더 이상 데이터가 없을 때 lastVisible을 null로 설정
                    }
                    isLoadingDataKeyword = false
                }
                .addOnFailureListener { exception ->
                    Log.d("checkLog", "로딩에러 -> ${exception}")
                    isLoadingDataKeyword = false
                }
        }
    }


    fun callData() {
        isKeyword = false
        isFilter = true
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
                val newCampList = mutableListOf<CampEntity>()
                for (document in documents) {
                    val camp = document.toObject(CampEntity::class.java)
                    newCampList.add(camp)
                }
                _myList.value = newCampList
                if(documents.size() > 0){
                    lastVisible = when(documents.size()){
                        5 -> documents.documents[documents.size() - 1]
                        4 -> documents.documents[documents.size() - 2]
                        3 -> documents.documents[documents.size() - 3]
                        2 -> documents.documents[documents.size() - 4]
                        1 -> documents.documents[documents.size() - 5]
                        else -> null
                    }
                }
                Log.d("Search", "첫번째 ${lastVisible?.get("facltNm")}")
            }
            .addOnFailureListener { exception ->
                // 오류 처리
                Log.w("TAG", "Error getting documents.", exception)
            }
    }
    fun loadMoreData() {
        if(isLoadingData){
            return
        }
        isLoadingData = true
        Log.d("Search", "loadMoreData")
        if (lastVisible != null) {
            var result = if (doNmList.isNotEmpty()) {
                baseQuery.whereIn("doNm", doNmList)
            } else {
                baseQuery
            }

            for (chip in activatedChips) {
                when (chip) {
                    "글램핑" -> baseQuery = baseQuery.whereIn("induty1", listOf("글램핑"))
                    "일반야영" -> baseQuery = baseQuery.whereIn("induty2", listOf("일반야영장"))
                    "차박" -> baseQuery = baseQuery.whereIn("induty3", listOf("자동차야영장"))
                    "카라반" -> baseQuery = baseQuery.whereIn("induty4", listOf("카라반"))
                    "화장실" -> baseQuery = baseQuery.whereIn("bathroom", listOf("화장실"))
                    "샤워실" -> baseQuery = baseQuery.whereIn("shower", listOf("샤워실"))
                    "화로대" -> baseQuery = baseQuery.whereIn("fire", listOf("화로대"))
                    "전기" -> baseQuery = baseQuery.whereIn("electronic", listOf("전기"))
                    "냉장고" -> baseQuery = baseQuery.whereIn("refrigerator", listOf("냉장고"))
                    "불멍" -> baseQuery = baseQuery.whereIn("firesee", listOf("불멍"))
                    "에어컨" -> baseQuery = baseQuery.whereIn("aircon", listOf("에어컨"))
                    "침대" -> baseQuery = baseQuery.whereIn("bed", listOf("침대"))
                    "TV" -> baseQuery = baseQuery.whereIn("tv", listOf("TV"))
                    "난방기구" -> baseQuery = baseQuery.whereIn("warmer", listOf("난방기구"))
                    "내부화장실" -> baseQuery = baseQuery.whereIn("innerBathroom", listOf("내부화장실"))
                    "내부샤워실" -> baseQuery = baseQuery.whereIn("innerShower", listOf("내부샤워실"))
                    "유무선인터넷" -> baseQuery = baseQuery.whereIn("internet", listOf("유무선인터넷"))
                    "애견동반" -> baseQuery = baseQuery.whereIn("animalCmgCl", listOf("가능", "가능(소형견)"))
                    "여름물놀이" -> baseQuery = baseQuery.whereIn("summerPlay", listOf("여름물놀이"))
                    "낚시" -> baseQuery = baseQuery.whereIn("fishing", listOf("낚시"))
                    "걷기길" -> baseQuery = baseQuery.whereIn("walking", listOf("걷기길"))
                    "액티비티" -> baseQuery = baseQuery.whereIn("activity", listOf("액티비티"))
                    "봄꽃여행" -> baseQuery = baseQuery.whereIn("springFlower", listOf("봄꽃여행"))
                    "가을단풍명소" -> baseQuery = baseQuery.whereIn("fallLeaves", listOf("가을단풍명소"))
                    "겨울눈꽃명소" -> baseQuery = baseQuery.whereIn("winterSnow", listOf("겨울눈꽃명소"))
                    "일몰명소" -> baseQuery = baseQuery.whereIn("sunset", listOf("일몰명소"))
                    "수상레저" -> baseQuery = baseQuery.whereIn("waterLeisure", listOf("수상레저"))
                    "잔디" -> baseQuery = baseQuery.whereIn("siteBottomCl1", listOf("잔디"))
                    "파쇄석" -> baseQuery = baseQuery.whereIn("siteBottomCl2", listOf("파쇄석"))
                    "테크" -> baseQuery = baseQuery.whereIn("siteBottomCl3", listOf("테크"))
                    "자갈" -> baseQuery = baseQuery.whereIn("siteBottomCl4", listOf("자갈"))
                    "맨흙" -> baseQuery = baseQuery.whereIn("siteBottomCl5", listOf("맨흙"))
                    else -> Unit
                }
            }
            val next = result
                .startAfter(lastVisible!!)
                .limit(5)

            next.get()
                .addOnSuccessListener { nextDocuments ->
                    val newCampList = mutableListOf<CampEntity>()
                    for (document in nextDocuments) {
                        val camp = document.toObject(CampEntity::class.java)
                        newCampList.add(camp)
                    }

                    val currentList = _myList.value ?: mutableListOf()
                    currentList.addAll(newCampList)
//                    Log.d("Search", "리스트확인 : ${newList.size}")
                    _myList.value = currentList
                    if (nextDocuments.size() > 0) {
                        lastVisible = nextDocuments.documents[nextDocuments.size() - 1]
                        Log.d("Search", "무한 ${lastVisible?.get("facltNm")}")
                    } else {
                        lastVisible = null  // 더 이상 데이터가 없을 때 lastVisible을 null로 설정
                    }
                    isLoadingData = false
                }
                .addOnFailureListener { exception ->
                    // 오류 처리
                    Log.w("TAG", "Error getting documents.", exception)
                    isLoadingData = false
                }
        }
    }

}