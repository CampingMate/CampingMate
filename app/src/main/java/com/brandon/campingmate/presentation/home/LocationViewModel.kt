package com.brandon.campingmate.presentation.home

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.brandon.campingmate.domain.model.CampEntity
import com.google.firebase.Firebase
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore

class LocationViewModel : ViewModel() {
    private val _myList: MutableLiveData<MutableList<CampEntity>> = MutableLiveData()
    val myList: LiveData<MutableList<CampEntity>> get() = _myList
    var lastVisible: DocumentSnapshot? = null
    var isLoading:Boolean = false
    private val db = Firebase.firestore
    private var baseQuery: Query = db.collection("camps")
    private val sudoList = listOf("서울시", "인천시", "경기도")
    private val gangwonList = listOf("강원도")
    private val chungcheongList = listOf("대전시", "세종시", "충청북도", "충청남도")
    private val jeollaList = listOf("광주시", "전라북도", "전라남도")
    private val gyeongsangList = listOf("부산시", "대구시", "울산시", "경상북도", "경상남도")
    private val allList = listOf("서울시", "인천시", "경기도","강원도","대전시", "세종시", "충청북도", "충청남도","광주시", "전라북도", "전라남도","부산시", "대구시", "울산시", "경상북도", "경상남도")
    fun callData(selectedChipName: String) {
        val result = when(selectedChipName){
            "수도권" -> baseQuery.whereIn("doNm", sudoList)
            "충청도" -> baseQuery.whereIn("doNm", chungcheongList)
            "강원도" -> baseQuery.whereIn("doNm", gangwonList)
            "경상도" -> baseQuery.whereIn("doNm", gyeongsangList)
            "전라도" -> baseQuery.whereIn("doNm", jeollaList)
            else -> baseQuery.whereIn("doNm", allList)
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
            }
    }

    fun loadMoreData(selectedChipName: String) {
        if(isLoading){
            return
        }
        isLoading = true
        if(lastVisible != null){
            val result = when(selectedChipName){
                "수도권" -> baseQuery.whereIn("doNm", sudoList)
                "충청도" -> baseQuery.whereIn("doNm", chungcheongList)
                "강원도" -> baseQuery.whereIn("doNm", gangwonList)
                "경상도" -> baseQuery.whereIn("doNm", gyeongsangList)
                "전라도" -> baseQuery.whereIn("doNm", jeollaList)
                else -> baseQuery.whereIn("doNm", allList)
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
                    _myList.value = currentList
                    if (nextDocuments.size() > 0) {
                        lastVisible = nextDocuments.documents[nextDocuments.size() - 1]
                        Log.d("Search", "무한 ${lastVisible?.get("facltNm")}")
                    } else {
                        lastVisible = null  // 더 이상 데이터가 없을 때 lastVisible을 null로 설정
                    }
                    isLoading = false
                }
                .addOnFailureListener { exception ->
                    isLoading = false
                }
        }
    }
}