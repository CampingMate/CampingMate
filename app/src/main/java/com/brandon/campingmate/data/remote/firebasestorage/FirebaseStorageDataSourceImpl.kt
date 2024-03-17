package com.brandon.campingmate.data.remote.firebasestorage

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class FireBaseStorageDataSourceImpl(
    private val storage: FirebaseStorage
) : FireBaseStorageDataSource {
    override suspend fun uploadPostImage(imageUri: Uri): Result<String> = withContext(IO) {
        runCatching {
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            val fileName = "IMG_${timeStamp}_${UUID.randomUUID()}.jpg"
            val imageRef = storage.reference.child("postImages/$fileName")
            val uploadTask = imageRef.putFile(imageUri).await() // 코루틴을 사용해 업로드를 기다림
            val imageUrl = uploadTask.metadata?.reference?.downloadUrl?.await()?.toString()
            imageUrl ?: throw Exception("Failed to get download URL")
        }
    }
}
