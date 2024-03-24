package com.brandon.campingmate.presentation.campdetail

import android.net.Uri
import android.util.Log
import android.view.View
import android.view.WindowId
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.brandon.campingmate.BuildConfig
import com.brandon.campingmate.R
import com.brandon.campingmate.domain.model.CampCommentEntity
import com.brandon.campingmate.domain.model.CampEntity
import com.brandon.campingmate.domain.model.Mart
import com.brandon.campingmate.network.retrofit.NetWorkClient
import com.firebase.geofire.GeoFireUtils
import com.firebase.geofire.GeoLocation
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.common.collect.Lists
import com.google.firebase.Firebase
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.storage
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.overlay.Align
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.util.MarkerIcons
import kotlinx.coroutines.launch
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class CampDetailViewModel : ViewModel() {

    private val _imageResult: MutableLiveData<MutableList<String>> = MutableLiveData()
    val imageResult: LiveData<MutableList<String>> get() = _imageResult
    private val _campEntity: MutableLiveData<CampEntity?> = MutableLiveData()
    val campEntity: LiveData<CampEntity?> get() = _campEntity
    private val _campComment: MutableLiveData<MutableList<CampCommentEntity>> = MutableLiveData()
    val campComment: LiveData<MutableList<CampCommentEntity>> get() = _campComment
    private val _checkLastComment: MutableLiveData<String?> = MutableLiveData()
    val checkLastComment: LiveData<String?> get() = _checkLastComment
    private val _commentCount: MutableLiveData<String?> = MutableLiveData()
    val commentCount: LiveData<String?> get() = _commentCount
    private lateinit var listenerRegistration: ListenerRegistration
    private val db = FirebaseFirestore.getInstance()
    val martMarker: LiveData<MutableList<Marker>> get() = _martMarker
    private val _martMarker: MutableLiveData<MutableList<Marker>> = MutableLiveData()
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

    private fun uploadComment(myId: String, myComment: CampCommentEntity) {
        val db = Firebase.firestore
        val campRef = db.collection("camps")
            .whereEqualTo("contentId", myId)
        campRef
            .get()
            .addOnSuccessListener { querySnapshot ->
                val document = querySnapshot.documents[0]
                val commentList = document.get("commentList") as? MutableList<Map<String, Any?>> ?: mutableListOf()
                val newComment = mutableMapOf(
                    "userId" to myComment.userId,
                    "userName" to myComment.userName,
                    "content" to myComment.content,
                    "date" to myComment.date,
                    "userProfile" to myComment.userProfile,
                )
                if(myComment.imageUrl.toString().isNotBlank()){
                    newComment["img"] = myComment.imageUrl.toString()
                } else{
                    newComment["img"] = ""
                }
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
                            val userId = comment["userId"] as? String ?: ""
                            val userName = comment["userName"] as? String ?: "유저"
                            val content = comment["content"] as? String ?: ""
                            val date = comment["date"] as? String ?: ""
                            val imageUrlString = comment["img"] as? String ?: ""
                            val imageUrl = Uri.parse(imageUrlString)
                            val userProfileString = comment["userProfile"] as? String ?: ""
                            val userProfileUrl = Uri.parse(userProfileString)
                            val data = CampCommentEntity(userId, userName, content, date, imageUrl, myId, userProfileUrl)
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

    fun checkComment(myId: String) {
        val db = Firebase.firestore
        var baseQuery: Query = db.collection("camps").whereEqualTo("contentId", myId)
        baseQuery.get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    _checkLastComment.value = "등록된 댓글이 없습니다. 첫 댓글을 작성해보세요!"
                } else {
                    val lastCommentList = documents.documents.lastOrNull()?.get("commentList") as? List<Map<*, *>>
                    if (!lastCommentList.isNullOrEmpty()) {
                        val lastComment = lastCommentList.lastOrNull()?.get("content") as? String
                        val commentSize = lastCommentList.size ?: 0
                        _commentCount.value = commentSize.toString()
                        if (lastComment != null) {
                            _checkLastComment.value = lastComment
                        } else {
                            _checkLastComment.value = "등록된 댓글이 없습니다. 첫 댓글을 작성해보세요!"
                        }
                    } else {
                        _checkLastComment.value = "등록된 댓글이 없습니다. 첫 댓글을 작성해보세요!"
                    }
                }
            }
            .addOnFailureListener { exception ->
            }
    }
    fun deleteComment(campId: String, comments: CampCommentEntity){
        val db = FirebaseFirestore.getInstance()
        val campRef = db.collection("camps").whereEqualTo("contentId", campId)
        campRef.get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    return@addOnSuccessListener
                }
                // 해당 캠핑장의 댓글 리스트를 가져와서 삭제할 댓글을 찾음
                val campDoc = documents.documents[0]
                val commentList = campDoc.get("commentList") as? MutableList<Map<String, Any?>> ?: mutableListOf()
                val iterator = commentList.iterator()

                // 삭제할 댓글을 찾아서 리스트에서 제거
                while (iterator.hasNext()) {
                    val comment = iterator.next()
                    val userId = comment["userId"] as String
                    val userName = comment["userName"] as String
                    val content = comment["content"] as String
                    val date = comment["date"] as String
                    val imageUrl = comment["img"] as String
                    val userProfile = comment["userProfile"] as String

                    if (userId == comments.userId &&
                        userName == comments.userName &&
                        content == comments.content &&
                        date == comments.date &&
                        imageUrl == comments.imageUrl.toString() &&
                        userProfile == comments.userProfile.toString()
                    ) {
                        iterator.remove()
                        break
                    }
                }
                // 업데이트된 댓글 리스트를 Firestore에 반영
                campDoc.reference.update("commentList", commentList)
                    .addOnSuccessListener {
                        Log.d("CampDetailActivity", "댓글 삭제 성공")
                    }
                    .addOnFailureListener { e ->
                        Log.e("CampDetailActivity", "댓글 삭제 실패: $e")
                    }
            }
            .addOnFailureListener { e ->
                Log.e("CampDetailActivity", "캠핑장 쿼리 실패: $e")
            }
    }

    fun bringUserData(
        userId: String,
        content: String,
        myImage: String,
        campId: String,
    ){
        val userDocRef = db.collection("users").document(userId)
        userDocRef
            .get()
            .addOnSuccessListener {
                val userName = it.get("nickName")
                val date =
                    SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(
                        Date()
                    )
                val userProfile = it.get("profileImage")
                if (myImage.isNotBlank()) {
                    val myImageUri = Uri.parse(myImage)
                    uploadImage(myImageUri) { imageUrl ->
                        val myComment = CampCommentEntity(
                            userId,
                            userName,
                            content,
                            date,
                            Uri.parse(imageUrl),
                            userId,
                            Uri.parse(userProfile.toString()),
                        )
                        uploadComment(campId, myComment)
                    }
                } else {
                    val myComment =
                        CampCommentEntity(
                            userId,
                            userName,
                            content,
                            date,
                            Uri.EMPTY,
                            userId,
                            Uri.parse(userProfile.toString())
                        )
                    uploadComment(campId, myComment)
                }
            }
    }

    fun callMart(lat :Double ,lon :Double){
        val db = Firebase.firestore
        val center = GeoLocation(lat, lon)
        // 반경 15km
        val radiusInM = 15.0 * 1000.0

        val bounds = GeoFireUtils.getGeoHashQueryBounds(center, radiusInM)
        val tasks: MutableList<Task<QuerySnapshot>> = ArrayList()
        for (b in bounds) {
            val q = db.collection("mart")
                .orderBy("geohash")
                .limit(12)
                .startAt(b.startHash)
                .endAt(b.endHash)
            tasks.add(q.get())
        }
        Tasks.whenAllComplete(tasks)
            .addOnCompleteListener {
                val matchingDocs: MutableList<DocumentSnapshot> = ArrayList()
                for(task in tasks){
                    val snap = task.result
                    for(doc in snap!!.documents){

                        val lat = doc.getDouble("latitude")!!
                        val lng = doc.getDouble("longitude")!!

                        val docLocation = GeoLocation(lat, lng)
                        val distanceInM = GeoFireUtils.getDistanceBetween(docLocation, center)
                        if (distanceInM <= radiusInM) {
                            matchingDocs.add(doc)
                        }
                    }
                }
                _martMarker.value = mutableListOf()
                val markers = mutableListOf<Marker>()
                for(doc in matchingDocs){
                    if(doc != null){
                        val mart = doc.toObject(Mart::class.java)
                       // for (mart in marts!!){
                            val marker = Marker()
                            marker.captionText = mart?.name.toString()
                            marker.icon = MarkerIcons.BLUE
                            if(mart?.latitude == null || mart.longitude == null){
                                continue
                            }
                            marker.position = LatLng(mart.latitude,mart.longitude)
                            marker.captionRequestedWidth = 400
                            marker.setCaptionAligns(Align.Top)
                            marker.captionOffset = 5
                            marker.captionTextSize = 16f
                            markers.add(marker)
                       // }
                    }
                }
                _martMarker.value = markers
                Timber.tag("check").d("마트 정보들 = ${markers}")

            }
            .addOnFailureListener { exception ->
                Log.e("check", "Error: ", exception)
            }
    }
}