package com.brandon.campingmate.utils

import android.net.Uri
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * firestore 의 경우
 * kotlinx-coroutines-play-services 에서 제공하는 await를 사용할 수 있지만
 * 다음과 같은 이유로 Deferred 로 통합하여 사용한다
 * 1. 다양한 비동기 소스의 일관된 처리
 * 2. 코루틴 기반의 프로젝트 아키텍처
 */

fun Timestamp?.toFormattedString(): String {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    return this?.toDate()?.let { dateFormat.format(it) } ?: ""
}

fun profileImgUpload(imageURI: Uri, userID: String) {
    val storage = Firebase.storage
    val storageRef = storage.getReference("profileImage")

    if (imageURI.toString().startsWith("http")) {
        GlobalScope.launch(Dispatchers.IO) {
            //인터넷URL의 경우 putStream사용.
            storageRef.child(userID).putStream(URL(imageURI.toString()).openStream())
        }
    } else {
        //로컬 파일 업로드 시 putFile 사용
        storageRef.child(userID).putFile(imageURI)
    }

}
