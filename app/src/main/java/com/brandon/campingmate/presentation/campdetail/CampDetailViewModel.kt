package com.brandon.campingmate.presentation.campdetail

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.brandon.campingmate.BuildConfig
import com.brandon.campingmate.R
import com.brandon.campingmate.network.retrofit.NetWorkClient
import kotlinx.coroutines.launch

class CampDetailViewModel : ViewModel() {

    private val _imageResult: MutableLiveData<MutableList<String>> = MutableLiveData()
    val imageResult: LiveData<MutableList<String>> get() = _imageResult
    fun setUpParkParameter(contentId: String) {
        val authKey = BuildConfig.camp_data_key
        communicateNetWork(hashMapOf(
            "numOfRows" to "10",
            "pageNo" to "1",
            "MobileOS" to "AND",
            "MobileApp" to "CampingMate",
            "serviceKey" to authKey,
            "_type" to "json",
            "contentId" to contentId
        ))
    }

    private fun communicateNetWork(param: HashMap<String, String>?) {
        viewModelScope.launch {
            val responseData = param?.let { NetWorkClient.imageNetWork.getImage(it) }
            val items = responseData?.response?.campBody?.campImageItems?.campImageItem
            val imageUrls = mutableListOf<String>()
            if (items != null) {
                for(item in items){
                    val imageUrl = item.imageUrl
                    if (imageUrl != null) {
                        imageUrls.add(imageUrl)
                    }
                }
            }
            if(imageUrls.isEmpty()){
                imageUrls.add("android.resource://${BuildConfig.APPLICATION_ID}/${R.drawable.default_camping}")
            }
            Log.d("campDetailViewModel", "$imageUrls")
            _imageResult.value = imageUrls
        }
    }
}