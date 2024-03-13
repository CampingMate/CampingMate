package com.brandon.campingmate.presentation.campdetail

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.brandon.campingmate.BuildConfig
import com.brandon.campingmate.R
import com.brandon.campingmate.domain.model.CampEntity
import com.brandon.campingmate.network.retrofit.NetWorkClient
import com.google.firebase.Firebase
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.launch

class CampDetailViewModel : ViewModel() {

    private val _imageResult: MutableLiveData<MutableList<String>> = MutableLiveData()
    val imageResult: LiveData<MutableList<String>> get() = _imageResult
    private val _campEntity: MutableLiveData<CampEntity?> = MutableLiveData()
    val campEntity: LiveData<CampEntity?> get() = _campEntity
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

    fun callIdData(id: String) {
        val db = Firebase.firestore
        var baseQuery: Query = db.collection("camps").whereEqualTo("contentId", id)
        baseQuery.get()
            .addOnSuccessListener { documents ->
                if(!documents.isEmpty){
                    val campEntity = documents.documents[0].toObject(CampEntity::class.java)
                    _campEntity.value = campEntity
                }
            }
            .addOnFailureListener { exception ->
                Log.e("CampDetailViewModel", "Error: ", exception)
            }
    }
}