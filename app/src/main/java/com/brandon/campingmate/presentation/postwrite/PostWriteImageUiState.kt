package com.brandon.campingmate.presentation.postwrite

import android.net.Uri

data class PostWriteImageUiState(
    val imageUris: List<Uri>,
) {
    companion object {
        fun init() = PostWriteImageUiState(
            imageUris = emptyList(),
        )
    }
}

sealed class PermissionState {
    object Granted : PermissionState()
    object Denied : PermissionState()
}




