package com.brandon.campingmate.presentation.home

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.brandon.campingmate.domain.model.HomeEntity
import com.google.firebase.Firebase
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.launch

class HomeViewModel: ViewModel() {
    private val _reviewItem  : MutableLiveData<MutableList<HomeEntity>> = MutableLiveData()
    val reviewItem : LiveData<MutableList<HomeEntity>> get() = _reviewItem

    private var allData : MutableList<HomeEntity> = mutableListOf()

    fun loadReviewItem(){
        viewModelScope.launch {
            _reviewItem.value?.clear()
            val db = Firebase.firestore
            val allCamps: Query = db.collection("camps").whereNotEqualTo("commentList", listOf<String>())
//            val allCamps: Query = db.collection("reviewTest").whereNotEqualTo("commentList", listOf<String>())
//            val allCamps: Query = db.collection("reviewTest_empty").whereNotEqualTo("commentList", listOf<String>())
            allCamps.get().addOnSuccessListener { documents ->
                Log.d("HomeViewModel","Success")
                for (document in documents) {
                    val allList = document.toObject(HomeEntity::class.java)
                    allData.add(allList)
                }
                Log.d("HomeViewModel","allData : $allData")
                _reviewItem.value=allData
            }
        }
    }


}