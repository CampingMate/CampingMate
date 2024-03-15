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
    var isLoadingData: Boolean = false
    var pageNo: Int = 1

    fun setUpParkParameter(text: String) {
        val authKey =
            "wDP6fsVX3kKuaOD7OKrRHaAgPUNtxYUy387PNJRBAW/F6GUdZgv5LyyIAkVXED3leDg3aUD+TFIgBHWCgMBdzQ=="
        communicateNetWork(
            hashMapOf(
                "numOfRows" to "10",
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
        val db = Firebase.firestore
        var baseQuery: Query = db.collection("camps")
        val result = baseQuery.whereIn("contentId", myList)
        result.limit(5)
            .get()
            .addOnSuccessListener { documents ->
                val newCampListKeyword = mutableListOf<CampEntity>()
                for (document in documents) {
                    val camp = document.toObject(CampEntity::class.java)
                    newCampListKeyword.add(camp)
                }
                _keyword.value = newCampListKeyword
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
        val db = Firebase.firestore
        if (lastVisible != null) {
            val next = db.collection("camps")
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