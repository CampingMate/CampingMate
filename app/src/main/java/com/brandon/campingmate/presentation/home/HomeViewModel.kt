package com.brandon.campingmate.presentation.home

import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.brandon.campingmate.BuildConfig
import com.brandon.campingmate.domain.model.HolidayItem
import com.brandon.campingmate.domain.model.HomeEntity
import com.brandon.campingmate.network.retrofit.NetWorkClient
import com.brandon.campingmate.presentation.home.adapter.HomeAdapter
import com.brandon.campingmate.presentation.home.adapter.PetAdapter
import com.google.firebase.Firebase
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

class HomeViewModel: ViewModel() {
    private val _reviewItem  : MutableLiveData<MutableList<HomeEntity>> = MutableLiveData()
    val reviewItem : LiveData<MutableList<HomeEntity>> get() = _reviewItem

    private val _petItem : MutableLiveData<MutableList<HomeEntity?>> = MutableLiveData()
    val petItem : MutableLiveData<MutableList<HomeEntity?>> get() = _petItem

    private val _districtItem : MutableLiveData<MutableList<HomeEntity>?> = MutableLiveData()
    val districtItem : MutableLiveData<MutableList<HomeEntity>?> get() = _districtItem

    private val _holidayItems = MutableLiveData<MutableList<HolidayItem?>>()
    val holidayItems: MutableLiveData<MutableList<HolidayItem?>> = _holidayItems

    private val db = Firebase.firestore
    private val allCity: Query = db.collection("camps")

    fun loadReviewItem(){
        viewModelScope.launch {
            _reviewItem.value?.clear()
            val db = Firebase.firestore
            val allCamps: Query = db.collection("camps").whereNotEqualTo("commentList", listOf<String>())
//            val allCamps: Query = db.collection("reviewTest").whereNotEqualTo("commentList", listOf<String>())
//            val allCamps: Query = db.collection("reviewTest_empty").whereNotEqualTo("commentList", listOf<String>())
            var allData : MutableList<HomeEntity> = mutableListOf()
            allCamps.get().addOnSuccessListener { documents ->
                Log.d("HomeViewModel","#csh Success")
                for (document in documents) {
                    val allList = document.toObject(HomeEntity::class.java)
                    allData.add(allList)
                }
                Log.d("HomeViewModel","allData : $allData")
                _reviewItem.value=allData
            }
        }
    }

    fun loadPetItem(){
        viewModelScope.launch {
            _petItem.value?.clear()
            val dataPet = allCity.whereIn("animalCmgCl", listOf("가능", "가능(소형견)")).limit(10)
            dataPet.get().addOnSuccessListener { documents ->
                val petList = mutableListOf<HomeEntity?>()
                for(document in documents){
                    val dataList = document.toObject(HomeEntity::class.java)
                    petList.add(dataList)
                }
                _petItem.postValue(petList)
            }
        }

//        //반려동물
//        val petData = mutableListOf<HomeEntity>()
//        val dataPet = allCity.whereIn("animalCmgCl", listOf("가능", "가능(소형견)")).limit(10)
//        dataPet.get().addOnSuccessListener { documents ->
//            for (document in documents) {
//                val dataList = document.toObject(HomeEntity::class.java)
////                _petItem.value?.add(dataList)
//                petData.add(dataList)
////                Log.d("Home","item : $dataItem")
//            }
////            if (isAdded){
////                val context = requireContext()
////                val petAdapter = PetAdapter(context, petItem)
////                binding.rvPetItem.adapter = petAdapter
////                binding.rvPetItem.layoutManager =
////                    LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
////                binding.rvPetItem.itemAnimator = null
////            }
//
//        }

    }

    fun loadDistrictItem(data: String) {
        Log.d("Home", "1. data=$data")
        viewModelScope.launch {
            _districtItem.value?.clear()
            val result = when (data) {
                "Capital" -> allCity.whereIn("doNm", listOf("서울시", "경기도", "인천시")).limit(10)
                "Chungcheong" -> allCity.whereIn("doNm", listOf("충청남도", "충청북도", "세종시", "대전시")).limit(10)
                "Gyeongsang" -> allCity.whereIn("doNm", listOf("경상북도", "경상남도", "부산시", "울산시", "대구시")).limit(10)
                "Jeolla" -> allCity.whereIn("doNm", listOf("전라북도", "전라남도", "광주시", "제주도")).limit(10)
                "Gangwon" -> allCity.whereIn("doNm", listOf("강원도")).limit(10)
                else -> {
                    throw IllegalArgumentException("Invalid data value: $data")
                }
            }
            result.get().addOnSuccessListener { documents ->
                val districtList : MutableList<HomeEntity> = mutableListOf()
                for (document in documents) {
                    val dataList = document.toObject(HomeEntity::class.java)
                    districtList.add(dataList)
//                Log.d("Home","item : $dataItem")
                }
                _districtItem.value = districtList
                Log.d("Home", "districtItem:$districtItem")
//            if(isAdded){
//                val context = requireContext()
//                districtAdapter = HomeAdapter(context, districtItem)
//                binding.rvDistrictItem.adapter = districtAdapter
//                binding.rvDistrictItem.layoutManager =
//                    GridLayoutManager(context, 2, GridLayoutManager.HORIZONTAL, false)
//                binding.rvDistrictItem.itemAnimator = null
//            }

            }.addOnFailureListener { exception ->
                Log.d("Home", "districtItem fail")
            }
        }
    }

    fun loadHolidayData(){
        val holidayList = mutableListOf<HolidayItem?>()
        val nowDate = LocalDate.now()
        val formatDate = nowDate.format(DateTimeFormatter.BASIC_ISO_DATE)
        val parse = formatDate.toString().substring(0,4)
        viewModelScope.launch {
            val data = communicateNetWork(parse,100)
            val dataSort = data.sortedBy { it.locdate }
            val dataFilter = dataSort?.filter { it.locdate != null && it.locdate >= formatDate.toInt()}

            if(dataFilter?.size!!<5) {
                holidayList.addAll(dataFilter)
                val addItem = communicateNetWork("${parse.toInt() + 1}", 5 - dataFilter.size!!)
                holidayList.addAll(addItem)
            }else{
                holidayList.addAll(dataFilter.take(5))
            }
            holidayList.forEach { it ->
                var dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd")
                var dDay = ChronoUnit.DAYS.between(LocalDate.parse(formatDate, dateFormatter), LocalDate.parse(it?.locdate.toString(), dateFormatter))
                it?.dDay = dDay
            }
            _holidayItems.postValue(holidayList)
        }
    }

    private suspend fun communicateNetWork(year: String, num: Int): MutableList<HolidayItem> {
        try {
            val authKey = BuildConfig.camp_data_key
            val date = LocalDate.now()
            val dateFormat = date.format(DateTimeFormatter.BASIC_ISO_DATE)
//            val parseYear = dateFormat.substring(0,3)
//        Log.d("Home", "parseYear=${parseYear}")
            val responseData = NetWorkClient.holidayNetWork.getRestDeInfo(authKey, year, "json", num)
            val holidayInfo = responseData.response.body.items.item
            Timber.tag("Home").d("holidayInfo=%s", responseData)
            return holidayInfo
        } catch (e: Exception) {
            Timber.tag("HOLIDAY").d("Error: $e")
            return mutableListOf()
        }
    }


}