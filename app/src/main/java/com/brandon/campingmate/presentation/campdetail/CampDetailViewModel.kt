package com.brandon.campingmate.presentation.campdetail

import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
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
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.storage
import kotlinx.coroutines.launch
import java.util.UUID

class CampDetailViewModel : ViewModel() {

    private val _imageResult: MutableLiveData<MutableList<String>> = MutableLiveData()
    val imageResult: LiveData<MutableList<String>> get() = _imageResult
    private val _campEntity: MutableLiveData<CampEntity?> = MutableLiveData()
    val campEntity: LiveData<CampEntity?> get() = _campEntity
    private val _campComment: MutableLiveData<MutableList<CampCommentEntity>> = MutableLiveData()
    val campComment: LiveData<MutableList<CampCommentEntity>> get() = _campComment
    private lateinit var listenerRegistration: ListenerRegistration
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

    fun uploadComment(myId: String, myComment: CampCommentEntity) {
        val db = Firebase.firestore
        val campRef = db.collection("camps")
            .whereEqualTo("contentId", myId)
        campRef
            .get()
            .addOnSuccessListener { querySnapshot ->
                val document = querySnapshot.documents[0]
                val commentList = document.get("commentList") as? MutableList<Map<String, Any?>> ?: mutableListOf()
                val newComment = mapOf(
                    "userId" to myComment.userId,
                    "userName" to myComment.userName,
                    "content" to myComment.content,
                    "date" to myComment.date,
                    "img" to myComment.imageUrl.toString()
                )
                commentList.add(newComment)

                document.reference.update("commentList", commentList)
                    .addOnSuccessListener {
                        Log.d("CampDetailViewModel", "댓글 업로드 완료")
                    }
                    .addOnFailureListener { e ->
                        Log.d("CampDetailViewModel", "댓글 업로드 실패: $e")
                    }
            }
            .addOnFailureListener { e ->
                Log.d("CampDetailViewModel", "캠핑장 쿼리중 오류 발생 : $e")
            }
    }

    fun registerRealtimeUpdates(myId: String) {
        listenerRegistration = FirebaseFirestore.getInstance()
            .collection("camps")
            .whereEqualTo("contentId", myId)  // 필터링 조건에 맞게 설정
            .addSnapshotListener { snapshot, exception ->
                if (exception != null) {
                    Log.e("CampDetailViewModel", "Listen failed", exception)
                    return@addSnapshotListener
                }

                if (snapshot != null && !snapshot.isEmpty) {
                    // 실시간 업데이트가 발생했을 때 RecyclerView에 반영
                    val comments = mutableListOf<CampCommentEntity>()
                    for (doc in snapshot) {
                        val commentList = doc.get("commentList") as? MutableList<Map<String, Any?>> ?: mutableListOf()
                        for (comment in commentList) {
                            val userId = comment["userId"] as String
                            val userName = comment["userName"] as String
                            val content = comment["content"] as String
                            val date = comment["date"] as String
                            val imageUrlString = comment["img"] as String
                            val imageUrl = Uri.parse(imageUrlString)
                            val data = CampCommentEntity(userId, userName, content, date, imageUrl)
                            comments.add(data)
                        }
                    }
                    // RecyclerView에 데이터를 업데이트
                    _campComment.value = comments
                } else {
                    Log.d("CampDetailViewModel", "No such document")
                }
            }
    }

    override fun onCleared() {
        super.onCleared()
        listenerRegistration.remove()
    }

    fun uploadImage(selectedImageUri: Uri?, callback: (String) -> Unit) {
        if (selectedImageUri != null) {
            val storage = Firebase.storage
            val storageRef = storage.reference

            val imageFileName = "${UUID.randomUUID()}.jpg"
            val campCommentRef = storageRef.child("campComment/$imageFileName")

            // 이미지 업로드
            campCommentRef.putFile(selectedImageUri)
                .addOnSuccessListener { taskSnapshot ->
                    // 업로드 성공 시 이미지 다운로드 URL 가져오기
                    campCommentRef.downloadUrl.addOnSuccessListener { uri ->
                        // 다운로드 URL을 콜백 함수를 통해 전달
                        val imageUrl = uri.toString()
                        callback(imageUrl) // 콜백 함수 호출하여 Firestore에 저장
                    }.addOnFailureListener { exception ->
                        // 이미지 다운로드 URL을 가져오지 못한 경우 처리
                        Log.e("FirebaseStorage", "Failed to get download URL: $exception")
                    }
                }
                .addOnFailureListener { exception ->
                    // 이미지 업로드 실패 시 처리
                    Log.e("FirebaseStorage", "Failed to upload image: $exception")
                }
        } else {
            // 이미지가 선택되지 않은 경우에 대한 처리
        }
    }
}