package com.brandon.campingmate

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import com.bumptech.glide.Glide
import kotlinx.coroutines.launch

class CampDetailViewModel : ViewModel() {

    private val _imageParam: MutableLiveData<HashMap<String, String>> = MutableLiveData()
    val imageParam: LiveData<HashMap<String, String>> get() = _imageParam
    private val _imageResult: MutableLiveData<MutableList<String>> = MutableLiveData()
    val imageResult: LiveData<MutableList<String>> get() = _imageResult
    fun setUpParkParameter(contentId: String) {
        val authKey =
            "wDP6fsVX3kKuaOD7OKrRHaAgPUNtxYUy387PNJRBAW/F6GUdZgv5LyyIAkVXED3leDg3aUD+TFIgBHWCgMBdzQ=="
        _imageParam.value = hashMapOf(
            "numOfRows" to "10",
            "pageNo" to "1",
            "MobileOS" to "AND",
            "MobileApp" to "CampingMate",
            "serviceKey" to authKey,
            "_type" to "json",
            "contentId" to contentId
        )
    }

    fun communicateNetWork(param: HashMap<String, String>?) {
        viewModelScope.launch {
            val responseData = param?.let { NetWorkClient.imageNetWork.getImage(it) }
            val items = responseData?.response?.campBody?.campImageItem!!
            val imageUrls = mutableListOf<String>()
            for(item in items){
                val imageUrl = item.imageurl
                if (imageUrl != null) {
                    imageUrls.add(imageUrl)
                }
            }
            _imageResult.value =imageUrls
        }
    }
}