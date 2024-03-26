package com.brandon.campingmate.data.remote.firebasestorage

import android.net.Uri

interface FireBaseStorageDataSource {
    suspend fun uploadPostImage(imageUri: Uri): Result<String>
    suspend fun deletePostImage(imageUrl: String)
}