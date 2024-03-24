package com.brandon.campingmate.presentation.splash

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.brandon.campingmate.domain.model.HomeEntity
import com.google.firebase.Firebase
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SplashViewModel:ViewModel() {
    private val _allCityData : MutableLiveData<MutableList<HomeEntity>> = MutableLiveData(mutableListOf())
    val allCityData : LiveData<MutableList<HomeEntity>> get() = _allCityData

    private val _allThemeData : MutableLiveData<MutableList<HomeEntity>> = MutableLiveData(mutableListOf())
    val allThemeData : LiveData<MutableList<HomeEntity>> get() = _allThemeData

//    private val _allCityData = mutableListOf<HomeEntity>()
//    val allCityData : MutableList<HomeEntity> get() = _allCityData
//
//    private val _allThemeData = mutableListOf<HomeEntity>()
//    val allThemeData : MutableList<HomeEntity> get() = _allThemeData

//    private val _isGet : MutableLiveData<Int> = MutableLiveData()
//    val isGet : LiveData<Int> get() = _isGet

    private val _isLoading: MutableLiveData<Boolean> = MutableLiveData(true)
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _isGet: MutableStateFlow<Map<String, Boolean>> =
        MutableStateFlow(mapOf("city" to false, "theme" to false))
    val isGet: StateFlow<Map<String, Boolean>> get() = _isGet

    init {
        loadData()
    }

    fun loadData() {
        Log.d("Splash ViewModel", "#csh loadData start")
        viewModelScope.launch {
            val db = Firebase.firestore
            val allCity: Query = db.collection("camps").limit(10)
//            val allCity: Query = db.collection("reviewTest")
//            val allCity: Query = db.collection("reviewTest_empty")
            val allTheme: Query = allCity.whereNotEqualTo("themaEnvrnCl", listOf<String>()).limit(10)
            allCity.get().addOnSuccessListener { documents ->
                for (document in documents) {
                    val cityList = document.toObject(HomeEntity::class.java)
                    _allCityData.value?.add(cityList)
//                    _allCityData.add(cityList)
                }
                Log.d("Splash ViewModel ","#csh _allCityData = ${_allCityData.value}")
                _isGet.value = _isGet.value.toMutableMap().apply {
                    this["city"] = true
                }
                setLoadingState()
            }
            allTheme.get().addOnSuccessListener {documents ->
                for (document in documents) {
                    val themeList = document.toObject(HomeEntity::class.java)
                    _allThemeData.value?.add(themeList)
//                    _allThemeData.add(themeList)
                }
                Log.d("Splash ViewModel ","#csh _allThemeData = ${_allThemeData.value}")
                _isGet.value = _isGet.value.toMutableMap().apply {
                    this["theme"] = true
                }
                setLoadingState()
            }
        }
    }

    fun setLoadingState(){
        Log.d("Splash ViewModel ","#csh setLoadingState")
        val allLoaded = _isGet.value.all{it.value}
        if(allLoaded){
            _isLoading.value=false
        }
        Log.d("Splash ViewModel ","#csh setLoadingState _isLoading.value : ${_isLoading.value}")
    }
}