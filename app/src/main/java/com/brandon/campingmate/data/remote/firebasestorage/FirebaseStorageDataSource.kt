package com.brandon.campingmate.data.remote.firebasestorage

import android.net.Uri

interface FireBaseStorageDataSource {
    suspend fun uploadPostImage(imageUris: Uri): Result<String>
}