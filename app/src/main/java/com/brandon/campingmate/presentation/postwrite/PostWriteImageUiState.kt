package com.brandon.campingmate.presentation.postwrite

import android.net.Uri

data class PostWriteImageUiState(
    val imageUris: List<Uri>,
    val title: String,
    val content: String,
    val isUploading: Boolean,
) {
    companion object {
        fun init() = PostWriteImageUiState(
            imageUris = emptyList(),
            title = "",
            content = "",
            isUploading = false,
        )
    }
}



