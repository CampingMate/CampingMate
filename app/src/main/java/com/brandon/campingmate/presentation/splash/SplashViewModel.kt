package com.brandon.campingmate.presentation.splash

import android.content.Intent
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.brandon.campingmate.domain.model.CampEntity
import com.brandon.campingmate.presentation.main.MainActivity
import com.google.firebase.Firebase
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.launch

class SplashViewModel:ViewModel() {
//    private val _allCityData : MutableLiveData<MutableList<CampEntity>> = MutableLiveData()
//    val allCityData : LiveData<MutableList<CampEntity>> get() = _allCityData
//
//    private val _allThemeData : MutableLiveData<MutableList<CampEntity>> = MutableLiveData()
//    val allThemeData : LiveData<MutableList<CampEntity>> get() = _allThemeData

    private val _allCityData = mutableListOf<CampEntity>()
    val allCityData : MutableList<CampEntity> get() = _allCityData

    private val _allThemeData = mutableListOf<CampEntity>()
    val allThemeData : MutableList<CampEntity> get() = _allThemeData

    private val _isGet : MutableLiveData<Int> = MutableLiveData()
    val isGet : LiveData<Int> get() = _isGet

    fun loadData(){
        Log.d("Splash ViewModel","#csh getData start")
        viewModelScope.launch {
            _isGet.value=0
            val db = Firebase.firestore
            val allCity: Query = db.collection("camps").limit(10)
            val allTheme : Query = allCity.whereNotEqualTo("themaEnvrnCl", listOf<String>()).limit(10)
            allCity.get().addOnSuccessListener {documents ->
                for (document in documents) {
                    val cityList = document.toObject(CampEntity::class.java)
//                    _allCityData.value?.add(cityList)
                    _allCityData.add(cityList)
                }
                _isGet.value = _isGet.value!! + 1
            }
            allTheme.get().addOnSuccessListener {documents ->
                for (document in documents) {
                    val themeList = document.toObject(CampEntity::class.java)
//                    _allThemeData.value?.add(themeList)
                    _allThemeData.add(themeList)
                }
                _isGet.value = _isGet.value!! + 1
            }
        }
    }

    fun getData(){

    }
}