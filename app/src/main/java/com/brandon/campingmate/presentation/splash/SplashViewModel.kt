package com.brandon.campingmate.presentation.splash

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.brandon.campingmate.domain.model.HomeEntity
import com.brandon.campingmate.network.firestore.FirebaseService.fireStoreDB
import com.brandon.campingmate.utils.UserCryptoUtils
import com.google.firebase.Firebase
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class SplashViewModel : ViewModel() {
    private val _allCityData: MutableLiveData<MutableList<HomeEntity>> = MutableLiveData(mutableListOf())
    val allCityData: LiveData<MutableList<HomeEntity>> get() = _allCityData

    private val _allThemeData: MutableLiveData<MutableList<HomeEntity>> = MutableLiveData(mutableListOf())
    val allThemeData: LiveData<MutableList<HomeEntity>> get() = _allThemeData

    private val _isLoading: MutableLiveData<Boolean> = MutableLiveData(true)
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _isGet: MutableStateFlow<Map<String, Boolean>> =
        MutableStateFlow(mapOf("city" to false, "theme" to false))
    val isGet: StateFlow<Map<String, Boolean>> get() = _isGet

    init {
        loadData()
//        loadKey()
    }

    private fun loadKey() {
        viewModelScope.launch {
            fireStoreDB.collection("keys")
                .document("9FwUIJRcHbLhrCYZJGfi").get().addOnSuccessListener {
                    val aesKey = it.getString("aesKey") ?: ""
                    UserCryptoUtils.AES_KEY = aesKey
                }.addOnFailureListener {
                    Timber.d("키 가져오기에 실패했습니다")
                }

        }
    }

    private fun loadData() {
        viewModelScope.launch {
            val db = Firebase.firestore
            val allCity: Query = db.collection("camps").limit(10)
            val allTheme: Query = allCity.whereNotEqualTo("themaEnvrnCl", listOf<String>()).limit(10)
            allCity.get().addOnSuccessListener { documents ->
                for (document in documents) {
                    val cityList = document.toObject(HomeEntity::class.java)
                    _allCityData.value?.add(cityList)
                }
                _isGet.value = _isGet.value.toMutableMap().apply {
                    this["city"] = true
                }
                setLoadingState()
            }
            allTheme.get().addOnSuccessListener { documents ->
                for (document in documents) {
                    val themeList = document.toObject(HomeEntity::class.java)
                    _allThemeData.value?.add(themeList)
                }
                _isGet.value = _isGet.value.toMutableMap().apply {
                    this["theme"] = true
                }
                setLoadingState()
            }
        }
    }

    private fun setLoadingState() {
        val allLoaded = _isGet.value.all { it.value }
        if (allLoaded) {
            _isLoading.value = false
        }
    }
}