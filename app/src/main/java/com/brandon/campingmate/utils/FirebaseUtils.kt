package com.brandon.campingmate.utils

import android.net.Uri
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.storage.storage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
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

fun profileImgUpload(imageURI: Uri, userID: String, onComplete: (Boolean, Uri?) -> Unit) {
    val storage = Firebase.storage
    val storageRef = storage.getReference("profileImage").child(userID)

    CoroutineScope(Dispatchers.IO).launch {
        try {
            val uploadTask = if (imageURI.toString().startsWith("http")) {
                // 인터넷 URL의 경우 putStream 사용
                val stream = URL(imageURI.toString()).openStream()
                storageRef.putStream(stream).await()
            } else {
                // 로컬 파일 업로드 시 putFile 사용
                storageRef.putFile(imageURI).await()
            }

            val uri = storageRef.downloadUrl.await()
            withContext(Dispatchers.Main) {
                onComplete(true, uri)
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                onComplete(false, null)
            }
        }
    }
}
