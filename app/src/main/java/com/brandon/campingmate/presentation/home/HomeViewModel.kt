package com.brandon.campingmate.presentation.home

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.brandon.campingmate.domain.model.HomeEntity
import com.brandon.campingmate.presentation.home.adapter.PetAdapter
import com.google.firebase.Firebase
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.launch

class HomeViewModel: ViewModel() {
    private val _reviewItem  : MutableLiveData<MutableList<HomeEntity>> = MutableLiveData()
    val reviewItem : LiveData<MutableList<HomeEntity>> get() = _reviewItem

    private val _petItem : MutableLiveData<MutableList<HomeEntity>> = MutableLiveData()
    val petItem : MutableLiveData<MutableList<HomeEntity>> get() = _petItem

    private var allData : MutableList<HomeEntity> = mutableListOf()

    private val db = Firebase.firestore
    private val allCity: Query = db.collection("camps")

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

    fun initPetView() : MutableList<HomeEntity>{
        //반려동물
        val petData = mutableListOf<HomeEntity>()
        val dataPet = allCity.whereIn("animalCmgCl", listOf("가능", "가능(소형견)")).limit(10)
        dataPet.get().addOnSuccessListener { documents ->
            for (document in documents) {
                val dataList = document.toObject(HomeEntity::class.java)
//                _petItem.value?.add(dataList)
                petData.add(dataList)
//                Log.d("Home","item : $dataItem")
            }
//            if (isAdded){
//                val context = requireContext()
//                val petAdapter = PetAdapter(context, petItem)
//                binding.rvPetItem.adapter = petAdapter
//                binding.rvPetItem.layoutManager =
//                    LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
//                binding.rvPetItem.itemAnimator = null
//            }

        }
        return petData

    }


}