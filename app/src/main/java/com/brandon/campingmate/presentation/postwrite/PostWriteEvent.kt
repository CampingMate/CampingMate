package com.brandon.campingmate.presentation.postwrite

import android.net.Uri

sealed class PostWriteEvent {

    data class PostUploadSuccess(
        val postId: String
    ) : PostWriteEvent()

    // TODO View 에서 관찰하지 않는 Event..
    data class UploadPost(
        val title: String,
        val content: String,
    ) : PostWriteEvent()

    data class UploadPostImages(
        val imageUris: List<Uri>,
    ) : PostWriteEvent()

}