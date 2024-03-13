package com.brandon.campingmate.presentation.postwrite

import android.net.Uri

data class PostWriteUiState(
    val imageUris: List<Uri>,
) {
    companion object {
        fun init() = PostWriteUiState(
            imageUris = emptyList(),
        )
    }
}